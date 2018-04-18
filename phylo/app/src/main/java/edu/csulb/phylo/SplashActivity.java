package edu.csulb.phylo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Danie on 2/19/2018.
 */

public class SplashActivity extends Activity {
    //Constants
    private final static String TAG = SplashActivity.class.getSimpleName();
    //Cognito login variables
    private CognitoUserPool cognitoUserPool;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize CognitoUserPool object
        cognitoUserPool = AuthHelper.getCognitoUserPool(this);

        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(this);

        //Check if the user has already signed in before
        if (userIsSignedIn()) {
            //Move to main activity because the user is already signed in
            Log.d(TAG, "onCreate: AstralUser is already signed in, moving to main activity");
            Intent moveToMainIntent = new Intent(this, MainActivityContainer.class);
            startActivity(moveToMainIntent);
        } else {
            //Send intent to start the login activity
            Log.d(TAG, "onCreate: AstralUser is not signed in, moving to authentication activity");
            Intent loginIntent = new Intent(this, AuthenticationContainer.class);
            loginIntent.setAction(AuthenticationContainer.START_LOGIN_ACTION);
            startActivity(loginIntent);
        }
    }

    /**
     * Checks if the user is currently already signed in with an account used before
     *
     * @return True if the user is already signed in and false otherwise
     */
    private boolean userIsSignedIn() {
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            Log.d(TAG, "userIsSignedIn: Google sign in: " + googleSignInAccount.getEmail());
            return true;
        } else if (cognitoUserPool.getCurrentUser().getUserId() != null) {
            CognitoUser cognitoUser = cognitoUserPool.getCurrentUser();
            Log.d(TAG, "userIsSignedIn: Normal sign in: " + cognitoUser.getUserId());
            return true;
        } else if (AccessToken.getCurrentAccessToken() != null) {
            AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
            Log.d(TAG, "userIsSignedIn: Facebook sign in: " + facebookAccessToken.getUserId());
            return true;
        }

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
