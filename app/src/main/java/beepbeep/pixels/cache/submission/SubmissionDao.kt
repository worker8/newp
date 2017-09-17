package beepbeep.pixels.cache.submission

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface SubmissionDao {

    @Query("SELECT * FROM SubmissionCache")
    fun getSubmissionCacheFlowable(): Flowable<List<SubmissionCache>>

    @Query("SELECT * FROM SubmissionCache")
    fun getSubmissionCacheMaybe(): Maybe<List<SubmissionCache>>

//    @Query("DELETE FROM SubmissionCache WHERE subredditName = :subredditName")
//    fun deleteAllSubmissionCacheFrom(subredditName: String)

    @Delete
    fun delete(submissionCacheList: List<SubmissionCache>)

    @Insert(onConflict = REPLACE)
    fun insert(submissionCache: SubmissionCache)

}