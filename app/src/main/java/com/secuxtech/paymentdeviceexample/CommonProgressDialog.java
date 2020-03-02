package com.secuxtech.paymentdeviceexample;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;



public class CommonProgressDialog {
    private static AlertDialog mAlertDialog;

    public static void showProgressDialog(Context context) {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        }

        View loadView = LayoutInflater.from(context).inflate(R.layout.common_progress_dialog_view, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvTip = loadView.findViewById(R.id.tvTip);
        tvTip.setText("");

        mAlertDialog.show();
    }

    public static void showProgressDialog(Context context, String tip) {
        if (TextUtils.isEmpty(tip)) {
            tip = "";
        }

        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        }

        View loadView = LayoutInflater.from(context).inflate(R.layout.common_progress_dialog_view, null);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        TextView tvTip = loadView.findViewById(R.id.tvTip);
        tvTip.setText(tip);

        mAlertDialog.show();
    }

    public static void setProgressTip(String tip){
        TextView tvTip = mAlertDialog.findViewById(R.id.tvTip);
        tvTip.setText(tip);
    }

    public static void setProgressSubTip(String tip){
        TextView tvTip = mAlertDialog.findViewById(R.id.tvTip2);
        tvTip.setText(tip);
    }



    public static boolean isProgressVisible(){
        if (mAlertDialog == null) {
            return false;
        }
        return true;
    }

    public static void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }
}
