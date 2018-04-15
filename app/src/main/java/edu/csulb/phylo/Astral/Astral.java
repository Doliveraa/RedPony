package edu.csulb.phylo.Astral;

import java.io.Serializable;

import edu.csulb.phylo.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Astral{
    //Constants
    public final static String ASTRAL_USERNAME = "astral username";
    //REST Api items
    private Retrofit.Builder retrofitBuilder;
    private OkHttpClient.Builder okHttpClientBuilder;

    /**
     * Astral object constructor
     */
    private Astral(String baseUrl) {
        //Instantiate a Retrofit.Builder object
        retrofitBuilder = new Retrofit.Builder();
        //Instantiate an OkHttpClient.Builder object
        okHttpClientBuilder = new OkHttpClient.Builder();

        //Create a standard retrofit object
        retrofitBuilder.baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create());
    }



    /**
     * Adds a logging interceptor for Retrofit only if its development mode, will crash otherwise
     *
     * @param loggingLevel This can be set to the following values:
     *                  Level.NONE : Nothing will be logged
     *                  Level.BASIC : Logs requests and response lines
     *                  Level.BODY : Will request everything ,however, be careful because it might print huge image arrays or videos
     *                  Level.HEADERS : Logs request and response lines, and along with the respective headers
     */
    public void addLoggingInterceptor(HttpLoggingInterceptor.Level loggingLevel) {
        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(loggingLevel);
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        } else {
            throw new RuntimeException("Cannot add interceptor outside of development build");
        }
    }

    /**
     * Adds the ability to intercept a request before sending it to add any runtime information
     *
     * @param interceptor The interceptor interface
     */
    public void addRequestInterceptor(Interceptor interceptor) {
        okHttpClientBuilder.addInterceptor(interceptor);
    }

    /**
     * Returns a Retrofit object based on the attachments previously made
     *
     * @return a Retrofit object
     */
    public Retrofit geClient() {
        //Add the current OkHttpClient that the user has optionally built
        retrofitBuilder.client(okHttpClientBuilder.build());
        //Return the client to be used for requests and responses
        return retrofitBuilder.build();
    }


}
