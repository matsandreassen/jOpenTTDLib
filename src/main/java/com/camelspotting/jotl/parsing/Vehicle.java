package com.camelspotting.jotl.parsing;

/**
 *
 * @author Mats Andreassen
 * @version 1.0
 */
public enum Vehicle {

    TRAIN(0, "Train"),
    TRUCK(1, "Truck"),
    BUS(2, "BUS"),
    AIRCRAFT(3, "Aircraft"),
    SHIP(4, "Ship");
    private int id;
    private String description;

    private Vehicle(int id, String description) {
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

    public static Vehicle toVehicle(int id) {
        for (Vehicle v : Vehicle.values()) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }
}
