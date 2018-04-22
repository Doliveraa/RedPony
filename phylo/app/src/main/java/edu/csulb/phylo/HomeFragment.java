package edu.csulb.phylo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by vietl on 2/25/2018.
 */

public class HomeFragment extends Fragment
    implements View.OnClickListener{

    //Phone Hardware
    private Vibrator vibrator;
    //Constants
    private final String TAG = HomeFragment.class.getSimpleName();
    //Views
    FloatingActionButton fabCreateRoom;
    //Variables
    private boolean roomLockedChoice;

    public static HomeFragment newInstance(){
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize Variables
        roomLockedChoice = false;

        //Initialize Hardware
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        //Initialize Views
        fabCreateRoom = getActivity().findViewById(R.id.fab_create_room);

        //Set listeners
        fabCreateRoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.fab_create_room: {
                Log.d(TAG, "User clicked on Create Room FAB");
                //Start Alert Dialog to create a Room
                AlertDialog roomCreationDialog = createRoomDialog();
                //Show the Alert Dialog
                roomCreationDialog.show();
            }
            break;
        }
    }

    /**
     * Create an AlertDialog object to allow the user to create
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

        //Set Listeners
        setExpirationDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpirationDateButton.setVisibility(View.GONE);
                setDateLinear.setVisibility(View.VISIBLE);
            }
        });
        cancelSetDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setDateLinear.setVisibility(View.GONE);
                setExpirationDateButton.setVisibility(View.VISIBLE);
            }
        });
        setButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(dateSpinner.getVisibility() == View.VISIBLE) {
                    //Get the date values
                    int month = dateSpinner.getMonth();
                    int day = dateSpinner.getDayOfMonth();
                    int year = dateSpinner.getYear();
                    String expDate = month + "/" + day + "/" + year;
                    //Display the chosen expiration date
                    expirationDateText.setText(expDate);
                    dateSpinner.setVisibility(View.GONE);
                    expirationDateText.setVisibility(View.VISIBLE);
                    timeSpinner.setVisibility(View.VISIBLE);
                } else {
                    //Get the time values
                    int hour = timeSpinner.getHour();
                    int minutes = timeSpinner.getMinute();
                    String dayTime = "AM";
                    if(hour > 12) {
                        dayTime = "PM";
                        hour -= 12;
                    }
                    String time = hour + ":" + minutes + " " + dayTime;
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
        expirationTimeText.setOnClickListener(new View.OnClickListener(){
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
                if(roomLockedChoice) {
                    //User wants the room to be unlocked
                    roomLockedChoice = false;
                    //Change the button color back to red
                    lockRoomButton.setBackgroundColor(getActivity().getColor(R.color.red));
                    lockRoomButton.setText("Lock Room");
                } else {
                    //User wants to lock the room
                    roomLockedChoice = true;
                    //Change the button color to green
                    lockRoomButton.setBackgroundColor(getActivity().getColor(R.color.green));
                    lockRoomButton.setText("Room is Locked");
                }
            }
        });
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomName = roomNameEditText.getText().toString();

                //Check if the Room name is in the correct format
                if(roomName.isEmpty()) {
                    displayToast("Room name cannot be empty", true);
                } else if (!roomNameIsValid(roomName)) {
                    displayToast("Room name\n3-12 Characters\na-z, A-Z, 0-9", true);
                } else {
                    //Check if the room name already exists
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
     * Displays a message as a toast
     * @param message The message to be displayed
     * @param vibratePhone If the phone should vibrate
     */
    private void displayToast(String message, boolean vibratePhone) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.show();
        if(vibratePhone) {
            vibrator.vibrate(500);
        }
    }

    /**
     * Checks if the room name contains the proper format
     *
     * @param roomName The room name
     *
     * @return True if the room name is valid
     */
    private boolean roomNameIsValid(String roomName) {
        String expression = "(?:[a-zA-Z0-9._-]{3,12}$)";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(roomName);

        return matcher.matches();
    }
}
