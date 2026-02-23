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

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val etName = view.findViewById<TextInputEditText>(R.id.etRegUsername)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etRegConfirmPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = view.findViewById<TextView>(R.id.tvLoginLink)

        tvLoginLink.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnRegister.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Username required"
                return@setOnClickListener
            }

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter valid email"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Minimum 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser!!
                        val uid = user.uid

                        // 🔥 Save username to Realtime Database
                        val userMap = mapOf(
                            "username" to name
                        )

                        com.google.firebase.database.FirebaseDatabase
                            .getInstance()
                            .getReference("users")
                            .child(uid)
                            .setValue(userMap)
                            .addOnSuccessListener {

                                Toast.makeText(
                                    context,
                                    "Registration Successful",
                                    Toast.LENGTH_SHORT
                                ).show()

                                (requireActivity() as MainActivity).showBottomNav()

                                (requireActivity() as MainActivity)
                                    .replaceFragment(HomeFragment())
                            }
                    }
                }
        }
    }
}