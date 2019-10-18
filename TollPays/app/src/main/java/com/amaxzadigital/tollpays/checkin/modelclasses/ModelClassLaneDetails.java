package com.amaxzadigital.tollpays.checkin.modelclasses;

import java.util.ArrayList;

/**
 * Created by Hussain Marvi on 07-Apr-17.
 */

public class ModelClassLaneDetails {
    private String lane_no, direction_inverse, lane_id;
    private ArrayList<ModelClassLanesCoordinates> arrayList;

    public ModelClassLaneDetails(String _lane_no, String _direction_inverse, String _lane_id, ArrayList<ModelClassLanesCoordinates> _arrayList) {
        setLane_no(_lane_no);
        setDirection_inverse(_direction_inverse);
        setLane_id(_lane_id);
        setLanesCoordinates(_arrayList);
    }

    public String getLane_no() {
        return lane_no;
    }

    public void setLane_no(String lane_no) {
        this.lane_no = lane_no;
    }

    public String getDirection_inverse() {
        return direction_inverse;
    }

    public void setDirection_inverse(String direction_inverse) {
        this.direction_inverse = direction_inverse;
    }

    public String getLane_id() {
        return lane_id;
    }

    public void setLane_id(String lane_id) {
        this.lane_id = lane_id;
    }

    public ArrayList<ModelClassLanesCoordinates> getLanesCoordinates() {
        return arrayList;
    }

    public void setLanesCoordinates(ArrayList<ModelClassLanesCoordinates> arrayList) {
        this.arrayList = arrayList;
    }
}
