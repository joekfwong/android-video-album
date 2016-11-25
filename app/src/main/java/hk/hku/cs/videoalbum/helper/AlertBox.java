package hk.hku.cs.videoalbum.helper;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class AlertBox {

    public void alert(String title, String mymessage) {
        new AlertDialog.Builder(null)
                .setMessage(mymessage)
                .setTitle(title)
                .setCancelable(true)
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .show();
    }
}