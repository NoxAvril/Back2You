package com.example.back2you

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter(
    private val items: List<PostItem>,
    private val onItemClick: (PostItem) -> Unit
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivItemImage)
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvFinder: TextView = view.findViewById(R.id.tvFinderName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvFinder.text = "Found by: ${item.finderName}"
        holder.ivImage.setImageResource(item.imageResId)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}