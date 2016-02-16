package com.projects.johnny.myway;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

public class DirectionsFragment extends Fragment {

    private static final String GOOGLE_MAPS_API_REQUEST = "https://maps.googleapis.com/maps/api/directions/";
    private static final int DURATION_INDEX = 1;

    EditText mToAddress;
    Button mTestButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_directions, container, false);

        // An instance of GeoApiContext is required to use Google Maps API
        final GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyAXSGlwghZsgIFWY2PIBYeyAD-Opq0pP2g");

        // Initialize views
        mToAddress = (EditText) v.findViewById(R.id.address_to_edit_text);
        mTestButton = (Button) v.findViewById(R.id.test_button);

        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create request for a Google Directions Api
                DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(context)
                        .origin("50 Washington St, Santa Clara, CA 95050")
                        .mode(TravelMode.DRIVING);

                // Set the address we want to go to
                directionsApiRequest.destination(mToAddress.getText().toString());

                try {
                    // Obtain result from api request
                    DirectionsResult result = directionsApiRequest.await();
                    // Obtain travel time deep within result
                    String travelTime = result.routes[0].legs[0].duration.humanReadable;

                    Toast.makeText(getActivity(), "Duration: " + travelTime, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Location from = new Location("Home", "50 Washington Street, Santa Clara, CA");
        Location to = new Location("School", "San Jose State University, San Jose, CA");

        return v;
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
        TextView mTravelTime;
        Button mNavigatorButton;

        public DirectionItemViewHolder(View itemView) {
            super(itemView);
            mAddress = (TextView) itemView.findViewById(R.id.address_item_text_view);
            mTravelTime = (TextView) itemView.findViewById(R.id.travel_time_item_text_view);
            mNavigatorButton = (Button) itemView.findViewById(R.id.navigator_item_button);
        }

        public void setListItems(Location location) {
            String locationName = location.getName();
            String locationAddress = location.getAddress();
            mAddress.setText(locationName);
        }
    }

    private class DirectionAdapter extends RecyclerView.Adapter<DirectionItemViewHolder> {

        @Override
        public DirectionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.direction_list_item, parent, false);
            return new DirectionItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DirectionItemViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}