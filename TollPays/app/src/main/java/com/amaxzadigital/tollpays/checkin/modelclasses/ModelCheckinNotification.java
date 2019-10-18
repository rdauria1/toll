package com.amaxzadigital.tollpays.checkin.modelclasses;

public class ModelCheckinNotification {
    private String type, text;
    private long timer;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
