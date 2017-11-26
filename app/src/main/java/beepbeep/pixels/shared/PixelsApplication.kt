package beepbeep.pixels.shared

import android.app.Application
import android.arch.persistence.room.Room
import beepbeep.pixels.cache.PixelsCache
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class PixelsApplication : Application() {
    companion object {
        var pixelsCache: PixelsCache? = null
    }

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())
        pixelsCache = Room.databaseBuilder(this, PixelsCache::class.java, "PixelsCache").build()
    }
}
