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
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),

    var returned: Boolean = false,
    var returnTimestamp: Long? = null

) : Parcelable {
    constructor() : this(
        null, null, null, null,
        null, null, null,
        0L, false, null
    )
}