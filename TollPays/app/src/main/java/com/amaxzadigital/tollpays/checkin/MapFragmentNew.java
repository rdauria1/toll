package com.amaxzadigital.tollpays.checkin;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;
import com.amaxzadigital.tollpays.SnowboyUtils;
import com.amaxzadigital.tollpays.SocketHandler;
import com.amaxzadigital.tollpays.Threadings;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelCheckinNotification;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassLaneDetails;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassLanesCoordinates;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassNearestLane;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassPlazaDetails;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelLane;
import com.amaxzadigital.tollpays.conflictmanagement.AdapterPairedUsers;
import com.amaxzadigital.tollpays.conflictmanagement.AddNonSmartPhoneUser;
import com.amaxzadigital.tollpays.conflictmanagement.ModelPairingSessions;
import com.amaxzadigital.tollpays.login.LoginActivity;
import com.amaxzadigital.tollpays.login.SharedPreferenceUserDetails;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.nkzawa.emitter.Emitter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.instacart.library.truetime.TrueTime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.kitt.snowboy.SnowboyDetect;
import q.rorbin.badgeview.QBadgeView;

public class MapFragmentNew extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, AdapterPairedUsers.MyInterface,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener, TextToSpeech.OnInitListener, MainActivity.MyActivityResult, AdapterTollLane.MyInterface, AdapterCheckinNotification.MyInterface {
    MapView mMapView;
    Switch stchCheckInTracking;
    TextView tvSearchingToll, tvCheckInPlateNo, tvTollName, tvTollPrice,
            tvTollMiles, tvLog, tvDynamicPriceReason;
    LinearLayout llCheckInToll, llNonGatedToll, llGatedToll, llLaneDetection;
    View parentView;
    HorizontalPicker lvToolLane;
    RecyclerView rvTollLane;
    CardView cvVehicle, cvLog;
    Button btnTollCheckIn;
    LatLng currentCoordinates, initialCoordinates/*, finalCoordinates*/;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    boolean markerAnimationStatus = false, noBarrierCheckInStatus = false;
    SharedPreferenceUserDetails sharedPreferenceUserDetails;
    SharedPreferenceMain sharedPreferenceMain;
    boolean tollFound = false, tollServiceRunning = false, autoLaneSelection = true, detectingLane = false, moveToLane1 = false, moveToLane2 = false, isForward = false, isBackward = false, isNotified = false, shouldNotity = false, isNearToll = false;
    MapRipple mapRipple;
    CameraPosition cameraPosition;
    ScrollView svLog;
    private GoogleMap googleMap;
    private Marker positionMarker, plazaMarker;
    private String tollName, tollAmount, logCat = "";
    private float plazaDistance = 10000, lastClosestplazaDistance = 10000;
    private int selectedLaneIndex = 0;
    MediaPlayer checkInAlertPlayer, normalAlertPlayer;
    AudioManager am;
    int ringerMode;
    LinearLayout llCheckInTollWithBarrierNotification;
    TextView tvCheckInTollWithBarrierNotification;
    CheckBox cbCheckInTollWithBarrierMode;
    private SensorManager mSensorManager;
    private Sensor mCompass;
    boolean isCameraZoomFinished = false;
    LocationManager locationManager;
    LayoutInflater inflater;
    View view;
    TextView tvplazaPrice, tvIncrementDecrement;
    ImageView ivplazaImage, ivIncrement, ivDecrement;
    Context context;
    AlertDialog alertDialogInsufficientBalance;
    AlertDialog alertDialogInvalidVehicle;
    TextToSpeech textToSpeech;
    JSONObject object;
    ArrayList<ModelPairingSessions> pairingSessionsArrayList;
    ModelPairingSessions modelPairingSessions;
    AdapterPairedUsers adapterPairingSession;
    RecyclerView rvPairing;
    Button btnAddNonSmartUser;
    CardView cVSession, cVExpandCollapse;
    ImageView ivExpand;
    Timer timerForDriver;
    public static LinearLayout llParedUsers;
    //    BroadcastReceiver mMessageReceiver;
    String driverPairingId, contributorPairingId, dialogMessageReceived, dialogMessage, acceptContributorServiceUrl, contributorName, contributorLeavePairingId = "", groupId = "", toll_remaining = "", myPairingId = "", pairingId = "", myPairingRole = "";
    AlertDialog alertTollFee;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSION_WRITE = 1;
    SpeechRecognizer mSpeechRecognizer;
    /* Keyword we are looking for to activate menu */
    AlertDialog dialogContributorRequest;
    JSONObject paramsObject;
    //    Voice voice;
    int positionInGroup;
    static int conflictRestart = 0;
    int tollRemaining;
    boolean voiceCommandTollIncrementDecrement;
    ModelAnnouncement modelAnnouncemnt;
    ModelDynamicPricing modelDynamicPricing;
    ArrayList<ModelDynamicPricing> pricingArrayList;
    ArrayList<ModelAnnouncement> arrayAnnouncement;
    ModelAnnouncementLatLng modelAnnouncemntLatLng;
    ArrayList<ModelAnnouncementLatLng> arrayAnnouncementLatLng;
    double lastSpeakNotificationlat = 0, lastSpeakNotificationLng = 0;
    float distanceAfterLastNotification = 1000;
    String distanceAfterLast = "0";
    String exitPlazaName;
    String entryPlazaName;
    String transuctionId;
    static boolean pairingServiceStatus;
    ProgressDialog conflictRestartDialogForContributor;
    ProgressDialog conflictRestartDialogForDriver;
    boolean conflictRestartdialog;
    boolean tollchange;
    Button btnCheckIn;
    static boolean multiTollFirstCheckIn = false;
    ImageView ic_refresh;
    private SpeechRecognizer speechRecognizer;
    private boolean shouldDetect;
    private boolean runOnUiThreadHandling;
    private SnowboyDetect snowboyDetect;
    AudioRecord audioRecord;
    Intent recognizerIntent;
    String currentDate;
    int dynamicPricingCurrentObjectIndex;
    Timer timerForCurrentTimeInterval, timerForUpcomingTimeInterval;
    String dynamicPricingFlag = "0";
    String tempButtonName = "";
    View rootView;
    JSONArray jsonArray;
    static float CAMERA_ZOOM_LEVEL = 16;
    ConstraintLayout clPairedUsers;
    static int pairedUsersViewHeight;
    RecyclerView rvNotifications;
    ArrayList<ModelCheckinNotification> checkinNotifications = new ArrayList<>();
    ModelCheckinNotification modelCheckinNotification;
    AdapterCheckinNotification adapterCheckinNotification;
    SnapHelper snapHelper;
    AdapterTollLane adapterTollLane;
    boolean nearTollNotificationAdded;
    Handler checkinNotificationHandler, checkinNotificationHandler2;
    int notificationScrollPosition = 0;
    NumberPicker numberPicker;
    AlertDialog alertDialog;
    SocketHandler socketHandler;
    String isCongestionPlaza, congestionFormId, checkInOrCheckOut, amount;

    static {
        System.loadLibrary("snowboy-detect-android");
    }

    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
        return dist;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_checkin_new, container, false);
        parentView = rootView;
        context = getActivity();
        ((MainActivity) this.getActivity()).setClickListener(this);
        checkPermission();
        socketHandler = SocketHandler.getInstance();
        arrayAnnouncement = new ArrayList<>();
        Common.alertDialogResumeStatus = true;
        Common.voiceCommandStartFragment = false;
        dialogContributorRequest = new AlertDialog.Builder(context).create();
        alertTollFee = new AlertDialog.Builder(context).create();
        alertDialogInvalidVehicle = new AlertDialog.Builder(context).create();
        alertDialogInsufficientBalance = new AlertDialog.Builder(context).create();
        mSensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sharedPreferenceUserDetails = SharedPreferenceUserDetails.GetObject(getActivity());
        sharedPreferenceMain = SharedPreferenceMain.GetObject(getActivity());
        findAllViews();
        RecyclerView.LayoutManager customLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true);
        lvToolLane.setOnItemSelectedListener(new HorizontalPicker.OnItemSelected() {
            @Override
            public void onItemSelected(int index) {
                selectedLaneIndex = index;
                if (autoLaneSelection) {
                    Toast.makeText(context, "Auto lane detection disabled", Toast.LENGTH_SHORT).show();
                    autoLaneSelection = false;
                }
            }
        });

        // switch state change listener

        stchCheckInTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (b) {
                        startSearchingToll();

                    } else {
                        stopSearchingToll();
                    }
                } else {
                    stchCheckInTracking.setChecked(false);
                    tvDynamicPriceReason.setVisibility(View.GONE);
                    dynamicPricingFlag = "0";
                    enabledLocation();
                }
            }
        });

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();


        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(40.4119042, -102.9379834)/*United States coordinates*/));


                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setPadding(0, 150, 0, 0);
                googleMap.setMyLocationEnabled(false);

                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        if (googleApiClient.isConnected())
                            registerSensorMovementListener();
                    }
                });
                googleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
                    @Override
                    public void onCameraMoveCanceled() {
                        unRegisterSensorMovementListener();
                    }
                });

            }
        });

        cbCheckInTollWithBarrierMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (checkInAlertPlayer != null)
                        checkInAlertPlayer.stop();
                    sharedPreferenceMain.setTollNotificationSound(false);
                } else {
                    checkInAlertPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.notification);
                    checkInAlertPlayer.setLooping(true);
                    checkInAlertPlayer.start();
                    sharedPreferenceMain.setTollNotificationSound(true);
                }
            }
        });


        Log.e("IM", "Group Id: " + sharedPreferenceMain.getGroupId());
        if (!sharedPreferenceMain.getGroupId().equals("")) {
            new UserInGroup().execute();
        }


        // When You Paired As A Driver Button End Session will Show OR When You Paired As A Contributor Button End Session will Hide
        checkInAlertPlayer = MediaPlayer.create(getActivity(), R.raw.notification);
        am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        ringerMode = am.getRingerMode();


//        mMessageReceiver = new BroadcastReceiver() {
//            @SuppressLint("NewApi")
//            @Override
//            public void onReceive(final Context context, Intent intent) {
//
//                // Extract data included in the Intent
//                String Data = intent.getStringExtra("Data");
//                try {
//                    JSONObject jObj = new JSONObject(Data);
//                    String params = jObj.getString("params").replaceAll("\\\\", "");
//                    paramsObject = new JSONObject(params);
//                    String silentPushType = jObj.getString("type");
//                    if (silentPushType.equals("mobile_notification")) {
//                        new DetailVehicleServiceForCount().execute();
//                    }
//                    Log.e("IM", silentPushType);
//
//                    if (silentPushType.equals("conflict_approve_popup")) {
//                        dialogMessage = jObj.getString("body");
//                        acceptContributorServiceUrl = jObj.getString("url").replaceAll("\\\\", "");
//                        contributorPairingId = paramsObject.getString("contributor_pairing_id");
//                        driverPairingId = paramsObject.getString("driver_pairing_id");
//                        contributorName = paramsObject.getString("contributor_name");
//                        if (Common.pairingRole.equals("Driver")) {
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(dialogMessage);
//                            }
//                            alertTollFee = new AlertDialog.Builder(context).create();
//                            alertTollFee.setTitle("HELLO!");
//                            alertTollFee.setMessage(dialogMessage);
//                            alertTollFee.setCancelable(false);
//                            alertTollFee.setButton(AlertDialog.BUTTON_POSITIVE, "",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            acceptContributor();
//                                        }
//                                    });
//
//                            alertTollFee.setButton(AlertDialog.BUTTON_NEGATIVE, "DECLINE",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            new DriverPressDecline().execute();
//                                            alertTollFee.dismiss();
//                                        }
//                                    });
//
//                            alertTollFee.show();
//                        } else if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
//
//                        }
//                    }
//                    if (silentPushType.equals("conflict_approve_accept")) {
//                        groupId = paramsObject.getString("group_id");
//                        Common.groupId = groupId;
//                        if (Common.plazaDetails != null && Common.pairingRole.equals("Driver")) {
//                            //todo step 15
////                            ivIncrement.setVisibility(View.VISIBLE);
////                            ivDecrement.setVisibility(View.VISIBLE);
//                            tvIncrementDecrement.setVisibility(View.VISIBLE);
//                            tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(dialogMessage);
//                            }
//                            new GroupPairingListing().execute(groupId);
//                            if (Common.saveCountMultiple) {
//                                timerForDriver.cancel();
//                                conflictRestartDialogForDriver.dismiss();
//                            }
//                            if (Common.saveCountMultiple && tollchange) {
//                                Common.selectedTollCount = Common.selectedTollCount - 1;
//                                new SaveTollCount().execute();
//                                tollchange = false;
//                            }
//                            new SetPricePlaza().execute();
//                        } else if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(dialogMessage);
//                            }
//                            ivIncrement.setVisibility(View.GONE);
//                            ivDecrement.setVisibility(View.GONE);
////                            tvIncrementDecrement.setVisibility(View.GONE);
//                            tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
//                            new GroupPairingListing().execute(groupId);
//                        }
//                    } else if (silentPushType.equals("conflict_approve_password_accept")) {
//                        groupId = paramsObject.getString("group_id");
//                        paramsObject.getString("toll_remaining");
//                        Common.groupId = groupId;
//                        //todo step 14
//                        if (Common.plazaDetails != null && Common.pairingRole.equals("Driver")) {
//                            if (Common.saveCountMultiple) {
//                                Common.selectedTollCount = Common.selectedTollCount - 1;
//                                new SaveTollCount().execute();
//                            }
//                            ivIncrement.setVisibility(View.VISIBLE);
////                            ivDecrement.setVisibility(View.VISIBLE);
//                            tvIncrementDecrement.setVisibility(View.VISIBLE);
////                            textViewToll.setVisibility(View.VISIBLE);
//                            tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(dialogMessage);
//                            }
//                            new GroupPairingListing().execute(groupId);
//                            new SetPricePlaza().execute();
//
//                        } else if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(dialogMessage);
//                            }
//                            ivIncrement.setVisibility(View.GONE);
//                            ivDecrement.setVisibility(View.GONE);
////                            tvIncrementDecrement.setVisibility(View.GONE);
//                            tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
//                            new GroupPairingListing().execute(groupId);
//                        }
//                    } else if (silentPushType.equals("conflict_driver_reject_request_announcement")) {
//                        if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(getString(R.string.user_request_rejected));
//                            }
//                            Toast.makeText(context, "Driver Rejected your Request", Toast.LENGTH_SHORT).show();
//                        }
//                    } else if (silentPushType.equals("restart_conflict_process")) {
//                        //todo step 7 contributor work started
//                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                            speakText(getString(R.string.resuming_group));
//                        }
//                        conflictRestartDialogForContributor = new ProgressDialog(context);
//                        conflictRestartDialogForContributor.setMessage("Resuming group. Please wait...");
//                        conflictRestartDialogForContributor.setCancelable(false);
//                        conflictRestartDialogForContributor.show();
//                        driverPairingId = paramsObject.getString("driver_pairing_id");
//                        Common.pairingRole = paramsObject.getString("role");
//                        tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
//                        if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
//                            if (!Common.ignorePush) {
//                                googleApiClient.connect();
//                                new Timer().schedule(new TimerTask() {
//                                    @Override
//                                    public void run() {
//                                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                            speakText(getString(R.string.remaining_toll));
//                                        }
//                                        new EnablePairing().execute();
//                                        this.cancel();
//                                    }
//                                }, 3000);
//                            }
//                        }
//
//                    } else if (silentPushType.equals("count_update_toll_remaining")) {
//                        tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
//                        if (Common.updatedToll) {
//                            Toast.makeText(context, "Updated toll count", Toast.LENGTH_SHORT).show();
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(getString(R.string.toll_updated));
//                            }
//                        }
//                        Common.updatedToll = true;
//
//                    } else if (silentPushType.equals("conflict_update_longitude_latitude")) {
//                        //todo step 16
//                        googleApiClient.connect();
//                        new Timer().schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                new UpdateCurrentLocation().execute();
//                            }
//                        }, 5000);
//                    } else if (silentPushType.equals("conflict_end_session_announcement")) {
//                        Toast.makeText(context, jObj.getString("body"), Toast.LENGTH_SHORT).show();
//                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                            speakText(getString(R.string.group_ended));
//                        }
//
//                        sharedPreferenceMain.setPairingEnabled(false);
//                        Common.pairingId = "";
//                        sharedPreferenceMain.setPairingId("");
//                        if (!Common.groupId.equals("")) {
//                            sharedPreferenceMain.setGroupId("");
//                            Common.groupId = "";
//                        }
//                        stchCheckInTracking.setChecked(false);
//                        llParedUsers.setVisibility(View.GONE);
//                        Common.multipleTollsGroup = "0";
//                        Common.contributorResumeDialogShow = "0";
//                        conflictRestart = 0;
//                        Common.tollCountRemaining = 0;
//                        Common.selectedTollCount = 0;
//                        tvIncrementDecrement.setText("0");
//
//                    } else if (silentPushType.equals("conflict_contributor_leave_announcement")) {
//                        Toast.makeText(context, jObj.getString("body"), Toast.LENGTH_SHORT).show();
//                        groupId = paramsObject.getString("group_id");
//                        contributorLeavePairingId = paramsObject.getString("contributor_leave_pairing_id");
//                        contributorName = paramsObject.getString("leave_contributor_name");
//                        new GroupPairingListing().execute(groupId);
//                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                            speakText(contributorName + "left from group");
//                        }
//                        if (contributorLeavePairingId.equals(myPairingId)) {
//                            new DisablePairing().execute();
//                            stchCheckInTracking.setChecked(false);
//                            multiTollFirstCheckIn = false;
//                            llParedUsers.setVisibility(View.GONE);
//                            Common.multipleTollsGroup = "0";
//                            Common.contributorResumeDialogShow = "0";
//                            conflictRestart = 0;
//                            Common.tollCountRemaining = 0;
//                            Common.selectedTollCount = 0;
//                            tvIncrementDecrement.setText("0");
//                        }
//                    } else if (silentPushType.equals("conflict_set_price")) {
//                        groupId = paramsObject.getString("groonCup_id");
//                        if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(getString(R.string.prize_distrubuted));
//                            }
//                            new GroupPairingListing().execute(groupId);
//                        }
//                    }
//                } catch (JSONException e) {
//                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            }
//        };


        return rootView;
    }

    Emitter.Listener conflictGroup = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            //scoketlistners
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jObj = (JSONObject) args[0];
                        String params = jObj.getString("params").replaceAll("\\\\", "");
                        paramsObject = new JSONObject(params);
                        String silentPushType = jObj.getString("type");

                        if (silentPushType.equals("conflict_end_session_announcement")) {
                            Toast.makeText(context, jObj.getString("body"), Toast.LENGTH_SHORT).show();
                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                speakText(getString(R.string.group_ended));
                            }

                            sharedPreferenceMain.setPairingEnabled(false);
                            Common.pairingId = "";
                            sharedPreferenceMain.setPairingId("");
                            if (!Common.groupId.equals("")) {
                                sharedPreferenceMain.setGroupId("");
                                Common.groupId = "";
                            }
                            stchCheckInTracking.setChecked(false);
                            llParedUsers.setVisibility(View.GONE);
                            Common.multipleTollsGroup = "0";
                            Common.contributorResumeDialogShow = "0";
                            conflictRestart = 0;
                            Common.tollCountRemaining = 0;
                            Common.selectedTollCount = 0;
                            tvIncrementDecrement.setText("0");

                        } else if (silentPushType.equals("conflict_set_price")) {
                            groupId = paramsObject.getString("group_id");
                            if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(getString(R.string.prize_distrubuted));
                                }
                                new GroupPairingListing().execute(groupId);
                            }
                        } else if (silentPushType.equals("count_update_toll_remaining")) {
                            tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
                            if (Common.updatedToll) {
                                Toast.makeText(context, "Updated toll count", Toast.LENGTH_SHORT).show();
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(getString(R.string.toll_updated));
                                }
                            }
                            Common.updatedToll = true;

                        } else if (silentPushType.equals("conflict_approve_accept")) {
                            groupId = paramsObject.getString("group_id");
                            Common.groupId = groupId;
                            if (Common.plazaDetails != null && Common.pairingRole.equals("Driver")) {
                                //todo step 15
//                            ivIncrement.setVisibility(View.VISIBLE);
//                            ivDecrement.setVisibility(View.VISIBLE);
                                tvIncrementDecrement.setVisibility(View.VISIBLE);
                                tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(dialogMessage);
                                }
                                new GroupPairingListing().execute(groupId);
                                if (Common.saveCountMultiple) {
                                    timerForDriver.cancel();
                                    conflictRestartDialogForDriver.dismiss();
                                }
                                if (Common.saveCountMultiple && tollchange) {
                                    Common.selectedTollCount = Common.selectedTollCount - 1;
                                    new SaveTollCount().execute();
                                    tollchange = false;
                                }
                                new SetPricePlaza().execute();
                            } else if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(dialogMessage);
                                }
                                ivIncrement.setVisibility(View.GONE);
                                ivDecrement.setVisibility(View.GONE);
