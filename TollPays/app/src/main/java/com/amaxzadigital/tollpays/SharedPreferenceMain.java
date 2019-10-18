package com.amaxzadigital.tollpays;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceMain {
    static Context context;
    static SharedPreferences myPrefs;
    private static SharedPreferenceMain sharedPreference = null;
    private static String spName = "MainSharedPreference";

    public static SharedPreferenceMain GetObject(Context con) {
        if (sharedPreference == null)
            sharedPreference = new SharedPreferenceMain();

        context = con;
        myPrefs = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sharedPreference;
    }

    public boolean isLogin() {
        return myPrefs.getBoolean("firstLogin", false);
    }

    public void setLogin(boolean value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("firstLogin", value);
        prefsEditor.commit();
    }

    public String getAccessToken() {
        return myPrefs.getString("accessToken", "");
    }

    public void setAccessToken(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("accessToken", value);
        prefsEditor.commit();
    }

    public String getUserName() {
        return myPrefs.getString("loginUserName", "");
    }

    public void setUserName(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("loginUserName", value);
        prefsEditor.commit();
    }


    public String getUserId() {
        return myPrefs.getString("loginUserId", "");
    }

    public void setUserId(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("loginUserId", value);
        prefsEditor.commit();
    }

    public String getUserPassword() {
        return myPrefs.getString("loginUserPassword", "");
    }

    public void setUserPassword(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("loginUserPassword", value);
        prefsEditor.commit();
    }

    public String getTouchIdUserName() {
        return myPrefs.getString("touchIdLoginUserName", "");
    }

    public void setTouchIdUserName(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("touchIdLoginUserName", value);
        prefsEditor.commit();
    }

    public String getUserPass() {
        return myPrefs.getString("userPassword", "");
    }

    public void setUserPass(String userPassword) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("userPassword", userPassword);
        prefsEditor.commit();
    }

    public String getTouchIdUserPassword() {
        return myPrefs.getString("touchIdLoginUserPassword", "");
    }

    public void setTouchIdUserPassword(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("touchIdLoginUserPassword", value);
        prefsEditor.commit();
    }

    public boolean isTouchIdEnabled() {
        return myPrefs.getBoolean("fingerPrint", false);
    }

    public void setTouchId(boolean value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("fingerPrint", value);
        prefsEditor.commit();
    }

    public boolean isAutoTrackingEnabled() {
        return myPrefs.getBoolean("autoTracking", true);
    }

    public void setAutoTracking(boolean value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("autoTracking", value);
        prefsEditor.commit();
    }

    public boolean isVoiceNotificationEnabled() {
        return myPrefs.getBoolean("voiceNotification", false);
    }

    public void setVoiceNotification(boolean value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("voiceNotification", value);
        prefsEditor.commit();
    }

    public boolean isVoiceCommandEnabled() {
        return myPrefs.getBoolean("voiceCommand", false);
    }

    public void setVoiceCommand(boolean value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("voiceCommand", value);
        prefsEditor.commit();
    }

    public boolean isTollNotificationSoundEnabled() {
        return myPrefs.getBoolean("notificationSound", true);
    }

    public void setTollNotificationSound(boolean value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("notificationSound", value);
        prefsEditor.commit();
    }

    public boolean isPairingEnabled() {
        return myPrefs.getBoolean("pairingStatus", false);
    }

    public void setPairingEnabled(boolean value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("pairingStatus", value);
        prefsEditor.commit();
    }

    public String getPairingId() {
        return myPrefs.getString("pairingId", "");
    }

    public void setPairingId(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("pairingId", value);
        prefsEditor.commit();
    }

    public String getGroupId() {
        return myPrefs.getString("groupid", "");
    }

    public void setGroupId(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("groupid", value);
        prefsEditor.commit();
    }

    public String getPairingRole() {
        return myPrefs.getString("pairingrole", "");

    }

    public void setPairingRole(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("pairingrole", value);
        prefsEditor.commit();
    }

//  public boolean isLocationEnabled() {
//        return myPrefs.getBoolean("fingerPrintPasscode", false);
//    }

    public void setLocationStatus(boolean value) {
//        SharedPreferences.Editor prefsEditor = myPrefs.edit();
//        prefsEditor.putBoolean("fingerPrintPasscode", value);
//        prefsEditor.commit();
    }
}
