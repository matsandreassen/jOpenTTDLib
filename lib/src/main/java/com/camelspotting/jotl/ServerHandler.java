/*
 * Copyright (c) 2007 Mats Andreassen <matsa@pvv.ntnu.no>
 * Copyright (c) 2007 Eivind Brandth Smedseng <eivbsmed@gmail.com>
 * All rights reserved.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.camelspotting.jotl;

import com.camelspotting.jotl.domain.ServerDetails;
import com.camelspotting.jotl.domain.ClientsDetails;
import com.camelspotting.jotl.domain.Company;
import com.camelspotting.jotl.domain.Game;
import com.camelspotting.jotl.exceptions.JOTLException;
import com.camelspotting.jotl.domain.Client;
import com.camelspotting.jotl.event.OpenTTDEvent;
import com.camelspotting.jotl.event.OpenTTDEventType;
import com.camelspotting.jotl.event.OpenTTDListener;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is one of the primary access points for any user of the library. It's
 * for monitoring an ongoing OpenTTD game. It can do updates continously or
 * manually.
 *
 * @author Mats Andreassen
 * @version 1.0
 */
public class ServerHandler
{

    private static final Logger LOG = LoggerFactory.getLogger( ServerHandler.class );
    /**
     * The listeners that are to be notified of events
     */
    private List<OpenTTDListener> listeners;
    /**
     * The update interval in milliseconds
     */
    private int updateInterval;
    /**
     * The previous game for remembering statistics
     */
    private List<Game> earlierGames;
    /**
     * The previous update
     */
    private Game lastUpdate;
    /**
     * The current update
     */
    private Game currentUpdate;
    /**
     * Has electric rail been made available?
     */
    private boolean electricRail;
    /**
     * Has monorail been made available?
     */
    private boolean monoRail;
    /**
     * Has maglev been made available?
     */
    private boolean maglev;
    /**
     * The newest companies
     */
    private Company[] newbies;
    /**
     * This counter is for checking whether a game is paused
     */
    private int pauseCounter;
    /**
     * This counter is for checking whether a game is unpaused
     */
    private int unpauseCounter;
    /**
     * The game is paused
     */
    private boolean paused = false;
    private final GameQuerier gameQuerier;

    /**
     * Main constructor for creating the server handler. Be aware that if you
     * supply no initial listeners and still tell it to update once immidiately
     * no one will receive any events. An OpenTTD-server runs by default on port
     * 3979. Regardless of what you supply as an update interval it will not
     * start and continous sequence without a call to start(). NOTE: According
     * to the OpenTTD-code a day is about 2 seconds if the machine running it is
     * able to do run it normally.
     *
     * @param host the server to monitor
     * @param localPort the local port to use
     * @param remotePort the remote port to use
     * @param updateInterval update interval in milliseconds, 0 or less for
     * manual
     * @param updateNow do an update at once?
     * @param otls any initial listeners?
     * @see #start()
     */
    public ServerHandler( GameQuerier gameQuerier, int updateInterval, boolean updateNow, OpenTTDListener... otls ) throws JOTLException
    {
        this.updateInterval = updateInterval;
        addOpenTTDListeners( otls ); // This will do nothing if otl is null
        if ( updateNow )
        {
            update();
        }
        this.gameQuerier = gameQuerier;
    }

    /**
     * Getter for easily finding out whether the game is paused or not. This
     * variable might contain old data if the {@link ServerHandler} has stopped
     * querying the OpenTTD server.
     *
     * @return whether or not the server is paused
     */
    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Getter for the monitored server's hostname.
     *
     * @return the server hostname
     */
    public String getServerName()
    {
        return gameQuerier.getServer().getAddress().getCanonicalHostName();
    }

