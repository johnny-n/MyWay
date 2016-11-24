package com.projects.johnny.myway;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AddressDialogFragment extends DialogFragment {

    private static final String ARG_ADDRESS = "arg_address";

    public static AddressDialogFragment newInstance(String address) {
        Bundle args = new Bundle();
        args.putString(ARG_ADDRESS, address);

        AddressDialogFragment addressDialogFragment = new AddressDialogFragment();
        addressDialogFragment.setArguments(args);
        return addressDialogFragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_address_display, null, false);

        final String address = getArguments().getString(ARG_ADDRESS);
        final TextView mAddressTextView = (TextView) view.findViewById(R.id.dialog_address_text_view);
        mAddressTextView.setText(address);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                            .setView(view)
                                            .setTitle(R.string.address_dialog_title)
                                            .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        return dialog;
    }
}
