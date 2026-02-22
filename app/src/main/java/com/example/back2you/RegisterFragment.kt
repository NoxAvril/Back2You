package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUser = view.findViewById<TextInputEditText>(R.id.etRegUsername)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPass = view.findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirm = view.findViewById<TextInputEditText>(R.id.etRegConfirmPassword)
        val btnReg = view.findViewById<Button>(R.id.btnRegister)

        btnReg.setOnClickListener {
            val username = etUser.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPass.text.toString()
            val confirm = etConfirm.text.toString()

            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create User
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update DisplayName with Username
                        val profileUpdates = userProfileChangeRequest {
                            displayName = username
                        }

                        auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show()
                            // Go back to Login or Home
                            parentFragmentManager.popBackStack()
                        }
                    } else {
                        Toast.makeText(context, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}