//                                tvIncrementDecrement.setVisibility(View.GONE);
                                tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
                                new GroupPairingListing().execute(groupId);
                            }
                        } else if (silentPushType.equals("conflict_contributor_leave_announcement")) {
                            Toast.makeText(context, jObj.getString("body"), Toast.LENGTH_SHORT).show();
                            groupId = paramsObject.getString("group_id");
                            contributorLeavePairingId = paramsObject.getString("contributor_leave_pairing_id");
                            contributorName = paramsObject.getString("leave_contributor_name");
                            new GroupPairingListing().execute(groupId);
                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                speakText(contributorName + "left from group");
                            }
                            if (contributorLeavePairingId.equals(myPairingId)) {
                                stchCheckInTracking.setChecked(false);
                                multiTollFirstCheckIn = false;
                                llParedUsers.setVisibility(View.GONE);
                                Common.multipleTollsGroup = "0";
                                Common.contributorResumeDialogShow = "0";
                                conflictRestart = 0;
                                Common.tollCountRemaining = 0;
                                Common.selectedTollCount = 0;
                                tvIncrementDecrement.setText("0");
                                new DisablePairing().execute();
                            }
                        } else if (silentPushType.equals("restart_conflict_process")) {
                            //todo step 7 contributor work started
                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                speakText(getString(R.string.resuming_group));
                            }
                            conflictRestartDialogForContributor = new ProgressDialog(context);
                            conflictRestartDialogForContributor.setMessage("Resuming group. Please wait...");
//                            conflictRestartDialogForContributor.setCancelable(false);
                            conflictRestartDialogForContributor.show();
                            driverPairingId = paramsObject.getString("driver_pairing_id");
                            Common.pairingRole = paramsObject.getString("role");
                            tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
                            if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                                if (!Common.ignorePush) {
                                    googleApiClient.connect();
                                    new Timer().schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                                speakText(getString(R.string.remaining_toll));
                                            }
                                            new EnablePairing().execute();
                                            this.cancel();
                                        }
                                    }, 3000);
                                }
                            }
                        } else if (silentPushType.equals("conflict_approve_password_accept")) {
                            groupId = paramsObject.getString("group_id");
//                            paramsObject.getString("toll_remaining");
                            Common.groupId = groupId;
                            //todo step 14
                            if (Common.plazaDetails != null && Common.pairingRole.equals("Driver")) {
                                if (Common.saveCountMultiple) {
                                    Common.selectedTollCount = Common.selectedTollCount - 1;
                                    new SaveTollCount().execute();
                                }
//                                ivIncrement.setVisibility(View.VISIBLE);
//                            ivDecrement.setVisibility(View.VISIBLE);
                                tvIncrementDecrement.setVisibility(View.VISIBLE);
//                            textViewToll.setVisibility(View.VISIBLE);
                                tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(dialogMessage);
                                }
                                new GroupPairingListing().execute(groupId);
                                new SetPricePlaza().execute();

                            } else if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(dialogMessage);
                                }
                                ivIncrement.setVisibility(View.GONE);
                                ivDecrement.setVisibility(View.GONE);
//                            tvIncrementDecrement.setVisibility(View.GONE);
                                tvIncrementDecrement.setText(paramsObject.getString("toll_remaining"));
                                new GroupPairingListing().execute(groupId);
                            }
                        } else if (silentPushType.equals("conflict_update_longitude_latitude")) {
                            //todo step 16
                            googleApiClient.connect();
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    new UpdateCurrentLocation().execute();
                                }
                            }, 5000);
                        }
                        Log.e("IM", "Ye!");
                        //----Ends Here----//
                    } catch (Exception e) {
                        return;
                    }
                }
            });

        }
    };

    Emitter.Listener conflictApprovePopup = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (!Common.isAppOnBackground) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jObj = (JSONObject) args[0];
                        try {
                            dialogMessage = jObj.getString("body");
                            acceptContributorServiceUrl = jObj.getString("url").replaceAll("\\\\", "");
                            String params = jObj.getString("params").replaceAll("\\\\", "");
                            paramsObject = new JSONObject(params);
                            contributorPairingId = paramsObject.getString("contributor_pairing_id");
                            driverPairingId = paramsObject.getString("driver_pairing_id");
                            contributorName = paramsObject.getString("contributor_name");
                            if (Common.pairingRole.equals("Driver")) {
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(dialogMessage);
                                }

                                LayoutInflater inflater = LayoutInflater.from(context);
                                View dialog_layout = inflater.inflate(R.layout.custom_pairing_dialog, null);
                                TextView tvMessage = (TextView) dialog_layout.findViewById(R.id.tvMessage);
                                TextView tvTitle = (TextView) dialog_layout.findViewById(R.id.tvTitle);
                                Button btnDecline = dialog_layout.findViewById(R.id.btnDecline);
                                Button btnAccept = dialog_layout.findViewById(R.id.btnAccept);
                                tvMessage.setText(dialogMessage);
                                if (dialogContributorRequest != null && dialogContributorRequest.isShowing()) {
                                    dialogContributorRequest.dismiss();
                                }

                                dialogContributorRequest = new AlertDialog.Builder(context).create();
                                dialogContributorRequest.setView(dialog_layout);
                                dialogContributorRequest.setCancelable(false);

                                btnAccept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogContributorRequest.dismiss();
                                        acceptContributor();
                                    }
                                });
                                btnDecline.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogContributorRequest.dismiss();
                                        new DriverPressDecline().execute();
                                    }
                                });
                                dialogContributorRequest.show();
                            }
                            Log.e("IM", "Ye hi hai bhai!");

                        } catch (
                                Exception e) {
                            return;
                        }
                    }
                });
            }
        }
    };

    void startSearchingToll() {
        googleApiClient.connect();
        tollchange = true;
        tvSearchingToll.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideInUp).duration(700).playOn(tvSearchingToll);
        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
            speakText(getString(R.string.searching_toll));
        }
    }

    void stopSearchingToll() {
        distanceAfterLastNotification = 1000;
        distanceAfterLast = "0";
        googleApiClient.disconnect();
        googleMap.clear();
        markerAnimationStatus = false;
        tollFound = false;
        tollServiceRunning = false;
        noBarrierCheckInStatus = false;
        isNotified = false;
        multiTollFirstCheckIn = false;
        unRegisterSensorMovementListener();
        if (mapRipple != null)
            mapRipple.stopRippleMapAnimation();
        if (checkInAlertPlayer.isPlaying())
            checkInAlertPlayer.stop();

        if (tvSearchingToll.getVisibility() == View.VISIBLE) {
            YoYo.with(Techniques.SlideOutDown).duration(700).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    tvSearchingToll.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).playOn(tvSearchingToll);
        }

        if (llCheckInToll.getVisibility() == View.VISIBLE) {
            YoYo.with(Techniques.FadeOut).duration(300).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    llCheckInToll.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).playOn(llCheckInToll);
        }

        llLaneDetection.setVisibility(View.GONE);
        if (llNonGatedToll.getVisibility() == View.VISIBLE) {
            YoYo.with(Techniques.FadeOut).duration(700).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    llNonGatedToll.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).playOn(llNonGatedToll);
        }
        if (llGatedToll.getVisibility() == View.VISIBLE) {
            YoYo.with(Techniques.FadeOut).duration(700).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    llGatedToll.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            }).playOn(llGatedToll);

        }

        tvDynamicPriceReason.setVisibility(View.GONE);
        if (checkinNotifications.size() > 0) {
            checkinNotifications.clear();
            adapterCheckinNotification = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
//        context.registerReceiver(mMessageReceiver, new IntentFilter("unique_name"));
        Log.e("IM", "Resumed");
        Common.updatedToll = false;
        runOnUiThreadHandling = true;
        if (sharedPreferenceMain.isVoiceCommandEnabled()) {
            if (!Common.voiceCommandStartFragment) {
                setupXiaoBaiButton();
                setupAsr();
                setupTts();
                setupNlu();
                setupHotword();
                startHotword();
            }

            if (Common.voiceCommandStartFragment) {
                Timer voiceCommandDelay = new Timer();
                voiceCommandDelay.schedule(new TimerTask() {
                    public void run() {
                        //todo step 3
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setupXiaoBaiButton();
                                setupAsr();
                                setupTts();
                                setupNlu();
                                setupHotword();
                                startHotword();
                            }
                        });
                    }
                }, 3000);
            }
        }
//        if (Common.pairingRole.equals("Driver")) {
//            for (int i = 0; i < Common.conflictApproveRequests.size(); i++) {
//                String data = Common.conflictApproveRequests.get(i);
//                try {
//                    JSONObject jsonObject = new JSONObject(data);
//                    dialogMessageReceived = jsonObject.getString("body");
//                    acceptContributorServiceUrl = jsonObject.getString("url").replaceAll("\\\\", "");
//                    String params = jsonObject.getString("params").replaceAll("\\\\", "");
//                    final JSONObject paramsObject = new JSONObject(params);
//                    contributorName = paramsObject.getString("contributor_name");
//
//                    alertTollFee = new AlertDialog.Builder(context).create();
//                    alertTollFee.setTitle("HELLO!");
//                    alertTollFee.setMessage(dialogMessageReceived);
//                    alertTollFee.setCancelable(false);
//                    alertTollFee.setButton(AlertDialog.BUTTON_POSITIVE, "",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    try {
//                                        contributorPairingId = paramsObject.getString("contributor_pairing_id");
//                                        driverPairingId = paramsObject.getString("driver_pairing_id");
//                                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                            speakText(dialogMessageReceived);
//                                        }
//                                        new AcceptContributorService().execute();
//                                        Common.conflictApproveRequests.remove(Common.conflictApproveRequests.size() - 1);
//                                        alertTollFee.dismiss();
//
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//
//                    alertTollFee.setButton(AlertDialog.BUTTON_NEGATIVE, "DECLINE",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    new DriverPressDecline().execute();
//                                    Common.conflictApproveRequests.remove(Common.conflictApproveRequests.size() - 1);
//                                    alertTollFee.dismiss();
//                                }
//                            });
//
//                    alertTollFee.show();
//
//                } catch (JSONException e) {
//                }
//            }
//        }

        if (Common.groupId.equals("") && Common.pairingId.equals("")) {
            //Todo uncomment this
//            llParedUsers.setVisibility(View.GONE);
        }

//        //-------------- To remove after testing -----------------
//
//        pairingSessionsArrayList = new ArrayList<>();
//        modelPairingSessions = new ModelPairingSessions();
//        modelPairingSessions.setPairingId("0");
//        modelPairingSessions.setUserId("0");
//        modelPairingSessions.setName("Driver");
//        modelPairingSessions.setPrice("$5");
//        modelPairingSessions.setRole("Driver");
//        modelPairingSessions.setLeaveContributorVisible(true);
//        pairingSessionsArrayList.add(modelPairingSessions);
//
//        modelPairingSessions = new ModelPairingSessions();
//        modelPairingSessions.setPairingId("0");
//        modelPairingSessions.setUserId("0");
//        modelPairingSessions.setName("Irtiqa");
//        modelPairingSessions.setPrice("$5");
//        modelPairingSessions.setRole("Paid Contributor");
//        modelPairingSessions.setLeaveContributorVisible(true);
//        pairingSessionsArrayList.add(modelPairingSessions);
//
//        modelPairingSessions = new ModelPairingSessions();
//        modelPairingSessions.setPairingId("0");
//        modelPairingSessions.setUserId("0");
//        modelPairingSessions.setName("Shariq");
//        modelPairingSessions.setPrice("$0");
//        modelPairingSessions.setRole("Unpaid Contributor");
//        modelPairingSessions.setLeaveContributorVisible(true);
//        pairingSessionsArrayList.add(modelPairingSessions);
//
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
//        rvPairing.setLayoutManager(mLayoutManager);
//        rvPairing.setItemAnimator(new DefaultItemAnimator());
//        adapterPairingSession = new AdapterPairedUsers(context, pairingSessionsArrayList, myPairingRole);
//        adapterPairingSession.setClickListener(MapFragmentNew.this);
//
//        rvPairing.setAdapter(adapterPairingSession);
//
//        //-------------- To remove after testing -----------------

        if (stchCheckInTracking.isChecked())
            registerSensorMovementListener();
        if (stchCheckInTracking.isChecked())
            googleApiClient.connect();
        Common.isAppOnBackground = false;
        if (normalAlertPlayer != null && normalAlertPlayer.isPlaying())
            normalAlertPlayer.stop();
        playAlertSound();

        if (!Common.groupId.equals("") && Common.pairingRole.equals("Driver")) {
            new SaveTollCount().execute();
        }
    }

    private void registerSensorMovementListener() {
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_STATUS_ACCURACY_LOW);
    }

    private void unRegisterSensorMovementListener() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
