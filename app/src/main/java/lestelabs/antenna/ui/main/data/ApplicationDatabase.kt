package lestelabs.antenna.ui.main.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import lestelabs.antenna.ui.main.MyApplication.Companion.ctx


/**
 * Room Application Database
 */


@Database(entities = arrayOf(Site::class, Server::class), version = 2)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun serverDao(): ServerDao



}