    /**
     * This method is for altering the update interval. If the new update
     * interval is set to less than or equal to 0 this will as a direct result
     * terminate the automatic updating. Be aware that while the method can stop
     * the updating it will never start it. A separate call to start() must be
     * done to achieve this. NOTE: According to the OpenTTD-code a day is about
     * 2 seconds if the machine running it is able to do run it normally so 2000
     * is the lower boundary for this setting.
     *
     * @param ms the number of milliseconds to set
     * @see #start()
     * @see #stop()
     */
    public void setUpdateInterval( int ms )
    {
        if ( ms < 2000 )
        {
            this.updateInterval = 2000;
        }
        else
        {
            this.updateInterval = ms;
        }
    }

    /**
     * This method is for starting the continous updating. This will either
     * start it with an initial update interval set to the lower boundary of
     * 2000 if the interval is still set to -1.
     */
    public void start()
    {
        if ( updateInterval <= 0 )
        {
            setUpdateInterval( 2000 );
        }
        new Thread()
        {
            private int timeouts = 0;
            private int maxTimouts = 12;
            private boolean running = true;
            private boolean lastUpdateFailed = false;
            private int slowInterval = 5000;

            @Override
            public void run()
            {
                while ( running )
                {
                    try
                    {
                        if ( updateInterval > 0 )
                        {
                            if ( lastUpdateFailed )
                            {
                                updateSlow();
                                LOG.debug( "5000 millisecond update rate fallback succeeded. Returning to " + updateInterval + "." );
                                lastUpdateFailed = false;
                                timeouts = 0;
                            }
                            else
                            {
                                updateNormal();
                            }
                        }
                        else
                        {
                            running = false;
                        }
                    }
                    catch ( InterruptedException ex )
                    {
                    }
                    catch ( JOTLException otx )
                    {
                        if ( otx.getMessage().contains( "timeout" ) )
                        {
                            if ( ++timeouts < maxTimouts )
                            {
                                LOG.debug( "Timeout {} occured. A new game may be loading.", timeouts );
                                lastUpdateFailed = true;
                            }
                            else
                            {
                                LOG.debug( "The maximum number of timeouts in a row({}) has been reached. Server may have gone down.", maxTimouts );
                                setUpdateInterval( -1 );
                                fireEvent( new OpenTTDEvent( OpenTTDEventType.LOST_CONNECTION ) );
                                LOG.debug( "Manual update mode has been set and any and all listeners have been notified." );
                            }
                        }
                    }
                }
                LOG.debug( "Update thread death." );
            }

            private void updateNormal() throws InterruptedException, JOTLException
            {
                Thread.sleep( updateInterval );
                update();
            }

            private void updateSlow() throws InterruptedException, JOTLException
            {
                Thread.sleep( slowInterval );
                update();
            }
        }.start();
    }

    /**
     * This method overrides the setUpdateInterval-method that refuses to lower
     * the interval to lower than 2000 and sets it to -1. This will cause the
     * update thread to terminate.
     *
     * @see #setUpdateInterval(int ms)
     * @see #start()
     */
    public void stop()
    {
        updateInterval = -1;
    }

    /**
     * This method is public so that any API-user might update manually. also
     * for the automatic update process. Note that this is the same method
     * invoked internally during the automatic update process. After updating
     * the data is checked to so if anything notable has happened since the last
     * update.
     *
     * @see OpenTTDEvent
     */
    public final synchronized void update() throws JOTLException
    {
        currentUpdate = gameQuerier.getAllInformation();
        checkForEvents();
    }

