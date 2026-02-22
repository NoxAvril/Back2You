package com.example.back2you

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HomeAdapter(
    private val items: List<PostItem>,
    private val onItemClick: (PostItem) -> Unit
) : RecyclerView.Adapter<HomeAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivItemImage: ImageView = itemView.findViewById(R.id.ivItemImage)
        val tvItemTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvItemType: TextView = itemView.findViewById(R.id.tvItemType)
        val tvFinderName: TextView = itemView.findViewById(R.id.tvFinderName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = items[position]

        holder.tvItemTitle.text = item.title ?: "No Title"
        holder.tvItemType.text = item.type ?: "Unknown Type"
        holder.tvFinderName.text = item.finderName ?: "Unknown Finder"

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.ivItemImage)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}