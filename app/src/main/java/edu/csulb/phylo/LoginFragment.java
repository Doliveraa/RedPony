package edu.csulb.phylo;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;

/**
 * Created by Danie on 1/24/2018.
 */

public class LoginFragment extends Fragment
    implements View.OnClickListener{
    //Views
    private Button normalLoginButton;
    private SignInButton googleLoginButton;
    private ImageButton facebookLoginButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView createAccountText;
    private TextView forgotPasswordText;
    //Interface
    public interface OnChangeFragmentListener{
        void buttonClicked(AuthenticationActivity.AuthFragmentType fragmentType);
    }
    private OnChangeFragmentListener onChangeFragmentListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize all of the views
        normalLoginButton = getActivity().findViewById(R.id.button_normal_login);
        facebookLoginButton = getActivity().findViewById(R.id.facebook_login_button);
        googleLoginButton = getActivity().findViewById(R.id.google_login_button);
        emailEditText = getActivity().findViewById(R.id.email_edit_text);
        passwordEditText = getActivity().findViewById(R.id.password_edit_text);
        createAccountText = getActivity().findViewById(R.id.create_account_text);
        forgotPasswordText = getActivity().findViewById(R.id.forgot_password_text);

        //Set listeners for buttons
        normalLoginButton.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        createAccountText.setOnClickListener(this);
        forgotPasswordText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.create_account_text: {
                onChangeFragmentListener.buttonClicked(AuthenticationActivity.AuthFragmentType.CREATE_ACCOUNT);
            }
            break;
            case R.id.forgot_password_text: {
                onChangeFragmentListener.buttonClicked(AuthenticationActivity.AuthFragmentType.FORGOT_PASSWORD);
            }
            break;
        }
    }

    public void setOnChangeFragmentListener(OnChangeFragmentListener onChangeFragmentListener) {
        this.onChangeFragmentListener = onChangeFragmentListener;
    }


}
