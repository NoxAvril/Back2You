package com.example.back2you

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 1. Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser

        // 2. Initialize UI views
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // 3. Inject dynamic data
        if (currentUser != null) {
            // If displayName is null (not set during signup), show a fallback
            tvName.text = currentUser.displayName ?: "Authenticated User"
            tvEmail.text = currentUser.email
        }

        // 4. Handle Logout logic
        btnLogout.setOnClickListener {
            auth.signOut()
            // Navigate back to Login (Ensure R.id.fragment_container is in your activity_main)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }

        return view
    }
}