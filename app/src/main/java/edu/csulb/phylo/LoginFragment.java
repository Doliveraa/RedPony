package edu.csulb.phylo;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Danie on 1/24/2018.
 */

public class LoginFragment extends Fragment
    implements View.OnClickListener{
    //Views
    private Button loginButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initialize Views
        loginButton = (Button) getActivity().findViewById(R.id.button_normal_login);


        //Initialize Listeners
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_normal_login: {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
            break;
        }
    }
}
