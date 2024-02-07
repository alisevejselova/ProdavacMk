package com.example.shopping.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.databinding.ActivityAddresListBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.Address
import com.example.shopping.ui.adapters.AddressListAdapter
import com.example.shopping.utils.Constants
import com.example.shopping.utils.SwipeToDeleteCallback
import com.example.shopping.utils.SwipeToEditCallback

class AddressListActivity : BaseActivity() {

  private lateinit var views: ActivityAddresListBinding
  private var mSelectAddress: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivityAddresListBinding.inflate(layoutInflater)
    setContentView(views.root)

    if (intent.hasExtra(Constants.EXTRA_SELECT_ADDRESS)) {
      mSelectAddress = intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESS, false)
    }

    setupActionBar()
    getAddressList()

    if (mSelectAddress) {
      views.tvTitle.text = resources.getString(R.string.title_select_address)
    }

    views.tvAddAddress.setOnClickListener {
      val intent = Intent(this@AddressListActivity, AddEditAddressActivity::class.java)
      startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)
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
      if (requestCode == Constants.ADD_ADDRESS_REQUEST_CODE) {
        getAddressList()
      }
    } else if (resultCode == Activity.RESULT_CANCELED) {
      Log.e("Request Cancelled", "To add the address.")
    }
  }

  /**
   * A function to get the list of address from cloud firestore.
   */
  private fun getAddressList() {

    showProgressDialog(resources.getString(R.string.please_wait))
    FirestoreClass().getAddressesList(this@AddressListActivity)
  }

  /**
   * A function to get the success result of address list from cloud firestore.
   *
   * @param addressList
   */
  fun successAddressListFromFirestore(addressList: ArrayList<Address>) {

    hideProgressDialog()

    if (addressList.size > 0) {

      views.rvAddressList.visibility = View.VISIBLE
      views.tvNoAddressFound.visibility = View.GONE

      views.rvAddressList.layoutManager = LinearLayoutManager(this@AddressListActivity)
      views.rvAddressList.setHasFixedSize(true)

      val addressAdapter = AddressListAdapter(this@AddressListActivity, addressList, mSelectAddress)
      views.rvAddressList.adapter = addressAdapter

      if (!mSelectAddress) {
        val editSwipeHandler = object : SwipeToEditCallback(this) {
          override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            val adapter = views.rvAddressList.adapter as AddressListAdapter
            adapter.notifyEditItem(
              this@AddressListActivity,
              viewHolder.adapterPosition
            )
          }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(views.rvAddressList)


        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
          override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            showProgressDialog(resources.getString(R.string.please_wait))

            FirestoreClass().deleteAddress(
              this@AddressListActivity,
              addressList[viewHolder.adapterPosition].id
            )
          }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(views.rvAddressList)
      }
    } else {
      views.rvAddressList.visibility = View.GONE
      views.tvNoAddressFound.visibility = View.VISIBLE
    }
  }

  /**
   * A function notify the user that the address is deleted successfully.
   */
  fun deleteAddressSuccess() {

    hideProgressDialog()

    Toast.makeText(
      this@AddressListActivity,
      resources.getString(R.string.err_your_address_deleted_successfully),
      Toast.LENGTH_SHORT
    ).show()

    getAddressList()
  }

  /**
   * A function for actionBar Setup.
   */
  private fun setupActionBar() {

    setSupportActionBar(views.toolbarAddressListActivity)

    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
    }

    views.toolbarAddressListActivity.setNavigationOnClickListener { onBackPressed() }
  }
}