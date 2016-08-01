package com.projects.johnny.myway;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private static final String LOCATIONS = "Locations";

    private final ItemTouchHelperAdapter mAdapter;
    private Activity mActivity;
    private Firebase mFirebaseRef;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, Activity activity) {
        mActivity = activity;
        mAdapter = adapter;

        // Get reference to Firebase
        App app = (App) mActivity.getApplicationContext();
        String UID = App.Companion.getUID();
        Firebase.setAndroidContext(mActivity);
        mFirebaseRef = new Firebase("https://myways.firebaseIO.com/").child(UID);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        DirectionsFragment.DirectionItemViewHolder vh = (DirectionsFragment.DirectionItemViewHolder) viewHolder;
        // Name of the location to search in Firebase for deleting
        String nameToSearchInFirebase = vh.getTitleOfPlace();
        mFirebaseRef.child(LOCATIONS).child(nameToSearchInFirebase).removeValue();
        Toast.makeText(mActivity.getApplicationContext(), "Place deleted...", Toast.LENGTH_SHORT).show();
    }

}