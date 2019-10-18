package com.amaxzadigital.tollpays.checkin;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;
import com.amaxzadigital.tollpays.login.LoginActivity;
import com.amaxzadigital.tollpays.settings.SharedPreferenceSettings;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;

public class CheckInActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String KEY_NAME = "toolPays";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    String passCode = "";
    TextView tvDialogTouchId1, tvDialogTouchId2, tvDialogTouchId3, tvDialogTouchId4, tvDialogTouchId5,
            tvDialogTouchId6, tvDialogTouchId7, tvDialogTouchId8, tvDialogTouchId9, tvDialogTouchId0,
            tvTouchIdPasscodeText, tvCheckInTollName, tvCheckInTollDate, tvCheckInTollAmount, tvCheckInVehicleNo, tvCheckInVehicleType, tvCheckInLaneNo;
    ImageView ivTouchIdPasscode1, ivTouchIdPasscode2, ivTouchIdPasscode3, ivTouchIdPasscode4;
    FrameLayout flDialogTouchIdBackspace, flCheckInContinue, flTouchIdMic;
    LinearLayout llCheckInTouchId;
    Button btnCheckInContinue;
    Context context = this;
    SharedPreferenceSettings sharedPreference;
    KeyguardManager keyguardManager;
    FingerprintManager fingerprintManager;
    CancellationSignal cancellationSignal;
    private Cipher cipher;
    private KeyStore keyStore;
    SharedPreferenceMain sharedPreferenceMain;
    private HashMap<String, Integer> captions;
    Intent intent;
    String transuctionId;
    ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        sharedPreferenceMain = SharedPreferenceMain.GetObject(context);
        sharedPreference = SharedPreferenceSettings.GetObject(context);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        getSupportActionBar().setTitle("Check In");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.cross);
        tvDialogTouchId1 = (TextView) findViewById(R.id.tvDialogTouchId1);
        tvDialogTouchId2 = (TextView) findViewById(R.id.tvDialogTouchId2);
        tvDialogTouchId3 = (TextView) findViewById(R.id.tvDialogTouchId3);
        tvDialogTouchId4 = (TextView) findViewById(R.id.tvDialogTouchId4);
        tvDialogTouchId5 = (TextView) findViewById(R.id.tvDialogTouchId5);
        tvDialogTouchId6 = (TextView) findViewById(R.id.tvDialogTouchId6);
        tvDialogTouchId7 = (TextView) findViewById(R.id.tvDialogTouchId7);
        tvDialogTouchId8 = (TextView) findViewById(R.id.tvDialogTouchId8);
        tvDialogTouchId9 = (TextView) findViewById(R.id.tvDialogTouchId9);
        tvDialogTouchId0 = (TextView) findViewById(R.id.tvDialogTouchId0);
        tvCheckInTollName = (TextView) findViewById(R.id.tvCheckInTollName);
