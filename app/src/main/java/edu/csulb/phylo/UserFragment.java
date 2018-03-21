package edu.csulb.phylo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by vietl on 2/25/2018.
 */

public class UserFragment extends Fragment
    implements View.OnClickListener{
    //Fragment Variables
    private User user;

    public static UserFragment newInstance(){
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initiate Views
        Button logoutButton = getActivity().findViewById(R.id.logout_button);

        //Attach listeners to buttons
        logoutButton.setOnClickListener(this);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_user, container, false);
    }

    /**
     * Provides a way for screen items to react to user events
     *
     * @param v the View item that the user has interacted with
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.logout_button: {

            }
            break;
        }
    }

    public void setCurrentUser(User currUser) {
        user = currUser;
    }
}
