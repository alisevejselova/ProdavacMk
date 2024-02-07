package com.example.shopping.ui.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.shopping.R
import com.example.shopping.databinding.ActivityAddEditAddressBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.Address
import com.example.shopping.utils.Constants

class AddEditAddressActivity : BaseActivity() {

  private lateinit var views: ActivityAddEditAddressBinding
  private var mAddressDetails: Address? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    views = ActivityAddEditAddressBinding.inflate(layoutInflater)
    setContentView(views.root)

    setupActionBar()

    if (intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)) {
      mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)!!
    }

    if (mAddressDetails != null) {
      if (mAddressDetails!!.id.isNotEmpty()) {

        views.tvTitle.text = resources.getString(R.string.title_edit_address)
        views.btnSubmitAddress.text = resources.getString(R.string.btn_lbl_update)

        views.etFullName.setText(mAddressDetails?.name)
        views.etPhoneNumber.setText(mAddressDetails?.mobileNumber)
        views.etAddress.setText(mAddressDetails?.address)
        views.etZipCode.setText(mAddressDetails?.zipCode)
        views.etAdditionalNote.setText(mAddressDetails?.additionalNote)

        when (mAddressDetails?.type) {
          Constants.HOME -> {
            views.rbHome.isChecked = true
          }

          Constants.OFFICE -> {
            views.rbOffice.isChecked = true
          }

          else -> {
            views.rbOther.isChecked = true
            views.tilOtherDetails.visibility = View.VISIBLE
            views.etOtherDetails.setText(mAddressDetails?.otherDetails)
          }
        }
      }
    }

    views.rgType.setOnCheckedChangeListener { _, checkedId ->
      if (checkedId == R.id.rb_other) {
        views.tilOtherDetails.visibility = View.VISIBLE
      } else {
        views.tilOtherDetails.visibility = View.GONE
      }
    }

    views.btnSubmitAddress.setOnClickListener {
      saveAddressToFirestore()
    }
  }

  /**
   * A function to validate the address input entries.
   */
  private fun validateData(): Boolean {
    return when {

      TextUtils.isEmpty(views.etFullName.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(
          resources.getString(R.string.err_msg_please_enter_full_name),
          true
        )
        false
      }

      TextUtils.isEmpty(views.etPhoneNumber.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(
          resources.getString(R.string.err_msg_please_enter_phone_number),
          true
        )
        false
      }

      TextUtils.isEmpty(views.etAddress.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_address), true)
        false
      }

      TextUtils.isEmpty(views.etZipCode.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
        false
      }

      views.rbOther.isChecked && TextUtils.isEmpty(
        views.etZipCode.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
        false
      }

      else -> {
        true
      }
    }
  }

  /**
   * A function to save the address to the cloud firestore.
   */
  private fun saveAddressToFirestore() {

    val fullName: String = views.etFullName.text.toString().trim { it <= ' ' }
    val phoneNumber: String = views.etPhoneNumber.text.toString().trim { it <= ' ' }
    val address: String = views.etAddress.text.toString().trim { it <= ' ' }
    val zipCode: String = views.etZipCode.text.toString().trim { it <= ' ' }
    val additionalNote: String = views.etAdditionalNote.text.toString().trim { it <= ' ' }
    val otherDetails: String = views.etOtherDetails.text.toString().trim { it <= ' ' }

    if (validateData()) {
      showProgressDialog(resources.getString(R.string.please_wait))

      val addressType: String = when {
        views.rbHome.isChecked -> {
          Constants.HOME
        }

        views.rbOffice.isChecked -> {
          Constants.OFFICE
        }

        else -> {
          Constants.OTHER
        }
      }

      val addressModel = Address(
        FirestoreClass().getCurrentUserID(),
        fullName,
        phoneNumber,
        address,
        zipCode,
        additionalNote,
        addressType,
        otherDetails
      )

      if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()) {
        FirestoreClass().updateAddress(
          this@AddEditAddressActivity,
          addressModel,
          mAddressDetails!!.id
        )
      } else {
        FirestoreClass().addAddress(this@AddEditAddressActivity, addressModel)
      }
    }
  }

  /**
   * A function to notify the success result of address saved.
   */
  fun addUpdateAddressSuccess() {

    // Hide progress dialog
    hideProgressDialog()

    val notifySuccessMessage: String =
      if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()) {
        resources.getString(R.string.msg_your_address_updated_successfully)
      } else {
        resources.getString(R.string.err_your_address_added_successfully)
      }

    Toast.makeText(
      this@AddEditAddressActivity,
      notifySuccessMessage,
      Toast.LENGTH_SHORT
    ).show()

    setResult(RESULT_OK)
    finish()
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarAddEditAddressActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarAddEditAddressActivity.setNavigationOnClickListener { onBackPressed() }
  }


}