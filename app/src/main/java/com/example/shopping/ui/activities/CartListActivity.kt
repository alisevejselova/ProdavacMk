package com.example.shopping.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.ActivityCardListBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.CartItem
import com.example.shopping.models.Product
import com.example.shopping.ui.adapters.CartItemsListAdapter
import com.example.shopping.utils.Constants
import java.text.DecimalFormat

class CartListActivity : BaseActivity() {

  private lateinit var views: ActivityCardListBinding
  private lateinit var mProductsList: ArrayList<Product>
  private lateinit var mCartListItems: ArrayList<CartItem>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityCardListBinding.inflate(layoutInflater)
    setContentView(views.root)

    setupActionBar()

    views.btnCheckout.setOnClickListener {
      val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
      intent.putExtra(Constants.EXTRA_SELECT_ADDRESS, true)
      startActivity(intent)
    }
  }

  override fun onResume() {
    super.onResume()
    getProductList()
  }

  private fun setupActionBar() {

    setSupportActionBar(views.toolbarCartListActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarCartListActivity.setNavigationOnClickListener { onBackPressed() }
  }

  /**
   * A function to get the success result of product list.
   *
   * @param productsList
   */
  fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
    hideProgressDialog()
    mProductsList = productsList

    getCartItemsList()
  }

  /**
   * A function to get product list to compare the current stock with the cart items.
   */
  private fun getProductList() {

    showProgressDialog(resources.getString(R.string.please_wait))

    FirestoreClass().getAllProductsList(this@CartListActivity)
  }

  /**
   * A function to get the list of cart items in the activity.
   */
  private fun getCartItemsList() {
    FirestoreClass().getCartList(this@CartListActivity)
  }

  /**
   * A function to notify the success result of the cart items list from cloud firestore.
   *
   * @param cartList
   */
  fun successCartItemsList(cartList: ArrayList<CartItem>) {
    hideProgressDialog()
    for (product in mProductsList) {
      for (cartItem in cartList) {
        if (product.product_id == cartItem.product_id) {

          cartItem.stock_quantity = product.stock_quantity

          if (product.stock_quantity.toInt() == 0) {
            cartItem.cart_quantity = product.stock_quantity
          }
        }
      }
    }

    mCartListItems = cartList

    if (mCartListItems.size > 0) {
      views.rvCartItemsList.visibility = View.VISIBLE
      views.llCheckout.visibility = View.VISIBLE
      views.tvNoCartItemFound.visibility = View.GONE

      views.rvCartItemsList.layoutManager = LinearLayoutManager(this@CartListActivity)
      views.rvCartItemsList.setHasFixedSize(true)

      val cartListAdapter = CartItemsListAdapter(this@CartListActivity, mCartListItems, true)
      views.rvCartItemsList.adapter = cartListAdapter

      var subTotal: Double = 0.00

      for (item in mCartListItems) {

        val availableQuantity = item.stock_quantity.toInt()

        if (availableQuantity > 0) {
          val price = item.price.toDouble()
          val quantity = item.cart_quantity.toInt()
          subTotal += (price * quantity)
        }
      }
      val decimalFormat = DecimalFormat("#,##0.00")

      val formattedSubtotal = decimalFormat.format(subTotal)
      views.tvSubTotal.text = "$formattedSubtotal denar"
      // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
      views.tvShippingCharge.text = "120.00 denar"

      if (subTotal > 0) {
        views.llCheckout.visibility = View.VISIBLE

        val total = subTotal + 120.00
        val formattedTotal = decimalFormat.format(total)
        views.tvTotalAmount.text = "$formattedTotal denar"
      } else {
        views.llCheckout.visibility = View.GONE
      }
    } else {
      views.rvCartItemsList.visibility = View.GONE
      views.llCheckout.visibility = View.GONE
      views.tvNoCartItemFound.visibility = View.VISIBLE
    }
  }

  /**
   * A function to notify the user about the item removed from the cart list.
   */
  fun itemRemovedSuccess() {

    hideProgressDialog()

    Toast.makeText(
      this@CartListActivity,
      resources.getString(R.string.msg_item_removed_successfully),
      Toast.LENGTH_SHORT
    ).show()

    getCartItemsList()
  }

  /**
   * A function to notify the user about the item quantity updated in the cart list.
   */
  fun itemUpdateSuccess() {

    hideProgressDialog()

    getCartItemsList()
  }
}
