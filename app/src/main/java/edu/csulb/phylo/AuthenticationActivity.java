package edu.csulb.phylo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInActivity;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

/**
 * Created by Danie on 1/24/2018.
 */

public class AuthenticationActivity extends Activity {
    //Constants
    private static final String TAG = AuthenticationActivity.class.getSimpleName();
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
            final String IDENTITY_POOL_ID = getResources().getString(R.string.cognito_identity_id);
            CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                    this,
                    IDENTITY_POOL_ID,
                    Regions.US_WEST_2
                    );
            final IdentityManager identityManager = new IdentityManager(
                    this,
                    cognitoCachingCredentialsProvider,
                    clientConfiguration
            );
            IdentityManager.setDefaultIdentityManager(identityManager);
            IdentityManager.getDefaultIdentityManager().login(
                    this,
                    new DefaultSignInResultHandler() {
                        @Override
                        public void onSuccess(Activity callingActivity, IdentityProvider provider) {
                            Log.d(TAG, "DefaultSignInResultHandler -> onSuccess: User logged in as: " +
                                    IdentityManager.getDefaultIdentityManager().getCachedUserID());
                            //Send the user to the main activity
                            Intent startMainActivityIntent = new Intent(AuthenticationActivity.this, MainActivityContainer.class);
                            startMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(startMainActivityIntent);
                            finish();
                        }

                        @Override
                        public boolean onCancel(Activity callingActivity) {
                            Log.d(TAG, "DefaultSignInResultHandler -> onCancel: User has canceled");
                            return false;
                        }
                    }
            );

            AuthUIConfiguration uiConfig = new AuthUIConfiguration.Builder()
                    .userPools(true)
                    .build();
            SignInActivity.startSignInActivity(this, uiConfig);
            return;
        }

        switch(action) {

        }
    }
}
