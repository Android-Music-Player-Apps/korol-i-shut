package com.example.android.uamp.utils

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

/**
 * Created by olehka on 06.09.2020.
 */

object DownloadUtil {

    private const val DIRECTORY_NAME = "downloads"

    private var cache: Cache? = null

    fun getCache(context: Context): Cache {
        if (cache == null) {
            val cacheDirectory = File(
                context.getExternalFilesDir(null),
                DIRECTORY_NAME
            )
            cache = SimpleCache(cacheDirectory, NoOpCacheEvictor(), ExoDatabaseProvider(context))
        }
        return cache!!
    }
}