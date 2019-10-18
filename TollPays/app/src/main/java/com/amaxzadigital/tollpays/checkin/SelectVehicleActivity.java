package com.amaxzadigital.tollpays.checkin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.amaxzadigital.tollpays.Common;
import com.amaxzadigital.tollpays.R;
import com.amaxzadigital.tollpays.SharedPreferenceMain;
import com.amaxzadigital.tollpays.login.LoginActivity;
import com.amaxzadigital.tollpays.login.SharedPreferenceUserDetails;
import com.amaxzadigital.tollpays.users.AdapterVehicle;
import com.amaxzadigital.tollpays.users.ModelClassVehicle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class SelectVehicleActivity extends AppCompatActivity implements AdapterVehicle.MyInterface {
    Context context = this;
    ListView lvManageVehicle;
    ModelClassVehicle object;
    AdapterVehicle adapter;
    ArrayList<ModelClassVehicle> arrayList, current_list;
    int selectedItem;
    AlertDialog dialogDeleteVehicle;
    SharedPreferenceUserDetails sharedPreferenceUserDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_vehicle);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        sharedPreferenceUserDetails = SharedPreferenceUserDetails.GetObject(context);
//        svToolbar = findViewById(R.id.svToolbar);
//        setSupportActionBar(svToolbar);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Select Vehicle");
//        bar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//        bar.setTitle(Html.fromHtml("<font color='#000000'>Select Vehicle</font>"));
        bar.setHomeButtonEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.svg_ic_cross);

        lvManageVehicle = (ListView) findViewById(R.id.lvManageVehicle);
        lvManageVehicle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItem = i;
                if (getIntent().getExtras().getString("CommingFrom").equals("MapFragment") || getIntent().getExtras().getString("CommingFrom").equals("MapFragmentNew")) {
                    new SetDefaultVehicles().execute();
                } else if (getIntent().getExtras().getString("CommingFrom").equals("AddUserActivity")) {
                    Intent intent = new Intent();
                    intent.putExtra("vehicleId", current_list.get(selectedItem).getId());
                    intent.putExtra("vehicleLiscenceNo", current_list.get(selectedItem).getPlateNo());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetVehiclesService().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_addvehicle:
                Intent intent = new Intent(context, AddVehicleActivity.class);
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_vehicle, menu);
        if (sharedPreferenceUserDetails.getUserType().equals("1"))
            menu.findItem(R.id.action_addvehicle).setVisible(true);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemsVisibility(menu, searchItem, false);
            }
        });

        // Detect SearchView close
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setItemsVisibility(menu, searchItem, true);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                // TODO Auto-generated method stub

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // TODO Auto-generated method stub


                if (query.length() > 0) {

                    // Update your listview

                    ArrayList<ModelClassVehicle> filteredList = new ArrayList<>();
//
                    for (ModelClassVehicle object : arrayList) {
                        if (object.getPlateNo().toLowerCase(Locale.getDefault()).contains(query) || object.getPlateNo().contains(query)) {
                            filteredList.add(object);
                        }
                    }
                    current_list = filteredList;
                } else {
                    current_list = arrayList;
                }
                adapter.setData(current_list);
                adapter.notifyDataSetChanged();
                lvManageVehicle.setAdapter(adapter);
                return true;
            }
        });
        return true;
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                new GetVehiclesService().execute();
            }
        }
    }

    @Override
    public void onDeleteClick(final int position) {
        dialogDeleteVehicle = new AlertDialog.Builder(SelectVehicleActivity.this).create();
        dialogDeleteVehicle.setMessage("You Want to Delete This Vehicle");
        dialogDeleteVehicle.setCancelable(true);

        dialogDeleteVehicle.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteVehicle().execute(String.valueOf(position));
                    }
                });
        dialogDeleteVehicle.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogDeleteVehicle.dismiss();
                    }
                });
        dialogDeleteVehicle.show();
    }

    @Override
    public void onEditClick(int position) {
        Intent intent = new Intent(SelectVehicleActivity.this, EditVehicleActivity.class);
        intent.putExtra("VehicleId", current_list.get(position).getId());
        intent.putExtra("VehicleTypeAxleId", current_list.get(position).getVehicleTypeAxleId());
        startActivity(intent);
    }

    class GetVehiclesService extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("user_id", sharedPreferenceUserDetails.getUserId());
            postParamenter.put("shared_rented_vehicle_show", "1");
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/allVechicles?access_token=" + Common.accessToken);
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
                    arrayList = new ArrayList<>();
                    current_list = new ArrayList<>();
                    JSONObject jsonobject;
                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        jsonobject = dataArray.getJSONObject(i);
                        object = new ModelClassVehicle(jsonobject.getString("id"), jsonobject.getString("license_no"), jsonobject.getString("vehicle_type"), jsonobject.getString("year"), jsonobject.getString("model_name"), jsonobject.getString("make_name"), jsonobject.getString("state"), jsonobject.getString("image1_url"), jsonobject.getString("default"), jsonobject.getString("name"), jsonobject.getString("vehicle_types_axles_id"));
                        object.setIsRented(jsonobject.getString("is_rented"));
                        object.setIsReturn(jsonobject.getString("is_return"));
                        arrayList.add(object);
                    }
                    adapter = new AdapterVehicle(context, arrayList, "SelectVehicleActivity");
                    adapter.setEditClickListener(SelectVehicleActivity.this);
                    adapter.setDeleteClickListener(SelectVehicleActivity.this);
                    lvManageVehicle.setAdapter(adapter);
                    current_list = arrayList;
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

    class DeleteVehicle extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;
        int vehiclePosition;

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
                vehiclePosition = Integer.parseInt(strings[0]);
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/deleteVehicle?access_token=" + Common.accessToken);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                postParamenter = new HashMap<String, String>();
                postParamenter.put("vehicle_id", current_list.get(vehiclePosition).getId());
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
                    arrayList.remove(vehiclePosition);
                    adapter.notifyDataSetChanged();

                    if (arrayList.size() == 0) {
                        new LogoutService().execute();
                    }
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

    class SetDefaultVehicles extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        String serviceResponse = "";
        HashMap<String, String> postParamenter;
        JSONObject jsonObject;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            postParamenter = new HashMap<String, String>();
            postParamenter.put("vehicle_id", current_list.get(selectedItem).getId());
            postParamenter.put("vehicle_types_axles_id ", current_list.get(selectedItem).getVehicleTypeAxleId());
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
// Send data
            try {
                /* forming th java.net.URL object */
                URL url = new URL(Common.baseUrl + "api/v1/app/defaultVehicle?access_token=" + Common.accessToken);
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
                    Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    sharedPreferenceUserDetails.setDefaultVehicleId(jsonObject.getString("vehicle_id"));
                    sharedPreferenceUserDetails.setDefaultVehicleType(jsonObject.getString("vehicle_type"));
                    sharedPreferenceUserDetails.setDefaultVehicleNo(jsonObject.getString("license_no"));
                    Intent intent = new Intent();
                    intent.putExtra("vehicleLiscenceNo", jsonObject.getString("license_no"));
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
