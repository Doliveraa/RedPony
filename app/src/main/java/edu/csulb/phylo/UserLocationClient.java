package edu.csulb.phylo;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;


/**
 * Created by Daniel on 1/28/2018.
 * API to retrieve and constantly retrieve the user's current location
 *
 */

public class UserLocationClient {
    //Variables
    private double latitude;
    private double longitude;
    private Context context;
    private Handler handler = new Handler();
    private boolean initialLocationReceived;
    //Interface
    public interface InitialLocationReceived{
        void onInitialLocationReceived(LatLng userCurrentLocation);
    }
    //Listener
    private OnLocationUpdatedListener locationListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            handler.postDelayed(locationRunnable, 10000);
            if(!initialLocationReceived) {
                initialLocationReceiveListener.onInitialLocationReceived(new LatLng(latitude, longitude));
            }
        }
    };
    private InitialLocationReceived initialLocationReceiveListener;

    //Initialization constructor
    public UserLocationClient(Context context) {
        this.context = context;
        initialLocationReceived = false;
    }

    //Begins tracking the user's location
    public void startUserLocationTracking() {
        SmartLocation.with(context).location().start(locationListener);
    }

    //Stops tracking the user's location
    public void stopUserLocationTracking() {
        SmartLocation.with(context).location().stop();
    }

    //Retrieves the user's location only if the initial location has been received
    public LatLng getUserLocation() {
        if(initialLocationReceived) {
            return new LatLng(latitude, longitude);
        }
        return null;
    }

    //Sets the listener that keeps track of if the initial location has been received
    public void setInitialLocationReceiveListener(InitialLocationReceived listener) {
        initialLocationReceiveListener = listener;
    }

    //Runnable that constantly runs the same method depending on the timer
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            SmartLocation.with(context).location().start(locationListener);
        }
    };
}
