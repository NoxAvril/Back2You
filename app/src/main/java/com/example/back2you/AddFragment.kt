package com.example.back2you

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*

class AddFragment : Fragment(R.layout.fragment_add) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private var imageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                imageUri = it.data?.data
                view?.findViewById<ImageView>(R.id.ivItemPreview)
                    ?.setImageURI(imageUri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val rgType = view.findViewById<RadioGroup>(R.id.rgType)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val layoutPhoto = view.findViewById<LinearLayout>(R.id.layoutPhotoUpload)
        val btnUpload = view.findViewById<Button>(R.id.btnUploadPhoto)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        rgType.setOnCheckedChangeListener { _, checkedId ->
            layoutPhoto.visibility =
                if (checkedId == R.id.rbFound) View.VISIBLE else View.GONE
        }

        btnSubmit.setOnClickListener {

            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val selectedTypeId = rgType.checkedRadioButtonId

            val currentUser = auth.currentUser ?: return@setOnClickListener

            val type = if (selectedTypeId == R.id.rbLost) "Lost" else "Found"

            val itemId = database.child("items").push().key ?: UUID.randomUUID().toString()

            if (imageUri != null) {
                uploadToCloudinary(imageUri!!, itemId, title, description, type, currentUser)
            } else {
                saveItem(itemId, title, description, type, currentUser, null)
            }
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
            //  Convert Uri to temp file
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            val preset = "back2you"
                .toRequestBody("text/plain".toMediaTypeOrNull())

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
                            Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CloudinaryResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Upload error", Toast.LENGTH_SHORT).show()
                    }
                })

        } catch (_: Exception) {
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

        val newItem = PostItem(
            id = itemId,
            title = title,
            description = description,
            type = type,
            finderName = user.displayName ?: "Anonymous",
            finderUid = user.uid,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        database.child("items").child(itemId)
            .setValue(newItem)
    }
}