package edu.csulb.phylo;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.phylo.Astral.AstralItem;

import static android.app.Activity.RESULT_OK;


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
    private UserLocationClient userLocationClient;

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
        userLocationClient = new UserLocationClient(getActivity());

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
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
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
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, UserPermission.PERM_CODE);
        } else {
            //Begin logic to retrieve files
            beginChooseFile();
        }
    }

    /**
     * Let the user begin choosing the files
     */
    private void beginChooseFile() {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        //Allow the user to choose any type of file
        chooseFileIntent.setType("*/*");
        //Allow the user to select multiple files
        chooseFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //Show chooser and start activity for result
        startActivityForResult(
                Intent.createChooser(chooseFileIntent, "Select Files"),
                FileUtil.FILE_CHOOSE_REQUEST
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FileUtil.FILE_CHOOSE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            ArrayList<Uri> fileUris = new ArrayList<Uri>();
            ClipData clipData = data.getClipData();
            if(clipData == null) {
                fileUris.add(data.getData());
            }else{
                for(int i =0; i < clipData.getItemCount(); i++ ){
                    ClipData.Item item = clipData.getItemAt(i);
                    fileUris.add(item.getUri());
                }
            }
            uploadFiles(fileUris);
        } else {
            Log.d(TAG, "onActivityResult: no file has been selected");
        }
    }

    private void uploadFiles(List<Uri> fileUriList) {
        final String roomName = getArguments().getString("room_name");
        final double latitude = getArguments().getDouble("latitude");
        final double longitude = getArguments().getDouble("longitude");

        ArrayList<Double> location = new ArrayList<>();
        location.add(latitude);
        location.add(longitude);

        AstralItem astralItem = new AstralItem();
        astralItem.setName("roomName");
        astralItem.setLocation(location);
        astralItem.setExpirationDate("2100-04-23T18:25:43.511Z");
    }
}
