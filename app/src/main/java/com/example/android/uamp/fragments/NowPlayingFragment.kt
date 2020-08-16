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
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.android.uamp.R
import com.example.android.uamp.databinding.FragmentNowplayingBinding
import com.example.android.uamp.utils.InjectorUtils
import com.example.android.uamp.viewmodels.MainActivityViewModel
import com.example.android.uamp.viewmodels.NowPlayingFragmentViewModel
import com.example.android.uamp.viewmodels.NowPlayingFragmentViewModel.NowPlayingMetadata
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.fragment_nowplaying.adContainer


/**
 * A fragment representing the current media item being played.
 */
class NowPlayingFragment : Fragment() {
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var nowPlayingViewModel: NowPlayingFragmentViewModel
    private lateinit var adView: AdView

    lateinit var binding: FragmentNowplayingBinding

    private var initialLayoutComplete = false
    // Determine the screen width (less decorations) to use for the ad width.
    // If the ad hasn't been laid out, default to the full screen width.
    private val adSize: AdSize
        get() {
            val display = activity?.windowManager?.defaultDisplay
            val outMetrics = DisplayMetrics()
            display?.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = adContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNowplayingBinding.inflate(inflater, container, false)
        return binding.root
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
        nowPlayingViewModel.mediaMetadata.observe(viewLifecycleOwner,
            Observer { mediaItem -> updateUI(view, mediaItem) })
        nowPlayingViewModel.mediaButtonRes.observe(viewLifecycleOwner,
            Observer { res ->
                binding.mediaButton.setImageResource(res)
            })
        nowPlayingViewModel.mediaPosition.observe(viewLifecycleOwner,
                Observer { pos ->
                    binding.position.text =
                            NowPlayingMetadata.timestampToMSS(context, pos)
                })

        // Setup UI handlers for buttons
        binding.mediaButton.setOnClickListener {
            nowPlayingViewModel.mediaMetadata.value?.let { mainActivityViewModel.playMediaId(it.id) }
        }

        // Initialize playback duration and position to zero
        binding.duration.text = NowPlayingMetadata.timestampToMSS(context, 0L)
        binding.position.text = NowPlayingMetadata.timestampToMSS(context, 0L)

        // Initialize banner ad
        initAdaptiveBannerAd()

        // Clear focus
        mainActivityViewModel.clearSearchFocus()
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    /**
     * Internal function used to update all UI elements except for the current item playback
     */
    private fun updateUI(view: View, metadata: NowPlayingMetadata) = with(binding) {
        if (metadata.albumArtUri == Uri.EMPTY) {
            albumArt.setImageResource(R.drawable.ic_album_black_24dp)
        } else {
            Glide.with(view)
                .load(metadata.albumArtUri)
                .into(albumArt)
        }
        title.text = metadata.title
        subtitle.text = metadata.subtitle
        duration.text = metadata.duration
    }

    private fun initAdaptiveBannerAd() {
        adView = AdView(context)
        adContainer.addView(adView)

        // Since we're loading the banner based on the adContainerView size, we need to wait
        // until this view is laid out before we can get the width.
        adContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadBanner()
            }
        }
    }

    private fun loadBanner() {
        adView.adUnitId = getString(R.string.banner_now_playing_ad_unit_id)
        adView.adSize = adSize

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    companion object {
        fun newInstance() = NowPlayingFragment()
    }
}

// This is an ad unit ID for a test ad. Replace with your own banner ad unit ID.
private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"