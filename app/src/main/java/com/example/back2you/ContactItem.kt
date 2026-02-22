package com.example.back2you

data class ContactItem(
    var id: String = "",
    var type: String = "",
    var value: String = "",
    var isPublic: Boolean = true
)