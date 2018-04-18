package edu.csulb.phylo.Astral

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface AstralHttpInterface {
    /**
     * Creates a astralUser once they have created an account with us or it is their
     * first time signing in to this application
     */
    @FormUrlEncoded
    @POST("users")
    fun createUser(@Body astralUser : AstralUser)
            : Call<AstralUser>

    /**
     * Checks to see if a username is available
     */
    @GET("users/check")
    fun checkUsernameAvailability(
            @Query("username") username : String
    ) : Call<ResponseBody>

    /**
     * Retrieve user token
     */
    @GET("users")
    fun getUserToken()
    : Call<AstralUser>

    /**
     * Retrieves the User's information
     */
    @GET("users")
    fun getUserInformation()
    : Call<AstralUser>

}