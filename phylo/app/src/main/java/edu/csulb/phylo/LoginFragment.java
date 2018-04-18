package edu.csulb.phylo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.csulb.phylo.Astral.Astral;
import edu.csulb.phylo.Astral.AstralHttpInterface;
import edu.csulb.phylo.Astral.AstralUser;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;

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
    //======================================= Listener Variables ===================================

    //Cognito Authentication Handler
    AuthenticationHandler authHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            Log.d(TAG, "AuthenticationHandler->onSuccess: Logged in with :" + cognitoUser.getUserId());

            //Retrieve user information and store it inside of SharedPreferences
            cognitoUser.getDetailsInBackground(new GetDetailsHandler() {
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    Log.d(TAG, "getDetailsInBackground-> onSuccess: Retrieving Cognito information");

                    //Quit the Looper operation
                    Looper.myLooper().quitSafely();

                    //Add that the current sign in provider is Cognito
                    AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.COGNITO_PROVIDER);

                    //Retrieve user information
                    CognitoUserAttributes cognitoUserAttributes = cognitoUserDetails.getAttributes();
                    Map<String, String> userInfo = cognitoUserAttributes.getAttributes();
                    String name = userInfo.get("name");
                    String email = userInfo.get("email");

                    //Store the user details inside of Shared Preferences
                    AuthHelper.cacheUserInformation(getActivity(), name, email);

                    //Checks if the user exists in the Astral Database
                    checkIfUserExists(getActivity(), name, email);
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

    //Facebook authentication handler
    FacebookCallback<LoginResult> facebookAuthHandler = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            Log.d(TAG, "FacebookCallback-> onSuccess: successfully logged in");

            //Set the current sign in provider to Facebook
            AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.FACEBOOK_PROVIDER);

            //Retrieve the user's information
            AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
            Log.d(TAG, "retrieveFacebookInformation: userID = " + facebookAccessToken.getUserId());
            GraphRequest request = GraphRequest.newMeRequest(
                    facebookAccessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                Log.d(TAG, "retrieveFacebookInformation: onCompleted: Finished retrieving Facebook information");

                                //Store the user's information in storage
                                String name = object.getString("name");
                                String email = object.getString("email");
                                AuthHelper.cacheUserInformation(getActivity(), name, email);

                                //Start the main activity
                                startMainActivity();
                            } catch (JSONException exception) {
                                Log.d(TAG, "retrieveFacebookInformation: failure, response code: " + response.getError());
                                exception.printStackTrace();
                            } catch (Exception exception) {
                                Log.d(TAG, "retrieveFacebookInformation: failure, response code: " + response.getError());
                            }
                        }
                    }
            );
            Bundle neededInformation = new Bundle();
            neededInformation.putString("fields", "name, email");
            request.setParameters(neededInformation);
            request.executeAsync();
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
    };

    //======================================= Inner Classes =======================================

    /**
     * Used to communicate with the Cognito AstralUser Pool login for normal logins
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

            Looper.prepare();
            Looper.loop();

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

    //======================================== Fragment Code =======================================

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
        cognitoUserPool = AuthHelper.getCognitoUserPool(getActivity());

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
        LoginManager.getInstance().registerCallback(callbackManager, facebookAuthHandler);

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

    //==================================== Sign In =================================================

    /**
     * Handles google sign in event after a result has been received through onActivityResult()
     *
     * @param completedTask The task object created from a received intent's data
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Successfully signed in with Google");

            //Set the current sign in provider
            AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.GOOGLE_PROVIDER);

            //Store the user's account information in cache
            AuthHelper.cacheUserInformation(getActivity(), account.getDisplayName(), account.getEmail());

            startMainActivity();
        } catch (ApiException exception) {
            Log.w(TAG, "handleSignInResult: failed code=" + exception.getStatusCode());
            alertDialog = createErrorDialog("Google sign in error");
            alertDialog.show();
        }
    }

    /**
     * Begins the normal login flow
     */
    private void startNormalLogin() {
        String userEmail = emailEditText.getText().toString().toLowerCase();
        String userPassword = passwordEditText.getText().toString();
        Log.d(TAG, "startNormalLogin: email = " + userEmail);
        Log.d(TAG, "startNormalLogin: password = " + userPassword);
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            alertDialog = createErrorDialog("Email and password fields cannot be empty");
            alertDialog.show();
            return;
        }
        CognitoAuthHelper cognitoHelper = new CognitoAuthHelper();
        cognitoHelper.execute(userEmail, userPassword);
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

    //======================================== Astral ==============================================

    /**
     * Retrieves the user's username from the server
     *
     * @param context The application's current context
     */
    private void checkIfUserExists(final Context context, final String name, final String email) {
        //First check if the user exists on the Server with a GET request
        final Astral astral = new Astral(getString(R.string.astral_base_url));

        //Intercept the request to add header items
        astral.addRequestInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                //Add the app key to the request header
                Request.Builder newRequest = request.newBuilder().header(
                        Astral.APP_KEY_HEADER, getString(R.string.astral_key))
                        .header(Astral.ASTRAL_EMAIL, email);
                //Continue the request
                return chain.proceed(newRequest.build());
            }
        });
        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();

        //Create the GET Request
        Call<AstralUser> request = astralHttpInterface.getUserToken();

        //Call the request asynchronously
        request.enqueue(new Callback<AstralUser>() {
            @Override
            public void onResponse(Call<AstralUser> call, retrofit2.Response<AstralUser> response) {
                Log.d(TAG, "retrieveAstralUsername-> onResponse: Code = " + response.code());

                if (response.isSuccessful()) {
                    if (response.code() == Astral.OK) {
                        //User exists, retrieve the user's username
                        String token = response.body().getToken();
                        //Store the token
                        Astral.storeAstralUserToken(context, token);
                        //Retrieve the user's username using the token
                        retrieveAstralUsername(token);
                    }
                } else {
                    //Check the error code
                    if (response.code() == Astral.NOT_FOUND) {
                        //Dismiss the currently showing Alert Dialog
                        alertDialog.dismiss();

                        //User does not exist in the database, begin method of creating Astral User
                        alertDialog = createUsernameDialog(email);
                        alertDialog.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AstralUser> call, Throwable t) {
                Log.w(TAG, "checkIfUserExists-> onFailure");
            }
        });
    }

    private void retrieveAstralUsername(final String userToken) {
        final Astral astral = new Astral(getString(R.string.astral_base_url));
        //Intercept the request to add header items
        astral.addRequestInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                //Add the app key to the request header
                Request.Builder newRequest = request.newBuilder().header(
                        Astral.APP_KEY_HEADER, getString(R.string.astral_key))
                        .header("token", userToken);

                //Continue the request
                return chain.proceed(newRequest.build());
            }
        });
        //Add a logging interceptor to log the request and response
        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();

        //Create the GET request
        Call<AstralUser> request = astralHttpInterface.getUserInformation();

        //Call the request asynchronously
        request.enqueue(new Callback<AstralUser>() {
            @Override
            public void onResponse(Call<AstralUser> call, retrofit2.Response<AstralUser> response) {
                if (response.code() == Astral.OK) {
                    String username = response.body().getUsername();
                    Log.d(TAG, "retrieveAstralUsername-> onResponse: signed in with " + username);
                    //Store the received Username
                    Astral.storeAstralUsername(getActivity(), username);

                    //Make the login dialog disappear
                    alertDialog.dismiss();

                    //Begin the Main Activity
                    startMainActivity();
                } else {
                    //Check other possible responses
                    switch (response.code()) {

                    }
                }
            }

            @Override
            public void onFailure(Call<AstralUser> call, Throwable t) {
                Log.w(TAG, "checkIfUserExists-> onFailure");
            }
        });
    }

    /**
     * Creates an Astral User from the federated identities
     *
     * @param email The user's email
     * @param username The user's chosen username
     */
    private void createAstralUser(final String email, final String username) {
        //Create an AstralUser object to send
        AstralUser astralUser = new AstralUser(username, email, null);

        //Start at POST request to create the user in the Astral Framework
        final Astral astral = new Astral(getString(R.string.astral_base_url));
        //Intercept the request to add a header item
        astral.addRequestInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                //Add the app key to the request header
                Request.Builder newRequest = request.newBuilder().header(
                        Astral.APP_KEY_HEADER, getString(R.string.astral_key));
                //Continue the request
                return chain.proceed(newRequest.build());
            }
        });
        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();
        //Create the POST request
        Call<AstralUser> request = astralHttpInterface.createUser(astralUser);
        //Call the request asynchronously
        request.enqueue(new Callback<AstralUser>() {
            @Override
            public void onResponse(Call<AstralUser> call, retrofit2.Response<AstralUser> response) {
                if(response.code() == Astral.OK) {
                    Log.d(TAG, "onClick-> onSuccess-> onResponse: Successful Response Code " + response.code());

                    //Retrieve the token received
                    String userToken = response.body().getToken();

                    //Store the User's token item
                    Astral.storeAstralUserToken(getActivity(), userToken);

                    //Create an Astral AstralUser account
                    startMainActivity();
                } else {
                    Log.d(TAG, "onClick-> onSuccess-> onResponse: Failed response Code " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AstralUser> call, Throwable t) {
                //The request has unexpectedly failed
                Log.d(TAG, "onCLick-> onSuccess-> onResponse: Unexpected request failure");
            }
        });
    }

    //========================================= Listeners ==========================================

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

    //====================================== ETC ===================================================

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
        alertDialogBuilder.setMessage("Logging AstralUser In...");
        alertDialogBuilder.setCancelable(false);
        return alertDialogBuilder.create();
    }

    /**
     * Create an AlertDialog object to allow the user to create a username
     *
     * @param email The user's email
     *
     * @return The AlertDialog object for the user to create their username
     */
    private AlertDialog createUsernameDialog(final String email) {
        //Create an instance of the Alert Dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        //User cannot cancel
        alertDialogBuilder.setCancelable(false);
        final View alertDialogView = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_create_username, null);

        //Initialize Custom Alert Dialog Views
        Button createUsername = (Button) alertDialogView.findViewById(R.id.button_create_username_adcu);
        final EditText usernameEditText = (EditText) alertDialogView.findViewById(R.id.edit_text_create_username);

        //Attach listeners
        createUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Make the X Mark disappear from the input field
                final ImageView xMarkImage = getActivity().findViewById(R.id.x_mark_username_availability_adcu);
                xMarkImage.setVisibility(View.GONE);

                //Retrieve the user's username
                final String username = usernameEditText.getText().toString();

                //Start a GET request to check if the username is available
                final Astral astral = new Astral(getString(R.string.astral_base_url));
                //Intercept the request to add a header item
                astral.addRequestInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        //Add the app key to the request header
                        Request.Builder newRequest = request.newBuilder().header(
                                Astral.APP_KEY_HEADER, getString(R.string.astral_key));
                        //Continue the request
                        return chain.proceed(newRequest.build());
                    }
                });
                astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
                AstralHttpInterface astralHttpInterface = astral.getHttpInterface();

                //Create the GET Request
                Call<ResponseBody> request = astralHttpInterface.checkUsernameAvailability(username);

                //Call the request asynchronously
                request.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                        if (response.isSuccessful()) {
                            Log.d(TAG, "onFocusChange-> onResponse: Successful Response Code " + response.code());
                            if (response.code() == Astral.OK) {
                                //The username is not available
                                displayToast("Username not available");
                                //Show an x_mark on the username
                                xMarkImage.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "onFocusChange-> onResponse: Failure Response Code " + response.code());
                            if (response.code() == Astral.NOT_FOUND) {
                                //We can use the username, it was not found
                                displayToast("Username available!");

                                //Now we have to Create a User
                                createAstralUser(email, username);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.w(TAG, "onFocusChange-> onFailure");
                    }
                });
            }
        });

        alertDialogBuilder.setView(alertDialogView);

        return alertDialogBuilder.create();
    }

    /**
     * Displays a message to the user
     *
     * @param message The message to be displayed to the user
     */
    private void displayToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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

}
