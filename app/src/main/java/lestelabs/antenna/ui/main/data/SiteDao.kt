package lestelabs.antenna.ui.main.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Book Dao (Data Access Object) for accessing Book Table functions.
 */

@Dao
interface SiteDao {
    @Query("SELECT * FROM Site")
    fun getAllSites(): MutableList<Site>

    @Query("SELECT * FROM Site WHERE codigo LIKE :codigoSite LIMIT 1")
    fun getSiteByCodigo(codigoSite: String): Site?

    @Query("SELECT * FROM Site WHERE operador LIKE :operadorSite LIMIT 1")
    fun getSiteByOperador(operadorSite: String): Site?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSite(site: Site): Long
}