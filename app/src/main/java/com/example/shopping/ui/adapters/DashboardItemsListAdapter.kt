package com.example.shopping.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.models.Product
import com.example.shopping.utils.CustomTextView
import com.example.shopping.utils.CustomTextViewBold
import com.example.shopping.utils.GlideLoader

/**
 * A adapter class for dashboard items list.
 */
open class DashboardItemsListAdapter(
  private val context: Context,
  private var list: ArrayList<Product>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var onClickListener: OnClickListener? = null

  /**
   * Inflates the item views which is designed in xml layout file
   *
   * create a new
   * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return MyViewHolder(
      LayoutInflater.from(context).inflate(
        R.layout.item_dashboard_layout,
        parent,
        false
      )
    )
  }

  fun setOnClickListener(onClickListener: OnClickListener) {
    this.onClickListener = onClickListener
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
  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val model = list[position]

    val image = holder.itemView.findViewById<ImageView>(R.id.iv_dashboard_item_image)
    val title = holder.itemView.findViewById<CustomTextViewBold>(R.id.tv_dashboard_item_title)
    val price = holder.itemView.findViewById<CustomTextView>(R.id.tv_dashboard_item_price)
    if (holder is MyViewHolder) {

      GlideLoader(context).loadProductPicture(
        model.image,
        image
      )
      title.text = model.title
      price.text = "${model.price} denar"
    }

    holder.itemView.setOnClickListener {
      if (onClickListener != null) {
        onClickListener!!.onClick(position, model)
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

  interface OnClickListener {
    fun onClick(position: Int, product: Product)
  }
}