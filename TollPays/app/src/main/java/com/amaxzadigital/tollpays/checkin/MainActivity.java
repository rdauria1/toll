package com.amaxzadigital.tollpays.checkin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;
import com.amaxzadigital.tollpays.SocketHandler;
import com.amaxzadigital.tollpays.conflictmanagement.TollFeesPoolingActivity;
import com.amaxzadigital.tollpays.discountprofile.RequestSubmissionActivity;
import com.amaxzadigital.tollpays.helpcenter.ElearningActivity;
import com.amaxzadigital.tollpays.login.LoginActivity;
import com.amaxzadigital.tollpays.login.SharedPreferenceUserDetails;
import com.amaxzadigital.tollpays.notifications.NotificationScreenActivity;
import com.amaxzadigital.tollpays.paymenthistory.PaymentHistoryActivity;
import com.amaxzadigital.tollpays.rentingborrowing.RentingBorrowingActivity;
import com.amaxzadigital.tollpays.settings.SettingsActivity;
import com.amaxzadigital.tollpays.users.UsersActivity;
import com.amaxzadigital.tollpays.violations.ViolationsActivity;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener /**, TollFeesPoolingActivity.MyInterface**/
{
    private static final int REQUEST_CODE_PERMISSION = 2;
    Toolbar toolbar;
    Context context = MainActivity.this;
    Intent intent;
    FragmentManager manager;
    FragmentTransaction transaction;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION, unreadMessageCounter = "";
    public static String passwordStatus = "", unattendedPassword = "";
    static String totalCountInAppNotification;
    SharedPreferenceMain sharedPreferenceMain;
    SharedPreferenceUserDetails sharedPreferenceUserDetails;
    TextView tvUserName, violationCounter, helpCenterCounter;
    Menu mymenu;
    MenuItem progress_menu_item;
    NavigationView navigationView;
    MyActivityResult myActivityResult;
    protected PowerManager.WakeLock mWakeLock;
    AlertDialog resumeGroupDialog;
    static ImageView ivNotification;
    static Badge badge;
    String conflictFlag, laneId, plaza_id, agency_id;
    VoiceCommandLinkedInterface voiceCommandLinkedInterface;
    SocketHandler socketHandler;
    boolean isClicked;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        socketHandler = SocketHandler.getInstance();


//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(Color.WHITE);
//        }

        Log.e("IM", "Firebase ID: " + FirebaseInstanceId.getInstance().getToken());
        sharedPreferenceMain = SharedPreferenceMain.GetObject(context);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ivNotification = findViewById(R.id.ivNotification);
        ivNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClicked) {
                    Intent intent = new Intent(MainActivity.this, NotificationScreenActivity.class);
                    startActivity(intent);
                    isClicked = false;
                }

            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        sharedPreferenceUserDetails = SharedPreferenceUserDetails.GetObject(context);
        Common.accessToken = sharedPreferenceMain.getAccessToken();
        Log.e("IM", Common.accessToken);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onPairClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        tvUserName = (TextView) hView.findViewById(R.id.tvUserName);
        tvUserName.setText(sharedPreferenceUserDetails.getUserFirstName() + "!");
        violationCounter = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_violations));
        helpCenterCounter = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_help));

        if (sharedPreferenceUserDetails.getUserType().equals("0")) {
            navigationView.getMenu().findItem(R.id.nav_admin_panel).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_discount_request).setVisible(false);
        }
        navigationView.setNavigationItemSelectedListener(this);
        manager = getFragmentManager();
        transaction = manager.beginTransaction();
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission) != MockPackageManager.PERMISSION_GRANTED) {

                EnableLocationFragment fragment = new EnableLocationFragment();
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                transaction.replace(R.id.main_layout, fragment, "");
                transaction.commit();

                // If any permission above not allowed by user, this condition will execute every time, else your else part will work
            } else {
//                MapFragment fragment = new MapFragment();
                MapFragmentNew fragment = new MapFragmentNew();
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                transaction.replace(R.id.main_layout, fragment, "");
                transaction.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.isAppOnBackground = false;
        isClicked = true;
        tvUserName.setText(sharedPreferenceUserDetails.getUserFirstName() + "!");
        navigationView.getMenu().getItem(0).setChecked(true);
        if (mymenu != null)
            new GetBalance().execute();
        new GetDetails().execute();
        Log.e("IM", "Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Common.isAppOnBackground = true;
        Log.e("IM", "ONPAUSE");
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new GetBalance().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mWakeLock.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mymenu = menu;
        progress_menu_item = mymenu.findItem(R.id.action_balance);
        progress_menu_item.setActionView(R.layout.menu_item_layout);
        new GetBalance().execute();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) { //Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_balance: {
                Intent intent = new Intent(context, CurrentBalanceActivity.class);
                intent.putExtra("userBalance", Common.userBalance);
                intent.putExtra("replenishmentAmount", Common.replenishmentAmount);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {// Handle navigation view item clicks here.
        int id = item.getItemId();
        manager = getFragmentManager();
        transaction = manager.beginTransaction();

        if (id == R.id.nav_checkin) {
//            toolbar.setTitle(R.string.check_in);
//            try {
//                if (ActivityCompat.checkSelfPermission(this, mPermission)
//                        != MockPackageManager.PERMISSION_GRANTED) {
//
//                    EnableLocationFragment fragment = new EnableLocationFragment();
//                    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
//                    transaction.replace(R.id.main_layout, fragment, "");
//                    transaction.commit();
//
//                    // If any permission above not allowed by user, this condition will execute every time, else your else part will work
//                } else {
//                    MapFragment fragment = new MapFragment();
//                    transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
//                    transaction.replace(R.id.main_layout, fragment, "");
//                    transaction.commit();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        } else if (id == R.id.nav_toll_fees_pooling) {
            intent = new Intent(context, TollFeesPoolingActivity.class);
            intent.putExtra("PasswordStatus", passwordStatus);
            intent.putExtra("UnattendedPassword", unattendedPassword);
            startActivityForResult(intent, 4);
            if (voiceCommandLinkedInterface != null) {
                voiceCommandLinkedInterface.voiceCommandLinkedInterface();
            }
        } else if (id == R.id.nav_violations) {
            intent = new Intent(context, ViolationsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_payment) {
            intent = new Intent(context, PaymentHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_users) {
            intent = new Intent(context, UsersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_discount_request) {
            intent = new Intent(context, RequestSubmissionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_renting_borrowing) {
            intent = new Intent(context, RentingBorrowingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            intent = new Intent(context, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            intent = new Intent(context, ElearningActivity.class);
            intent.putExtra("UnreadMessageCounter", unreadMessageCounter);
            startActivity(intent);
        } else if (id == R.id.nav_admin_panel) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tollpays.com/?login"));
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == MockPackageManager.PERMISSION_GRANTED) {
                manager = getFragmentManager();
                transaction = manager.beginTransaction();
                MapFragmentNew fragment = new MapFragmentNew();
                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                transaction.replace(R.id.main_layout, fragment, "");
                transaction.commit();
            } else {
            }
        }
    }

    // Web service for getting current balance of user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (myActivityResult != null) {
                myActivityResult.myActivityResult(requestCode, resultCode, data);
            }
//            Toast.makeText(context, "Data Received", Toast.LENGTH_SHORT).show();
        }
    }
//    @Override
//    public void myNotification() {
//        new GetDetails().execute();
//    }

//    @Override
//    public void dismissGroupView() {
//        ((TollFeesPoolingActivity)context).setClickListener(this);
//        if (viewDismiss != null) viewDismiss.dismissView();
//
//    }


    class GetBalance extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/balance?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        serviceResponse += line;
                    }
                } else {
                    serviceResponse = "";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serviceResponse;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");

                if (status.equals("SUCCESS")) {
                    progress_menu_item = mymenu.findItem(R.id.action_balance);
                    progress_menu_item.setActionView(null);
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    Common.selectedCardNo = dataObject.getJSONObject("selected_card").getString("card");
                    Common.selectedCardType = dataObject.getJSONObject("selected_card").getString("type");
                    Common.userBalance = dataObject.getString("balance");
                    Common.replenishmentAmount = dataObject.getString("replenish_amount");
                    progress_menu_item.setTitle("$" + dataObject.getString("balance"));
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Conflict Resume Dialog Show to Contributor if his app was close when app open this dialog will show to contributor
    class ResumeGroupDialogStatusUpdate extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/RemoveDialogueShow?access_token=" + Common.accessToken);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(Common.getPostDataString(postParamenter));
                writer.flush();
                writer.close();
                os.close();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        serviceResponse += line;
                    }
                } else {
                    serviceResponse = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serviceResponse;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    //todo step 21
                    resumeGroupDialog.dismiss();
                    Common.alertDialogResumeStatus = false;
                    Common.contributorResumeDialogShow = "0";
                    Common.multipleTollsGroup = "0";
                    if (Common.contributorResumeDialog) {
                        Intent intent = new Intent(context, TollFeesPoolingActivity.class);
                        intent.putExtra("PasswordStatus", passwordStatus);
                        intent.putExtra("UnattendedPassword", unattendedPassword);
                        startActivityForResult(intent, 4);
                    }


                } else if (status.equals("FAILED")) {

                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    Common.groupId = "";

                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    class OnlyCheckInService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<>();
            postParamenter.put("agency_id", agency_id);
            postParamenter.put("plaza_id", plaza_id);
            postParamenter.put("lane_id", laneId);
            postParamenter.put("conflict_flag", conflictFlag);

        }

        @SuppressLint("NewApi")
        @Override
        protected String doInBackground(String... strings) {
            // Send data
            try {

                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/checkin?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(30000);
                urlConnection.setConnectTimeout(40000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(Common.getPostDataString(postParamenter));
                writer.flush();
                writer.close();
                os.close();
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        serviceResponse += line;
                    }
                } else {
                    serviceResponse = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return serviceResponse;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {


                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class GetDetails extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... strings) {
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/mydetails?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        serviceResponse += line;
                    }
                } else {
                    serviceResponse = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serviceResponse;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    JSONObject object = jsonObject.getJSONObject("data");
                    violationCounter.setTextColor(getResources().getColor(R.color.colorPrimary));
                    violationCounter.setTypeface(null, Typeface.BOLD);
                    violationCounter.setGravity(Gravity.CENTER);
                    violationCounter.setTextSize(12);
                    sharedPreferenceMain.setUserId(object.getString("id"));
                    passwordStatus = object.getString("conflict_password_toggle");
                    totalCountInAppNotification = object.getString("total_count_in_app_notification");
                    //todo step 19
                    Common.multipleTollsGroup = object.getString("multiple_toll_group");
                    Common.contributorResumeDialogShow = object.getString("multiple_resume_dialogue_show");
                    //checkInServiceFlag = object.getString("check_in_service_hit_flag");
                    //checkInServiceData = object.getJSONObject("check_in_service_data");
                    //conflictFlag = checkInServiceData.getString("conflict_flag");
                    //laneId = checkInServiceData.getString("Lane_id");
                    //plaza_id = checkInServiceData.getString("plaza_id");
                    //agency_id = checkInServiceData.getString("agency_id");
                    //if (checkInServiceFlag.equals("1")) {
                    //      new OnlyCheckInService().execute();
                    //  }
                    //todo step 20;
                    if (Common.contributorResumeDialogShow.equals("1") && resumeGroupDialog == null && Common.alertDialogResumeStatus) {
                        resumeGroupDialog = new AlertDialog.Builder(context).create();
                        resumeGroupDialog.setTitle("Resume Group");
                        resumeGroupDialog.setMessage("You want to Resume group with Driver");
                        resumeGroupDialog.setCancelable(false);
                        resumeGroupDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Common.contributorResumeDialog = true;
                                //todo step 22
                                new ResumeGroupDialogStatusUpdate().execute();
                            }
                        });

                        resumeGroupDialog.setButton(Dialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ResumeGroupDialogStatusUpdate().execute();
                                dialog.dismiss();
                            }
                        });
                        resumeGroupDialog.show();
                    }
                    unattendedPassword = object.getString("conflict_password_user");
                    if (Integer.parseInt(object.getString("total_violations")) > 0)
                        violationCounter.setText(object.getString("total_violations"));
                    else
                        violationCounter.setText("");
                    helpCenterCounter.setTextColor(getResources().getColor(R.color.colorPrimary));
                    helpCenterCounter.setGravity(Gravity.CENTER);
                    helpCenterCounter.setTypeface(null, Typeface.BOLD);
                    helpCenterCounter.setTextSize(12);
                    if (Integer.parseInt(object.getString("total_pending_msg")) > 0) {
                        helpCenterCounter.setText(object.getString("total_pending_msg"));
                        unreadMessageCounter = object.getString("total_pending_msg");
                    } else {
                        helpCenterCounter.setText("");
                        unreadMessageCounter = "";
                    }

                    badge = new QBadgeView(context);
                    if (totalCountInAppNotification.equals("0")) {
                        ivNotification.setVisibility(View.GONE);
                        ivNotification.setVisibility(View.VISIBLE);
                        badge.hide(true);
                    } else if (!totalCountInAppNotification.equals("0")) {
                        badge.setBadgeNumber(Integer.parseInt(totalCountInAppNotification));
                        badge.setBadgeBackgroundColor(Color.RED);
                        badge.setShowShadow(false);
                        badge.setBadgeTextSize(7, true);
                        badge.setGravityOffset(1, 6, true);
                        badge.bindTarget(ivNotification);
                    }
                    socketHandler.mSocket.emit("add_user_id", "android," + object.getString("id"));

                } else if (status.equals("FAILED") && jsonObject.getString("ref").equals("invalid_token")) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void setClickListener(MyActivityResult itemClickListener) {
        this.myActivityResult = itemClickListener;
    }

    public void setVoiceCommandLinkedInterface(VoiceCommandLinkedInterface itemClickListener) {
        this.voiceCommandLinkedInterface = itemClickListener;
    }

    public interface MyActivityResult {
        void myActivityResult(int requestCode, int resultCode, Intent data);
    }

    public interface VoiceCommandLinkedInterface {
        void voiceCommandLinkedInterface();
    }

}
