package edu.csulb.phylo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by vietl on 2/25/2018.
 */

public class UploadFragment extends Fragment implements View.OnClickListener{

    public static UploadFragment newInstance(){
        UploadFragment fragment = new UploadFragment();
        return fragment;
        }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
            case R.id.select_file:
                //If the user selects this, select a file from the user's phone
                break;
            case R.id.upload:
                //Upload the file
                break;
        }
        fragmentTransaction.commit();

    }
}
