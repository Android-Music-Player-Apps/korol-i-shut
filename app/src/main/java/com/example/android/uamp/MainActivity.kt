/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.uamp

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.app.ShareCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer
import com.example.android.uamp.fragments.MediaItemFragment
import com.example.android.uamp.media.MusicService
import com.example.android.uamp.utils.Event
import com.example.android.uamp.utils.InjectorUtils
import com.example.android.uamp.viewmodels.MainActivityViewModel
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext


class MainActivity : DrawerActivity() {

    private val viewModel by viewModels<MainActivityViewModel> {
        InjectorUtils.provideMainActivityViewModel(this)
    }
    private lateinit var searchView: SearchView
    private var castContext: CastContext? = null
    private var shareActionProvider: ShareActionProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        // Initialize the Cast context. This is required so that the media route button can be
        // created in the AppBar
        castContext = CastContext.getSharedInstance(this)

        setContentView(R.layout.activity_main)
        setToolbar()
        setSearchView()

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        setNavigationView()

        // Since UAMP is a music player, the volume controls should adjust the music volume while
        // in the app.
        volumeControlStream = AudioManager.STREAM_MUSIC

        /**
         * Observe [MainActivityViewModel.navigateToFragment] for [Event]s that request a
         * fragment swap.
         */
        viewModel.navigateToFragment.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { fragmentRequest ->
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(
                    R.id.fragmentContainer, fragmentRequest.fragment, fragmentRequest.tag
                )
                if (fragmentRequest.backStack) transaction.addToBackStack(null)
                transaction.commit()
            }
        })

        /**
         * Observe changes to the [MainActivityViewModel.rootMediaId]. When the app starts,
         * and the UI connects to [MusicService], this will be updated and the app will show
         * the initial list of media items.
         */
        viewModel.rootMediaId.observe(this,
            Observer<String> { rootMediaId ->
                rootMediaId?.let { navigateToMediaItem(it) }
            })

        /**
         * Observe [MainActivityViewModel.navigateToMediaItem] for [Event]s indicating
         * the user has requested to browse to a different [MediaItemData].
         */
        viewModel.navigateToMediaItem.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { mediaId ->
                navigateToMediaItem(mediaId)
            }
        })

        viewModel.clearFocusSearch.observe(this, Observer {
            searchView.clearFocus()
            dismissKeyboard(searchView)
        })
    }

    override fun onResume() {
        super.onResume()
        setNavigationItemSelected(0)
    }

    override fun onPause() {
        dismissKeyboard(searchView)
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                searchView.apply {
                    visibility = View.VISIBLE
                    isIconified = false
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        menu.findItem(R.id.share).also {
            shareActionProvider = MenuItemCompat.getActionProvider(it) as ShareActionProvider?
        }
        val shareIntent = ShareCompat.IntentBuilder.from(this)
            .setType("text/plain").setText(BuildConfig.URL_TO_SHARE).intent
        shareActionProvider?.setShareIntent(shareIntent)

        /**
         * Set up a MediaRouteButton to allow the user to control the current media playback route
         */
        CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, R.id.media_route)
        return true
    }

    private fun navigateToMediaItem(mediaId: String) {
        var fragment: MediaItemFragment? = getBrowseFragment(mediaId)
        if (fragment == null) {
            fragment = MediaItemFragment.newInstance(mediaId)
            // If this is not the top level media (root), we add it to the fragment
            // back stack, so that actionbar toggle and Back will work appropriately:
            viewModel.showFragment(fragment, !isRootId(mediaId), mediaId)
        }
    }

    private fun isRootId(mediaId: String) = mediaId == viewModel.rootMediaId.value

    private fun getBrowseFragment(mediaId: String): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaId) as? MediaItemFragment
    }

    private fun setSearchView() {
        searchView = findViewById(R.id.searchView)
        searchView.apply {
            setOnQueryTextListener(
                object : OnQueryTextListener {
                    override fun onQueryTextChange(newText: String): Boolean {
                        viewModel.onSearchQueryChanged(newText)
                        return true
                    }

                    override fun onQueryTextSubmit(query: String): Boolean {
                        clearFocus()
                        dismissKeyboard(this@apply)
                        return true
                    }
                }
            )
            // Set focus on the SearchView and open the keyboard
            setOnQueryTextFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    visibility = View.VISIBLE
                    showKeyboard(view.findFocus())
                }
            }
            setOnCloseListener {
                visibility = View.GONE
                return@setOnCloseListener false
            }
        }
    }

    private fun showKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    private fun dismissKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
