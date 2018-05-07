package edu.csulb.phylo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.phylo.Astral.Astral;
import edu.csulb.phylo.Astral.AstralHttpInterface;
import edu.csulb.phylo.Astral.AstralItem;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by vietl on 2/25/2018.
 */

public class MapsFragment extends Fragment
        implements OnMapReadyCallback, UserLocationClient.LocationListener, View.OnClickListener {
    //Constants
    private final String TAG = MapsFragment.class.getSimpleName();
    private final int PERMISSION_REQUEST_CODE = 2035;
    public final static String MAPS_FRAGMENT_PREF = "Maps Fragment";
    public final static String IS_FIRST_TIME = "is first time";
    private final String LATITUDE_PREF = "latitude";
    private final String LONGITUDE_PREF = "longitude";
    private final float DEFAULT_ZOOM = 18;
    //Class Variables
    private View fragmentView;
    private boolean isRetrievingUserPermission;
    private UserLocationClient userLocationClient;
    private boolean hasLocationPermission;
    private boolean isFirstTime;
    private Marker currMarker;
    private LatLng onRestartCurrLocation;
    private User user;
    private List<AstralItem> astralItemList;
    private boolean heatmapActive;
    private TileOverlay mOverLay;
    private HeatmapTileProvider heatmapTileProvider;
    private List<LatLng> list;
    //Views
    private MapView mapView;
    private GoogleMap googleMap;
    private ProgressBar progressBar;


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
            currMarker = googleMap.addMarker(new MarkerOptions().position(location[0]));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location[0], DEFAULT_ZOOM));
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

        if(onRestartCurrLocation != null) {
            progressBar.setVisibility(View.GONE);
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onRestartCurrLocation, DEFAULT_ZOOM));
            currMarker = this.googleMap.addMarker(new MarkerOptions().position(onRestartCurrLocation));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Initialize variables
        isRetrievingUserPermission = false;
        userLocationClient = new UserLocationClient(getActivity());
        isFirstTime = true;
        heatmapActive = false; //To see if the heatmap is active or not
        mOverLay = null;
        astralItemList = new ArrayList<>();
        heatmapTileProvider = null;
        list = new ArrayList<>();


        //Initiate Views
        Button heatmapButton = getActivity().findViewById(R.id.heatmap);

        //Set on click listener for the heatmap button
        heatmapButton.setOnClickListener(this);

        //Initialize the map view item
        mapView = fragmentView.findViewById(R.id.map);
        progressBar = fragmentView.findViewById(R.id.map_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        user = User.getInstance(getActivity());

        //MapView
        if(mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        userLocationClient.setLocationUpdatedListener(this);

        //Check if we have location permission
        hasLocationPermission = UserPermission.checkUserPermission(getActivity(),
                UserPermission.Permission.LOCATION_PERMISSION);
    }

    /**
     * Provides a way for screen items to react to user events
     *
     * @param v the View item that the user has interacted with
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.heatmap:
                //If the user clicks the heat map, toggle it on/off.
                if (!heatmapActive){
                    heatmapActive = true;
                    userLocationClient.singleLocationRetrieval(getActivity());
                }
                else{
                    heatmapActive = false;
                    userLocationClient.singleLocationRetrieval(getActivity());
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isFirstTime = checkIsFirstTime();

        if(hasLocationPermission && isFirstTime) {
            userLocationClient.startUserLocationTracking(1000);
        } else if (hasLocationPermission && !isFirstTime) {
            onRestartCurrLocation = retrieveCachedUserLocation();
            userLocationClient.startUserLocationTracking(1000);
        }else {
            //We do not have permission to receive the user's location, ask for permission
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, UserPermission.PERM_CODE);
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
        cacheUserLocation();
    }

    /**
     * Updates the user's current location
     *
     * @param userCurrentLocation The user's current latitude and longitude
     */
    @Override
    public void onLocationUpdated(LatLng userCurrentLocation) {
        if(isFirstTime) {
            animateToInitialLocation(userCurrentLocation);
        } else {
            animateMarkerToNewLoc(currMarker, new LatLng(userCurrentLocation.latitude, userCurrentLocation.longitude),
                    new LatLngInterpolator.LinearFixed());
        }
    }

    @Override
    public void onSingleLocationReceived(LatLng location) {
        if (heatmapActive){
            createHeatMap(location); //creates the heatmap
        }
        else{
            removeHeatMap(mOverLay);
        }
    }

    /**
     * Animate the screen to the user's current location
     *
     * @param location The user'c current location
     */
    private void animateToInitialLocation(LatLng location) {
        ScreenAnimator screenAnimator = new ScreenAnimator();
        if(googleMap != null) {
            progressBar.setVisibility(View.GONE);
            screenAnimator.doInBackground(location);
            SharedPreferences sharedPreferences = getSharedPref();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(IS_FIRST_TIME, false);
            editor.commit();
        }
    }

    private void animateMarkerToNewLoc(final Marker marker, final LatLng finalPosition,
                                       final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                //Repeat till process is complete
                if(t < 1) {
                    //Post again 16ms later
                    handler.postDelayed(this, 16);
                }
            }
        });
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
                    userLocationClient.startUserLocationTracking(1000);
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
     * Checks if this is the first time that the Maps Fragment is updating
     *
     * @return True if it is the first time the user has started this Fragment, false otherwise
     */
    private boolean checkIsFirstTime() {
        SharedPreferences sharedPreferences = getSharedPref();
        return sharedPreferences.getBoolean(IS_FIRST_TIME, true);
    }

    /**
     * Cache user location
     */
    private void cacheUserLocation() {
        if(userLocationClient.getCurrLocation().get(UserLocationClient.LATITUDE) != null){
            double latitude = userLocationClient.getCurrLocation().get(UserLocationClient.LATITUDE);
            double longitude = userLocationClient.getCurrLocation().get(UserLocationClient.LONGITUDE);
            //Store the values
            SharedPreferences sharedPreferences = getSharedPref();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(LATITUDE_PREF, Double.toString(latitude));
            editor.putString(LONGITUDE_PREF, Double.toString(longitude));
            editor.apply();
        }
    }

    /**
     *
     * @return Retrieve and return the cached user location
     */
    private LatLng retrieveCachedUserLocation() {
        SharedPreferences sharedPreferences = getSharedPref();
        double latitude = Double.parseDouble(sharedPreferences.getString(LATITUDE_PREF, ""));
        double longitude = Double.parseDouble(sharedPreferences.getString(LONGITUDE_PREF, ""));
        return new LatLng(latitude, longitude);
    }

    /**
     * Retrieve's the fragment's shared preferences folder
     *
     * @return The Fragments shared preferences folder
     */
    public SharedPreferences getSharedPref() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MAPS_FRAGMENT_PREF,
                Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    /**
     * Retrieves all if the rooms around the area
     */
    private void createHeatMap(final LatLng currUserLocation){
        //Start a GET request to retrieve all of the rooms in the area
        final Astral astral = new Astral(getActivity().getString(R.string.astral_base_url));
        //Add logging interceptor
        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();

        //Create the GET request
        Call<List<AstralItem>> request = astralHttpInterface.getRooms(
                getString(R.string.astral_key),
                currUserLocation.latitude,
                currUserLocation.longitude,
                20000,
                user.getUserAstralTokens()
        );

        request.enqueue(new Callback<List<AstralItem>>() {
            @Override
            public void onResponse(Call<List<AstralItem>> call, retrofit2.Response<List<AstralItem>> response) {
                Log.d(TAG, "retrieveRooms-> onResponse: ");
                if (response.code() == Astral.OK) {
                    Log.d(TAG, "retrieveRooms-> onResponse: Success Code : " + response.code());
                    astralItemList = response.body();
                    //Progress bar must dissapear, we have loaded all the rooms
                    addHeatMap();
                }
            }

            @Override
            public void onFailure(Call<List<AstralItem>> call, Throwable t) {
                Log.w(TAG, "retrieveRooms-> onFailure");
                t.printStackTrace();
            }
        });
    }

    private void addHeatMap() {
        //List<LatLng> list = null;
        Log.d(TAG, "retrieveRooms-> Heatmap created from data set ");
        // Get the latitude/longitude positions of files : GET Request
        readItems();//Read the list of current rooms
        // Create a heat map tile provider, passing it the latlngs
        if (list != null && !list.isEmpty()) {
            heatmapTileProvider = new HeatmapTileProvider.Builder()
                    .data(list)
                    .build();
            mOverLay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
        }
        // Add a tile overlay to the map, using the heat map tile provider.
        //If there is nothing returned to the rooms, don't create a heatmap
    }

    /**
     * From the list of given rooms, return the lat and long of each one and add it
     * to the list for GoogleMaps
     * @return the list of locations for the heatmap
     */
    private void readItems() {

        if (astralItemList != null){
            for (int i = 0; i < astralItemList.size(); i++){
                AstralItem astralItem = astralItemList.get(i);
                list.add(new LatLng(astralItem.getLocation().get(1), astralItem.getLocation().get(0)));
            }
        }
        else{
            list.add(new LatLng(0,0));//Display some random coordinate
        }
        return;
    }

    /**
     * removes the HeatMap created
     * @param mOverlay the overlay of the heatmap to be removed
     */
    private void removeHeatMap(TileOverlay mOverlay){
        Log.d(TAG, "Removing Active HeatMap ");
        if((list != null && !list.isEmpty())){
            mOverlay.remove();
            mOverlay.clearTileCache();
        }
    }
}