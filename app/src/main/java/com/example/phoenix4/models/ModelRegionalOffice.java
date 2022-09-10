package com.example.phoenix4.models;

public class ModelRegionalOffice {

    private String regionalOfficeId;
    private String userName;
    private String email;
    private Double regionalOfficeLat;
    private Double regionalOfficeLong;
    private Double distance;

    public ModelRegionalOffice() {
    }

    public ModelRegionalOffice(String regionalOfficeId, String userName, String email, Double currentLocationRegOfficeLat, Double currentLocationRegOfficeLong, Double distance) {
        this.regionalOfficeId = regionalOfficeId;
        this.userName = userName;
        this.email = email;
        this.regionalOfficeLat = currentLocationRegOfficeLat;
        this.regionalOfficeLong = currentLocationRegOfficeLong;
        this.distance = distance;
    }

    public String getRegionalOfficeId() {
        return regionalOfficeId;
    }

    public void setRegionalOfficeId(String regionalOfficeId) {
        this.regionalOfficeId = regionalOfficeId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getRegionalOfficeLat() {
        return regionalOfficeLat;
    }

    public void setRegionalOfficeLat(Double regionalOfficeLat) {
        this.regionalOfficeLat = regionalOfficeLat;
    }

    public Double getRegionalOfficeLong() {
        return regionalOfficeLong;
    }

    public void setRegionalOfficeLong(Double regionalOfficeLong) {
        this.regionalOfficeLong = regionalOfficeLong;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}