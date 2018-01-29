package edu.csulb.phylo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Danie on 1/24/2018.
 */

public class AuthenticationActivity extends Activity {
    //Constants
    private static final String TAG = "AuthenticationActivity";
    public static final String USER_LOGIN_ACTION = "ula";
    //Fragments
    private LoginFragment loginFragment = new LoginFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //Check why this activity was started
        activityStartReason(getIntent().getAction());

    }

    private void activityStartReason(String action) {
        Log.d(TAG, "activityStartReason : " + action);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        //If no action is specified, login screen is the default
        if(action == Intent.ACTION_MAIN || action.equals(USER_LOGIN_ACTION)) {
            //Begin Transaction
            fragmentTransaction.add(R.id.user_authentication_container, loginFragment);
        }

        fragmentTransaction.commit();
    }
}
