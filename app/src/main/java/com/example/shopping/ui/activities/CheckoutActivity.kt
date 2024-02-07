package com.example.shopping.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.ActivityCheckoutBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.Address
import com.example.shopping.models.CartItem
import com.example.shopping.models.Order
import com.example.shopping.models.Product
import com.example.shopping.ui.adapters.CartItemsListAdapter
import com.example.shopping.utils.Constants
import java.text.DecimalFormat

class CheckoutActivity : BaseActivity() {

  private lateinit var views: ActivityCheckoutBinding

  private var mAddressDetails: Address? = null
  private lateinit var mProductsList: ArrayList<Product>
  private lateinit var mCartItemsList: ArrayList<CartItem>
  private var mSubTotal: Double = 0.00
  private var mTotalAmount: Double = 0.00
  private lateinit var mOrderDetails: Order

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    views = ActivityCheckoutBinding.inflate(layoutInflater)
    setContentView(views.root)

    if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
      mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_SELECTED_ADDRESS)
    }

    if (mAddressDetails != null) {
      views.tvCheckoutAddressType.text = mAddressDetails?.type
      views.tvCheckoutFullName.text = mAddressDetails?.name
      views.tvCheckoutAddress.text = mAddressDetails?.address
      views.tvCheckoutAdditionalNote.text = mAddressDetails?.additionalNote

      if (mAddressDetails?.otherDetails!!.isNotEmpty()) {
        views.tvCheckoutOtherDetails.text = mAddressDetails?.otherDetails
      }
      views.tvCheckoutMobileNumber.text = mAddressDetails?.mobileNumber
    }
    setupActionBar()

    getProductList()

    views.btnPlaceOrder.setOnClickListener {
      placeAnOrder()
    }
  }

  private fun getProductList() {
    showProgressDialog(resources.getString(R.string.please_wait))

    FirestoreClass().getAllProductsList(this@CheckoutActivity)
  }

  fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
    hideProgressDialog()
    mProductsList = productsList
    getCartItemsList()
  }

  private fun getCartItemsList() {
    FirestoreClass().getCartList(this@CheckoutActivity)
  }

  /**
   * A function to notify the success result of the cart items list from cloud firestore.
   *
   * @param cartList
   */
  fun successCartItemsList(cartList: ArrayList<CartItem>) {

    hideProgressDialog()

    for (product in mProductsList) {
      for (cart in cartList) {
        if (product.product_id == cart.product_id) {
          cart.stock_quantity = product.stock_quantity
        }
      }
    }

    mCartItemsList = cartList

    views.rvCartListItems.layoutManager = LinearLayoutManager(this@CheckoutActivity)
    views.rvCartListItems.setHasFixedSize(true)

    val cartListAdapter = CartItemsListAdapter(this@CheckoutActivity, mCartItemsList, false)
    views.rvCartListItems.adapter = cartListAdapter

    for (item in mCartItemsList) {

      val availableQuantity = item.stock_quantity.toInt()

      if (availableQuantity > 0) {
        val price = item.price.toDouble()
        val quantity = item.cart_quantity.toInt()

        mSubTotal += (price * quantity)
      }
    }
    val decimalFormat = DecimalFormat("#,##0.00")
    val formattedSubtotal = decimalFormat.format(mSubTotal)
    views.tvCheckoutSubTotal.text = "$formattedSubtotal denar"
    // Here we have kept Shipping Charge is fixed as 120denars but in your case it may cary. Also, it depends on the location and total amount.
    views.tvCheckoutShippingCharge.text = "120.00 denar"

    if (mSubTotal > 0) {
      views.llCheckoutPlaceOrder.visibility = View.VISIBLE

      mTotalAmount = mSubTotal + 120.00
      val formattedTotal = decimalFormat.format(mTotalAmount)
      views.tvCheckoutTotalAmount.text = "$formattedTotal denar"
    } else {
      views.llCheckoutPlaceOrder.visibility = View.GONE
    }
  }

  /**
   * A function to prepare the Order details to place an order.
   */
  private fun placeAnOrder() {

    showProgressDialog(resources.getString(R.string.please_wait))
    if (mAddressDetails != null) {
      mOrderDetails = Order(
        FirestoreClass().getCurrentUserID(),
        mCartItemsList,
        mAddressDetails!!,
        "${System.currentTimeMillis()}",
        mCartItemsList[0].image,
        mSubTotal.toString(),
        "120",
        mTotalAmount.toString(),
        System.currentTimeMillis(),
        "Pending"
      )
      FirestoreClass().placeOrder(this@CheckoutActivity, mOrderDetails)
    }
  }

  /**
   * A function to notify the success result of the order placed.
   */
  fun orderPlacedSuccess() {

    FirestoreClass().updateAllDetails(this, mCartItemsList, mOrderDetails)
  }

  /**
   * A function to notify the success result after updating all the required details.
   */
  fun allDetailsUpdatedSuccessfully() {

    // Hide the progress dialog.
    hideProgressDialog()

    Toast.makeText(this@CheckoutActivity, "Your order placed successfully.", Toast.LENGTH_SHORT)
      .show()

    val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
  }

  private fun setupActionBar() {

    setSupportActionBar(views.toolbarCheckoutActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarCheckoutActivity.setNavigationOnClickListener { onBackPressed() }
  }
}