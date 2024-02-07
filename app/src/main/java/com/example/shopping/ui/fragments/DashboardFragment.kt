package com.example.shopping.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.FragmentDashboardBinding
import com.example.shopping.firestore.FirestoreClass
import com.example.shopping.models.Product
import com.example.shopping.ui.activities.CartListActivity
import com.example.shopping.ui.activities.ProductDetailsActivity
import com.example.shopping.ui.activities.SettingsActivity
import com.example.shopping.ui.adapters.DashboardItemsListAdapter
import com.example.shopping.utils.Constants

class DashboardFragment : BaseFragment() {

  private var _views: FragmentDashboardBinding? = null

  private val views get() = _views!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onResume() {
    super.onResume()

    getDashboardItemsList()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _views = FragmentDashboardBinding.inflate(inflater, container, false)
    val root: View = views.root

    // Set up SwipeRefreshLayout
    views.swipeRefreshLayout.setOnRefreshListener {
      // This method will be invoked when the user performs a swipe-to-refresh gesture
      getDashboardItemsList()
    }

    return root
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.dashboard_menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val id = item.itemId
    when (id) {
      R.id.action_settings -> {
        startActivity(Intent(activity, SettingsActivity::class.java))
        return true
      }

      R.id.action_cart -> {
        startActivity(Intent(activity, CartListActivity::class.java))
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _views = null
  }

  /**
   * A function to get the dashboard items list from cloud firestore.
   */
  private fun getDashboardItemsList() {
    showProgressDialog(resources.getString(R.string.please_wait))

    FirestoreClass().getDashboardItemsList(this@DashboardFragment)
  }

  /**
   * A function to get the success result of the dashboard items from cloud firestore.
   *
   * @param dashboardItemsList
   */
  fun successDashboardItemsList(dashboardItemsList: ArrayList<Product>) {

    hideProgressDialog()

    if (dashboardItemsList.size > 0) {

      views.rvDashboardItems.visibility = View.VISIBLE
      views.tvNoDashboardItemsFound.visibility = View.GONE

      views.rvDashboardItems.layoutManager = GridLayoutManager(activity, 2)
      views.rvDashboardItems.setHasFixedSize(true)

      val adapter = DashboardItemsListAdapter(requireActivity(), dashboardItemsList)
      views.rvDashboardItems.adapter = adapter

      adapter.setOnClickListener(object : DashboardItemsListAdapter.OnClickListener {
        override fun onClick(position: Int, product: Product) {
          val intent = Intent(context, ProductDetailsActivity::class.java)
          intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.product_id)
          intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.user_id)
          startActivity(intent)
        }
      }
      )

      // Stop refreshing animation
      views.swipeRefreshLayout.isRefreshing = false
    } else {
      views.rvDashboardItems.visibility = View.GONE
      views.tvNoDashboardItemsFound.visibility = View.VISIBLE
    }
  }
}