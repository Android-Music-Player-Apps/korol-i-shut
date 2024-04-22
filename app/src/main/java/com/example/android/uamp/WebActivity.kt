package com.example.android.uamp

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView

class WebActivity : DrawerActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        setToolbar()
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        setNavigationView()
        setWebView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.hasExtra(KEY_URI) == true) {
            webView.loadUrl(intent.getStringExtra(KEY_URI)!!)
        }
    }

    override fun onResume() {
        super.onResume()
        setNavigationItemSelected(0, false)
    }

    private fun setWebView() {
        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.loadUrl(intent.getStringExtra(KEY_URI)!!)
    }
}
