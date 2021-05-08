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
    fun saveSites(sites: List<Site>) {
        sites.forEach { saveSite(it) }
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