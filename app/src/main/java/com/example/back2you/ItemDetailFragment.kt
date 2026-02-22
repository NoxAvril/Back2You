package com.example.back2you

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Fragment to display detailed information about a lost/found item.
 * Uses constructor-based inflation for R.layout.fragment_item_detail.
 */
class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    private var item: PostItem? = null

    companion object {
        private const val ARG_ITEM_DATA = "item_data"

        /**
         * Static factory method to create a new instance of this fragment
         * with the required [PostItem] data.
         */
        fun newInstance(item: PostItem): ItemDetailFragment {
            return ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM_DATA, item)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the parcelable data passed from HomeFragment
        item = arguments?.getParcelable(ARG_ITEM_DATA)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. View Binding
        val tvTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvDesc = view.findViewById<TextView>(R.id.tvDetailDescription)
        val tvUser = view.findViewById<TextView>(R.id.tvUserName)
        val ivImage = view.findViewById<ImageView>(R.id.ivItemDetailImage)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvContactDetails)
        val userContainer = view.findViewById<LinearLayout>(R.id.user_info_container)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnBackArrow = view.findViewById<ImageButton>(R.id.btnBackArrow)

        // 2. Data Binding Logic
        item?.let { data ->
            tvTitle.text = data.title
            tvDesc.text = data.description
            tvUser.text = data.finderName
            ivImage.setImageResource(data.imageResId)

            // Setup the contacts list in non-editable mode for public view
            setupContactsRecyclerView(rvContacts, data.contacts)
        }

        // 3. Centralized Navigation Logic
        val handleBackNavigation = View.OnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Set listeners for both back buttons (bottom button and top arrow)
        btnBack?.setOnClickListener(handleBackNavigation)
        btnBackArrow?.setOnClickListener(handleBackNavigation)

        // Navigate to the Finder's Public Profile
        userContainer?.setOnClickListener {
            navigateToUserProfile(item?.finderName ?: "Unknown")
        }
    }

    private fun setupContactsRecyclerView(recyclerView: RecyclerView, contacts: List<ContactItem>) {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Passing 'false' to isEditMode hides the delete icons in your ContactAdapter
        recyclerView.adapter = ContactAdapter(contacts.toMutableList(), false) {
            // No-op: Users cannot delete contacts from the detail view
        }
    }

    private fun navigateToUserProfile(userName: String) {
        val userFragment = UserProfileFragment().apply {
            arguments = Bundle().apply {
                putString("userName", userName)
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, userFragment)
            .addToBackStack(null)
            .commit()
    }
}