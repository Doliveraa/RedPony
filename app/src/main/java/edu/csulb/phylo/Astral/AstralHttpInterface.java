package edu.csulb.phylo.Astral;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AstralHttpInterface {

    //Creates a astralUser once they have created an account with us or it is their first time signing
    //in to this application
    @FormUrlEncoded
    @POST("users")
    Call<ResponseBody> createUser(@Body AstralUser astralUser);
}
