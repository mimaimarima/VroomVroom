package com.example.workshop;

public class Ride {
    private int id;
    private String displayName;
    private String startTime;
    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;
    private String price;
    public int rideFinished;
    public int driverRated;
    public int passengersRated;
    public Ride(int id, String displayName, String startTime, double startLatitude,
                double startLongitude, double endLatitude, double endLongitude,
                String price, int rideFinished, int driverRated, int passengersRated)
    {
        this.id = id;
        this.displayName = displayName;
        this.startTime = startTime;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.price = price;
        this.rideFinished = rideFinished;
        this.driverRated= driverRated;
        this.passengersRated=passengersRated;
    }
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getPrice() {
        return price;
    }

    public int getId()
    {
        return id;
    }
    public double getStartLatitude()
    {
        return startLatitude;
    }

    public double getStartLongitude()
    {
        return startLongitude;
    }

    public double getEndLatitude()
    {
        return endLatitude;
    }

    public double getEndLongitude()
    {
        return endLongitude;
    }
    public int getRideFinished()
    {
        return this.rideFinished;
    }
    public int getDriverRated()
    {
        return this.driverRated;
    }
    public int getPassengersRated()
    {
        return this.passengersRated;
    }
    public void setRideFinished()
    {
        rideFinished=1;
    }
    public void setPassengersRated()
    {
        passengersRated=1;
    }
    public void setDriverRated()
    {
        driverRated=1;
    }
}
