package com.amaxzadigital.tollpays.checkin;


import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelLane;

import java.util.ArrayList;

public class AdapterTollLane extends RecyclerView.Adapter<AdapterTollLane.MyViewsHolder> {

    ArrayList<ModelLane> arrayList;
    ModelLane modelLane;
    Context context;
    AdapterTollLane.MyInterface myInterface;
    String myPairingRole;

    public AdapterTollLane(Context context, ArrayList<ModelLane> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    public class MyViewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvLaneNo;

        public MyViewsHolder(View itemView) {
            super(itemView);
            tvLaneNo = (TextView) itemView.findViewById(R.id.tvLaneNo);
            tvLaneNo.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (myInterface != null) myInterface.onLaneClick(getAdapterPosition());
        }
    }

    @Override
    public AdapterTollLane.MyViewsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_lane_no, parent, false);
        return new AdapterTollLane.MyViewsHolder(view);
    }

    public void setClickListener(AdapterTollLane.MyInterface itemClickListener) {
        this.myInterface = itemClickListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final AdapterTollLane.MyViewsHolder holder, final int position) {
        modelLane = arrayList.get(position);
        holder.tvLaneNo.setText(modelLane.getLaneNo());

//        if(modelCheckinNotification.isSelected()){
//            holder.tvLaneNo.setBackground(context.getResources().getDrawable(R.drawable.svg_ic_upward));
//        }else{
//            holder.tvLaneNo.setBackground(context.getResources().getDrawable(R.drawable.ic_arrow_downward));
//        }


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
//        if (position == 0) {
//            params.setMargins(600, 0, 0, 0);
//            holder.tvLaneNo.setLayoutParams(params);
//            holder.tvLaneNo.setVisibility(View.INVISIBLE);
//        } else if (position == arrayList.size() - 1) {
//            params.setMargins(0, 0, 600, 0);
//            holder.tvLaneNo.setLayoutParams(params);
//            holder.tvLaneNo.setVisibility(View.INVISIBLE);
//
//        }
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface MyInterface {
        void onLaneClick(int position);
    }
}