package com.example.back2you

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: HomeAdapter
    private val itemList = mutableListOf<PostItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHome = view.findViewById<RecyclerView>(R.id.rvHome)

        // Navigation logic using the Parcelable Item
        adapter = HomeAdapter(itemList) { clickedItem ->
            val detailFragment = ItemDetailFragment.newInstance(clickedItem)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        rvHome.layoutManager = LinearLayoutManager(context)
        rvHome.adapter = adapter

        // Fetching items from Firebase
        database = FirebaseDatabase.getInstance().getReference("items")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (postSnapshot in snapshot.children) {
                    val item = postSnapshot.getValue(PostItem::class.java)
                    item?.let { itemList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}