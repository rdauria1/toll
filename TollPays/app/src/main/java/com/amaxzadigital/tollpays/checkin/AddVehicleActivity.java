package com.amaxzadigital.tollpays.checkin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassCarMake;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassCarModel;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassStates;
import com.amaxzadigital.tollpays.checkin.modelclasses.ModelClassVehicleTypes;
import com.amaxzadigital.tollpays.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class AddVehicleActivity extends AppCompatActivity implements View.OnClickListener {
    Context context = this;
    EditText etAddVehiclePlateNo, etAddVehicleYear;
    TextView tvAddVehiclePlateCountry, tvAddVehiclePlateState, tvAddVehiclePlateType, tvAddVehicleMake, tvAddVehicleModel,
            tvAddVehicleType;
    ImageView ivAddVehicleImage;
    ImageView ivAddImage;
    TextView tvAddVehicleTypeAxles;
    ConstraintLayout cLAddVehicleImage;
    TextView tvPlateImage;
    Button btnAddVehicleAdd;
    PopupMenu popup, statesPopup, carMakePopup, carModelPopup, vehiclePopup, vehicleAxles;
    ArrayList<ModelClassStates> states;
    ArrayList<ModelClassCarMake> carMake;
    ArrayList<ModelClassCarModel> carModels;
    ArrayList<ModelClassVehicleTypes> vehicleTypes;
    ArrayList<ModelAxles> axlesList;
    int selectedCarMakeIndex, selectedVehicleTypeIndex, selectedVehicleAxleIndex, selectedModelIndex;
    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_PICTURE = 1;
    String base64Image = "";
    Intent cameraIntent;
    Bitmap bitmap;
    String selectedImagePath;
    String vehicle_axle_id, vehicle_axle_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        ivAddVehicleImage = (ImageView) findViewById(R.id.ivAddVehicleImage);
        cLAddVehicleImage = (ConstraintLayout) findViewById(R.id.cLAddVehicleImage);
        cLAddVehicleImage.setOnClickListener(this);
        ivAddImage = (ImageView) findViewById(R.id.ivAddImage);
        tvPlateImage = (TextView) findViewById(R.id.tvPlateImage);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Add Vehicle");
        etAddVehiclePlateNo = (EditText) findViewById(R.id.etAddVehiclePlateNo);
        etAddVehicleYear = (EditText) findViewById(R.id.etAddVehicleYear);
        tvAddVehiclePlateCountry = (TextView) findViewById(R.id.tvAddVehiclePlateCountry);
        tvAddVehiclePlateCountry.setOnClickListener(this);
        tvAddVehiclePlateState = (TextView) findViewById(R.id.tvAddVehiclePlateState);
        tvAddVehiclePlateState.setOnClickListener(this);
        tvAddVehiclePlateType = (TextView) findViewById(R.id.tvAddVehiclePlateType);
        tvAddVehiclePlateType.setOnClickListener(this);
        tvAddVehicleMake = (TextView) findViewById(R.id.tvAddVehicleMake);
        tvAddVehicleMake.setOnClickListener(this);
        tvAddVehicleModel = (TextView) findViewById(R.id.tvAddVehicleModel);
        tvAddVehicleModel.setOnClickListener(this);
        tvAddVehicleType = (TextView) findViewById(R.id.tvAddVehicleType);
        tvAddVehicleType.setOnClickListener(this);
        tvAddVehicleTypeAxles = findViewById(R.id.tvAddVehicleTypeAxles);
        tvAddVehicleTypeAxles.setOnClickListener(this);
        btnAddVehicleAdd = (Button) findViewById(R.id.btnAddVehicle);
        btnAddVehicleAdd.setOnClickListener(this);
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
            case R.id.tvAddVehiclePlateCountry: {
                popup = new PopupMenu(context, view);
//                popup.setGravity(Gravity.RIGHT);
                popup.getMenu().add("United States");
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        tvAddVehiclePlateCountry.setText(item.getTitle().toString());
                        return false;
                    }
                });
                break;
            }
            case R.id.tvAddVehiclePlateState: {
//                popup.setGravity(Gravity.RIGHT);
                if (statesPopup == null) {
                    if (!tvAddVehiclePlateCountry.getText().toString().equals(""))
                        getStates(view);
                    else
                        Toast.makeText(context, "Please select country", Toast.LENGTH_SHORT).show();
                } else
                    statesPopup.show();

                break;
            }
            case R.id.tvAddVehiclePlateType: {
                popup = new PopupMenu(context, view);
//                popup.setGravity(Gravity.RIGHT);
                popup.getMenu().add("Standard");
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        tvAddVehiclePlateType.setText(item.getTitle().toString());
                        return false;
                    }
                });
                break;
            }
            case R.id.tvAddVehicleMake: {
                if (carMakePopup == null) {
//                    if (!tvAddVehiclePlateState.getText().toString().equals(""))
//                    selectedCarMakeIndex = 2;
                    getCarMakes(view);
//                    else
//                        Toast.makeText(context, "Please select country", Toast.LENGTH_SHORT).show();
                } else
                    carMakePopup.show();
                break;
            }
            case R.id.tvAddVehicleModel: {
                if (!tvAddVehicleMake.getText().equals("")) {
                    carModelPopup = new PopupMenu(context, view);
                    for (int i = 0; i < carMake.get(selectedCarMakeIndex).getModels().size(); i++)
                        carModelPopup.getMenu().add(0, i, 0, carMake.get(selectedCarMakeIndex).getModels().get(i).getModel());
                    carModelPopup.show();
                    carModelPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            tvAddVehicleModel.setText(item.getTitle().toString());
                            selectedModelIndex = item.getItemId();
                            return false;
                        }
                    });
                } else
                    Toast.makeText(context, "Select make first", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.tvAddVehicleType: {
                if (vehiclePopup == null) {
                    getVehicleTypes(view);
                } else
                    vehiclePopup.show();
//                popup.setGravity(Gravity.RIGHT);
                break;
            }
            case R.id.btnAddVehicle: {
                if (!etAddVehiclePlateNo.getText().toString().equals("")
                        && !tvAddVehiclePlateCountry.getText().toString().equals("") && !tvAddVehiclePlateState.getText().toString().equals("") && !tvAddVehiclePlateType.getText().toString().equals("")
                        && !tvAddVehicleMake.getText().toString().equals("")
                        && !tvAddVehicleModel.getText().toString().equals("")
                        && !etAddVehicleYear.getText().toString().equals("")
                        && !tvAddVehicleType.getText().toString().equals("")
                        && !base64Image.equals("") && !tvAddVehicleTypeAxles.getText().toString().equals("")) {
                    if (Integer.parseInt(etAddVehicleYear.getText().toString()) <= Calendar.getInstance().get(Calendar.YEAR))
                    {
                        new AddVehicleService().execute();
                    } else{
                        Toast.makeText(context, "Please enter a valid vehicle year", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.cLAddVehicleImage: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    showImageDialog();
                } else
                    ActivityCompat.requestPermissions(AddVehicleActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                break;
            }

            case R.id.tvAddVehicleTypeAxles: {
                getVehicleTypeAxles(view);
//                popup.setGravity(Gravity.RIGHT);
                break;
            }
        }
    }

    private void getStates(View view) {
        statesPopup = new PopupMenu(context, view);
        new GetStatesService().execute();
    }


    private void getCarMakes(View view) {
        carMakePopup = new PopupMenu(context, view);
        new GetCarMakesService().execute();
    }

    private void getVehicleTypes(View view) {
        vehiclePopup = new PopupMenu(context, view);
        new GetVehicleTypesService().execute();
    }

    private void getVehicleTypeAxles(View view) {
        vehicleAxles = new PopupMenu(context, view);
        if (!tvAddVehicleType.getText().equals("")) {
            new GetVehicleTypeAxles().execute();
        } else {
            Toast.makeText(context, "Please Select Vehicle First", Toast.LENGTH_SHORT).show();
        }

//        if (!tvAddVehicleMake.getText().equals("")) {
//        for (int i = 0; i < vehicleTypes.get(selectedVehicleTypeIndex).getModels().size(); i++)
//            vehicleAxles.getMenu().add(0, i, 0, vehicleTypes.get(selectedVehicleTypeIndex).getModels().get(i).getName());
//        vehicleAxles.show();
//        vehicleAxles.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                tvAddVehicleTypeAxles.setText(item.getTitle().toString());
//                selectedVehicleAxleIndex = item.getItemId();
//                return false;
//            }
//        });
//        } else {
//            Toast.makeText(context, "Select Vehicle Type first", Toast.LENGTH_SHORT).show();
//        }
    }

// Web service for getting states

    class GetStatesService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/getStates?access_token=" + Common.accessToken);
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

        @SuppressLint("NewApi")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            progressDialog.dismiss();
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");

                if (status.equals("SUCCESS")) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    JSONObject jsonobject;
                    states = new ArrayList<>();
                    for (int i = 0; i < dataArray.length(); i++) {
                        jsonobject = dataArray.getJSONObject(i);
                        statesPopup.getMenu().add(0, i, 0, jsonobject.getString("name"));
                        states.add(new ModelClassStates(jsonobject.getString("name"), jsonobject.getString("abbreviation")));
                    }

                    statesPopup.show();
                    statesPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            tvAddVehiclePlateState.setText(item.getTitle().toString());
                            return false;
                        }
                    });
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Web service for getting car manufacturers
    class GetCarMakesService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/getAllCarMakes?access_token=" + Common.accessToken);
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
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    JSONObject jsonobject;
                    JSONArray modelsArray;
                    String make;
                    ModelClassCarMake object;
                    carMake = new ArrayList<>();
                    for (int i = 0; i < dataArray.length(); i++) {
                        jsonobject = dataArray.getJSONObject(i);
                        object = new ModelClassCarMake();
                        make = jsonobject.getString("make");
                        object.setMake(make);
                        carMakePopup.getMenu().add(0, i, 0, make);
                        modelsArray = jsonobject.getJSONArray("models");
                        carModels = new ArrayList<>();
                        for (int j = 0; j < modelsArray.length(); j++) {
                            carModels.add(new ModelClassCarModel(modelsArray.getJSONObject(j).getString("make_id"), modelsArray.getJSONObject(j).getString("id"), modelsArray.getJSONObject(j).getString("model")));
                        }
                        object.setModels(carModels);
                        carMake.add(object);
                    }
                    carMakePopup.show();
                    carMakePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            selectedCarMakeIndex = item.getItemId();
                            tvAddVehicleMake.setText(item.getTitle().toString());
                            tvAddVehicleModel.setText("");
                            return false;

                        }
                    });
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

