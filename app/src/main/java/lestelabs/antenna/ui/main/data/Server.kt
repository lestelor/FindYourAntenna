package lestelabs.antenna.ui.main.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A book Model representing a piece of content.
 */

@Entity(tableName = "Server")
data class Server(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var server: String = "",
    var dlURL: String = "",
    var ulURL: String = "",
    var pingURL: String = "",
    var getIpURL: String = "",
    var sponsorName: String = "",
    var sponsorURL: String = ""
)

