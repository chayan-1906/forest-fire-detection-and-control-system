package com.example.phoenix4.models;

public class ModelReport {

    private Long reportId;
    private String userName;
    private String email;
    private Double fireLocationLat;
    private Double fireLocationLong;
    private Double currentLocationRegOfficeLat;
    private Double currentLocationRegOfficeLong;
    private Double distance;
    private Long timestamp;

    public ModelReport() {
    }

    public ModelReport(Long reportId, String userName, String email, Double fireLocationLat, Double fireLocationLong, Double currentLocationRegOfficeLat, Double currentLocationRegOfficeLong, Double distance, Long timestamp) {
        this.reportId = reportId;
        this.userName = userName;
        this.email = email;
        this.fireLocationLat = fireLocationLat;
        this.fireLocationLong = fireLocationLong;
        this.currentLocationRegOfficeLat = currentLocationRegOfficeLat;
        this.currentLocationRegOfficeLong = currentLocationRegOfficeLong;
        this.distance = distance;
        this.timestamp = timestamp;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
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

    public Double getFireLocationLat() {
        return fireLocationLat;
    }

    public void setFireLocationLat(Double fireLocationLat) {
        this.fireLocationLat = fireLocationLat;
    }

    public Double getFireLocationLong() {
        return fireLocationLong;
    }

    public void setFireLocationLong(Double fireLocationLong) {
        this.fireLocationLong = fireLocationLong;
    }

    public Double getCurrentLocationRegOfficeLat() {
        return currentLocationRegOfficeLat;
    }

    public void setCurrentLocationRegOfficeLat(Double currentLocationRegOfficeLat) {
        this.currentLocationRegOfficeLat = currentLocationRegOfficeLat;
    }

    public Double getCurrentLocationRegOfficeLong() {
        return currentLocationRegOfficeLong;
    }

    public void setCurrentLocationRegOfficeLong(Double currentLocationRegOfficeLong) {
        this.currentLocationRegOfficeLong = currentLocationRegOfficeLong;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
