package edu.csulb.phylo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Danie on 3/19/2018.
 */

//TODO: Create a way to update the AstralUser sign in provider

public class User implements Serializable {

    //Forces userInstance to be thread safe, all the write will happen on this instance
    //before being read from
    private static volatile User userInstace;
    //AstralUser Variables
    private static String signInProvider;
    private static String email;
    private static String name;
    private static String username;
    //Astral Variables
    private static String userAstralTokens;
    //Constants
    private final static String TAG = User.class.getSimpleName();
    //Public Constants
    public final static String USER_PREFERENCES = "user";
    public final static String USER_SIGN_IN_PROVIDER = "provider";


    /**
     * Does not allow the developer to create a AstralUser object through the constructor
     */
    private User() {
        //Prevent instantiation from the reflection API
        if(userInstace != null) {
            throw new RuntimeException("Must use getInstance() to generate this object");
        }
    }

    /**
     * Automatically looks into the shared preferences and builds a AstralUser object
     * based on the current sign in provider
     *
     * @return
     */
    public static User getInstance(Context context) {
        //Create a new user object if there is no current AstralUser object
        if(userInstace == null) {
            synchronized (User.class) {
                if(userInstace == null) {
                    userInstace = new User();
                    retrieveUserInformation(context);
                }
            }
        }
        return userInstace;
    }

    /**
     * The user's name retrieved from Sign In Providers
     *
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * The user's email retrieved from Sign In Providers
     *
     * @return The user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * The user's username retrieved from Astral
     *
     * @return The user's username
     */
    public String getUsername() { return username; }

    /**
     * The user's sign in provider
     *
     * @return The sign in provider that the user is currently signed in with
     */
    public String getSignInProvider() {
        return signInProvider;
    }


    private static void retrieveUserInformation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        String signInProvider = sharedPreferences.getString(USER_SIGN_IN_PROVIDER, null);
        if(signInProvider == null) {
            throw new RuntimeException("Sign in provider is null, user is not signed in");
        }
        switch(signInProvider) {
            case AuthHelper.GOOGLE_PROVIDER:{
                Log.d(TAG, "retrieveUserInformation: Retrieving user's google information");
                retrieveGoogleInformation(context);
            }
            break;
            case AuthHelper.FACEBOOK_PROVIDER: {
                Log.d(TAG, "retrieveUserInformation: Retrieving user's facebook information");
                retrieveFacebookInformation();
            }
            break;
            case AuthHelper.COGNITO_PROVIDER: {
                Log.d(TAG, "retrieveUserInformation: Retrieveing user's cognito information");
                retrieveCognitoInformation(context);
            }
            break;
        }
    }

    /**
     * Retrieve's the user's google information
     *
     * @param context The activity where the AstralUser object is created
     */
    private static void retrieveGoogleInformation(Context context) {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(context);
        email = googleAccount.getEmail();
        name = googleAccount.getDisplayName();
        signInProvider = AuthHelper.GOOGLE_PROVIDER;
        Log.d(TAG, "retrieveGoogleInformation: Finished retrieving Google information");
    }

    /**
     * Retrieve's the user's facebook information
     */
    private static void retrieveFacebookInformation() {
        AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
        Log.d(TAG, "retrieveFacebookInformation: userID = " + facebookAccessToken.getUserId());
        GraphRequest request = GraphRequest.newMeRequest(
                facebookAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            name = object.getString("name");
                            email = object.getString("email");
                            signInProvider = AuthHelper.FACEBOOK_PROVIDER;
                            Log.d(TAG, "retrieveFacebookInformation: onCompleted: Finished retrieving Facebook information");
                        }catch(JSONException exception) {
                            Log.d(TAG, "retrieveFacebookInformation: failure, response code: " + response.getError());
                            exception.printStackTrace();
                        } catch(Exception exception) {
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

    /**
     * Retrieve's the user's cognito user pool information
     *
     * @param context The activity where the AstralUser object is created
     */
    private static void retrieveCognitoInformation(Context context) {
        //Retrieve user's information from shared preferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(AuthHelper.COGNITO_INFO, Context.MODE_PRIVATE);
        name = sharedPreferences.getString(AuthHelper.COGNITO_USER_NAME, null);
        email = sharedPreferences.getString(AuthHelper.COGNITO_EMAIL, null);
        if(name == null || email == null) {
            throw new RuntimeException("Attempt to retrieve empty Cognito user details");
        }
        signInProvider = AuthHelper.COGNITO_PROVIDER;
        Log.d(TAG, "retrieveCognitoInformation: Finished retrieving Cognito information");
    }

    /**
     * Retrieve's the user's access tokens for astral
     *
     * @param tokens The token received back from the HTTP request
     */
    public void setUserAstralTokens(String tokens) {
        userAstralTokens = tokens;
    }

    /**
     *
     * @return The user's Astral tokens, null if it hasn't been set before
     */
    public String getUserAstralTokens() {
        return userAstralTokens;
    }

    /**
     * Make singleton from serialize and deserialize operation
     *
     * @param context The activity calling this
     *
     * @return The user instance object
     */
    protected User readResolve(Context context) {
        return getInstance(context);
    }
}