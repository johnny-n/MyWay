package com.projects.johnny.myway;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AddressDialogFragment extends DialogFragment {

    private static final String ARG_ADDRESS = "arg_address";

    private TextView mAddressTextView;

    public static AddressDialogFragment newInstance(String address) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ADDRESS, address);

        AddressDialogFragment addressDialogFragment = new AddressDialogFragment();
        addressDialogFragment.setArguments(args);
        return addressDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_address_display, null, false);

        String address = (String) getArguments().getSerializable(ARG_ADDRESS);
        mAddressTextView = (TextView) v.findViewById(R.id.dialog_address_text_view);
        mAddressTextView.setText(address);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.address_dialog_title)
                .create();
    }
}
