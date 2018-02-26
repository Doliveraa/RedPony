package edu.csulb.phylo;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by vietl on 2/25/2018.
 */

public class ChangeFragment extends Fragment{
    public static ChangeFragment newInstance(){
        ChangeFragment fragment = new ChangeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
