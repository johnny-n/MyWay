package com.projects.johnny.myway

import android.animation.Animator
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.util.Log
import android.view.*
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.firebase.client.ValueEventListener
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationServices
import com.google.maps.GeoApiContext
import kotlinx.android.synthetic.main.direction_list_item_card_view.view.*
import kotlinx.android.synthetic.main.fragment_directions.*
import java.util.*

class DirectionsFragment() : Fragment(), ConnectionCallbacks, OnConnectionFailedListener {

    companion object {
        private val arg_cred = "ARG_CRED"
        private val firebase_refresh_placeholder = "REFRESH"
        private val dialog_address = "dialog_address"
        fun newInstance(credential: Credential): DirectionsFragment {
            val fragment = DirectionsFragment()
            val args = Bundle()
            args.putParcelable(arg_cred, credential)
            fragment.arguments = args
            return fragment
        }
    }

    // Instance of GeoApiContext required to use Google Maps API
    // Using server key
    val geoApiContext = GeoApiContext().setApiKey("AIzaSyBi5TCA2EQsN0D0DXb7n0WpJ2lCWv-SHb4")
    var locations = ArrayList<MyLocation>()
    var googleApiClient: GoogleApiClient? = null
    var isFabRotated: Boolean = false
    lateinit var etaHandlerThread: EtaHandlerThread<DirectionsFragment.DirectionItemViewHolder>
    lateinit var directionAdapter: DirectionAdapter
    lateinit var credential: Credential
    lateinit var firebaseRef: Firebase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_directions, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get credential from argument
        credential = arguments.getParcelable(arg_cred)
        val uid = App.getInstance().uid
        Firebase.setAndroidContext(activity)
        firebaseRef = Firebase("https://myways.firebaseIO.com/").child(uid)

