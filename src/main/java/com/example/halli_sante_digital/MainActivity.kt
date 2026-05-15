package com.example.halli_sante_digital

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.halli_sante_digital.databinding.ActivityMainBinding
import com.example.halli_sante_digital.model.Product
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var allProducts = listOf<Product>()
    private var currentCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupCategories()
        setupWelcomeMessage()
        fetchProducts()

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        binding.searchView.editText.setOnEditorActionListener { v, actionId, event ->
            val query = binding.searchView.text.toString()
            filterProducts(query, currentCategory)
            binding.searchView.hide()
            false
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupWelcomeMessage() {
        val user = auth.currentUser
        if (user != null) {
            val name = user.displayName ?: user.email?.substringBefore("@") ?: "User"
            binding.tvWelcome.text = getString(R.string.welcome_user, name)
            binding.tvWelcome.visibility = View.VISIBLE
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(emptyList()) { product ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("product_id", product.id)
                putExtra("product_name", product.name)
                putExtra("product_price", product.price)
                putExtra("product_category", product.category)
                putExtra("product_image", product.imageUrl)
                putExtra("product_seller_name", product.sellerName)
                putExtra("product_seller_phone", product.sellerPhone)
            }
            startActivity(intent)
        }
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvProducts.adapter = adapter
    }

    private fun setupCategories() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                currentCategory = chip.text.toString()
                filterProducts(binding.searchView.text.toString(), currentCategory)
            }
        }
    }

    private fun fetchProducts() {
        firestore.collection("products")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                
                val products = value?.toObjects(Product::class.java) ?: emptyList()
                allProducts = products
                filterProducts(binding.searchView.text.toString(), currentCategory)
            }
    }

    private fun filterProducts(query: String, category: String) {
        var filteredList = allProducts

        if (category != "All") {
            filteredList = filteredList.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter { it.name.contains(query, ignoreCase = true) }
        }

        updateUI(filteredList)
    }

    private fun updateUI(products: List<Product>) {
        if (products.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
            adapter.updateData(products)
        }
    }
}
