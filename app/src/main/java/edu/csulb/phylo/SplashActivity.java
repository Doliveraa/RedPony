package edu.csulb.phylo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by Danie on 2/19/2018.
 */

public class SplashActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Send intent to start the login activity
        Intent loginIntent = new Intent(this, AuthenticationActivity.class);
        loginIntent.setAction(AuthenticationActivity.START_LOGIN_ACTION);
        startActivity(loginIntent);
    }

    /**
     * Right when the activity goes out of sight, pop it from the stack
     */
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
