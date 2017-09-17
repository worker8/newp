package beepbeep.pixels.shared

import android.app.Application
import android.arch.persistence.room.Room
import beepbeep.pixels.cache.PixelsCache


class PixelsApplication : Application() {
    companion object {
        var pixelsCache: PixelsCache? = null
    }

    override fun onCreate() {
        super.onCreate()
        pixelsCache = Room.databaseBuilder(this, PixelsCache::class.java, "PixelsCache").build()
    }
}
