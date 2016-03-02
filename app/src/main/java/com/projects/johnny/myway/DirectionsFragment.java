package com.projects.johnny.myway;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DirectionsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // An instance of GeoApiContext is required to use Google Maps API
    final GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyAXSGlwghZsgIFWY2PIBYeyAD-Opq0pP2g");

    // Used as a placeholder in Firebase.
    // We overwrite this in Firebase to activite the listener for updates
    private static final String FIREBASE_REFRESH_PLACEHOLDER = "REFRESH";
    private static final int LOCATION_REQUEST_CODE = 2;

    private GoogleApiClient mGoogleApiClient;

    private Double lat;
    private Double lng;

    private RecyclerView mRecyclerView;
    private DirectionAdapter mDirectionAdapter;
    private ArrayList<MyLocation> locations;
    private Firebase mFirebaseRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_directions, container, false);
        locations = new ArrayList<>();

        // Get reference to Firebase
        App app = (App) getActivity().getApplicationContext();
        String UID = app.getUID();
        Firebase.setAndroidContext(getActivity());
        mFirebaseRef = new Firebase("https://myways.firebaseIO.com/").child(UID);

        // Add listener to update recycler view every time Firebase is updated
        mFirebaseRef.child("Locations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateLocationsWithDataSnapshot(dataSnapshot);
                updateUI();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Create instance of GoogleAPIClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        /**
         * Note: You need to explicitly check in code whether location permissions are granted. If they are
         *       not granted, then you need to call Activity.Compat.requestpermissions to request it.
         */
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( getActivity(), new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION  }, LOCATION_REQUEST_CODE );
            Log.i("Location Check", "Completed");
        }

        System.out.println("Location is..." + isLocationEnabled(getContext()));

        if (isLocationEnabled(getContext())) {
            // Get last known location after performing explicit permission check
            // TODO: Fix bug where app crashes when device's location setting is turned off
            final Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            // Get latitude and longitude of last known location
            lat = location.getLatitude();
            lng = location.getLongitude();
        }

        mDirectionAdapter = new DirectionAdapter(locations);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mDirectionAdapter);


        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        updateUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateUI() {
        mFirebaseRef.child("Locations").child(FIREBASE_REFRESH_PLACEHOLDER).setValue("Refresh");
        mDirectionAdapter.updateLocations(locations);
        mDirectionAdapter.notifyDataSetChanged();
    }

    // Updates our list of locations stored in the fragment
    private void updateLocationsWithDataSnapshot(DataSnapshot dataSnapshot) {
        // Obtain location data as Map object and iterate through, adding to locations variable
        locations = new ArrayList<>();
        Map<String, String> mLocations = (Map<String, String>) dataSnapshot.getValue();
        Iterator it = mLocations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            MyLocation location = new MyLocation(pair.getKey().toString(), pair.getValue().toString());
            if (!pair.getKey().toString().equals(FIREBASE_REFRESH_PLACEHOLDER)) {
                locations.add(location);
            }
        }
    }

    // Returns true/false depending on whether locations is turned on or off.
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    // You must explicitly tell the FragmentManager that your fragment should receive
    // a call to onCrateOptionsMenu(...) inside onCreate(...)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.directions_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_menu_refresh:
                Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
                updateUI();
                return true;
            case R.id.ic_add_location:
                Intent addLocationIntent = new Intent(getActivity(), AddLocationActivity.class);
                startActivity(addLocationIntent);
                return true;
            case R.id.ic_sign_out:
                App app = (App) getActivity().getApplicationContext();
                app.setUID(null);
                Intent signOutIntent = new Intent(getActivity(), SignInActivity.class);
                startActivity(signOutIntent);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ViewHolder for a each location in list item
    private class DirectionItemViewHolder extends RecyclerView.ViewHolder {

        TextView mAddress;
        TextView mTravelTimeTextView;
        Button mNavigatorButton;

        public DirectionItemViewHolder(View itemView) {
            super(itemView);
            mAddress = (TextView) itemView.findViewById(R.id.address_item_text_view);
            mTravelTimeTextView = (TextView) itemView.findViewById(R.id.travel_time_item_text_view);
            mNavigatorButton = (Button) itemView.findViewById(R.id.navigator_item_button);
            mNavigatorButton.playSoundEffect(SoundEffectConstants.CLICK);
        }

        public void setListItems(MyLocation myLocation) {
            String locationName = myLocation.getNameOfPlace();
            final String locationAddress = myLocation.getAddress();

            // Create request for GoogleDirectionsApi
            // Last known location is set here
            DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(context)
                    .origin(new LatLng(lat, lng))
                    .mode(TravelMode.DRIVING);

            directionsApiRequest.destination(locationAddress);
            DirectionsResult result;
            try {
                // Obtain result from api request
                result = directionsApiRequest.await();
                // Obtain travel time deep within result
                String travelTime = result.routes[0].legs[0].duration.humanReadable;
                mTravelTimeTextView.setText(travelTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mAddress.setText(locationName);

            // Use implicit intent to use google maps
            mNavigatorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + locationAddress);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }
    }

    private class DirectionAdapter extends RecyclerView.Adapter<DirectionItemViewHolder> {

        ArrayList<MyLocation> mLocations;

        public DirectionAdapter(ArrayList<MyLocation> locations) {
            mLocations = locations;
        }

        public void updateLocations(ArrayList<MyLocation> locations) {
            mLocations = locations;
        }

        @Override
        public DirectionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.direction_list_item_card_view, parent, false);
            return new DirectionItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DirectionItemViewHolder holder, int position) {
            holder.setListItems(mLocations.get(position));
        }

        @Override
        public int getItemCount() {
            return mLocations.size();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("GoogleApiClient", "Successfully connected to GoogleApiClient!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("GoogleApiClient", "Connection to GoogleApiClient suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("GoogleApiClient", connectionResult.getErrorMessage());
    }
}