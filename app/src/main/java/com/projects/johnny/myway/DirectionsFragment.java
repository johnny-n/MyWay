package com.projects.johnny.myway;

import android.animation.Animator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.maps.GeoApiContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;

// TODO: Fix bug where app crash when requesting permissions for accessing location's device for the first time.

public class DirectionsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // An instance of GeoApiContext is required to use Google Maps API
    final GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyAXSGlwghZsgIFWY2PIBYeyAD-Opq0pP2g");

    // Used as a placeholder in Firebase.
    // We overwrite this in Firebase to activite the listener for updates
    public static final String FIREBASE_REFRESH_PLACEHOLDER = "REFRESH";
    private static final String DIALOG_ADDRESS = "dialog_address";
    private static final int LOCATION_REQUEST_CODE = 2;

    public static DirectionsFragment newInstance() {
        return new DirectionsFragment();
    }

    private GoogleApiClient mGoogleApiClient;

    private Double lat;
    private Double lng;

    private RevealFrameLayout mContainer;
    private FrameLayout mCircularRevealView;
    private TextView mAddPlaceTextView;
    private FloatingActionButton mFabAddLocation;
    private boolean mIsFabRotated;

    private RecyclerView mRecyclerView;
    private DirectionAdapter mDirectionAdapter;
    private ArrayList<MyLocation> locations;
    private Firebase mFirebaseRef;

    private EtaHandlerThread<DirectionItemViewHolder> mEtaHandlerThread;

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

        // Goes to activity that notifies user to turn on locations
        if (!isLocationEnabled(getActivity())) {
            Intent intent = new Intent(getActivity(), RequestLocationActivity.class);
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            getActivity().finish();
        }

        /**
         * Note: You need to explicitly check in code whether location permissions are granted. If they are
         *       not granted, then you need to call Activity.Compat.requestpermissions to request it.
         */
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        System.out.println("Location is..." + isLocationEnabled(getActivity()));

        if (isLocationEnabled(getActivity())) {
            // Get last known location after performing explicit permission check
            final Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            // Get latitude and longitude of last known location
            lat = location.getLatitude();
            lng = location.getLongitude();

            // Set up Handler & HandlerThread to do background task
            final Handler responseHandler = new Handler();
            mEtaHandlerThread = new EtaHandlerThread<>(responseHandler, context, lat, lng);
            mEtaHandlerThread.setEtaInterface(new EtaHandlerThread.EtaInterface() {
                @Override
                public void setInformation(DirectionItemViewHolder viewHolder, String travelTime) {
                    viewHolder.setEtaTime(travelTime);
                }
            });
            mEtaHandlerThread.start();
            mEtaHandlerThread.getLooper();

            // We set up the adapter if and only if location places are set;
            mDirectionAdapter = new DirectionAdapter(locations);
            mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mDirectionAdapter);

            // for touch events on RecyclerView
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mDirectionAdapter, getActivity());
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRecyclerView);

        }

        mContainer = (RevealFrameLayout) v.findViewById(R.id.container);
        mCircularRevealView = (FrameLayout) v.findViewById(R.id.circular_reveal_view);

        mAddPlaceTextView = (TextView) v.findViewById(R.id.add_place_text_view);
        mAddPlaceTextView.setOnClickListener(fabOnClickListener());

        mFabAddLocation = (FloatingActionButton) v.findViewById(R.id.add_location_fab);
        mIsFabRotated = false;
        mFabAddLocation.setOnClickListener(fabOnClickListener());
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        updateUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateUI() {
        if (isLocationEnabled(getActivity())) {
            mFirebaseRef.child("Locations").child(FIREBASE_REFRESH_PLACEHOLDER).setValue("Refresh");
            mDirectionAdapter.updateLocations(locations);
            mDirectionAdapter.notifyDataSetChanged();

            if (mDirectionAdapter.getItemCount() == 0) {
                Log.i("Item Count", String.valueOf(mDirectionAdapter.getItemCount()));
                mRecyclerView.setVisibility(View.GONE);
                mAddPlaceTextView.setVisibility(View.VISIBLE);
            } else {
                mRecyclerView.setVisibility(View.VISIBLE);
                mAddPlaceTextView.setVisibility(View.GONE);
            }
        }
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

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mEtaHandlerThread.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEtaHandlerThread.clearQueue();
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

    @Override
    public void onResume() {
        super.onResume();
        mCircularRevealView.setVisibility(View.INVISIBLE);
    }

    // You must explicitly tell the FragmentManager that your fragment should receive
    // a call to onCreateOptionsMenu(...) inside onCreate(...)
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

    public void animateFab() {
        float rotation;
        if (mIsFabRotated) {
            rotation = 0f;
        } else {
            rotation = 225f;
        }
        mFabAddLocation.animate()
                .rotation(rotation)
                .setDuration(300)
                .start();
        mIsFabRotated = !mIsFabRotated;
    }

    public View.OnClickListener fabOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

                // Determine center position for circular reveal
                final int x;
                final int y;
                // Position center of circular reveal depending on text or FAB clicked
                if (v.getId() == mAddPlaceTextView.getId()) {
                    x = v.getRight() / 2;
                    y = v.getBottom() / 2;
                } else {
                    x = v.getRight();
                    y = v.getBottom();
                }

                // Determine radius sizes
                final int containerWidth = mContainer.getWidth();
                final int containerHeight = mContainer.getHeight();

                final float startingRadius = 0;
                final float endRadius = (float) Math.sqrt((containerWidth * containerWidth) + (containerHeight * containerHeight));

                final Animator animator = ViewAnimationUtils.createCircularReveal(mCircularRevealView, x, y, startingRadius, endRadius);
                animator.setDuration(450);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        final FragmentManager fm = getFragmentManager();
                        Fragment fragment = AddLocationFragment.newInstance();
                        fm.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                        // This view will still be visible if user presses the back button, but not the up button.
                        mFabAddLocation.setEnabled(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mCircularRevealView.setVisibility(View.VISIBLE);
                animator.start();
            }
        };
    }

    // ViewHolder for a each location in list item
    public class DirectionItemViewHolder extends RecyclerView.ViewHolder {

        TextView mAddress;
        TextView mTravelTimeTextView;
        Button mNavigatorButton;
        ImageView mDisplayAddressIcon;
        String mTitleOfPlace;

        public DirectionItemViewHolder(View itemView) {
            super(itemView);
            mAddress = (TextView) itemView.findViewById(R.id.address_item_text_view);
            mTravelTimeTextView = (TextView) itemView.findViewById(R.id.travel_time_item_text_view);
            mNavigatorButton = (Button) itemView.findViewById(R.id.navigator_item_button);
            mNavigatorButton.playSoundEffect(SoundEffectConstants.CLICK);
            mDisplayAddressIcon = (ImageView) itemView.findViewById(R.id.display_address_icon);
        }

        public void setListItems(MyLocation myLocation) {
            String locationName = myLocation.getNameOfPlace();
            mTitleOfPlace = locationName;
            final String locationAddress = myLocation.getAddress();

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

            mDisplayAddressIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final FragmentManager fm = getFragmentManager();
                    AddressDialogFragment addressDialogFragment = AddressDialogFragment.newInstance(locationAddress);
                    addressDialogFragment.show(fm, DIALOG_ADDRESS);
                }
            });
        }

        public void setEtaTime(String travelTime) {
            mTravelTimeTextView.setText(travelTime);
        }

        public String getTitleOfPlace() {
            return mTitleOfPlace;
        }
    }

    public class DirectionAdapter extends RecyclerView.Adapter<DirectionItemViewHolder> implements ItemTouchHelperAdapter {

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
            mEtaHandlerThread.requestEtaTime(holder, mLocations.get(position));
            if (mDirectionAdapter.getItemCount() == 0) {
                Log.i("Item Count", String.valueOf(mDirectionAdapter.getItemCount()));
                mRecyclerView.setVisibility(View.GONE);
                mAddPlaceTextView.setVisibility(View.VISIBLE);
            } else {
                mRecyclerView.setVisibility(View.VISIBLE);
                mAddPlaceTextView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mLocations.size();
        }

        // Determines the movement swipe gestures to detect
        // You must override getMovementFlags() to specify which directions of drags and swipes are supported.
        // Use the helper ItemTouchHelper.makeMovementFlags(int, int) to build the returned flags.
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

            return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mLocations, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mLocations, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            mLocations.remove(position);
            notifyItemRemoved(position);
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