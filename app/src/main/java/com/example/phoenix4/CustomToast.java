package com.example.phoenix4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast extends Toast {

    private static final String TAG = "CustomToast";

    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout relativeLayoutToast;
    @SuppressLint("StaticFieldLeak")
    private static TextView textViewSuccessToast;
    @SuppressLint("StaticFieldLeak")
    private static ImageView imageViewSuccessToast;

    public CustomToast(Context context) {
        super ( context );
    }

    public static Toast makeSuccessToast(Context context, String toastMessage, int duration) {
        Toast toast = new Toast ( context );
        toast.setDuration ( duration );
        View view = LayoutInflater.from ( context ).inflate ( R.layout.toast, null, false );
        relativeLayoutToast = view.findViewById ( R.id.relativeLayoutToast );
        textViewSuccessToast = view.findViewById ( R.id.textViewSuccessToast );
        imageViewSuccessToast = view.findViewById ( R.id.imageViewSuccessToast );
        relativeLayoutToast.setBackgroundColor ( Color.rgb ( 204, 255, 189 ) );
        textViewSuccessToast.setText ( toastMessage );
        imageViewSuccessToast.setImageResource ( R.drawable.success_toast );
        toast.setView ( view );
        Log.i ( TAG, "makeSuccessText: " + toastMessage );
        return toast;
    }

    public static Toast makeErrorToast(Context context, String toastMessage, int duration) {
        Toast toast = new Toast ( context );
        toast.setDuration ( duration );
        View view = LayoutInflater.from ( context ).inflate ( R.layout.toast, null, false );
        relativeLayoutToast = view.findViewById ( R.id.relativeLayoutToast );
        textViewSuccessToast = view.findViewById ( R.id.textViewSuccessToast );
        imageViewSuccessToast = view.findViewById ( R.id.imageViewSuccessToast );
        relativeLayoutToast.setBackgroundColor ( Color.rgb ( 244, 153, 149 ) );
        textViewSuccessToast.setText ( toastMessage );
        imageViewSuccessToast.setImageResource ( R.drawable.error_toast );
        toast.setView ( view );
        Log.i ( TAG, "makeErrorText: " + toastMessage );
        return toast;
    }

    public static Toast makeWarningToast(Context context, String toastMessage, int duration) {
        Toast toast = new Toast ( context );
        toast.setDuration ( duration );
        View view = LayoutInflater.from ( context ).inflate ( R.layout.toast, null, false );
        relativeLayoutToast = view.findViewById ( R.id.relativeLayoutToast );
        textViewSuccessToast = view.findViewById ( R.id.textViewSuccessToast );
        imageViewSuccessToast = view.findViewById ( R.id.imageViewSuccessToast );
        relativeLayoutToast.setBackgroundColor ( Color.rgb ( 233, 209, 209 ) );
        textViewSuccessToast.setText ( toastMessage );
        imageViewSuccessToast.setImageResource ( R.drawable.warning_toast );
        toast.setView ( view );
        Log.i ( TAG, "makeWarningToast: " + toastMessage );
        return toast;
    }

    public static Toast makeInfoToast(Context context, String toastMessage, int duration) {
        Toast toast = new Toast ( context );
        toast.setDuration ( duration );
        View view = LayoutInflater.from ( context ).inflate ( R.layout.toast, null, false );
        relativeLayoutToast = view.findViewById ( R.id.relativeLayoutToast );
        textViewSuccessToast = view.findViewById ( R.id.textViewSuccessToast );
        imageViewSuccessToast = view.findViewById ( R.id.imageViewSuccessToast );
        relativeLayoutToast.setBackgroundColor ( Color.rgb ( 188, 234, 241 ) );
        textViewSuccessToast.setText ( toastMessage );
        imageViewSuccessToast.setImageResource ( R.drawable.info_toast );
        toast.setView ( view );
        Log.i ( TAG, "makeInfoToast: " + toastMessage );
        return toast;
    }
}
