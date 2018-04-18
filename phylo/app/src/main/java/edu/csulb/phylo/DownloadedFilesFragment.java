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
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by vietl on 3/28/2018.
 */

public class DownloadedFilesFragment extends Fragment
        implements View.OnClickListener{

    //AstralUser Fragment
    UserFragment userFragment;

    public static DownloadedFilesFragment newInstance(){
        DownloadedFilesFragment fragment = new DownloadedFilesFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_downloaded_files, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initiate Views
        ImageButton backButton = getActivity().findViewById(R.id.back_button_downloaded);
        ImageButton helpButton = getActivity().findViewById(R.id.help_button_downloaded);

        //Attach listeners to buttons
        backButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        //Initliaze Fragments
        userFragment = UserFragment.newInstance();
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
            case R.id.back_button_downloaded:
                fragmentTransaction.replace(R.id.main_activity_container, userFragment);
                break;
            case R.id.help_button_downloaded:
                //Pop out that displays a help message for the user
                break;
        }
        //   fragmentTransaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

    }
}