    /**
     * This method is for checking if anything interesting has happened since
     * last time this method was called.
     */
    private void checkForEvents()
    {
        List<OpenTTDEvent> evts = new ArrayList<OpenTTDEvent>();
        // Let's see if anything interesting has happened since last time
        LOG.debug( "Let's check this new update for changes." );
        boolean isSameGame = representsSameGame( lastUpdate, currentUpdate );
        List<Company> lastList = ( lastUpdate != null ? lastUpdate.getServerInfo().getCompanies() : null );
        List<Company> curList = currentUpdate.getServerInfo().getCompanies();

        // Has a new game started?
        OpenTTDEvent newgame = checkForNewGame( isSameGame, currentUpdate );
        if ( newgame != null )
        {
            int diff = currentUpdate.getClientsInfo().getGameDate().compareTo( currentUpdate.getClientsInfo().getStartDate() );
            if ( diff < 0 )
            {
                // This is not a "new" game, but a game in progress
                LOG.debug( "I found out a 'new game' was acually a game in progress." );
                newgame = new OpenTTDEvent( OpenTTDEventType.GAME_IN_PROGRESS, Integer.valueOf( currentUpdate.getClientsInfo().getGameDate().getYear() ) );
            }
        }
        OpenTTDEvent endgame = checkForEndGame( isSameGame );

        // If a game ended we have to archive it
        if ( endgame != null )
        {
            archiveGame();
        }

        if ( newgame != null )
        {
            // This is a new game
            if ( endgame != null )
            {
                evts.add( endgame );
            }
            evts.add( newgame );
            // Did any companies start before we discovered the new game?
            // Did any clients connect before we discovered the new game?
            OpenTTDEvent newcomers = checkForNewcomers( curList );
            if ( newcomers != null )
            {
                evts.add( newcomers );
                // If more than 1 has started someone is in the lead.
                if ( newbies.length > 1 )
                {
                    evts.add( new OpenTTDEvent( OpenTTDEventType.NEW_LEADER, curList.get( 0 ) ) );
                }
            }
        }
        else
        {
            // A new game didn't start but we still have to see what
            // happens.
            // Has any type of new rail been made available?
            OpenTTDEvent railEvent = checkForNewRail( currentUpdate.getClientsInfo().getGameDate().getYear() );

            // Do we have a new leader?
            OpenTTDEvent newLeader = checkForNewLeader( lastList, curList );
            // Do we have any new companies?
            OpenTTDEvent newcomers = checkForNewcomers( lastList, curList );
            // Has the game been paused/unpaused?
            OpenTTDEvent pause = checkForPauseUnpaused( isSameGame );
            // Has any company been removed
            List<OpenTTDEvent> removedEvents = checkForRemovedCompanies( isSameGame, lastUpdate.getServerInfo().getCompanies(), currentUpdate.getServerInfo().getCompanies() );

            // Add the events to the queue
            if ( railEvent != null )
            {
                evts.add( railEvent );
            }
            if ( newLeader != null )
            {
                evts.add( newLeader );
            }
            if ( newcomers != null )
            {
                evts.add( newcomers );
            }
            if ( removedEvents != null )
            {
                evts.addAll( removedEvents );
            }
            if ( pause != null )
            {
                evts.add( pause );
            }
        }

        // This check will prevent the scoped code from throwing any exceptions.
        // As of version OpenTTD 0.6.2, all client info has been dropped
        // from the SERVER_DETAILED_INFO-packet so the methods for
        // getting clients/spectators now throws exception if UDP-version
        // is higher than 4.
        if ( lastUpdate.getServerInfo().getVersion() >= 5 )
        {
            if ( lastUpdate != null )
            {
                // Did any clients join/leave?
                List<Client> oldClients = lastUpdate.getServerInfo().getClients();
                List<Client> newClients = currentUpdate.getServerInfo().getClients();
                List<OpenTTDEvent> joinEvents = checkForClientsJoined( oldClients, newClients );
                List<OpenTTDEvent> leftEvents = checkForClientsLeft( oldClients, newClients );

                evts.addAll( joinEvents );
                evts.addAll( leftEvents );
            }
            else
            {
                List<Client> newClients = currentUpdate.getServerInfo().getClients();
                if ( newClients.size() > 0 )
                {
                    evts.add( new OpenTTDEvent( OpenTTDEventType.CLIENT_JOIN, (Object[]) newClients.toArray( new Client[ newClients.size() ] ) ) );
                }
            }
        }

        if ( evts.size() > 0 )
        {
            fireEvents( evts );
        }
        else
        {
            LOG.debug( "No new events were found." );
        }
        doFinalBookkeeping();
    }

