package edu.csulb.phylo;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by vietl on 2/25/2018.
 */

public class MapsFragment extends Fragment
        implements OnMapReadyCallback, UserLocationClient.InitialLocationReceived, UserLocationClient.CurrLocationListener {
    //Constants
    private final String TAG = MapsFragment.class.getSimpleName();
    private final int PERMISSION_REQUEST_CODE = 2035;
    //View variables
    private GoogleMap googleMap;
    private MapView mapView;
    private View fragmentView;
    private ProgressBar progressBar;
    //Other Variables
    private boolean hasLocationPermission;
    private UserLocationClient userLocationClient;
    private boolean isRetrievingUserPermission;

    private class ScreenAnimator extends AsyncTask<LatLng, Void, Void> {
        @Override
        protected Void doInBackground(LatLng... location) {
            googleMap.addMarker(new MarkerOptions().position(location[0]));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location[0], 10f));
            return null;
        }
    }

    /**
     * Instantiates an instance of UserFragment
     *
     * @return A UserFragment object
     */
    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_maps, container, false);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Initialize the map view item
        mapView = fragmentView.findViewById(R.id.map);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        //Initialize Fragment Views
        progressBar = fragmentView.findViewById(R.id.map_progress_bar);
        //Begin the progress bar
        progressBar.setVisibility(View.VISIBLE);

        //Initialize variables
        userLocationClient = new UserLocationClient(getActivity());

        //Initialize Listeners
        userLocationClient.setInitialLocationReceiveListener(this);
        userLocationClient.setCurrLocationListener(this);

        //Check if we currently have the user's permission to access their location
        hasLocationPermission = UserPermission.checkUserPermission(getActivity(), UserPermission.Permission.LOCATION_PERMISSION);
    }

    /**
     * Begins tracking the user's location if they have permission
     * If they don't have any permissions, this methods asks them for the permissions
     */
    @Override
    public void onStart() {
        super.onStart();
        //If we have the user's permission to receive their location, start the location tracking
        if (hasLocationPermission) {
            userLocationClient.startUserLocationTracking();
        } else {
            //We do not have permission to receive the user's location, ask for permission
            requestPermission();
        }
    }

    /**
     * Stops tracking the user's current location
     */
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Stopping user's location tracking");
        userLocationClient.stopUserLocationTracking();
    }

    /**
     * Asks for the user's permission, double check just in case, don't want to ask the user a second time
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestPermission : Requesting Fine Location permission");
            isRetrievingUserPermission = true;
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Requests user permission to track their current location
     *
     * @param requestCode  The request code received back from asking permission
     * @param permissions  The permissions asked
     * @param grantResults The granted results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission has been granted, we can now start tracking the user's location
                    Log.d(TAG, "onRequestPermissionResult : AstralUser has accepted location permissions");
                    userLocationClient.startUserLocationTracking();
                    hasLocationPermission = true;
                } else {
                    Log.d(TAG, "onRequestPermissionResult : AstralUser has denied location permissions");
                    //Permission Denied
                    Toast.makeText(getActivity(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Animates the camera to the user's current location
     *
     * @param userCurrentLocation The user's current latitude and longitude
     */
    @Override
    public void onInitialLocationReceived(LatLng userCurrentLocation) {
        Log.d(TAG, "onInitialLocationReceived : initial location has been received");
        if (googleMap != null) {
            progressBar.setVisibility(View.GONE);
        }
        ScreenAnimator screenAnimator = new ScreenAnimator();
        screenAnimator.doInBackground(userCurrentLocation);
    }


    /**
     * Updates the user's current location
     *
     * @param userCurrentLocation The user's current latitude and longitude
     */
    @Override
    public void onLocationUpdated(LatLng userCurrentLocation) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Map is Ready, initialize map interface
        MapsInitializer.initialize(getContext());

        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    /**
     * Used to check if the fragment is currently retrieving the user's permissions
     *
     * @return True if the fragment is currently retrieving the user's location, False otherwise
     */
    public boolean isRetrievingLocPermission() {
        return isRetrievingUserPermission;
    }

}
