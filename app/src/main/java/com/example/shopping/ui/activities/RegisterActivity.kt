package com.example.shopping.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.shopping.R
import com.example.shopping.databinding.ActivityRegisterBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : BaseActivity() {

  private lateinit var views: ActivityRegisterBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityRegisterBinding.inflate(layoutInflater)
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
    setupActionBar()
    views = ActivityRegisterBinding.inflate(layoutInflater)
    setContentView(views.root)

    views.tvLogin.setOnClickListener {
      onBackPressed()
    }

    views.btnRegister.setOnClickListener { registerUser() }
  }

  private fun setupActionBar() {

    setSupportActionBar(views.toolbarRegisterActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_back_narrow)
    }

    views.toolbarRegisterActivity.setNavigationOnClickListener { onBackPressed() }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }

  /**
   * A function to validate the entries of a new user.
   */
  private fun validateRegisterDetails(): Boolean {
    return when {
      TextUtils.isEmpty(views.etFirstName.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
        false
      }

      TextUtils.isEmpty(views.etLastName.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
        false
      }

      TextUtils.isEmpty(views.etEmail.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
        false
      }

      TextUtils.isEmpty(views.etPassword.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
        false
      }

      TextUtils.isEmpty(views.etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
        false
      }

      views.etPassword.text.toString().trim { it <= ' ' } != views.etConfirmPassword.text.toString()
        .trim { it <= ' ' } -> {
        showErrorSnackBar(
          resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
          true
        )
        false
      }

      !views.cbTermsAndCondition.isChecked -> {
        showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
        false
      }

      else -> {
        // showErrorSnackBar(resources.getString(R.string.registry_successful), false)
        true
      }
    }
  }

  /**
   * A function to register the user with email and password using FirebaseAuth.
   */
  private fun registerUser() {

    // Check with validate function if the entries are valid or not.
    if (validateRegisterDetails()) {

      showProgressDialog(resources.getString(R.string.please_wait))

      val email: String = views.etEmail.text.toString().trim { it <= ' ' }
      val password: String = views.etConfirmPassword.text.toString().trim { it <= ' ' }

      // Create an instance and create a register a user with email and password.
      FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(
          OnCompleteListener<AuthResult> { task ->
            // If the registration is successfully done
            if (task.isSuccessful) {

              // Firebase registered user
              val firebaseUser: FirebaseUser = task.result!!.user!!

              val user = User(
                firebaseUser.uid,
                views.etFirstName.text.toString().trim { it <= ' ' },
                views.etLastName.text.toString().trim { it <= ' ' },
                views.etEmail.text.toString().trim { it <= ' ' }
              )

              FirestoreClass().registerUser(this, user)
            } else {
              hideProgressDialog()
              // If the registering is not successful then show error message.
              showErrorSnackBar(task.exception!!.message.toString(), true)
            }
          })
    }
  }

  fun userRegistrationSuccess() {
    hideProgressDialog()
    Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show()
    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
    startActivity(intent)
    finish()
  }
}