    private int compareDates( Game lastUpdate, Game currentUpdate )
    {
        return lastUpdate.getClientsInfo().getGameDate().compareTo( currentUpdate.getClientsInfo().getGameDate() );
    }

    /**
     * This method will determine whether any {@link Company}'s have been
     * removed. Normally these {@link OpenTTDEvent}s will only be generated if a
     * server administrator removes {@link Company} by force.
     *
     * @param isSameGame whether this is the same game as the last query or not
     * @param oldList the {@link List} of {@link Company}'s from last query
     * @param newList the {@link List} of {@link Company}'s from the current
     * query
     * @return a {@link List} of {@link OpenTTDEvent}s of type COMPANY_REMOVED
     */
    private List<OpenTTDEvent> checkForRemovedCompanies( boolean isSameGame, List<Company> oldList, List<Company> newList )
    {
        List<OpenTTDEvent> evts = new ArrayList<OpenTTDEvent>();
        if ( isSameGame && ( oldList.size() > newList.size() ) )
        {
            for ( Company com : oldList )
            {
                if ( !newList.contains( com ) )
                {
                    evts.add( new OpenTTDEvent( OpenTTDEventType.COMPANY_REMOVED, com ) );
                }
            }
        }
        return evts;
    }

    /**
     * This method will compare the client-lists and determine whether any
     * clients have left.
     *
     * @param oldList the {@link List} of {@link Client}s from the last query
     * @param newList the {@link List} of {@link Client}s from the current query
     * @return an {@link OpenTTDEvent} of type CLIENT_LEFT or null
     */
    private List<OpenTTDEvent> checkForClientsLeft( List<Client> oldList, List<Client> newList )
    {
        List<OpenTTDEvent> evts = new ArrayList<OpenTTDEvent>();
        for ( Client client : oldList )
        {
            if ( !client.isSpectator() && !newList.contains( client ) )
            {
                evts.add( new OpenTTDEvent( OpenTTDEventType.CLIENT_LEFT, client ) );
            }
        }
        return evts;
    }

    /**
     * This method will compare the client-lists and determine whether any
     * clients have joined.
     *
     * @param oldList the {@link List} of {@link Client}s from the last query
     * @param newList the {@link List} of {@link Client}s from the current query
     * @return an {@link OpenTTDEvent} of type CLIENT_JOIN or null
     */
    private List<OpenTTDEvent> checkForClientsJoined( List<Client> oldList, List<Client> newList )
    {
        List<OpenTTDEvent> evts = new ArrayList<OpenTTDEvent>();
        for ( Client client : newList )
        {
            LOG.debug( "Checking {}.", client );
            if ( !client.isSpectator() )
            {
                if ( oldList.contains( client ) )
                {
                    LOG.debug( "{} was already playing.", client );
                }
                else
                {
                    LOG.debug( "{} is new! Generating event.", client );
                    evts.add( new OpenTTDEvent( OpenTTDEventType.CLIENT_JOIN, client ) );
                }
            }
        }
        return evts;
    }

