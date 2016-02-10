package com.projects.johnny.myway;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

/**
 * Created by Johnny on 2/9/16.
 */
public class AddLocationFragment extends Fragment {

    SupportPlaceAutocompleteFragment mPlaceAutocompleteFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_location, container, false);

        mPlaceAutocompleteFragment = getPlacesAutoCompleteFragment();
        
        return v;
    }

    /**
     * Creates a fragment for autocompleting a Google place API search.
     * @return new SupportPlaceAutocompleteFragment with listener set
     */
    public SupportPlaceAutocompleteFragment getPlacesAutoCompleteFragment() {

        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Location found", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Unable to find location", "An error occurred: " + status);
            }
        });

        return autocompleteFragment;
    }

}
