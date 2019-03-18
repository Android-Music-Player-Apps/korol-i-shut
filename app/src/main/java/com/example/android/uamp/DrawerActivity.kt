package com.example.android.uamp

import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

const val KEY_URI = "key_uri"

abstract class DrawerActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun setNavigationView() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            drawerLayout.closeDrawers()

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            when (menuItem.itemId) {
                R.id.nav_albums -> {
                    openMainActivity()
                }
                R.id.nav_site -> {
                    openWebView("http://www.korol-i-shut.ru/news/")
                }
                R.id.nav_wiki -> {
                    openWebView("https://ru.m.wikipedia.org/wiki/%D0%9A%D0%BE%D1%80%D0%BE%D0%BB%D1%8C_%D0%B8_%D0%A8%D1%83%D1%82")
                }
                R.id.nav_about -> {
                    openWebView("https://olehkapustianov.com/")
                }
            }

            true
        }
    }

    fun setNavigationItemSelected(index: Int, flag: Boolean = true) = navigationView.menu.getItem(index).setChecked(flag)

    fun setToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
        }
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun openWebView(uri: String) {
        val intent = Intent(this, WebActivity::class.java).apply {
            putExtra(KEY_URI, uri)
        }
        startActivity(intent)
    }
}