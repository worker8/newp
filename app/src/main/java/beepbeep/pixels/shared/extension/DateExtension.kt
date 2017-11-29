package beepbeep.pixels.shared.extension

import android.text.format.DateUtils
import java.util.*


fun Date.toRelativetime(): String
        = DateUtils.getRelativeTimeSpanString(time, Date().time, DateUtils.HOUR_IN_MILLIS).toString()
