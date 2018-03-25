package com.example.hingo.jump360;

/**
 * Created by hingo on 21-03-2018.
 */

//Contact class to store data of contact object

public class Contact {
    private String name;
    private Long contactNo;
    private double latitude;
    private double longitude;

    public Contact(String name, Long contactNo, double latitude, double longitude) {
        this.name = name;
        this.contactNo = contactNo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Contact(){}

    //Getter Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getContactNo() {
        return contactNo;
    }

    public void setContactNo(Long contactNo) {
        this.contactNo = contactNo;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
