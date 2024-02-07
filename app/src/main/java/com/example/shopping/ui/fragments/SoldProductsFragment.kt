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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.FragmentSoldProductsBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.SoldProduct
import com.example.shopping.ui.activities.SettingsActivity
import com.example.shopping.ui.adapters.SoldProductsListAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [SoldProductsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SoldProductsFragment : BaseFragment() {

  private var _binding: FragmentSoldProductsBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentSoldProductsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onResume() {
    super.onResume()

    getSoldProductsList()
  }

  private fun getSoldProductsList() {
    showProgressDialog(resources.getString(R.string.please_wait))
    FirestoreClass().getSoldProductsList(this@SoldProductsFragment)
  }

  fun deleteSoldProduct(orderId: String) {

    showAlertDialogToDeleteSoldProduct(orderId)
  }

  private fun showAlertDialogToDeleteSoldProduct(orderId: String) {

    val builder = AlertDialog.Builder(requireActivity())
    builder.setTitle(resources.getString(R.string.delete_dialog_title))
    builder.setMessage(resources.getString(R.string.delete_sold_product_dialog_message))
    builder.setIcon(android.R.drawable.ic_dialog_alert)

    builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
      showProgressDialog(resources.getString(R.string.please_wait))
      FirestoreClass().deleteSoldProduct(this@SoldProductsFragment, orderId)

      dialogInterface.dismiss()
    }

    builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
      dialogInterface.dismiss()
    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.setCancelable(false)
    alertDialog.show()
  }


  fun soldProdutDeleteSuccess() {

    hideProgressDialog()
    Toast.makeText(
      requireActivity(),
      resources.getString(R.string.product_delete_success_message),
      Toast.LENGTH_SHORT
    ).show()

    getSoldProductsList()
  }

  /**
   * A function to get the list of sold products.
   */
  fun successSoldProductsList(soldProductsList: ArrayList<SoldProduct>) {

    hideProgressDialog()

    if (soldProductsList.size > 0) {
      binding.rvSoldProductItems.visibility = View.VISIBLE
      binding.tvNoSoldProductsFound.visibility = View.GONE

      binding.rvSoldProductItems.layoutManager = LinearLayoutManager(activity)
      binding.rvSoldProductItems.setHasFixedSize(true)

      val soldProductsListAdapter =
        SoldProductsListAdapter(requireActivity(), soldProductsList, this)
      binding.rvSoldProductItems.adapter = soldProductsListAdapter
    } else {
      binding.rvSoldProductItems.visibility = View.GONE
      binding.tvNoSoldProductsFound.visibility = View.VISIBLE
    }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.sold_products_menu, menu)
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
}