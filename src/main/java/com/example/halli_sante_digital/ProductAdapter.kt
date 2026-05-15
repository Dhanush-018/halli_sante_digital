package com.example.halli_sante_digital

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.halli_sante_digital.databinding.ItemProductBinding
import com.example.halli_sante_digital.model.Product

class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.binding.apply {
            tvName.text = product.name
            tvPrice.text = "₹${product.price}"
            tvCategory.text = product.category
            
            val imageSource: Any = if (product.imageUrl.startsWith("data:image") || !product.imageUrl.startsWith("http")) {
                try {
                    val base64String = if (product.imageUrl.contains(",")) product.imageUrl.split(",")[1] else product.imageUrl
                    android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                } catch (e: Exception) {
                    product.imageUrl
                }
            } else {
                product.imageUrl
            }

            Glide.with(ivProduct.context)
                .load(imageSource)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivProduct)

            root.setOnClickListener { onItemClick(product) }
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
