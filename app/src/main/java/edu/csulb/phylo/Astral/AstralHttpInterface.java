package edu.csulb.phylo.Astral;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface AstralHttpInterface {

    //Creates a astralUser once they have created an account with us or it is their first time signing
    //in to this application
    @FormUrlEncoded
    @POST("users")
    Call<AstralUser> createUser(@Body AstralUser astralUser);

    //Checks to see if a username is available
    @GET("users/check")
    Call<ResponseBody> checkUsernameAvailability(
            @Query("username") String username
    );
}
