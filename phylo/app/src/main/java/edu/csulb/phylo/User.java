package edu.csulb.phylo;

import android.content.Context;

import java.io.Serializable;

import edu.csulb.phylo.Astral.Astral;

/**
 * Created by Danie on 3/19/2018.
 */

public class User implements Serializable {

    //Forces userInstance to be thread safe, all the write will happen on this instance
    //before being read from
    private static volatile User userInstace;
    //AstralUser Variables
    private static String signInProvider;
    private static String email;
    private static String name;
    private static String astralUsername;
    //Astral Variables
    private static String userAstralTokens;
    //Constants
    private final static String TAG = User.class.getSimpleName();


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
    public String getUsername() { return astralUsername; }

    /**
     * The user's sign in provider
     *
     * @return The sign in provider that the user is currently signed in with
     */
    public String getSignInProvider() {
        return signInProvider;
    }


    private static void retrieveUserInformation(final Context context) {
        //Retrieves the current sign in provider
        signInProvider = AuthHelper.getCurrentSignInProvider(context);
        if(signInProvider == null) {
            throw new RuntimeException("Sign in provider is null, user is not signed in");
        }

        //Check if the user's information has already been cached
        final boolean containsInfo = AuthHelper.containsUserInfo(context);
        if(containsInfo) {
            name = AuthHelper.getCachedUserName(context);
            email = AuthHelper.getCachedUserEmail(context);
            astralUsername = Astral.getCachedAstralUsername(context);
        } else {
            throw new RuntimeException("Attempt to retrieve null user information");
        }
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