package com.example.myapp.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.myapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

@Entity(tableName = "ledger", primaryKeys = {"landmark", "location", "date"})
public class LedgerEntity {
    private int id;
    @NotNull
    private String landmark;
    @NotNull
    private String location;
    @NotNull
    private Date date;
    private String needs;
    private String sender;
    private String latitude;
    private String longitude;
    private String accuracy;

    public void setLatitude(String latitude) { this.latitude = latitude; }

    public void setLongitude(String longitude) { this.longitude = longitude; }

    public void setAccuracy(String accuracy) { this.accuracy = accuracy; }

    public String getLatitude() { return latitude; }

    public String getLongitude() { return longitude; }

    public String getAccuracy() { return accuracy; }

    public String getSender() {return sender;}

    public void setSender(String sender) {this.sender = sender;}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLandmark(String landmark) {this.landmark = landmark;}

    public Date getDate() {return date;}

    public void setDate(Date date) {this.date = date;}

    public String getNeeds() {return needs;}

    public void setNeeds(String needs) {this.needs = needs;}

    public LedgerEntity() {    }

    public LedgerEntity(String locationName, String landmarkName, ArrayList<String> latLongAcc, ArrayList<String> requiredItems, Integer img ){

    }
}
