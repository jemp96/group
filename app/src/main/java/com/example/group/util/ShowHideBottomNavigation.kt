package com.example.group.util

import android.view.View
import androidx.fragment.app.Fragment
import com.example.group.R
import com.example.group.activities.ShoppingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigationView(){
    val bottonNavigationView = (activity as ShoppingActivity).findViewById<BottomNavigationView>(
        com.example.group.R.id.bottomNavigation)
    bottonNavigationView.visibility = android.view.View.GONE

}

fun Fragment.showBottomNavigationView(){
    val bottonNavigationView = (activity as ShoppingActivity).findViewById<BottomNavigationView>(
        com.example.group.R.id.bottomNavigation)
    bottonNavigationView.visibility = android.view.View.VISIBLE

}