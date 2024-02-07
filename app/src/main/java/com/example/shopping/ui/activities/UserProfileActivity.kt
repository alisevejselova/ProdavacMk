package com.example.shopping.ui.activities

import android.Manifest
import android.app.Activity
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
import com.example.shopping.databinding.ActivityUserProfileAcivityBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.User
import com.example.shopping.utils.Constants
import com.example.shopping.utils.GlideLoader
import java.io.IOException

/**
 * A user profile screen.
 */
class UserProfileActivity : BaseActivity(), View.OnClickListener {

  private lateinit var views: ActivityUserProfileAcivityBinding
  private lateinit var mUserDetails: User
  private var mSelectedImageUri: Uri? = null
  private var mUserProfileImageURL: String = ""

  /**
   * This function is auto created by Android when the Activity Class is created.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityUserProfileAcivityBinding.inflate(layoutInflater)
    setContentView(views.root)

    if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
      mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
    }

    views.etFirstName.setText(mUserDetails.firstName)
    views.etLastName.setText(mUserDetails.lastName)
    views.etEmail.isEnabled = false
    views.etEmail.setText(mUserDetails.email)

    if (mUserDetails.profileCompleted == 0) {
      views.tvTitle.text = resources.getString(R.string.title_complete_profile)
      views.etFirstName.isEnabled = false
      views.etLastName.isEnabled = false
    } else {
      setupActionBar()
      views.tvTitle.text = resources.getString(R.string.title_edit_profile)
      GlideLoader(this).loadUserPicture(mUserDetails.image, views.ivUserPhoto)

      if (mUserDetails.mobile != 0L) {
        views.etMobileNumber.setText(mUserDetails.mobile.toString())
      }
      if (mUserDetails.gender == Constants.MALE) {
        views.rbMale.isChecked = true
      } else {
        views.rbFemale.isChecked = true
      }
    }

    views.ivUserPhoto.setOnClickListener(this@UserProfileActivity)
    views.btnSave.setOnClickListener(this@UserProfileActivity)
  }

  override fun onClick(view: View?) {
    if (view != null) {
      when (view.id) {

        R.id.iv_user_photo -> {

          if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
          ) {
            Constants.showImageChooser(this@UserProfileActivity)
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

        R.id.btn_save -> {

          if (validateUserProfileDetails()) {

            showProgressDialog(resources.getString(R.string.please_wait))
            if (mSelectedImageUri != null) {
              FirestoreClass().uploadImageToCloudStorage(
                this,
                mSelectedImageUri,
                Constants.USER_PROFILE_IMAGE
              )
            } else {
              updateUserProfileDetails()
            }
          }
        }
      }
    }
  }

  private fun updateUserProfileDetails() {
    val userHashMap = HashMap<String, Any>()

    val firstName = views.etFirstName.text.toString().trim { it <= ' ' }
    if (firstName != mUserDetails.firstName) {
      userHashMap[Constants.FIRST_NAME] = firstName
    }
    val lastName = views.etLastName.text.toString().trim { it <= ' ' }
    if (firstName != mUserDetails.lastName) {
      userHashMap[Constants.LAST_NAME] = lastName
    }
    val mobileNumber = views.etMobileNumber.text.toString().trim { it <= ' ' }
    if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.mobile.toString()) {
      userHashMap[Constants.MOBILE] = mobileNumber.toLong()
    }

    val gender = if (views.rbMale.isChecked) {
      Constants.MALE
    } else {
      Constants.FEMALE
    }
    userHashMap[Constants.GENDER] = gender

    if (mUserProfileImageURL.isNotEmpty()) {
      userHashMap[Constants.IMAGE] = mUserProfileImageURL
    }

    if (gender.isNotEmpty() && gender != mUserDetails.gender) {
      userHashMap[Constants.GENDER] = gender
    }

    userHashMap[Constants.COMPLETE_PROFILE] = 1

    FirestoreClass().updateUserProfileData(this@UserProfileActivity, userHashMap)

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
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Constants.showImageChooser(this@UserProfileActivity)
      } else {
        Toast.makeText(
          this,
          resources.getString(R.string.read_storage_permission_denied),
          Toast.LENGTH_LONG
        ).show()
      }
    }
  }

  /**
   * Receive the result from a previous call to
   * {@link #startActivityForResult(Intent, int)}.  This follows the
   * related Activity API as described there in
   * {@link Activity#onActivityResult(int, int, Intent)}.
   *
   * @param requestCode The integer request code originally supplied to
   *                    startActivityForResult(), allowing you to identify who this
   *                    result came from.
   * @param resultCode The integer result code returned by the child activity
   *                   through its setResult().
   * @param data An Intent, which can return result data to the caller
   *               (various data can be attached to Intent "extras").
   */
  public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
        if (data != null) {
          try {
            mSelectedImageUri = data.data!!
            GlideLoader(this@UserProfileActivity).loadUserPicture(
              mSelectedImageUri!!,
              views.ivUserPhoto
            )
          } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
              this@UserProfileActivity,
              resources.getString(R.string.image_selection_failed),
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      }
    } else if (resultCode == Activity.RESULT_CANCELED) {
      // A log is printed when user close or cancel the image selection.
      Log.e("Request Cancelled", "Image selection cancelled")
    }
  }

  /**
   * A function to validate the input entries for profile details.
   */
  private fun validateUserProfileDetails(): Boolean {
    return when {

      // We have kept the user profile picture is optional.
      // The FirstName, LastName, and Email Id are not editable when they come from the login screen.
      // The Radio button for Gender always has the default selected value.

      // Check if the mobile number is not empty as it is mandatory to enter.
      TextUtils.isEmpty(views.etMobileNumber.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
        false
      }

      else -> {
        true
      }
    }
  }

  /**
   * A function to notify the success result and proceed further accordingly after updating the user details.
   */
  fun userProfileUpdateSuccess() {

    hideProgressDialog()

    Toast.makeText(
      this@UserProfileActivity,
      resources.getString(R.string.msg_profile_update_success),
      Toast.LENGTH_SHORT
    ).show()

    startActivity(Intent(this@UserProfileActivity, DashboardActivity::class.java))
    finish()
  }

  /**
   * A function to notify the success result of image upload to the Cloud Storage.
   *
   * @param imageURL After successful upload the Firebase Cloud returns the URL.
   */
  fun imageUploadSuccess(imageURL: String) {
    mUserProfileImageURL = imageURL
    updateUserProfileDetails()
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarUserProfileActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarUserProfileActivity.setNavigationOnClickListener { onBackPressed() }
  }
}