//        context.unregisterReceiver(mMessageReceiver);
        unRegisterSensorMovementListener();
        Common.isAppOnBackground = true;
        playAlertSound();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
            }
        }
        if (requestCode == PERMISSION_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
            }
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        Log.e("Im", "Destroyed");
        mSpeechRecognizer = null;

        if (checkInAlertPlayer != null) {
            checkInAlertPlayer.stop();
        }

        socketHandler.mSocket.off("Conflict_Group", conflictGroup);
        socketHandler.mSocket.off("Conflict_Approve_Popup", conflictApprovePopup);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void acceptContributor() {
        try {
            driverPairingId = paramsObject.getString("driver_pairing_id");
            contributorPairingId = paramsObject.getString("contributor_pairing_id");
            contributorName = paramsObject.getString("contributor_name");
            new AcceptContributorService().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        int permissionCheck1 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE);
            return;
        }
    }

    void findAllViews() {
        lvToolLane = parentView.findViewById(R.id.lvToolLane);
        rvTollLane = parentView.findViewById(R.id.rvTollLane);
        rvNotifications = parentView.findViewById(R.id.rvNotifications);
        btnCheckIn = parentView.findViewById(R.id.btnCheckIn);
        btnCheckIn.setOnClickListener(this);
        cvVehicle = parentView.findViewById(R.id.cvVehicle);
        cvLog = parentView.findViewById(R.id.cvLog);
        cvLog.setOnClickListener(this);
        svLog = parentView.findViewById(R.id.svLog);
        btnTollCheckIn = parentView.findViewById(R.id.btnTollCheckIn);
        btnTollCheckIn.setOnClickListener(this);
        cvVehicle.setOnClickListener(this);
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        llNonGatedToll = parentView.findViewById(R.id.llNonGatedToll);
        llCheckInToll = parentView.findViewById(R.id.llCheckInToll);

        llGatedToll = parentView.findViewById(R.id.llGatedToll);
        llLaneDetection = parentView.findViewById(R.id.llLaneDetection);
        mMapView = parentView.findViewById(R.id.mapView);
        tvTollName = parentView.findViewById(R.id.tvTollName);
        ic_refresh = parentView.findViewById(R.id.ic_refresh);
        ic_refresh.setOnClickListener(this);
        tvTollPrice = parentView.findViewById(R.id.tvTollPrice);
        tvSearchingToll = parentView.findViewById(R.id.tvCheckInSearchingToll);
        tvCheckInPlateNo = parentView.findViewById(R.id.tvCheckInPlateNo);
        tvCheckInPlateNo.setText(sharedPreferenceUserDetails.getDefaultVehicleNo());
        tvTollMiles = parentView.findViewById(R.id.tvTollMiles);
        tvLog = parentView.findViewById(R.id.tvLog);
        tvDynamicPriceReason = parentView.findViewById(R.id.tvDynamicPriceReason);
        stchCheckInTracking = parentView.findViewById(R.id.stchCheckInTracking);
        llCheckInTollWithBarrierNotification = parentView.findViewById(R.id.llCheckInTollWithBarrierNotification);
        tvCheckInTollWithBarrierNotification = parentView.findViewById(R.id.tvCheckInTollWithBarrierNotification);
        cbCheckInTollWithBarrierMode = parentView.findViewById(R.id.cbCheckInTollWithBarrierMode);
        rvPairing = parentView.findViewById(R.id.rvPairing);
        ivExpand = parentView.findViewById(R.id.ivExpand);
        ivExpand.setOnClickListener(this);
        cVExpandCollapse = parentView.findViewById(R.id.cVExpandCollapse);
        ivDecrement = parentView.findViewById(R.id.ivDecrement);
        ivDecrement.setOnClickListener(this);
        ivIncrement = parentView.findViewById(R.id.ivIncrement);
        ivIncrement.setOnClickListener(this);
        tvIncrementDecrement = parentView.findViewById(R.id.tvIncrementDecrement);
        tvIncrementDecrement.setOnClickListener(this);
        cVSession = parentView.findViewById(R.id.cVSession);
        cVSession.setOnClickListener(this);
        llParedUsers = parentView.findViewById(R.id.llParedUsers);
        btnAddNonSmartUser = parentView.findViewById(R.id.btnAddNonSmartUser);
        btnAddNonSmartUser.setOnClickListener(this);
        clPairedUsers = parentView.findViewById(R.id.clPairedUsers);
    }

    public static void expand(final View v) {
        v.measure(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
//        pairedUsersViewHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                        : (int) (pairedUsersViewHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (pairedUsersViewHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        pairedUsersViewHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = pairedUsersViewHeight - (int) (pairedUsersViewHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (pairedUsersViewHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tvTollFound: {
                YoYo.with(Techniques.SlideOutDown)
                        .duration(700)
                        .playOn(parentView.findViewById(R.id.tvCheckInSearchingToll));
                llNonGatedToll.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInUp)
                        .duration(700)
                        .playOn(parentView.findViewById(R.id.llNonGatedToll));
                break;
            }

            case R.id.tvCheckIn: {
                YoYo.with(Techniques.SlideOutDown)
                        .duration(700)
                        .playOn(parentView.findViewById(R.id.llNonGatedToll));
                llGatedToll.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInUp)
                        .duration(700)
                        .playOn(parentView.findViewById(R.id.llGatedToll));
                break;
            }

            case R.id.cvVehicle: {
                Intent intent = new Intent(context, SelectVehicleActivity.class);
                intent.putExtra("CommingFrom", "MapFragmentNew");
                startActivityForResult(intent, 1);
                break;
            }
            case R.id.btnCheckIn: {
                tempButtonName = "CongestionCheckIn";
                new TollCheckInService().execute();
                break;
            }

            case R.id.tvIncrementDecrement: {
                if (myPairingRole.equals("Driver")) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
// ...Irrelevant code for customizing the buttons and title
//                    if (sharedPreference.isVoiceNotificationEnabled()) {
//                        speakText(getString(R.string.select_toll));
//                    }
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dialogView = inflater.inflate(R.layout.custom_toll_layout, null);
                    dialogBuilder.setView(dialogView);
                    alertDialog = dialogBuilder.show();
                    Button btnDone = dialogView.findViewById(R.id.btnDone);
                    TextView textView9 = dialogView.findViewById(R.id.textView9);
                    textView9.setVisibility(View.GONE);
                    alertDialog.setCancelable(false);
                    numberPicker = dialogView.findViewById(R.id.numberPicker);
                    numberPicker.setMinValue(1);
                    numberPicker.setMaxValue(10);
                    numberPicker.setValue(Common.selectedTollCount);
                    numberPicker.setBackgroundColor(Color.WHITE);
                    numberPicker.setWrapSelectorWheel(true);
                    numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                            //abc
                        }
                    });
                    btnDone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Common.selectedTollCount = numberPicker.getValue();
                            new SaveTollCount().execute();
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                break;
            }

            case R.id.btnTollCheckIn: {
                if (isNearToll) {
                    Intent intent = new Intent(context, CheckInActivity.class);
                    intent.putExtra("tollName", tollName);
                    intent.putExtra("dynamicFlag", dynamicPricingFlag);
                    if (dynamicPricingFlag.equals("1")) {
                        intent.putExtra("tollAmount", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice());
                        intent.putExtra("dynamicScenarioId", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getScenarioId());
                    } else
                        intent.putExtra("tollAmount", tollAmount);
                    intent.putExtra("laneNo", lvToolLane.getSelectedItemText());
                    intent.putExtra("laneId", Common.plazaDetails.getLanesDetails().get(selectedLaneIndex).getLane_id());
                    intent.putExtra("vehicleNo", sharedPreferenceUserDetails.getDefaultVehicleNo());
                    intent.putExtra("vehicleType", sharedPreferenceUserDetails.getDefaultVehicleType());
                    startActivityForResult(intent, 2);
                    llParedUsers.setVisibility(View.GONE);
                    if (!Common.plazaDetails.getType().equals("entry")) {
                        if (sharedPreferenceMain.isAutoTrackingEnabled())
                            stchCheckInTracking.setChecked(false);
                        else {
                            stchCheckInTracking.setChecked(false);
                        }
                    }

                    YoYo.with(Techniques.SlideOutDown)
                            .duration(700)
                            .playOn(parentView.findViewById(R.id.llGatedToll));
                    YoYo.with(Techniques.SlideOutDown)
                            .duration(700)
                            .playOn(parentView.findViewById(R.id.llNonGatedToll));

                } else
                    Toast.makeText(context, "Wait for your turn to Check-in", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.btnAddNonSmartUser: {
                Intent intent = new Intent(context, AddNonSmartPhoneUser.class);
                intent.putExtra("ComingFrom", "MapFragmentNew");
                startActivityForResult(intent, 5);
                break;
            }

            case R.id.ivExpand: {
                if (clPairedUsers.getVisibility() == View.GONE) {
                    expand(clPairedUsers);
//                    YoYo.with(Techniques.FadeInDown).duration(700).playOn(clPairedUsers);
                    ivExpand.setImageResource(R.drawable.svg_ic_upward);
                } else if (clPairedUsers.getVisibility() == View.VISIBLE) {
                    ivExpand.setImageResource(R.drawable.svg_ic_downward);
                    collapse(clPairedUsers);
                }
                break;
            }
            case R.id.ic_refresh: {
                new UpdateDriverCurrentLocation().execute();
                break;

            }

//            case R.id.btnSave: {
//                btnSave.setVisibility(View.GONE);
//                new SaveTollCount().execute();
//                break;
//            }

            case R.id.ivDecrement: {
//                btnSave.setVisibility(View.VISIBLE);
                if (Common.selectedTollCount > 1) {
                    Common.selectedTollCount--;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                }

                break;
            }

            case R.id.ivIncrement: {
//                btnSave.setVisibility(View.VISIBLE);
                if (Common.selectedTollCount < 10) {
                    Common.selectedTollCount++;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else
                    Toast.makeText(context, "Limit reached", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void startAsr() {
        if (runOnUiThreadHandling) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO: Set Language
                    recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
                    // Stop hotword detection in case it is still running
                    shouldDetect = false;

                    // TODO: Start ASR
                    speechRecognizer.startListening(recognizerIntent);
                }
            };

            Threadings.runInMainThread(getActivity(), runnable);
        }
    }


    private void speakText(String message) {
        if (textToSpeech != null) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void setupXiaoBaiButton() {
        String BUTTON_ACTION = "com.gowild.action.clickDown_action";

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BUTTON_ACTION);

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: Add action to do after button press is detected
                startAsr();
            }
        };
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void setupTts() {
        // TODO: Setup TTS
        textToSpeech = new TextToSpeech(context, this);
    }

    private void setupAsr() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Ignore
            }

            @Override
            public void onBeginningOfSpeech() {
                // Ignore
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Ignore
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Ignore
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                Log.e("asr", "Error: " + Integer.toString(error));
                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                    speakText("Please Say Any Command");
                }
                startHotword();
            }

            @Override
            public void onResults(Bundle results) {
                List<String> texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (texts == null || texts.isEmpty()) {
                    if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                        speakText("I Dont Know What You Say");
                    }

                } else {
                    String text = texts.get(0);
                    processResult(text);
                    startHotword();

                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }


    private void setupNlu() {
        // TODO: Change Client Access Token
        String clientAccessToken = "e40fa9b3a8824d439841612caeade14f";
        AIConfiguration aiConfiguration = new AIConfiguration(clientAccessToken,
                AIConfiguration.SupportedLanguages.English);
        AIDataService aiDataService = new AIDataService(aiConfiguration);
    }

    private void setupHotword() {
        shouldDetect = false;
        SnowboyUtils.copyAssets(context);

        // TODO: Setup Model File
        File snowboyDirectory = SnowboyUtils.getSnowboyDirectory();
        File model = new File(snowboyDirectory, "alexa_02092017.umdl");
        File common = new File(snowboyDirectory, "common.res");

        // TODO: Set Sensitivity
        snowboyDetect = new SnowboyDetect(common.getAbsolutePath(), model.getAbsolutePath());
        snowboyDetect.setSensitivity("0.60");
        snowboyDetect.applyFrontend(true);
    }

    private void startHotword() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                shouldDetect = true;
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                int bufferSize = 3200;
                byte[] audioBuffer = new byte[bufferSize];
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        16000,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                );

                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e("hotword", "audio record fail to initialize");
                    return;
                }

                audioRecord.startRecording();
                Log.d("hotword", "start listening to hotword");


                while (shouldDetect) {
                    audioRecord.read(audioBuffer, 0, audioBuffer.length);

                    short[] shortArray = new short[audioBuffer.length / 2];
                    ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray);

                    int result = snowboyDetect.runDetection(shortArray, shortArray.length);
                    if (result > 0) {
                        Log.d("hotword", "detected");
                        shouldDetect = false;
                    }
                }
                audioRecord.stop();
                audioRecord.release();
                Log.d("hotword", "stop listening to hotword");

                // TODO: Add action after hotword is detected
                startAsr();
            }
        };
        Threadings.runInBackgroundThread(runnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        new LatLng(location.getLatitude(), location.getLongitude());
        currentCoordinates = new LatLng(latitude, longitude);
        //todo step 2
        if (Common.multipleTollsGroup.equals("1") && multiTollFirstCheckIn) {
            Common.pairingRole = "Driver";
            if (!pairingServiceStatus && Common.pairingRole.equals("Driver")) {
                new EnablePairing().execute();
            }
        }
        if (initialCoordinates == null)
            initialCoordinates = currentCoordinates;

        if (!tollFound && !tollServiceRunning) {
            new FindNearestTollService().execute();
//            new DynamicPricingTestService().execute();
        } else if (tollFound) {
            plazaDistance = distFrom(latitude, longitude, Double.parseDouble(Common.plazaDetails.getLatitude()), Double.parseDouble((Common.plazaDetails.getLongitude())));
            updateLog("Toll: " + String.valueOf(plazaDistance) + "m");
            if (plazaDistance > 2100)
                tollFound = false;
            else if (plazaDistance < 2100) {
                if (plazaDistance < lastClosestplazaDistance) {
                    lastClosestplazaDistance = plazaDistance;
                }
                float difference = plazaDistance - lastClosestplazaDistance;

                if (difference > 80) {
//                    Toast.makeText(context, "Moved away from nearest toll", Toast.LENGTH_SHORT).show();
//                    stopSearchingToll();
//                    startSearchingToll();
//                    stchCheckInTracking.setChecked(false);
//                    stchCheckInTracking.setChecked(true);
//                    if (plazaMarker != null)
//                        plazaMarker.setVisible(false);
                }
//                if (difference < 80) {
//
//                    if (plazaMarker != null)
//                        plazaMarker.setVisible(false);
//                }
            }

            switch (Common.plazaDetails.getBarrier()) {
                case "yes": {
                    if (plazaDistance < 100 && autoLaneSelection && !detectingLane && Common.hasCheckInBalance) {

                        if (llLaneDetection.getVisibility() == View.GONE) {
                            llLaneDetection.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.FadeIn)
                                    .duration(700)
                                    .playOn(llLaneDetection);
                        }

                        ModelClassNearestLane object = new ModelClassNearestLane();

                        float currentDistance = 0, lastClosestDistance = 0;
                        for (int i = 0; i < Common.plazaDetails.getLanesDetails().size(); i++) {
                            detectingLane = true;
                            // int vote = 0;
                            for (int j = 0; j < Common.plazaDetails.getLanesDetails().get(i).getLanesCoordinates().size(); j++) {
                                currentDistance = distFrom(latitude, longitude, Double.parseDouble(Common.plazaDetails.getLanesDetails().get(i).getLanesCoordinates().get(j).getLatitude()), Double.parseDouble(Common.plazaDetails.getLanesDetails().get(i).getLanesCoordinates().get(j).getLongitude()));
                                Log.e("IM", "lane " + Integer.parseInt(Common.plazaDetails.getLanesDetails().get(i).getLane_no()) + " coordinate " + (j + 1) + " distance " + currentDistance + " m");
                                if (i == 0) {
                                    if (j == 0) {
                                        object = new ModelClassNearestLane(currentDistance, i);
                                        lastClosestDistance = currentDistance;
                                        Log.e("IM", "closest distance" + " = " + currentDistance);
                                        //                                        vote++;
                                    } else if (j > 0 && object.getDistance() > currentDistance) {
                                        Log.e("IM", object.getDistance() + " (closest distance) > " + currentDistance + " (current distance)");
                                        object = new ModelClassNearestLane(currentDistance, i);
                                        lastClosestDistance = currentDistance;
                                        Log.e("IM", "closest distance" + " = " + currentDistance);
                                        lvToolLane.setSelectedItem(object.getLane());
                                        selectedLaneIndex = object.getLane();
                                    }
                                } else {
                                    if (j == 0 && currentDistance < lastClosestDistance) {
                                        object = new ModelClassNearestLane(currentDistance, i);

                                    } else if (j > 0 && object.getDistance() > currentDistance && currentDistance < lastClosestDistance) {
                                        object = new ModelClassNearestLane(currentDistance, i);
                                        lastClosestDistance = currentDistance;
                                        Log.e("IM", "closest distance" + " = " + currentDistance);
                                        lvToolLane.setSelectedItem(object.getLane());
                                        selectedLaneIndex = object.getLane();
                                    }
                                }
                            }
                        }
                        detectingLane = false;
                        Common.foundTollLaneId = String.valueOf(object.getLane());
                        if (!isNotified && plazaDistance < 30) {

                            isNearToll = true;
                            getUserDirection(location);
                            if (shouldNotity) {
                                if (Common.isAppOnBackground)
                                    showCheckInNotification();

                                if (!nearTollNotificationAdded) {
                                    modelCheckinNotification = new ModelCheckinNotification();
                                    modelCheckinNotification.setText("Click checkin when you are ready to open gate  ");
                                    modelCheckinNotification.setType("NearGatedToll");
                                    checkinNotifications.add(modelCheckinNotification);

                                    if (adapterCheckinNotification == null) {
                                        adapterCheckinNotification = new AdapterCheckinNotification(context, checkinNotifications);
                                        rvNotifications.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                                        adapterCheckinNotification.setClickListener(MapFragmentNew.this);
                                        rvNotifications.setAdapter(adapterCheckinNotification);
                                        snapHelper = new LinearSnapHelper();
                                        snapHelper.attachToRecyclerView(rvNotifications);
                                    } else
                                        adapterCheckinNotification.notifyDataSetChanged();

                                    if (checkinNotifications.size() > 1) {

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                while (true) {
                                                    try {
                                                        Thread.sleep(6000);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (notificationScrollPosition == 0) {
                                                        rvNotifications.smoothScrollToPosition(notificationScrollPosition);
                                                        notificationScrollPosition++;
                                                    } else {
                                                        rvNotifications.smoothScrollToPosition(notificationScrollPosition);
                                                        notificationScrollPosition--;
                                                    }
                                                }
                                            }
                                        }).start();
                                    }

//                                if (sharedPreferenceMain.isTollNotificationSoundEnabled()) {
//                                    checkInAlertPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.notification);
//                                    checkInAlertPlayer.setLooping(true);
//                                    checkInAlertPlayer.start();
//                                    sharedPreferenceMain.setTollNotificationSound(true);
//                                }

                                    nearTollNotificationAdded = true;

                                }

//                                llCheckInTollWithBarrierNotification.setVisibility(View.VISIBLE);
//                                YoYo.with(Techniques.Flash)
//                                        .repeat(YoYo.INFINITE)
//                                        .duration(3000)
//                                        .playOn(tvCheckInTollWithBarrierNotification);
                                if (sharedPreferenceMain.isTollNotificationSoundEnabled())
                                    cbCheckInTollWithBarrierMode.setChecked(false);
                                else
                                    cbCheckInTollWithBarrierMode.setChecked(true);
                                isNotified = true;
                                playAlertSound();


                            }
                        }
                        updateLog("Is Notified: " + isNotified);
                        updateLog("--------------------------------------------");

                    }
                    tvTollMiles.setText(new DecimalFormat("#.##").format(plazaDistance * 0.000621371) + "mi");
                    break;
                }
                //Congestion Tolling Zone Work For Forward and Reverse Direction.
                case "no": {
                    tvTollMiles.setText(new DecimalFormat("#.##").format(plazaDistance * 0.000621371) + "mi");
                    if (isCongestionPlaza.equals("1")) {
                        if (plazaDistance < 30 && !noBarrierCheckInStatus) {
                            float lane1shortestDistance, lane2shortestDistance;
                            ArrayList<ModelClassLanesCoordinates>
                                    lane1 = Common.plazaDetails.getLanesDetails().get(0).getLanesCoordinates(),
                                    lane2 = Common.plazaDetails.getLanesDetails().get(1).getLanesCoordinates();
                            lane1shortestDistance = getShortestDistance(lane1);
                            lane2shortestDistance = getShortestDistance(lane2);

                            if (lane1shortestDistance > lane2shortestDistance) {
                                moveToLane2 = true;
                            } else {
                                moveToLane1 = true;
                            }

                            if (moveToLane2 && !moveToLane1) {
                                isBackward = true;
                            } else if (moveToLane1 && !moveToLane2) {
                                isForward = true;
                            }

                            if (moveToLane2 && lane1shortestDistance < lane2shortestDistance) {
                                moveToLane1 = true;
                            } else if (moveToLane1 && lane1shortestDistance > lane2shortestDistance) {
                                moveToLane2 = true;
                            }
                            if (moveToLane1 && moveToLane2 && isForward) {
                                checkInOrCheckOut = "in";
                                Common.checkInOrOut = "in";
//                                Toast.makeText(context, checkInOrCheckOut, Toast.LENGTH_SHORT).show();
                                new TollCheckInService().execute();
                            } else if (moveToLane1 && moveToLane2 && isBackward) {
                                checkInOrCheckOut = "out";
                                Common.checkInOrOut = "out";
//                                Toast.makeText(context, checkInOrCheckOut, Toast.LENGTH_SHORT).show();
                                new TollCheckInService().execute();
                            }
                        }
                    } else {
                        if (plazaDistance < 30 && !noBarrierCheckInStatus) {
                            float lane1shortestDistance, lane2shortestDistance;
                            ArrayList<ModelClassLanesCoordinates>
                                    lane1 = Common.plazaDetails.getLanesDetails().get(0).getLanesCoordinates(),
                                    lane2 = Common.plazaDetails.getLanesDetails().get(1).getLanesCoordinates();
                            lane1shortestDistance = getShortestDistance(lane1);
                            lane2shortestDistance = getShortestDistance(lane2);
                            if (lane1shortestDistance < lane2shortestDistance) {
                                moveToLane1 = true;
                            }
                            if (moveToLane1 && !moveToLane2) {
                                isForward = true;
                            }
                            if (moveToLane1 && lane1shortestDistance > lane2shortestDistance) {
                                moveToLane2 = true;
                            }

                            if (moveToLane1 && moveToLane2 && isForward) {
                                new TollCheckInService().execute();
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (positionMarker != null) {
            positionMarker.remove();
        }

        positionMarker = googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .icon((bitmapDescriptorFromVector(R.drawable.svg_ic_car_marker)))
        );
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentCoordinates));
        if (!markerAnimationStatus)
            cameraPosition = new CameraPosition.Builder().target(currentCoordinates).zoom(CAMERA_ZOOM_LEVEL).build();
        else
            cameraPosition = new CameraPosition.Builder().target(currentCoordinates).zoom(googleMap.getCameraPosition().zoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if (!markerAnimationStatus) {
                    mapRipple = new MapRipple(googleMap, currentCoordinates, context);
                    mapRipple.withNumberOfRipples(1);
                    mapRipple.withStrokewidth(2);
                    mapRipple.withDistance(250);
                    mapRipple.withRippleDuration(2000);
                    mapRipple.withTransparency(0.8f);
                    mapRipple.startRippleMapAnimation();
                    markerAnimationStatus = true;
                }
                isCameraZoomFinished = true;
            }

            @Override
            public void onCancel() {

            }
        });

        if (arrayAnnouncement.size() > 0) {
            if (distanceAfterLast.equals("1")) {
                distanceAfterLastNotification = distFrom(latitude, longitude, lastSpeakNotificationlat, lastSpeakNotificationLng);
            }

            if (distanceAfterLastNotification > 100 && (distanceAfterLast.equals("0") || distanceAfterLast.equals("1"))) {
                for (int i = 0; i < arrayAnnouncement.size(); i++) {
                    modelAnnouncemnt = arrayAnnouncement.get(i);
                    float distance = distFrom(latitude, longitude, modelAnnouncemnt.getLatitude(), modelAnnouncemnt.getLongitude());
                    if (distance < 50 && !modelAnnouncemnt.isStatusSpeak()) {
                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                            speakText(modelAnnouncemnt.getTitle());
                        }
                        lastSpeakNotificationlat = latitude;
                        lastSpeakNotificationLng = longitude;
                        modelAnnouncemnt.setStatusSpeak(true);
                        distanceAfterLast = "1";
                    }
                }
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(@DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    float getShortestDistance(ArrayList<ModelClassLanesCoordinates> lane) {
        float distance = 0, shortestDistance = 999999999;
        for (int i = 0; i < lane.size(); i++) {
            distance = distFrom(currentCoordinates.latitude, currentCoordinates.longitude, Double.parseDouble(lane.get(i).getLatitude()), Double.parseDouble(lane.get(i).getLongitude()));
            if (distance < shortestDistance) {
                shortestDistance = distance;
            }
        }
        return shortestDistance;
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("IM", connectionResult.getErrorMessage());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("IM", "Pairing Id: " + sharedPreferenceMain.getPairingId());
        Log.e("IM", "Group Id: " + sharedPreferenceMain.getGroupId());
        Log.e("IM", "STOP");
        shouldDetect = false;
        runOnUiThreadHandling = false;
        Threadings.stopInBackroundThread();
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        googleApiClient.disconnect();
    }

    @Override
    public void myActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            socketHandler.mSocket.on("Conflict_Group", conflictGroup);
            socketHandler.mSocket.on("Conflict_Approve_Popup", conflictApprovePopup);
            llParedUsers.setVisibility(View.VISIBLE);
            groupId = data.getStringExtra("GroupId");
            myPairingId = data.getStringExtra("PairingId");
            myPairingRole = data.getStringExtra("MyPairingRole");
            new GroupPairingListing().execute(groupId);
            if (myPairingRole.equals("Paid Contributor") || myPairingRole.equals("Unpaid Contributor") || myPairingRole.equals("Non Smart Phone")) {
                ic_refresh.setVisibility(View.GONE);
                ivIncrement.setVisibility(View.GONE);
                ivDecrement.setVisibility(View.GONE);
                if (stchCheckInTracking.isChecked())
                    stchCheckInTracking.setChecked(false);
                stchCheckInTracking.setChecked(true);
                tvIncrementDecrement.setBackgroundResource(android.R.color.transparent);


            } else if (myPairingRole.equals("Driver")) {
                ic_refresh.setVisibility(View.VISIBLE);
                if (stchCheckInTracking.isChecked())
                    stchCheckInTracking.setChecked(false);
                stchCheckInTracking.setChecked(true);
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == getActivity().RESULT_OK) {
                tvCheckInPlateNo.setText(data.getStringExtra("vehicleLiscenceNo"));
                if (stchCheckInTracking.isChecked()) {
                    stchCheckInTracking.setChecked(false);
                    stchCheckInTracking.setChecked(true);
                }
            }

        } else if (requestCode == 2) {
            if (data != null) {
                if (sharedPreferenceMain.isAutoTrackingEnabled())
                    stchCheckInTracking.setChecked(false);
                else {
                    stchCheckInTracking.setChecked(false);
                    stchCheckInTracking.setChecked(true);
                }
                new AlertDialog.Builder(getActivity()).setTitle("Check In Status")
                        .setMessage("You have just entered " + Common.plazaDetails.getPlaza_name() + ". Your balance will be charged on exit gate")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
            if (checkInAlertPlayer.isPlaying())
                checkInAlertPlayer.stop();
        } else if (requestCode == 3) {
            stchCheckInTracking.setChecked(true);
        } else if (resultCode == 4) {
            llParedUsers.setVisibility(View.VISIBLE);
            Toast.makeText(context, "Data Recieved", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 5) {
            groupId = data.getStringExtra("GroupId");
            myPairingId = data.getStringExtra("PairingId");
            myPairingRole = data.getStringExtra("MyPairingRole");
            new GroupPairingListing().execute(groupId);
        }

    }

    void updateLog(String log) {
        logCat += log;
        tvLog.setText(logCat);
        logCat += "\n";
        svLog.post(new Runnable() {
            @Override
            public void run() {
                svLog.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void showCheckInNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Check-in Ahead")
                        .setContentText("You are about to check-in, Get ready for your turn")
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
        isNotified = true;
    }

    private void showNotification() {
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Check In Status")
                    .setContentText("You have passed by " + Common.plazaDetails.getPlaza_name() + ". Your balance will be charged on exit gate.")
                    .setAutoCancel(true)
                    .setChannelId("com.amaxzadigital.tollpays");

            NotificationChannel channel = new NotificationChannel("com.amaxzadigital.tollpays",
                    "TollPays Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notification.setChannelId("com.amaxzadigital.tollpays");
            manager.createNotificationChannel(channel);
            manager.notify(0, notification.build());
        } else {

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getActivity())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Check In Status")
                            .setContentText("You have passed by " + Common.plazaDetails.getPlaza_name() + ". Your balance will be charged on exit gate.")
                            .setAutoCancel(true);

            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            manager.notify(0, builder.build());
        }
    }

    private void showDynamicPricingNotification(String text) {
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Dynamic Pricing Status")
                    .setContentText(text)
                    .setAutoCancel(true)
//              .setContentIntent(pendingIntent)
                    .setChannelId("com.amaxzadigital.tollpays");

            NotificationChannel channel = new NotificationChannel("com.amaxzadigital.tollpays",
                    "TollPays Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notification.setChannelId("com.amaxzadigital.tollpays");
            manager.createNotificationChannel(channel);
            manager.notify(0, notification.build());
        } else {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getActivity())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Dynamic Pricing Status")
                            .setContentText(text)
                            .setAutoCancel(true);

            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }
    }

    private void playAlertSound() {
        if (isNotified) {
            if (Common.isAppOnBackground && stchCheckInTracking.isChecked()) {
                if (!checkInAlertPlayer.isPlaying()) {
                    checkInAlertPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.notification);
                    checkInAlertPlayer.setLooping(true);
                    checkInAlertPlayer.start();
                }
            } else {
                if (isNotified && !checkInAlertPlayer.isPlaying() && sharedPreferenceMain.isTollNotificationSoundEnabled()) {
                    checkInAlertPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.notification);
                    checkInAlertPlayer.setLooping(true);
                    checkInAlertPlayer.start();
                } else if (!sharedPreferenceMain.isTollNotificationSoundEnabled()) {
                    checkInAlertPlayer.stop();
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float azimuth = Math.round(sensorEvent.values[0]);
        if (!markerAnimationStatus && cameraPosition != null && isCameraZoomFinished) {
            cameraPosition = CameraPosition.builder(googleMap.getCameraPosition()).target(currentCoordinates).bearing(azimuth).zoom(CAMERA_ZOOM_LEVEL).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else if (cameraPosition != null && isCameraZoomFinished) {
            cameraPosition = CameraPosition.builder(googleMap.getCameraPosition()).target(currentCoordinates).bearing(azimuth).zoom(googleMap.getCameraPosition().zoom).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onInit(int i) {
        if (i != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(Locale.UK);
        }
    }

    private void endSession() {
        dialogContributorRequest.setMessage("Do You Want to End Session");
        dialogContributorRequest.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EndGroupDialogOk();

                    }
                });

        dialogContributorRequest.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialogContributorRequest.show();
    }

    private void processResult(String command) {
        command = command.toLowerCase();
        if (command.indexOf("yes") != -1) {
            if (alertTollFee.isShowing()) {
                acceptContributor();
                alertTollFee.dismiss();
            }
        } else if (command.indexOf("no") != -1) {
            if (alertTollFee.isShowing()) {
                new DriverPressDecline().execute();
                alertTollFee.dismiss();
            }
        } else if (command.indexOf("increment toll") != -1 || command.indexOf("increment all") != -1 || command.indexOf("increment tall") != -1 || command.indexOf("incre mental") != -1 || command.indexOf("increment tool") != -1) {
            if (Common.selectedTollCount <= 10) {
                if (Common.selectedTollCount == 1) {
                    Common.selectedTollCount = 1 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 2) {
                    Common.selectedTollCount = 2 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 3) {
                    Common.selectedTollCount = 3 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 4) {
                    Common.selectedTollCount = 4 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 5) {
                    Common.selectedTollCount = 5 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 6) {
                    Common.selectedTollCount = 6 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 7) {
                    Common.selectedTollCount = 7 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 8) {
                    Common.selectedTollCount = 8 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 9) {
                    Common.selectedTollCount = 9 + 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                }
            }
            /*else */
            if (tollRemaining <= 10 && voiceCommandTollIncrementDecrement) {
                if (tollRemaining == 1) {
                    tollRemaining = 1 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 2) {
                    tollRemaining = 2 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 3) {
                    tollRemaining = 3 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 4) {
                    tollRemaining = 4 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 5) {
                    tollRemaining = 5 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 6) {
                    tollRemaining = 6 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 7) {
                    tollRemaining = 7 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 8) {
                    tollRemaining = 8 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 9) {
                    tollRemaining = 9 + 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                }
            } else {
                Toast.makeText(context, "Limit Reached", Toast.LENGTH_SHORT).show();
            }
        } else if (command.indexOf("decrement toll") != -1 || command.indexOf("decrement all") != -1 || command.indexOf("decrement tall") != -1 || command.indexOf("degree mental") != -1 || command.indexOf("decrement tool") != -1) {
            if (Common.selectedTollCount <= 10) {
                if (Common.selectedTollCount == 10) {
                    Common.selectedTollCount = 10 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 9) {
                    Common.selectedTollCount = 9 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 8) {
                    Common.selectedTollCount = 8 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 7) {
                    Common.selectedTollCount = 7 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 6) {
                    Common.selectedTollCount = 6 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 5) {
                    Common.selectedTollCount = 5 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 4) {
                    Common.selectedTollCount = 4 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 3) {
                    Common.selectedTollCount = 3 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                } else if (Common.selectedTollCount == 2) {
                    Common.selectedTollCount = 2 - 1;
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                }
            }
            if (tollRemaining <= 10 && voiceCommandTollIncrementDecrement) {
                if (tollRemaining == 10) {
                    tollRemaining = 10 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 9) {
                    tollRemaining = 9 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 8) {
                    tollRemaining = 8 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 7) {
                    tollRemaining = 7 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 6) {
                    tollRemaining = 6 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 5) {
                    tollRemaining = 5 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 4) {
                    tollRemaining = 4 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 3) {
                    tollRemaining = 3 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                } else if (tollRemaining == 2) {
                    tollRemaining = 2 - 1;
                    tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                }
            }
        } else if (command.indexOf("save") != -1) {
            new SaveTollCount().execute();
        } else if (command.indexOf("end group") != -1 || command.indexOf("and group") != -1) {
            endSession();
        } else if (command.indexOf("approve") != -1) {
            if (dialogContributorRequest.isShowing()) {
                EndGroupDialogOk();
                dialogContributorRequest.dismiss();
            }
        } else if (command.indexOf("dismiss") != -1) {
            if (dialogContributorRequest.isShowing()) {
                dialogContributorRequest.dismiss();
            }
        } else if (command.indexOf("leave ") != -1 || command.indexOf("lyf ") != -1 || command.indexOf("live ") != -1) {
            if (command.indexOf("first") != -1) {
                pairingSessionsArrayList.indexOf(1);
                new LeaveContributorGroup().execute(String.valueOf(1));
            } else if (command.indexOf("second") != -1) {
                pairingSessionsArrayList.indexOf(2);
                new LeaveContributorGroup().execute(String.valueOf(2));
            } else if (command.indexOf("third") != -1) {
                pairingSessionsArrayList.indexOf(3);
                new LeaveContributorGroup().execute(String.valueOf(3));
            } else if (command.indexOf("fourth") != -1) {
                pairingSessionsArrayList.indexOf(4);
                new LeaveContributorGroup().execute(String.valueOf(4));
            } else if (command.indexOf("fifth") != -1) {
                pairingSessionsArrayList.indexOf(5);
                new LeaveContributorGroup().execute(String.valueOf(4));
            }
        }

        if (command.indexOf("turn on tracking") != -1 || command.indexOf("turn on traking") != -1 || command.indexOf("turn on thracking") != -1) {
            if (!stchCheckInTracking.isChecked()) {
                stchCheckInTracking.setChecked(true);
            } else {
                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                    speakText(getString(R.string.tracking_on));
                }
            }
        } else if (command.indexOf("turn off tracking") != -1 || command.indexOf("turn off traking") != -1 || command.indexOf("turn off thracking") != -1) {
            if (stchCheckInTracking.isChecked()) {
                stchCheckInTracking.setChecked(false);
            } else {
                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                    speakText(getString(R.string.tracking_off));
                }
            }
        } else if (command.indexOf("tell me my balance") != -1) {
            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                speakText("your balance is" + Common.userBalance + "dollar");
            }
        } else if (command.indexOf("change vehicle") != -1) {
            if (alertDialogInvalidVehicle.isShowing()) {
                {
                    changeVehicle();
                }
            }
        } else if (command.indexOf("ok") != -1) {
            if (alertDialogInvalidVehicle.isShowing()) {
                cancelInvalidVehicle();
            }
        } else if (command.indexOf("insufficient balance") != -1) {
            if (alertDialogInsufficientBalance.isShowing()) {
                {
                    insufficientBalance();
                }
            }
        } else if (command.indexOf("cancel this") != -1) {
            if (alertDialogInsufficientBalance.isShowing()) {
                cancelInsufficientBalance();
            }
        }
    }

    private void EndGroupDialogOk() {
        new DriverEndSession().execute();
        Common.conflictApproveRequests.clear();
        pairingSessionsArrayList.remove(adapterPairingSession);
        adapterPairingSession.notifyDataSetChanged();
    }

    private void changeVehicle() {
        Intent intent = new Intent(context, SelectVehicleActivity.class);
        intent.putExtra("CommingFrom", "MapFragmentNew");
        startActivityForResult(intent, 1);
        alertDialogInvalidVehicle.dismiss();
        normalAlertPlayer.stop();
    }

    private void cancelInvalidVehicle() {
        normalAlertPlayer.stop();
        alertDialogInvalidVehicle.dismiss();

    }

    private void cancelInsufficientBalance() {
        normalAlertPlayer.stop();
        alertDialogInsufficientBalance.dismiss();

    }

    private void insufficientBalance() {
        Intent intent = new Intent(context, CurrentBalanceActivity.class);
        try {
            intent.putExtra("userBalance", object.getString("balance"));
            intent.putExtra("replenishmentAmount", Common.replenishmentAmount);

            Common.foundTollAmount = object.getString("amount");
            startActivity(intent);
            alertDialogInsufficientBalance.dismiss();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        normalAlertPlayer.stop();
    }

    void getUserDirection(Location location) {

        String[] coordNames = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        int coordIndex = Math.round(location.getBearing() / 45);
        int coordIndexLeft = coordIndex - 1;
        int coordIndexRight = coordIndex + 1;
        if (coordIndex < 0) {
            coordIndex = coordIndex + 8;
        }

        if (coordIndexLeft < 0) {
            coordIndexLeft = coordIndexLeft + 8;
        }

        if (coordIndexRight < 0) {
            coordIndexRight = coordIndexRight + 8;
        }

        if (coordIndex > 8) {
            coordIndex = coordIndex - 8;
        }

        if (coordIndexLeft > 8) {
            coordIndexLeft = coordIndexLeft - 8;
        }

        if (coordIndexRight > 8) {
            coordIndexRight = coordIndexRight - 8;
        }
        int plazaDirectionIndex = Arrays.asList(coordNames).indexOf(Common.plazaDetails.getPlaza_direction());
        updateLog("My Directions: " + coordNames[coordIndexLeft] + ", " + coordNames[coordIndex] + ", " + coordNames[coordIndexRight]);
        shouldNotity = plazaDirectionIndex == coordIndex || plazaDirectionIndex == coordIndexLeft || plazaDirectionIndex == coordIndexRight;
        updateLog("Should Notify: " + shouldNotity);
    }

    private void enabledLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enable Location");  // GPS not found
        builder.setMessage("Your location settings is off. Please enable location to use this app"); // Want to enable?
        builder.setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 3);
            }

        });
        builder.setNegativeButton("CANCEL", null);
        builder.create().show();
        return;
    }

    private Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

    //------- Click Listener of ivCross (Adapter Paired Users)-----//
    @Override
    public void onPairedUserEventClick(int position) {
        if (pairingSessionsArrayList.get(position).getRole().equals("Driver"))
            endSession();
        else
            LeaveContributor(position);
    }
