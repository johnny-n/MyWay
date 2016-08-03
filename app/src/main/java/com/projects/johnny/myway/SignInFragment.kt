package com.projects.johnny.myway

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.Status
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.jetbrains.anko.onClick

class SignInFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameRetry.visibility = View.GONE

        username.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val name = username.text.toString()
                val visibility = if (isValidUsername(name)) View.GONE else View.VISIBLE
                usernameRetry.visibility = visibility
            }
        }
        signUpButton.onClick { // onClick belongs to anko
            // TODO: set up listener to start SignUpFragment
        }
        signInButton.setOnClickListener {
            if (isValid()) {
                with(activity as MainActivity) {
                    val email = username.text.toString()
                    val pass = password.text.toString()
                    val credential = Credential.Builder(email)
                                        .setName(email)
                                        .setPassword(pass)
                                        .build()
                    Auth.CredentialsApi.save(credentialsClient, credential).setResultCallback {
                        status: Status ->
                        Log.d("Credentials", status.toString())
                        if (status.isSuccess) {
                            Log.i("SignInFragment", "Credentials saved!")
                            authenticateWithCredential(credential)
                        } else {
                            if (status.hasResolution()) {
                                // Try to resolve the save request. This will prompt
                                // the user if the credential is new.
                                status.startResolutionForResult(activity, MainActivity.rc_save)
                                authenticateWithCredential(credential)
                            } else {
                                Log.e("CredentialsApi.save", "Save failed!")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isValidUsername(name: CharSequence): Boolean =
            Patterns.EMAIL_ADDRESS.matcher(name).matches()

    // Checks if username & password entries are valid in length
    private fun isValid(): Boolean {
        val name = username.text.toString()
        val pass = password.text.toString()
        return if (name.length > 0 && pass.length > 0) true else false
    }
}