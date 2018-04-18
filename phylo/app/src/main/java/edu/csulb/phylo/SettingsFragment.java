package edu.csulb.phylo;


import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by vietl on 3/28/2018.
 */

public class SettingsFragment extends Fragment implements View.OnClickListener{

    //User Fragment
    UserFragment userFragment;

    public static SettingsFragment newInstance(){
        SettingsFragment fragment = new SettingsFragment();
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
        ImageButton backButton = getActivity().findViewById(R.id.back_button_settings);
        ImageButton helpButton = getActivity().findViewById(R.id.help_button_settings);

        //Attach listeners to buttons
        backButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        //Initliaze Fragments
        userFragment = UserFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /**
     * Provides a way for screen items to react to user events
     *
     * @param v the View item that the user has interacted with
     */
    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        switch(v.getId()) {
            case R.id.back_button_settings:
                fragmentTransaction.replace(R.id.main_activity_container, userFragment);
                break;
            case R.id.help_button_settings:
                //Pop out that displays a help message for the user
                break;
        }
        //   fragmentTransaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

    }
}