//------ Ends Here------//

    private void LeaveContributor(final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage("Are you sure you want to leave contributor from group");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new LeaveContributorGroup().execute(String.valueOf(position));
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onLaneClick(int position) {
        rvTollLane.scrollToPosition(position + 1);
    }

    @Override
    public void onBellClick(int position) {
        if (sharedPreferenceMain.isTollNotificationSoundEnabled()) {
            if (checkInAlertPlayer != null)
                checkInAlertPlayer.stop();
            sharedPreferenceMain.setTollNotificationSound(false);
        } else {
            checkInAlertPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.notification);
            checkInAlertPlayer.setLooping(true);
            checkInAlertPlayer.start();
            sharedPreferenceMain.setTollNotificationSound(true);
        }
        adapterCheckinNotification.notifyDataSetChanged();


    }

    ////////////////////////////////////////////////////////////////////////Find Nearest Toll Service Ends/////////////////////////////////////////////////////
    class FindNearestTollService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        JSONObject dataObject;

        @SuppressLint("NewApi")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (alertDialogInvalidVehicle != null && alertDialogInvalidVehicle.isShowing()) {
                alertDialogInvalidVehicle.dismiss();
                normalAlertPlayer.stop();
            }
            tollServiceRunning = true;
            postParamenter = new HashMap<String, String>();
            postParamenter.put("latitude", "24.925161");
            postParamenter.put("longitude", "67.0973827");
