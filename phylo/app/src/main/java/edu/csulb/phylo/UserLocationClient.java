package edu.csulb.phylo;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;


/**
 * Created by Daniel on 1/28/2018.
 * API to retrieve and constantly retrieve the user's current location
 *
 */

public class UserLocationClient
    implements OnLocationUpdatedListener{
    //Constants
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static String TAG = UserLocationClient.class.getSimpleName();
    //Class variables
    private Context context;
    private Map<String, Double> userLocation;
    private Handler handler = new Handler();
    private int updateTime;
    //Interface
    public interface LocationListener{
        void onLocationUpdated(LatLng location);
        void onSingleLocationReceived(LatLng location);
    }
    private LocationListener locationUpdatedListener;

    /**
     * Initialization Constructor
     *
     * @param context The current context of the application
     *
     */
    public UserLocationClient(Context context) {
        this.context = context;
        userLocation = new HashMap<String, Double>();
    }

    /**
     * Starts tracking the User's current location
     */
    public void startUserLocationTracking(int updateTime) {
        this.updateTime = updateTime;
        SmartLocation.with(context).location().start(this);
    }

    /**
     * Retrieves a LatLng object
     *
     * @param context The current context of the application
     */
    public void singleLocationRetrieval(final Context context){
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        try{
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null) {
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        locationUpdatedListener.onSingleLocationReceived(new LatLng(latitude, longitude));
                    }
                }
            });
        }catch(SecurityException exception) {
            Log.d(TAG, "Application does not contain location permission");
        }
    }

    /**
     * Stops tracking the User's current location
     */
    public void stopUserLocationTracking() {
        SmartLocation.with(context).location().stop();
    }

    /**
     * Sets a listener for every time the location has updated
     *
     * @param listener The listener object
     */
    public void setLocationUpdatedListener(LocationListener listener) {
        locationUpdatedListener = listener;
    }

    /**
     *
     * @return The user's previously updated location
     */
    public Map<String, Double> getCurrLocation() {
        return userLocation;
    }

    /**
     * Update the user's current location now
     *
     * @param location The user's current location
     */
    @Override
    public void onLocationUpdated(Location location) {
        userLocation.put(LATITUDE, location.getLatitude());
        userLocation.put(LONGITUDE, location.getLongitude());
        handler.postDelayed(locationRunnable, updateTime);
        LatLng currLocation = new LatLng(location.getLatitude(), location.getLongitude());
        locationUpdatedListener.onLocationUpdated(currLocation);
    }

    /**
     * Runs every 10 seconds
     */
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            startUserLocationTracking(updateTime);
        }
    };
}
