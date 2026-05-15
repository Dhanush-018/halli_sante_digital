package com.example.halli_sante_digital

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.halli_sante_digital.databinding.ActivityUploadBinding
import com.example.halli_sante_digital.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://halli-sante-digitial.firebasestorage.app")
    private val categories = arrayOf("Fruits", "Vegetables", "Grains", "Dairy", "Pottery", "Arts", "Toys")

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.ivProduct.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryDropdown()

        binding.btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.btnUpload.setOnClickListener {
            validateAndUpload()
        }
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun validateAndUpload() {
        val name = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val sellerName = binding.etSellerName.text.toString().trim()
        val sellerPhone = binding.etSellerPhone.text.toString().trim()

        if (name.isEmpty() || price.isEmpty() || category.isEmpty() || sellerName.isEmpty() || sellerPhone.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImage(name, price, category, sellerName, sellerPhone)
    }

    private fun uploadImage(name: String, price: String, category: String, sellerName: String, sellerPhone: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpload.isEnabled = false

        try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            
            // Aggressive resize to keep Base64 string small (Firestore limit is 1MB total)
            val maxDimension = 600
            val ratio = Math.min(maxDimension.toDouble() / originalBitmap.width, maxDimension.toDouble() / originalBitmap.height)
            val width = (originalBitmap.width * ratio).toInt()
            val height = (originalBitmap.height * ratio).toInt()
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
            val byteArray = baos.toByteArray()
            val base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

            saveProductToFirestore(name, price, category, base64Image, sellerName, sellerPhone)
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            binding.btnUpload.isEnabled = true
            Toast.makeText(this, "Image processing error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProductToFirestore(name: String, price: String, category: String, imageUrl: String, sellerName: String, sellerPhone: String) {
        val id = firestore.collection("products").document().id
        val product = Product(id, name, price, category, imageUrl, sellerName, sellerPhone, System.currentTimeMillis())

        firestore.collection("products").document(id)
            .set(product)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Product uploaded successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnUpload.isEnabled = true
                Toast.makeText(this, "Error saving product: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
