package edu.csulb.phylo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by vietl on 2/25/2018.
 */

public class UserFragment extends Fragment
    implements View.OnClickListener{
    //User Variable
    private User user;

    //Fragment Variables
    UploadedFilesFragment uploadedFilesFragment;
    DownloadedFilesFragment downloadedFilesFragment;
    SettingsFragment settingsFragment;

    /**
     * Instantiates an instance of UserFragment
     *
     * @return A UserFragment object
     */
    public static UserFragment newInstance(){
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initiate Views
        Button uploadedButton = getActivity().findViewById(R.id.user_files);
        Button downloadedButton = getActivity().findViewById(R.id.downloaded_files);
        Button settingsB = getActivity().findViewById(R.id.settings);
        Button logoutButton = getActivity().findViewById(R.id.logout_button);

        //Attach listeners to buttons
        uploadedButton.setOnClickListener(this);
        downloadedButton.setOnClickListener(this);
        settingsB.setOnClickListener(this);
        logoutButton.setOnClickListener(this);

        //Instantiate Fragments
        uploadedFilesFragment = uploadedFilesFragment.newInstance();
        downloadedFilesFragment = downloadedFilesFragment.newInstance();
        settingsFragment = settingsFragment.newInstance();
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
            case R.id.user_files:
                fragmentTransaction.replace(R.id.main_activity_container, uploadedFilesFragment);
                break;
            case R.id.downloaded_files:
                fragmentTransaction.replace(R.id.main_activity_container, downloadedFilesFragment);
                break;
            case R.id.settings:
                fragmentTransaction.replace(R.id.main_activity_container, settingsFragment);
                break;
            case R.id.logout_button:
                AuthHelper.signOutUser(getActivity(), user);
                break;
        }
        fragmentTransaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

    }

    /**
     * Sets the current user object for this fragment
     *
     * @param currUser The current user on the application
     */
    public void setCurrentUser(User currUser) {
        user = currUser;
    }
}
