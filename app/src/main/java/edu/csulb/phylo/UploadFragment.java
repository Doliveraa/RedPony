package edu.csulb.phylo;

import android.support.v4.app.Fragment;
import android.os.Bundle;

/**
 * Created by vietl on 2/25/2018.
 */

public class UploadFragment extends Fragment{
        public static UploadFragment newInstance(){
            UploadFragment fragment = new UploadFragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
}
