package edu.csulb.phylo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Danie on 1/24/2018.
 */

public class LoginFragment extends Fragment
        implements View.OnClickListener {
    //Constants
    private final static int RC_SIGN_IN = 9001;
    private final static String TAG = LoginFragment.class.getSimpleName();
    //Login Variables
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private CognitoUserPool cognitoUserPool;
    private CognitoUser cognitoUser;
    private boolean isSigningIn;
    //Views
    private EditText emailEditText;
    private EditText passwordEditText;
    //Other Variables
    private AlertDialog alertDialog;
    //Interface
    public interface OnChangeFragmentListener {
        void buttonClicked(AuthenticationContainer.AuthFragmentType fragmentType);
    }
    private OnChangeFragmentListener onChangeFragmentListener;
    //Listeners
    AuthenticationHandler authHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            Log.d(TAG, "AuthenticationHandler->onSuccess: Logged in with :" + cognitoUser.getUserId());

            //Retrieve user information and store it inside of SharedPreferences
            cognitoUser.getDetailsInBackground(new GetDetailsHandler() {
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    Log.d(TAG, "getDetailsInBackground-> onSuccess: Retrieving Cognito information");

                    //Cache the user's information to be used for a quicker reference later
                    AuthHelper.cacheUserInformation(getActivity(), cognitoUserDetails);

                    //Dismiss the alert dialog
                    alertDialog.dismiss();
                    //Add that the current sign in provider is Cognito
                    AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.COGNITO_PROVIDER);

                    //Start the main activity
                    startMainActivity();
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.w(TAG, "getDetailsInBackground-> onFailure: Unable to retrieve user information");
                    exception.printStackTrace();
                }
            });
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            Log.d(TAG, "AuthenticationHandler->getAuthenticationDetails");
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
            Log.d(TAG, "AuthenticationHandler->getMFACode");
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            Log.d(TAG, "AuthenticationHandler->authenticationChallenge");
        }

        @Override
        public void onFailure(Exception exception) {
            Log.w(TAG, "AuthenticationHandler->onFailure");
            exception.printStackTrace();

            //Dismiss the current alert dialog to show a new one
            alertDialog.dismiss();
            //Create error dialog for the error case
            alertDialog = createErrorDialog("Email and Password combination not found");
            alertDialog.show();
        }
    };

    /**
     * Used to communicate with the Cognito User Pool login for normal logins
     */
    private class CognitoAuthHelper extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String userEmail = strings[0];
            String userPassword = strings[1];

            Log.d(TAG, "AuthHelper: doInBackground");

            //Initiate AuthenticationDetails object to be used with the Authentication Flow
            //In this case, SRP Auth
            AuthenticationDetails authDetails = new AuthenticationDetails(userEmail, userPassword,
                    new HashMap<String, String>());

            //Creates a CognitoUser object to be used as a starting point for authentication
            cognitoUser = cognitoUserPool.getUser(userEmail);

            Runnable authRunnable = cognitoUser.initiateUserAuthentication(authDetails, authHandler, true);
            authRunnable.run();

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Make the login alert dialog show
            alertDialog = createLoginDialog();
            alertDialog.show();
        }
    }

    /**
     * Inflates the specified layout
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return The created view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    /**
     * Initializes the main components of the fragment
     *
     * @param savedInstanceState The saved instance of the fragment
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize variables
        isSigningIn = false;

        //Initialize all of the views
        Button normalLoginButton = getActivity().findViewById(R.id.button_normal_login);
        ImageButton facebookLoginButton = getActivity().findViewById(R.id.facebook_login_button);
        SignInButton googleLoginButton = getActivity().findViewById(R.id.google_login_button);
        emailEditText = getActivity().findViewById(R.id.email_edit_text);
        passwordEditText = getActivity().findViewById(R.id.password_edit_text);
        TextView createAccountText = getActivity().findViewById(R.id.create_account_text);
        TextView forgotPasswordText = getActivity().findViewById(R.id.forgot_password_text);

        //Set listeners for buttons
        normalLoginButton.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        createAccountText.setOnClickListener(this);
        forgotPasswordText.setOnClickListener(this);

        //Initialize Google Sign In
        googleSignInClient = AuthHelper.getGoogleSignInClient(getActivity());

        //Initialize Facebook Sign In
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.d(TAG, "FacebookCallback-> onSuccess: successfully logged in with " + accessToken.getUserId());
                AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.FACEBOOK_PROVIDER);
                startMainActivity();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "FacebookCallback-> onCancel: user has canceled facebook log in flow");
            }

            @Override
            public void onError(FacebookException error) {
                Log.w(TAG, "FacebookCallback-> onError: " + error.toString());
                alertDialog = createErrorDialog("Facebook log in error");
                alertDialog.show();
            }
        });

    }

    /**
     * The fragment has received a result back from one of its calls
     *
     * @param requestCode The request code used for the request
     * @param resultCode  The result code received from the request
     * @param data        Additional data received from the result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Fragment is executing its login flow
        if (isSigningIn) {
            if (requestCode == RC_SIGN_IN) {
                //Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(..);
                Log.d(TAG, "onActivityResult: result returned from google sign in");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleGoogleSignInResult(task);
            } else if (FacebookSdk.isFacebookRequestCode(requestCode)) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Gets called upon the user clicking an interactive item on screen
     *
     * @param view The current view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_account_text: {
                Log.d(TAG, "onClick: create account text clicked.");
                onChangeFragmentListener.buttonClicked(AuthenticationContainer.AuthFragmentType.CREATE_ACCOUNT);
            }
            break;
            case R.id.forgot_password_text: {
                Log.d(TAG, "onClick: forgot password text clicked.");
                onChangeFragmentListener.buttonClicked(AuthenticationContainer.AuthFragmentType.FORGOT_PASSWORD);
            }
            break;
            case R.id.google_login_button: {
                Log.d(TAG, "onClick: google login button clicked.");
                isSigningIn = true;
                Intent googleSignInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(googleSignInIntent, 9001);
            }
            break;
            case R.id.facebook_login_button: {
                Log.d(TAG, "onClick: facebook login button clicked.");
                isSigningIn = true;
                LoginManager.getInstance().logInWithReadPermissions(getActivity(),
                        Arrays.asList("public_profile", "email"));
            }
            break;
            case R.id.button_normal_login: {
                Log.d(TAG, "onClick: normal login button clicked.");
                isSigningIn = true;
                startNormalLogin();
            }
            break;
        }
    }

    /**
     * Sets the OnChangeFragmentListener to communicate from this fragment to the activity
     *
     * @param onChangeFragmentListener The listener for communication
     */
    public void setOnChangeFragmentListener(OnChangeFragmentListener onChangeFragmentListener) {
        this.onChangeFragmentListener = onChangeFragmentListener;
    }

    /**
     * Method that allows the activity to check whether the fragment is going through a
     * sign in flow
     *
     * @return True if sign in flow is active and false otherwise
     */
    public boolean isSigningIn() {
        return isSigningIn;
    }

    /**
     * Handles google sign in event after a result has been received through onActivityResult()
     *
     * @param completedTask The task object created from a received intent's data
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Signed in with " + account.getId());
            AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.GOOGLE_PROVIDER);
            startMainActivity();
        } catch (ApiException exception) {
            Log.w(TAG, "handleSignInResult: failed code=" + exception.getStatusCode());
            alertDialog = createErrorDialog("Google sign in error");
            alertDialog.show();
        }
    }

    /**
     * Creates an error dialog to show the specific error reason to the user
     *
     * @param errorMessage The error message to show the user
     * @return an AlertDialog object to hold as reference
     */
    private AlertDialog createErrorDialog(String errorMessage) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Error");
        alertDialogBuilder.setMessage(errorMessage);
        alertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return alertDialogBuilder.create();
    }

    /**
     * Creates a login dialog displaying that the application is currently logging the user in
     *
     * @return The created login alert dialog object
     */
    private AlertDialog createLoginDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("PetSit SignIn");
        //Animate this later on
        alertDialogBuilder.setMessage("Logging User In...");
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }

    /**
     * Begins the normal login flow
     */
    private void startNormalLogin() {
        String userEmail = emailEditText.getText().toString().toLowerCase();
        String userPassword = passwordEditText.getText().toString();
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            alertDialog = createErrorDialog("Email and password fields cannot be empty");
            return;
        }
        CognitoAuthHelper cognitoHelper = new CognitoAuthHelper();
        cognitoHelper.execute(userEmail, userPassword);
    }

    /**
     * Starts the main activity
     */
    private void startMainActivity() {
        //Dismiss the alert dialog if it is currently showing
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        Intent startMainActivityIntent = new Intent(getActivity(), MainActivityContainer.class);
        startActivity(startMainActivityIntent);
        getActivity().finish();
    }

    /**
     * Sets the value to this cognito user pool
     *
     * @param cognitoUserPool The initialized cognito user pool to work with this activity
     */
    public void setCognitoUserPool(CognitoUserPool cognitoUserPool) {
        this.cognitoUserPool = cognitoUserPool;
    }


}
