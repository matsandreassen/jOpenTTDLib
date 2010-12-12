package com.camelspotting.jotl.parsing;

/**
 *
 * @author Mats Andreassen
 * @version 1.0
 */
public enum Station {

    TRAIN(0, "Train station"),
    TRUCK(1, "Truck Stop"),
    BUS(2, "Bus stop"),
    AIRPORT(3, "Airport"),
    DOCK(4, "Dock");
    private int id;
    private String description;

    private Station(int id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public static Station toStation(int id) {
        for (Station s : Station.values()) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }
}
