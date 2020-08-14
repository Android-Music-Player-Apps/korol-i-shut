package com.example.android.uamp.ads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.uamp.MediaItemData
import com.example.android.uamp.MediaViewHolder
import com.example.android.uamp.R
import com.example.android.uamp.databinding.FragmentMediaitemBinding
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.formats.UnifiedNativeAd

/**
 * Created by olehka on 14.08.2020.
 */
class AdMediaItemAdapter(
    private val itemClickedListener: (MediaItemData) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data: MutableList<MediaItemData> = mutableListOf()
    private val ads: MutableList<UnifiedNativeAd> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            NATIVE_AD_VIEW_TYPE -> {
                val view = inflater.inflate(R.layout.view_native_ad, parent, false) as TemplateView
                NativeAdViewHolder(view)
            }
            else -> {
                val binding = FragmentMediaitemBinding.inflate(inflater, parent, false)
                MediaViewHolder(binding, itemClickedListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size + ads.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (isAdvertisement(position)) {
            holder as NativeAdViewHolder
            holder.bind(ads[0])
        } else {
            val itemPosition = if (ads.isEmpty()) position else position - 1
            val mediaItem = data[itemPosition]
            var fullRefresh = payloads.isEmpty()

            holder as MediaViewHolder
            if (payloads.isNotEmpty()) {
                payloads.forEach { payload ->
                    when (payload) {
                        MediaItemData.PLAYBACK_RES_CHANGED -> {
                            holder.playbackState.setImageResource(mediaItem.playbackRes)
                        }
                        // If the payload wasn't understood, refresh the full item (to be safe).
                        else -> fullRefresh = true
                    }
                }
            }

            // Normally we only fully refresh the list item if it's being initially bound, but
            // we might also do it if there was a payload that wasn't understood, just to ensure
            // there isn't a stale item.
            if (fullRefresh) {
                holder.item = mediaItem
                holder.titleView.text = mediaItem.title
                holder.subtitleView.text = mediaItem.subtitle
                holder.playbackState.setImageResource(mediaItem.playbackRes)

                Glide.with(holder.albumArt)
                    .load(mediaItem.albumArtUri)
                    .into(holder.albumArt)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdvertisement(position)) NATIVE_AD_VIEW_TYPE
        else MEDIA_ITEM_VIEW_TYPE
    }

    fun setData(items: List<MediaItemData>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    fun setNativeAd(unifiedNativeAd: UnifiedNativeAd) {
        ads.clear()
        ads.add(unifiedNativeAd)
        notifyDataSetChanged()
    }

    fun destroyNativeAd() {
        ads.forEach { nativeAd ->
            nativeAd.destroy()
        }
    }

    private fun isAdvertisement(position: Int): Boolean {
        return position == 0 && ads.isNotEmpty()
    }

    companion object {
        const val MEDIA_ITEM_VIEW_TYPE = 0
        const val NATIVE_AD_VIEW_TYPE = 1
    }

    inner class NativeAdViewHolder(
        val templateView: TemplateView
    ) : RecyclerView.ViewHolder(templateView) {

        fun bind(unifiedNativeAd: UnifiedNativeAd) {
            templateView.setNativeAd(unifiedNativeAd)
        }
    }
}