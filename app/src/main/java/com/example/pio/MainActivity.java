package com.example.pio;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import com.example.ptsdblibrary.PointProfileBean;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;



public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback{

    private static final String TAG = "MainActivity";



    private JSONObject jo;

    private double lat;
    private double lng;





    private PointProfileBean pointProfileBean = new PointProfileBean();





    private static final String FINE_LOCATION = permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16.5f;



    //widgets

    private ImageView mGps;
    private ImageView mBuss;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);




        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);



        /*Database Connection Test*/









        mBuss =  findViewById(R.id.ic_bus);

        mBuss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDriverLocation();
            }
        });











        mGps = findViewById(R.id.ic_gps);





        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });





        getLocationPermission();


        hideSoftKeyboard();


    }








   private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


       mapFragment.getMapAsync(MainActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {permission.ACCESS_FINE_LOCATION,
                permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
//        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void getDriverLocation(){






       String link = "http://10.0.2.2/afnan/fetchPointdet.php";

        try {
            URL url = new URL(link);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpClient client = new DefaultHttpClient();
        HttpGet req = new HttpGet();
        try {
            req.setURI(new URI(link));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse res = client.execute(req);
            BufferedReader in = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));

            String out = in.readLine();

            JSONArray ja = new JSONArray(out);

            for(int i=0 ; i < ja.length(); i++){

                jo = ja.getJSONObject(i);
                lat = jo.getDouble("current_lat");
                lng = jo.getDouble("current_lng");

                pointProfileBean.setLng(lng);

                pointProfileBean.setLat(lat);

                //Log.d(TAG, "current_lng  :"+pointProfileBean.getLng()+"\n"+"current_lat :"+pointProfileBean.getLat());

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "getDriverLocation: getting the drivers devices current location Latitude: "+ pointProfileBean.getLat()+"\n Longitude: "+pointProfileBean.getLng());





//       moveCamera(new LatLng(pointProfileBean.getLat(), pointProfileBean.getLng()), DEFAULT_ZOOM, "Driver Location");


        Intent intent = new Intent(this, DistanceActivity.class);
        intent.putExtra("point_profile",pointProfileBean);
        startActivity(intent);

        /*This method will get the location of driver by getting the latitute
         and longitude from the database and then put this in moveCamera method*/

    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    private void moveCamera(LatLng latLng, float defaultZoom, String my_location) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom));

        if (!my_location.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(my_location);
            mMap.addMarker(options);
        }else if (!my_location.equals("Driver Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(my_location);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);


        }

        /*use this code for current direction of bus driver*/


        /*LatLng oldLocation, newLocaation;

        float bearing = (float) beringBetweenLocation(oldLocation, newLocaation);
        rotateMarker(start_marker, bearing);*/


        /*use this code for current direction of bus driver*/

    }

    float[] mGravity;
    float[] mGeomagnetic;

    /*@Override
    public void onSensorChanged(SensorEvent sensorEvent) {


            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = sensorEvent.values;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = sensorEvent.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimut = orientation[0];
                }
            }
            //mCustomDrawableView.invalidate();
            rotateImage(azimut);
    }*/

    private void rotateImage(Float azimut) {
    }

    /*@Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }*/
}