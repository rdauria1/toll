package com.amaxzadigital.tollpays.checkin;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.amaxzadigital.tollpays.R;

/**
 * Created by Hussain Marvi on 28-Feb-17.
 */

public class EnableLocationFragment extends Fragment {
    Button btnEnableLocation;
    FragmentManager manager;
    FragmentTransaction transaction;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQUEST_CODE_PERMISSION = 2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enable_location, container, false);
        btnEnableLocation = (Button) view.findViewById(R.id.btnEnableLocation);
        btnEnableLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{mPermission}, REQUEST_CODE_PERMISSION);
            }
        });
        return view;
    }
}
