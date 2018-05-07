package edu.csulb.phylo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Daniel on 1/28/2018.
 */

public class UserPermission {
    public final static int PERM_CODE = 2035;
    public enum Permission{
        READ_PERMISSION,
        WRITE_PERMISSION,
        LOCATION_PERMISSION
    }

    public static boolean checkUserPermission(Context context, Permission permission) {
        switch(permission) {
            case READ_PERMISSION:{
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED){
                    return true;
                }
            }
            break;
            case WRITE_PERMISSION:{
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
            break;
            case LOCATION_PERMISSION:{
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
            break;
        }
        return false;
    }
}
