package com.amaxzadigital.tollpays.checkin.modelclasses;

import java.util.ArrayList;

/**
 * Created by Hussain Marvi on 28-Mar-17.
 */

public class ModelClassCarMake {
    private String make;
    private ArrayList<ModelClassCarModel> models;

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public ArrayList<ModelClassCarModel> getModels() {
        return models;
    }

    public void setModels(ArrayList<ModelClassCarModel> models) {
        this.models = models;
    }
}
