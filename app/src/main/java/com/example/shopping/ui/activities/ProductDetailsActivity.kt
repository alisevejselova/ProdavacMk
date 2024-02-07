package com.example.shopping.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.shopping.R
import com.example.shopping.databinding.ActivityProductDetailsBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.CartItem
import com.example.shopping.models.Product
import com.example.shopping.utils.Constants
import com.example.shopping.utils.GlideLoader

/**
 * Product Details Screen.
 */
class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

  private lateinit var views: ActivityProductDetailsBinding
  private var mProductId: String = ""
  private lateinit var mProductDetails: Product
  private var mProductOwnerId: String = ""

  /**
   * This function is auto created by Android when the Activity Class is created.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    views = ActivityProductDetailsBinding.inflate(layoutInflater)
    setContentView(views.root)

    if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
      mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
      Log.i("Product Id", mProductId)
    }

    if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
      mProductOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
    }

    if (FirestoreClass().getCurrentUserID() == mProductOwnerId) {
      views.btnAddToCart.visibility = View.GONE
      views.btnGoToCart.visibility = View.GONE
    } else {
      views.btnAddToCart.visibility = View.VISIBLE
    }

    views.btnAddToCart.setOnClickListener(this)
    views.btnGoToCart.setOnClickListener(this)
    setupActionBar()

    getProductDetails()
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarProductDetailsActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarProductDetailsActivity.setNavigationOnClickListener { onBackPressed() }
  }

  /**
   * A function to call the firestore class function that will get the product details from cloud firestore based on the product id.
   */
  private fun getProductDetails() {
    showProgressDialog(resources.getString(R.string.please_wait))
    FirestoreClass().getProductDetails(this@ProductDetailsActivity, mProductId)
  }

  /**
   * A function to notify the success result of the product details based on the product id.
   *
   * @param product A model class with product details.
   */
  fun productDetailsSuccess(product: Product) {
    mProductDetails = product

    GlideLoader(this@ProductDetailsActivity).loadProductPicture(
      product.image,
      views.ivProductDetailImage
    )

    views.tvProductDetailsTitle.text = product.title
    views.tvProductDetailsPrice.text = "${product.price} denar"
    views.tvProductDetailsDescription.text = product.description
    views.tvProductDetailsStockQuantity.text = product.stock_quantity

    if (product.stock_quantity.toInt() == 0) {
      hideProgressDialog()
      // Hide the AddToCart button if the item is already in the cart.
      views.btnAddToCart.visibility = View.GONE

      views.tvProductDetailsStockQuantity.text =
        resources.getString(R.string.lbl_out_of_stock)

      views.tvProductDetailsStockQuantity.setTextColor(
        ContextCompat.getColor(
          this@ProductDetailsActivity,
          R.color.colorSnackBarError
        )
      )
    } else {
      // There is no need to check the cart list if the product owner himself is seeing the product details.
      if (FirestoreClass().getCurrentUserID() == product.user_id) {
        hideProgressDialog()
      } else {
        FirestoreClass().checkIfItemExistInCart(this@ProductDetailsActivity, mProductId)
      }
    }
  }

  private fun addToCard() {
    val addToCard = CartItem(
      FirestoreClass().getCurrentUserID(),
      mProductOwnerId,
      mProductId,
      mProductDetails.title,
      mProductDetails.price,
      mProductDetails.image,
      Constants.DEFAULT_CART_QUANTITY
    )

    showProgressDialog(resources.getString(R.string.please_wait))
    FirestoreClass().addCartItems(this, addToCard)
  }

  /**
   * A function to notify the success result of item added to the to cart.
   */
  fun addToCartSuccess() {
    hideProgressDialog()

    Toast.makeText(
      this@ProductDetailsActivity,
      resources.getString(R.string.success_message_item_added_to_cart),
      Toast.LENGTH_SHORT
    ).show()

    // Hide the AddToCart button if the item is already in the cart.
    views.btnAddToCart.visibility = View.GONE
    // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
    views.btnGoToCart.visibility = View.VISIBLE
  }

  /**
   * A function to notify the success result of item exists in the cart.
   */
  fun productExistsInCart() {

    hideProgressDialog()

    // Hide the AddToCart button if the item is already in the cart.
    views.btnAddToCart.visibility = View.GONE
    // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
    views.btnGoToCart.visibility = View.VISIBLE
  }

  override fun onClick(v: View?) {
    if (v != null) {
      when (v.id) {
        R.id.btn_add_to_cart -> {
          addToCard()
        }

        R.id.btn_go_to_cart -> {
          startActivity(Intent(this, CartListActivity::class.java))
        }
      }
    }
  }
}