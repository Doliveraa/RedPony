package edu.csulb.phylo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by vietl on 2/25/2018.
 */

public class MapsFragment extends Fragment
        implements OnMapReadyCallback, UserLocationClient.InitialLocationReceived{
    //Variables
    private boolean hasLocationPermission;
    private UserLocationClient userLocationClient;
    private boolean isRetrievingUserPermission;
    //Constants
    private final String TAG = "MapsActivity";
    private final int PERMISSION_REQUEST_CODE = 2035;
    //Fragments
    private GoogleMap mMap;
    //Views
    private ProgressBar progressBar;

    /**
     * Instantiates an instance of UserFragment
     *
     * @return A UserFragment object
     */
    public static MapsFragment newInstance(){
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize variables
        userLocationClient = new UserLocationClient(getActivity());

        //Initialize Views
        progressBar = getActivity().findViewById(R.id.map_progress_bar);

        //Initialize Listeners
        userLocationClient.setInitialLocationReceiveListener(this);

        //Begin the progress bar
        progressBar.setVisibility(View.VISIBLE);

        //Check if we currently have the user's permission to access their location
        hasLocationPermission = UserPermission.checkUserPermission(getActivity(), UserPermission.Permission.LOCATION_PERMISSION);
    }

    /**
     * Animates the camera to the user's current location
     *
     * @param userCurrentLocation The user's current latitude and longitude
     */
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

    /**
     * Asks for the user's permission, double check just in case, don't want to ask the user a second time
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestPermission : Requesting Fine Location permission");
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Requests user permission to track their current location
     *
     * @param requestCode The request code received back from asking permission
     * @param permissions The permissions asked
     * @param grantResults The granted results
     */
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
                    Toast.makeText(getActivity(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Used to check if the fragment is currently retrieving the user's permissions
     *
     * @return True if the fragment is currently retrieving the user's location, False otherwise
     */
    public boolean isRetrievingLocPermission() {
        return isRetrievingUserPermission;
    }

    /**
     * Begins tracking the user's location if they have permission
     * If they don't have any permissions, this methods asks them for the permissions
     */
    @Override
    public void onStart() {
        super.onStart();
        //If we have the user's permission to receive their location, start the location tracking
        if(hasLocationPermission) {
            userLocationClient.startUserLocationTracking();
        } else {
            //We do not have permission to receive the user's location, ask for permission
            requestPermission();
        }
    }

    /**
     * The map is ready to be displayed
     *
     * @param googleMap The google map object to be used in this fragment
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    /**
     * Stops tracking the user's current location
     */
    @Override
    public void onStop() {
        super.onStop();
        userLocationClient.stopUserLocationTracking();
    }
}