//            postParamenter.put("latitude", String.valueOf(currentCoordinates.latitude));
//            postParamenter.put("longitude", String.valueOf(currentCoordinates.longitude));
//            postParamenter.put("date", currentDate);
        }

        @Override
        protected String doInBackground(String... strings) {

            // Send data
            try {
                TrueTime.build().initialize();
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/getNearstPlaza?access_token=" + Common.accessToken);
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
                Log.e("IM", e.toString());
            }
            return serviceResponse;
        }

        @SuppressLint("NewApi")
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            tollServiceRunning = false;
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    object = jsonObject.getJSONObject("data");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new GetAnnouncement().execute();
                        }
                    }, 3000);
                    //todo Toll Found Voice Notification
//                    if (!sharedPreferenceMain.isAutoTrackingEnabled() && !object.getString("plaza_id").equals(Common.lastCheckedInTollId)) {
                    Common.plazaDetails = new ModelClassPlazaDetails();
                    tollFound = true;
                    YoYo.with(Techniques.SlideOutDown)
                            .duration(700)
                            .withListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    tvSearchingToll.setVisibility(View.GONE);
                                    llCheckInToll.setVisibility(View.VISIBLE);
                                    YoYo.with(Techniques.SlideInUp).duration(700).playOn(llCheckInToll);
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            })
                            .playOn(tvSearchingToll);


                    tollName = object.getString("plaza_name");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                speakText(tollName + getString(R.string.toll_found));
                            }
                            this.cancel();

                        }
                    }, 1000);
                    plazaMarker = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(object.getString("latitude")), Double.parseDouble(object.getString("longitude"))))
                                    .icon(bitmapDescriptorFromVector(R.drawable.svg_plaza_green))
                                    .zIndex(1000)

                    );

                    Common.plazaDetails.setPlaza_id(object.getString("plaza_id"));
                    Common.plazaDetails.setAgency_id(object.getString("agency_id"));
                    Common.plazaDetails.setDynamic_price(object.getString("dynamic_price"));
                    Common.plazaDetails.setZone_name(object.getString("zone_name"));
                    Common.plazaDetails.setPlaza_name(object.getString("plaza_name"));
                    Common.plazaDetails.setPlaza_direction(object.getString("direction"));
                    updateLog("Vehicle Direction: " + Common.plazaDetails.getPlaza_direction());
                    Common.plazaDetails.setLatitude(object.getString("latitude"));
                    Common.plazaDetails.setLongitude(object.getString("longitude"));
                    Common.plazaDetails.setBarrier(object.getString("barrier"));
                    Common.plazaDetails.setType(object.getString("type"));
                    tollAmount = "$" + object.getString("amount");
                    isCongestionPlaza = object.getString("is_congestion_plaza");
                    congestionFormId = object.getString("congestion_form_id");
                    amount = object.getString("amount");
                    Toast.makeText(context, isCongestionPlaza, Toast.LENGTH_SHORT).show();

                    if (object.getString("insufficient_balance").equals("1")) {
                        //todo Insufficient balance Voice Notification
                        normalAlertPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.notification);
                        normalAlertPlayer.setLooping(true);
                        if (Common.isAppOnBackground)
                            normalAlertPlayer.start();
                        alertDialogInsufficientBalance.setTitle("Insufficient Balance");
                        alertDialogInsufficientBalance.setMessage("You have insufficient balance for " + tollName + ". If you don’t want to checkin, app will keep looking for the next toll as you drive forward.");
                        alertDialogInsufficientBalance.setButton(AlertDialog.BUTTON_POSITIVE, "Add Balance",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        insufficientBalance();
                                    }
                                });

                        alertDialogInsufficientBalance.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        cancelInsufficientBalance();
                                    }
                                });

                        alertDialogInsufficientBalance.show();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(getString(R.string.insufficient_balance));
                                }
                                this.cancel();
                            }
                        }, 3000);


                    } else if (object.getString("insufficient_balance").equals("0"))
                        Common.hasCheckInBalance = true;
                    JSONArray lanesArray = object.getJSONArray("lanes");
                    ModelClassLaneDetails laneDetails;
                    ModelClassLanesCoordinates lanesCoordinates;
                    JSONObject jsonobject, jsonobjectLanes;
                    ArrayList<ModelClassLanesCoordinates> arrayListLaneCoordinates;
                    ArrayList<ModelClassLaneDetails> arrayListLaneDetails = new ArrayList<>();
                    for (int i = 0; i < lanesArray.length(); i++) {
                        jsonobject = lanesArray.getJSONObject(i);
                        arrayListLaneCoordinates = new ArrayList<>();
                        JSONArray lanesCoordinatesJsonArray = jsonobject.getJSONArray("coordinates");

                        for (int j = 0; j < lanesCoordinatesJsonArray.length(); j++) {
                            jsonobjectLanes = lanesCoordinatesJsonArray.getJSONObject(j);
                            lanesCoordinates = new ModelClassLanesCoordinates(jsonobjectLanes.getString("latitude"), jsonobjectLanes.getString("longitude"));
                            arrayListLaneCoordinates.add(lanesCoordinates);
                        }

                        laneDetails = new ModelClassLaneDetails(jsonobject.getString("lane_no"), jsonobject.getString("direction_inverse"), jsonobject.getString("lane_id"), arrayListLaneCoordinates);
                        arrayListLaneDetails.add(laneDetails);
                        Common.plazaDetails.setLanesDetails(arrayListLaneDetails);
                    }
                    inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.custom_marker, null);
                    tvplazaPrice = view.findViewById(R.id.tvplazaPrice);
                    ivplazaImage = view.findViewById(R.id.ivplazaImage);
                    JSONArray plazasGery = object.getJSONArray("grey_plazas");
                    for (int i = 0; i < plazasGery.length(); i++) {
                        jsonobject = plazasGery.getJSONObject(i);
                        tvplazaPrice.setText("$" + jsonobject.getString("amount"));
                        ivplazaImage.setImageResource(R.drawable.svg_plaza_grey);
                        plazaMarker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(jsonobject.getString("latitude")), Double.parseDouble(jsonobject.getString("longitude"))))
                                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromView(view)))
                        );
                    }
                    JSONArray plazasYellow = object.getJSONArray("yellow_plazas");
                    for (int i = 0; i < plazasYellow.length(); i++) {
                        jsonobject = plazasYellow.getJSONObject(i);
                        tvplazaPrice.setText("$" + jsonobject.getString("amount"));
                        ivplazaImage.setImageResource(R.drawable.svg_ic_yellow);
                        plazaMarker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(jsonobject.getString("latitude")), Double.parseDouble(jsonobject.getString("longitude"))))
                                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromView(view)))
                        );
                    }
                    if (object.getString("barrier").equals("yes") /*&& Common.hasCheckInBalance*/) {
                        CharSequence[] values = new String[Common.plazaDetails.getLanesDetails().size()];
                        final ArrayList<ModelLane> arrayList = new ArrayList<>();
                        ModelLane modelLane;
                        for (int i = 0; i < Common.plazaDetails.getLanesDetails().size(); i++) {
                            values[i] = Common.plazaDetails.getLanesDetails().get(i).getLane_no();
                            modelLane = new ModelLane(Common.plazaDetails.getLanesDetails().get(i).getLane_no(), false);
                            arrayList.add(modelLane);
                            arrayList.add(modelLane);
                        }
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        rvTollLane.setLayoutManager(layoutManager);
                        adapterTollLane = new AdapterTollLane(context, arrayList);
                        adapterTollLane.setClickListener(MapFragmentNew.this);
                        rvTollLane.setAdapter(adapterTollLane);

                        if (snapHelper == null) {
                            snapHelper = new LinearSnapHelper();
                            snapHelper.attachToRecyclerView(rvTollLane);
                        }


//                        rvTollLane.addOnScrollListener(new RecyclerView.OnScrollListener() {
//
//                            @Override
//                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                                super.onScrollStateChanged(recyclerView, newState);
//                                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
//                                    View centerView = snapHelper.findSnapView(layoutManager);
//                                    int pos = layoutManager.getPosition(centerView);
//                                    arrayList.get(pos).setSelected(true);
//                                    adapterTollLane.notifyDataSetChanged();
//                                }
//                            }
//                        });


                        shouldNotity = false;
                        autoLaneSelection = true;
                        lvToolLane.setValues(values);
                        tvTollName.setText(tollName);
                        tvTollPrice.setText(tollAmount);
                        llGatedToll.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInUp).duration(700).playOn(parentView.findViewById(R.id.llGatedToll));
                    } else if (object.getString("barrier").equals("no")) {
                        moveToLane1 = false;
                        moveToLane2 = false;
                        isForward = false;
                        noBarrierCheckInStatus = false;
                        tvTollName.setText(tollName);
                        if (Common.pairingRole.equals("")) {
                            tvTollPrice.setText(tollAmount);
                        }
                        llNonGatedToll.setVisibility(View.VISIBLE);

                        YoYo.with(Techniques.SlideInUp)
                                .duration(700)
                                .playOn(parentView.findViewById(R.id.llNonGatedToll));
                    }

                    if (object.getString("is_vehicle_valid").equals("0")) {
                        //todo Invalid Vehicle Voice Notification
                        if (Common.groupId.equals("") || (!Common.groupId.equals("") && Common.pairingRole.equals("Driver"))) {
                            if (normalAlertPlayer != null && normalAlertPlayer.isPlaying())
                                normalAlertPlayer.stop();
                            normalAlertPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.notification);
                            normalAlertPlayer.setLooping(true);
                            if (Common.isAppOnBackground)
                                normalAlertPlayer.start();
                            alertDialogInvalidVehicle.setTitle("Invalid Vehicle");
                            alertDialogInvalidVehicle.setMessage(tollName + " doesn’t allow the selected vehicle type. If you don’t want to checkin, app will keep looking for the next toll as you drive forward.");
                            alertDialogInvalidVehicle.setButton(AlertDialog.BUTTON_POSITIVE, "Change Vehicle",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            changeVehicle();
                                        }
                                    });
                            alertDialogInvalidVehicle.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            cancelInvalidVehicle();
                                        }
                                    });

                            alertDialogInvalidVehicle.show();

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                        speakText(getString(R.string.invalid_vehicle));
                                    }
                                    this.cancel();
                                }
                            }, 3000);

                        }

                    }

                    //If dynamic Price is equal to 1 Then Dynamic Price Chart Service Hit
