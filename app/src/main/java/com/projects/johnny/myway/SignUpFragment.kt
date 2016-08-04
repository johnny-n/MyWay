package com.projects.johnny.myway

import android.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignUpFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameRetryText.visibility = View.GONE
        passwordRetryText.visibility = View.GONE

        username.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                usernameRetryText.visibility = getVisibility(isValidUsername())
            }
        }
        // Resets password confirm entry field
        password.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                passwordConfirm.setText("")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        // Checks if both passwords fields match, alert user if they do not
        password.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                passwordRetryText.visibility = getVisibility(passwordsMatch())
            }
        }
        signUpCompleteButton.setOnClickListener {
            if (isValidForm()) {
                val email = username.text.toString()
                val pass  = password.text.toString()
                val firebaseRef = Firebase("https://myways.firebaseIO.com/")
                firebaseRef.createUser(email, pass, object: Firebase.ValueResultHandler<Map<String, Any>> {
                    override fun onSuccess(result: Map<String, Any>) {
                        val uid = result.get("uid") as String
                        firebaseRef.setValue(uid)
                        var firebaseRefChild = firebaseRef.child(uid)

                        firebaseRefChild = firebaseRefChild.child("Name")
                        firebaseRefChild.child("FirstName").setValue(firstName.text.toString())
                        firebaseRefChild.child("LastName").setValue(lastName.text.toString())

                        fragmentManager.popBackStack()
                    }
                    override fun onError(error: FirebaseError) {
                        Toast.makeText(activity, error.message, Toast.LENGTH_SHORT)
                    }

                })
            }
        }
    }

    private fun isValidForm(): Boolean {
        val firstNameValid = firstName.text.toString().length > 0
        val lastNameValid = lastName.text.toString().length > 0
        return firstNameValid && lastNameValid && passwordsMatch() && isValidUsername()
    }

    private fun passwordsMatch(): Boolean = password.text == passwordConfirm.text

    private fun isValidUsername(): Boolean {
        val email = username.text.toString()
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun getVisibility(isValid: Boolean) = if (isValid) View.GONE else View.INVISIBLE
}