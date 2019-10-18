package com.amaxzadigital.tollpays.checkin.modelclasses;

/**
 * Created by Hussain Marvi on 28-Mar-17.
 */

public class ModelClassCarModel {
    private String makeId, modelId, model;

    public ModelClassCarModel(String _makeId, String _modelId, String _model) {
        setMakeId(_makeId);
        setModelId(_modelId);
        setModel(_model);
    }

    public String getMakeId() {
        return makeId;
    }

    public void setMakeId(String makeId) {
        this.makeId = makeId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
