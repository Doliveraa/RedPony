package edu.csulb.phylo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by vietl on 2/25/2018.
 */

public class ChangeFragment extends Fragment{
    public static ChangeFragment newInstance(){
        ChangeFragment fragment = new ChangeFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Intent startMapsIntent = new Intent(getActivity(), MapsActivity.class);
        startActivity(startMapsIntent);
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change, container, false);
    }
}
