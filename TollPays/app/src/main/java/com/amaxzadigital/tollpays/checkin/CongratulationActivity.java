package com.amaxzadigital.tollpays.checkin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amaxzadigital.tollpays.R;

public class CongratulationActivity extends AppCompatActivity {
    Button btnCongratulationOk;
    TextView tvCongratulationsAgency, tvCongratulationsAmount, tvCongratulationsVehicleNo, tvCongratulationsVehicleType,
            tvCongratulationsYear, tvCongratulationsModel, tvCongratulationsMake, tvCongratulationsState, tvCongratulationsEntryTime,
            tvCongratulationsEntryPlaza, tvCongratulationsEntryLane, tvCongratulationsExitTime, tvCongratulationsExitPlaza, tvCongratulationsExitLane, tvCongratulationTimer;
    ImageView ivEntryExitMargin;
    RelativeLayout upperLayout, lowerLayout;
    int time = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulation);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        upperLayout = (RelativeLayout) findViewById(R.id.upperLayout);
        lowerLayout = (RelativeLayout) findViewById(R.id.lowerLayout);
        ivEntryExitMargin = (ImageView) findViewById(R.id.ivEntryExitMargin);
        getSupportActionBar().hide();
        btnCongratulationOk = (Button) findViewById(R.id.btnCongratulationOk);
        btnCongratulationOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        tvCongratulationsAgency = (TextView) findViewById(R.id.tvCongratulationsAgency);
        tvCongratulationsAgency.setText(getIntent().getExtras().getString("AgencyName"));
        tvCongratulationsAmount = (TextView) findViewById(R.id.tvCongratulationsAmount);
        tvCongratulationsAmount.setText("$" + getIntent().getExtras().getString("TollAmount"));
        tvCongratulationsVehicleNo = (TextView) findViewById(R.id.tvCongratulationsVehicleNo);
        tvCongratulationsVehicleNo.setText(getIntent().getExtras().getString("LiscenceNo"));
        tvCongratulationsVehicleType = (TextView) findViewById(R.id.tvCongratulationsVehicleType);
        tvCongratulationsVehicleType.setText(getIntent().getExtras().getString("VehicleType"));
        tvCongratulationsYear = (TextView) findViewById(R.id.tvCongratulationsYear);
        tvCongratulationsYear.setText(getIntent().getExtras().getString("VehicleYear"));
        tvCongratulationsModel = (TextView) findViewById(R.id.tvCongratulationsModel);
        tvCongratulationsModel.setText(getIntent().getExtras().getString("VehicleModel"));
        tvCongratulationsMake = (TextView) findViewById(R.id.tvCongratulationsMake);
        tvCongratulationsMake.setText(getIntent().getExtras().getString("VehicleMake"));
        tvCongratulationsState = (TextView) findViewById(R.id.tvCongratulationsState);
        tvCongratulationsState.setText(getIntent().getExtras().getString("VehicleState"));

        tvCongratulationsEntryTime = (TextView) findViewById(R.id.tvCongratulationsEntryTime);
        tvCongratulationsEntryPlaza = (TextView) findViewById(R.id.tvCongratulationsEntryPlaza);
        tvCongratulationsEntryLane = (TextView) findViewById(R.id.tvCongratulationsEntryLane);
        tvCongratulationsExitTime = (TextView) findViewById(R.id.tvCongratulationsExitTime);
        tvCongratulationsExitPlaza = (TextView) findViewById(R.id.tvCongratulationsExitPlaza);
        tvCongratulationsExitLane = (TextView) findViewById(R.id.tvCongratulationsExitLane);
        tvCongratulationTimer = (TextView) findViewById(R.id.tvCongratulationTimer);
        switch (getIntent().getExtras().getString("PlazaType")) {
            case "entry": {
                tvCongratulationsEntryTime.setText(getIntent().getExtras().getString("EntryTime"));
                tvCongratulationsEntryPlaza.setText(getIntent().getExtras().getString("EntryPlaza"));
                tvCongratulationsEntryLane.setText(getIntent().getExtras().getString("EntryLane"));
                lowerLayout.setVisibility(View.GONE);
                ivEntryExitMargin.setImageResource(R.drawable.small_circle);
                break;
            }
            case "exit": {
                upperLayout.setVisibility(View.GONE);
                ivEntryExitMargin.setImageResource(R.drawable.small_circle);
                tvCongratulationsExitTime.setText(getIntent().getExtras().getString("ExitTime"));
                tvCongratulationsExitPlaza.setText(getIntent().getExtras().getString("ExitPlaza"));
                tvCongratulationsExitLane.setText(getIntent().getExtras().getString("ExitLane"));
                break;
            }
            case "both": {
                tvCongratulationsEntryTime.setText(getIntent().getExtras().getString("EntryTime"));
                tvCongratulationsEntryPlaza.setText(getIntent().getExtras().getString("EntryPlaza"));
                tvCongratulationsEntryLane.setText(getIntent().getExtras().getString("EntryLane"));
                tvCongratulationsExitTime.setText(getIntent().getExtras().getString("ExitTime"));
                tvCongratulationsExitPlaza.setText(getIntent().getExtras().getString("ExitPlaza"));
                tvCongratulationsExitLane.setText(getIntent().getExtras().getString("ExitLane"));
                break;
            }
        }
        new CountDownTimer(6000, 1000) {

            public void onTick(long millisUntilFinished) {
                tvCongratulationTimer.setText("Screen will be close in" + checkDigit(time) + " sec");
                time--;
            }

            public void onFinish() {
                finish();
            }

        }.start();
    }

    public String checkDigit(int number) {
        return number <= 9 ? "" + number : String.valueOf(number);
    }
}
