package edu.csulb.phylo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
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
        implements UserFragment.StartNewFragmentListener { //Fragment Views
    private HomeFragment homeFragment;
    private MapsFragment mapsFragment;
    private UploadFragment uploadFragment;
    private UserFragment userFragment;
    private PinnedFragment pinnedFragment;
    private UploadedFilesFragment uploadedFilesFragment;
    private DownloadedFilesFragment downloadedFilesFragment;
    private SettingsFragment settingsFragment;
    //Activity Constants
    private static final String TAG = "MainActivityContainer";

    /**
     * Bottom Navigation Bar
     */
    private BottomNavigationView.OnNavigationItemSelectedListener NavItemListen =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    return startFragment(item.getItemId());
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
            case R.id.change_views:
                fragmentTransaction.replace(R.id.main_activity_container, mapsFragment);
                break;

            case R.id.upload_files:
                fragmentTransaction.replace(R.id.main_activity_container, uploadFragment);
                break;

            case R.id.home_lobby:
                fragmentTransaction.replace(R.id.main_activity_container, homeFragment);
                break;

            case R.id.website_user:
                fragmentTransaction.replace(R.id.main_activity_container, pinnedFragment);
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
        pinnedFragment = PinnedFragment.newInstance();
        uploadFragment = UploadFragment.newInstance();
        userFragment = UserFragment.newInstance();
        uploadedFilesFragment = uploadedFilesFragment.newInstance();
        downloadedFilesFragment = downloadedFilesFragment.newInstance();
        settingsFragment = settingsFragment.newInstance();

        //Set the listener for the user fragment
        userFragment.setStartNewFragmentListener(this);
        //Set that it is the first time the Maps Fragment is opening
        setMapFragmentFirstTime();

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

    /**
     * Sets that the first time we are opening the map fragment to true
     */
    private void setMapFragmentFirstTime() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(MapsFragment.MAPS_FRAGMENT_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MapsFragment.IS_FIRST_TIME, true);
        editor.commit();
    }

    /**
     * Starts a new Fragment depending on the id received
     *
     * @param itemId The id of the fragment to start
     */
    @Override
    public void onStartNewFragment(int itemId) {
        startFragment(itemId);
    }
}
