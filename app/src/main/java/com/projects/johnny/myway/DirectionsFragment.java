package com.projects.johnny.myway;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class DirectionsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // An instance of GeoApiContext is required to use Google Maps API
    final GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyAXSGlwghZsgIFWY2PIBYeyAD-Opq0pP2g");

    private static final int LOCATION_REQUEST_CODE = 2;

    private GoogleApiClient mGoogleApiClient;

    private Double lat;
    private Double lng;

    private RecyclerView mRecyclerView;

    MyLocation[] locations = {
            new MyLocation("Home", "50 Washington Street, Santa Clara, CA"),
            new MyLocation("School", "San Jose State University, San Jose, CA"),
            new MyLocation("Parents", "2641 Quail Dr, Union City, CA"),
            new MyLocation("Work", "2089 El Camino Real, Santa Clara, CA"),
            new MyLocation("Target",  "533 Coleman Ave, San Jose, CA"),
            new MyLocation("Trader Joes", "Coleman Ave, San Jose, CA"),
            new MyLocation("Japantown", "618 N 3rd St, San Jose, CA"),
            new MyLocation("Henry's House", "Alexis Circle, Daly City, CA")
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_directions, container, false);

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
        // Get last known location after performing explicit permission check
        // TODO: Fix bug where app crashes when device's location setting is turned off
        final Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        // Get latitude and longitude of last known location
        lat = location.getLatitude();
        lng = location.getLongitude();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new DirectionAdapter());

        return v;
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
            String locationAddress = myLocation.getAddress();

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
        }
    }

    private class DirectionAdapter extends RecyclerView.Adapter<DirectionItemViewHolder> {

        @Override
        public DirectionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.direction_list_item_card_view, parent, false);
            return new DirectionItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DirectionItemViewHolder holder, int position) {
            holder.setListItems(locations[position]);
        }

        @Override
        public int getItemCount() {
            return locations.length;
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