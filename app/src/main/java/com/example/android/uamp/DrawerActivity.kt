package com.example.android.uamp

import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.ShareActionProvider
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


abstract class DrawerActivity : AppCompatActivity() {

    private var shareActionProvider: ShareActionProvider? = null

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        menu.findItem(R.id.share).also {
            shareActionProvider = MenuItemCompat.getActionProvider(it) as ShareActionProvider?
        }
        val shareIntent = ShareCompat.IntentBuilder.from(this)
            .setType("text/plain").setText(BuildConfig.URL_TO_SHARE).intent
        shareActionProvider?.setShareIntent(shareIntent)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
                    openWebView(getString(R.string.official_website_url))
                }
                R.id.nav_wiki -> {
                    openWebView(getString(R.string.link_to_wiki_url))
                }
                R.id.nav_about -> {
                    openWebView(getString(R.string.about_project_url))
                }
                R.id.nav_privacy_policy -> {
                    openWebBrowserActivity(getString(R.string.privacy_policy_url))
                }
            }

            true
        }
    }

    fun setNavigationItemSelected(index: Int, flag: Boolean = true) =
        navigationView.menu.getItem(index).setChecked(flag)

    fun setToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_search)
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

    private fun openWebBrowserActivity(uri: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    }
}

const val KEY_URI = "key_uri"