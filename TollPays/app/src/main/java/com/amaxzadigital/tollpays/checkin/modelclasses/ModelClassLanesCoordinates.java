package com.amaxzadigital.tollpays.checkin.modelclasses;

/**
 * Created by Hussain Marvi on 07-Apr-17.
 */

public class ModelClassLanesCoordinates {
    private String latitude, longitude;

    public ModelClassLanesCoordinates(String _latitude, String _longitude) {
        setLatitude(_latitude);
        setLongitude(_longitude);
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
}
