package com.amaxzadigital.tollpays.checkin.modelclasses;

import java.util.ArrayList;

/**
 * Created by Hussain Marvi on 05-Apr-17.
 */

public class ModelClassPlazaDetails {
    private String plaza_id, dynamic_price, zone_name, plaza_name, agency_id, barrier, type, latitude, longitude, amount, balance, insufficient_balance, plaza_direction;
    private ArrayList<ModelClassLaneDetails> lanesDetails;

    public String getPlaza_id() {
        return plaza_id;
    }

    public void setPlaza_id(String plaza_id) {
        this.plaza_id = plaza_id;
    }

    public String getPlaza_name() {
        return plaza_name;
    }

    public void setPlaza_name(String plaza_name) {
        this.plaza_name = plaza_name;
    }

    public String getAgency_id() {
        return agency_id;
    }

    public void setAgency_id(String agency_id) {
        this.agency_id = agency_id;
    }

    public String getBarrier() {
        return barrier;
    }

    public void setBarrier(String barrier) {
        this.barrier = barrier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getInsufficient_balance() {
        return insufficient_balance;
    }

    public void setInsufficient_balance(String insufficient_balance) {
        this.insufficient_balance = insufficient_balance;
    }

    public ArrayList<ModelClassLaneDetails> getLanesDetails() {
        return lanesDetails;
    }

    public void setLanesDetails(ArrayList<ModelClassLaneDetails> lanesDetails) {
        this.lanesDetails = lanesDetails;
    }

    public String getPlaza_direction() {
        return plaza_direction;
    }

    public void setPlaza_direction(String plaza_direction) {
        this.plaza_direction = plaza_direction;
    }

    public String getDynamic_price() {
        return dynamic_price;
    }

    public void setDynamic_price(String dynamic_price) {
        this.dynamic_price = dynamic_price;
    }

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
    }
}
