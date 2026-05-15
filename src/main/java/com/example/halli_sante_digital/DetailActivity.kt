package com.example.halli_sante_digital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.halli_sante_digital.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val name = intent.getStringExtra("product_name")
        val price = intent.getStringExtra("product_price")
        val category = intent.getStringExtra("product_category")
        val imageUrl = intent.getStringExtra("product_image")
        val sellerName = intent.getStringExtra("product_seller_name")
        val sellerPhone = intent.getStringExtra("product_seller_phone")

        binding.apply {
            tvName.text = name
            tvPrice.text = "₹$price"
            tvCategory.text = "Category: $category"
            tvSellerName.text = "Seller: $sellerName"
            tvSellerPhone.text = "Phone: $sellerPhone"

            val imageSource: Any = if (imageUrl?.startsWith("data:image") == true || (imageUrl != null && !imageUrl.startsWith("http"))) {
                try {
                    val base64String = if (imageUrl!!.contains(",")) imageUrl.split(",")[1] else imageUrl
                    android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                } catch (e: Exception) {
                    imageUrl ?: android.R.drawable.ic_menu_gallery
                }
            } else {
                imageUrl ?: android.R.drawable.ic_menu_gallery
            }

            Glide.with(this@DetailActivity)
                .load(imageSource)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivProduct)

            btnContact.setOnClickListener {
                // Mock message to seller via WhatsApp or SMS
                val message = "Hello $sellerName, I am interested in your product: $name listed on Halli-Sante."
                val uri = Uri.parse("smsto:$sellerPhone")
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                intent.putExtra("sms_body", message)
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@DetailActivity, "No messaging app found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
