package com.amaxzadigital.tollpays.checkin.modelclasses;

/**
 * Created by Hussain Marvi on 07-Apr-17.
 */

public class ModelClassNearestLane {
    private float distance;
    private int lane;

    public ModelClassNearestLane(float _distance, int _lane) {
        setDistance(_distance);
        setLane(_lane);
    }

    public ModelClassNearestLane() {

    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }
}
