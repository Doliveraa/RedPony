package edu.csulb.phylo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by vietl on 2/21/2018.
 */

public class MainActivityContainer extends AppCompatActivity{

    //Variables
    //Cognito User
    //Bottom Navigation View
    private BottomNavigationView.OnNavigationItemSelectedListener NavItemListen =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.change_views:
                            //go to change_view fragment
                            selectedFragment = HomeFragment.newInstance();
                            break;

                        case R.id.upload_files:
                            //go to upload_files fragment
                            selectedFragment = HomeFragment.newInstance();
                            break;

                        case R.id.home_lobby:
                            selectedFragment = HomeFragment.newInstance();
                            break;

                        case R.id.website_user:
                            //go to website user
                            selectedFragment = HomeFragment.newInstance();
                            break;

                        case R.id.user_account:
                            //go to user account
                            selectedFragment = HomeFragment.newInstance();
                            break;

                    }
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, selectedFragment);
                    transaction.commit();
                    return true;
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set content view
        //create bottom navigation bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home_lobby);
        bottomNavigationView.setOnNavigationItemSelectedListener(NavItemListen);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, HomeFragment.newInstance());
        transaction.commit();
    }
}
