package com.example.phoenix4.models;

public class ModelJob {

    private String uid;
    private Double fireLocationLat;
    private Double fireLocationLong;
    private Double distance;
    private Long timestamp;
    private Long timeSpent;

    public ModelJob() {
    }

    public ModelJob(String uid, Double fireLocationLat, Double fireLocationLong, Double distance, Long timestamp, Long timeSpent) {
        this.uid = uid;
        this.fireLocationLat = fireLocationLat;
        this.fireLocationLong = fireLocationLong;
        this.distance = distance;
        this.timestamp = timestamp;
        this.timeSpent = timeSpent;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public Long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }
}
