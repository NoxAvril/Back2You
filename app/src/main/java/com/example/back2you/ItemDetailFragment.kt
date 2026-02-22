package com.example.back2you

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    companion object {
        private const val ARG_ITEM = "selected_item"

        fun newInstance(item: PostItem): ItemDetailFragment {
            val fragment = ItemDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_ITEM, item)
            fragment.arguments = args
            return fragment
        }
    }

    private var selectedItem: PostItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_ITEM, PostItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_ITEM)
        }

        val ivImage = view.findViewById<ImageView>(R.id.ivItemImage)
        val tvTitle = view.findViewById<TextView>(R.id.tvItemTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvItemDescription)
        val tvFinderName = view.findViewById<TextView>(R.id.tvFinderName)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnViewProfile = view.findViewById<Button>(R.id.btnViewProfile)

        selectedItem?.let { item ->

            tvTitle.text = item.title ?: "No Title"
            tvDescription.text = item.description ?: "No Description"
            tvFinderName.text = item.finderName ?: "Unknown Finder"

            // 🔥 IMAGE LOGIC
            if (!item.imageUrl.isNullOrEmpty()) {
                ivImage.visibility = View.VISIBLE

                Glide.with(this)
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(ivImage)
            } else {
                ivImage.visibility = View.GONE
            }

            btnViewProfile.setOnClickListener {
                val profileFragment =
                    UserProfileFragment.newInstance(
                        item.finderUid ?: "",
                        item.finderName ?: "User"
                    )

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}