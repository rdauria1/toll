package com.amaxzadigital.tollpays.checkin.modelclasses;

public class ModelNotification {
    private String notificationId, time, description, statusReadUnRead, type, type_table_id;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatusReadUnRead() {
        return statusReadUnRead;
    }

    public void setStatusReadUnRead(String statusReadUnRead) {
        this.statusReadUnRead = statusReadUnRead;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType_table_id() {
        return type_table_id;
    }

    public void setType_table_id(String type_table_id) {
        this.type_table_id = type_table_id;
    }
}
