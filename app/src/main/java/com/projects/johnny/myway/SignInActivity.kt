package com.projects.johnny.myway

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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

class SignInActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.fragment_container)
        requestPermissions()

        Firebase.setAndroidContext(this)

        // Check for saved credentials
        Auth.CredentialsApi.request(credentialsClient, credentialRequest).setResultCallback {
            result: CredentialRequestResult ->
                if (result.status.isSuccess) {
                    // Login user automatically if password is saved
                    authenticateWithCredential(result.credential)
                } else {
                    // Proceed with SignInFragment
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, SignInFragment()) // TODO: Change to newInstance() pattern
                            .commit()
                }
        }
    }

    fun authenticateWithCredential(credential: Credential) {
        firebaseRef.authWithPassword(credential.name, credential.password,
                object: Firebase.AuthResultHandler {
                    override fun onAuthenticated(authData: AuthData) {
                        App.UID = authData.uid
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Finish activity
                    }
                    override fun onAuthenticationError(error: FirebaseError?) {
                        Toast.makeText(getApplicationContext(), "Failed to log in...", Toast.LENGTH_SHORT).show()
                        println(error?.message)
                    }
                })
    }

    fun requestPermissions() {
        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), App.locationRequestCode)
                Log.i("Location Check", "Completed")

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
