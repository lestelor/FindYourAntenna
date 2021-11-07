package lestelabs.antenna.ui.main.data

import androidx.room.Database
import androidx.room.RoomDatabase


/**
 * Room Application Database
 */


@Database(entities = arrayOf(Site::class, Server::class), version = 1)
abstract class ApplicationDatabaseFinal : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun serverDao(): ServerDao



}
