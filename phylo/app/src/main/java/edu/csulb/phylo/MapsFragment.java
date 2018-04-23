package edu.csulb.phylo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by vietl on 2/25/2018.
 */

public class MapsFragment extends Fragment
        implements OnMapReadyCallback, UserLocationClient.LocationUpdateListener {
    //Constants
    private final String TAG = MapsFragment.class.getSimpleName();
    private final int PERMISSION_REQUEST_CODE = 2035;
    //Class Variables
    private View fragmentView;
    private boolean isRetrievingUserPermission;
    private UserLocationClient userLocationClient;
    private boolean hasLocationPermission;
    //Views
    private MapView mapView;
    private GoogleMap googleMap;

    /**
     * Instantiates an instance of UserFragment
     *
     * @return A MapFragment Object
     */
    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    private class ScreenAnimator extends AsyncTask<LatLng, Void, Void> {
        @Override
        protected Void doInBackground(LatLng... location) {
            googleMap.addMarker(new MarkerOptions().position(location[0]));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location[0], 18f));
            return null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_maps, container, false);
        return fragmentView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Map is Ready, initialize map interface
        MapsInitializer.initialize(getContext());

        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Initialize variables
        isRetrievingUserPermission = false;
        userLocationClient = new UserLocationClient(getActivity());

        //Initialize the map view item
        mapView = fragmentView.findViewById(R.id.map);

        //MapView
        if(mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        //Check if we have location permission
        hasLocationPermission = UserPermission.checkUserPermission(getActivity(),
                UserPermission.Permission.LOCATION_PERMISSION);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(hasLocationPermission) {
            userLocationClient.startUserLocationTracking();
        } else {
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
     * Updates the user's current location
     *
     * @param userCurrentLocation The user's current latitude and longitude
     */
    @Override
    public void onLocationUpdated(LatLng userCurrentLocation) {

        //clear the markers on the map
        googleMap.clear();
        //Place current location marker
        LatLng latLng = new LatLng(userCurrentLocation.latitude, userCurrentLocation.longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        googleMap.addMarker(markerOptions);

        //move map camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(30));
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
     * Used to check if the fragment is currently retrieving the user's permissions
     *
     * @return True if the fragment is currently retrieving the user's location, False otherwise
     */
    public boolean isRetrievingLocPermission() {
        return isRetrievingUserPermission;
    }

}
