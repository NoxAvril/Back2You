package com.example.back2you

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private lateinit var ivProfilePic: ImageView
    private lateinit var etEditName: EditText
    private lateinit var layoutContactList: LinearLayout

    // ------------------------------
    // IMAGE PICKER
    // ------------------------------
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                ivProfilePic.setImageURI(it)
                uploadProfileToCloudinary(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth.currentUser ?: return

        ivProfilePic = view.findViewById(R.id.ivEditProfilePic)
        etEditName = view.findViewById(R.id.etEditName)
        layoutContactList = view.findViewById(R.id.layoutContactList)

        val btnAddContact = view.findViewById<Button>(R.id.btnAddContact)
        val btnSaveUsername = view.findViewById<ImageButton>(R.id.btnSaveUsername)
        val btnCancelUsername = view.findViewById<ImageButton>(R.id.btnCancelUsername)

        // ------------------------------
        // PROFILE IMAGE CLICK
        // ------------------------------
        ivProfilePic.setOnClickListener {
            showPhotoDialog()
        }

        // ------------------------------
        // LOAD USER DATA
        // ------------------------------
        database.child("users")
            .child(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->

                val username = snapshot.child("username")
                    .getValue(String::class.java)

                val profileImage = snapshot.child("profileImage")
                    .getValue(String::class.java)

                etEditName.setText(username ?: "")

                if (!profileImage.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(profileImage)
                        .into(ivProfilePic)
                }

                layoutContactList.removeAllViews()

                snapshot.child("contacts").children.forEach { child ->
                    val contact = ContactItem(
                        id = child.key ?: "",
                        type = child.child("type").value?.toString() ?: "",
                        value = child.child("value").value?.toString() ?: "",
                        isPublic = child.child("isPublic")
                            .getValue(Boolean::class.java) ?: false
                    )
                    addContactView(contact)
                }
            }

        // ------------------------------
        // ADD CONTACT
        // ------------------------------
        btnAddContact.setOnClickListener {
            showAddDialog(currentUser.uid)
        }

        // ------------------------------
        // SAVE USERNAME
        // ------------------------------
        btnSaveUsername.setOnClickListener {

            val newUsername = etEditName.text.toString().trim()

            if (newUsername.isEmpty()) {
                etEditName.error = "Username required"
                return@setOnClickListener
            }

            database.child("users")
                .child(currentUser.uid)
                .child("username")
                .setValue(newUsername)
                .addOnSuccessListener {

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newUsername)
                        .build()

                    currentUser.updateProfile(profileUpdates)

                    Toast.makeText(
                        requireContext(),
                        "Username Updated",
                        Toast.LENGTH_SHORT
                    ).show()

                    navigateBack()
                }
        }

        // ------------------------------
        // CANCEL
        // ------------------------------
        btnCancelUsername.setOnClickListener {
            navigateBack()
        }
    }

    // =========================================================
    // PHOTO DIALOG
    // =========================================================
    private fun showPhotoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Profile Picture")
            .setItems(arrayOf("Upload Profile", "Cancel")) { dialog, which ->
                if (which == 0) imagePicker.launch("image/*")
                else dialog.dismiss()
            }
            .show()
    }

    // =========================================================
    // CLOUDINARY PROFILE UPLOAD
    // =========================================================
    private fun uploadProfileToCloudinary(uri: Uri) {

        val currentUser = auth.currentUser ?: return

        try {
            val inputStream =
                requireContext().contentResolver.openInputStream(uri)

            val tempFile = File.createTempFile(
                "profile_upload",
                ".jpg",
                requireContext().cacheDir
            )

            tempFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }

            val requestFile = tempFile
                .asRequestBody("image/*".toMediaTypeOrNull())

            val body = MultipartBody.Part
                .createFormData("file", tempFile.name, requestFile)

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

                            val imageUrl =
                                response.body()?.secure_url ?: return

                            // Save in Realtime Database
                            database.child("users")
                                .child(currentUser.uid)
                                .child("profileImage")
                                .setValue(imageUrl)

                            // Update FirebaseAuth photo
                            val profileUpdates =
                                UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(imageUrl))
                                    .build()

                            currentUser.updateProfile(profileUpdates)

                            Toast.makeText(
                                requireContext(),
                                "Profile Photo Updated",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Upload failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<CloudinaryResponse>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Upload error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "File processing failed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // ADD CONTACT DIALOG
    // =========================================================
    private fun showAddDialog(uid: String) {

        val dialogView =
            layoutInflater.inflate(R.layout.dialog_add_contact, null)

        val etValue =
            dialogView.findViewById<EditText>(R.id.etNewValue)

        val spType =
            dialogView.findViewById<Spinner>(R.id.spContactType)

        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.contact_types,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        spType.adapter = spinnerAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Add Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->

                val type = spType.selectedItem.toString()
                val value = etValue.text.toString().trim()

                if (value.isEmpty()) return@setPositiveButton

                val contactId = database.child("users")
                    .child(uid)
                    .child("contacts")
                    .push().key ?: return@setPositiveButton

                val newContact = ContactItem(
                    id = contactId,
                    type = type,
                    value = value,
                    isPublic = true
                )

                database.child("users")
                    .child(uid)
                    .child("contacts")
                    .child(contactId)
                    .setValue(newContact)

                addContactView(newContact)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================
    // CONTACT VIEW
    // =========================================================
    private fun addContactView(contact: ContactItem) {

        val currentUser = auth.currentUser ?: return

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }

        val infoLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val textView = TextView(requireContext()).apply {
            text = "${contact.type}: ${contact.value}"
            textSize = 16f
        }

        val visibilityLabel = TextView(requireContext()).apply {
            textSize = 13f
            text = if (contact.isPublic) "Visible" else "Hidden"
        }

        val switch = Switch(requireContext()).apply {
            isChecked = contact.isPublic
        }

        val deleteBtn = ImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_delete)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            visibilityLabel.text =
                if (isChecked) "Visible" else "Hidden"

            database.child("users")
                .child(currentUser.uid)
                .child("contacts")
                .child(contact.id)
                .child("isPublic")
                .setValue(isChecked)
        }

        deleteBtn.setOnClickListener {
            database.child("users")
                .child(currentUser.uid)
                .child("contacts")
                .child(contact.id)
                .removeValue()

            layoutContactList.removeView(row)
        }

        infoLayout.addView(textView)
        infoLayout.addView(visibilityLabel)

        row.addView(infoLayout)
        row.addView(switch)
        row.addView(deleteBtn)

        layoutContactList.addView(row)
    }

    // =========================================================
    // NAVIGATION
    // =========================================================
    private fun navigateBack() {
        (requireActivity() as? MainActivity)
            ?.replaceFragment(ProfileFragment())
    }
}