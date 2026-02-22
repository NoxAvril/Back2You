package com.example.back2you

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val rvHome = view.findViewById<RecyclerView>(R.id.rvHome)

        // Mock Data
        val itemList = listOf(
            PostItem("Blue Wallet", "Found near the park. Has a library card inside.", "Alice", R.drawable.ic_launcher_background,
                listOf(ContactItem("Phone", "123456"), ContactItem("WhatsApp", "123456"))),
            PostItem("Car Keys", "Toyota keys found in the cafeteria.", "Bob", R.drawable.ic_launcher_background,
                listOf(ContactItem("Email", "bob@example.com")))
        )

        rvHome.layoutManager = LinearLayoutManager(context)
        rvHome.adapter = HomeAdapter(itemList) { clickedItem ->
            val detailFragment = ItemDetailFragment.newInstance(clickedItem)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}