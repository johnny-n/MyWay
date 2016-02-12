package com.projects.johnny.myway;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

/**
 * Created by Johnny on 2/9/16.
 */
public class AddLocationActivity extends AppCompatActivity {

    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        // Set up Firebase in Android
        // Must be initialized once with an Android context
        Firebase.setAndroidContext(this);
        final Firebase myFirebaseRef = new Firebase("https://myways.firebaseio.com/");

        // Obtain FragmentManager
        android.app.FragmentManager fm = getFragmentManager();
        // Get reference to PlaceAutocomplete UI widget
        mPlaceAutocompleteFragment = (PlaceAutocompleteFragment) fm.findFragmentById(R.id.place_autocomplete_fragment);
        // Set listener for autocomplete widget
        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i("Location found", "Place: " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Unable to find location", "An error occurred: " + status);
            }
        });
    }
}
