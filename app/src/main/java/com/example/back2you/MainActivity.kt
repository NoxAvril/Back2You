package com.example.back2you

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Load theme BEFORE super.onCreate to prevent flickering
        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        applyTheme(isDarkMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_add -> replaceFragment(AddFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
    }

    // This is called from the Fragment to save and switch themes
    fun toggleTheme() {
        val sharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("DarkMode", false)
        val newMode = !isDarkMode

        sharedPref.edit().putBoolean("DarkMode", newMode).apply()
        applyTheme(newMode)
    }

    private fun applyTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}