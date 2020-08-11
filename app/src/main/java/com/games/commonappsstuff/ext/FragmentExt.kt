package com.games.commonappsstuff.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

fun AppCompatActivity.navigateTo(fragment: Fragment, container: Int) {
    val lastFragmentTransaction = supportFragmentManager.beginTransaction()
    val lastFragment = supportFragmentManager.findFragmentById(container)
    lastFragment?.let { it1 -> lastFragmentTransaction.hide(it1) }
    lastFragmentTransaction.addToBackStack(fragment.javaClass.toString())
    lastFragmentTransaction.commit()
    val fragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.add(container, fragment)
    fragmentTransaction.commit()
}

fun AppCompatActivity.navigateBack(fragment: Fragment) {
    val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.remove(fragment)
    supportFragmentManager.popBackStack()
    fragmentTransaction.commitNow()
}

fun AppCompatActivity.addFragment(fragment: Fragment, container: Int) {
    supportFragmentManager.beginTransaction()
        .add(container, fragment, fragment::class.java.name)
        .commit()
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, container: Int, startAnim: Int = 0, endAnim: Int = 0) {
    supportFragmentManager.beginTransaction()
        .setCustomAnimations(startAnim, endAnim, 0, 0)
        .replace(container, fragment)
        .commit()
}