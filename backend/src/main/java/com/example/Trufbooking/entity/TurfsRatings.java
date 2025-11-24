package com.example.Trufbooking.entity;

import jakarta.persistence.*;

import java.util.List;
import java.util.Map;

@Entity
public class TurfsRatings {
    @Id
    @Column(name = "turfid")
    private int turfId;

    @Column(name="user_rating", columnDefinition = "JSON")
    private String userRatings;

    private double average;

    public int getTurfId() {
        return turfId;
    }

    public void setTurfId(int turfId) {
        this.turfId = turfId;
    }

    public String getUserRatings() {
        return userRatings;
    }

    public void setUserRatings(String userRatings) {
        this.userRatings = userRatings;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