    /**
     * This method will determine whether the game is running or if it has been
     * put on hold due to no clients being connected.
     *
     * @param sameGame whether this is the same game as the last query or not
     * @return an {@link OpenTTDEvent} of type pause or unpause
     */
    private OpenTTDEvent checkForPauseUnpaused( boolean sameGame )
    {
        OpenTTDEvent evt = null;
        // If it isn't the same game this is pointless.
        if ( sameGame )
        {
            if ( paused )
            {
                LOG.debug( "Has the game started again?" );
                if ( compareDates( lastUpdate, currentUpdate ) > 0 )
                {
                    if ( --pauseCounter == 0 )
                    {
                        LOG.debug( "Decremented the pause test counter to {}.", pauseCounter );
                        LOG.debug( "Yes it has!" );
                        paused = false;
                        unpauseCounter = 0;
                        evt = new OpenTTDEvent( OpenTTDEventType.UNPAUSED, Integer.valueOf( currentUpdate.getClientsInfo().getGameDate().getYear() ) );
                    }
                }
                LOG.debug( "No it hasn't." );
            }
            else
            {
                LOG.debug( "Has the game paused?" );
                if ( compareDates( lastUpdate, currentUpdate ) == 0 )
                {
                    if ( ++unpauseCounter == 3 )
                    {
                        LOG.debug( "Incremented the pause test counter to {}.", unpauseCounter );
                        LOG.debug( "Yes it has!" );
                        paused = true;
                        pauseCounter = 1;
                        evt = new OpenTTDEvent( OpenTTDEventType.PAUSED, Integer.valueOf( currentUpdate.getClientsInfo().getGameDate().getYear() ) );
                    }
                }
                else
                {
                    LOG.debug( "No it hasn't. Resetting the pauseCounter." );
                    unpauseCounter = 0;
                }
            }
        }
        else
        {
            // if a new game has started
            pauseCounter = 0;
            paused = false;
        }
        return evt;
    }

    /**
     * This will compare to instances of {@link JOTLQuerier} to see if they
     * represent the same game. The method compares all data that is immutable
     * throughout one game.
     *
     * @param lastUpdate the last update
     * @param curUpdate the new update
     * @return whether they are equal
     */
    private boolean representsSameGame( Game lastUpdate, Game curUpdate )
    {
        // Is the first game?
        if ( lastUpdate == null )
        {
            LOG.debug( "Comparing games: this is the first game I've seen." );
            return false;
        }

        ServerDetails sriOld = lastUpdate.getClientsInfo();
        ServerDetails sriNew = curUpdate.getClientsInfo();
        ClientsDetails sdiOld = lastUpdate.getServerInfo();
        ClientsDetails sdiNew = curUpdate.getServerInfo();

        // Check terrain type
        if ( sriOld.getTileset() != sriNew.getTileset() )
        {
            LOG.debug( "Comparing games: different tilesets found." );
            return false;
        }
        // Check number of new graphics
        if ( sriOld.getGraphicsCount() != sriNew.getGraphicsCount() )
        {
            LOG.debug( "Comparing games: different graphic count found." );
            return false;
        }
        // Check max number of companies, clients and spectators
        if ( sriOld.getMaxNumberOfClients() != sriNew.getMaxNumberOfClients() )
        {
            LOG.debug( "Comparing games: different max number of clients found." );
            return false;
        }
        if ( sriOld.getMaxNumberOfCompanies() != sriNew.getMaxNumberOfCompanies() )
        {
            LOG.debug( "Comparing games: different max number of companies found." );
            return false;
        }
        if ( sriOld.getMaxNumberOfSpectators() != sriNew.getMaxNumberOfSpectators() )
        {
            LOG.debug( "Comparing games: different max number of spectators found." );
            return false;
        }
        // Check map size
        if ( sriOld.getMapHeight() != sriNew.getMapHeight() || sriOld.getMapWidth() != sriNew.getMapWidth() )
        {
            LOG.debug( "Comparing games: different maps.ize found." );
            return false;
        }
        // Check startdate
        if ( !sriOld.getStartDate().equals( sriNew.getStartDate() ) )
        {
            LOG.debug( "Comparing games: different start dates found." );
            return false;
        }
        // The current date must be is at least equal
        if ( sriOld.getGameDate().compareTo( sriNew.getGameDate() ) < 0 )
        {
            LOG.debug( "Comparing games: new game has lower game date than the old game." );
            return false;
        }
        // Check language
        if ( sriOld.getServerLanguage() != sriNew.getServerLanguage() )
        {
            LOG.debug( "Comparing games: different server languages found." );
            return false;
        }
        // Check server version
        if ( !sriOld.getVersion().equalsIgnoreCase( sriNew.getVersion() ) )
        {

            LOG.debug( "Comparing games: different server version found." );
            return false;
        }

        // Check dedication^^
        if ( sriOld.isDedicated() != sriNew.isDedicated() )
        {
            LOG.debug( "Comparing games: different dedicated status." );
            return false;
        }
        // Ok, so far so good. All immutable ServerResponse info has been
        // matched.
        LOG.debug( "Comparing games: Same game, as far as I know." );
        return true;
    }

