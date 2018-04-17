package edu.csulb.phylo;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

import java.util.Map;

import edu.csulb.phylo.Astral.Astral;

/**
 * Created by Daniel on 1/15/2018.
 */

public class AuthHelper {

    //Authentication Constants
    public final static String USER_INFO = "user_info";
    public final static String USER_EMAIL = "cognito email";
    public final static String USER_NAME = "users_name";
    public final static String USER_SIGN_IN_PROVIDER = "sign_in_provider";
    public final static String GOOGLE_PROVIDER  = "google";
    public final static String FACEBOOK_PROVIDER = "facebook";
    public final static String COGNITO_PROVIDER = "cognito";
    public final static String CONTAINS_INFO = "contains_info";
    //Private Authentication Constants
    private final static String TAG = AuthHelper.class.getSimpleName();
    private final static String LAST_USER_CACHE = "CognitoIdentityProviderCache";

    /**
     * AstralUser should not be able to generate this class
     */
    private AuthHelper() {
        throw new RuntimeException("AuthHelper should not be generated");
    }

    public static CognitoUserPool getCognitoUserPool(Context context) {
        CognitoUserPool cognitoUserPool = new CognitoUserPool(
                context,
                context.getResources().getString(R.string.cognito_pool_id),
                context.getResources().getString(R.string.application_client_id),
                context.getResources().getString(R.string.application_client_secret),
                Regions.US_WEST_2
        );

        return cognitoUserPool;
    }


    /**
     * A more general method that caches the user information based on the values that you give
     *
     * @param context The activity in which this activity was called
     * @param name The name of the user
     * @param email The email of the user
     */
    public static void cacheUserInformation(final Context context, final String name,final String email) {
        //Initialize the SharedPreferences object and choose the folder
        SharedPreferences sharedPreferences = getAuthPreferences(context);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        //Store the information
        spEditor.putString(AuthHelper.USER_EMAIL, email);
        spEditor.putString(AuthHelper.USER_NAME, name);
        spEditor.putBoolean(AuthHelper.CONTAINS_INFO, true);
        //Commit the changes
        spEditor.apply();
    }

    /**
     * Caches the currently signed in Cognito user in shared preferences
     *
     * @param context The activity that has called this method
     *
     * @param userID The id of the user
     */
    public static void cacheCurrentCognitoSignedInUser(Context context, String userID) {
        //Open shared preferences folder where the last signed in user is stored
        final SharedPreferences csiCachedTokens = context
                .getSharedPreferences(LAST_USER_CACHE, Context.MODE_PRIVATE);
        //Set the client id variable
        final String clientId = context.getString(R.string.application_client_id);
        //Create the key to input the last signed in user
        final String csiLastUserKey = "CognitoIdentityProvider." + clientId + ".LastAuthUser";

        SharedPreferences.Editor editor = csiCachedTokens.edit();
        editor.putString(csiLastUserKey, userID);
        editor.apply();
    }

    /**
     * Set the current sign in provider in the shared preferences folder
     *
     * @param context The activity in which this method was called
     * @param provider The sign in provider used for the user authentication
     */
    public static void setCurrentSignInProvider(final Context context, final String provider){
        SharedPreferences sharedPreferences = getAuthPreferences(context);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putString(USER_SIGN_IN_PROVIDER, provider);
        spEditor.commit();
    }


    /**
     * Retrieves the current user's sign in provider
     *
     * @param context The application's current context
     *
     * @return The current sign in provider
     */
    public static String getCurrentSignInProvider(final Context context) {
        SharedPreferences sharedPreferences = getAuthPreferences(context);
        String signInProvider = sharedPreferences.getString(USER_SIGN_IN_PROVIDER, null);
        return signInProvider;
    }

    /**
     * Retrieves the current user's name
     *
     * @param context The application's current context
     *
     * @return The current signed in User's name
     */
    public static String getCachedUserName(final Context context) {
        SharedPreferences sharedPreferences = getAuthPreferences(context);
        return sharedPreferences.getString(USER_NAME, null);
    }

    /**
     * Retrieves the current user's email
     *
     * @param context The application's current context
     *
     * @return The current signed in User's email
     */
    public static String getCachedUserEmail(final Context context) {
        SharedPreferences sharedPreferences = getAuthPreferences(context);
        return sharedPreferences.getString(USER_EMAIL, null);
    }

    /**
     * Checks to see if the Authentication preferences folder contains the user's information
     *
     * @param context The application's current context
     *
     * @return True if it contains the user's information and false otherwise
     */
    public static boolean containsUserInfo(final Context context) {
        SharedPreferences sharedPreferences = getAuthPreferences(context);
        return sharedPreferences.getBoolean(CONTAINS_INFO, false);
    }

    /**
     * Signs out the current user
     *
     * @param user The currently signed in user
     */
    public static void signOutUser(Context context, User user) {

        String resultMessage;
        switch(user.getSignInProvider()) {
            case GOOGLE_PROVIDER: {
                Log.d(TAG, "Attempting to sign out from Google");
                GoogleSignInClient googleSignInClient = getGoogleSignInClient(context);
                Task task = googleSignInClient.signOut();
                resultMessage = "Successful Signout";
            }
            break;
            case FACEBOOK_PROVIDER: {
                Log.d(TAG, "Attempting to sign out from Facebook");
                LoginManager.getInstance().logOut();
                resultMessage = "Successful Facebook Sign out";
            }
            break;
            case COGNITO_PROVIDER: {
                Log.d(TAG, "Attempting to sign out from Cognito");
                CognitoUserPool cognitoUserPool = getCognitoUserPool(context);
                CognitoUser cognitoUser = cognitoUserPool.getCurrentUser();
                cognitoUser.signOut();
                resultMessage = "Successful Cognito Sign out";
            }
            break;
            default:
                resultMessage = "Error, Did not enter case statements";
        }
        Log.d(TAG, "signOutUser: AstralUser Sign out Result: " + resultMessage);
    }

    public static GoogleSignInClient getGoogleSignInClient(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(context.getString(R.string.google_web_client_id))
                .requestEmail().requestProfile().build();

        return GoogleSignIn.getClient(context, gso);
    }

    /**
     * Retrieves the shared preferences folder storing all the user information
     *
     * @param context The current context of the application
     *
     * @return A shared preferences object
     */
    private static SharedPreferences getAuthPreferences(final Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_INFO, context.MODE_PRIVATE);
        return sharedPreferences;
    }
}

