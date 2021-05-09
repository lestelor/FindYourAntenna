package lestelabs.antenna.ui.main.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A book Model representing a piece of content.
 */

@Entity(tableName = "Site")
data class Site(
    @PrimaryKey var codigo: String = "",
    var operador: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    var direccion: String? = null,
    var frecuencias: String? = null
)