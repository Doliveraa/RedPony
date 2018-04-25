package edu.csulb.phylo;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

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
    //Class variables
    private Context context;
    private Map<String, Double> userLocation;
    private Handler handler = new Handler();
    //Interface
    public interface LocationUpdateListener{
        void onLocationUpdated(LatLng location);
    }
    private LocationUpdateListener locationUpdatedListener;

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
    public void startUserLocationTracking() {
        SmartLocation.with(context).location().start(this);
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
    public void setLocationUpdatedListener(LocationUpdateListener listener) {
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
        final int LOCATION_UPDATE_DELAY = 1000;
        handler.postDelayed(locationRunnable, LOCATION_UPDATE_DELAY);
        LatLng currLocation = new LatLng(location.getLatitude(), location.getLongitude());
        locationUpdatedListener.onLocationUpdated(currLocation);
    }

    /**
     * Runs every 10 seconds
     */
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            startUserLocationTracking();
        }
    };
}
