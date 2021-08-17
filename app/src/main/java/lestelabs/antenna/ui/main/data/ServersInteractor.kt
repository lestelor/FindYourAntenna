package lestelabs.antenna.ui.main.data


class ServersInteractor(private val ServerDao: ServerDao) {

    // Get All Books from DAO
    fun getAllServers(): MutableList<Server> {
        return ServerDao.getAllServers()
    }

    // Save Book
    fun saveServer(Server: Server) {
        ServerDao.saveServer(Server)
    }

    // Save List of Books
    fun saveServers(vararg Servers: Server) {
//        var i=0
//        Servers.forEach {
//            Log.d("Serversinteractor", "saved " + i)
//            i++
//            saveServer(it) }
        ServerDao.saveServers(*Servers)
    }

    // Get Book by id
    fun getServerByName(id: String): Server? {
        return ServerDao.getServerByName(id)
    }

    // Get Book by id
    fun getServerBySponsor(id: String): Array<Server>? {
        return ServerDao.getServerBySponsor(id)
    }

}