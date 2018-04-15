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
    public final static String COGNITO_INFO = "cognito info";
    public final static String COGNITO_EMAIL = "cognito email";
    public final static String COGNITO_USER_NAME = "cognito user name";
    public final static String GOOGLE_PROVIDER  = "google";
    public final static String FACEBOOK_PROVIDER = "facebook";
    public final static String COGNITO_PROVIDER = "cognito";
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
     * Caches the user information if given a CognitoUserDetails object
     *
     * @param context The activity in which this method was called
     *
     * @param cognitoUserDetails The object that encapsulates the user's information
     */
    public static void cacheUserInformation(Context context, CognitoUserDetails cognitoUserDetails) {
        CognitoUserAttributes cognitoUserAttributes = cognitoUserDetails.getAttributes();
        Map<String, String> userInfo = cognitoUserAttributes.getAttributes();
        //Store the user details inside of Shared Preferences
        cacheUserInformation(context, userInfo.get("name"), userInfo.get("email"));
    }

    /**
     * A more general method that caches the user information based on the values that you give
     *
     * @param context The activity in which this activity was called
     * @param name The name of the user
     * @param email The email of the user
     */
    public static void cacheUserInformation(Context context, String name, String email) {
        //Initialize the SharedPreferences object and choose the folder
        SharedPreferences sharedPreferences = context.getSharedPreferences(AuthHelper.COGNITO_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        //Store the information
        spEditor.putString(AuthHelper.COGNITO_EMAIL, email);
        spEditor.putString(AuthHelper.COGNITO_USER_NAME, name);
        //Commit the changes
        spEditor.commit();
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
    public static void setCurrentSignInProvider(Context context, String provider){
        SharedPreferences sharedPreferences = context.getSharedPreferences(User.USER_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putString(User.USER_SIGN_IN_PROVIDER, provider);
        spEditor.commit();
    }

    /**
     * Store the user's username inside of the phone for accessibility
     *
     * @param context The activity where this method is being called from
     * @param username The User's username
     */
    public static void storeUsername(Context context, String username) {
        //Create or open folder holding that hold's the user's Astral information
        SharedPreferences sharedPreferences = context.getSharedPreferences(Astral.ASTRAL_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //Add the User's username to the folder
        editor.putString(Astral.ASTRAL_USERNAME, username);
        editor.apply();
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
}

