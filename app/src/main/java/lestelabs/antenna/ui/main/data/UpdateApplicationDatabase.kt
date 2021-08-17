package lestelabs.antenna.ui.main.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object UpdateApplicationDatabase {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE `Server` (`id` INTEGER, `name` TEXT, `server` TEXT, `dlURL` TEXT, `ulURL` TEXT," +
                    "`pingURL` TEXT, `getIpURL` TEXT, `sponsorName` TEXT,`sponsorURL` TEXT," +
                    "PRIMARY KEY(`id`))")
        }
    }
    fun getDatabase(context: Context): ApplicationDatabase {
        return Room
            .databaseBuilder(context.applicationContext, ApplicationDatabase::class.java, "prova.db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

}