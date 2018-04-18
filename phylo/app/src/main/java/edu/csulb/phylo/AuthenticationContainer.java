package edu.csulb.phylo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;

import edu.csulb.phylo.Astral.AstralUser;

/**
 * Created by Danie on 1/24/2018.
 */

public class AuthenticationContainer extends Activity
        implements LoginFragment.OnChangeFragmentListener {
    //Constants
    private static final String TAG = AuthenticationContainer.class.getSimpleName();
    public static final String START_LOGIN_ACTION = "SLA";
    //Variables
    private CognitoUser cognitoUser;
    private AstralUser astralUser;

    //Enumerator
    public enum AuthFragmentType {
        LOGIN,
        FORGOT_PASSWORD,
        CREATE_ACCOUNT,
        VERIFY_CODE,
    }

    //Fragments
    private LoginFragment loginFragment;
    private CreateAccountFragment createAccountFragment;
    private VerifyCodeFragment verifyCodeFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //Initialize Login Fragment
        loginFragment = new LoginFragment();
        loginFragment.setOnChangeFragmentListener(this);

        //Initialize Verify Code Fragment
        verifyCodeFragment = new VerifyCodeFragment();

        //Initialize Create Account Fragment
        createAccountFragment = new CreateAccountFragment();
        createAccountFragment.setOnAccountCreatedListener(new CreateAccountFragment.OnAccountCreatedListener() {
            @Override
            public void onAccountCreated(CognitoUser receivedCognitoUser, AstralUser receivedAstralUser) {
                cognitoUser = receivedCognitoUser;
                astralUser = receivedAstralUser;
            }

            @Override
            public void onCreateAccountFinished() {
                beginFragment(AuthFragmentType.VERIFY_CODE, true, true);
            }

        });


        //Check why this activity was started
        beginActivityFlow(getIntent().getAction());
    }

    /**
     * Activity has received a result back
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If LoginFragment is currently executing the Login Flow, send the activity result
        //back to the LoginFragment
        if (loginFragment.isSigningIn()) {
            loginFragment.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Gets the reason why the activity was started and begins the respective activity flow
     *
     * @param action Determines the flow of the activity
     */
    private void beginActivityFlow(String action) {
        Log.d(TAG, "activityStartReason : " + action);

        switch (action) {
            case START_LOGIN_ACTION: {
                Log.d(TAG, "beginActivityFlow: Start login fragment");
                beginFragment(AuthFragmentType.LOGIN, false, false);
            }
            break;
        }
    }

    /**
     * Listener to buttons that are clicked in the Login Fragment that requires the activity to
     * switch fragments
     *
     * @param fragmentType The type of fragment that needs to be opened
     */
    @Override
    public void buttonClicked(AuthFragmentType fragmentType) {
        switch (fragmentType) {
            case CREATE_ACCOUNT: {
                Log.d(TAG, "buttonClicked: Start create account fragment");
                beginFragment(AuthFragmentType.CREATE_ACCOUNT, true, true);
            }
            break;
        }
    }

    /**
     * Helper method that replaces the current fragment with one that is specified
     *
     * @param fragmentType   The fragment that should now appear
     * @param setTransition  If the fragment should be transitioned in to the viewer
     * @param addToBackStack If the fragment should be added to the activity's back-stack
     */
    private void beginFragment(AuthFragmentType fragmentType, boolean setTransition, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        switch (fragmentType) {
            case LOGIN: {
                fragmentTransaction.replace(R.id.user_authentication_container, loginFragment);
            }
            break;
            case CREATE_ACCOUNT: {
                fragmentTransaction.replace(R.id.user_authentication_container, createAccountFragment);
            }
            break;
            case VERIFY_CODE: {
                verifyCodeFragment.setUser(cognitoUser, astralUser);
                fragmentTransaction.replace(R.id.user_authentication_container, verifyCodeFragment);
            }
            break;
        }
        if (setTransition) {
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

}
