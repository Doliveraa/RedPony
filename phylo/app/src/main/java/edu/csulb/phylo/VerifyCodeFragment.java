package edu.csulb.phylo;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

import java.io.IOException;

import edu.csulb.phylo.Astral.Astral;
import edu.csulb.phylo.Astral.AstralHttpInterface;
import edu.csulb.phylo.Astral.AstralUser;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Daniel on 2/11/2018.
 */

//TODO: Build user object and begin sending requests

public class VerifyCodeFragment extends Fragment
        implements View.OnClickListener {
    //Views
    private Button confirmCodeButton;
    private EditText codeInputEditText;
    //Variables
    private CognitoUser cognitoUser;
    private AstralUser astralUser;
    //Constants
    private final static String TAG = VerifyCodeFragment.class.getSimpleName();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_code, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize all of the buttons
        confirmCodeButton = (Button) getActivity().findViewById(R.id.button_confirm_verification_code);
        confirmCodeButton.setEnabled(false);
        codeInputEditText = (EditText) getActivity().findViewById(R.id.edit_text_verify_code);

        //Set listeners
        confirmCodeButton.setOnClickListener(this);
        codeInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().isEmpty()) {
                    confirmCodeButton.setEnabled(false);
                    confirmCodeButton.setTextColor(getResources().getColor(R.color.gray, null));
                } else {
                    if(!confirmCodeButton.isEnabled()) {
                        confirmCodeButton.setEnabled(true);
                        confirmCodeButton.setTextColor(getResources().getColor(R.color.black, null));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_confirm_verification_code: {
                //Check if the verification code typed by the user is correct
                String confirmationCode = codeInputEditText.getText().toString();
                cognitoUser.confirmSignUpInBackground(confirmationCode, true, new GenericHandler() {
                    @Override
                    public void onSuccess() {
                        //Verification successful
                        Log.d(TAG, "onClick-> onSuccess: Starting Post Request");

                        //Set that the user has authenticated with Cognito
                        AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.COGNITO_PROVIDER);
                        AuthHelper.cacheCurrentCognitoSignedInUser(getActivity(), cognitoUser.getUserId());
                        AuthHelper.cacheUserInformation(getActivity(), astralUser.getName(), astralUser.getEmail());
                        Astral.storeAstralUsername(getActivity(), astralUser.getUsername());

                        //Start at POST request to create the user in the Astral Framework
                        final Astral astral = new Astral(getString(R.string.astral_base_url));
                        //Intercept the request to add a header item
                        astral.addRequestInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request request = chain.request();
                                //Add the app key to the request header
                                Request.Builder newRequest = request.newBuilder().header(
                                        Astral.APP_KEY_HEADER, getString(R.string.astral_key));
                                //Continue the request
                                return chain.proceed(newRequest.build());
                            }
                        });
                        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
                        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();
                        //Create the POST request
                        Call<AstralUser> request = astralHttpInterface.createUser(astralUser.getUsername(),
                                astralUser.getEmail(), null);
                        //Call the request asynchronously
                        request.enqueue(new Callback<AstralUser>() {
                            @Override
                            public void onResponse(Call<AstralUser> call, retrofit2.Response<AstralUser> response) {
                                if(response.isSuccessful()) {
                                    Log.d(TAG, "onClick-> onSuccess-> onResponse: Successful Response Code " + response.code());

                                    //Retrieve the token received
                                    String userToken = response.body().getToken();

                                    //Store the User's token item
                                    Astral.storeAstralUserToken(getActivity(), userToken);

                                    //Create an Astral AstralUser account
                                    Intent intent = new Intent(getActivity(), MainActivityContainer.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else {
                                    Log.d(TAG, "onClick-> onSuccess-> onResponse: Failed response Code " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<AstralUser> call, Throwable t) {
                                //The request has unexpectedly failed
                                Log.d(TAG, "onCLick-> onSuccess-> onResponse: Unexpected request failure");
                            }
                        });

                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.d(TAG, "onClick-> onFailure: Confirmation failed");
                        exception.printStackTrace();
                        printToast("Incorrect verification code.");
                    }
                });
            }
            break;
        }
    }

    /**
     * Sets the CognitoUser object and Astral object to complete account creation
     *
     * @param cognitoUser the CognitoUser object being sent to AWS Cognito
     * @param astralUser the AstralUser object being sent to the Astral Framework
     */
    public void setUser(CognitoUser cognitoUser, AstralUser astralUser) {
        this.cognitoUser = cognitoUser;
        this.astralUser = astralUser;
    }


    private void printToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}