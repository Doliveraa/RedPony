package edu.csulb.phylo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import edu.csulb.phylo.Astral.Astral;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by vietl on 2/21/2018.
 */

public class MainActivityContainer extends AppCompatActivity
        implements UserFragment.StartNewFragmentListener, HomeFragment.OnRoomEnterListener{
    //Fragments
    HomeFragment homeFragment;
    MapsFragment mapsFragment;
    UploadFragment uploadFragment;
    UserFragment userFragment;
    UploadedFilesFragment uploadedFilesFragment;
    DownloadedFilesFragment downloadedFilesFragment;
    SettingsFragment settingsFragment;
    //Activity Constants
    private static final String TAG = "MainActivityContainer";

    /**
     * Bottom Navigation Bar
     */
    private BottomNavigationView.OnNavigationItemSelectedListener NavItemListen =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    switch (item.getItemId()) {
                        case R.id.map:
                            fragmentTransaction.replace(R.id.container, mapsFragment);
                            break;
                        case R.id.home_lobby:
                            fragmentTransaction.replace(R.id.container, homeFragment);
                            break;

                        case R.id.user_account:
                            //go to user account
                            fragmentTransaction.replace(R.id.container, userFragment);
                            break;

                    }
                    //fragmentTransaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.commit();
                    return true;
                }
            };

    /**
     * Starts a fragment for the user
     *
     * @param fragmentId The id of the fragment that is going to start
     * @return True if the fragment has started correctly
     */
    private boolean startFragment(int fragmentId) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (fragmentId) {
            case R.id.map:
                fragmentTransaction.replace(R.id.main_activity_container, mapsFragment);
                break;
            case R.id.home_lobby:
                fragmentTransaction.replace(R.id.main_activity_container, homeFragment);
                break;

            case R.id.user_account:
                fragmentTransaction.replace(R.id.main_activity_container, userFragment);
                break;
            case R.id.user_files:
                fragmentTransaction.replace(R.id.main_activity_container, uploadedFilesFragment);
                break;
            case R.id.downloaded_files:
                fragmentTransaction.replace(R.id.main_activity_container, downloadedFilesFragment);
                break;
            case R.id.settings:
                fragmentTransaction.replace(R.id.main_activity_container, settingsFragment);
                break;

        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        return true;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_container);

        //create bottom navigation bar
        createBottomNavigationView();

        //Instantiate fragments
        homeFragment = HomeFragment.newInstance();
        mapsFragment = MapsFragment.newInstance();
        uploadFragment = UploadFragment.newInstance();
        userFragment = UserFragment.newInstance();
    }
    /**
     * Creates the bottom navigation for the activity container. Sets the home screen as default.
     */
    public void createBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //Disables automatic shifting from the bottom navigation bar
        BottomNavigationBarShiftHelp.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home_lobby);
        bottomNavigationView.setOnNavigationItemSelectedListener(NavItemListen);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, HomeFragment.newInstance());
        //Commit changes transaction
        transaction.commit();
    }

    @Override
    public void roomEntered(final String roomName, final double latitude, final double longitude) {
        Log.d(TAG, "roomEntered: Entering a Room");
        //Instantiate Room Fragment
        InsideRoomFragment insideRoomFragment = InsideRoomFragment.newInstance();
        //Set bundle objects to send to Fragment
        Bundle args = new Bundle();
        args.putString("room_name", roomName);
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        insideRoomFragment.setArguments(args);

        //Begin Inside room fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, insideRoomFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onStartNewFragment(int itemId) {

    }
}
