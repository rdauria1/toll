package com.amaxzadigital.tollpays.checkin;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class OtherActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    Context context = this;
    static final String MY_TIME_ZONE = "Asia/Karachi";
    TextToSpeech textToSpeech;
    TextView tvCheckInTollPrice;
    ArrayList<ModelDynamicPricing> pricingArrayList;
    JSONObject dataObject;
    JSONObject jsonObject;
    ModelDynamicPricing modelDynamicPricing;
    SimpleDateFormat dateFormat, dateTimeFormat;
    TimeZone timeZone;
    String currentDate;
    Date dynamicPricingStartTime, dynamicPricingEndTime, currentTime, currentEndTime, nextStartTime;
    Timer timerForNextTimeInterval, timerForCurrentTimeInterval;
    int dynamicPricingCurrentObjectIndex = 0;
    long nextInterval = 0, currentInteval = 0, currentObjectEndTime, nextObjectStartTime;
    boolean intervalTimeSetByIndexing, currentIntervalStopNextInterval, stopNextObjectTime;
    String tollAmount = "$2";   //base price
    String serviceResponse = "{\"status\": \"SUCCESS\", \"ref\": \"dynamic_price_price_chart\", \"data\": [{\"start_time\": \"04:01:00 PM\", \"end_time\": \"04:03:00 PM\", \"scenario_id\": \"33\", \"price\": \"1\"},{\"start_time\": \"04:05:00 PM\", \"end_time\": \"04:07:00 PM\", \"scenario_id\": \"33\", \"price\": \"3\"}, {\"start_time\": \"04:09:00 PM\", \"end_time\": \"04:10:00 PM\", \"scenario_id\": \"36\", \"price\": \"5\"}]}";
    long tempNextInterval = 0;
    long tempCurrentInterval = 0;
    boolean repeatMethod;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        findAllViews();
        timeZone = TimeZone.getTimeZone(MY_TIME_ZONE);
        Date date = new Date();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(timeZone);
        currentDate = dateFormat.format(date);
        currentTime = new Date();
        //----- Dynamic Pricing Working -----//
        tvCheckInTollPrice.setText(tollAmount);

        String dateString = "10:21:00 AM";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        try {
            Date dayEnd = format.parse(currentDate + " " + dateString);
            Date currentTime = new Date();
            long dateConvertToMilliSecond = dayEnd.getTime() - currentTime.getTime();
            Toast.makeText(context, "" + dateConvertToMilliSecond, Toast.LENGTH_SHORT).show();
            timerForNextTimeInterval = new Timer();
            timerForNextTimeInterval.schedule(new TimerTask() {
                public void run() {
                    runOnUiThread(new TimerTask() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Your Day Was Ended", Toast.LENGTH_SHORT).show();
                            Log.e("IM", "Your Day Was Ended");
                        }
                    });
                }
            }, dateConvertToMilliSecond);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        textToSpeech = new TextToSpeech(context, this);


        try {
            jsonObject = new JSONObject(serviceResponse);
            String status = jsonObject.getString("status");
            if (status.equals("SUCCESS")) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                pricingArrayList = new ArrayList<>();
                dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataObject = jsonArray.getJSONObject(i);
                    modelDynamicPricing = new ModelDynamicPricing(dataObject.getString("start_time"), dataObject.getString("end_time"), dataObject.getString("scenario_id"), dataObject.getString("price"), dataObject.getString("reason"));
                    pricingArrayList.add(modelDynamicPricing);
                }

                for (int i = 0; i < pricingArrayList.size(); i++) {
                    modelDynamicPricing = pricingArrayList.get(i);
                    dynamicPricingStartTime = dateTimeFormat.parse(currentDate + " " + modelDynamicPricing.getStartTime());
                    dynamicPricingEndTime = dateTimeFormat.parse(currentDate + " " + modelDynamicPricing.getEndTime());
                    if (dynamicPricingStartTime.before(currentTime) && dynamicPricingEndTime.after(currentTime)) {
                        dynamicPricingCurrentObjectIndex = i;
//                        nextObjectStartTime = pricingArrayList.get(i + 1).getFormatedStartTime().getTime();
                        tvCheckInTollPrice.setText("$" + modelDynamicPricing.getPrice());
                        speakText("Toll price updated to $" + modelDynamicPricing.getPrice());
                        Toast.makeText(context, "Dynamic price set", Toast.LENGTH_SHORT).show();
                        currentInteval = dynamicPricingEndTime.getTime() - currentTime.getTime();
                        Log.e("IM", currentTime.getTime() + " - " + dynamicPricingStartTime.getTime());
                        repeatMethod = true;
//                        scheduleCurrentTimeFinishTimer(currentInteval);
                    }
                }
                currentTime = new Date();
                Log.e("IM", nextObjectStartTime + " - " + currentTime.getTime());
                nextInterval = nextObjectStartTime - currentTime.getTime();
                Date dynamicpricingDate = dateTimeFormat.parse(currentDate + " " + pricingArrayList.get(0).getStartTime());
                if (dynamicPricingCurrentObjectIndex == 0 && dynamicpricingDate.after(currentTime)) {
                    nextObjectStartTime = dateTimeFormat.parse(currentDate + " " + pricingArrayList.get(dynamicPricingCurrentObjectIndex).getStartTime()).getTime();
                    nextInterval = nextObjectStartTime - currentTime.getTime();
                    intervalTimeSetByIndexing = true;
                } else if (dynamicPricingCurrentObjectIndex >= 0 && !intervalTimeSetByIndexing) {
                    nextInterval = currentObjectEndTime - nextObjectStartTime;
                }
                if (nextInterval > 0) {
//                    scheduleNextIntervalTimer(nextInterval);

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //----- Dynamic Pricing Working -----//

    }
