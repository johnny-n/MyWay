package com.projects.johnny.myway;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class DirectionsFragment extends Fragment {

    Button mMiddleButton;
    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_directions, container, false);



        return v;
    }

    // You must explicitly tell the FragmentManager that your fragment should receive
    // a call to onCrateOptionsMenu(...)


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