//        tvCheckInTollName.setText(getIntent().getExtras().getString("tollName"));
        tvCheckInTollDate = (TextView) findViewById(R.id.tvCheckInTollDate);
        DateFormat df = new DateFormat();
        tvCheckInTollDate.setText(DateFormat.format("dd/MM/yyyy hh:mm a", new java.util.Date()));
        tvCheckInTollAmount = (TextView) findViewById(R.id.tvCheckInTollAmount);
        tvCheckInTollAmount.setText(getIntent().getExtras().getString("tollAmount"));
        tvCheckInVehicleNo = (TextView) findViewById(R.id.tvCheckInVehicleNo);
        tvCheckInVehicleNo.setText(getIntent().getExtras().getString("vehicleNo"));
        tvCheckInVehicleType = (TextView) findViewById(R.id.tvCheckInVehicleType);
        tvCheckInVehicleType.setText(getIntent().getExtras().getString("vehicleType"));
        tvCheckInLaneNo = (TextView) findViewById(R.id.tvCheckInLaneNo);
        tvCheckInLaneNo.setText("Lane No. " + getIntent().getExtras().getString("laneNo"));
        tvTouchIdPasscodeText = (TextView) findViewById(R.id.tvTouchIdPasscodeText);
        ivTouchIdPasscode1 = (ImageView) findViewById(R.id.ivTouchIdPasscode1);
        ivTouchIdPasscode2 = (ImageView) findViewById(R.id.ivTouchIdPasscode2);
        ivTouchIdPasscode3 = (ImageView) findViewById(R.id.ivTouchIdPasscode3);
        ivTouchIdPasscode4 = (ImageView) findViewById(R.id.ivTouchIdPasscode4);
        flDialogTouchIdBackspace = (FrameLayout) findViewById(R.id.flTouchIdBackspace);
        flDialogTouchIdBackspace.setOnClickListener(this);
        flCheckInContinue = (FrameLayout) findViewById(R.id.flCheckInContinue);
        flTouchIdMic = (FrameLayout) findViewById(R.id.flTouchIdMic);
        flTouchIdMic.setOnClickListener(this);
        llCheckInTouchId = (LinearLayout) findViewById(R.id.llCheckInTouchId);
        if (sharedPreference.getPasscode().equals("")) {
            llCheckInTouchId.setVisibility(View.GONE);
            flCheckInContinue.setVisibility(View.VISIBLE);
        } else
            recieveFingerPrint();
        btnCheckInContinue = (Button) findViewById(R.id.btnCheckInContinue);
        btnCheckInContinue.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void getPassCode(View v) {
        switch (v.getId()) {
            case R.id.tvDialogTouchId1: {
                if (passCode.length() < 4) {
                    passCode += tvDialogTouchId1.getText().toString();
                }
                break;
            }
            case R.id.tvDialogTouchId2: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId2.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId3: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId3.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId4: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId4.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId5: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId5.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId6: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId6.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId7: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId7.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId8: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId8.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId9: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId9.getText().toString();
                break;
            }
            case R.id.tvDialogTouchId0: {
                if (passCode.length() < 4)
                    passCode += tvDialogTouchId0.getText().toString();
                break;
            }
        }
        switch (passCode.length()) {
            case 1:
                ivTouchIdPasscode1.setImageResource(R.drawable.select);
                break;
            case 2:
                ivTouchIdPasscode2.setImageResource(R.drawable.select);
                break;
            case 3:
                ivTouchIdPasscode3.setImageResource(R.drawable.select);
                break;
            case 4:
                ivTouchIdPasscode4.setImageResource(R.drawable.select);
                break;
        }
        Log.e("IM", passCode);
        if (passCode.length() == 4 && passCode.equals(sharedPreference.getPasscode())) {
            new TollCheckInService().execute();
//            llCheckInTouchId.setVisibility(View.GONE);
//            flCheckInContinue.setVisibility(View.VISIBLE);
//            YoYo.with(Techniques.FadeIn)
//                    .duration(700)
//                    .playOn(flCheckInContinue);
            if (cancellationSignal != null)
                cancellationSignal.cancel();
        } else if (passCode.length() == 4 && !passCode.equals(sharedPreference.getPasscode())) {
            passCode = "";
            ivTouchIdPasscode1.setImageResource(R.drawable.deselect);
            ivTouchIdPasscode2.setImageResource(R.drawable.deselect);
            ivTouchIdPasscode3.setImageResource(R.drawable.deselect);
            ivTouchIdPasscode4.setImageResource(R.drawable.deselect);
            YoYo.with(Techniques.Shake)
                    .duration(700)
                    .playOn(tvTouchIdPasscodeText);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.flTouchIdBackspace: {
                if (passCode.length() > 0) {
                    switch (passCode.length()) {
                        case 1:
                            ivTouchIdPasscode1.setImageResource(R.drawable.deselect);
                            break;
                        case 2:
                            ivTouchIdPasscode2.setImageResource(R.drawable.deselect);
                            break;
                        case 3:
                            ivTouchIdPasscode3.setImageResource(R.drawable.deselect);
                            break;
                        case 4:
                            ivTouchIdPasscode4.setImageResource(R.drawable.deselect);
                            break;
                    }
                    passCode = passCode.substring(0, passCode.length() - 1);
//                Log.e("IM", passCode.length() + "");
                    Log.e("IM", passCode);
                }
                break;
            }
            case R.id.btnCheckInContinue: {
                new TollCheckInService().execute();
                break;
            }
            case R.id.flTouchIdMic: {
                break;
            }
        }
    }

    private void promptSpeechInput() {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }


        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }


        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void recieveFingerPrint() {
        // Check whether the device has a Fingerprint sensor.
        if (!fingerprintManager.isHardwareDetected()) {
            /**
             * An error message will be displayed if the device does not contain the fingerprint hardware.
             * However if you plan to implement a default authentication method,
             * you can redirect the user to a default authentication activity from here.
             * Example:
             * Intent intent = new Intent(this, DefaultAuthenticationActivity.class);
             * startActivity(intent);
             */
            Toast.makeText(context, "Your Device does not have a Fingerprint Sensor", Toast.LENGTH_SHORT).show();
        } else {
            // Checks whether fingerprint permission is set on manifest
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Fingerprint authentication permission not enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Check whether at least one fingerprint is registered
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    Toast.makeText(context, "Register at least one fingerprint in Settings", Toast.LENGTH_SHORT).show();
                } else {
                    // Checks whether lock screen security is enabled or not
                    if (!keyguardManager.isKeyguardSecure()) {
                        Toast.makeText(context, "Lock screen security not enabled in Settings", Toast.LENGTH_SHORT).show();
                    } else {

                        generateKey();

                        if (cipherInit()) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            FingerprintHandler helper = new FingerprintHandler(context);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    Toast.makeText(context, result.get(0), Toast.LENGTH_SHORT).show();
                    if (result.get(0).equals(sharedPreference.getPasscode())) {
                        llCheckInTouchId.setVisibility(View.GONE);
                        flCheckInContinue.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeIn)
                                .duration(700)
                                .playOn(flCheckInContinue);
                    } else {
                        YoYo.with(Techniques.Shake)
                                .duration(700)
                                .playOn(tvTouchIdPasscodeText);
                    }
                }
                break;
            }

        }
    }

    private void showNotification() {
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Check In Status")
                        .setContentText("You have been checked in successfully!")
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    private void processResult(String command) {
        command = command.toLowerCase();
        if (command.indexOf("enter passcode") != -1) {
            promptSpeechInput();
        }
        if (command.indexOf("continue") != -1) {
            new TollCheckInService().execute();
        }
    }

    // Web service for Toll CheckIn

    @RequiresApi(api = Build.VERSION_CODES.M)
    public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {


        private Context context;

        // Constructor
        public FingerprintHandler(Context mContext) {
            context = mContext;
        }

        public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
            cancellationSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }


        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            this.update("Fingerprint Authentication error\n" + errString, false);
        }


        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            this.update("Fingerprint Authentication help\n" + helpString, false);
        }


        @Override
        public void onAuthenticationFailed() {
            this.update("Fingerprint Authentication failed.", false);
        }


        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            this.update("Fingerprint Authentication succeeded.", true);
        }


        public void update(String e, Boolean success) {
            Log.e("IM", e);
            if (success) {
                llCheckInTouchId.setVisibility(View.GONE);
                flCheckInContinue.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(700)
                        .playOn(flCheckInContinue);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Common.isAppOnBackground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Common.isAppOnBackground = false;
    }

    class TollCheckInService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("agency_id", Common.plazaDetails.getAgency_id());
            postParamenter.put("plaza_id", Common.plazaDetails.getPlaza_id());
            postParamenter.put("lane_id", getIntent().getExtras().getString("laneId"));
            if (sharedPreferenceMain.isPairingEnabled())
                postParamenter.put("conflict_flag", "1");
            else
                postParamenter.put("conflict_flag", "0");
            postParamenter.put("dynamic_flag", getIntent().getExtras().getString("dynamicFlag"));
            if (getIntent().getExtras().getString("dynamicFlag").equals("1")) {
                postParamenter.put("dynamic_amount", getIntent().getExtras().getString("tollAmount").replace("$",""));
                postParamenter.put("dynamic_scenario_id", getIntent().getExtras().getString("dynamicScenarioId"));
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            // Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/checkin?access_token=" + Common.accessToken);
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
                    JSONObject object = jsonObject.getJSONObject("data");
                    transuctionId = object.getString("id");

                    MapFragmentNew.conflictRestart = 0;
                    Common.saveCountMultiple = false;
                    MapFragmentNew.pairingServiceStatus = false;
                    Common.foundTollId = Common.plazaDetails.getPlaza_id();
                    Common.lastCheckedInTollId = Common.plazaDetails.getPlaza_id();

                    if (sharedPreferenceMain.isPairingEnabled()) {
                        sharedPreferenceMain.setPairingEnabled(false);
                        //todo step 2.1
                        if (Common.pairingRole.equals("Driver")) {
                            if (Common.tollCountRemaining > 0) {
                                Common.multipleTollsGroup = "1";
                                MapFragmentNew.multiTollFirstCheckIn = true;

                            } else {
                                Common.multipleTollsGroup = "0";
                            }
                        } else {
                            Common.multipleTollsGroup = "0";
                        }
                        sharedPreferenceMain.setPairingId("");
                        sharedPreferenceMain.setGroupId("");
                        Common.groupId = "";
                        Common.pairingId = "";
                    }


                    if (Common.pairingRole.equals("Driver") || Common.pairingRole.equals("")) {
                        new AxleMethodCheck().execute();
//                        }
                    }

                    progressDialog.dismiss();
                    sharedPreferenceMain.setPairingEnabled(false);
                    sharedPreferenceMain.setPairingId("");
                    sharedPreferenceMain.setGroupId("");
                    if (object.getString("show").equals("entry"))

                    {
                        if (Common.isAppOnBackground) {
                            showNotification();
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("PlazaType", object.getString("show"));
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else {
                        Intent intent = new Intent(context, CongratulationActivity.class);
                        if (getIntent().getExtras().getString("dynamicFlag").equals("1")) {
                            intent.putExtra("TollAmount", getIntent().getExtras().getString("tollAmount"));
                        } else
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
                        finish();
                    }


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
                        finishAffinity();
                    }
//                    else if ((jsonObject.getString("ref").equals("violation_occur"))){
                    AlertDialog.Builder alertdialog = new AlertDialog.Builder(context);
                    alertdialog.setTitle("Alert");
                    alertdialog.setMessage(jsonObject.getString("message"));
                    alertdialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            progressDialog.dismiss();
                            //do things
                        }
                    });
//                    alertdialog.show();
//                    new Timer().schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            if (sharedPreferenceMain.isVoiceNotificationEnabled()) {
//                                speakText(getString(R.string.violation_occur));
//                            }
//                        }
//                    }, 3000);
//                    }
                }


            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }


}
