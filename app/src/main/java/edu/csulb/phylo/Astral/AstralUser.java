package edu.csulb.phylo.Astral;

public class AstralUser {
    private String username;
    private String email;
    private String password;
    private String userToken;

    /**
     * Constructor to create an Astral User object
     * @param email User's email
     * @param username User's chosen username
     * @param password should be null since we are not using the password to be sent to Astral
     */
    public AstralUser(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    /**
     * Retrieves the user's token from a response
     *
     * @return The User's token retrieved from Astral
     */
    public String getUserToken() {
        return userToken;
    }

    public String getUsername() {
        return username;
    }

    public String getUserEmail() {
        return email;
    }

}