// Web service for getting vehicle types

    class GetVehicleTypesService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/getAllVehicleTypes?access_token=" + Common.accessToken);
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

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String serviceResponse) {
            super.onPostExecute(serviceResponse);
            Log.e("IM", serviceResponse);
            progressDialog.dismiss();
            try {
                jsonObject = new JSONObject(serviceResponse);
                String status = jsonObject.getString("status");

                if (status.equals("SUCCESS")) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    JSONObject jsonobject;
                    vehicleTypes = new ArrayList<>();
                    for (int i = 0; i < dataArray.length(); i++) {
                        jsonobject = dataArray.getJSONObject(i);
                        vehiclePopup.getMenu().add(0, i, 0, jsonobject.getString("type"));
                        vehicleTypes.add(new ModelClassVehicleTypes(vehicle_axle_id, jsonobject.getString("id"), jsonobject.getString("type")));
                    }
                    vehiclePopup.show();
                    vehiclePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            tvAddVehicleType.setText(item.getTitle().toString());
                            tvAddVehicleTypeAxles.setText("");
                            selectedVehicleTypeIndex = item.getItemId();
                            return false;
                        }
                    });
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finishAffinity();
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class GetVehicleTypeAxles extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;
        ModelAxles modelAxles;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("vehicle_type_id", vehicleTypes.get(selectedVehicleTypeIndex).getTypeId());
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/getAllVehicleTypeAxles?access_token=" + Common.accessToken);
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
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    JSONObject jsonobject;
                    axlesList = new ArrayList<>();
                    for (int i = 0; i < dataArray.length(); i++) {
                        jsonobject = dataArray.getJSONObject(i);
                        vehicle_axle_id = jsonobject.getString("id");
                        vehicle_axle_name = jsonobject.getString("name");
                        modelAxles = new ModelAxles(jsonobject.getString("vehicle_types_id"), vehicle_axle_id, jsonobject.getString("name"));
                        vehicleAxles.getMenu().add(0, i, 0, vehicle_axle_name);
                        axlesList.add(modelAxles);
                    }

                    vehicleAxles.show();
                    vehicleAxles.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            tvAddVehicleTypeAxles.setText(item.getTitle().toString());
                            selectedVehicleAxleIndex = item.getItemId();
                            return false;
                        }
                    });

                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

