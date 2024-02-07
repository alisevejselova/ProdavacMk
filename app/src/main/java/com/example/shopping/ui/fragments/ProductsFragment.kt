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
import com.example.shopping.databinding.FragmentProductsBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.Product
import com.example.shopping.ui.activities.AddProductActivity
import com.example.shopping.ui.activities.SettingsActivity
import com.example.shopping.ui.adapters.MyProductsListAdapter

class ProductsFragment : BaseFragment() {

  private var _views: FragmentProductsBinding? = null
  private val views get() = _views!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _views = FragmentProductsBinding.inflate(inflater, container, false)
    val root: View = views.root
    return root
  }

  override fun onResume() {
    super.onResume()
    getProductListFromFireStore()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _views = null
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.add_product_menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId

    when (id) {
      R.id.action_add_product -> {
        startActivity(Intent(activity, AddProductActivity::class.java))
        return true
      }

      R.id.action_settings -> {
        startActivity(Intent(activity, SettingsActivity::class.java))
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun getProductListFromFireStore() {
    showProgressDialog(resources.getString(R.string.please_wait))
    FirestoreClass().getProductsList(this@ProductsFragment)
  }

  /**
   * A function to get the successful product list from cloud firestore.
   *
   * @param productsList Will receive the product list from cloud firestore.
   */
  fun successProductsListFromFireStore(productsList: ArrayList<Product>) {

    hideProgressDialog()

    if (productsList.size > 0) {
      views.rvMyProductItems.visibility = View.VISIBLE
      views.tvNoProductsFound.visibility = View.GONE

      views.rvMyProductItems.layoutManager = LinearLayoutManager(activity)
      views.rvMyProductItems.setHasFixedSize(true)

      val adapterProducts =
        MyProductsListAdapter(requireActivity(), productsList, this@ProductsFragment)
      views.rvMyProductItems.adapter = adapterProducts
    } else {
      views.rvMyProductItems.visibility = View.GONE
      views.tvNoProductsFound.visibility = View.VISIBLE
    }
  }

  /**
   * A function that will call the delete function of FirestoreClass that will delete the product added by the user.
   *
   * @param productID To specify which product need to be deleted.
   */
  fun deleteProduct(productID: String) {

    showAlertDialogToDeleteProduct(productID)
  }

  /**
   * A function to notify the success result of product deleted from cloud firestore.
   */
  fun productDeleteSuccess() {

    hideProgressDialog()

    Toast.makeText(
      requireActivity(),
      resources.getString(R.string.product_delete_success_message),
      Toast.LENGTH_SHORT
    ).show()

    getProductListFromFireStore()
  }

  /**
   * A function to show the alert dialog for the confirmation of delete product from cloud firestore.
   */
  private fun showAlertDialogToDeleteProduct(productID: String) {

    val builder = AlertDialog.Builder(requireActivity())
    builder.setTitle(resources.getString(R.string.delete_dialog_title))
    builder.setMessage(resources.getString(R.string.delete_dialog_message))
    builder.setIcon(android.R.drawable.ic_dialog_alert)
    builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->

      showProgressDialog(resources.getString(R.string.please_wait))
      FirestoreClass().deleteProduct(this@ProductsFragment, productID)
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