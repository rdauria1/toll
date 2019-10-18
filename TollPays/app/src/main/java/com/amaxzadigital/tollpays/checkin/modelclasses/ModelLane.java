package com.amaxzadigital.tollpays.checkin.modelclasses;

public class ModelLane {
    private String laneNo;
    private boolean isSelected;

    public ModelLane(String laneNo, boolean isSelected) {
        this.laneNo = laneNo;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getLaneNo() {
        return laneNo;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
