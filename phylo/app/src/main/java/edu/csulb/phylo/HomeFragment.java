package edu.csulb.phylo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.csulb.phylo.Astral.Astral;
import edu.csulb.phylo.Astral.AstralHttpInterface;
import edu.csulb.phylo.Astral.AstralItem;
import edu.csulb.phylo.Astral.RoomKey;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;


/**
 * Created by vietl on 2/25/2018.
 */

public class HomeFragment extends Fragment
        implements View.OnClickListener, UserLocationClient.LocationListener {

    public interface OnRoomEnterListener{
        void roomEntered();
    }

    //Fragment View
    private View fragView;
    //Variables
    private boolean creatingRoom;
    private boolean updateRoomList;
    //Constants
    private final String TAG = HomeFragment.class.getSimpleName();
    //Views
    private FloatingActionButton fabCreateRoom;
    private ProgressBar progressBar;
    private User user;
    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<AstralItem> astralItemList;
    private AlertDialog alertDialog;
    //Location Permissions Variables
    private boolean hasLocationPermission;
    private UserLocationClient userLocationClient;
    private String roomName;
    private StringBuilder expiration;
    private String passwordKey;
    //Interface
    OnRoomEnterListener onRoomEnterListener;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Make sure that the container activity has implemented
        try{
            onRoomEnterListener = (OnRoomEnterListener) context;
        }catch(ClassCastException exception) {
            throw new ClassCastException(context.toString()
            +  " must implement OnRoomEnterListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_home, container, false);
        return fragView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) throws SecurityException {
        super.onActivityCreated(savedInstanceState);

        //Initialize Variables
        userLocationClient = new UserLocationClient(getActivity());
        creatingRoom = false;
        passwordKey = "";
        updateRoomList = false;
        expiration = new StringBuilder();
        expiration.setLength(0);

        //Initialize Views
        fabCreateRoom = fragView.findViewById(R.id.fab_create_room);
        progressBar = fragView.findViewById(R.id.progress_bar_home);
        recyclerView = fragView.findViewById(R.id.recycler_view_home);

        //Set that the recycler view has a fixed size to increase performance
        recyclerView.setHasFixedSize(true);

        //Use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        progressBar.setVisibility(View.VISIBLE);

        //Set listeners
        fabCreateRoom.setOnClickListener(this);
        userLocationClient.setLocationUpdatedListener(this);

        user = User.getInstance(getActivity());

        //Check to see if we have the user's permission
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
            userLocationClient.singleLocationRetrieval(getActivity());
            userLocationClient.startUserLocationTracking(5000);
        } else {
            //We do not have permission to receive the user's location, ask for permission
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, UserPermission.PERM_CODE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_create_room: {
                Log.d(TAG, "User clicked on Create Room FAB");
                //Start Alert Dialog to create a Room
                alertDialog = createRoomDialog();
                //Show the Alert Dialog
                alertDialog.show();
            }
            break;
        }
    }

    /**
     * Create an AlertDialog object to allow the user to create
     *
     * @return
     */
    private AlertDialog createRoomDialog() {
        //Create an instance of the Alert Dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        //Set the View of the Alert Dialog
        final View alertDialogView = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_create_room, null);
        alertDialogBuilder.setView(alertDialogView);

        //Initialize Views for this Fragment
        final Button setExpirationDateButton = (Button) alertDialogView.findViewById(R.id.button_set_expiration);
        final Button lockRoomButton = (Button) alertDialogView.findViewById(R.id.button_lock_room);
        final Button createRoom = (Button) alertDialogView.findViewById(R.id.button_create_room);
        final LinearLayout setDateLinear = (LinearLayout) alertDialogView.findViewById(R.id.set_expiration_date_layout);
        final Button cancelSetDateButton = (Button) alertDialogView.findViewById(R.id.button_cancel_set_date);
        final DatePicker dateSpinner = (DatePicker) alertDialogView.findViewById(R.id.date_spinner);
        final TimePicker timeSpinner = (TimePicker) alertDialogView.findViewById(R.id.time_spinner);
        final Button setButton = (Button) alertDialogView.findViewById(R.id.button_set_value);
        final TextView expirationDateText = (TextView) alertDialogView.findViewById(R.id.textview_expiration_date);
        final TextView expirationTimeText = (TextView) alertDialogView.findViewById(R.id.textview_expiration_time);
        final EditText roomNameEditText = (EditText) alertDialogView.findViewById(R.id.edit_text_room_name);
        final EditText setPassword = (EditText) alertDialogView.findViewById(R.id.password_edit_text_set);
        final Button cancelPassword = (Button) alertDialogView.findViewById(R.id.button_cancel_password);

        //Set Listeners
        setExpirationDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpirationDateButton.setVisibility(View.GONE);
                setDateLinear.setVisibility(View.VISIBLE);
            }
        });
        cancelSetDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateLinear.setVisibility(View.GONE);
                setExpirationDateButton.setVisibility(View.VISIBLE);
            }
        });
        cancelPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPassword.setVisibility(View.GONE);
                cancelPassword.setVisibility(View.GONE);
                lockRoomButton.setVisibility(View.VISIBLE);
            }
        });
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateSpinner.getVisibility() == View.VISIBLE) {
                    //Get the date values
                    int month = dateSpinner.getMonth() + 1;
                    int day = dateSpinner.getDayOfMonth();
                    int year = dateSpinner.getYear();
                    String day00 = (day < 10 ? "0" : "") + day;
                    String month00 = (month < 10 ? "0" : "") + month;
                    expiration.append(year + "-" + month00 + "-" + day00 + "T");
                    String expDate = month00 + "/" + day00 + "/" + year;
                    //Display the chosen expiration date
                    expirationDateText.setText(expDate);
                    dateSpinner.setVisibility(View.GONE);
                    expirationTimeText.setVisibility(View.GONE);
                    expirationDateText.setVisibility(View.VISIBLE);
                    timeSpinner.setVisibility(View.VISIBLE);
                } else {
                    //Get the time values
                    int hour = timeSpinner.getHour();
                    int minutes = timeSpinner.getMinute();
                    String hour00 = (hour < 10 ? "0" : "") + hour;
                    String minutes00 = (minutes < 10 ? "0" : "") + minutes;
                    String dayTime = "AM";
                    expiration.append(hour00 + ":" + minutes00 + ":43.511Z");
                    if (hour > 12) {
                        dayTime = "PM";
                        hour -= 12;
                        hour00 = (hour < 10 ? "0" : "") + hour;
                    }
                    String time = hour00 + ":" + minutes00 + " " + dayTime;
                    //Display the time to the user
                    expirationTimeText.setText(time);
                    setButton.setVisibility(View.GONE);
                    expirationTimeText.setVisibility(View.VISIBLE);
                    timeSpinner.setVisibility(View.GONE);
                }
            }
        });
        expirationDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expirationDateText.setVisibility(View.GONE);
                dateSpinner.setVisibility(View.VISIBLE);
                setButton.setVisibility(View.VISIBLE);
            }
        });
        expirationTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expirationTimeText.setVisibility(View.GONE);
                timeSpinner.setVisibility(View.VISIBLE);
                setButton.setVisibility(View.VISIBLE);
            }
        });
        lockRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lockRoomButton.getVisibility() == View.GONE) {
                    //User wants the room to have no password
                    //Remove the button_room
                    lockRoomButton.setVisibility(View.VISIBLE);
                    //The cancel button_room is gone
                    setPassword.setVisibility(View.GONE);
                    cancelPassword.setVisibility(View.GONE);
                } else {
                    //User wants the room to have a password
                    //Remove the button_room
                    lockRoomButton.setVisibility(View.GONE);
                    //Allow the cancel button_room to appear
                    setPassword.setVisibility(View.VISIBLE);
                    cancelPassword.setVisibility(View.VISIBLE);
                }
            }
        });
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomName = roomNameEditText.getText().toString();
                passwordKey = setPassword.getText().toString();

                //Check if the Room name is in the correct format
                if (roomName.isEmpty()) {
                    UserNotification.displayToast(
                            getActivity(),
                            "Room name cannot be empty",
                            true);
                } else if (!roomNameIsValid(roomName)) {
                    UserNotification.displayToast(
                            getActivity(),
                            "Room name\n3-12 Characters\na-z, A-Z, 0-9",
                            true);
                } else {
                    //Check if the room name already exists
                    for (AstralItem astralItem : astralItemList) {
                        if (astralItem.getName().equals(roomName)) {
                            //Room with the same name exists nearby, display error message
                            UserNotification.displayToast(
                                    getActivity(),
                                    "Room name with the same name \nalready exists nearby",
                                    true);
                            return;
                        }
                    }
                    creatingRoom = true;
                    //No rooms nearby that have the same name, create the room
                    userLocationClient.singleLocationRetrieval(getActivity());
                }
            }
        });

        //Set the minimum and maximum date on the Date Spinner object
        final double maximumTime = 6.307e10;
        long currentTime = Calendar.getInstance().getTimeInMillis();
        dateSpinner.setMinDate(currentTime);
        dateSpinner.setMaxDate((long) (currentTime + maximumTime));


        return alertDialogBuilder.create();
    }

    /**
     * Retrieves all if the rooms around the area
     */
    private void retrieveRooms(final LatLng currUserLocation, final boolean toUpdate) {
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
                10,
                user.getUserAstralTokens()
        );

        request.enqueue(new Callback<List<AstralItem>>() {
            @Override
            public void onResponse(Call<List<AstralItem>> call, retrofit2.Response<List<AstralItem>> response) {
                if (response.code() == Astral.OK) {
                    Log.d(TAG, "retrieveRooms-> onResponse: Success Code : " + response.code());
                    astralItemList = response.body();
                    //Progress bar must disappear, we have loaded all the rooms
                    if(!toUpdate) {
                        progressBar.setVisibility(View.GONE);
                        adapter = new RoomAdapter(astralItemList, new RoomAdapter.OnRoomClickedListener() {
                            @Override
                            public void onRoomClick(String roomPassword) {
                                if(!roomPassword.isEmpty()) {
                                    alertDialog = createInputPasswordDialog(roomPassword);
                                    alertDialog.show();
                                } else{
                                    //Enter room

                                }
                            }
                        });
                        recyclerView.setAdapter(adapter);
                        updateRoomList = true;
                    } else {
                        //We are updating the set of rooms
                        adapter.changeData(astralItemList);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AstralItem>> call, Throwable throwable) {
                Log.w(TAG, "retrieveRooms-> onFailure");
            }
        });
    }


    /**
     * Checks if the room name contains the proper format
     *
     * @param roomName The room name
     * @return True if the room name is valid
     */
    private boolean roomNameIsValid(String roomName) {
        String expression = "(?:[a-zA-Z0-9._-]{3,12}$)";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(roomName);

        return matcher.matches();
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
            case UserPermission.PERM_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission has been granted, we can now start tracking the user's location
                    Log.d(TAG, "onRequestPermissionResult : AstralUser has accepted location permissions");
                    hasLocationPermission = true;
                    //The user has given us their Location Permissions
                    userLocationClient.singleLocationRetrieval(getActivity());
                    userLocationClient.startUserLocationTracking(5000);
                } else {
                    Log.d(TAG, "onRequestPermissionResult : AstralUser has denied location permissions");
                    //Permission Denied
                    Toast.makeText(getActivity(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Creates an astral room given the parameters
     *
     * @param roomName     the name of the room
     * @param currLocation The current location
     * @param expiration   when the room expires
     */
    private void createAstralRoom(final String roomName, final LatLng currLocation,
                                  String expiration) {
        final double longit = currLocation.longitude;
        final double lat = currLocation.latitude;
        //Checks if the user has set an expiration date
        if(expiration.length() == 0) {
            expiration = "2100-04-23T18:25:43.511Z";
        }

        //Put the longitude and the latitude into a double arraylist
        final ArrayList<Double> location = new ArrayList<>();
        location.add(lat); //lat
        location.add(longit); //longit

        //Creating Astral Room to send
        final AstralItem astralItem = new AstralItem();
        astralItem.setOwner(user.getName());//owner
        astralItem.setName(roomName);//roomName
        astralItem.setLocation(location);//location
        astralItem.setExpirationDate(expiration);//expiration
        RoomKey roomKey = new RoomKey(passwordKey);
        Gson gson = new Gson();
        String roomString = gson.toJson(roomKey);

        //Astral Start POST Request
        final Astral astral = new Astral(getString(R.string.astral_base_url));
        //Intercept the request to add a header item
        astral.addRequestInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                //Add the app key and token to the request header
                Request.Builder newRequest = request.newBuilder().header(
                        Astral.APP_KEY_HEADER, getString(R.string.astral_key))
                        .header("token", user.getUserAstralTokens());
                //Continue the request
                return chain.proceed(newRequest.build());
            }
        });
        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();
        Log.d(TAG, "Interface");
        //Create the POST request
        Call<ResponseBody> request = astralHttpInterface.createRoom(astralItem.getName(), astralItem.getLatitude(),
                astralItem.getLongitude(), astralItem.getExpirationDate(), roomString);
        Log.d(TAG, "Create Post request");

        //Call the request asynchronously
        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.code() == Astral.OK) {
                    Log.d(TAG, "onClick-> onSuccess-> onResponse: Successful Response Code " + response.code() +
                            " File Created Successfully");
                    //We have finished creating the room, update the view
                    creatingRoom = false;
                    userLocationClient.singleLocationRetrieval(getActivity());
                    alertDialog.dismiss();
                } else if (response.code() == Astral.FILE_NAME_CONFLICT) {
                    Log.d(TAG, "onClick-> onSuccess-> onResponse: File name already exists.");
                    UserNotification.displayToast(
                            getActivity(),
                            "Room name already exists.", true);
                } else {
                    Log.d(TAG, "onClick-> onSuccess-> onResponse: Failed response Code " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //The request has unexpectedly failed
                Log.d(TAG, "createAstralRoom-> onClick-> onSuccess-> onResponse: Unexpected request failure");
                t.printStackTrace();
            }
        });
    }

    /**
     * Listens for the user's updated locations
     *
     * @param location The user's current location
     */
    @Override
    public void onLocationUpdated(LatLng location) {
        retrieveRooms(location, updateRoomList);
    }

    /**
     * Retrieve single location update
     *
     * @param location The user's current location
     */
    @Override
    public void onSingleLocationReceived(LatLng location) {
        Log.d(TAG, "onActivityCreated-> onSingleLocationReceived: Attempting to retrieve rooms");
        if (!creatingRoom) {
            retrieveRooms(location, updateRoomList);
        } else {
            createAstralRoom(roomName, location, expiration.toString());
        }
    }


    /**
     * Creates and shows a dialog for the user to input a password to enter the room
     *
     * @param password The room's password
     *
     * @return An alert dialog
     */
    public AlertDialog createInputPasswordDialog(final String password) {
        //Create an instance of the Alert Dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        //Set the view of the Alert Dialog
        final View alertDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.alert_dialog_get_room_password, null);
        alertDialogBuilder.setView(alertDialogView);

        //Initialize Views within the fragment
        final EditText passwordInput = alertDialogView.findViewById(R.id.edit_text_room_password_input);
        final Button enterButton = alertDialogView.findViewById(R.id.button_enter_room_password);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = passwordInput.getText().toString();
                if(!userInput.equals(password)) {
                    UserNotification.displayToast(getActivity(), "Wrong Password", false);
                } else {
                    //Let the user enter the room
                    onRoomEnterListener.roomEntered();
                }
            }
        });

        return alertDialogBuilder.create();
    }
}
