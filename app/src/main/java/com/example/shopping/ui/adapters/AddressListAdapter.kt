package com.example.shopping.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.models.Address
import com.example.shopping.ui.activities.AddEditAddressActivity
import com.example.shopping.ui.activities.CheckoutActivity
import com.example.shopping.utils.Constants
import com.example.shopping.utils.CustomTextView
import com.example.shopping.utils.CustomTextViewBold

/**
 * An adapter class for AddressList adapter.
 */
open class AddressListAdapter(
  private val context: Context,
  private var list: ArrayList<Address>,
  private val selectAddress: Boolean
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
        R.layout.item_address_layout,
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
    val model = list[position]

    val fullName = holder.itemView.findViewById<CustomTextViewBold>(R.id.tv_address_full_name)
    val addressType = holder.itemView.findViewById<CustomTextView>(R.id.tv_address_type)
    val addressDetails = holder.itemView.findViewById<CustomTextView>(R.id.tv_address_details)
    val addressMobileNumber =
      holder.itemView.findViewById<CustomTextView>(R.id.tv_address_mobile_number)

    if (holder is MyViewHolder) {
      fullName.text = model.name
      addressType.text = model.type
      addressDetails.text = "${model.address}, ${model.zipCode}"
      addressMobileNumber.text = model.mobileNumber

      if (selectAddress) {
        holder.itemView.setOnClickListener {
          val intent = Intent(context, CheckoutActivity::class.java)
          intent.putExtra(Constants.EXTRA_SELECTED_ADDRESS, model)
          context.startActivity(intent)
        }
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
   * A function to edit the address details and pass the existing details through intent.
   *
   * @param activity
   * @param position
   */
  fun notifyEditItem(activity: Activity, position: Int) {
    val intent = Intent(context, AddEditAddressActivity::class.java)
    intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS, list[position])
    activity.startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)

    notifyItemChanged(position) // Notify any registered observers that the item at position has changed.
  }

  /**
   * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
   */
  private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
