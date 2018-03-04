package edu.csulb.phylo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Danie on 2/19/2018.
 */

public class SplashActivity extends Activity {
    //Constants
    private final static String TAG = SplashActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(userIsSignedIn()) {
            //Move to main activity because the user is already signed in
            Log.d(TAG, "onCreate: User is already signed in, moving to main activity");
            Intent moveToMainIntent = new Intent(this, MainActivityContainer.class);
            startActivity(moveToMainIntent);
        } else {
            //Send intent to start the login activity
            Log.d(TAG, "onCreate: User is not signed in, moving to authentication activity");
            Intent loginIntent = new Intent(this, AuthenticationActivity.class);
            loginIntent.setAction(AuthenticationActivity.START_LOGIN_ACTION);
            startActivity(loginIntent);
        }
    }

    /**
     * Checks if the user is currently already signed in with an account used before
     *
     * @return True if the user is already signed in and false otherwise
     */
    private boolean userIsSignedIn() {
        if(GoogleSignIn.getLastSignedInAccount(this) != null) {
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            Log.d(TAG, "userIsSignedIn: Google sign in: " + googleSignInAccount.getEmail());
            return true;
        }
//        } else if(AccessToken.getCurrentAccessToken() != null) {
//            AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
//            Log.d(TAG, "userIsSignedIn: Facebook sign in: " + facebookAccessToken.getUserId());
//            return true;
//        }

        return false;
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
