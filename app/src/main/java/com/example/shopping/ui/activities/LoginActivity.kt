package com.example.shopping.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.example.shopping.R
import com.example.shopping.databinding.ActivityLoginBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.User
import com.example.shopping.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity(), View.OnClickListener {

  private lateinit var views: ActivityLoginBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(views.root)

    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
    }
    // Click event assigned to Forgot Password text.
    views.tvForgotPassword.setOnClickListener(this)
    // Click event assigned to Login button.
    views.btnLogin.setOnClickListener(this)
    // Click event assigned to Register text.
    views.tvRegister.setOnClickListener(this)

  }

  /**
   * In Login screen the clickable components are Login Button, ForgotPassword text and Register Text.
   */
  override fun onClick(view: View?) {
    if (view != null) {
      when (view.id) {

        R.id.tv_forgot_password -> {

          // Launch the forgot password screen when the user clicks on the forgot password text.
          val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
          startActivity(intent)
        }

        R.id.btn_login -> {

          logInRegisteredUser()
        }

        R.id.tv_register -> {
          // Launch the register screen when the user clicks on the text.
          val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
          startActivity(intent)
        }
      }
    }
  }

  /**
   * A function to validate the login entries of a user.
   */
  private fun validateLoginDetails(): Boolean {
    return when {
      TextUtils.isEmpty(views.etEmail.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
        false
      }

      TextUtils.isEmpty(views.etPassword.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
        false
      }

      else -> {
        true
      }
    }
  }

  /**
   * A function to Log-In. The user will be able to log in using the registered email and password with Firebase Authentication.
   */
  private fun logInRegisteredUser() {

    if (validateLoginDetails()) {

      showProgressDialog(resources.getString(R.string.please_wait))

      val email = views.etEmail.text.toString().trim { it <= ' ' }
      val password = views.etPassword.text.toString().trim { it <= ' ' }

      // Log-In using FirebaseAuth
      FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->

          if (task.isSuccessful) {
            FirestoreClass().getUserDetails(this@LoginActivity)
          } else {
            hideProgressDialog()
            showErrorSnackBar(task.exception!!.message.toString(), true)
          }
        }
    }
  }

  /**
   * A function to notify user that logged in success and get the user details from the FireStore database after authentication.
   */
  fun userLoggedInSuccess(user: User) {

    hideProgressDialog()

    if (user.profileCompleted == 0) {
      // If the user profile is incomplete then launch the UserProfileActivity.
      val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
      intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
      startActivity(intent)
    } else {
      // Redirect the user to Main Screen after log in.
      startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
    }
    finish()
  }
}