package com.bignerdranch.android.locatr;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ProgressFragment extends DialogFragment {

    private ProgressDialog mProgressDialog;

    public ProgressFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.show();

        return mProgressDialog;
    }

    public void dismiss() {
        mProgressDialog.dismiss();
    }
}
