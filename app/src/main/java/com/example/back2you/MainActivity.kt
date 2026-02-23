package com.example.back2you

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        applyTheme(isDarkMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        bottomNav = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            checkUserStatus()
        }

        bottomNav.setOnItemSelectedListener { item ->

            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container)

            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentFragment !is HomeFragment)
                        replaceFragment(HomeFragment())
                }

                R.id.nav_add -> {
                    if (currentFragment !is AddFragment)
                        replaceFragment(AddFragment())
                }

                R.id.nav_profile -> {
                    if (currentFragment !is ProfileFragment)
                        replaceFragment(ProfileFragment())
                }
            }
            true
        }
    }

    private fun checkUserStatus() {

        val currentUser = auth.currentUser

        if (currentUser == null) {
            hideBottomNav()
            replaceFragment(LoginFragment())
        } else {

            currentUser.reload()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        showBottomNav()
                        replaceFragment(HomeFragment())
                    } else {
                        auth.signOut()
                        hideBottomNav()
                        replaceFragment(LoginFragment())
                    }
                }
        }
    }

    fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }

    fun hideBottomNav() {
        bottomNav.visibility = View.GONE
    }

    fun logout() {
        auth.signOut()
        hideBottomNav()
        replaceFragment(LoginFragment())
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun applyTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}