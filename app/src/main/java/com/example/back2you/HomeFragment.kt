package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: HomeAdapter
    private lateinit var valueEventListener: ValueEventListener

    private val itemList = mutableListOf<PostItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHome = view.findViewById<RecyclerView>(R.id.rvHome)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        adapter = HomeAdapter(itemList) { clickedItem ->
            val detailFragment = ItemDetailFragment.newInstance(clickedItem)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        rvHome.layoutManager = LinearLayoutManager(requireContext())
        rvHome.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("items")

        // Order by timestamp (make sure PostItem has "timestamp" field)
        val query = database.orderByChild("timestamp")

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()

                for (postSnapshot in snapshot.children) {
                    val item = postSnapshot.getValue(PostItem::class.java)
                    item?.let { itemList.add(it) }
                }

                // Show newest first
                itemList.reverse()

                adapter.notifyDataSetChanged()

                // Handle empty state
                if (itemList.isEmpty()) {
                    tvEmpty?.visibility = View.VISIBLE
                } else {
                    tvEmpty?.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load items: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        query.addValueEventListener(valueEventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        database.removeEventListener(valueEventListener)
    }
}