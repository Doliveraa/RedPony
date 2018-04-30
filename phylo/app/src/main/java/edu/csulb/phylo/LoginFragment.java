package edu.csulb.phylo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import java.util.HashSet;
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

//TODO: Do a get request when they log in with google or facebook

public class LoginFragment extends Fragment
        implements View.OnClickListener {
    //Phone Hardware
    private Vibrator vibrator;
    //Constants
    private final static int RC_SIGN_IN = 9001;
    private final static String TAG = LoginFragment.class.getSimpleName();
    //Login Variables
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private CognitoUserPool cognitoUserPool;
    private CognitoUser cognitoUser;
    private boolean isSigningIn;
    private HashSet<String> previouslySuccessfulUsernames;
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

                    //Retrieve user information
                    CognitoUserAttributes cognitoUserAttributes = cognitoUserDetails.getAttributes();
                    Map<String, String> userInfo = cognitoUserAttributes.getAttributes();
                    String name = userInfo.get("name");
                    String email = userInfo.get("email");

                    //Checks if the user exists in the Astral Database
                    checkIfUserExists(getActivity(), name, email, AuthHelper.COGNITO_PROVIDER);
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

            //Show the Login Dialog
            alertDialog = createLoginDialog();
            alertDialog.show();
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

                                //Check if the User exists in the Astral Database and force them
                                //to create a Username if they don't exist
                                checkIfUserExists(getActivity(), name, email, AuthHelper.FACEBOOK_PROVIDER);

                            } catch (JSONException exception) {
                                Log.d(TAG, "retrieveFacebookInformation: JSON failure, response code: " + response.getError());
                                exception.printStackTrace();
                            } catch (Exception exception) {
                                exception.printStackTrace();
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
        previouslySuccessfulUsernames = new HashSet<String>();

        //Initialize Hardware
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

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

        emailEditText.setText("vietle8362@gmail.com");
        passwordEditText.setText("Trxcjo19");

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

            //Display the Login dialog
            alertDialog = createLoginDialog();
            alertDialog.show();

            //Check if the user exists in the Astral database
            checkIfUserExists(getActivity(), account.getDisplayName(), account.getEmail(), AuthHelper.GOOGLE_PROVIDER);

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
    private void checkIfUserExists(final Context context, final String name, final String email, final String signInProvider) {
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

        Log.d(TAG, "Starting request to check user existence");
        //Call the request asynchronously
        request.enqueue(new Callback<AstralUser>() {
            @Override
            public void onResponse(Call<AstralUser> call, retrofit2.Response<AstralUser> response) {
                Log.d(TAG, "retrieveAstralUsername-> onResponse: Code = " + response.code());

                if (response.isSuccessful()) {
                    if (response.code() == Astral.OK) {
                        //User exists, retrieve the user's token
                        String token = response.body().getToken();
                        //Store the token
                        Astral.storeAstralUserToken(context, token);
                        //Retrieve the user's username using the token
                        retrieveAstralUsername(token, signInProvider, name, email);
                    }
                } else {
                    //Check the error code
                    if (response.code() == Astral.UNAUTHORIZED) {
                        //Dismiss the current Login dialog
                        alertDialog.dismiss();
                        //User does not exist in the database, begin method of creating Astral User
                        alertDialog = createUsernameDialog(name, email, signInProvider);
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

    private void retrieveAstralUsername(final String userToken, final String signInProvider,
                                        final String name, final String email) {
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

                    //Store the user's account information in cache
                    AuthHelper.cacheUserInformation(getActivity(), name, email);

                    //Add the current sign in provider
                    AuthHelper.setCurrentSignInProvider(getActivity(), signInProvider);
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
     * @param email    The user's email
     * @param username The user's chosen username
     */
    private void createAstralUser(final String name, final String email, final String username, final String signInProvider) {
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
        Call<AstralUser> request = astralHttpInterface.createUser(astralUser.getUsername(),
                astralUser.getEmail(), null);
        //Call the request asynchronously
        request.enqueue(new Callback<AstralUser>() {
            @Override
            public void onResponse(Call<AstralUser> call, retrofit2.Response<AstralUser> response) {
                if (response.code() == Astral.OK) {
                    Log.d(TAG, "createAstralUser-> onClick-> onSuccess-> onResponse: Successful Response Code " + response.code());

                    //Retrieve the token received
                    String userToken = response.body().getToken();

                    //Store the sign in provider
                    AuthHelper.setCurrentSignInProvider(getActivity(), signInProvider);
                    //Store the User's token item
                    Astral.storeAstralUserToken(getActivity(), userToken);
                    //Store the user details inside of Shared Preferences
                    AuthHelper.cacheUserInformation(getActivity(), name, email);

                    //Create an Astral AstralUser account
                    startMainActivity();
                } else {
                    Log.d(TAG, "createAstralUser-> onClick-> onSuccess-> onResponse: Failed response Code " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AstralUser> call, Throwable t) {
                //The request has unexpectedly failed
                Log.d(TAG, "createAstralUser-> onClick-> onSuccess-> onResponse: Unexpected request failure");
                t.printStackTrace();
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
     * @return The AlertDialog object for the user to create their username
     */
    private AlertDialog createUsernameDialog(final String name, final String email, final String signInProvider) {
        //Create an instance of the Alert Dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        //User cannot cancel
        alertDialogBuilder.setCancelable(false);
        final View alertDialogView = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_create_username, null);

        //Initialize Custom Alert Dialog Views
        final Button createUsername = (Button) alertDialogView.findViewById(R.id.button_create_username_adcu);
        final EditText usernameEditText = (EditText) alertDialogView.findViewById(R.id.edit_text_create_username);
        final Button checkUsernameButton = (Button) alertDialogView.findViewById(R.id.button_check_username_adcu);
        final ImageView xMarkImage = (ImageView) alertDialogView.findViewById(R.id.x_mark_username_availability_adcu);
        final ImageView checkMarkImage = (ImageView) alertDialogView.findViewById(R.id.checkmark_username_availability_adcu);
        final ProgressBar usernamePb = (ProgressBar) alertDialogView.findViewById(R.id.progress_bar_username_availability_adcu);

        //Attach listeners
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Reset the check-marks and x-marks
                xMarkImage.setVisibility(View.GONE);
                checkMarkImage.setVisibility(View.GONE);
                //Retrieve the user's username
                String currUsername = s.toString();
                //See if the username was previously successful
                boolean prevSuccess = previouslySuccessfulUsernames.contains(currUsername);
                if (prevSuccess) {
                    checkMarkImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        checkUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Reset the views on the username input field
                xMarkImage.setVisibility(View.GONE);
                checkMarkImage.setVisibility(View.GONE);

                //Retrieve the user's username
                final String username = usernameEditText.getText().toString();
                //Check if the username is of the right format
                if (!AuthHelper.isUsernameValid(username)) {
                    xMarkImage.setVisibility(View.VISIBLE);
                    displayToast("Username Format Invalid", true);
                    return;
                }

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
                        //Continue the requestp
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
                        usernamePb.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onFocusChange-> onResponse: Successful Response Code " + response.code());
                            if (response.code() == Astral.OK) {
                                //Show an x_mark on the username
                                xMarkImage.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "onFocusChange-> onResponse: Failure Response Code " + response.code());
                            if (response.code() == Astral.NOT_FOUND) {
                                //Add it to the list of usernames that the user has tried
                                previouslySuccessfulUsernames.add(username);
                                //Show a check mart on the username box
                                checkMarkImage.setVisibility(View.VISIBLE);
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

        createUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMarkImage.getVisibility() == View.VISIBLE) {
                    String username = usernameEditText.getText().toString();
                    createAstralUser(name, email, username, signInProvider);
                } else if(xMarkImage.getVisibility() == View.GONE && checkMarkImage.getVisibility() == View.GONE){
                    displayToast("Please check username availability", false);
                } else {
                    //Cannot use the username
                    displayToast("Cannot use username", true);
                }
            }
        });

        alertDialogBuilder.setView(alertDialogView);

        return alertDialogBuilder.create();
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
     * Displays a message as a toast
     *
     * @param message      The message to be displayed
     * @param vibratePhone If the phone should vibrate
     */
    private void displayToast(String message, boolean vibratePhone) {
        Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.show();
        if (vibratePhone) {
            vibrator.vibrate(500);
        }
    }

}
