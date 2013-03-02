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

import com.camelspotting.jotl.domain.Server;
import com.camelspotting.jotl.event.OpenTTDEvent;
import com.camelspotting.jotl.event.OpenTTDEventType;
import com.camelspotting.jotl.event.OpenTTDListener;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is one of the primary access points for any user of the library. It's
 * for being used to monitor an ongoing OpenTTD-server and it's games. It can do
 * updates continously or manually.
 *
 * @author Mats Andreassen
 * @version 1.0
 */
public class ServerHandler
{

    /**
     * The listeners that are to be notified of events
     */
    private List<OpenTTDListener> listeners;
    /**
     * The host being monitored
     */
    private Server monitoredServer;
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
    private JOTLQuerier lastUpdate;
    /**
     * The current update
     */
    private JOTLQuerier currentUpdate;
    /**
     * The local port
     */
    private int fromPort;
    /**
     * The remote port
     */
    private int destPort;
    /**
     * Has electric rail been made available?
     */
    private boolean electricRail = false;
    /**
     * Has monorail been made available?
     */
    private boolean monoRail = false;
    /**
     * Has maglev been made available?
     */
    private boolean maglev = false;
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
    private boolean debug = false;

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
    public ServerHandler( String host, int localPort, int remotePort, int updateInterval, boolean updateNow, OpenTTDListener... otls ) throws UnknownHostException, JOTLException
    {
        this.monitoredServer = Parser.parseHost( host );
        this.updateInterval = updateInterval;
        this.fromPort = localPort;
        this.destPort = remotePort;
        addOpenTTDListeners( otls ); // This will do nothing if otl is null
        if ( updateNow )
        {
            update();
        }
    }