//                    if (Common.plazaDetails.getDynamic_price().equals("1")) {
//                        new DynamicPriceChart().execute();
//                    }

                    JSONArray jsonArray = object.getJSONArray("price_chart");

                    //hardcoded dynamic pricing time
//                    JSONArray jsonArray = new JSONArray("[{\"start_time\": \"05:00:00 PM\",\"end_time\": \"05:10:00 PM\",\"scenario_id\": \"33\",\"scenario_title\": \"rule no 8\",\"reason\": \"Congestion base pricing rule no 1 applied.\",\"price\": \"2.5\"}," +
//                            "{\"start_time\": \"05:11:00 PM\",\"end_time\": \"05:15:00 PM\",\"scenario_id\": \"33\",\"scenario_title\": \"rule no 8\",\"reason\": \"Congestion base pricing rule no 2 applied.\",\"price\": \"5.3\"}]");

//                    jsonArray = new JSONArray("[{\"start_time\": \"11:00:00 AM\",\"end_time\": \"09:30:30 PM\",\"scenario_id\": \"33\",\"scenario_title\": \"rule no 8\",\"reason\": \"Congestion base pricing rule no 8 applied.\",\"price\": \"2.5\"},{\"start_time\": \"05:30:00 PM\",\"end_time\": \"06:55:00 PM\",\"scenario_id\": \"33\",\"scenario_title\": \"rule no 8\",\"reason\": \"Congestion base pricing rule no 8 applied.\",\"price\": \"5.3\"},{\"start_time\": \"11:23:00 AM\",\"end_time\": \"11:25:00 AM\",\"scenario_id\": \"33\",\"scenario_title\": \"rule no 8\",\"reason\": \"Congestion base pricing rule no 8 applied.\",\"price\": \"5.3\"}]");


                    ////////////////Getting Current Date From Time Zone////////////////
//                    timeZone = TimeZone.getTimeZone(Common.plazaDetails.getZone_name());
//                    Date date = new Date();
//                    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                    dateFormat.setTimeZone(timeZone);
//                    currentDate = dateFormat.format(date);
//                    dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
//                    DateTimeZone.setDefault(DateTimeZone.forID(Common.plazaDetails.getZone_name()));
//                    DateTime currentDateTime = DateTime.now(DateTimeZone.forID(Common.plazaDetails.getZone_name()) );


                    // ------------- Dynamic Pricing Work starts

                    DateTime currentDateTime = new DateTime(TrueTime.now().getTime()).withZone(DateTimeZone.forID(Common.plazaDetails.getZone_name()));

//                    currentDateTime.withZone(DateTimeZone.forID(Common.plazaDetails.getZone_name()));

//                    currentTime = new Date();

                    if (jsonArray.length() > 0) {

                        pricingArrayList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            dataObject = jsonArray.getJSONObject(i);
                            modelDynamicPricing = new ModelDynamicPricing(dataObject.getString("start_time"), dataObject.getString("end_time"), dataObject.getString("scenario_id"), dataObject.getString("price"), dataObject.getString("reason"));
                            // Check if object timings is upcoming or not.
                            if (currentDateTime.isBefore(modelDynamicPricing.getFormatedEndTime()))
                                pricingArrayList.add(modelDynamicPricing);
                        }
                        if (pricingArrayList.size() > 0) {
                            for (int i = 0; i < pricingArrayList.size(); i++) {
                                modelDynamicPricing = pricingArrayList.get(i);
                                // if current time lie in any interval
                                if (modelDynamicPricing.getFormatedStartTime().isBefore(modelDynamicPricing.getCurrentDateTime()) && modelDynamicPricing.getFormatedEndTime().isAfter(modelDynamicPricing.getCurrentDateTime())) {
                                    dynamicPricingCurrentObjectIndex = i;
                                    dynamicPricingFlag = "1";

                                    if (llNonGatedToll.getVisibility() == View.VISIBLE) {
                                        tvTollPrice.setText("$" + modelDynamicPricing.getPrice());
                                        tvDynamicPriceReason.setVisibility(View.VISIBLE);
                                        tvDynamicPriceReason.setText(modelDynamicPricing.getReason());
                                    } else if (llGatedToll.getVisibility() == View.VISIBLE) {
                                        tvTollPrice.setText("$" + modelDynamicPricing.getPrice());
                                        tvDynamicPriceReason.setVisibility(View.VISIBLE);
                                        tvDynamicPriceReason.setText(modelDynamicPricing.getReason());
                                    }


                                    if (!Common.pairingId.equals("") && !Common.pairingRole.equals("")) {
                                        new SetPricePlaza().execute();
                                    }


                                    modelCheckinNotification = new ModelCheckinNotification();
                                    modelCheckinNotification.setText(modelDynamicPricing.getReason());
                                    modelCheckinNotification.setType("DynamicPricing");
                                    modelCheckinNotification.setTimer(modelDynamicPricing.getDifferenceFromCurrentTime()/*+TimeUnit.MILLISECONDS.toSeconds(modelDynamicPricing.getDifferenceFromCurrentTime())*/);
                                    checkinNotifications.add(modelCheckinNotification);
                                    if (adapterCheckinNotification == null) {
                                        adapterCheckinNotification = new AdapterCheckinNotification(context, checkinNotifications);
                                        rvNotifications.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                                        adapterCheckinNotification.setClickListener(MapFragmentNew.this);
                                        rvNotifications.setAdapter(adapterCheckinNotification);

                                        if (snapHelper == null) {
                                            snapHelper = new LinearSnapHelper();
                                            snapHelper.attachToRecyclerView(rvNotifications);
                                        }
                                    } else
                                        adapterCheckinNotification.notifyDataSetChanged();

                                    scheduleCurrentTimeFinishTimer(modelDynamicPricing.getDifferenceFromCurrentTime());
                                }
                                // if current time doesn't lie in any interval and this is last loop iteration
                                // then set timer for first object
                                if (i == pricingArrayList.size() - 1 && dynamicPricingFlag.equals("0")) {
                                    dynamicPricingCurrentObjectIndex = 0;
                                    modelDynamicPricing = pricingArrayList.get(0);
                                    //scheduleUpcomingIntervalTimer
                                    scheduleUpcomingIntervalTimer(modelDynamicPricing.getDifferenceFromCurrentTime());
                                }

                            }

                            if (checkinNotifications.size() > 1) {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (true) {
                                            try {
                                                Thread.sleep(6000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            if (notificationScrollPosition == 0) {
                                                rvNotifications.smoothScrollToPosition(notificationScrollPosition);
                                                notificationScrollPosition++;
                                            } else {
                                                rvNotifications.smoothScrollToPosition(notificationScrollPosition);
                                                notificationScrollPosition--;
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }

                        //Working
                        // When Day Was Ended Dynamic Price Service will be hit for next day data
                        try {
                            String dateString = "11:59:59 PM";
//                            String dateString = "03:50:30 AM";
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                            DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");


                            currentDateTime = new DateTime();
                            currentDate = dateFormatter.print(currentDateTime);

                            Date dayEnd = format.parse(currentDate + " " + dateString);
                            Date currentTime = new Date();
                            long dateConvertToMilliSecond = dayEnd.getTime() - currentTime.getTime();
//                            Toast.makeText(context, "" + dateConvertToMilliSecond, Toast.LENGTH_SHORT).show();

                            if (dateConvertToMilliSecond > 0) {
                                timerForUpcomingTimeInterval = new Timer();
                                timerForUpcomingTimeInterval.schedule(new TimerTask() {
                                    public void run() {
                                        /*getActivity().*/
                                        getActivity().runOnUiThread(new TimerTask() {
                                            @Override
                                            public void run() {
                                                Log.e("IM", "Your Day Was Ended");
                                                if (Common.plazaDetails.getDynamic_price().equals("1")) {
                                                    tollFound = false;
                                                    tollServiceRunning = false;
                                                    googleMap.clear();
                                                }
                                            }
                                        });
                                    }
                                }, dateConvertToMilliSecond);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }


                    // ------------- Dynamic Pricing Work ends


                    ///////////////////////////////Ends//////////////////////////////////

                    //---- If Group is Created and Pairing Role is Driver Then Set Prize Plaza Service Will Hit ----//
                    if (!sharedPreferenceMain.getGroupId().equals("") && Common.pairingRole.equals("Driver")) {
                        new SetPricePlaza().execute();
                    } else if (!groupId.equals("") && Common.pairingRole.equals("Driver")) {
                        new SetPricePlaza().execute();
                    }
//                    }
                    //------Ends Here-----//


                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } /*catch (ParseException e) {
                e.printStackTrace();
            }*/
        }
    }

    ///////////////////////////////////////////////////////////////////////Toll Check In Service Starts////////////////////////////////////////////////////////
    class TollCheckInService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            noBarrierCheckInStatus = true;
            postParamenter = new HashMap<>();
            if (tempButtonName.equals("EntryCheckIn")) {
                postParamenter.put("agency_id", "56");
                postParamenter.put("plaza_id", "303");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "1");
            } else if (tempButtonName.equals("ExitCheckIn")) {
                postParamenter.put("agency_id", "56");
                postParamenter.put("plaza_id", "304");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "1");
            } else if (tempButtonName.equals("EntryCheckInSimple")) {
                postParamenter.put("agency_id", "56");
                postParamenter.put("plaza_id", "303");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "0");
            } else if (tempButtonName.equals("ExitCheckInSimple")) {
                postParamenter.put("agency_id", "56");
//                postParamenter.put("plaza_id", "262");
                postParamenter.put("plaza_id", "304");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "0");
            } else if (tempButtonName.equals("EntryCheckInDynamic")) {
                postParamenter.put("agency_id", "56");
                postParamenter.put("plaza_id", "303");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "1 ");
                postParamenter.put("dynamic_flag", "0");
                postParamenter.put("dynamic_amount", "0");
                postParamenter.put("dynamic_scenario_id", "0");
            } else if (tempButtonName.equals("ExitCheckInDynamic")) {
                postParamenter.put("agency_id", "56");
//                postParamenter.put("plaza_id", "262");
                postParamenter.put("plaza_id", "304");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "1");
                postParamenter.put("dynamic_flag", "1");
                postParamenter.put("dynamic_amount", "3");
                postParamenter.put("dynamic_scenario_id", "39");
            } else if (tempButtonName.equals("JustEntryCheckInConflictDynamic")) {
                postParamenter.put("agency_id", "56");
                postParamenter.put("plaza_id", "303");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "1");
                postParamenter.put("dynamic_flag", "0");
                postParamenter.put("dynamic_amount", "0");
                postParamenter.put("dynamic_scenario_id", "0");
            } else if (tempButtonName.equals("JustExitCheckInConflictDynamic")) {
                postParamenter.put("agency_id", "56");
                postParamenter.put("plaza_id", "304");
                postParamenter.put("lane_id", "1");
                postParamenter.put("conflict_flag", "1");
                postParamenter.put("dynamic_flag", "1");
                postParamenter.put("dynamic_amount", "3");
                postParamenter.put("dynamic_scenario_id", "631");
            } else {
                postParamenter.put("agency_id", Common.plazaDetails.getAgency_id());
                postParamenter.put("plaza_id", Common.plazaDetails.getPlaza_id());
                postParamenter.put("lane_id", Common.plazaDetails.getLanesDetails().get(selectedLaneIndex).getLane_id());
//                if (!checkInOrCheckOut.equals("")) {
//            }
                if (sharedPreferenceMain.isPairingEnabled()) {
                    postParamenter.put("conflict_flag", "1");
                } else {
                    postParamenter.put("conflict_flag", "0");
                }
                postParamenter.put("dynamic_flag", dynamicPricingFlag);
                if (dynamicPricingFlag.equals("1")) {
                    Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                    postParamenter.put("dynamic_amount", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice().replace("$", ""));
                    postParamenter.put("dynamic_scenario_id", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getScenarioId());
                }
                if (isCongestionPlaza.equals("1")) {
                    postParamenter.put("congestion_module_flag", "1");
                    postParamenter.put("congestion_form_id", congestionFormId);
                    postParamenter.put("congestion_module_plaza_type", checkInOrCheckOut);
                    if (dynamicPricingFlag.equals("1")) {
                        postParamenter.put("congestion_module_amount", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice().replace("$", ""));
                    } else {

                        postParamenter.put("congestion_module_amount", amount);
                    }

                }
            }
        }

        @SuppressLint("NewApi")
        @Override
        protected String doInBackground(String... strings) {
            // Send data
            try {

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

        @SuppressLint("NewApi")
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speakText(getString(R.string.toll_checkin));
                }
            }
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    /**todo Un initialize conflictRestart**/
                    conflictRestart = 0;
                    moveToLane1 = false;
                    moveToLane2 = false;
                    isForward = false;
                    isBackward = false;
                    Common.saveCountMultiple = false;
                    pairingServiceStatus = false;
                    Common.foundTollId = Common.plazaDetails.getPlaza_id();
                    Common.lastCheckedInTollId = Common.plazaDetails.getPlaza_id();
                    Intent intent = new Intent(context, CongratulationActivity.class);
                    JSONObject object = jsonObject.getJSONObject("data");
                    if (object.getString("show").equals("entry") && Common.isAppOnBackground) {
                        showNotification();
                    }
                    if (dynamicPricingFlag.equals("1"))
                        intent.putExtra("TollAmount", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice());
                    else
                        intent.putExtra("TollAmount", object.getString("amount"));
                    intent.putExtra("AgencyName", object.getString("agency_name"));
                    intent.putExtra("LiscenceNo", object.getString("license_number"));
                    intent.putExtra("VehicleType", object.getString("vehicle_type"));
                    intent.putExtra("VehicleYear", object.getString("vehicle_year"));
                    intent.putExtra("VehicleModel", object.getString("vehicle_model"));
                    intent.putExtra("VehicleMake", object.getString("vehicle_make"));
                    intent.putExtra("VehicleState", object.getString("vehicle_state"));
                    intent.putExtra("EntryTime", object.getString("entry_time"));
                    intent.putExtra("EntryPlaza", object.getString("entry_plaza_name"));
                    intent.putExtra("EntryLane", object.getString("entry_lane_number"));
                    intent.putExtra("ExitTime", object.getString("exit_time"));
                    intent.putExtra("ExitPlaza", object.getString("exit_plaza_name"));
                    intent.putExtra("ExitLane", object.getString("exit_lane_number"));
                    intent.putExtra("PlazaType", object.getString("show"));
                    startActivity(intent);
                    exitPlazaName = object.getString("exit_plaza_name");
                    entryPlazaName = object.getString("entry_plaza_name");
                    transuctionId = object.getString("id");
                    if (sharedPreferenceMain.isAutoTrackingEnabled()) {
                        stchCheckInTracking.setChecked(false);
                    } else {
                        stchCheckInTracking.setChecked(false);
                        stchCheckInTracking.setChecked(true);

//                        llLaneDetection.setVisibility(View.INVISIBLE);
//                        if (llTollLayout.getVisibility() == View.VISIBLE)
//                            llTollLayout.setVisibility(View.GONE);
//                        if (llNonGatedToll.getVisibility() == View.VISIBLE)
//                            llNonGatedToll.setVisibility(View.GONE);
//                        if (llGatedToll.getVisibility() == View.VISIBLE) {
//                            llGatedToll.setVisibility(View.GONE);
//
//                        }
//
//                        tvSearchingToll.setVisibility(View.VISIBLE);

                    }

