package com.amaxzadigital.tollpays.checkin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.login.LoginActivity;
import com.amaxzadigital.tollpays.login.SharedPreferenceUserDetails;
import com.amaxzadigital.tollpays.paymentoption.PaymentOptionActivity;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

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

public class CurrentBalanceActivity extends AppCompatActivity implements View.OnClickListener {
    Context context = this;
    ImageView ivCurrentBalanceCardImage;
    TextView tvCurrentBalance, tvCurrentBalanceCardNo;
    LinearLayout llCurrentBalanceInsufficient, llCurrentBalanceCreditCard, llCurrentBalanceOneTimePayment, llCurrentBalanceSelectCard;
    CheckBox cbCurrentBalanceCreditCard, cbCurrentBalanceOneTimePayment;
    Button btnCurrentBalanceContinue;
    EditText etCurrentBalanceReplenishmentAmount;
    SharedPreferenceUserDetails sharedPreferenceUserDetails;
    ImageView ivInsufficientBalance;
    TextView tvDoNotEzPass, tvInsufficientBalance;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_balance);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        sharedPreferenceUserDetails = SharedPreferenceUserDetails.GetObject(context);
        getSupportActionBar().setTitle("Wallet");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.svg_ic_cross);
        ivInsufficientBalance = findViewById(R.id.ivInsufficientBalance);
        tvInsufficientBalance = findViewById(R.id.tvInsufficientBalance);
        ivCurrentBalanceCardImage = (ImageView) findViewById(R.id.ivCurrentBalanceCardImage);
        switch (Common.selectedCardType) {
            case "visa": {
                ivCurrentBalanceCardImage.setImageResource(R.drawable.card_visa);
                break;
            }
            case "diners_club": {
                ivCurrentBalanceCardImage.setImageResource(R.drawable.card_dinersclub);
                break;
            }
            case "amex": {
                ivCurrentBalanceCardImage.setImageResource(R.drawable.card_amex);
                break;
            }
            case "jcb": {
                ivCurrentBalanceCardImage.setImageResource(R.drawable.card_jcb);
                break;
            }
            case "mastercard": {
                ivCurrentBalanceCardImage.setImageResource(R.drawable.card_master);
                break;
            }
            case "discover": {
                ivCurrentBalanceCardImage.setImageResource(R.drawable.card_disc);
                break;
            }
            case "maestro": {
                ivCurrentBalanceCardImage.setImageResource(R.drawable.card_maestro);
                break;
            }
        }
        tvCurrentBalance = (TextView) findViewById(R.id.tvCurrentBalance);
        tvCurrentBalanceCardNo = (TextView) findViewById(R.id.tvCurrentBalanceCardNo);
        tvCurrentBalanceCardNo.setText(Common.selectedCardNo);
        etCurrentBalanceReplenishmentAmount = (EditText) findViewById(R.id.etCurrentBalanceReplenishmentAmount);
        etCurrentBalanceReplenishmentAmount.setText(getIntent().getExtras().getString("replenishmentAmount"));
        etCurrentBalanceReplenishmentAmount.setSelection(etCurrentBalanceReplenishmentAmount.getText().length());
        llCurrentBalanceInsufficient = (LinearLayout) findViewById(R.id.llCurrentBalanceInsufficient);
        llCurrentBalanceCreditCard = (LinearLayout) findViewById(R.id.llCurrentBalanceCreditCard);
        cbCurrentBalanceCreditCard = (CheckBox) findViewById(R.id.cbCurrentBalanceCreditCard);
        cbCurrentBalanceOneTimePayment = (CheckBox) findViewById(R.id.cbCurrentBalanceOneTimePayment);

        if (sharedPreferenceUserDetails.getUserType().equals("0")) {
            llCurrentBalanceCreditCard.setVisibility(View.GONE);
            cbCurrentBalanceCreditCard.setChecked(false);
            cbCurrentBalanceOneTimePayment.setChecked(true);
        } else
            llCurrentBalanceCreditCard.setOnClickListener(this);
        llCurrentBalanceOneTimePayment = (LinearLayout) findViewById(R.id.llCurrentBalanceOneTimePayment);
        llCurrentBalanceOneTimePayment.setOnClickListener(this);
        llCurrentBalanceSelectCard = (LinearLayout) findViewById(R.id.llCurrentBalanceSelectCard);
        llCurrentBalanceSelectCard.setOnClickListener(this);

        cbCurrentBalanceCreditCard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    cbCurrentBalanceOneTimePayment.setChecked(false);

            }
        });
        cbCurrentBalanceOneTimePayment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    cbCurrentBalanceCreditCard.setChecked(false);

            }
        });
        cbCurrentBalanceOneTimePayment.setOnClickListener(this);
        btnCurrentBalanceContinue = (Button) findViewById(R.id.btnCurrentBalanceContinue);
        tvDoNotEzPass = (TextView) findViewById(R.id.tvDoNotEzPass);
        btnCurrentBalanceContinue.setOnClickListener(this);

        if (!Common.foundTollAmount.equals("") && Float.parseFloat(Common.foundTollAmount) > Float.parseFloat(getIntent().getExtras().getString("userBalance")))
        {
            tvCurrentBalance.setText("$" + getIntent().getExtras().getString("userBalance"));
            tvCurrentBalance.setTextColor(Color.parseColor("#000000"));
            tvInsufficientBalance.setTextColor(Color.parseColor("#f04e63"));
            tvInsufficientBalance.setText("Insufficient Balance!");
            tvDoNotEzPass.setVisibility(View.VISIBLE);
            ivInsufficientBalance.setBackground(getResources().getDrawable(R.drawable.svg_ic_insufficient_balance));
        } else {
            tvCurrentBalance.setText("$" + getIntent().getExtras().getString("userBalance"));
            tvCurrentBalance.setTextColor(Color.parseColor("#000000"));
            tvInsufficientBalance.setTextColor(Color.parseColor("#21904d"));
            tvInsufficientBalance.setText("Current Balance!");
            tvDoNotEzPass.setVisibility(View.GONE);
            ivInsufficientBalance.setBackground(getResources().getDrawable(R.drawable.svg_ic_sufficient_balance));
        }
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


            case R.id.llCurrentBalanceCreditCard: {
                cbCurrentBalanceCreditCard.setChecked(true);
                cbCurrentBalanceOneTimePayment.setChecked(false);
                break;
            }
            case R.id.llCurrentBalanceOneTimePayment: {
                cbCurrentBalanceOneTimePayment.setChecked(true);
                cbCurrentBalanceCreditCard.setChecked(false);
                break;
            }
            case R.id.btnCurrentBalanceContinue: {
                if (cbCurrentBalanceCreditCard.isChecked())
                    new AddCreditService().execute();
                else if (cbCurrentBalanceOneTimePayment.isChecked()) {
                    Intent intent = new Intent(context, OneTimePaymentActivity.class);
                    intent.putExtra("ReplenishmentAmount", etCurrentBalanceReplenishmentAmount.getText().toString());
                    startActivityForResult(intent, 2);
                }
                finish();
                break;
            }
            case R.id.llCurrentBalanceSelectCard: {
                Intent intent = new Intent(context, PaymentOptionActivity.class);
                startActivityForResult(intent, 1);
                break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                tvCurrentBalanceCardNo.setText(data.getStringExtra("updatedCardNo"));
                ivCurrentBalanceCardImage.setImageBitmap((Bitmap) data.getParcelableExtra("updatedCarThumbnail"));
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK)
                tvCurrentBalance.setText(data.getStringExtra("CurrentAmount"));
            YoYo.with(Techniques.Flash)
                    .duration(700)
                    .playOn(findViewById(R.id.tvCurrentBalance));
        }
    }

    // Web service for adding credit

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
            postParamenter.put("amount", etCurrentBalanceReplenishmentAmount.getText().toString());
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
                URL url = new URL(Common.baseUrl + "api/v1/app/addCredits?access_token=" + Common.accessToken);
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

        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");
                String message = jsonObject.getString("message");
                if (status.equals("SUCCESS")) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    JSONObject object = jsonObject.getJSONObject("data");
                    tvCurrentBalance.setText("$" + object.getString("balance"));
                    YoYo.with(Techniques.Flash)
                            .duration(700)
                            .playOn(findViewById(R.id.tvCurrentBalance));
                    Common.selectedCardNo = object.getJSONObject("selected_card").getString("card");
                    Common.selectedCardType = object.getJSONObject("selected_card").getString("type");
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
