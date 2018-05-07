package edu.csulb.phylo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;


public class InsideRoomFragment extends Fragment
    implements View.OnClickListener{
    //Constants
    final static String TAG = InsideRoomFragment.class.getSimpleName();
    //Fragment variables
    private View fragView;
    //Views
    private FloatingActionButton fabAddFile;
    private ProgressBar progressBar;
    //Variables
    private boolean hasReadPermission;

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
        //Initialize variables
        hasReadPermission = UserPermission.checkUserPermission(getActivity(), UserPermission.Permission.READ_PERMISSION);

        //Initialize Views
        progressBar = (ProgressBar) fragView.findViewById(R.id.progress_bar_inside_room);
        fabAddFile = (FloatingActionButton) fragView.findViewById(R.id.fab_add_file);

        //Set listeners for views
        fabAddFile.setOnClickListener(this);
    }

    /**
     * Receives back the results of the user's permission
     *
     * @param requestCode  The request code received back from asking permission
     * @param permissions  The permissions asked
     * @param grantResults The granted results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case UserPermission.PERM_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission has been granted, allow user to choose files
                    beginChooseFile();
                } else {
                    //Permission Denied
                    Toast.makeText(getActivity(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.fab_add_file: {
                beginAddFile();
            }
            break;
        }
    }

    /**
     * Begins the process to add a file to Astral
     */
    private void beginAddFile() {
        if(!hasReadPermission) {
            //We do not have read permissions, ask for read permission from the user
            this.requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, UserPermission.PERM_CODE);
        } else {
            //Begin logic to retrieve files
            beginChooseFile();
        }
    }

    /**
     * Let the user begin choosing the files
     */
    private void beginChooseFile() {
        Intent chooseFileIntent = new Intent();
        //Allow the user to choose any type of file
        chooseFileIntent.setType("*");
        //Allow the user to select multiple files
        chooseFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    }
}
