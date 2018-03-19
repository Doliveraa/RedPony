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
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

/**
 * Created by Daniel on 2/11/2018.
 */

public class VerifyCodeFragment extends Fragment
        implements View.OnClickListener {
    //Views
    private Button confirmCodeButton;
    private EditText codeInputEditText;
    //Variables
    private CognitoUser cognitoUser;
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
                        Log.d(TAG, "onClick-> onSuccess: Confirmation successful, sending user to main activity");

                        //Set that the user has authenticated with Cognito
                        AuthHelper.setCurrentSignInProvider(getActivity(), AuthHelper.COGNITO_PROVIDER);
                        AuthHelper.cacheCurrentSignedInUser(getActivity(), cognitoUser.getUserId());
                        Intent intent = new Intent(getActivity(), MainActivityContainer.class);
                        startActivity(intent);
                        getActivity().finish();
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

    public void setCognitoUser(CognitoUser cognitoUser) {
        this.cognitoUser = cognitoUser;
    }

    private void printToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}