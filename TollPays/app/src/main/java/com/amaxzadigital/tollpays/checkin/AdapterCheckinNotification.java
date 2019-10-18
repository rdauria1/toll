package com.amaxzadigital.tollpays.checkin;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelCheckinNotification;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AdapterCheckinNotification extends RecyclerView.Adapter<AdapterCheckinNotification.MyViewsHolder> {

    ArrayList<ModelCheckinNotification> arrayList;
    ModelCheckinNotification modelCheckinNotification;
    Context context;
    AdapterCheckinNotification.MyInterface myInterface;
    String myPairingRole;
    SharedPreferenceMain sharedPreferenceMain;
    SimpleDateFormat simpleDateFormat;
    CountDownTimer countDownTimer;

    public AdapterCheckinNotification(Context context, ArrayList<ModelCheckinNotification> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        sharedPreferenceMain = SharedPreferenceMain.GetObject(context);
        simpleDateFormat = new SimpleDateFormat("mm:00");
    }

    public class MyViewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivBell;
        TextView tvNotification, tvTimer;
        LinearLayout llNotification;

        public MyViewsHolder(View itemView) {
            super(itemView);
            ivBell = itemView.findViewById(R.id.ivBell);
            ivBell.setOnClickListener(this);
            tvNotification = itemView.findViewById(R.id.tvNotification);
            tvTimer = itemView.findViewById(R.id.tvTimer);
            llNotification = itemView.findViewById(R.id.llNotification);
        }

        @Override
        public void onClick(View view) {
            if (myInterface != null) myInterface.onBellClick(getAdapterPosition());
        }
    }

    @Override
    public AdapterCheckinNotification.MyViewsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_checkin_notification, parent, false);
        return new AdapterCheckinNotification.MyViewsHolder(view);
    }

    public void setClickListener(AdapterCheckinNotification.MyInterface itemClickListener) {
        this.myInterface = itemClickListener;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final AdapterCheckinNotification.MyViewsHolder holder, final int position) {
        modelCheckinNotification = arrayList.get(position);
        holder.tvNotification.setText(modelCheckinNotification.getText());
        holder.llNotification.setSelected(true);

        if (sharedPreferenceMain.isTollNotificationSoundEnabled()) {
            holder.ivBell.setImageDrawable(context.getResources().getDrawable(R.drawable.svg_ic_checkin_bell));
        } else {
            holder.ivBell.setImageDrawable(context.getResources().getDrawable(R.drawable.svg_ic_checkin_bellcancel));
        }


//        holder.ivBell.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (sharedPreferenceMain.isTollNotificationSoundEnabled()) {
//                    holder.ivBell.setImageDrawable(context.getResources().getDrawable(R.drawable.svg_ic_checkin_bellcancel));
//                    sharedPreferenceMain.setTollNotificationSound(false);
//                } else {
//                    holder.ivBell.setImageDrawable(context.getResources().getDrawable(R.drawable.svg_ic_checkin_bell));
//                    sharedPreferenceMain.setTollNotificationSound(true);
//                }
//            }
//        });

        if (modelCheckinNotification.getType().equals("NearGatedToll")) {
            holder.tvTimer.setVisibility(View.GONE);
            holder.tvNotification.setTextColor(context.getResources().getColor(R.color.checkin_notification_1));
            holder.llNotification.getBackground().setTint(Color.parseColor("#fde8e6"));
            holder.ivBell.setColorFilter(ContextCompat.getColor(context, R.color.checkin_notification_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (modelCheckinNotification.getType().equals("DynamicPricing")) {
            holder.tvTimer.setVisibility(View.VISIBLE);
            holder.tvTimer.setText(simpleDateFormat.format(new Date(modelCheckinNotification.getTimer())));
            holder.tvNotification.setTextColor(context.getResources().getColor(R.color.checkin_notification_2));
            holder.llNotification.getBackground().setTint(Color.parseColor("#e1f4fd"));
            holder.ivBell.setColorFilter(ContextCompat.getColor(context, R.color.checkin_notification_2), android.graphics.PorterDuff.Mode.SRC_IN);




//            if (countDownTimer == null) {
                countDownTimer = new CountDownTimer(modelCheckinNotification.getTimer(), 60000) {

                    public void onTick(long millisUntilFinished) {
                        holder.tvTimer.setText(simpleDateFormat.format(new Date(millisUntilFinished)));
                        //here you can have your logic to set text to edittext
                    }

                    public void onFinish() {
//                holder.tvTimer.setText("done!");
                    }

                }.start();
//            }
        }


//        YoYo.with(Techniques.Flash)
//                .repeat(YoYo.INFINITE)
//                .duration(3000)
//                .playOn(holder.tvNotification);
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface MyInterface {
        void onBellClick(int position);
    }
}