package com.example.shopping.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.models.SoldProduct
import com.example.shopping.ui.activities.SoldProductDetailsActivity
import com.example.shopping.ui.fragments.SoldProductsFragment
import com.example.shopping.utils.Constants
import com.example.shopping.utils.CustomTextView
import com.example.shopping.utils.CustomTextViewBold
import com.example.shopping.utils.GlideLoader

/**
 * A adapter class for sold products list items.
 */
open class SoldProductsListAdapter(
  private val context: Context,
  private var list: ArrayList<SoldProduct>,
  private val fragment: SoldProductsFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


  /**
   * Inflates the item views which is designed in xml layout file
   *
   * create a new
   * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return MyViewHolder(
      LayoutInflater.from(context).inflate(
        R.layout.item_list_layout,
        parent,
        false
      )
    )
  }

  /**
   * Binds each item in the ArrayList to a view
   *
   * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
   * an item.
   *
   * This new ViewHolder should be constructed with a new View that can represent the items
   * of the given type. You can either create a new View manually or inflate it from an XML
   * layout file.
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val model = list[position]

    val image = holder.itemView.findViewById<ImageView>(R.id.iv_item_image)
    val title = holder.itemView.findViewById<CustomTextView>(R.id.tv_item_name)
    val price = holder.itemView.findViewById<CustomTextViewBold>(R.id.tv_item_price)
    val delete = holder.itemView.findViewById<ImageButton>(R.id.ib_delete_product)
    val status = holder.itemView.findViewById<CustomTextViewBold>(R.id.tv_status)

    if (holder is MyViewHolder) {

      GlideLoader(context).loadProductPicture(
        model.image,
        image
      )

      title.text = model.title
      price.text = "${model.price} denar"

      status.text = model.status
      if (model.status == "Delivered") {
        delete.visibility = View.VISIBLE
      } else {
        delete.visibility = View.INVISIBLE
      }

      delete.setOnClickListener {
        fragment.deleteSoldProduct(model.title)
      }
      holder.itemView.setOnClickListener {
        val intent = Intent(context, SoldProductDetailsActivity::class.java)
        intent.putExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS, model)
        context.startActivity(intent)
      }
    }
  }

  /**
   * Gets the number of items in the list
   */
  override fun getItemCount(): Int {
    return list.size
  }

  /**
   * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
   */
  class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}