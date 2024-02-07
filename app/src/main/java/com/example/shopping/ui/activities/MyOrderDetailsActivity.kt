package com.example.shopping.ui.activities

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.ActivityMyOrderDetailsAcivityBinding
import com.example.shopping.models.Order
import com.example.shopping.ui.adapters.CartItemsListAdapter
import com.example.shopping.utils.Constants
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyOrderDetailsActivity : AppCompatActivity() {

  private lateinit var views: ActivityMyOrderDetailsAcivityBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityMyOrderDetailsAcivityBinding.inflate(layoutInflater)
    setContentView(views.root)
    setupActionBar()

    var myOrderDetails: Order = Order()

    if (intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS)) {
      myOrderDetails =
        intent.getParcelableExtra<Order>(Constants.EXTRA_MY_ORDER_DETAILS)!!
    }

    setupUI(myOrderDetails)
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarMyOrderDetailsActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }
    views.toolbarMyOrderDetailsActivity.setNavigationOnClickListener { onBackPressed() }
  }

  /**
   * A function to setup UI.
   *
   * @param orderDetails Order details received through intent.
   */
  private fun setupUI(orderDetails: Order) {

    views.tvOrderDetailsId.text = orderDetails.title
    val dateFormat = "dd MMM yyyy HH:mm"
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = orderDetails.order_datetime
    val orderDateTime = formatter.format(calendar.time)

    views.tvOrderDetailsDate.text = orderDateTime
    views.tvOrderStatus.text = orderDetails.status

    views.rvMyOrderItemsList.layoutManager = LinearLayoutManager(this@MyOrderDetailsActivity)
    views.rvMyOrderItemsList.setHasFixedSize(true)

    val cartListAdapter =
      CartItemsListAdapter(this@MyOrderDetailsActivity, orderDetails.items, false)
    views.rvMyOrderItemsList.adapter = cartListAdapter

    views.tvMyOrderDetailsAddressType.text = orderDetails.address.type
    views.tvMyOrderDetailsFullName.text = orderDetails.address.name
    views.tvMyOrderDetailsAddress.text =
      "${orderDetails.address.address}, ${orderDetails.address.zipCode}"
    views.tvMyOrderDetailsAdditionalNote.text = orderDetails.address.additionalNote

    if (orderDetails.address.otherDetails.isNotEmpty()) {
      views.tvMyOrderDetailsOtherDetails.visibility = View.VISIBLE
      views.tvMyOrderDetailsOtherDetails.text = orderDetails.address.otherDetails
    } else {
      views.tvMyOrderDetailsOtherDetails.visibility = View.GONE
    }
    views.tvMyOrderDetailsMobileNumber.text = orderDetails.address.mobileNumber

    val decimalFormat = DecimalFormat("#,##0.00")
    val formattedSubtotal = decimalFormat.format(orderDetails.sub_total_amount.toDouble())
    val subtotal = SpannableStringBuilder()
      .append(formattedSubtotal)
      .append(" denar")
    views.tvOrderDetailsSubTotal.text = subtotal
    views.tvOrderDetailsShippingCharge.text = "120.00 denar"
    val formattedTotal = decimalFormat.format(orderDetails.total_amount.toDouble())
    val total = SpannableStringBuilder()
      .append(formattedTotal)
      .append(" denar")
    views.tvOrderDetailsTotalAmount.text = total
  }
}