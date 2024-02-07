package com.example.shopping.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.shopping.R
import com.example.shopping.databinding.ActivitySettingsBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.User
import com.example.shopping.utils.Constants
import com.example.shopping.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth

/**
 * Setting screen of the app.
 */
class SettingsActivity : BaseActivity(), View.OnClickListener {

  private lateinit var views: ActivitySettingsBinding
  private lateinit var mUserDetails: User

  /**
   * This function is auto created by Android when the Activity Class is created.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivitySettingsBinding.inflate(layoutInflater)
    setContentView(views.root)

    setupActionBar()

    views.tvEdit.setOnClickListener(this@SettingsActivity)
    views.btnLogout.setOnClickListener(this@SettingsActivity)
    views.llAddress.setOnClickListener(this@SettingsActivity)
  }

  override fun onResume() {
    super.onResume()

    getUserDetails()
  }

  override fun onClick(v: View?) {
    if (v != null) {
      when (v.id) {

        R.id.tv_edit -> {
          val intent = Intent(this@SettingsActivity, UserProfileActivity::class.java)
          intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
          startActivity(intent)
        }

        R.id.btn_logout -> {
          showAlertDialogToLogout()
        }

        R.id.ll_address -> {
          val intent = Intent(this@SettingsActivity, AddressListActivity::class.java)
          startActivity(intent)
        }
      }
    }
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarSettingsActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarSettingsActivity.setNavigationOnClickListener { onBackPressed() }
  }

  /**
   * A function to get the user details from firestore.
   */
  private fun getUserDetails() {

    showProgressDialog(resources.getString(R.string.please_wait))

    // Call the function of Firestore class to get the user details from firestore which is already created.
    FirestoreClass().getUserDetails(this@SettingsActivity)
  }

  /**
   * A function to receive the user details and populate it in the UI.
   */
  fun userDetailsSuccess(user: User) {

    mUserDetails = user
    hideProgressDialog()

    // Load the image using the Glide Loader class.
    GlideLoader(this@SettingsActivity).loadUserPicture(user.image, views.ivUserPhoto)

    views.tvName.text = "${user.firstName} ${user.lastName}"
    views.tvGender.text = user.gender
    views.tvEmail.text = user.email
    views.tvMobileNumber.text = "${user.mobile}"
  }

  private fun showAlertDialogToLogout() {

    val builder = AlertDialog.Builder(this)
    builder.setTitle(resources.getString(R.string.logout_dialog_title))
    builder.setMessage(resources.getString(R.string.logout_dialog_message))
    builder.setIcon(android.R.drawable.ic_dialog_alert)

    builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->

      showProgressDialog(resources.getString(R.string.please_wait))
      FirebaseAuth.getInstance().signOut()

      val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      startActivity(intent)
      finish()
      dialogInterface.dismiss()
    }

    builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
      dialogInterface.dismiss()
    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.setCancelable(false)
    alertDialog.show()
  }
}