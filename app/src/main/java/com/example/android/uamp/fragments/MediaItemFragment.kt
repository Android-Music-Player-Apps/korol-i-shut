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

package com.example.android.uamp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.android.uamp.MediaItemAdapter
import com.example.android.uamp.MediaItemData
import com.example.android.uamp.R
import com.example.android.uamp.databinding.FragmentMediaitemListBinding
import com.example.android.uamp.utils.InjectorUtils
import com.example.android.uamp.viewmodels.MainActivityViewModel
import com.example.android.uamp.viewmodels.MediaItemFragmentViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

/**
 * A fragment representing a list of MediaItems.
 */
class MediaItemFragment : Fragment() {
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel> {
        InjectorUtils.provideMainActivityViewModel(requireContext())
    }
    private val mediaItemFragmentViewModel by viewModels<MediaItemFragmentViewModel> {
        InjectorUtils.provideMediaItemFragmentViewModel(requireContext(), mediaId)
    }

    private lateinit var mediaId: String
    private lateinit var binding: FragmentMediaitemListBinding
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var clickedItem: MediaItemData

    private val listAdapter = MediaItemAdapter { clickedItem ->
        mainActivityViewModel.mediaItemClicked(clickedItem)
    }

    companion object {
        fun newInstance(mediaId: String): MediaItemFragment {

            return MediaItemFragment().apply {
                arguments = Bundle().apply {
                    putString(MEDIA_ID_ARG, mediaId)
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMediaitemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Always true, but lets lint know that as well.
        mediaId = arguments?.getString(MEDIA_ID_ARG) ?: return

        mediaItemFragmentViewModel.mediaItems.observe(viewLifecycleOwner,
            Observer { list ->
                binding.loadingSpinner.visibility =
                    if (list?.isNotEmpty() == true) View.GONE else View.VISIBLE
                listAdapter.submitList(list)
            })
        mediaItemFragmentViewModel.networkError.observe(viewLifecycleOwner,
            Observer { error ->
                if (error) {
                    binding.loadingSpinner.visibility = View.GONE
                    binding.networkError.visibility = View.VISIBLE
                } else {
                    binding.networkError.visibility = View.GONE
                }
            })

        mainActivityViewModel.searchQuery.observe(viewLifecycleOwner,
            Observer { query ->
                mediaItemFragmentViewModel.search(query)
            }
        )

        // Set the adapter
        binding.list.adapter = listAdapter

        // Create the InterstitialAd and set it up.
        if ((0..9).random() > 8) createInterstitialAd()

        // Initialize NativeAd
        createNativeAd()

        // Clear focus
        mainActivityViewModel.clearSearchFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createInterstitialAd() {
        interstitialAd = InterstitialAd(context).apply {
            adUnitId = getString(R.string.inter_media_item_ad_unit_id)
            adListener = (object : AdListener() {
                override fun onAdLoaded() { }

                override fun onAdFailedToLoad(errorCode: Int) { }

                override fun onAdClosed() {
                    startNextScreen()
                }
            })
            loadAd(AdRequest.Builder().build())
        }
    }

    private fun createNativeAd() {
        val builder = AdLoader.Builder(context, getString(R.string.native_media_item_ad_unit_id))
        builder.forUnifiedNativeAd { unifiedNativeAd ->
            // OnUnifiedNativeAdLoadedListener implementation.
            // If this callback occurs after the activity is destroyed, you must call
            // destroy and return or you may get a memory leak.
            if (activity?.isDestroyed == true) {
                unifiedNativeAd.destroy()
                return@forUnifiedNativeAd
            }
        }
        val adLoader = builder.build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun startNextScreen() {
        mainActivityViewModel.mediaItemClicked(clickedItem)
    }
}

private const val MEDIA_ID_ARG = "com.example.android.uamp.fragments.MediaItemFragment.MEDIA_ID"
private const val INTER_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
private const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
