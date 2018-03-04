package edu.csulb.phylo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.exceptions.CognitoInternalErrorException;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoAccessToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoRefreshToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoSecretHash;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoServiceConstants;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProvider;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidentityprovider.model.AuthFlowType;
import com.amazonaws.services.cognitoidentityprovider.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidentityprovider.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidentityprovider.model.InitiateAuthResult;
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidentityprovider.model.RespondToAuthChallengeResult;
import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
    //Enumerators
    private enum AuthType{
        GOOGLE,
        FACEBOOK,
        NORMAL
    }
    //Interface
    public interface OnChangeFragmentListener {
        void buttonClicked(AuthenticationActivity.AuthFragmentType fragmentType);
    }
    private OnChangeFragmentListener onChangeFragmentListener;

    private class CognitoHelper extends AsyncTask<String, Void, Void> {
        private AuthHelper authHelper = new AuthHelper(cognitoUserPool.getUserPoolId());
        private final int SRP_RADIX = 16;
        private String usernameInternal;
        private String secretHash;
        private AlertDialog alertDialog;
        private boolean loginSuccess;

        @Override
        protected Void doInBackground(String... strings) {
            String userEmail = strings[0];
            String userPassword = strings[1];

            Log.d(TAG, "CognitoHelper: doInBackground");
            Log.d(TAG, "CognitoHelper: email: " + userEmail);
            Log.d(TAG, "CognitoHelper: password" + userPassword);

            //Creates a authentication request to start authentication with user SRP verification
            final InitiateAuthRequest initiateAuthRequest = new InitiateAuthRequest();
            initiateAuthRequest.setAuthFlow(AuthFlowType.USER_SRP_AUTH);
            initiateAuthRequest.setClientId(cognitoUserPool.getClientId());
            initiateAuthRequest.addAuthParametersEntry(CognitoServiceConstants.AUTH_PARAM_SECRET_HASH,
                    CognitoSecretHash.getSecretHash(
                            userEmail,
                            getString(R.string.application_client_id),
                            getString(R.string.application_client_secret)
                    ));
            initiateAuthRequest.addAuthParametersEntry(CognitoServiceConstants.AUTH_PARAM_SRP_A,
                    authHelper.getA().toString(SRP_RADIX));
            initiateAuthRequest.addAuthParametersEntry(CognitoServiceConstants.AUTH_PARAM_USERNAME,
                    userEmail);

            //Build Client
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            AmazonCognitoIdentityProvider cipClient = new AmazonCognitoIdentityProviderClient(
                    new AnonymousAWSCredentials(),
                    clientConfiguration
            );
            cipClient.setRegion(Region.getRegion(Regions.US_EAST_1));

            //Start the user authentication with user password verification
            final InitiateAuthResult initiateAuthResult = cipClient.initiateAuth(initiateAuthRequest);
            if (CognitoServiceConstants.CHLG_TYPE_USER_PASSWORD_VERIFIER.equals(
                    initiateAuthResult.getChallengeName())) {
                final String userIdForSRP = initiateAuthResult.getChallengeParameters()
                        .get(CognitoServiceConstants.CHLG_PARAM_USER_ID_FOR_SRP);
                usernameInternal = initiateAuthResult.getChallengeParameters()
                        .get(CognitoServiceConstants.CHLG_PARAM_USERNAME);
                secretHash = CognitoSecretHash.getSecretHash(
                        usernameInternal,
                        getString(R.string.application_client_id),
                        getString(R.string.application_client_secret)
                );
                final BigInteger srpB = new BigInteger(initiateAuthResult.getChallengeParameters()
                        .get(CognitoServiceConstants.CHLG_PARAM_SRP_B), 16);
                if (srpB.mod(authHelper.N).equals(BigInteger.ZERO)) {
                    throw new CognitoInternalErrorException("SRP error, B cannot be zero");
                }
                final BigInteger salt = new BigInteger(initiateAuthResult.getChallengeParameters()
                        .get(CognitoServiceConstants.CHLG_PARAM_SALT), 16);
                final byte[] key = authHelper.getPasswordAuthenticationKey(userIdForSRP,
                        userPassword, srpB, salt);

                final Date timestamp = new Date();
                byte[] hmac;
                String dateString;
                try {
                    final Mac mac = Mac.getInstance("HmacSHA256");
                    final SecretKey keySpec = new SecretKeySpec(key, "HmacSHA256");
                    mac.init(keySpec);
                    mac.update(cognitoUserPool.getUserPoolId().split("_", 2)[1].getBytes(StringUtils.UTF8));
                    mac.update(userIdForSRP.getBytes(StringUtils.UTF8));
                    final byte[] secretBlock = Base64.decode(initiateAuthResult.getChallengeParameters()
                            .get(CognitoServiceConstants.CHLG_PARAM_SECRET_BLOCK));
                    mac.update(secretBlock);
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                            "EEE MMM d HH:mm:ss z yyyy", Locale.US
                    );
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    dateString = simpleDateFormat.format(timestamp);
                    final byte[] dateBytes = dateString.getBytes(StringUtils.UTF8);

                    hmac = mac.doFinal(dateBytes);
                } catch (final Exception exception) {
                    throw new CognitoInternalErrorException("SRP error", exception);
                }

                try {
                    final Map<String, String> srpAuthResponses = new HashMap<>();
                    srpAuthResponses.put(CognitoServiceConstants.CHLG_RESP_PASSWORD_CLAIM_SECRET_BLOCK,
                            initiateAuthResult.getChallengeParameters().get(CognitoServiceConstants.CHLG_PARAM_SECRET_BLOCK));
                    srpAuthResponses.put(CognitoServiceConstants.CHLG_RESP_PASSWORD_CLAIM_SIGNATURE,
                            new String(Base64.encode(hmac), StringUtils.UTF8));
                    srpAuthResponses.put(CognitoServiceConstants.CHLG_RESP_TIMESTAMP, dateString);
                    srpAuthResponses.put(CognitoServiceConstants.CHLG_RESP_USERNAME, usernameInternal);
                    srpAuthResponses.put(CognitoServiceConstants.CHLG_RESP_SECRET_HASH, secretHash);

                    final RespondToAuthChallengeRequest authChallengeRequest = new RespondToAuthChallengeRequest();
                    authChallengeRequest.setChallengeName(initiateAuthResult.getChallengeName());
                    authChallengeRequest.setClientId(getString(R.string.application_client_id));
                    authChallengeRequest.setSession(initiateAuthResult.getSession());
                    authChallengeRequest.setChallengeResponses(srpAuthResponses);
                    final RespondToAuthChallengeResult challenge = cipClient.respondToAuthChallenge(authChallengeRequest);

                    AuthenticationResultType authenticationResultType = challenge.getAuthenticationResult();
                    CognitoIdToken cognitoIdToken = new CognitoIdToken(authenticationResultType.getIdToken());
                    CognitoAccessToken cognitoAccessToken = new CognitoAccessToken(authenticationResultType.getAccessToken());
                    CognitoRefreshToken cognitoRefreshToken = new CognitoRefreshToken(authenticationResultType.getRefreshToken());
                    CognitoUserSession cognitoUserSession = new CognitoUserSession(cognitoIdToken, cognitoAccessToken, cognitoRefreshToken);

                    String idToken = cognitoUserSession.getIdToken().getJWTToken();

                    startMainActivity();
                    Log.d(TAG, "onPostExecute : Login Successful");
                } catch (NotAuthorizedException notAuthorizedException) {
                    Log.w(TAG, "Wrong password or username");
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute");
            alertDialog.dismiss();

            if (!loginSuccess) {
                Log.d(TAG, "onPostExecute: Login Failure");
                AlertDialog errorDialog = createErrorDialog("Email and Password combination not found");
                errorDialog.show();
            } else {
                Log.d(TAG, "onPostExecute: Login Success");

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loginSuccess = false;

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
            Log.d(TAG, "Signed in with " + account.getId());
            startMainActivity();
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
     * Starts the main activity
     */
    private void startMainActivity() {
        //Dismiss the alert dialog if it is currently showing
        if(alertDialog.isShowing()) {
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
