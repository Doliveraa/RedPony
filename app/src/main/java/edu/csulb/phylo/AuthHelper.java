package edu.csulb.phylo;


import android.content.Context;
import android.content.SharedPreferences;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.exceptions.CognitoInternalErrorException;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.Hkdf;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

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
    private final static String LAST_USER_CACHE = "CognitoIdentityProviderCache";

    /**
     * User should not be able to generate this class
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
     * Chaches the user information if given a CognitoUserDetails object
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

    public static void cacheCurrentSignedInUser(Context context, String userID) {
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
}

