package lestelabs.antenna.ui.main.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Application Database
 */

@Database(entities = [Site::class], version = 1)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
}