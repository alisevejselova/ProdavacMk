package com.example.shopping.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.shopping.R
import com.example.shopping.databinding.ActivitySoldProductDetailsBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.SoldProduct
import com.example.shopping.utils.Constants
import com.example.shopping.utils.GlideLoader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SoldProductDetailsActivity : AppCompatActivity() {

  private lateinit var views: ActivitySoldProductDetailsBinding
  lateinit var soldProduct: SoldProduct

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivitySoldProductDetailsBinding.inflate(layoutInflater)
    setContentView(views.root)

    var productDetails = SoldProduct()

    if (intent.hasExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)) {
      productDetails =
        intent.getParcelableExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)!!
    }

    setupActionBar()

    setupUI(productDetails)
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarSoldProductDetailsActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarSoldProductDetailsActivity.setNavigationOnClickListener { onBackPressed() }
  }

  /**
   * A function to setup UI.
   *
   * @param productDetails Order details received through intent.
   */
  private fun setupUI(productDetails: SoldProduct) {
    soldProduct = productDetails
    views.tvSoldProductDetailsId.text = productDetails.order_id

    // Date Format in which the date will be displayed in the UI.
    val dateFormat = "dd MMM yyyy HH:mm"
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = productDetails.order_date
    views.tvSoldProductDetailsDate.text = formatter.format(calendar.time)

    GlideLoader(this@SoldProductDetailsActivity).loadProductPicture(
      productDetails.image,
      views.ivProductItemImage
    )
    views.tvProductItemName.text = productDetails.title
    views.tvProductItemName.text = "${productDetails.price} denar"
    views.tvSoldProductQuantity.text = productDetails.sold_quantity

    views.tvSoldDetailsAddressType.text = productDetails.address.type
    views.tvSoldDetailsFullName.text = productDetails.address.name
    views.tvSoldDetailsAddress.text =
      "${productDetails.address.address}, ${productDetails.address.zipCode}"
    views.tvSoldDetailsAdditionalNote.text = productDetails.address.additionalNote

    if (productDetails.address.otherDetails.isNotEmpty()) {
      views.tvSoldDetailsOtherDetails.visibility = View.VISIBLE
      views.tvSoldDetailsOtherDetails.text = productDetails.address.otherDetails
    } else {
      views.tvSoldDetailsOtherDetails.visibility = View.GONE
    }
    views.tvSoldDetailsMobileNumber.text = productDetails.address.mobileNumber

    views.tvSoldProductSubTotal.text = "${productDetails.sub_total_amount} denar"
    views.tvSoldProductShippingCharge.text = "${productDetails.shipping_charge} denar"
    views.tvSoldProductTotalAmount.text = "${productDetails.total_amount} denar"

    if (productDetails.status == "Delivered") {
      views.submitOrderButton.visibility = View.GONE
    }

    views.submitOrderButton.setOnClickListener {
      FirestoreClass().updateOrderStatus(
        this@SoldProductDetailsActivity,
        productDetails.order_id,
        "Delivered"
      )
    }
  }

  fun orderDelivered() {
    FirestoreClass().updateSoldProductStatus(
      this@SoldProductDetailsActivity,
      soldProduct.title,
      "Delivered"
    )
  }

  fun delivered() {
    views.submitOrderButton.visibility = View.GONE
  }
}