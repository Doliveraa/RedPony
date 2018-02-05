package edu.csulb.phylo;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, UserLocationClient.InitialLocationReceived {

    //Variables
    private boolean hasLocationPermission;
    private UserLocationClient userLocationClient;
    //Constants
    private final String TAG = "MapsActivity";
    private final int PERMISSION_REQUEST_CODE = 2035;
    //Fragments
    private GoogleMap mMap;
    //Views
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize variables
        userLocationClient = new UserLocationClient(this);

        //Initialize Views
        progressBar = findViewById(R.id.map_progress_bar);

        //Initialize Listeners
        userLocationClient.setInitialLocationReceiveListener(this);

        //Begin the progress bar
        progressBar.setVisibility(View.VISIBLE);

        //Check if we currently have the user's permission to access their location
        hasLocationPermission = UserPermission.checkUserPermission(this, UserPermission.Permission.LOCATION_PERMISSION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //If we have the user's permission to receive their location, start the location tracking
        if(hasLocationPermission) {
            userLocationClient.startUserLocationTracking();
        } else {
            //We do not have permission to receive the user's location, ask for permission
            requestPermission();
        }
    }

    //Asks for the user's permission, double check just in case, don't want to ask the user a second time
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestPermission : Requesting Fine Location permission");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    //Permission result received, act accordingly
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission has been granted, we can now start tracking the user's location
                    Log.d(TAG, "onRequestPermissionResult : User has accepted location permissions");
                    hasLocationPermission = true;
                    userLocationClient.startUserLocationTracking();
                } else {
                    Log.d(TAG, "onRequestPermissionResult : User has denied location permissions");
                    //Permission Denied
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onInitialLocationReceived(LatLng userCurrentLocation) {
        Log.d(TAG, "onInitialLocationReceived : initial location has been received");
        //Some checks to see if mMap is null or not to avoid crash
        if(mMap != null) {
            progressBar.setVisibility(View.GONE);
            mMap.addMarker(new MarkerOptions().position(userCurrentLocation));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 10f));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onStop() {
        super.onStop();
        userLocationClient.stopUserLocationTracking();
    }
}
