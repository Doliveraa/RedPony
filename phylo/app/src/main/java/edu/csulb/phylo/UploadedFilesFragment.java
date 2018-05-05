package edu.csulb.phylo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by vietl on 3/28/2018.
 */

public class UploadedFilesFragment extends Fragment
    implements View.OnClickListener{

    //Views
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    UserFragment userFragment;

    public static UploadedFilesFragment newInstance(){
        UploadedFilesFragment fragment = new UploadedFilesFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_uploaded_files, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initiate Variables
        layoutManager = new LinearLayoutManager(getActivity());

        //Initiate Views
        ImageButton backButton = getActivity().findViewById(R.id.back_button_uploaded);
        ImageButton helpButton = getActivity().findViewById(R.id.help_button_uploaded);
        recyclerView = getActivity().findViewById(R.id.recycler_view_upload_files);
        recyclerView.setLayoutManager(layoutManager);

        //Attach listeners to buttons
        backButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        //Initiate fragments
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
            case R.id.back_button_uploaded:
                fragmentTransaction.replace(R.id.main_activity_container, userFragment);
                break;
            case R.id.help_button_uploaded:
                //Pop out for help for upload page
                break;
        }
        //   fragmentTransaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();

    }
}
