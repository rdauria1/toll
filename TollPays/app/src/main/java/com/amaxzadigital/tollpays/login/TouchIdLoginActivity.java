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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;
import com.amaxzadigital.tollpays.checkin.MainActivity;

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

public class TouchIdLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String KEY_NAME = "toolPays";
    Context context = this;
    Button btnFLogin;
    EditText etFLoginUsername, etFLoginPassword, etForgotPassword;
    TextView tvFLoginForgotPassword;
    KeyguardManager keyguardManager;
    FingerprintManager fingerprintManager;
    CancellationSignal cancellationSignal;
    SharedPreferenceMain sharedPreference;
    AlertDialog adEnableTouchId;
    private Cipher cipher;
    private KeyStore keyStore;
    String forgotPasswordUsername = "";
    AlertDialog alertDialog;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_id_login);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        tvFLoginForgotPassword = (TextView) findViewById(R.id.tvFLoginForgotPassword);
        tvFLoginForgotPassword.setOnClickListener(this);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        getSupportActionBar().hide();
        sharedPreference = SharedPreferenceMain.GetObject(context);
        etFLoginUsername = (EditText) findViewById(R.id.etFLoginUsername);
        etFLoginPassword = (EditText) findViewById(R.id.etFLoginPassword);
        btnFLogin = (Button) findViewById(R.id.btnFLogin);
        btnFLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch ((view.getId())) {
            case R.id.btnFLogin: {
                if (etFLoginPassword.getText().length() > 5) {
                    loginProcess(etFLoginUsername.getText().toString(), etFLoginPassword.getText().toString());
                } else
                    Toast.makeText(context, "Please input at least 6 digits password", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.tvFLoginForgotPassword: {

                alertDialog = new AlertDialog.Builder(context).setTitle("Forgot Password")
                        .setView(R.layout.dialog_forgotpassword)
                        .setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                etForgotPassword = (EditText) alertDialog.findViewById(R.id.etForgotPassword);
                                forgotPasswordUsername = etForgotPassword.getText().toString();
                                new ForgotPasswordService().execute();
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
                        if (!sharedPreference.isTouchIdEnabled()) {
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
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                postParamenter = new HashMap<String, String>();
                postParamenter.put("loginid", userName);
                postParamenter.put("password", password);
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... strings) {
// Send data
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
                progressDialog.dismiss();
                try {
                    jsonObject = new JSONObject(serviceResponse);
                    String status = jsonObject.getString("status");

                    if (status.equals("SUCCESS")) {
                        Common.accessToken = jsonObject.getString("access_token");
                        sharedPreference.setAccessToken(jsonObject.getString("access_token"));
                        new AlertDialog.Builder(context).setTitle("Enable Touch ID")
                                .setCancelable(false)
                                .setMessage("Please note that any fingerprint registered with Touch ID or this device will have an access to Toll Pays account through Touch ID")
                                .setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.M)
                                    public void onClick(DialogInterface dialog, int which) {
                                        recieveFingerPrint();
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        progressDialog.dismiss();
                        String message = jsonObject.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }.execute();
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
                SharedPreferenceMain sharedPreference = SharedPreferenceMain.GetObject(context);
                sharedPreference.setLogin(true);
                sharedPreference.setTouchId(true);
                sharedPreference.setTouchIdUserName(etFLoginUsername.getText().toString());
                sharedPreference.setTouchIdUserPassword(etFLoginPassword.getText().toString());
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finishAffinity();
//                loginProcess(sharedPreferenceMain.getTouchIdUserName(), sharedPreferenceMain.getTouchIdUserPassword());
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
}
