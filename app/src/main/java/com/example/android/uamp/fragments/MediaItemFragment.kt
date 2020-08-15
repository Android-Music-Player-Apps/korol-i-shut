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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.android.uamp.MediaItemData
import com.example.android.uamp.ads.AdMediaItemAdapter
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
    private lateinit var mediaId: String
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel
    private lateinit var binding: FragmentMediaitemListBinding
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var clickedItem: MediaItemData

    private val listAdapter = AdMediaItemAdapter { clickedItem ->
        this.clickedItem = clickedItem
        if (::interstitialAd.isInitialized && interstitialAd.isLoaded) {
            interstitialAd.show()
        } else {
            startNextScreen()
        }
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
        val context = activity ?: return
        mediaId = arguments?.getString(MEDIA_ID_ARG) ?: return

        mainActivityViewModel = ViewModelProviders
            .of(context, InjectorUtils.provideMainActivityViewModel(context))
            .get(MainActivityViewModel::class.java)

        mediaItemFragmentViewModel = ViewModelProviders
            .of(this, InjectorUtils.provideMediaItemFragmentViewModel(context, mediaId))
            .get(MediaItemFragmentViewModel::class.java)

        mediaItemFragmentViewModel.mediaItems.observe(viewLifecycleOwner,
                Observer { list ->
                    binding.loadingSpinner.visibility =
                            if (list?.isNotEmpty() == true) View.GONE else View.VISIBLE
                    listAdapter.setData(list)
                })
        mediaItemFragmentViewModel.networkError.observe(viewLifecycleOwner,
                Observer { error ->
                    binding.networkError.visibility = if (error) View.VISIBLE else View.GONE
                    if (error) {
                        binding.loadingSpinner.visibility = View.GONE
                    }
                })

        // Set the adapter
        binding.list.adapter = listAdapter

        // Create the InterstitialAd and set it up.
        if ((0..9).random() > 8) createInterstitialAd()

        // Initialize NativeAd
        createNativeAd()

        // Test search
        mediaItemFragmentViewModel.search("Король")
    }

    override fun onDestroy() {
        listAdapter.destroyNativeAd()
        super.onDestroy()
    }

    private fun createInterstitialAd() {
        interstitialAd = InterstitialAd(context).apply {
            adUnitId = INTER_AD_UNIT_ID
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
        val builder = AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
        builder.forUnifiedNativeAd { unifiedNativeAd ->
            // OnUnifiedNativeAdLoadedListener implementation.
            // If this callback occurs after the activity is destroyed, you must call
            // destroy and return or you may get a memory leak.
            if (activity?.isDestroyed == true) {
                unifiedNativeAd.destroy()
                return@forUnifiedNativeAd
            }
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            listAdapter.destroyNativeAd()
            listAdapter.setNativeAd(unifiedNativeAd)
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
