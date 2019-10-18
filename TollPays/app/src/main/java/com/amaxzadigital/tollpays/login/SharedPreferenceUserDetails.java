package com.amaxzadigital.tollpays.login;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Hussain Marvi on 16-Mar-17.
 */

public class SharedPreferenceUserDetails {
    static Context context;
    static SharedPreferences myPrefs;
    private static SharedPreferenceUserDetails sharedPreference = null;
    private static String spName = "UserDetailsSharedPreference";

    public static SharedPreferenceUserDetails GetObject(Context con) {

        if (sharedPreference == null)
            sharedPreference = new SharedPreferenceUserDetails();

        context = con;
        myPrefs = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sharedPreference;
    }

    public String getUserId() {
        return myPrefs.getString("userId", "");
    }

    public void setUserId(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("userId", value);
        prefsEditor.commit();
    }

    public String getUserName() {
        return myPrefs.getString("userName", "");
    }

    public void setUserName(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("userName", value);
        prefsEditor.commit();
    }

    public String getUserFirstName() {
        return myPrefs.getString("firstName", "");
    }

    public void setUserFirstName(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("firstName", value);
        prefsEditor.commit();
    }

    public String getUserLastName() {
        return myPrefs.getString("lastName", "");
    }

    public void setUserLastName(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("lastName", value);
        prefsEditor.commit();
    }

    public String getUserEmail() {
        return myPrefs.getString("email", "");
    }

    public void setUserEmail(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("email", value);
        prefsEditor.commit();
    }

    public String getUserAccountNo() {
        return myPrefs.getString("accountNo", "");
    }

    public void setUserAccountNo(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("accountNo", value);
        prefsEditor.commit();
    }

    public String getUserPhoneNo() {
        return myPrefs.getString("phoneNo", "");
    }

    public void setUserPhoneNo(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("phoneNo", value);
        prefsEditor.commit();
    }

    public String getDefaultVehicleId() {
        return myPrefs.getString("vehicleId", "");
    }

    public void setDefaultVehicleId(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("vehicleId", value);
        prefsEditor.commit();
    }


    public String getDefaultVehicleType() {
        return myPrefs.getString("vehicleType", "");
    }

    public void setDefaultVehicleType(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("vehicleType", value);
        prefsEditor.commit();
    }

    public String getDefaultVehicleNo() {
        return myPrefs.getString("vehicleNo", "");
    }

    public void setDefaultVehicleNo(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("vehicleNo", value);
        prefsEditor.commit();
    }



    public String getUserType() {
        return myPrefs.getString("userType", "");
    }

    public void setUserType(String value) {
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("userType", value);
        prefsEditor.commit();
    }

}
