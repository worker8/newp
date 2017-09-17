package beepbeep.pixels.cache

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.cache.submission.SubmissionDao

@Database(entities = arrayOf(SubmissionCache::class), version = 1)
abstract class PixelsCache : RoomDatabase() {
    abstract fun submissionDao(): SubmissionDao
}