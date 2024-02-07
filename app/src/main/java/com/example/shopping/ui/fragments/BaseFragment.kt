package com.example.shopping.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shopping.R
import com.example.shopping.utils.CustomTextView

/**
 * A simple [Fragment] subclass.
 * Use the [BaseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class BaseFragment : Fragment() {

  private lateinit var mProgressDialog: Dialog

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_base, container, false)
  }

  fun showProgressDialog(text: String) {

    mProgressDialog = Dialog(requireActivity())

    /*Set the screen content from a layout resource.
    The resource will be inflated, adding all top-level views to the screen.*/
    mProgressDialog.setContentView(R.layout.dialog_progress)
    mProgressDialog.findViewById<CustomTextView>(R.id.tv_progress_text).text = text
    mProgressDialog.setCancelable(false)
    mProgressDialog.setCanceledOnTouchOutside(false)

    //Start the dialog and display it on screen.
    mProgressDialog.show()
  }

  fun hideProgressDialog() {
    mProgressDialog.dismiss()
  }
}