//                    if (!exitPlazaName.equals("") && entryPlazaName.equals("")) {
                    if (Common.pairingRole.equals("Driver") || Common.pairingRole.equals("")) {
                        new AxleMethodCheck().execute();
//                        }
                    }
                    if (sharedPreferenceMain.isPairingEnabled()) {
                        sharedPreferenceMain.setPairingEnabled(false);
                        //todo step 2.1
                        if (Common.pairingRole.equals("Driver")) {
                            if (Common.tollCountRemaining > 0) {
                                Common.multipleTollsGroup = "1";
                                multiTollFirstCheckIn = true;

                            } else {
                                Common.multipleTollsGroup = "0";
                            }
                        } else {
                            Common.multipleTollsGroup = "0";
                        }

                        llParedUsers.setVisibility(View.GONE);
                        sharedPreferenceMain.setPairingId("");
                        sharedPreferenceMain.setGroupId("");
                        ivIncrement.setVisibility(View.GONE);
                        ivDecrement.setVisibility(View.GONE);
                        Common.groupId = "";
                        Common.pairingId = "";
                    }

                    dynamicPricingFlag = "0";
                    if (pricingArrayList != null)
                        pricingArrayList.clear();
                    dynamicPricingCurrentObjectIndex = 0;

                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, jsonObject.getString("data"), Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////Group Pairing Service Starts////////////////////////////////////////////////
    class GroupPairingListing extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject, dataObject, listingObject;
        JSONArray jsonArray;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(Common.baseUrl + "api/v1/app/grouppairinglisting?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                postParamenter = new HashMap<>();
                postParamenter.put("group_id", strings[0]);
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
                    dataObject = jsonObject.getJSONObject("data");
                    Common.selectedTollCount = Integer.parseInt(dataObject.getString("toll_remaining"));
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
                    Common.tollCountRemaining = Integer.parseInt(dataObject.getString("toll_remaining"));

                    jsonArray = dataObject.getJSONArray("users");
                    if (Common.pairingRole.equals("Driver") || Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                        tollAmount = "$" + dataObject.getString("total_payment");
                        tvTollPrice.setText(tollAmount);
                    }
                    pairingSessionsArrayList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        listingObject = jsonArray.getJSONObject(i);
                        modelPairingSessions = new ModelPairingSessions();
                        modelPairingSessions.setPairingId(listingObject.getString("pairing_id"));
                        if (listingObject.getString("pairing_id").equals(myPairingId)) {
                            positionInGroup = i;
                        }
                        modelPairingSessions.setUserId(listingObject.getString("user_id"));
                        modelPairingSessions.setName(listingObject.getString("name"));
                        modelPairingSessions.setPrice(listingObject.getString("price"));
                        modelPairingSessions.setRole(listingObject.getString("display_role"));

                        if (listingObject.getString("user_id").equals(sharedPreferenceMain.getUserId()))
                            modelPairingSessions.setLeaveContributorVisible(true);
                        pairingSessionsArrayList.add(modelPairingSessions);
                    }

                    llParedUsers.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
                    rvPairing.setLayoutManager(mLayoutManager);
                    rvPairing.setItemAnimator(new DefaultItemAnimator());
                    adapterPairingSession = new AdapterPairedUsers(context, pairingSessionsArrayList, myPairingRole);
                    adapterPairingSession.setClickListener(MapFragmentNew.this);
                    rvPairing.setAdapter(adapterPairingSession);

                    if (jsonArray.length() == 1) {
                        new DriverEndSession().execute();
                        Common.conflictApproveRequests.clear();
                    }

                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Group Pairing Listing Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Driver Press Decline Service Starts////////////////////////////////////////////////////
    class DriverPressDecline extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/drivepressdecline?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                postParamenter = new HashMap<>();
                postParamenter.put("contributor_pairing_id", contributorPairingId);
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
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Driver Press Decline Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Set Prize Plaza Starts////////////////////////////////////////////////////
    class SetPricePlaza extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("IM", "plaza_id = " + Common.plazaDetails.getPlaza_id());
            Log.e("IM", "agency_id = " + Common.plazaDetails.getAgency_id());
            postParamenter = new HashMap<>();
            if (isCongestionPlaza.equals("1")) {
                postParamenter.put("congestion_module_flag", "1");
                if (dynamicPricingFlag.equals("1")) {
                    postParamenter.put("congestion_module_amount", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice().replace("$", ""));
                } else {
                    postParamenter.put("congestion_module_amount", amount);
                }
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/setpriceplaza?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                postParamenter = new HashMap<>();
                postParamenter.put("plaza_id", Common.plazaDetails.getPlaza_id());
                postParamenter.put("agency_id", Common.plazaDetails.getAgency_id());
                postParamenter.put("pairing_id", myPairingId);
                postParamenter.put("dynamic_flag", dynamicPricingFlag);
                if (dynamicPricingFlag.equals("1")) {
                    postParamenter.put("dynamic_amount", pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice());
                }

                //todo here

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

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                speakText(getString(R.string.prize_distrubuted));
                            }
                        }
                        this.cancel();
                    }
                }, 1000);
                if (status.equals("SUCCESS")) {
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    //todo step 18
                    if (Common.pairingRole.equals("Driver")) {
                        new SendPushGroup().execute();
                    }
                    new GroupPairingListing().execute(groupId);
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Set Prize Plaza Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Leave Contributor Group Service Starts////////////////////////////////////////////////////
    class LeaveContributorGroup extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                positionInGroup = Integer.parseInt(strings[0]);
                URL url = new URL(Common.baseUrl + "api/v1/app/leavecontributorgroup?access_token=" + Common.accessToken);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                postParamenter = new HashMap<>();
                postParamenter.put("group_id", groupId);
                if (myPairingRole.equals("Paid Contributor") || myPairingRole.equals("Unpaid Contributor")) {
                    postParamenter.put("pairing_id", myPairingId);

                } else if (myPairingRole.equals("Driver")) {
                    postParamenter.put("pairing_id", pairingSessionsArrayList.get(positionInGroup).getPairingId());
                }
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
                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                    speakText(getString(R.string.you_left_group));
                }
                if (status.equals("SUCCESS")) {
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    if (myPairingRole.equals("Paid Contributor") || myPairingRole.equals("Unpaid Contributor")) {
                        stchCheckInTracking.setChecked(false);
                        multiTollFirstCheckIn = false;
                        new DisablePairing().execute();
                        Common.updatedToll = false;
                        llParedUsers.setVisibility(View.GONE);
                    }

                    if (myPairingRole.equals("Driver")) {
                        pairingSessionsArrayList.remove(positionInGroup);
                        adapterPairingSession.notifyDataSetChanged();
                        new GroupPairingListing().execute(groupId);
                    }

                } else if (status.equals("FAILED")) {

                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }

                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Leave Contributor Group Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Driver Ends Group Service Starts////////////////////////////////////////////////////
    class DriverEndSession extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<>();
            postParamenter.put("group_id", groupId);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/endsessiondriver?access_token=" + Common.accessToken);
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
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    clPairedUsers.setVisibility(View.GONE);
                    new ResetDriverCounter().execute();
                    stchCheckInTracking.setChecked(false);
                    stchCheckInTracking.setChecked(true);
                    multiTollFirstCheckIn = false;
                    if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                        speakText(getString(R.string.group_ended));
                    }

                    if (Common.pairingRole.equals("Driver")) {
                        llParedUsers.setVisibility(View.GONE);
                        sharedPreferenceMain.setPairingEnabled(false);
                        Common.pairingId = "";
                        sharedPreferenceMain.setPairingId("");
                        if (!Common.groupId.equals("")) {
                            sharedPreferenceMain.setGroupId("");
                            Common.groupId = "";
                            groupId = "";
                        }
                        Common.updatedToll = false;
                        Common.multipleTollsGroup = "0";
                        Common.contributorResumeDialogShow = "0";
                        conflictRestart = 0;
                        Common.tollCountRemaining = 0;
                        Common.selectedTollCount = 0;
                        tvIncrementDecrement.setText("0");
                    }


                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }

                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Driver Ends Group Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Accept Contributor Request Service Starts////////////////////////////////////////////////////
    class AcceptContributorService extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Log.e("IM", "doInBackground");
                URL url = new URL(acceptContributorServiceUrl + "?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                postParamenter = new HashMap<>();
                postParamenter.put("driver_pairing_id", driverPairingId);
                postParamenter.put("contributor_pairing_id", contributorPairingId);
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
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
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    groupId = jsonObject.getString("group_id");
                    if (Common.plazaDetails != null && Common.pairingRole.equals("Driver")) {
                        new SetPricePlaza().execute();
                    } else {
                        new GroupPairingListing().execute(groupId);
                    }
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    } else if (jsonObject.getString("ref").equals("contributor_pairing_id_invalid")) {
                        Toast.makeText(context, contributorName + " status is paired off", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = jsonObject.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (JSONException e1) {
                Toast.makeText(context, e1.toString(), Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Accept Contributor Request Service Ends////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Resume Group When Accidently Mobile Off Service Starts////////////////////////////////////////////////////
    class UserInGroup extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject, jsonData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            // Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/UserInGroup?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                postParamenter = new HashMap<>();
                postParamenter.put("pairing_id", sharedPreferenceMain.getPairingId());
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
                    jsonData = jsonObject.getJSONObject("data");
                    String groupStatus = jsonData.getString("group_status");
                    if (groupStatus.equals("1")) {
                        myPairingRole = jsonData.getString("role");
                        groupId = jsonData.getString("group_id");
                        myPairingId = jsonData.getString("pairing_id");
                        tollRemaining = Integer.parseInt(jsonData.getString("toll_remaining"));
                        Common.pairingId = myPairingId;
                        Common.groupId = groupId;
                        sharedPreferenceMain.setPairingEnabled(true);
                        sharedPreferenceMain.setPairingId(myPairingId);
                        sharedPreferenceMain.setGroupId(groupId);
                        stchCheckInTracking.setChecked(true);
                        voiceCommandTollIncrementDecrement = true;
                        if (myPairingRole.equals("Paid Contributor") || myPairingRole.equals("Unpaid Contributor")) {
                            llParedUsers.setVisibility(View.VISIBLE);
                            ic_refresh.setVisibility(View.GONE);
                            tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                            ivIncrement.setVisibility(View.GONE);
                            ivDecrement.setVisibility(View.GONE);
                        } else if (myPairingRole.equals("Driver")) {
                            llParedUsers.setVisibility(View.VISIBLE);
                            ic_refresh.setVisibility(View.VISIBLE);
//                            ivIncrement.setVisibility(View.VISIBLE);
//                            ivDecrement.setVisibility(View.VISIBLE);
                            tvIncrementDecrement.setText(String.valueOf(tollRemaining));
                        }
                        new GroupPairingListing().execute(groupId);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                    speakText(getString(R.string.group_resumed));
                                }
                                this.cancel();
                            }
                        }, 1000);


                    } else if (groupStatus.equals("0")) {
                        new DisablePairing().execute();
                    }

                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Resume Group Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Toll Count Save Service Starts/////////////////////////////////////////////////
    class SaveTollCount extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<>();
            postParamenter.put("driver_pairing_id", Common.pairingId);
            postParamenter.put("group_id", Common.groupId);
            postParamenter.put("toll_remaining", String.valueOf(Common.selectedTollCount));
        }

        @Override
        protected String doInBackground(String... strings) {// Send data
            try {/* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/savedcountgroup?access_token=" + Common.accessToken);
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
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    tvIncrementDecrement.setText(String.valueOf(Common.selectedTollCount));
//                    if (Common.updatedToll) {
                    if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                        speakText(getString(R.string.toll_updated_driver));
//                        }
//                        Common.updatedToll = true;
                    }
//                    Common.tollCountRemaining = Common.selectedTollCount - 1;
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    getActivity().finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Toll Count Save Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Pairing Enable Service Starts/////////////////////////////////////////////////
    class EnablePairing extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pairingServiceStatus = true;
            postParamenter = new HashMap<>();
            postParamenter.put("role", Common.pairingRole);
            postParamenter.put("latitude", String.valueOf(currentCoordinates.latitude));
            postParamenter.put("longitude", String.valueOf(currentCoordinates.longitude));
//            googleApiClient.disconnect();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/pairingon?access_token=" + Common.accessToken);

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

        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");

                if (status.equals("SUCCESS")) {
                    myPairingId = jsonObject.getString("pairing_id");
                    Common.pairingId = myPairingId;
                    sharedPreferenceMain.setPairingEnabled(true);
                    if (Common.pairingRole.equals("Driver")) {
                        conflictRestartDialogForDriver = new ProgressDialog(context);
                        conflictRestartDialogForDriver.setMessage("Resuming group. Please wait...");
//                        conflictRestartDialogForDriver.setCancelable(false);
                        conflictRestartDialogForDriver.show();
                        timerForDriver = new Timer();
                        timerForDriver.schedule(new TimerTask() {
                            public void run() {
                                Log.e("IM", "Conflict Count: " + conflictRestart);
                                //todo step 3
                                if (conflictRestart == 3) {
                                    timerForDriver.cancel();
                                    conflictRestartdialog = true;
                                    conflictRestart = 0;
                                    //todo step 4
                                    conflictRestartDialogForDriver.dismiss();
                                    llParedUsers.setVisibility(View.VISIBLE);
                                    new ResetDriverCounter().execute();
                                    stchCheckInTracking.setChecked(false);
                                    Common.multipleTollsGroup = "0";
                                } else {
                                    new ConflictRestart().execute();
                                    conflictRestart++;
                                    Common.ignorePush = true;
                                    Common.multipleTollsGroup = "0";
                                }
                            }
                        }, 0, 40000);

                    } else if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                        //todo step 7
                        new GetNearbyUsersList().execute();
                    }
                } else if (status.equals("FAILED")) {
                    pairingServiceStatus = false;
                    sharedPreferenceMain.setPairingEnabled(true);
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            getActivity().finishAffinity();
                        }
                    }
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Enable Pairing Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Conflict Restart Service Starts/////////////////////////////////////////////////
    class ConflictRestart extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<>();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/conflictrestartpushsend?access_token=" + Common.accessToken);
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
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    Common.saveCountMultiple = true;
                } else if (status.equals("FAILED")) {

                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                    conflictRestartDialogForDriver.dismiss();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Conflict Restart Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Nearby Users List Service Starts/////////////////////////////////////////////////
    class GetNearbyUsersList extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;
        String id;
        boolean driverInRangeFlag = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            googleApiClient.connect();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("latitude", String.valueOf(currentCoordinates.latitude));
            postParamenter.put("longitude", String.valueOf(currentCoordinates.longitude));
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/getdriverlist?access_token=" + Common.accessToken);

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
                googleApiClient.disconnect();
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                        //todo step 10
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject dataObject = jsonArray.getJSONObject(i);
                            id = dataObject.getString("id");
                            if (id.equals(driverPairingId)) {
                                driverInRangeFlag = true;
                            }
                        }
                        if (driverInRangeFlag) {
                            new CheckBalance().execute();

                        } else {
                            conflictRestartDialogForContributor.dismiss();
                            AlertDialog dialog = new AlertDialog.Builder(context).create();
                            dialog.setTitle("Error");
                            dialog.setMessage("Driver is Not in Range");
                            dialog.show();
                            driverInRangeFlag = false;
                            Common.ignorePush = true;
                            new DisablePairing().execute();
                        }
                    }
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e1) {
                Toast.makeText(context, e1.toString(), Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Nearby Users List Service Ends//////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Check Balance Service Starts/////////////////////////////////////////////////
    class CheckBalance extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("pairing_id", driverPairingId);
            postParamenter.put("conflict_restart_flag", "0");
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Connection in process please wait.");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/popuptoshowforpairingdevice?access_token=" + Common.accessToken);


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
            progressDialog.dismiss();
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    String insufficient_balance = jsonObject.getString("insufficient_balance");
                    String is_password = "";
                    if (jsonObject.has("is_password"))
                        is_password = jsonObject.getString("is_password");


                    // If you have Insufficient balance then this Alert Dialog box will show
                    if (insufficient_balance.equals("1")) {
                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                            speakText(getString(R.string.insufficient_balance));
                        }
                        //todo step 11
                        dialogContributorRequest.setTitle("Insufficient Balance !");
                        dialogContributorRequest.setMessage(jsonObject.getString("message"));
                        dialogContributorRequest.setCancelable(true);
                        dialogContributorRequest.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //                                      modelPairingUsers.setLoading(false);
                                        dialogContributorRequest.dismiss();
                                    }
                                });

                        dialogContributorRequest.show();
                        Common.ignorePush = true;
                        new DisablePairing().execute();

                        // If you have sufficient balance and password is set then this Alert Dialog box show
                    } else if (insufficient_balance.equals("0") && is_password.equals("0")) {
                        //todo step 12
                        new CreateGroup().execute();
                    }
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();

                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (
                    JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Check Balance Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Group Create Service Starts/////////////////////////////////////////////////
    class CreateGroup extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            conflictRestartDialogForContributor.dismiss();

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/groupcreated?access_token=" + Common.accessToken);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                postParamenter = new HashMap<>();
                postParamenter.put("driver_pairing_id", driverPairingId);
                postParamenter.put("contributor_pairing_id", myPairingId);
                postParamenter.put("conflict_restart_flag", "0");
                postParamenter.put("password", "");

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
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    Common.contributorResumeDialogShow = "0";
                    Common.alertDialogResumeStatus = false;
                    conflictRestart = 5;
                    if (Common.pairingRole.equals("Paid Contributor") || Common.pairingRole.equals("Unpaid Contributor")) {
                        //todo step 13
                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                            speakText(getString(R.string.remaining_toll));
                        }

                        groupId = jsonObject.getString("group_id");
                        Common.groupId = groupId;
                        new GroupPairingListing().execute(groupId);
                        stchCheckInTracking.setChecked(true);
                    }
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }

                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            speakText(getString(R.string.invalid_password));
                        }
                    }
                    conflictRestartDialogForContributor.dismiss();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Group Create Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Reset Driver Counter Service Starts/////////////////////////////////////////////////
    class ResetDriverCounter extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(Common.baseUrl + "api/v1/app/resetdrivercounter?access_token=" + Common.accessToken);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                postParamenter = new HashMap<>();

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
//                    new DisablePairing().execute();
                    conflictRestart = 0;
                    Common.tollCountRemaining = 0;
                    Common.multipleTollsGroup = "0";
                    Common.selectedTollCount = 0;
                    tvIncrementDecrement.setText("0");
                    //todo step 5

                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }

                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            speakText(getString(R.string.invalid_password));
                        }
                    }
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Reset Driver Counter Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Disable Pairing Service Starts/////////////////////////////////////////////////
    class DisablePairing extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            postParamenter = new HashMap<String, String>();
            postParamenter.put("pairing_id", Common.pairingId);
            if (!Common.groupId.equals("")) {
                postParamenter.put("group_id", Common.groupId);
            }

        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/pairingoff?access_token=" + Common.accessToken);

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
                    socketHandler.mSocket.emit("Conflict_Group", jsonObject.getString("socket_emit_data"));
                    //todo step 6
                    sharedPreferenceMain.setPairingEnabled(false);
                    Common.pairingId = "";
                    sharedPreferenceMain.setPairingId("");
                    if (!Common.groupId.equals("")) {
                        sharedPreferenceMain.setGroupId("");
                        Common.groupId = "";
                    }
                } else if (status.equals("FAILED")) {

                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Common.groupId = "";

                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Disable Pairing Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Send Push When Driver Turn On His Tracking For Remaining Toll Service Starts/////////////////////////////////////////////////
    class SendPushGroup extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("group_id", groupId);
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/SendPushGroup?access_token=" + Common.accessToken);

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
                } else if (status.equals("FAILED")) {

                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
//                    String message = jsonObject.getString("message");
//                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    Common.groupId = "";

                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Send Push When Driver Turn On His Tracking For Remaining Toll Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Check Contributor In Range When Driver Turn On Tracking Service Starts/////////////////////////////////////////////////
    class CheckContributorInRange extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("pairing_id", Common.pairingId);
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                /* forming th java.net.URL object */

                URL url = new URL(Common.baseUrl + "api/v1/app/CheckContributorsInRange?access_token=" + Common.accessToken);

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
            Common.debugger.writeLog("Check Contributor In Range:", Common.formatStringToJson(serviceResponse));
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    //todo step 17
                    String EndSession = jsonObject.getString("end_session");
                    groupId = jsonObject.getString("group_id");
                    JSONArray jsonData = jsonObject.getJSONArray("execute_leave_contributor");
                    if (EndSession.equals("0")) {
                        if (!jsonData.equals("")) {
                            for (int i = 0; i < jsonData.length(); i++) {
                                JSONObject dataObject = jsonData.getJSONObject(i);
                                pairingId = dataObject.getString("pairing_id");
                                myPairingRole = dataObject.getString("role");
                                if (myPairingRole.equals("Paid Contributor") || myPairingRole.equals("Unpaid Contributor"))
                                    new LeaveContributorGroup().execute(String.valueOf(pairingId));
                            }
                        }
                    } else if (EndSession.equals("1")) {
                        new DriverEndSession().execute();

                    }
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
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
////////////////////////////////////////////////////////////////////////Check Contributor In Range When Driver Turn On Tracking Service Ends/////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Location Update of Driver only 1 time Service Starts/////////////////////////////////////////////////
    class UpdateCurrentLocation extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("pairing_id", myPairingId);
            postParamenter.put("latitude", String.valueOf(currentCoordinates.latitude));
            postParamenter.put("longitude", String.valueOf(currentCoordinates.longitude));
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/driverlongitudelatitudeupdate?access_token=" + Common.accessToken);

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
//                    Toast.makeText(context, "Service Hit", Toast.LENGTH_SHORT).show();
                    //todo step 16
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (Common.pairingRole.equals("Driver")) {
                                new CheckContributorInRange().execute();
                            }
                            this.cancel();
                        }
                    }, 3000);
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }

                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Location Update of Driver only 1 time Service Ends/////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////Driver Location Update Service When Pressing Refresh Button Service Starts/////////////////////////////////////////////////

    class UpdateDriverCurrentLocation extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("pairing_id", myPairingId);
            postParamenter.put("latitude", String.valueOf(currentCoordinates.latitude));
            postParamenter.put("longitude", String.valueOf(currentCoordinates.longitude));
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/driverlongitudelatitudeupdate?access_token=" + Common.accessToken);

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
                googleApiClient.disconnect();
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                if (status.equals("SUCCESS")) {
                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show();
                    if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                        speakText("Location updated");
                    }
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
/////////////////////////////////////////////////////////////////////Driver Location Update Service When Pressing Refresh Button Service Ends////////////////////

    ///////////////////////////////////////////////////////////////////////Announcement When you enter Any City or Place Service Starts//////////////////////////////
    class GetAnnouncement extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;
        JSONObject object;
        JSONObject latLngObject;
        JSONObject announcementObject;
        JSONArray latLngArray;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("latitude", String.valueOf(currentCoordinates.latitude));
            postParamenter.put("longitude", String.valueOf(currentCoordinates.longitude));
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/getannouncements?access_token=" + Common.accessToken);

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
                    JSONArray array = jsonObject.getJSONArray("data");
                    arrayAnnouncement = new ArrayList<>();
                    arrayAnnouncementLatLng = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        object = array.getJSONObject(i);
                        latLngObject = object.getJSONObject("loc");
                        announcementObject = object.getJSONObject("announcements");
                        latLngArray = latLngObject.getJSONArray("coordinates");
                        modelAnnouncemnt = new ModelAnnouncement();
                        modelAnnouncemnt.setLatitude(Double.parseDouble(latLngArray.get(0).toString()));
                        modelAnnouncemnt.setLongitude(Double.parseDouble(latLngArray.get(1).toString()));
                        modelAnnouncemnt.setAnnouncementId(object.getString("announcement_id"));
                        modelAnnouncemnt.setTitle(announcementObject.getString("title"));
                        arrayAnnouncement.add(modelAnnouncemnt);

                        modelAnnouncemntLatLng = new ModelAnnouncementLatLng();
                        modelAnnouncemntLatLng.setLatitude(Double.parseDouble(latLngArray.get(0).toString()));
                        modelAnnouncemntLatLng.setLongitude(Double.parseDouble(latLngArray.get(1).toString()));
                        arrayAnnouncementLatLng.add(modelAnnouncemntLatLng);
                    }

                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }

                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////Announcement When you enter Any City or Place Service Ends///////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////Service Hit For Notification Count Service Starts//////////////////////////////
    class DetailVehicleServiceForCount extends AsyncTask<String, String, String> {
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
                    MainActivity.totalCountInAppNotification = object.getString("total_count_in_app_notification");
                    MainActivity.badge = new QBadgeView(context);
                    if (MainActivity.totalCountInAppNotification.equals("")) {
                        MainActivity.ivNotification.setVisibility(View.GONE);
                        MainActivity.ivNotification.setVisibility(View.VISIBLE);
                        MainActivity.badge.setBadgeNumber(Integer.parseInt(MainActivity.totalCountInAppNotification));
                        MainActivity.badge.hide(true);
                    } else {
                        MainActivity.badge.setBadgeNumber(Integer.parseInt(MainActivity.totalCountInAppNotification));
                        MainActivity.badge.setBadgeBackgroundColor(Color.RED);
                        MainActivity.badge.setShowShadow(false);
                        MainActivity.badge.setBadgeTextSize(7, true);
                        MainActivity.badge.setGravityOffset(1, 6, true);
                        MainActivity.badge.bindTarget(MainActivity.ivNotification);
                    }

                } else if (status.equals("FAILED") && jsonObject.getString("ref").equals("invalid_token")) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getActivity().finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
