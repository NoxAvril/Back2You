package com.example.back2you

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class PostItem(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val finderName: String? = null,
    val finderUid: String? = null,
    val imageUrl: String? = null
) : Parcelable {
    // Required empty constructor for Firebase
    constructor() : this(null, null, null, null, null, null, null)
}