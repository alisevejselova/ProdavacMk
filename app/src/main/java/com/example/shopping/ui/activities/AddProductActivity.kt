package com.example.shopping.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shopping.R
import com.example.shopping.databinding.ActivityAddProductBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.Product
import com.example.shopping.utils.Constants
import com.example.shopping.utils.GlideLoader
import java.io.IOException

class AddProductActivity : BaseActivity(), View.OnClickListener {

  private lateinit var views: ActivityAddProductBinding

  // A global variable for URI of a selected image from phone storage.
  private var mSelectedImageFileUri: Uri? = null

  // A global variable for uploaded product image URL.
  private var mProductImageURL: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityAddProductBinding.inflate(layoutInflater)
    setContentView(views.root)
    setupActionBar()

    views.ivAddUpdateProductImage.setOnClickListener(this)
    views.btnSubmit.setOnClickListener(this)
  }

  override fun onClick(v: View?) {
    if (v != null) {
      when (v.id) {

        // The permission code is similar to the user profile image selection.
        R.id.iv_add_update_product_image -> {
          if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
          ) {
            Constants.showImageChooser(this@AddProductActivity)
          } else {
            /*Requests permissions to be granted to this application. These permissions
             must be requested in your manifest, they should not be granted to your app,
             and they should have protection level*/
            ActivityCompat.requestPermissions(
              this,
              arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
              Constants.READ_STORAGE_PERMISSION_CODE
            )
          }
        }

        R.id.btn_submit -> {
          if (validateProductDetails()) {

            uploadProductImage()
          }
        }
      }
    }
  }

  /**
   * A function to validate the product details.
   */
  private fun validateProductDetails(): Boolean {
    return when {

      mSelectedImageFileUri == null -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
        false
      }

      TextUtils.isEmpty(views.etProductTitle.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
        false
      }

      TextUtils.isEmpty(views.etProductPrice.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
        false
      }

      TextUtils.isEmpty(views.etProductDescription.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(
          resources.getString(R.string.err_msg_enter_product_description),
          true
        )
        false
      }

      TextUtils.isEmpty(views.etProductQuantity.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(
          resources.getString(R.string.err_msg_enter_product_quantity),
          true
        )
        false
      }

      else -> {
        true
      }
    }
  }

  /**
   * A function to upload the selected product image to firebase cloud storage.
   */
  private fun uploadProductImage() {

    showProgressDialog(resources.getString(R.string.please_wait))

    FirestoreClass().uploadImageToCloudStorage(
      this@AddProductActivity,
      mSelectedImageFileUri, Constants.PRODUCT_IMAGE
    )
  }

  /**
   * A function to get the successful result of product image upload.
   */
  fun imageUploadSuccess(imageURL: String) {

    mProductImageURL = imageURL

    uploadProductDetails()
  }

  private fun uploadProductDetails() {

    // Get the logged in username from the SharedPreferences that we have stored at a time of login.
    val username =
      this.getSharedPreferences(Constants.SHOPPING_PREFERENCES, Context.MODE_PRIVATE)
        .getString(Constants.LOGGED_IN_USERNAME, "")!!

    val product = Product(
      FirestoreClass().getCurrentUserID(),
      username,
      views.etProductTitle.text.toString().trim { it <= ' ' },
      views.etProductPrice.text.toString().trim { it <= ' ' },
      views.etProductDescription.text.toString().trim { it <= ' ' },
      views.etProductQuantity.text.toString().trim { it <= ' ' },
      mProductImageURL
    )

    FirestoreClass().uploadProductDetails(this@AddProductActivity, product)
  }

  /**
   * A function to return the successful result of Product upload.
   */
  fun productUploadSuccess() {

    // Hide the progress dialog
    hideProgressDialog()

    Toast.makeText(
      this@AddProductActivity,
      resources.getString(R.string.product_uploaded_success_message),
      Toast.LENGTH_SHORT
    ).show()

    finish()
  }

  /**
   * This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
   *
   * @param requestCode
   * @param permissions
   * @param grantResults
   */
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
      //If permission is granted
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Constants.showImageChooser(this@AddProductActivity)
      } else {
        //Displaying another toast if permission is not granted
        Toast.makeText(
          this,
          resources.getString(R.string.read_storage_permission_denied), Toast.LENGTH_LONG
        ).show()
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
      if (data!!.data != null) {
        // Replace the add icon with edit icon once the image is selected.
        views.ivAddUpdateProductImage
          .setImageDrawable(
            ContextCompat.getDrawable(
              this@AddProductActivity,
              R.drawable.ic_vector_edit
            )
          )

        mSelectedImageFileUri = data.data!!

        try {
          // Load the product image in the ImageView.
          GlideLoader(this@AddProductActivity)
            .loadProductPicture(mSelectedImageFileUri!!, views.ivProductImage)
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }
    } else {
      if (resultCode == Activity.RESULT_CANCELED) {
        // A log is printed when user close or cancel the image selection.
        Log.e("Request Cancelled", "Image selection cancelled")
      }
    }
  }


  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarAddProductActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarAddProductActivity.setNavigationOnClickListener { onBackPressed() }
  }
}