    /**
     * Convenience constructor for creating the server handler. Be aware that if
     * you supply no initial listeners and still tell it to update once
     * immidiately no one will receive any events. Regardless of what you supply
     * as an update interval it will not start and continous sequence without a
     * call to start(). Standard ports, local 2222 and remote 3979 is used.
     * NOTE: According to the OpenTTD-code a day is about 2 seconds if the
     * machine running it is able to do run it normally.
     *
     * @param host the server to monitor
     * @param updateInterval update interval in milliseconds, 0 or less for
     * manual
     * @param updateNow do an update right away?
     * @param otls any initial listeners?
     * @see #start()
     */
    public ServerHandler( String host, int updateInterval, boolean updateNow, OpenTTDListener... otls ) throws UnknownHostException, JOTLException
    {
        this( host, 2222, 3979, updateInterval, updateNow, otls );
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
        return monitoredServer.getAddress().getCanonicalHostName();
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
                                printDebugMessage( "5000 millisecond update rate fallback succeeded. Returning to " + updateInterval + "." );
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
                                printDebugMessage( "Timeout " + timeouts + "occured. A new game may be loading." );
                                lastUpdateFailed = true;
                            }
                            else
                            {
                                printDebugMessage( "The maximum number of timeouts in a row(" + maxTimouts + ") been reached. Server may have gone down." );
                                setUpdateInterval( -1 );
                                fireEvent( new OpenTTDEvent( OpenTTDEventType.LOST_CONNECTION ) );
                                printDebugMessage( "Manual update mode has been set and any and all listeners have been notified." );
                            }
                        }
                    }
                }
                printDebugMessage( "Update thread death." );
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
        currentUpdate = new JOTLQuerier( monitoredServer, fromPort, destPort, true );
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
        printDebugMessage( "Let's check this new update for changes." );
        boolean isSameGame = representsSameGame( lastUpdate, currentUpdate );
        List<Company> lastList = ( lastUpdate != null ? lastUpdate.getServerDetailedInfo().getCompanies() : null );
        List<Company> curList = currentUpdate.getServerDetailedInfo().getCompanies();

        // Has a new game started?
        OpenTTDEvent newgame = checkForNewGame( isSameGame, currentUpdate );
        if ( newgame != null )
        {
            int diff = compareDates( currentUpdate.getServerResponseInfo().getGameDate(), currentUpdate.getServerResponseInfo().getStartDate() );
            if ( diff < 0 )
            {
                // This is not a "new" game, but a game in progress
                printDebugMessage( "I found out a 'new game' was acually a game in progress." );
                newgame = new OpenTTDEvent( OpenTTDEventType.GAME_IN_PROGRESS, Integer.valueOf( currentUpdate.getServerResponseInfo().getGameDate()[2] ) );
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
            OpenTTDEvent railEvent = checkForNewRail( currentUpdate.getServerResponseInfo().getGameDate()[2] );

            // Do we have a new leader?
            OpenTTDEvent newLeader = checkForNewLeader( lastList, curList );
            // Do we have any new companies?
            OpenTTDEvent newcomers = checkForNewcomers( lastList, curList );
            // Has the game been paused/unpaused?
            OpenTTDEvent pause = checkForPauseUnpaused( isSameGame );
            // Has any company been removed
            List<OpenTTDEvent> removedEvents = checkForRemovedCompanies( isSameGame, lastUpdate.getServerDetailedInfo().getCompanies(), currentUpdate.getServerDetailedInfo().getCompanies() );

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
        if ( lastUpdate.getServerDetailedInfo().getVersion() >= 5 )
        {
            try
            {
                if ( lastUpdate != null )
                {
                    // Did any clients join/leave?
                    List<Client> oldClients = lastUpdate.getServerDetailedInfo().getAllClients();
                    List<Client> newClients = currentUpdate.getServerDetailedInfo().getAllClients();
                    List<OpenTTDEvent> joinEvents = checkForClientsJoined( oldClients, newClients );
                    List<OpenTTDEvent> leftEvents = checkForClientsLeft( oldClients, newClients );

                    evts.addAll( joinEvents );
                    evts.addAll( leftEvents );
                }
                else
                {
                    List<Client> newClients = currentUpdate.getServerDetailedInfo().getAllClients();
                    if ( newClients.size() > 0 )
                    {
                        evts.add( new OpenTTDEvent( OpenTTDEventType.CLIENT_JOIN, (Object[]) newClients.toArray( new Client[ newClients.size() ] ) ) );
                    }
                }
            }
            catch ( JOTLException otx )
            {
            }
        }

        if ( evts.size() > 0 )
        {
            fireEvents( evts );
        }
        else
        {
            printDebugMessage( "No new events were found." );
        }
        doFinalBookkeeping();
    }

    private int compareDates( int[] first, int second[] )
    {
        // Year example: 2000 - 1973
        int diff = second[2] - first[2]; // Year
        if ( diff == 0 )
        {
            // Month example: 11 - 9
            diff = second[1] - first[1]; // Month
            if ( diff == 0 )
            {
                // Day example: 26 - 13
                diff = second[0] - first[0]; // Day
            }
        }
        String sign = null;
        if ( diff > 0 )
        {
            sign = ">";
        }
        else if ( diff == 0 )
        {
            sign = "=";
        }
        else
        {
            sign = "<";
        }
        printDebugMessage( "Comparing dates: " + Arrays.toString( second ) + " " + sign + " " + Arrays.toString( first ) );
        return diff;
    }

    private int compareDates( JOTLQuerier lastUpdate, JOTLQuerier currentUpdate )
    {
        return compareDates( lastUpdate.getServerResponseInfo().getGameDate(), currentUpdate.getServerResponseInfo().getGameDate() );
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
            printDebugMessage( "Checking " + client + "." );
            if ( !client.isSpectator() )
            {
                if ( oldList.contains( client ) )
                {
                    printDebugMessage( client + " was already playing." );
                }
                else
                {
                    printDebugMessage( client + " is new! Generating event." );
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
                printDebugMessage( "Has the game started again?" );
                if ( compareDates( lastUpdate, currentUpdate ) > 0 )
                {
                    if ( --pauseCounter == 0 )
                    {
                        printDebugMessage( "Decremented the pause test counter to " + pauseCounter + "." );
                        printDebugMessage( "Yes it has!" );
                        paused = false;
                        unpauseCounter = 0;
                        evt = new OpenTTDEvent( OpenTTDEventType.UNPAUSED, Integer.valueOf( currentUpdate.getServerResponseInfo().getGameDate()[2] ) );
                    }
                }
                printDebugMessage( "No it hasn't." );
            }
            else
            {
                printDebugMessage( "Has the game paused?" );
                if ( compareDates( lastUpdate, currentUpdate ) == 0 )
                {
                    if ( ++unpauseCounter == 3 )
                    {
                        printDebugMessage( "Incremented the pause test counter to " + unpauseCounter + "." );
                        printDebugMessage( "Yes it has!" );
                        paused = true;
                        pauseCounter = 1;
                        evt = new OpenTTDEvent( OpenTTDEventType.PAUSED, Integer.valueOf( currentUpdate.getServerResponseInfo().getGameDate()[2] ) );
                    }
                }
                else
                {
                    printDebugMessage( "No it hasn't." );
                    printDebugMessage( "Resetting the pauseCounter." );
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
    private boolean representsSameGame( JOTLQuerier lastUpdate, JOTLQuerier curUpdate )
    {
        // Is the first game?
        if ( lastUpdate == null )
        {
            printDebugMessage( "Comparing games: this is the first game I've seen." );
            return false;
        }

        Clients sriOld = lastUpdate.getServerResponseInfo();
        Clients sriNew = curUpdate.getServerResponseInfo();
        ServerInfo sdiOld = lastUpdate.getServerDetailedInfo();
        ServerInfo sdiNew = curUpdate.getServerDetailedInfo();

        // Check terrain type
        if ( sriOld.getTileset() != sriNew.getTileset() )
        {
            printDebugMessage( "Comparing games: different tilesets found." );
            return false;
        }
        // Check number of new graphics
        if ( sriOld.getGraphicsCount() != sriNew.getGraphicsCount() )
        {
            printDebugMessage( "Comparing games: different graphic count found." );
            return false;
        }
        // Check max number of companies, clients and spectators
        if ( sriOld.getMaxNumberOfClients() != sriNew.getMaxNumberOfClients() )
        {
            printDebugMessage( "Comparing games: different max number of clients found." );
            return false;
        }
        if ( sriOld.getMaxNumberOfCompanies() != sriNew.getMaxNumberOfCompanies() )
        {
            printDebugMessage( "Comparing games: different max number of companies found." );
            return false;
        }
        if ( sriOld.getMaxNumberOfSpectators() != sriNew.getMaxNumberOfSpectators() )
        {
            printDebugMessage( "Comparing games: different max number of spectators found." );
            return false;
        }
        // Check map size
        if ( sriOld.getMapHeight() != sriNew.getMapHeight() || sriOld.getMapWidth() != sriNew.getMapWidth() )
        {
            printDebugMessage( "Comparing games: different maps.ize found." );
            return false;
        }
        // Check startdate
        int[] d1 = sriOld.getStartDate();
        int[] d2 = sriNew.getStartDate();
        for ( int i = 0; i < 3; i++ )
        {
            if ( d1[i] != d2[i] )
            {
                printDebugMessage( "Comparing games: different start dates found." );
                return false;
            }
        }
        // The current date must be is at least equal
        d1 = sriOld.getGameDate();
        d2 = sriNew.getGameDate();
        if ( compareDates( d1, d2 ) < 0 )
        {
            printDebugMessage( "Comparing games: new game has lower game date than the old game." );
            return false;
        }
        // Check language
        int[] v1 = sriOld.getVersion();
        int[] v2 = sriNew.getVersion();
        if ( sriOld.getServerLanguage() != sriNew.getServerLanguage() )
        {
            printDebugMessage( "Comparing games: different server languages found." );
            return false;
        }
        // Check server version
        for ( int i = 0; i < v1.length; i++ )
        {
            if ( v1[i] != v2[i] )
            {
                printDebugMessage( "Comparing games: different server version found." );
                return false;
            }
        }
        // Check dedication^^
        if ( sriOld.isDedicated() != sriNew.isDedicated() )
        {
            printDebugMessage( "Comparing games: different dedicated status." );
            return false;
        }
        // Ok, so far so good. All immutable ServerResponse info has been
        // matched.
        printDebugMessage( "Comparing games: Same game, as far as I know." );
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
        earlierGames.add( 0, lastUpdate.toGame() );
    }

    /**
     * This method prepares the handler for the next server query.
     */
    private void doFinalBookkeeping()
    {
        // currentUpdate is now made lastUpdate
        lastUpdate = currentUpdate;
        // Make currentUpdate unable to do any updates.
        // This shouldn't be necessary since it's only kept for
        // statistical purposes.
        currentUpdate.makeUnupdatable();
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
            evt = new OpenTTDEvent( OpenTTDEventType.GAME_END, Integer.valueOf( lastUpdate.getServerResponseInfo().getGameDate()[2] ) );
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
    private OpenTTDEvent checkForNewGame( boolean samegame, JOTLQuerier currentUpdate )
    {
        // Check to find out whether a new game has started.
        OpenTTDEvent evt = null;
        if ( !samegame )
        {
            // This means a new game has started! :)
            evt = new OpenTTDEvent( OpenTTDEventType.GAME_START, Integer.valueOf( currentUpdate.getServerResponseInfo().getStartDate()[2] ) );
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
        printDebugMessage( "Firing " + evts + " events" );
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
            printDebugMessage( "'" + otl + "' has been notified of event: '" + evt + "'." );
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
            printDebugMessage( "Adding listener " + otl + "." );
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

    private void printDebugMessage( String msg )
    {
        if ( debug )
        {
            System.out.println( "ServerHandler: " + msg );
        }
    }
}
