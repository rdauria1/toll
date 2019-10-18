package com.amaxzadigital.tollpays.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.checkin.MainActivity;
import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;

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

public class WelcomeBackActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnWelcomeLogin;
    Context context = this;
    TextView tvWelcomeNotCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_back_new);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        getSupportActionBar().hide();
        tvWelcomeNotCurrentUser = (TextView) findViewById(R.id.tvWelcomeNotCurrentUser);
        btnWelcomeLogin = (Button) findViewById(R.id.btnWelcomeLogin);
        SharedPreferenceMain sharedPreference = SharedPreferenceMain.GetObject(context);
        btnWelcomeLogin.setOnClickListener(this);
//        if (sharedPreference.isTouchIdEnabled()) {
//            btnWelcomeLogin.setText("Login as " + sharedPreference.getTouchIdUserName());
//            tvWelcomeNotCurrentUser.setText("Not " + sharedPreference.getTouchIdUserName() + "?");
//        } else {
            btnWelcomeLogin.setText("Login as " + sharedPreference.getUserName());
            tvWelcomeNotCurrentUser.setText("Not " + sharedPreference.getUserName() + "?");
//        }
        tvWelcomeNotCurrentUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnWelcomeLogin: {
                SharedPreferenceMain sharedPreference = SharedPreferenceMain.GetObject(context);
                sharedPreference.setLogin(true);
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.tvWelcomeNotCurrentUser: {
                new LogoutService().execute();
                break;
            }
        }
    }

    class LogoutService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SharedPreferenceMain sharedPreference = SharedPreferenceMain.GetObject(context);
            postParamenter = new HashMap<String, String>();
            postParamenter.put("loginid", sharedPreference.getUserName());
            postParamenter.put("password", sharedPreference.getUserPassword());
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/logout?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("GET");
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
                    SharedPreferenceMain sharedPreference = SharedPreferenceMain.GetObject(context);
                    sharedPreference.setLogin(false);
                    sharedPreference.setUserName("");
                    sharedPreference.setUserId("");
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
//                else {
//                    String message = jsonObject.getString("message");
//                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
