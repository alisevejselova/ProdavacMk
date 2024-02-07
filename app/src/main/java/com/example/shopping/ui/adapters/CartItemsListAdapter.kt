package com.example.shopping.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.CartItem
import com.example.shopping.ui.activities.CartListActivity
import com.example.shopping.utils.Constants
import com.example.shopping.utils.CustomTextView
import com.example.shopping.utils.CustomTextViewBold
import com.example.shopping.utils.GlideLoader

/**
 * A adapter class for dashboard items list.
 */
open class CartItemsListAdapter(
  private val context: Context,
  private var list: ArrayList<CartItem>,
  private var updateCartItem: Boolean
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
        R.layout.item_cart_layout,
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
  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    val image = holder.itemView.findViewById<ImageView>(R.id.iv_cart_item_image)
    val title = holder.itemView.findViewById<CustomTextView>(R.id.tv_cart_item_title)
    val price = holder.itemView.findViewById<CustomTextViewBold>(R.id.tv_cart_item_price)
    val quantity = holder.itemView.findViewById<CustomTextView>(R.id.tv_cart_quantity)
    val removeItem = holder.itemView.findViewById<ImageButton>(R.id.ib_remove_cart_item)
    val addItem = holder.itemView.findViewById<ImageButton>(R.id.ib_add_cart_item)
    val deleteItem = holder.itemView.findViewById<ImageButton>(R.id.ib_delete_cart_item)

    val model = list[position]

    if (holder is MyViewHolder) {

      GlideLoader(context).loadProductPicture(model.image, image)

      title.text = model.title
      price.text = "${model.price} denar"
      quantity.text = model.cart_quantity

      if (model.cart_quantity == "0") {
        removeItem.visibility = View.GONE
        addItem.visibility = View.GONE

        if (updateCartItem) {
          deleteItem.visibility = View.VISIBLE
        } else {
          deleteItem.visibility = View.GONE
        }
        quantity.text = context.resources.getString(R.string.lbl_out_of_stock)

        quantity.setTextColor(
          ContextCompat.getColor(
            context,
            R.color.colorSnackBarError
          )
        )
      } else {

        if (updateCartItem) {
          removeItem.visibility = View.VISIBLE
          addItem.visibility = View.VISIBLE
          deleteItem.visibility = View.VISIBLE
        } else {
          removeItem.visibility = View.GONE
          addItem.visibility = View.GONE
          deleteItem.visibility = View.GONE
        }
        quantity.setTextColor(
          ContextCompat.getColor(
            context,
            R.color.colorSecondaryText
          )
        )
      }

      removeItem.setOnClickListener {

        if (model.cart_quantity == "1") {
          FirestoreClass().removeItemFromCart(context, model.id)
        } else {
          val cartQuantity: Int = model.cart_quantity.toInt()

          val itemHashMap = HashMap<String, Any>()

          itemHashMap[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

          if (context is CartListActivity) {
            context.showProgressDialog(context.resources.getString(R.string.please_wait))
          }

          FirestoreClass().updateMyCart(context, model.id, itemHashMap)
        }
      }

      addItem.setOnClickListener {

        val cartQuantity: Int = model.cart_quantity.toInt()

        if (cartQuantity < model.stock_quantity.toInt()) {

          val itemHashMap = HashMap<String, Any>()

          itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

          if (context is CartListActivity) {
            context.showProgressDialog(context.resources.getString(R.string.please_wait))
          }

          FirestoreClass().updateMyCart(context, model.id, itemHashMap)
        } else {
          if (context is CartListActivity) {
            context.showErrorSnackBar(
              context.resources.getString(
                R.string.msg_for_available_stock,
                model.stock_quantity
              ),
              true
            )
          }
        }
      }

      deleteItem.setOnClickListener {

        when (context) {
          is CartListActivity -> {
            context.showProgressDialog(context.resources.getString(R.string.please_wait))
          }
        }

        FirestoreClass().removeItemFromCart(context, model.id)
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
  private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}