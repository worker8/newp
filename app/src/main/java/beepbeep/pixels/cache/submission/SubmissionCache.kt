package beepbeep.pixels.cache.submission

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import net.dean.jraw.models.Submission
import java.util.*

@Entity
data class SubmissionCache(@PrimaryKey
                           var id: String,
                           var author: String,
                           var title: String,
                           var selftext: String,
                           var subredditName: String,
                           var url: String,
                           var date: Date) {

    constructor(submission: Submission) : this(
            submission.id,
            submission.author,
            submission.title,
            submission.selftext,
            submission.subredditName,
            submission.url,
            submission.created)
}