///////////////////////////////////////////////////////////////////////Service Hit For Notification Count Service Ends///////////////////////////////////////////

    /////////////////////////////////////////////////////////////////Violation occur dialog for wrong axle selection Service Starts//////////////////////////////////
    class AxleMethodCheck extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        HashMap<String, String> postParamenter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("user_toll_transcation_id", transuctionId);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/AxleMethodCheck?access_token=" + Common.accessToken);

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
//                    Toast.makeText(context, "Succesfully Service Hit", Toast.LENGTH_SHORT).show();
                } else if (status.equals("FAILED")) {
                    if ((jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                        getActivity().finishAffinity();
                    }
//                    else if ((jsonObject.getString("ref").equals("violation_occur"))){
                    AlertDialog.Builder alertdialog = new AlertDialog.Builder(context);
                    alertdialog.setTitle("Alert");
                    alertdialog.setMessage(jsonObject.getString("message"));
                    alertdialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
                    alertdialog.show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                                speakText(getString(R.string.violation_occur));
                            }
                        }
                    }, 3000);
//                    }
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    class DynamicPricingTestService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        JSONObject dataObject;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                TrueTime.build().initialize();
                /* forming th java.net.URL object */
                URL url = new URL("https://toll-pays.firebaseio.com/dynamic-pricing.json");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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
                jsonArray = new JSONArray(serviceResponse);
                new FindNearestTollService().execute();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

    }

    /////////////////////////////////////////Dynamic Price Chart Service Ends//////////////////////////////
    void scheduleUpcomingIntervalTimer(long time) {
        Log.e("IM", "Upcoming Interval Seconds: " + TimeUnit.MILLISECONDS.toSeconds(time) + ", Next Interval Minutes: " + TimeUnit.MILLISECONDS.toMinutes(time));
        timerForUpcomingTimeInterval = new Timer();
        timerForUpcomingTimeInterval.schedule(new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        modelDynamicPricing = pricingArrayList.get(dynamicPricingCurrentObjectIndex);
                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                            speakText("Toll price updated to $" + modelDynamicPricing.getPrice());
                        }
                        if (Common.isAppOnBackground)
                            showDynamicPricingNotification("Toll price updated to $" + modelDynamicPricing.getPrice());
                        Toast.makeText(context, "Dynamic price applied", Toast.LENGTH_SHORT).show();
                        dynamicPricingFlag = "1";

                        if (llNonGatedToll.getVisibility() == View.VISIBLE) {
                            tvTollPrice.setText("$" + modelDynamicPricing.getPrice());
                            tvDynamicPriceReason.setVisibility(View.VISIBLE);
                            tvDynamicPriceReason.setText(modelDynamicPricing.getReason());
                        } else if (llGatedToll.getVisibility() == View.VISIBLE) {
                            tvTollPrice.setText("$" + modelDynamicPricing.getPrice());
                            tvDynamicPriceReason.setVisibility(View.VISIBLE);
                            tvDynamicPriceReason.setText(modelDynamicPricing.getReason());
                        }

                        if (!Common.pairingId.equals("") && !Common.pairingRole.equals("")) {
                            new SetPricePlaza().execute();
                        }

//                        dynamicPricingCurrentObjectIndex = i;

                        modelCheckinNotification = new ModelCheckinNotification();
                        modelCheckinNotification.setText(modelDynamicPricing.getReason());
                        modelCheckinNotification.setType("DynamicPricing");
                        modelCheckinNotification.setTimer(modelDynamicPricing.getDifferenceFromCurrentTime()/*+TimeUnit.MILLISECONDS.toSeconds(modelDynamicPricing.getDifferenceFromCurrentTime())*/);
                        checkinNotifications.add(modelCheckinNotification);
                        if (adapterCheckinNotification == null) {
                            adapterCheckinNotification = new AdapterCheckinNotification(context, checkinNotifications);
                            rvNotifications.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                            adapterCheckinNotification.setClickListener(MapFragmentNew.this);
                            rvNotifications.setAdapter(adapterCheckinNotification);

                            if (snapHelper == null) {
                                snapHelper = new LinearSnapHelper();
                                snapHelper.attachToRecyclerView(rvNotifications);
                            }
                        } else
                            adapterCheckinNotification.notifyDataSetChanged();


                        scheduleCurrentTimeFinishTimer(modelDynamicPricing.getDifferenceFromCurrentTime());
                    }
                });
            }
        }, time);
    }

    void scheduleCurrentTimeFinishTimer(long time) {
        Log.e("IM", "Current Interval Seconds: " + TimeUnit.MILLISECONDS.toSeconds(time) + ", Minutes: " + TimeUnit.MILLISECONDS.toMinutes(time));
        timerForCurrentTimeInterval = new Timer();
        timerForCurrentTimeInterval.schedule(new TimerTask() {
            public void run() {
                /*getActivity().*/
                getActivity().runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        for (int i = 0; i < checkinNotifications.size(); i++) {
                            if (checkinNotifications.get(i).getType().equals("DynamicPricing")) {
                                checkinNotifications.remove(i);
                            }
                        }
                        adapterCheckinNotification.notifyDataSetChanged();

                        if (llNonGatedToll.getVisibility() == View.VISIBLE) {
                            tvTollPrice.setText(tollAmount);
                            tvDynamicPriceReason.setVisibility(View.GONE);
                            dynamicPricingFlag = "0";
                        } else if (llGatedToll.getVisibility() == View.VISIBLE) {
                            tvTollPrice.setText(tollAmount);
                            tvDynamicPriceReason.setVisibility(View.GONE);
                            dynamicPricingFlag = "0";
                        }

                        Toast.makeText(context, "Base price applied", Toast.LENGTH_SHORT).show();
                        if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
                            speakText("Base price applied to toll which is " + tollAmount);
                        }

                        if (!Common.pairingId.equals("") && !Common.pairingRole.equals("")) {
                            new SetPricePlaza().execute();
                        }

                        if (dynamicPricingCurrentObjectIndex < pricingArrayList.size() - 1) {
                            dynamicPricingCurrentObjectIndex++;
                            modelDynamicPricing = pricingArrayList.get(dynamicPricingCurrentObjectIndex);
                            scheduleUpcomingIntervalTimer(modelDynamicPricing.getDifferenceFromCurrentTime());
                        }
                        //
                        else {
                            timerForCurrentTimeInterval.cancel();
                            if (timerForUpcomingTimeInterval != null)
                                timerForUpcomingTimeInterval.cancel();
                            Log.e("IM", "Dynamic Pricing Finished");

                        }
                    }
                });
            }
        }, time);
    }


}