    private void archiveGame()
    {
        // Make sure the earlierGames-list exists.
        if ( earlierGames == null )
        {
            earlierGames = new ArrayList<Game>();
        }
        // Remember it and insert it into the bottom of the list. :)
        earlierGames.add( 0, lastUpdate );
    }

    /**
     * This method prepares the handler for the next server query.
     */
    private void doFinalBookkeeping()
    {
        // currentUpdate is now made lastUpdate
        lastUpdate = currentUpdate;
        // Make sure that when the next update occurs
        // lastUpdate isn't used.
        currentUpdate = null;
    }

    /**
     * This method will check if a game has ended and create an
     * {@link OpenTTDEvent} if it has.
     *
     * @param sameGame The games are not equal
     * @return the {@link OpenTTDEvent} of type GAME_END or null
     * @see OpenTTDEventType
     */
    private OpenTTDEvent checkForEndGame( boolean sameGame )
    {
        OpenTTDEvent evt = null;
        // If sameGame == false there are two reasons:
        // The games are not equal or lastUpdate == null
        // So if lastUpdate IS NOT null that means a game ended.
        if ( !sameGame && lastUpdate != null )
        {
            evt = new OpenTTDEvent( OpenTTDEventType.GAME_END, Integer.valueOf( lastUpdate.getClientsInfo().getGameDate().getYear() ) );
        }
        return evt;
    }

    /**
     * This method will check if a new game has started.
     *
     * @param samegame whether or not this {@link JOTLQuerier} has data from the
     * same game as the previous one
     * @param currentUpdate the current {@link JOTLQuerier}
     * @return an {@link OpenTTDEvent} of type GAME_START or null
     * @see OpenTTDEventType
     */
    private OpenTTDEvent checkForNewGame( boolean samegame, Game currentUpdate )
    {
        // Check to find out whether a new game has started.
        OpenTTDEvent evt = null;
        if ( !samegame )
        {
            // This means a new game has started! :)
            evt = new OpenTTDEvent( OpenTTDEventType.GAME_START, Integer.valueOf( currentUpdate.getClientsInfo().getStartDate().getYear() ) );
            // Let's reset some useful variables
            electricRail = false;
            monoRail = false;
            maglev = false;
        }
        return evt;
    }

    /**
     * This method will return an {@link OpenTTDEvent} with information on what
     * new {@link Company}s have come into play. Note: this method should only
     * be used when a new game has started.
     *
     * @param curList the {@link List} of current {@link Company}'s
     * @return an {@link OpenTTDEvent} of type COMPANY_NEW or null
     */
    private OpenTTDEvent checkForNewcomers( List<Company> curList )
    {
        // A new game has been started. Has anyone started playing?
        OpenTTDEvent evt = null;
        if ( curList.size() > 0 )
        {
            evt = new OpenTTDEvent( OpenTTDEventType.COMPANY_NEW, (Object[]) curList.toArray( new Company[ curList.size() ] ) );
            this.newbies = curList.toArray( new Company[ curList.size() ] );
        }
        return evt;
    }

    /**
     * This method will return an {@link OpenTTDEvent} with information on what
     * new {@link Company}s have come into play. Note: this method should only
     * be used when a new game has started.
     *
     * @param lastList the {@link List} of {@link Company}'s from the last
     * {@link JOTLQuerier}
     * @param curList the {@link List} of current {@link Company}'s
     * @return an {@link OpenTTDEvent} of type COMPANY_NEW or null
     */
    private OpenTTDEvent checkForNewcomers( List<Company> lastList, List<Company> curList )
    {
        // Do we have any newcomers?
        OpenTTDEvent evt = null;
        if ( curList.size() > lastList.size() )
        {
            boolean newcomers = false;
            List<Company> n00bs = new ArrayList<Company>();
            for ( Company c : curList )
            {
                if ( !lastList.contains( c ) )
                {
                    newcomers = true;
                    n00bs.add( c );
                }
            }
            if ( newcomers )
            {
                evt = new OpenTTDEvent( OpenTTDEventType.COMPANY_NEW, (Object[]) n00bs.toArray( new Company[ n00bs.size() ] ) );
            }
        }
        return evt;
    }

