package com.reservation.model;

public class Booking {
    private String pnr;
    private String passengerName;
    private String trainNumber;
    private String trainName;
    private String classType;
    private String journeyDate;
    private String sourceStation;
    private String destinationStation;
    private String bookingTime;

    public Booking(String pnr, String passengerName, String trainNumber, String trainName,
                    String classType, String journeyDate, String sourceStation,
                    String destinationStation, String bookingTime) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.classType = classType;
        this.journeyDate = journeyDate;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.bookingTime = bookingTime;
    }

    public String getPnr() { return pnr; }
    public String getPassengerName() { return passengerName; }
    public String getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public String getClassType() { return classType; }
    public String getJourneyDate() { return journeyDate; }
    public String getSourceStation() { return sourceStation; }
    public String getDestinationStation() { return destinationStation; }
    public String getBookingTime() { return bookingTime; }
}
