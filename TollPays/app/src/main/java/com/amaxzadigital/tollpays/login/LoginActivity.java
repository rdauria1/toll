package com.amaxzadigital.tollpays.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
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
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;
import com.amaxzadigital.tollpays.checkin.MainActivity;
import com.google.firebase.iid.FirebaseInstanceId;

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
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String KEY_NAME = "toolPays";
    Context context = this;
    LinearLayout llLoginSignUp, llLoginTouchId;
    Button btnLogin;
    TextView tvLoginForgotPassword;
    EditText etLoginUsername, etLoginPassword, etForgotPassword;
    KeyguardManager keyguardManager;
    FingerprintManager fingerprintManager;
    CancellationSignal cancellationSignal;
    SharedPreferenceMain sharedPreferenceMain;
    SharedPreferenceUserDetails sharedPreferenceUserDetails;
    AlertDialog adEnableTouchId;
    ProgressDialog progressDialog;
    String forgotPasswordUsername = "";
    AlertDialog alertDialog;
    private Cipher cipher;
    private KeyStore keyStore;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_login_new);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        getSupportActionBar().hide();

        // Initializing both Android Keyguard Manager and Fingerprint Manager
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (fingerprintManager != null && fingerprintManager.isHardwareDetected())
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }

        sharedPreferenceMain = SharedPreferenceMain.GetObject(context);
        sharedPreferenceMain.setLogin(false);
        findAllViews();

        if (!sharedPreferenceMain.getPairingRole().equals("")) {
            sharedPreferenceMain.setPairingRole("");
            sharedPreferenceMain.setGroupId("");
            sharedPreferenceMain.setPairingRole("");
            sharedPreferenceMain.setPairingEnabled(false);
            Common.pairingId = "";
            Common.groupId = "";
            Common.pairingRole = "";
        }
    }

    private void findAllViews() {
        etLoginUsername = (EditText) findViewById(R.id.etLoginUsername);
        etLoginPassword = (EditText) findViewById(R.id.etLoginPassword);
        tvLoginForgotPassword = (TextView) findViewById(R.id.tvLoginForgotPassword);
        tvLoginForgotPassword.setOnClickListener(this);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        llLoginSignUp = (LinearLayout) findViewById(R.id.llLoginSignUp);
        llLoginSignUp.setOnClickListener(this);
        llLoginTouchId = (LinearLayout) findViewById(R.id.llLoginTouchId);
        llLoginTouchId.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llLoginSignUp: {
                Intent intent = new Intent(context, SignupActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.llLoginTouchId: {
                if (!sharedPreferenceMain.isTouchIdEnabled()) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    if (fingerprintManager.isHardwareDetected()) {
                        /**
                         * An error message will be displayed if the device does not contain the fingerprint hardware.
                         * However if you plan to implement a default authentication method,
                         * you can redirect the user to a default authentication activity from here.
                         * Example:
                         * Intent intent = new Intent(this, DefaultAuthenticationActivity.class);
                         * startActivity(intent);
                         */
                        Intent intent = new Intent(context, TouchIdLoginActivity.class);
                        startActivityForResult(intent, 1);
                    } else {
//                        Toast.makeText(context, "Your Device does not have a Fingerprint Sensor", Toast.LENGTH_SHORT).show();

                        new AlertDialog.Builder(context).setTitle("Touch ID")
                                .setMessage("Sorry! Your phone does not support touch ID.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_touchid, null);
                    dialogBuilder.setView(dialogView);
                    final AlertDialog alertDialog = dialogBuilder.show();
                    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                    recieveFingerPrint();
                }
                break;
            }
            case R.id.btnLogin: {
                if (!etLoginUsername.getText().toString().equals("")) {
                    if (etLoginPassword.getText().length() > 5) {
                        loginProcess(etLoginUsername.getText().toString(), etLoginPassword.getText().toString());
//                    new LoginService().execute();
                    } else {
                        Toast.makeText(context, "Please input at least 6 digits password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Please Enter the Username or Account Number", Toast.LENGTH_SHORT).show();
                }

                break;
            }
            case R.id.tvLoginForgotPassword: {
                alertDialog = new AlertDialog.Builder(context).setTitle("Forgot Password")
                        .setView(R.layout.dialog_forgotpassword)
                        .setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                etForgotPassword = (EditText) alertDialog.findViewById(R.id.etForgotPassword);
                                forgotPasswordUsername = etForgotPassword.getText().toString();
                                if (!etForgotPassword.equals("")) {
                                    new ForgotPasswordService().execute();
                                } else {
                                    Toast.makeText(context, "Please enter the Username or Account Number. ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            }
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

                        if (!sharedPreferenceMain.isTouchIdEnabled()) {
                            adEnableTouchId = new AlertDialog.Builder(context).setTitle("Touch ID")
                                    .setCancelable(false)
                                    .setView(R.layout.dialog_touchid)

                                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            cancellationSignal.cancel();
                                        }
                                    })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            cancellationSignal.cancel();
                                        }
                                    })
                                    .show();
                        }

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onPause() {
        super.onPause();
        if (cancellationSignal != null)
            cancellationSignal.cancel();
    }

    @SuppressLint("StaticFieldLeak")
    private void loginProcess(final String userName, final String password) {
        new AsyncTask<String, String, String>() {
            HttpURLConnection urlConnection;
            String serviceResponse = "";
            HashMap<String, String> postParamenter;
            JSONObject jsonObject;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                postParamenter = new HashMap<String, String>();
                postParamenter.put("loginid", userName);
                postParamenter.put("password", password);
                postParamenter.put("device_type", "android");
                postParamenter.put("device_id", FirebaseInstanceId.getInstance().getToken());
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    /* forming th java.net.URL object */
                    URL url = new URL(Common.baseUrl + "api/v1/app/login");
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
                        Common.accessToken = jsonObject.getString("access_token");
                        sharedPreferenceMain.setAccessToken(Common.accessToken);
                        sharedPreferenceMain.setUserName(userName);
                        sharedPreferenceMain.setUserPassword(password);
                        new GetDetails().execute();
                    } else if (status.equals("FAILED")) {
                        progressDialog.dismiss();
                        if ((jsonObject.getString("ref").equals("account_deactive"))) {
                            AlertDialog.Builder alertdialog = new AlertDialog.Builder(context);
                            alertdialog.setTitle("DeActivate Login");
                            alertdialog.setMessage(jsonObject.getString("message"));
                            alertdialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                }
                            });
                            alertdialog.show();

                        }
                        String message = jsonObject.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.execute();
    }

    class GetDetails extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... strings) {
            // Send data
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

        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            progressDialog.dismiss();
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");

                if (status.equals("SUCCESS")) {
                    sharedPreferenceMain.setLogin(true);
                    sharedPreferenceUserDetails = SharedPreferenceUserDetails.GetObject(context);
                    JSONObject object = jsonObject.getJSONObject("data");
                    sharedPreferenceUserDetails.setUserId(object.getString("id"));
                    sharedPreferenceUserDetails.setUserName(object.getString("username"));
                    sharedPreferenceUserDetails.setUserFirstName(object.getString("first_name"));
                    sharedPreferenceUserDetails.setUserLastName(object.getString("last_name"));
                    sharedPreferenceUserDetails.setUserEmail(object.getString("email"));
                    sharedPreferenceUserDetails.setUserAccountNo(object.getString("account_no"));
                    sharedPreferenceUserDetails.setUserPhoneNo(object.getString("phone"));
                    sharedPreferenceUserDetails.setDefaultVehicleId(object.getString("vehicle_id"));
                    sharedPreferenceUserDetails.setDefaultVehicleType(object.getString("vehicle_type"));
                    sharedPreferenceUserDetails.setDefaultVehicleNo(object.getString("license_no"));
                    sharedPreferenceUserDetails.setUserType(object.getString("main_account_user"));
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();

            }

        }
    }

    class ForgotPasswordService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("loginid", forgotPasswordUsername);
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/forget");
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
            progressDialog.dismiss();
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                String message = jsonObject.getString("message");
                if (status.equals("SUCCESS")) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

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
                loginProcess(sharedPreferenceMain.getTouchIdUserName(), sharedPreferenceMain.getTouchIdUserPassword());
            }
        }
    }
}
