package com.bignerdranch.android.locatr;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public  class PermissionRationaleFragment extends DialogFragment {

    private PositiveButtonClickCallback mPositiveButtonClickCallback;

    public PermissionRationaleFragment(PositiveButtonClickCallback positiveButtonClickCallback) {
        mPositiveButtonClickCallback = positiveButtonClickCallback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
     return    new AlertDialog.Builder(getActivity())
                .setMessage("Locatr uses location data to find images near you on Flickr.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPositiveButtonClickCallback.onClick(dialog, which);
                    }
                })
                .create();
    }



     public interface PositiveButtonClickCallback {
        void onClick(DialogInterface dialog, int which);
     }
}
