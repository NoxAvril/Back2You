package com.example.back2you

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class AddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        // Initialize Views
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val rgType = view.findViewById<RadioGroup>(R.id.rgType)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val layoutPhoto = view.findViewById<LinearLayout>(R.id.layoutPhotoUpload)
        val ivPreview = view.findViewById<ImageView>(R.id.ivItemPreview)
        val btnUpload = view.findViewById<Button>(R.id.btnUploadPhoto)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        // Setup Spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.item_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }

        // TOGGLE PHOTO SECTION based on Lost/Found selection
        rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbFound) {
                layoutPhoto.visibility = View.VISIBLE
            } else {
                layoutPhoto.visibility = View.GONE
                ivPreview.tag = null // Clear photo status if switching to Lost
                ivPreview.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }

        // Mock Photo Upload
        btnUpload.setOnClickListener {
            ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            ivPreview.tag = "selected"
            Toast.makeText(context, "Photo attached", Toast.LENGTH_SHORT).show()
        }

        // SUBMIT VALIDATION
        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val categoryPos = spinnerCategory.selectedItemPosition
            val selectedTypeId = rgType.checkedRadioButtonId

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }

            if (categoryPos == 0) {
                Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTypeId == -1) {
                Toast.makeText(context, "Select Lost or Found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Photo is ONLY required if Found is selected
            if (selectedTypeId == R.id.rbFound && ivPreview.tag == null) {
                Toast.makeText(context, "Found items must have a photo", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // SUCCESS
            Toast.makeText(context, "Successfully posted: $title", Toast.LENGTH_LONG).show()
            clearForm(etTitle, rgType, spinnerCategory, etDescription, ivPreview, layoutPhoto)
        }

        return view
    }

    private fun clearForm(etT: EditText, rg: RadioGroup, spn: Spinner, etD: EditText, iv: ImageView, layout: LinearLayout) {
        etT.text.clear()
        etD.text.clear()
        rg.clearCheck()
        spn.setSelection(0)
        iv.setImageResource(android.R.drawable.ic_menu_camera)
        iv.tag = null
        layout.visibility = View.GONE
    }
}