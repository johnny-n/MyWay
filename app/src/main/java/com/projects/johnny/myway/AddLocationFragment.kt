package com.projects.johnny.myway

import android.animation.Animator
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.firebase.client.Firebase
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import io.codetail.animation.ViewAnimationUtils
import kotlinx.android.synthetic.main.fragment_add_location.*
import org.jetbrains.anko.enabled

class AddLocationFragment : Fragment() {

    companion object {
        fun newInstance() = AddLocationFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_add_location, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        // Firebase stuff
        val uid = App.getInstance().uid
        Firebase.setAndroidContext(activity.applicationContext)
        val firebaseRef = Firebase("https://myways.firebaseIO.com/").child(uid).child("Locations");
        addPlaceButton.enabled = false

        // Because we are INSIDE fragment and we are FINDING a fragment, we need to use childFragmentManager
        val autocompleteFrag = childFragmentManager.findFragmentById(R.id.placeAutocompleteFragment) as PlaceAutocompleteFragment
        var locationAddress: String? = null
        autocompleteFrag.setHint("Search Places")
        autocompleteFrag.setOnPlaceSelectedListener(object: PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                locationAddress = place.address.toString()
                autocompleteFrag.setText(locationAddress)
                // Enable utton after selecting a place
                // TODO: Gray out button when it is disabled?
                addPlaceButton.enabled = true
            }
            override fun onError(status: Status) {
                Log.e(tag, "An error occured: $status")
            }
        })

        addPlaceButton.setOnClickListener {
            // Hide keyboard
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(nickname.windowToken, 0)

            val nickname = if (nickname.text.length > 0) nickname.text.toString() else locationAddress
            firebaseRef.child(nickname).setValue(locationAddress)
            fragmentManager.popBackStack()
        }

        // Animation stuff
        addLocationContainer.post {
            with(addLocationContainer) {
                // Determine center position for circular reveal (fab button)
                // Radius sizes = width, height of addLocationContainer, respectively
                val x = right / 2
                val y = bottom / 2

                val startingRadius = Math.sqrt((width * width).toDouble() + (height * height).toDouble()).toFloat()
                val endRadius = 0.0f

                val animator = ViewAnimationUtils.createCircularReveal(circularRevealView, x, y, startingRadius, endRadius)
                animator.setDuration(400)
                animator.addListener(object: Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) { }

                    override fun onAnimationEnd(animation: Animator?) {
                        circularRevealView.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(animation: Animator?) { }

                    override fun onAnimationStart(animation: Animator?) { }
                })
                animator.start()
            }
        }
    }
}