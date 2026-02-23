package com.example.back2you

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*

class AddFragment : Fragment(R.layout.fragment_add) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private var imageUri: Uri? = null

    private lateinit var etTitle: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var layoutPhoto: LinearLayout
    private lateinit var ivItemPreview: ImageView
    private lateinit var btnSubmit: Button

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                imageUri = it.data?.data
                ivItemPreview.setImageURI(imageUri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTitle = view.findViewById(R.id.etTitle)
        rgType = view.findViewById(R.id.rgType)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etDescription = view.findViewById(R.id.etDescription)
        layoutPhoto = view.findViewById(R.id.layoutPhotoUpload)
        ivItemPreview = view.findViewById(R.id.ivItemPreview)
        val btnUpload = view.findViewById<Button>(R.id.btnUploadPhoto)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        // Spinner setup
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.item_categories,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Image picker
        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        // Show photo section only for Found
        rgType.setOnCheckedChangeListener { _, checkedId ->
            layoutPhoto.visibility =
                if (checkedId == R.id.rbFound) View.VISIBLE else View.GONE
        }

        btnSubmit.setOnClickListener { submitPost() }
    }

    private fun submitPost() {

        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val selectedTypeId = rgType.checkedRadioButtonId
        val category = spinnerCategory.selectedItem?.toString() ?: ""
        val currentUser = auth.currentUser ?: return

        if (title.isEmpty()) {
            etTitle.error = "Item name is required"
            etTitle.requestFocus()
            return
        }

        if (selectedTypeId == -1) {
            Toast.makeText(requireContext(), "Please select Lost or Found", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (selectedTypeId == R.id.rbLost) "Lost" else "Found"

        if (spinnerCategory.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (type == "Found" && imageUri == null) {
            Toast.makeText(requireContext(), "Photo is required for Found items", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false

        val itemId = database.child("items").push().key
            ?: UUID.randomUUID().toString()

        if (imageUri != null) {
            uploadToCloudinary(imageUri!!, itemId, title, description, type, currentUser)
        } else {
            saveItem(itemId, title, description, type, currentUser, null)
        }
    }

    private fun uploadToCloudinary(
        uri: Uri,
        itemId: String,
        title: String,
        description: String,
        type: String,
        user: com.google.firebase.auth.FirebaseUser
    ) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)

            tempFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            val preset = "back2you".toRequestBody("text/plain".toMediaTypeOrNull())

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.cloudinary.com/v1_1/drbvfzozf/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(CloudinaryService::class.java)

            service.uploadImage(body, preset)
                .enqueue(object : Callback<CloudinaryResponse> {

                    override fun onResponse(
                        call: Call<CloudinaryResponse>,
                        response: Response<CloudinaryResponse>
                    ) {
                        if (response.isSuccessful) {
                            val imageUrl = response.body()?.secure_url
                            saveItem(itemId, title, description, type, user, imageUrl)
                        } else {
                            btnSubmit.isEnabled = true
                            Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
                        btnSubmit.isEnabled = true
                        Toast.makeText(requireContext(), "Upload error", Toast.LENGTH_SHORT).show()
                    }
                })

        } catch (e: Exception) {
            btnSubmit.isEnabled = true
            Toast.makeText(requireContext(), "File processing failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveItem(
        itemId: String,
        title: String,
        description: String,
        type: String,
        user: com.google.firebase.auth.FirebaseUser,
        imageUrl: String?
    ) {

        val userRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(user.uid)

        userRef.get().addOnSuccessListener { snapshot ->

            val realUsername =
                snapshot.child("username").getValue(String::class.java) ?: "Anonymous"

            val newItem = PostItem(
                id = itemId,
                title = title,
                description = description,
                type = type,
                finderName = realUsername,
                finderUid = user.uid,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis(),
                returned = false,                // ✅ Explicit
                returnTimestamp = null           // ✅ Explicit
            )

            database.child("items").child(itemId)
                .setValue(newItem)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Item posted successfully", Toast.LENGTH_SHORT).show()
                    clearFields()
                    btnSubmit.isEnabled = true
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to post item", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun clearFields() {
        etTitle.text?.clear()
        etDescription.text?.clear()
        rgType.clearCheck()
        spinnerCategory.setSelection(0)
        imageUri = null
        ivItemPreview.setImageResource(0)
        layoutPhoto.visibility = View.GONE
    }
}