        // Add listener to update recycler view every time Firebase is updated
        firebaseRef.child("Locations")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    dataSnapshot?.let { updateLocationsWithDataSnapshot(it) }
                    updateUI()
                }

                override fun onCancelled(firebaseError: FirebaseError?) { }

            })

        // Create instance of GoogleAPIClient
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        }

        // Goes to the activity that notifies user to turn on locations if
        // the user's lcoation setting is turned off
        if (!isLocationEnabled(activity)) {
            val intent = Intent(activity, RequestLocationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
            activity.finish()
        }

        /**
         * Note: You need to explicity check in code whether location permissions are granted. If they are
         *       not granted, then you need to call Activity.Compat.requestPermissions to request it.
         */
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        println("Location is..." + isLocationEnabled(activity))

        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        // Get latitude and longitude of last known location
        val latitude = location.latitude
        val longitude = location.longitude

        // Set up Handler & HandlerThread to do background task
        val responseHandler = Handler()
        etaHandlerThread = EtaHandlerThread(responseHandler, geoApiContext, latitude, longitude)
        etaHandlerThread.setEtaInterface { viewHolder, travelTime ->
            viewHolder.etaTime = travelTime }
        etaHandlerThread.start()
        etaHandlerThread.looper

        // We set up the adapter if and only if location places are set
        directionAdapter = DirectionAdapter()
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = directionAdapter

        // For touch events on RecyclerView
        val itemTouchCallback = SimpleItemTouchHelperCallback(directionAdapter, activity)
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        updateUI()
        addLocationFab.setOnClickListener(fabOnClickListener())
    }

    fun updateUI() {
        firebaseRef.child("Locations").child(firebase_refresh_placeholder).setValue("Refresh")
        directionAdapter.updateLocations(locations)
        directionAdapter.notifyDataSetChanged()

        if (directionAdapter.itemCount == 0) {
            Log.i("Item Count:", directionAdapter.itemCount.toString())
            recyclerView.visibility = View.GONE
            addPlaceTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            addPlaceTextView.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.directions_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ic_menu_refresh -> {
                context.makeText("Refreshing...")
                updateUI()
            }
            R.id.ic_sign_out -> {
                // Delete credentials before signing out.
                val mainActivity = activity as MainActivity
                Auth.CredentialsApi.delete(mainActivity.credentialsClient, credential).setResultCallback {
                    val status = it.status
                    if (status.isSuccess) {
                        // Credential was successfully deleted
                        fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, SignInFragment())
                            .commit()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        etaHandlerThread.quit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        etaHandlerThread.quit()
    }

    override fun onStart() {
        googleApiClient?.connect()
        super.onStart()
    }

    override fun onStop() {
        googleApiClient?.disconnect()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        circularRevealView.visibility = View.INVISIBLE
    }

    fun animateFab() {
        val rotation = if (isFabRotated) 0f else 225f
        addLocationFab.animate()
            .rotation(rotation)
            .setDuration(300)
            .start()
        isFabRotated = !isFabRotated
    }

    // Updates our list of locations stored in the fragment
    fun updateLocationsWithDataSnapshot(dataSnapshot: DataSnapshot) {
        // Obtain location data as Map object and iterate through, adding to locations variable
        locations = ArrayList<MyLocation>()
        val mapDataSnapshot = dataSnapshot.value as Map<String, String>
        val iterator = mapDataSnapshot.entries.iterator()
        while (iterator.hasNext()) {
            val pair = iterator.next()
            val location = MyLocation(pair.key.toString(), pair.value.toString())
            if (!(pair.key.toString() == firebase_refresh_placeholder)) {
                locations.add(location)
            }
        }
    }

    fun isLocationEnabled(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            val locationProviders = Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
            !TextUtils.isEmpty(locationProviders)
        }

    inner class DirectionItemViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

        var titleOfPlace: String = ""
        var etaTime: String = "0"
            set(newEtaTime) {
                field = newEtaTime
                v.travelTimeItemTextView.setText(field)
            }

        fun setListItems(location: MyLocation) {
            titleOfPlace = location.nameOfPlace
            val locationAddress = location.address

            v.addressItemTextView.setText(titleOfPlace)

            // Use implicit intent to use Google Maps
            v.navigatorItemButton.setOnClickListener {
                val gmmIntentUri = Uri.parse("geo:0,0?q=$locationAddress")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
            v.displayAddressIcon.setOnClickListener {
                val addressDialogFragment = AddressDialogFragment.newInstance(locationAddress)
                val fm = this@DirectionsFragment.fragmentManager
                addressDialogFragment.show(fm, dialog_address)
//                val animatedDialogFragment = AnimatedDialogFragment()
//                val fm = this@DirectionsFragment.fragmentManager
//                animatedDialogFragment.show(fm, dialog_address)
            }
        }
    }

    inner class DirectionAdapter() : RecyclerView.Adapter<DirectionItemViewHolder>(), ItemTouchHelperAdapter {

        fun updateLocations(newLocations: ArrayList<MyLocation>) {
            locations = newLocations
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DirectionItemViewHolder {
            val inflater = LayoutInflater.from(activity)
            val view = inflater.inflate(R.layout.direction_list_item_card_view, parent, false)
            return DirectionItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: DirectionItemViewHolder, position: Int) {
            holder.setListItems(locations[position])
            etaHandlerThread.requestEtaTime(holder, locations[position])
            if (directionAdapter.itemCount == 0) {
                Log.i("Item Count:", directionAdapter.itemCount.toString())

            }
        }

        override fun getItemCount(): Int = locations.size

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            if (fromPosition < toPosition) {
                for (i in fromPosition.until(toPosition)) {
                    Collections.swap(locations, i, i + 1)
                }
            } else {
                for (i in toPosition.until(fromPosition)) {
                    Collections.swap(locations, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onItemDismiss(position: Int) {
            locations.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onConnected(p0: Bundle?) {
        Log.d("GoogleApiClient", "Successfully connected to GoogleApiClient!")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e("GoogleApiClient", "Connection to GoogleApiClient suspended.")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e("GoogleApiClient", connectionResult.errorMessage)
    }

    fun fabOnClickListener(): View.OnClickListener = View.OnClickListener { view ->
        view.isEnabled = false

        // Determine center of circular reveal depending on text or FAB clicked
        val x: Int
        val y: Int
        if (view.id == addPlaceTextView.id) {
            x = view.right / 2
            y = view.bottom / 2
        } else {
            x = view.right
            y = view.bottom
        }

        // Container width/height are radius sizes
        val startingRadius = 0.toFloat()
        val endRadius = Math.sqrt(((container.width * container.width)
                + (container.height * container.height)).toDouble()).toFloat()

        val animator = ViewAnimationUtils.createCircularReveal(circularRevealView, x, y, startingRadius, endRadius)
        animator.duration = 450
        animator.addListener(object: Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                val fragment = AddLocationFragment.newInstance()
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
                addLocationFab.isEnabled = true
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        circularRevealView.visibility = View.VISIBLE
        animator.start()
    }
}