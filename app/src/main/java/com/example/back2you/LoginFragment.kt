package com.example.back2you

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val etEmail = view.findViewById<TextInputEditText>(R.id.etLoginEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etLoginPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegisterLink)

        tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter valid email"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Minimum 6 characters"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(
                            requireContext(),
                            "Login successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        (requireActivity() as MainActivity).showBottomNav()

                        (requireActivity() as MainActivity)
                            .replaceFragment(HomeFragment())

                    } else {

                        val message = when (task.exception) {

                            is FirebaseAuthInvalidCredentialsException ->
                                "Invalid email or password"

                            else ->
                                "Login failed. Please try again."
                        }

                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}