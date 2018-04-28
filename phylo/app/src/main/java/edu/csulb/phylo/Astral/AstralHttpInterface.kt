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
    fun createUser(@Field("username") username: String,
                   @Field("email") email: String,
                   @Field("password") password: String?)
            : Call<AstralUser>

    /**
     * Checks to see if a username is available
     */
    @GET("users/check")
    fun checkUsernameAvailability(
            @Query("username") username: String
    ): Call<ResponseBody>

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

    /**
     * Creates a Room with certain specifications
     */
    @FormUrlEncoded
    @POST("files")
    fun createRoom(@Field("name") roomName: String,
                   @Field("latitude") latitude: Float,
                   @Field("longitude") longitude: Float,
                   @Field("expirationDate") expirationDate: Long)
            :Call<ResponseBody>
}