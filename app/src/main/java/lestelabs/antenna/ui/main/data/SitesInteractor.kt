package lestelabs.antenna.ui.main.data


class SitesInteractor(private val siteDao: SiteDao) {

    // Get All Books from DAO
    fun getAllSites(): MutableList<Site> {
        return siteDao.getAllSites()
    }

    // Save Book
    fun saveSite(site: Site) {
        siteDao.saveSite(site)
    }

    // Save List of Books
    fun saveSites(vararg sites: Site) {
//        var i=0
//        sites.forEach {
//            Log.d("Sitesinteractor", "saved " + i)
//            i++
//            saveSite(it) }
        siteDao.saveSites(*sites)
    }

    // Get Book by id
    fun getSiteByCodigo(id: String): Site? {
        return siteDao.getSiteByCodigo(id)
    }

    // Get Book by id
    fun getSiteByOperador(id: String): Site? {
        return siteDao.getSiteByCodigo(id)
    }

}