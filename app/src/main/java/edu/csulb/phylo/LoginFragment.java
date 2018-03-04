package edu.csulb.phylo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

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
    private boolean isSigningIn;
    //Views
    private Button debuggerButton;
    private Button normalLoginButton;
    private SignInButton googleLoginButton;
    private ImageButton facebookLoginButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView createAccountText;
    private TextView forgotPasswordText;
    //Other Variables
    private AlertDialog alertDialog;

    //Interface
    public interface OnChangeFragmentListener {
        void buttonClicked(AuthenticationActivity.AuthFragmentType fragmentType);
    }

    private OnChangeFragmentListener onChangeFragmentListener;


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
        debuggerButton = getActivity().findViewById(R.id.button_debugger);
        normalLoginButton = getActivity().findViewById(R.id.button_normal_login);
        facebookLoginButton = getActivity().findViewById(R.id.facebook_login_button);
        googleLoginButton = getActivity().findViewById(R.id.google_login_button);
        emailEditText = getActivity().findViewById(R.id.email_edit_text);
        passwordEditText = getActivity().findViewById(R.id.password_edit_text);
        createAccountText = getActivity().findViewById(R.id.create_account_text);
        forgotPasswordText = getActivity().findViewById(R.id.forgot_password_text);

        //Set listeners for buttons
        normalLoginButton.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        createAccountText.setOnClickListener(this);
        forgotPasswordText.setOnClickListener(this);
        debuggerButton.setOnClickListener(this);

        //Initialize Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail().requestProfile().build();
        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        //Initialize Facebook Sign In
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.d(TAG, "FacebookCallback-> onSuccess: successfully logged in with " + accessToken.getUserId());
                handleLoginToken(accessToken.getToken());
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
     * @param resultCode The result code received from the request
     * @param data Additional data received from the result
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
            } else if(FacebookSdk.isFacebookRequestCode(requestCode)) {
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
                onChangeFragmentListener.buttonClicked(AuthenticationActivity.AuthFragmentType.CREATE_ACCOUNT);
            }
            break;
            case R.id.forgot_password_text: {
                Log.d(TAG, "onClick: forgot password text clicked.");
                onChangeFragmentListener.buttonClicked(AuthenticationActivity.AuthFragmentType.FORGOT_PASSWORD);
            }
            break;
            case R.id.button_debugger: {
                Log.d(TAG, "onClick: debugger button clicked.");
                onChangeFragmentListener.buttonClicked(AuthenticationActivity.AuthFragmentType.MOVE_TO_ACTIVITY);
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
            Log.d(TAG, "Signed in with " + account.getEmail());
            handleLoginToken(account.getIdToken());
            //Send the user to the main activity
            Intent intent = new Intent(getActivity(), MainActivityContainer.class);
            startActivity(intent);
            getActivity().finish();
        }catch (ApiException exception) {
            Log.w(TAG, "handleSignInResult: failed code=" + exception.getStatusCode());
            alertDialog = createErrorDialog("Google sign in error");
            alertDialog.show();
        }
    }

    /**
     * Creates an error dialog to show the specific error reason to the user
     *
     * @param errorMessage The error message to show the user
     *
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
     * Handles the access token after the user has logged in
     *
     * @param token The token received back from the log in provider
     */
    private void handleLoginToken(String token) {

    }

}
