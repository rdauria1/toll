package com.amaxzadigital.tollpays.checkin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.login.LoginActivity;

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

public class OneTimePaymentActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    Button btnOneTimePaymentSave;
    TextView tvOneTimePaymentReplenishmentAmount;
    TextView tvOneTimePaymentExpiryDate;
    Context context = this;
    DatePickerDialog datePickerDialog;
    EditText etOneTimePaymentCardNo, etOneTimePaymentCvv, etOneTimePaymentCountry, etOneTimePaymentZip;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_time_payment);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        getSupportActionBar().setTitle("One Time Payment");
        getSupportActionBar().setHomeButtonEnabled(true);
        tvOneTimePaymentReplenishmentAmount = (TextView) findViewById(R.id.tvOneTimePaymentReplenishmentAmount);
        tvOneTimePaymentReplenishmentAmount.setText("$" + getIntent().getExtras().getString("ReplenishmentAmount"));
        btnOneTimePaymentSave = (Button) findViewById(R.id.btnOneTimePaymentSave);
        btnOneTimePaymentSave.setOnClickListener(this);
        etOneTimePaymentCardNo = (EditText) findViewById(R.id.etOneTimePaymentCardNo);
        etOneTimePaymentCvv = (EditText) findViewById(R.id.etOneTimePaymentCvv);
        etOneTimePaymentCountry = (EditText) findViewById(R.id.etOneTimePaymentCountry);
        etOneTimePaymentZip = (EditText) findViewById(R.id.etOneTimePaymentZip);
        tvOneTimePaymentExpiryDate = (TextView) findViewById(R.id.tvOneTimePaymentExpiryDate);
        tvOneTimePaymentExpiryDate.setOnClickListener(this);
        datePickerDialog = new DatePickerDialog(
                context, this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOneTimePaymentSave: {
                new AddCreditService().execute();
                break;
            }
            case R.id.tvOneTimePaymentExpiryDate: {
                datePickerDialog.show();
                break;
            }
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        tvOneTimePaymentExpiryDate.setText(i + "/" + (i1 + 1) + "/" + i2);
    }

    class AddCreditService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("cardno", etOneTimePaymentCardNo.getText().toString());
            postParamenter.put("expmonth", String.valueOf(datePickerDialog.getDatePicker().getMonth() + 1));
            postParamenter.put("expyear", String.valueOf(datePickerDialog.getDatePicker().getYear()));
            postParamenter.put("cvv", etOneTimePaymentCvv.getText().toString());
            postParamenter.put("zipcode", etOneTimePaymentZip.getText().toString());
            postParamenter.put("amount", tvOneTimePaymentReplenishmentAmount.getText().toString().substring(1));
            postParamenter.put("default", "1");
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
                URL url = new URL(Common.baseUrl + "api/v1/app/singlePayment?access_token=" + Common.accessToken);
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
                    JSONObject object = jsonObject.getJSONObject("data");
                    Intent intent = new Intent();
                    intent.putExtra("CurrentAmount", "$" + object.getString("balance"));
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
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
