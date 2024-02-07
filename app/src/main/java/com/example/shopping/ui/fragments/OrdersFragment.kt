package com.example.shopping.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.FragmentOrdersBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.Order
import com.example.shopping.ui.activities.SettingsActivity
import com.example.shopping.ui.adapters.MyOrdersListAdapter

class OrdersFragment : BaseFragment() {

  private var _binding: FragmentOrdersBinding? = null

  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentOrdersBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onResume() {
    super.onResume()

    getMyOrdersList()
  }

  /**
   * A function to get the list of my orders.
   */
  private fun getMyOrdersList() {
    showProgressDialog(resources.getString(R.string.please_wait))

    FirestoreClass().getMyOrdersList(this@OrdersFragment)
  }


  /**
   * A function to get the success result of the my order list from cloud firestore.
   *
   * @param ordersList List of my orders.
   */
  fun populateOrdersListInUI(ordersList: ArrayList<Order>) {

    hideProgressDialog()

    if (ordersList.size > 0) {

      binding.rvMyOrderItems.visibility = View.VISIBLE
      binding.tvNoOrdersFound.visibility = View.GONE

      binding.rvMyOrderItems.layoutManager = LinearLayoutManager(activity)
      binding.rvMyOrderItems.setHasFixedSize(true)

      val myOrdersAdapter = MyOrdersListAdapter(requireActivity(), ordersList, this)
      binding.rvMyOrderItems.adapter = myOrdersAdapter
    } else {
      binding.rvMyOrderItems.visibility = View.GONE
      binding.tvNoOrdersFound.visibility = View.VISIBLE
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.orders_menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    when (id) {
      R.id.action_settings -> {
        startActivity(Intent(activity, SettingsActivity::class.java))
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  fun deleteOrder(orderId: String) {

    showAlertDialogToDeleteOrder(orderId)
  }

  private fun showAlertDialogToDeleteOrder(orderId: String) {

    val builder = AlertDialog.Builder(requireActivity())
    builder.setTitle(resources.getString(R.string.delete_dialog_title))
    builder.setMessage(resources.getString(R.string.delete_order_dialog_message))
    builder.setIcon(android.R.drawable.ic_dialog_alert)

    builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->

      showProgressDialog(resources.getString(R.string.please_wait))
      FirestoreClass().deleteOrder(this@OrdersFragment, orderId)
      dialogInterface.dismiss()
    }

    builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
      dialogInterface.dismiss()
    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.setCancelable(false)
    alertDialog.show()
  }

  fun orderDeleteSuccess() {

    hideProgressDialog()
    Toast.makeText(
      requireActivity(),
      resources.getString(R.string.product_delete_success_message),
      Toast.LENGTH_SHORT
    ).show()

    // Get the latest products list from cloud firestore.
    getMyOrdersList()
  }
}