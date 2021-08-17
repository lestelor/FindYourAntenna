package lestelabs.antenna.ui.main.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Book Dao (Data Access Object) for accessing Book Table functions.
 */

@Dao
interface ServerDao {
    @Query("SELECT * FROM Server")
    fun getAllServers(): MutableList<Server>

    @Query("SELECT * FROM Server WHERE name LIKE :nameServer LIMIT 1")
    fun getServerByName(nameServer: String): Server?

    @Query("SELECT * FROM Server WHERE sponsorName LIKE :sponsorServer")
    fun getServerBySponsor(sponsorServer: String): Array<Server>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveServer(Server: Server): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveServers(vararg Server: Server)
}