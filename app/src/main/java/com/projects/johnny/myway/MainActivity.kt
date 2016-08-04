package com.projects.johnny.myway

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Toast
import com.firebase.client.AuthData
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.credentials.CredentialRequestResult
import com.google.android.gms.auth.api.credentials.IdentityProviders
import com.google.android.gms.common.api.GoogleApiClient

class MainActivity : AppCompatActivity() {

    companion object {
        val locationRequestCode = 2
        val rc_save = 12345
    }

    val firebaseRef: Firebase by lazy { Firebase("https://myways.firebaseIO.com/") }

    val credentialsClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
                .addApi(Auth.CREDENTIALS_API)
                .build()
    }

    val credentialRequest: CredentialRequest by lazy {
        CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()

        Firebase.setAndroidContext(this)
        credentialsClient.connect()

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, SignInFragment()) // TODO: Pass credentials to directionsFragment?
                        .commit()

        // TODO: Handle situation where user decides to "never" save credentials using Google smart lock
        // Check for saved credentials
        Auth.CredentialsApi.request(credentialsClient, credentialRequest).setResultCallback {
            result: CredentialRequestResult ->
            if (result.status.isSuccess) {
                // Login user automatically if password is saved
                Log.d("SignInActivity", "Result success, authenticating user...")
                authenticateWithCredential(result.credential)
            }
            // Should be no resolution required since no crednetial exists for request.
        }
    }

    override fun onBackPressed() {

        val count = fragmentManager.backStackEntryCount

        // pops the back stack if there are fragments alive there
        // do nothing otherwise (meaning, dont exit app if we're inside DirectionsFragment
        if (count == 0) null else fragmentManager.popBackStack()
    }

    fun authenticateWithCredential(credential: Credential) {
        firebaseRef.authWithPassword(credential.name, credential.password,
                object: Firebase.AuthResultHandler {
                    override fun onAuthenticated(authData: AuthData) {
                        App.getInstance().uid = authData.uid
                        // TODO: Move fragment transaction to be peformed on main thread?
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, DirectionsFragment.newInstance(credential))
                                .commitAllowingStateLoss()
                    }
                    override fun onAuthenticationError(error: FirebaseError?) {
                        Toast.makeText(applicationContext, "Failed to log in...", Toast.LENGTH_SHORT).show()
                        println(error?.message)
                    }
                })
    }

    fun requestPermissions() {
        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), locationRequestCode)
                Log.i("Location Check", "Completed")

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}