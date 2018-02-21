package edu.csulb.phylo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;

/**
 * Created by Danie on 1/24/2018.
 */

public class AuthenticationActivity extends Activity {
    //Constants
    private static final String TAG = "AuthenticationActivity";
    public static final String START_LOGIN_ACTION ="SLA";
    //Fragments
    private LoginFragment loginFragment = new LoginFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //Check why this activity was started
        beginActivityFlow(getIntent().getAction());
    }

    /**
     * Gets the reason why the activity was started and begins the respective activity flow
     *
     * @param action Determines the flow of the activity
     *
     */
    private void beginActivityFlow(String action) {
        Log.d(TAG, "activityStartReason : " + action);

        //Activity was started for user login flow
        if(action.equals(START_LOGIN_ACTION)) {
            ClientConfiguration clientConfiguration = new ClientConfiguration();

            //Read from text file and create variables with needed information
            //TODO: Create variables here -------------------------
            String clientID = null;

            //-----------------------------------------------------
            //Create a CognitoUserPool object to refer to your user pool

            return;
        }

        switch(action) {

        }
    }
}