// Web service for adding new vehicle

    class AddVehicleService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("license_no", etAddVehiclePlateNo.getText().toString());
            postParamenter.put("state", tvAddVehiclePlateState.getText().toString());
            postParamenter.put("license_plate_type", tvAddVehiclePlateType.getText().toString());
            postParamenter.put("vehicle_type_id", vehicleTypes.get(selectedVehicleTypeIndex).getTypeId());
            postParamenter.put("vehicle_types_axles_id", axlesList.get(selectedVehicleAxleIndex).getAxleId());
            Log.e("IM", vehicleTypes.get(selectedVehicleTypeIndex).getType());
            postParamenter.put("year", etAddVehicleYear.getText().toString());
            postParamenter.put("make_id", carMake.get(selectedCarMakeIndex).getModels().get(selectedModelIndex).getMakeId());
            Log.e("IM", "" + carModels.get(selectedModelIndex).getMakeId());
            postParamenter.put("model_id", carMake.get(selectedCarMakeIndex).getModels().get(selectedModelIndex).getModelId());
            Log.e("IM", "" + carModels.get(selectedModelIndex).getModelId());
            postParamenter.put("image1", base64Image);
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/addVehicle?access_token=" + Common.accessToken);
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
                    Toast.makeText(AddVehicleActivity.this, "Vehicle added successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (status.equals("FAILED") && (jsonObject.getString("ref").equals("invalid_token") || jsonObject.getString("ref").equals("expired_token"))) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        finishAffinity();
                    }
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = jsonObject.getString("message");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showImageDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.camera_gallery_dialog, null);
        myAlertDialog.setView(dialogView);
        final AlertDialog alertDialog = myAlertDialog.show();
        alertDialog.setCancelable(false);
        Button btnCamera = dialogView.findViewById(R.id.btnCamera);
        ImageView ivCross = dialogView.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                cameraIntent = new Intent
                        (MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(f));
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else
                    ActivityCompat.requestPermissions(AddVehicleActivity.this, new String[]{Manifest.permission.CAMERA}, 2);

            }

        });
        Button btnGallery = dialogView.findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent pictureActionIntent = null;

                pictureActionIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(
                        pictureActionIntent,
                        GALLERY_PICTURE);

            }

        });
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {

            File f = new File(Environment.getExternalStorageDirectory()
                    .toString());
            for (File temp : f.listFiles()) {
                if (temp.getName().equals("temp.jpg")) {
                    f = temp;
                    break;
                }
            }

            if (!f.exists()) {

                Toast.makeText(getBaseContext(),

                        "Error while capturing image", Toast.LENGTH_LONG)

                        .show();

                return;

            }

            try {
                bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

                bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true);

                int rotate = 0;
                try {
                    ExifInterface exif = new ExifInterface(f.getAbsolutePath());
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);

                int aspectRatio = 500 * bitmap.getHeight() / bitmap.getWidth();
                bitmap = Bitmap.createScaledBitmap(bitmap, 500, aspectRatio, false);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                ivAddVehicleImage.setVisibility(View.GONE);
                tvPlateImage.setVisibility(View.GONE);
                ivAddImage.setImageBitmap(bitmap);
//                img_logo.setImageBitmap(bitmap);
                //storeImageTosdCard(bitmap);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (resultCode == RESULT_OK && requestCode == GALLERY_PICTURE) {
            if (data != null) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = context.getContentResolver().query(selectedImage, filePath,
                        null, null, null);
                if (c == null) { // Source is Dropbox or other similar local file
                    // path
                    selectedImagePath = selectedImage.getPath();
                } else {

                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    selectedImagePath = c.getString(columnIndex);
                    c.close();
                }

                if (selectedImagePath != null) {
//                    txt_image_path.setText(selectedImagePath);
                }
                bitmap = BitmapFactory.decodeFile(selectedImagePath); // load

                // preview image
                int aspectRatio = 500 * bitmap.getHeight() / bitmap.getWidth();
                bitmap = Bitmap.createScaledBitmap(bitmap, 500, aspectRatio, false);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                ivAddVehicleImage.setVisibility(View.GONE);
                tvPlateImage.setVisibility(View.GONE);
                ivAddImage.setImageBitmap(bitmap);
            } else {
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImageDialog();
                }
                break;
            }
            case 2: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(cameraIntent,
                            CAMERA_REQUEST);
                }
                break;
            }
        }
    }
}
