package com.amaxzadigital.tollpays.checkin;

public class ModelAnnouncement {
    private double latitude, longitude;
    private String announcementId, title;
    private boolean statusSpeak;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isStatusSpeak() {
        return statusSpeak;
    }

    public void setStatusSpeak(boolean statusSpeak) {
        this.statusSpeak = statusSpeak;
    }
}
