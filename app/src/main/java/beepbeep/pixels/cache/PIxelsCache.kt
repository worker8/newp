package beepbeep.pixels.cache

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.cache.submission.SubmissionDao

@Database(entities = arrayOf(SubmissionCache::class), version = 1)
@TypeConverters(DateConverter::class)
abstract class PixelsCache : RoomDatabase() {
    abstract fun submissionDao(): SubmissionDao
}