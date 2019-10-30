/*
 * Copyright 2019 Google Inc. All rights reserved.
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

package com.example.android.uamp.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.android.uamp.R
import com.example.android.uamp.utils.InjectorUtils
import com.example.android.uamp.viewmodels.MainActivityViewModel
import com.example.android.uamp.viewmodels.NowPlayingFragmentViewModel
import com.example.android.uamp.viewmodels.NowPlayingFragmentViewModel.NowPlayingMetadata
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

const val MOTOROLA_AD_TEST_ID = "9D5E2A9D2650549F32FBDB849158C645"
const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
const val PROD_AD_UNIT_ID = "ca-app-pub-8081141113344620/3425002907"


/**
 * A fragment representing the current media item being played.
 */
class NowPlayingFragment : Fragment() {
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var nowPlayingViewModel: NowPlayingFragmentViewModel

    companion object {
        fun newInstance() = NowPlayingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_nowplaying, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Always true, but lets lint know that as well.
        val context = activity ?: return

        // Initialize playback duration and position to zero
        view.findViewById<TextView>(R.id.duration).text =
                NowPlayingMetadata.timestampToMSS(context, 0L)
        val positionTextView = view.findViewById<TextView>(R.id.position)
                .apply { text = NowPlayingMetadata.timestampToMSS(context, 0L) }

        // Inject our activity and view models into this fragment
        mainActivityViewModel = ViewModelProviders
                .of(context, InjectorUtils.provideMainActivityViewModel(context))
                .get(MainActivityViewModel::class.java)
        nowPlayingViewModel = ViewModelProviders
                .of(context, InjectorUtils.provideNowPlayingFragmentViewModel(context))
                .get(NowPlayingFragmentViewModel::class.java)

        // Attach observers to the LiveData coming from this ViewModel
        nowPlayingViewModel.mediaMetadata.observe(this,
                Observer { mediaItem -> updateUI(view, mediaItem) })
        nowPlayingViewModel.mediaButtonRes.observe(this,
                Observer { res -> view.findViewById<ImageView>(R.id.media_button).setImageResource(res) })
        nowPlayingViewModel.mediaPosition.observe(this,
                Observer { pos ->
                    positionTextView.text =
                            NowPlayingMetadata.timestampToMSS(context, pos)
                })

        // Setup UI handlers for buttons
        view.findViewById<ImageButton>(R.id.media_button).setOnClickListener {
            nowPlayingViewModel.mediaMetadata.value?.let { mainActivityViewModel.playMediaId(it.id) }
        }

        initBannerAdView(view)
    }

    /**
     * Internal function used to update all UI elements except for the current item playback
     */
    private fun updateUI(view: View, metadata: NowPlayingFragmentViewModel.NowPlayingMetadata) {
        val albumArtView = view.findViewById<ImageView>(R.id.albumArt)
        if (metadata.albumArtUri == Uri.EMPTY) {
            albumArtView.setImageResource(R.drawable.ic_album_black_24dp)
        } else {
            Glide.with(view)
                    .load(metadata.albumArtUri)
                    .into(albumArtView)
        }
        view.findViewById<TextView>(R.id.title).text = metadata.title
        view.findViewById<TextView>(R.id.subtitle).text = metadata.subtitle
        view.findViewById<TextView>(R.id.duration).text = metadata.duration
    }

    private fun initBannerAdView(view: View) {
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(MOTOROLA_AD_TEST_ID)
                .build()

        val adListenerImpl = object : AdListener() {

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("Ads", "onAdLoaded")
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                Log.d("Ads", "onAdFailedToLoad: $p0")
            }
        }

        val adView = AdView(context).apply {
            adSize = AdSize.SMART_BANNER
            adUnitId = TEST_AD_UNIT_ID
            adListener = adListenerImpl
        }

        view.findViewById<FrameLayout>(R.id.adContainer).apply {
            addView(adView)
        }

        adView.loadAd(adRequest)
    }
}