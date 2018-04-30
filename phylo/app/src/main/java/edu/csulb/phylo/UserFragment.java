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
import android.widget.TextView;

import edu.csulb.phylo.Astral.Astral;

/**
 * Created by vietl on 2/25/2018.
 */

public class UserFragment extends Fragment
    implements View.OnClickListener{
    //AstralUser Variable
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

        //Generate user
        user = User.getInstance(getActivity());

        //Initiate Views
        Button uploadedButton = getActivity().findViewById(R.id.user_files);
        Button downloadedButton = getActivity().findViewById(R.id.downloaded_files);
        Button settingsB = getActivity().findViewById(R.id.settings);
        Button logoutButton = getActivity().findViewById(R.id.logout_button);
        final TextView emailTextView = getActivity().findViewById(R.id.email_text_view);

        //Put user's email into the text view
        emailTextView.setText(user.getEmail());

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

                //Removes the user's astral token from cache
                Astral.removeAstralToken(getActivity());

                //Sends the user back to the Authentication Screen
                Intent signoutIntent = new Intent(getActivity(), AuthenticationContainer.class);
                signoutIntent.setAction(AuthenticationContainer.START_LOGIN_ACTION);
                startActivity(signoutIntent);
                getActivity().finish();
                break;
        }
   //   fragmentTransaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

    }


}
