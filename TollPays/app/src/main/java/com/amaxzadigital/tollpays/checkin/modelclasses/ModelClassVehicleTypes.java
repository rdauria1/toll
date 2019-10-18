package com.amaxzadigital.tollpays.checkin.modelclasses;

import com.amaxzadigital.tollpays.checkin.ModelAxles;

import java.util.ArrayList;

/**
 * Created by Hussain Marvi on 28-Mar-17.
 */

public class ModelClassVehicleTypes {
    private String typeId, type;
    private ArrayList<ModelAxles> models;

    public ModelClassVehicleTypes(String vehicle_axle_id, String id, String type) {
        this.type=type;
        this.typeId=id;
    }


    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<ModelAxles> getModels() {
        return models;
    }

    public void setModels(ArrayList<ModelAxles> models) {
        this.models = models;
    }
}
