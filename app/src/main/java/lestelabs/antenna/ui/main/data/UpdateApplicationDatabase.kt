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
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE Server ADD COLUMN lat TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE Server ADD COLUMN lon TEXT NOT NULL DEFAULT ''")
            //database.execSQL("DROP TABLE IF EXISTS Server")
            //database.execSQL("CREATE TABLE  `Server` (`id` INTEGER, `name` TEXT, `server` TEXT, `dlURL` TEXT, `ulURL` TEXT," +
            //        "`pingURL` TEXT, `getIpURL` TEXT, `sponsorName` TEXT,`sponsorURL` TEXT, `lat` TEXT, `lon` TEXT," +
            //        "PRIMARY KEY(`id`))")
        }
    }
    val MIGRATION_3_1 = object : Migration(3, 1) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE Server")
            database.execSQL("CREATE TABLE Server (id INTEGER, name TEXT, server TEXT, dlURL TEXT, ulURL TEXT, " +
                    "pingURL TEXT, getIpURL TEXT, sponsorName TEXT, sponsorURL TEXT, " +
                    "PRIMARY KEY(id))")
        }
    }

    fun getDatabase(context: Context): ApplicationDatabaseFinal {
        return Room
            .databaseBuilder(context.applicationContext, ApplicationDatabaseFinal::class.java, "app_database.db")
            //.addMigrations(MIGRATION_3_1)
            .fallbackToDestructiveMigration()
            .build()
    }

}