//
//    void scheduleNextIntervalTimer(long time) {
//        Log.e("IM", "Next Interval Seconds: " + TimeUnit.MILLISECONDS.toSeconds(time));
//        Log.e("IM", "Next Temp Interval Minutes: " + TimeUnit.MILLISECONDS.toMinutes(tempNextInterval));
//        Log.e("IM", "Next Interval Minutes: " + TimeUnit.MILLISECONDS.toMinutes(time));
//        timerForNextTimeInterval = new Timer();
//        timerForNextTimeInterval.schedule(new TimerTask() {
//            public void run() {
//                /*getActivity().*/
//                runOnUiThread(new TimerTask() {
//                    @Override
//                    public void run() {
//                        if (dynamicPricingCurrentObjectIndex <= pricingArrayList.size() - 1) {
//                            currentEndTime = pricingArrayList.get(dynamicPricingCurrentObjectIndex).getFormatedEndTime();
//                            Date currentTime = new Date();
//                            if (!stopNextObjectTime) {
//                                nextStartTime = pricingArrayList.get(dynamicPricingCurrentObjectIndex + 1).getFormatedStartTime();
//                                nextObjectStartTime = nextStartTime.getTime();
//                            }
//                            currentObjectEndTime = currentEndTime.getTime();
//                            nextInterval = 0;
//                            Log.e("IM", "Current Time: " + currentTime);
//                            nextInterval = currentTime.getTime() - nextObjectStartTime;
//
//                            Log.e("IM", "Next Interval In Minutes" + String.valueOf(TimeUnit.MILLISECONDS.toMinutes(nextInterval)));
//                            Log.e("IM", "Next Interval In Seconds" + String.valueOf(TimeUnit.MILLISECONDS.toSeconds(nextInterval)));
//
//                            Log.e("IM", "currentTime: " + TimeUnit.MILLISECONDS.toMinutes(currentTime.getTime()) + ", nextObjectStartTime: " + TimeUnit.MILLISECONDS.toMinutes(nextObjectStartTime) + ", Temp Next Interval: " + TimeUnit.MILLISECONDS.toMinutes(tempNextInterval));
//
//                            if (nextInterval > 0 && !currentIntervalStopNextInterval) {
//                                tvCheckInTollPrice.setText("$" + pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice());
//
//                                Log.e("IM", "Previous Current Interval" + currentInteval);
//                                currentInteval = 0;
//                                currentInteval = currentTime.getTime() - nextObjectStartTime;
//                                Log.e("IM", "Current Interval In Minutes" + String.valueOf(TimeUnit.MILLISECONDS.toMinutes(currentInteval)));
//                                Log.e("IM", "Current Interval In Seconds" + String.valueOf(TimeUnit.MILLISECONDS.toSeconds(currentInteval)));
//
//                                Log.e("IM", currentTime.getTime() + " - " + nextStartTime.getTime());
//                                if (currentInteval > 0)
//                                    scheduleCurrentTimeFinishTimer(currentInteval);
//                                speakText("Toll price updated to $" + pricingArrayList.get(dynamicPricingCurrentObjectIndex).getPrice());
//                                Toast.makeText(context, "Dynamic price set", Toast.LENGTH_SHORT).show();
//                            }
//
//                            if (nextInterval > 0 && dynamicPricingCurrentObjectIndex < pricingArrayList.size()) {
//                                currentIntervalStopNextInterval = false;
//                                scheduleNextIntervalTimer(nextInterval);
//                            }
//                        } else {
//                            stopNextObjectTime = false;
//                            timerForNextTimeInterval.cancel();
//                            tvCheckInTollPrice.setText(tollAmount);
//                            Toast.makeText(context, "Base price set", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        }, time);
//    }
//
//    void scheduleCurrentTimeFinishTimer(long time) {
//        Log.e("IM", "Current Interval Seconds: " + TimeUnit.MILLISECONDS.toSeconds(time));
//        Log.e("IM", "Current Temp Interval Minutes: " + TimeUnit.MILLISECONDS.toMinutes(tempCurrentInterval));
//        Log.e("IM", "Temp Current Interval: " + TimeUnit.MILLISECONDS.toMinutes(time));
//
//        timerForCurrentTimeInterval = new Timer();
//        timerForCurrentTimeInterval.schedule(new TimerTask() {
//            public void run() {
//                /*getActivity().*/
//                runOnUiThread(new TimerTask() {
//                    @Override
//                    public void run() {
//                        tvCheckInTollPrice.setText(tollAmount);
//                        Toast.makeText(context, "Base price set", Toast.LENGTH_SHORT).show();
//                        if (stopNextObjectTime) {
//                            nextStartTime = pricingArrayList.get(dynamicPricingCurrentObjectIndex + 1).getFormatedStartTime();
//                            nextObjectStartTime = nextStartTime.getTime();
//                        }
//                        dynamicPricingCurrentObjectIndex++;
//                        currentIntervalStopNextInterval = true;
//                        stopNextObjectTime = true;
//                        if (repeatMethod) {
//                            currentTime = new Date();
//                            nextStartTime = pricingArrayList.get(dynamicPricingCurrentObjectIndex + 1).getFormatedStartTime();
//                            nextObjectStartTime = nextStartTime.getTime();
//                            nextInterval = currentTime.getTime() - nextObjectStartTime;
//                            repeatMethod = false;
//                            scheduleNextIntervalTimer(nextInterval);
//                        }
//                    }
//                });
//            }
//        }, time);
//    }

    private void speakText(String message) {
        if (textToSpeech != null) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }


    private void findAllViews() {
        tvCheckInTollPrice = findViewById(R.id.tvCheckInTollPrice);
    }

    @Override
    public void onInit(int i) {
        if (i != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(Locale.UK);
        }
    }
}

