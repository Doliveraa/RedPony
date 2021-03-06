package edu.csulb.phylo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.phylo.Astral.Astral;
import edu.csulb.phylo.Astral.AstralFile;
import edu.csulb.phylo.Astral.AstralHttpInterface;
import edu.csulb.phylo.Astral.AstralItem;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by vietl on 3/28/2018.
 */

public class UploadedFilesFragment extends Fragment
    implements View.OnClickListener{

    //Constants
    private final String TAG = UploadedFilesFragment.class.getSimpleName();
    //Variables
    private boolean hasLocationPermission;
    private UserLocationClient userLocationClient;
    private boolean isRetrievingUserPermission;
    private User user;
    //Views
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List <AstralItem> astralItemList;

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
        hasLocationPermission = UserPermission.checkUserPermission(getActivity(), UserPermission.Permission.LOCATION_PERMISSION);
        userLocationClient = new UserLocationClient(getActivity());
        isRetrievingUserPermission = false;
        user = User.getInstance(getActivity());
        astralItemList = new ArrayList<>();

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
     * Begins tracking the user's location if they have permission
     * If they don't have any permissions, this methods asks them for the permissions
     */
    @Override
    public void onStart() {
        super.onStart();
        //If we have the user's permission to receive their location, start the location tracking
        if (hasLocationPermission) {
            userLocationClient.singleLocationRetrieval(getActivity());
        } else {
            //We do not have permission to receive the user's location, ask for permission
//            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, UserPermission.PERM_CODE);
        }
    }



    /**
     * Requests user permission to track their current location
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
                    //Permission has been granted, we can now start tracking the user's location
                    Log.d(TAG, "onRequestPermissionResult : AstralUser has accepted location permissions");
                    userLocationClient.singleLocationRetrieval(getActivity());
                    hasLocationPermission = true;
                } else {
                    Log.d(TAG, "onRequestPermissionResult : AstralUser has denied location permissions");
                    //Permission Denied
                    Toast.makeText(getActivity(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

    /**
     * GET request for the user files
     */
    public void getUserFiles(){
        //Start a GET request to retrieve all of the rooms in the area
        final Astral astral = new Astral(getActivity().getString(R.string.astral_base_url));
        //Add logging interceptor
        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.BODY);
        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();

        //Create the GET request
        Call<List<AstralItem>> request = astralHttpInterface.getFiles(
                getString(R.string.astral_key),
                user.getUserAstralTokens()
        );

        request.enqueue(new Callback<List<AstralItem>>() {
            @Override
            public void onResponse(Call<List<AstralItem>> call, retrofit2.Response<List<AstralItem>> response) {
                if (response.code() == Astral.OK) {
                    Log.d(TAG, "retrieveRooms-> onResponse: Success Code : " + response.code());
                        List<AstralItem> astralHolder;//holds items/files
                        astralHolder = response.body();//add all the items/files to astralHolder
                        for (int i = 0; i < astralHolder.size(); i++) {
                            Gson gson = new Gson();
                            String filePS = gson.toJson(astralHolder);
                            if (astralHolder.get(i).getName().equals("room")) { //if the owner is room
                                astralItemList.add(astralItemList.get(i)); //add it to the list of files
                            }
                        }
                        adapter = new FileAdapter(astralItemList);
                        recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<AstralItem>> call, Throwable throwable) {
                Log.w(TAG, "retrieveUserFiles-> onFailure");
            }
        });

    }
}
