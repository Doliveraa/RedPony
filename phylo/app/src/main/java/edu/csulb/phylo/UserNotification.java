package edu.csulb.phylo;

import android.content.Context;
import android.os.Vibrator;
import android.view.Gravity;
import android.widget.Toast;

public class UserNotification {

    /**
     * Displays a message as a toast
     *
     * @param message      The message to be displayed
     * @param vibratePhone If the phone should vibrate
     */
    public static void displayToast(Context context, String message, boolean vibratePhone) {
        Vibrator vibrator =(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.show();
        if (vibratePhone) {
            vibrator.vibrate(500);
        }
    }
}
