package com.example.shopping.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.shopping.models.Address
import com.example.shopping.models.CartItem
import com.example.shopping.models.Order
import com.example.shopping.models.Product
import com.example.shopping.models.SoldProduct
import com.example.shopping.models.User
import com.example.shopping.ui.activities.AddEditAddressActivity
import com.example.shopping.ui.activities.AddProductActivity
import com.example.shopping.ui.activities.AddressListActivity
import com.example.shopping.ui.activities.CartListActivity
import com.example.shopping.ui.activities.CheckoutActivity
import com.example.shopping.ui.activities.LoginActivity
import com.example.shopping.ui.activities.ProductDetailsActivity
import com.example.shopping.ui.activities.RegisterActivity
import com.example.shopping.ui.activities.SettingsActivity
import com.example.shopping.ui.activities.SoldProductDetailsActivity
import com.example.shopping.ui.activities.UserProfileActivity
import com.example.shopping.ui.fragments.DashboardFragment
import com.example.shopping.ui.fragments.OrdersFragment
import com.example.shopping.ui.fragments.ProductsFragment
import com.example.shopping.ui.fragments.SoldProductsFragment
import com.example.shopping.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

  private val mFireStore = FirebaseFirestore.getInstance()

  /**
   * A function to make an entry of the registered user in the FireStore database.
   */
  fun registerUser(activity: RegisterActivity, userInfo: User) {

    // The "users" is collection name. If the collection is already created then it will not create the same one again.
    mFireStore.collection(Constants.USERS) //like a table
      // Document ID for users fields. Here the document it is the User ID.
      .document(userInfo.id)
      // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
      .set(userInfo, SetOptions.merge())
      .addOnSuccessListener {
        // Here call a function of base activity for transferring the result to it.
        activity.userRegistrationSuccess()
      }
      .addOnFailureListener { e ->
        activity.hideProgressDialog()
        Log.e(
          activity.javaClass.simpleName,
          "Error while registering the user.",
          e
        )
      }
  }

  /**
   * A function to get the user id of current logged user.
   */
  fun getCurrentUserID(): String {
    // An Instance of currentUser using FirebaseAuth
    val currentUser = FirebaseAuth.getInstance().currentUser

    // A variable to assign the currentUserId if it is not null or else it will be blank.
    var currentUserID = ""
    if (currentUser != null) {
      currentUserID = currentUser.uid
    }

    return currentUserID
  }

  /**
   * A function to get the logged user details from from FireStore Database.
   */
  fun getUserDetails(activity: Activity) {

    mFireStore.collection(Constants.USERS)
      // The document id to get the Fields of user.
      .document(getCurrentUserID())
      .get()
      .addOnSuccessListener { document ->

        Log.i(activity.javaClass.simpleName, document.toString())

        // Here we have received the document snapshot which is converted into the User Data model object.
        val user = document.toObject(User::class.java)!!

        val sharedPreferences =
          activity.getSharedPreferences(
            Constants.SHOPPING_PREFERENCES,
            Context.MODE_PRIVATE
          )

        // Create an instance of the editor which is help us to edit the SharedPreference.
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(
          Constants.LOGGED_IN_USERNAME,
          "${user.firstName} ${user.lastName}"
        )
        editor.apply()

        when (activity) {
          is LoginActivity -> {
            // Call a function of base activity for transferring the result to it.
            activity.userLoggedInSuccess(user)
          }

          is SettingsActivity -> {
            activity.userDetailsSuccess(user)
          }
        }
      }
      .addOnFailureListener { e ->
        // Hide the progress dialog if there is any error. And print the error in log.
        when (activity) {
          is LoginActivity -> {
            activity.hideProgressDialog()
          }

          is SettingsActivity -> {
            activity.hideProgressDialog()
          }
        }

        Log.e(activity.javaClass.simpleName, "Error while getting user details.", e)
      }
  }

  /**
   * A function to update the user profile data into the database.
   *
   * @param activity The activity is used for identifying the Base activity to which the result is passed.
   * @param userHashMap HashMap of fields which are to be updated.
   */
  fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
    mFireStore.collection(Constants.USERS)
      // Document ID against which the data to be updated. Here the document id is the current logged in user id.
      .document(getCurrentUserID())
      .update(userHashMap) // A HashMap of fields which are to be updated.
      .addOnSuccessListener {

        when (activity) {
          is UserProfileActivity -> {
            // Call a function of base activity for transferring the result to it.
            activity.userProfileUpdateSuccess()
          }
        }
      }
      .addOnFailureListener { e ->

        when (activity) {
          is UserProfileActivity -> {
            // Hide the progress dialog if there is any error. And print the error in log.
            activity.hideProgressDialog()
          }
        }

        Log.e(
          activity.javaClass.simpleName,
          "Error while updating the user details.",
          e
        )
      }
  }

  // A function to upload the image to the cloud storage.
  fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {
    //getting the storage reference
    val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
      imageType + System.currentTimeMillis() + "."
        + Constants.getFileExtension(
        activity,
        imageFileURI
      )
    )

    //adding the file to reference
    sRef.putFile(imageFileURI!!)
      .addOnSuccessListener { taskSnapshot ->
        Log.e(
          "Firebase Image URL",
          taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
        )

        // Get the downloadable url from the task snapshot
        taskSnapshot.metadata!!.reference!!.downloadUrl
          .addOnSuccessListener { uri ->
            Log.e("Downloadable Image URL", uri.toString())

            // Here call a function of base activity for transferring the result to it.
            when (activity) {
              is UserProfileActivity -> {
                activity.imageUploadSuccess(uri.toString())
              }

              is AddProductActivity -> {
                activity.imageUploadSuccess(uri.toString())
              }
            }
          }
      }
      .addOnFailureListener { exception ->
        // Hide the progress dialog if there is any error. And print the error in log.
        when (activity) {
          is UserProfileActivity -> {
            activity.hideProgressDialog()
          }

          is AddProductActivity -> {
            activity.hideProgressDialog()
          }
        }
        Log.e(activity.javaClass.simpleName, exception.message, exception)
      }
  }

  /**
   * A function to make an entry of the user's product in the cloud firestore database.
   */
  fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {

    mFireStore.collection(Constants.PRODUCTS)
      .document()
      // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
      .set(productInfo, SetOptions.merge())
      .addOnSuccessListener {

        // Here call a function of base activity for transferring the result to it.
        activity.productUploadSuccess()
      }
      .addOnFailureListener { e ->

        activity.hideProgressDialog()

        Log.e(
          activity.javaClass.simpleName,
          "Error while uploading the product details.",
          e
        )
      }
  }

  /**
   * A function to get the products list from cloud firestore.
   *
   * @param fragment The fragment is passed as parameter as the function is called from fragment and need to the success result.
   */
  fun getProductsList(fragment: Fragment) {
    mFireStore.collection(Constants.PRODUCTS)
      .whereEqualTo(Constants.USER_ID, getCurrentUserID())
      .get() // Will get the documents snapshots.
      .addOnSuccessListener { document ->

        // Here we get the list of boards in the form of documents.
        Log.e("Products List", document.documents.toString())

        // Here we have created a new instance for Products ArrayList.
        val productsList: ArrayList<Product> = ArrayList()

        // A for loop as per the list of documents to convert them into Products ArrayList.
        for (i in document.documents) {

          val product = i.toObject(Product::class.java)
          product!!.product_id = i.id

          if (product.stock_quantity != "0") {
            productsList.add(product)
          }
        }

        when (fragment) {
          is ProductsFragment -> {
            fragment.successProductsListFromFireStore(productsList)
          }
        }
      }
      .addOnFailureListener { e ->
        // Hide the progress dialog if there is any error based on the base class instance.
        when (fragment) {
          is ProductsFragment -> {
            fragment.hideProgressDialog()
          }
        }
        Log.e("Get Product List", "Error while getting product list.", e)
      }
  }

  /**
   * A function to get the dashboard items list. The list will be an overall items list, not based on the user's id.
   */
  fun getDashboardItemsList(fragment: DashboardFragment) {

    mFireStore.collection(Constants.PRODUCTS)
      .get() // Will get the documents snapshots.On the dashboard we will see all the products
      .addOnSuccessListener { document ->

        // Here we get the list of boards in the form of documents.
        Log.e(fragment.javaClass.simpleName, document.documents.toString())

        // Here we have created a new instance for Products ArrayList.
        val productsList: ArrayList<Product> = ArrayList()

        // A for loop as per the list of documents to convert them into Products ArrayList.
        for (i in document.documents) {

          val product = i.toObject(Product::class.java)!!
          product.product_id = i.id

          if (product.user_id != getCurrentUserID() && product.stock_quantity.toInt() != 0) {
            productsList.add(product)
          }
        }
        // Pass the success result to the base fragment.
        fragment.successDashboardItemsList(productsList)
      }
      .addOnFailureListener { e ->
        // Hide the progress dialog if there is any error which getting the dashboard items list.
        fragment.hideProgressDialog()
        Log.e(fragment.javaClass.simpleName, "Error while getting dashboard items list.", e)
      }
  }

  /**
   * A function to delete the product from the cloud firestore.
   */
  fun deleteProduct(fragment: ProductsFragment, productId: String) {

    mFireStore.collection(Constants.PRODUCTS)
      .document(productId)
      .delete()
      .addOnSuccessListener {
        // Notify the success result to the base class.
        fragment.productDeleteSuccess()
      }
      .addOnFailureListener { e ->
        // Hide the progress dialog if there is an error.
        fragment.hideProgressDialog()
        Log.e(
          fragment.requireActivity().javaClass.simpleName,
          "Error while deleting the product.",
          e
        )
      }
  }

  /**
   * A function to get the product details based on the product id.
   */
  fun getProductDetails(activity: ProductDetailsActivity, productId: String) {
    mFireStore.collection(Constants.PRODUCTS)
      .document(productId)
      .get() // Will get the document snapshots.
      .addOnSuccessListener { document ->

        // Here we get the product details in the form of document.
        Log.e(activity.javaClass.simpleName, document.toString())

        // Convert the snapshot to the object of Product data model class.
        val product = document.toObject(Product::class.java)!!

        activity.productDetailsSuccess(product)
      }
      .addOnFailureListener { e ->
        // Hide the progress dialog if there is an error.
        activity.hideProgressDialog()
        Log.e(activity.javaClass.simpleName, "Error while getting the product details.", e)
      }
  }

  /**
   * A function to add the item to the cart in the cloud firestore.
   *
   * @param activity
   * @param addToCart
   */
  fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem) {

    mFireStore.collection(Constants.CART_ITEMS)
      .document()
      // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
      .set(addToCart, SetOptions.merge())
      .addOnSuccessListener {

        // Here call a function of base activity for transferring the result to it.
        activity.addToCartSuccess()
      }
      .addOnFailureListener { e ->

        activity.hideProgressDialog()

        Log.e(
          activity.javaClass.simpleName,
          "Error while creating the document for cart item.", e
        )
      }
  }

  /**
   * A function to check whether the item already exist in the cart or not.
   */
  fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String) {

    mFireStore.collection(Constants.CART_ITEMS)
      .whereEqualTo(Constants.USER_ID, getCurrentUserID()) // bu kullanici
      .whereEqualTo(Constants.PRODUCT_ID, productId) // bu urunu sepete koymus mu
      .get()
      .addOnSuccessListener { document ->

        Log.e(activity.javaClass.simpleName, document.documents.toString())

        // If the document size is greater than 1 it means the product is already added to the cart.
        if (document.documents.size > 0) {
          activity.productExistsInCart()
        } else {
          activity.hideProgressDialog()
        }
      }
      .addOnFailureListener { e ->
        // Hide the progress dialog if there is an error.
        activity.hideProgressDialog()

        Log.e(
          activity.javaClass.simpleName,
          "Error while checking the existing cart list.",
          e
        )
      }
  }

  /**
   * A function to get the cart items list from the cloud firestore.
   *
   * @param activity
   */
  fun getCartList(activity: Activity) {
    mFireStore.collection(Constants.CART_ITEMS)
      .whereEqualTo(Constants.USER_ID, getCurrentUserID())
      .get()
      .addOnSuccessListener { document ->

        // Here we get the list of cart items in the form of documents.
        Log.e(activity.javaClass.simpleName, document.documents.toString())

        // Here we have created a new instance for Cart Items ArrayList.
        val list: ArrayList<CartItem> = ArrayList()

        // A for loop as per the list of documents to convert them into Cart Items ArrayList.
        for (i in document.documents) {

          val cartItem = i.toObject(CartItem::class.java)!!
          cartItem.id = i.id

          list.add(cartItem)
        }

        when (activity) {
          is CartListActivity -> {
            activity.successCartItemsList(list)
          }

          is CheckoutActivity -> {
            activity.successCartItemsList(list)
          }
        }
      }
      .addOnFailureListener { e ->
        when (activity) {
          is CartListActivity -> {
            activity.hideProgressDialog()
          }

          is CheckoutActivity -> {
            activity.hideProgressDialog()
          }
        }

        Log.e(activity.javaClass.simpleName, "Error while getting the cart list items.", e)
      }
  }

  /**
   * A function to get all the product list from the cloud firestore.
   *
   * @param activity The activity is passed as parameter to the function because it is called from activity and need to the success result.
   */
  fun getAllProductsList(activity: Activity) {
    mFireStore.collection(Constants.PRODUCTS)
      .get()
      .addOnSuccessListener { document ->

        // Here we get the list of boards in the form of documents.
        Log.e("Products List", document.documents.toString())

        // Here we have created a new instance for Products ArrayList.
        val productsList: ArrayList<Product> = ArrayList()

        // A for loop as per the list of documents to convert them into Products ArrayList.
        for (i in document.documents) {

          val product = i.toObject(Product::class.java)
          product!!.product_id = i.id

          productsList.add(product)
        }
        when (activity) {
          is CartListActivity -> {
            activity.successProductsListFromFireStore(productsList)
          }

          is CheckoutActivity -> {
            activity.successProductsListFromFireStore(productsList)
          }
        }

      }
      .addOnFailureListener { e ->
        when (activity) {
          is CartListActivity -> {
            activity.hideProgressDialog()
          }

          is CheckoutActivity -> {
            activity.hideProgressDialog()
          }
        }

        Log.e("Get Product List", "Error while getting all product list.", e)
      }
  }

  /**
   * A function to remove the cart item from the cloud firestore.
   *
   * @param context the context.
   * @param cart_id cart id of the item.
   */
  fun removeItemFromCart(context: Context, cart_id: String) {

    mFireStore.collection(Constants.CART_ITEMS)
      .document(cart_id)
      .delete()
      .addOnSuccessListener {
        when (context) {
          is CartListActivity -> {
            context.itemRemovedSuccess()
          }
        }
      }
      .addOnFailureListener { e ->
        when (context) {
          is CartListActivity -> {
            context.hideProgressDialog()
          }
        }
        Log.e(
          context.javaClass.simpleName,
          "Error while removing the item from the cart list.",
          e
        )
      }
  }

  /**
   * A function to update the cart item in the cloud firestore.
   *
   * @param context the context.
   * @param cart_id cart it.
   * @param itemHashMap to be updated values.
   */
  fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>) {

    mFireStore.collection(Constants.CART_ITEMS)
      .document(cart_id)
      .update(itemHashMap) // A HashMap of fields which are to be updated.
      .addOnSuccessListener {

        when (context) {
          is CartListActivity -> {
            context.itemUpdateSuccess()
          }
        }
      }
      .addOnFailureListener { e ->
        when (context) {
          is CartListActivity -> {
            context.hideProgressDialog()
          }
        }
        Log.e(
          context.javaClass.simpleName,
          "Error while updating the cart item.",
          e
        )
      }
  }

  /**
   * A function to add address to the cloud firestore.
   *
   * @param activity
   * @param addressInfo
   */
  fun addAddress(activity: AddEditAddressActivity, addressInfo: Address) {

    mFireStore.collection(Constants.ADDRESSES)
      .document()
      .set(addressInfo, SetOptions.merge())
      .addOnSuccessListener {
        activity.addUpdateAddressSuccess()
      }
      .addOnFailureListener { e ->
        activity.hideProgressDialog()
        Log.e(
          activity.javaClass.simpleName,
          "Error while adding the address.",
          e
        )
      }
  }

  /**
   * A function to get the list of address from the cloud firestore.
   *
   * @param activity
   */
  fun getAddressesList(activity: AddressListActivity) {
    mFireStore.collection(Constants.ADDRESSES)
      .whereEqualTo(Constants.USER_ID, getCurrentUserID())
      .get()
      .addOnSuccessListener { document ->
        Log.e(activity.javaClass.simpleName, document.documents.toString())
        val addressList: ArrayList<Address> = ArrayList()
        for (i in document.documents) {
          val address = i.toObject(Address::class.java)!!
          address.id = i.id
          addressList.add(address)
        }
        activity.successAddressListFromFirestore(addressList)
      }
      .addOnFailureListener { e ->
        activity.hideProgressDialog()
        Log.e(activity.javaClass.simpleName, "Error while getting the address list.", e)
      }
  }

  /**
   * A function to update the existing address to the cloud firestore.
   *
   * @param activity Base class
   * @param addressInfo Which fields are to be updated.
   * @param addressId existing address id
   */
  fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId: String) {

    mFireStore.collection(Constants.ADDRESSES)
      .document(addressId)
      .set(addressInfo, SetOptions.merge())
      .addOnSuccessListener {
        activity.addUpdateAddressSuccess()
      }
      .addOnFailureListener { e ->
        activity.hideProgressDialog()
        Log.e(
          activity.javaClass.simpleName,
          "Error while updating the Address.",
          e
        )
      }
  }

  /**
   * A function to delete the existing address from the cloud firestore.
   *
   * @param activity Base class
   * @param addressId existing address id
   */
  fun deleteAddress(activity: AddressListActivity, addressId: String) {

    mFireStore.collection(Constants.ADDRESSES)
      .document(addressId)
      .delete()
      .addOnSuccessListener {
        activity.deleteAddressSuccess()
      }
      .addOnFailureListener { e ->
        activity.hideProgressDialog()
        Log.e(
          activity.javaClass.simpleName,
          "Error while deleting the address.",
          e
        )
      }
  }

  /**
   * A function to place an order of the user in the cloud firestore.
   *
   * @param activity base class
   * @param order Order Info
   */
  fun placeOrder(activity: CheckoutActivity, order: Order) {

    mFireStore.collection(Constants.ORDERS)
      .document()
      .set(order, SetOptions.merge())
      .addOnSuccessListener {
        activity.orderPlacedSuccess()
      }
      .addOnFailureListener { e ->
        activity.hideProgressDialog()
        Log.e(
          activity.javaClass.simpleName,
          "Error while placing an order.",
          e
        )
      }
  }

  /**
   * A function to update all the required details in the cloud firestore after placing the order successfully.
   *
   * @param activity Base class.
   * @param cartList List of cart items.
   */
  fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<CartItem>, order: Order) {

    val writeBatch = mFireStore.batch()

    // Prepare the sold product details
    for (cart in cartList) {
      val soldProduct = SoldProduct(
        cart.product_owner_id,
        cart.title,
        cart.price,
        cart.cart_quantity,
        cart.image,
        order.title,
        order.order_datetime,
        order.sub_total_amount,
        order.shipping_charge,
        order.total_amount,
        order.address
      )

      val documentReference = mFireStore.collection(Constants.SOLD_PRODUCTS)
        .document()
      writeBatch.set(documentReference, soldProduct)
    }
    for (cart in cartList) {
      val productHashMap = HashMap<String, Any>()
      productHashMap[Constants.STOCK_QUANTITY] =
        (cart.stock_quantity.toInt() - cart.cart_quantity.toInt()).toString()

      val documentReference = mFireStore.collection(Constants.PRODUCTS)
        .document(cart.product_id)
      writeBatch.update(documentReference, productHashMap)
    }

    for (cart in cartList) {
      val documentReference = mFireStore.collection(Constants.CART_ITEMS)
        .document(cart.id)
      writeBatch.delete(documentReference)
    }

    writeBatch.commit().addOnSuccessListener {

      activity.allDetailsUpdatedSuccessfully()

    }.addOnFailureListener { e ->
      activity.hideProgressDialog()

      Log.e(
        activity.javaClass.simpleName,
        "Error while updating all the details after order placed.",
        e
      )
    }
  }

  /**
   * A function to get the list of orders from cloud firestore.
   */
  fun getMyOrdersList(fragment: OrdersFragment) {
    mFireStore.collection(Constants.ORDERS)
      .whereEqualTo(Constants.USER_ID, getCurrentUserID())
      .get()
      .addOnSuccessListener { document ->
        Log.e(fragment.javaClass.simpleName, document.documents.toString())
        val list: ArrayList<Order> = ArrayList()

        for (i in document.documents) {
          val orderItem = i.toObject(Order::class.java)!!
          orderItem.id = i.id
          list.add(orderItem)
        }

        fragment.populateOrdersListInUI(list)
      }
      .addOnFailureListener { e ->
        fragment.hideProgressDialog()
        Log.e(fragment.javaClass.simpleName, "Error while getting the orders list.", e)
      }
  }

  /**
   * A function to get the list of sold products from the cloud firestore.
   *
   *  @param fragment Base class
   */
  fun getSoldProductsList(fragment: SoldProductsFragment) {
    mFireStore.collection(Constants.SOLD_PRODUCTS)
      .whereEqualTo(Constants.USER_ID, getCurrentUserID())
      .get()
      .addOnSuccessListener { document ->
        Log.e(fragment.javaClass.simpleName, document.documents.toString())
        val list: ArrayList<SoldProduct> = ArrayList()

        for (i in document.documents) {
          val soldProduct = i.toObject(SoldProduct::class.java)!!
          soldProduct.id = i.id
          list.add(soldProduct)
        }

        fragment.successSoldProductsList(list)
      }
      .addOnFailureListener { e ->
        fragment.hideProgressDialog()
        Log.e(
          fragment.javaClass.simpleName,
          "Error while getting the list of sold products.",
          e
        )
      }
  }

  /**
   * A function to update the order status in the cloud firestore.
   *
   * @param orderId Order ID of the order to be updated.
   * @param newStatus New status to be set for the order.
   * @param activity Base class
   */
  fun updateOrderStatus(
    activity: SoldProductDetailsActivity,
    orderId: String,
    newStatus: String
  ) {
    mFireStore.collection(Constants.ORDERS)
      .whereEqualTo(Constants.ORDER_ID, orderId)
      .get()
      .addOnSuccessListener { documents ->
        if (documents.isEmpty) {
          Log.e(activity.javaClass.simpleName, "No order found with order id: $orderId")
        } else {
          for (document in documents) {
            val orderId = document.id
            mFireStore.collection(Constants.ORDERS)
              .document(orderId)
              .update(Constants.ORDER_STATUS, newStatus)
              .addOnSuccessListener {
                activity.orderDelivered()
              }
          }
        }
      }
      .addOnFailureListener { e ->
        Log.e(
          activity.javaClass.simpleName,
          "Error while updating the order status.",
          e
        )
      }
  }

  fun updateSoldProductStatus(
    activity: SoldProductDetailsActivity,
    soldProductId: String,
    newStatus: String
  ) {
    mFireStore.collection(Constants.SOLD_PRODUCTS)
      .whereEqualTo(Constants.ORDER_ID, soldProductId)
      .get()
      .addOnSuccessListener { documents ->
        if (documents.isEmpty) {
          Log.e(activity.javaClass.simpleName, "No order found with order id: $soldProductId")
        } else {
          for (document in documents) {
            val orderId = document.id
            mFireStore.collection(Constants.SOLD_PRODUCTS)
              .document(orderId)
              .update(Constants.ORDER_STATUS, newStatus)
              .addOnSuccessListener {
                activity.delivered()
              }
          }
        }
      }
      .addOnFailureListener { e ->
        Log.e(
          activity.javaClass.simpleName,
          "Error while updating the order status.",
          e
        )
      }
  }

  /**
   * A function to delete the order from the cloud firestore.
   */
  fun deleteOrder(fragment: OrdersFragment, orderId: String) {

    mFireStore.collection(Constants.ORDERS)
      .whereEqualTo(Constants.ORDER_ID, orderId)
      .get()
      .addOnSuccessListener { documents ->
        for (document in documents) {
          // Delete the order with the matching orderId
          mFireStore.collection(Constants.ORDERS)
            .document(document.id)
            .delete()
            .addOnSuccessListener {
              fragment.orderDeleteSuccess()
            }
            .addOnFailureListener { e ->
              fragment.hideProgressDialog()
              Log.e(
                fragment.requireActivity().javaClass.simpleName,
                "Error while deleting the order.",
                e
              )
            }
        }
      }
      .addOnFailureListener { e ->
        fragment.hideProgressDialog()
        Log.e(
          fragment.requireActivity().javaClass.simpleName,
          "Error while deleting the product.",
          e
        )
      }
  }

  /**
   * A function to delete the sold produt from the cloud firestore.
   */
  fun deleteSoldProduct(fragment: SoldProductsFragment, soldProductId: String) {

    mFireStore.collection(Constants.SOLD_PRODUCTS)
      .whereEqualTo(Constants.SOLD_PRODUCT_ID, soldProductId)
      .get()
      .addOnSuccessListener { documents ->
        for (document in documents) {
          // Delete the order with the matching orderId
          mFireStore.collection(Constants.SOLD_PRODUCTS)
            .document(document.id)
            .delete()
            .addOnSuccessListener {
              fragment.soldProdutDeleteSuccess()
            }
            .addOnFailureListener { e ->
              fragment.hideProgressDialog()
              Log.e(
                fragment.requireActivity().javaClass.simpleName,
                "Error while deleting the order.",
                e
              )
            }
        }
      }
      .addOnFailureListener { e ->
        fragment.hideProgressDialog()
        Log.e(
          fragment.requireActivity().javaClass.simpleName,
          "Error while deleting the product.",
          e
        )
      }
  }
}