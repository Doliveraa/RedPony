package edu.csulb.phylo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

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
                    switch (item.getItemId()) {
                        case R.id.change_views:
                            //go to change_view fragment
                            break;

                        case R.id.upload_files:
                            //go to upload_files fragment
                            break;

                        case R.id.home_lobby:
                            //go to home_lobby fragment
                            break;

                        case R.id.website_user:
                            //go to website user
                            break;

                        case R.id.user_account:
                            //go to user account
                            break;

                    }
                    return true;
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set content view
        //create bottom navigation bar
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);
    }
}

}
