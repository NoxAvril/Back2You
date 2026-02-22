package com.example.back2you

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostItem(
    val title: String,
    val description: String,
    val finderName: String,
    val imageResId: Int, // Local resource ID for now
    val contacts: List<ContactItem>
) : Parcelable