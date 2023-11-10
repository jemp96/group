package com.example.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.group.data.CartProduct
import com.example.group.firebase.FirebaseCommon
import com.example.group.helper.getProductPrice
import com.example.group.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
): ViewModel() {

    private val _cartProduts = MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    val cartProducts = _cartProduts.asStateFlow()

    private var cartProductDocuments = emptyList<DocumentSnapshot>()



    val productsPrice = cartProducts.map {
        when (it){
            is Resource.Success->{
                calculatePrice(it.data!!)
            }
            else -> null
        }
    }

    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()

    fun deleteCartProduct(cartProduct: CartProduct) {
        val index  = cartProducts.value.data?.indexOf(cartProduct)
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("cart").document(documentId).delete()
        }
    }

    private fun calculatePrice(data: List<CartProduct>): Float {
        return data.sumByDouble {cartProduct ->
            (cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price)*cartProduct.quantity).toDouble()
        }.toFloat()
    }



    init {
        getCartProducts()
    }

    private fun getCartProducts(){
        viewModelScope.launch { _cartProduts.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .addSnapshotListener { value, error ->
                if (error != null || value == null){
                    viewModelScope.launch { _cartProduts.emit(Resource.Error(error?.message.toString())) }
                }else{
                    cartProductDocuments = value.documents
                    val cartProducts = value.toObjects(CartProduct::class.java)
                    viewModelScope.launch { _cartProduts.emit(Resource.Success(cartProducts)) }
                }
            }
    }

    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChanging: FirebaseCommon.QuantityChanging
    ){



        val index = cartProducts.value.data?.indexOf(cartProduct)

        if(index != null && index != -1){
            val documentId = cartProductDocuments[index].id
            when(quantityChanging){
                FirebaseCommon.QuantityChanging.INCREASE->{
                    viewModelScope.launch { _cartProduts.emit(Resource.Loading()) }
                    increaseQuantity(documentId)
                }
                FirebaseCommon.QuantityChanging.DECRESASE->{
                    if (cartProduct.quantity == 1){
                        viewModelScope.launch { _deleteDialog.emit(cartProduct) }
                        return
                    }
                    viewModelScope.launch { _cartProduts.emit(Resource.Loading()) }
                    decreaseQuantity(documentId)
                }
            }
        }

    }

    private fun decreaseQuantity(documentId: String) {
       firebaseCommon.decreaseQuantity(documentId){result, exception ->
           if (exception != null)
               viewModelScope.launch { _cartProduts.emit(Resource.Error(exception.message.toString())) }
       }
    }

    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId){result, exception ->
            if (exception != null)
                viewModelScope.launch { _cartProduts.emit(Resource.Error(exception.message.toString())) }
        }
    }
}