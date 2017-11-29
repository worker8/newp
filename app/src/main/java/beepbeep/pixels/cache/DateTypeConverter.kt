package beepbeep.pixels.cache

import android.arch.persistence.room.TypeConverter
import java.util.*

class DateConverter {
    companion object {
        @JvmStatic
        @TypeConverter
        fun toDate(dateLong: Long): Date {
            return Date(dateLong)
        }

        @JvmStatic
        @TypeConverter
        fun fromDate(date: Date): Long {
            return date.getTime()
        }
    }
}