    /**
     * This method will determine whether a new company has climbed to the top
     * and return a {@link OpenTTD}-new-leader-event.
     *
     * @param lastList the {@link List} of {@link Company}'s from the last
     * {@link JOTLQuerier}
     * @param curList the {@link List} of current {@link Company}'s
     * @return an {@link OpenTTD} of type NEW_LEADER or null
     */
    private OpenTTDEvent checkForNewLeader( List<Company> lastList, List<Company> curList )
    {
        // Has a new leader climbed to the top?
        if ( lastList.size() > 0 && curList.size() > 0 && ( !lastList.get( 0 ).equals( curList.get( 0 ) ) ) )
        {
            return new OpenTTDEvent( OpenTTDEventType.NEW_LEADER, curList.get( 0 ) );
        }
        return null;
    }

    /**
     * This method will determine whether or not a new type of rail has been
     * made available and create an {@link OpenTTD}-new-railtype-event if it
     * has.
     *
     * @param currentYear the current year will determine what is available
     * @return an {@link OpenTTD}-new-railtype-event or null
     */
    private OpenTTDEvent checkForNewRail( int currentYear )
    {
        // Has any new types of rail been made available?
        // Also make sure it isn't made available again.
        OpenTTDEvent evt = null;
        if ( currentYear <= 2022 )
        {
            if ( currentYear == 2022 && !maglev )
            {
                evt = new OpenTTDEvent( OpenTTDEventType.MAGLEV_AVAILABLE );
                maglev = true;
            }
            else if ( currentYear == 1999 && !monoRail )
            {
                evt = new OpenTTDEvent( OpenTTDEventType.MONORAIL_AVAILABLE );
                monoRail = true;
            }
            else if ( currentYear == 1965 && !electricRail )
            {
                evt = new OpenTTDEvent( OpenTTDEventType.ELECTRIC_AVAILABLE );
                electricRail = true;
            }
        }
        return evt;
    }

    /**
     * This is a convenience method for firing {@link OpenTTDEvent}s to any and
     * all listeners.
     *
     * @param evt the event to fire
     */
    private void fireEvents( List<OpenTTDEvent> evts )
    {
        LOG.debug( "Firing events: {}.", evts );
        if ( listeners != null )
        {
            for ( OpenTTDEvent evt : evts )
            {
                fireEvent( evt );
            }
        }
    }

    /**
     * This method is for firing a {@link OpenTTDEvent} to any and all
     * listeners.
     */
    private void fireEvent( OpenTTDEvent evt )
    {
        for ( OpenTTDListener otl : listeners )
        {
            LOG.debug( "'{}' has been notified of event: ''.", otl, evt );
            otl.eventOccured( evt );
        }
    }

    /**
     * Method for registering listeners for any events.
     *
     * @param otls the listener to add
     */
    public final void addOpenTTDListeners( OpenTTDListener... otls )
    {
        if ( listeners == null )
        {
            listeners = new ArrayList<OpenTTDListener>();
        }
        for ( OpenTTDListener otl : otls )
        {
            LOG.debug( "Adding listener {}.", otl );
            listeners.add( otl );
        }
    }

    /**
     * Method for deregistering listeners.
     *
     * @param otls the listeners to remove
     */
    public final void removeOpenTTDListener( OpenTTDListener... otls )
    {
        if ( listeners != null )
        {
            for ( OpenTTDListener otl : otls )
            {
                listeners.remove( otl );
            }
        }
    }
}
