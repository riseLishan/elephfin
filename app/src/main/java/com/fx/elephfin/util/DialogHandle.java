package com.fx.elephfin.util;

/**
 * Created by Administrator on 2017/2/28 0028.
 */
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.fx.elephfin.R;

import java.lang.reflect.Field;


public class DialogHandle {
        public Runnable ans_true = null;
        public Runnable ans_false = null;

        // Dialog. --------------------------------------------------------------

        public boolean Confirm(Context context, String Title, String ConfirmText,
                               String CancelBtn, String OkBtn, Runnable aProcedure, Runnable bProcedure) {
            ans_true = aProcedure;
            ans_false= bProcedure;
            AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setIcon(R.mipmap.ic_launcher);
            dialog.setTitle(Title);
            dialog.setMessage(ConfirmText);
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, OkBtn,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int buttonId) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ans_true.run();
                        }
                    });
//            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, CancelBtn,
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int buttonId) {
//                            try {
//                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
//                                field.setAccessible(true);
//                                field.set(dialog, false);
//
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            ans_false.run();
//                        }
//                    });
            //dialog.setIcon(android.R.drawable.ic_dialog_alert);
//            dialog.setIcon(R.mipmap.ic_launcher);
            dialog.show();
            return true;
        }
}

