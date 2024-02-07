package com.example.shopping.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.example.shopping.R
import com.example.shopping.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Forgot Password Screen of the application.
 */
class ForgotPasswordActivity : BaseActivity() {

  private lateinit var views: ActivityForgotPasswordBinding

  /**
   * This function is auto created by Android when the Activity Class is created.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityForgotPasswordBinding.inflate(layoutInflater)
    setContentView(views.root)

    setupActionBar()

    views.btnSubmit.setOnClickListener {

      val email: String = views.etEmail.text.toString().trim { it <= ' ' }
      if (email.isEmpty()) {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
      } else {
        showProgressDialog(resources.getString(R.string.please_wait))

        // This piece of code is used to send the reset password link to the user's email id if the user is registered.
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
          .addOnCompleteListener { task ->
            hideProgressDialog()

            if (task.isSuccessful) {
              Toast.makeText(
                this@ForgotPasswordActivity,
                resources.getString(R.string.email_sent_success),
                Toast.LENGTH_LONG
              ).show()
              finish()
            } else {
              showErrorSnackBar(task.exception!!.message.toString(), true)
            }
          }
      }
    }
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarForgotPasswordActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.title = null
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }
    views.toolbarForgotPasswordActivity.setNavigationOnClickListener { onBackPressed() }
  }
}