package edu.csulb.phylo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InsideRoomFragment extends Fragment {
    //Fragment variables
    private View fragView;

    //Instantiate an InsideRoomFragment object
    public static InsideRoomFragment newInstance() {
        InsideRoomFragment fragment = new InsideRoomFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_room, container, false);
        return fragView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
