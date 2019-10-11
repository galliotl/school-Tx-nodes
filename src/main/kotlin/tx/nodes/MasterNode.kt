package tx.nodes

import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread
import tx.nodes.interfaces.Nodeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintWriter
import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tx.nodes.models.Message
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


/**
 * Sort of proxy
 *
 * Starts, then
 * 1) listen to connection requests
 * 2) accepts them
 * 3) handles in a new thread
 *  a. Listen for messages from InputStream
 *  b. Can send messages to defined OutputStream
 *
 * V1: Keeps the connection thread active = highly non scalable
 */
class MasterNode : Nodeable {
    private val IP = "localhost"
    private val PORT = 7777
    private val HTTP_PORT = 7770
    private val server: ServerSocket = ServerSocket(PORT)
    private val ipMap: HashMap<String, InetSocketAddress> = HashMap()
    private var active = true

    override fun run() {
        // Start listening server
        active = true
        thread{ listen() }
        thread{ httpserver() }
        thread{ idleCheck(10000) }
    }

    override fun listen() = runBlocking{
        println("   MN: master node listening on port $PORT")
        active = true
        while (active) {
            val node = server.accept()
            GlobalScope.launch { connectionHandler(node) }
        }
    }

    /**
     * Todo: describe how it's done
     */
    override fun connectionHandler(socket: Socket) {
        try {
            val oos = ObjectOutputStream(socket.getOutputStream())
            val ois = ObjectInputStream(socket.getInputStream())

            fun handleMessage(msg: Message) {
                // we check if we have the sender as a contact
                if (ipMap[msg.nodeId] == null) {
                    ipMap[msg.nodeId] = InetSocketAddress.createUnresolved(msg.senderIp, msg.senderPort)
                }
                when (msg.type) {
                    "connect" -> {
                        // we just added it in the first statement, so we print a confirmation
                        println("   MN: ${msg.nodeId} is now added in contacts $ipMap")
                    }
                    else -> {
                        println("   MN: type not known, connection will shutdown")
                    }
                }
            }

            try {
                val msg = ois.readObject() as Message
                handleMessage(msg)
                // connection is always closed after msg is dealt with
                oos.close()
                ois.close()
                socket.close()
            } catch (e: Exception) {
                oos.close()
                ois.close()
                socket.close()
            }
        } catch (e: Exception) {
            // Exception thrown when the input streams are corrupted
            println("   MN: wrong TCP connection")
        }
    }

    fun httpserver() {
        println("   MN: listening for http requests on port $HTTP_PORT")
        HttpServer.create(InetSocketAddress(HTTP_PORT), 0).apply {
            createContext("/request") { http ->
                when (http.requestMethod.toString()) {
                    "GET" -> {
                        println("   MN: ${getQueryParams(http.requestURI.query.toString())}")
                        println("   MN: ${getQueryType(http.requestURI.query.toString())}")
                        http.responseHeaders.add("Content-type", "text/plain")
                        http.sendResponseHeaders(200, 0)
                        PrintWriter(http.responseBody).use { out ->
                            out.println("Hello ${http.remoteAddress.hostName}!")
                        }
                    }
                    "POST" -> {
                        http.responseHeaders.add("Content-type", "text/plain")
                        http.sendResponseHeaders(200, 0)
                        PrintWriter(http.responseBody).use { out ->
                            out.println("Hello ${http.remoteAddress.hostName}!")
                        }
                    }
                    else -> {
                        http.responseHeaders.add("Content-type", "text/plain")
                        http.sendResponseHeaders(200, 0)
                        PrintWriter(http.responseBody).use { out ->
                            out.println("Do not use it")
                        }
                    }
                }
            }
            start()
        }
    }

    fun getQueryParams(params: String): String {
        var toReturn = "could not find query params"
        try {
            val p = Pattern.compile("=(.*)")
            val matcher = p.matcher(params)
            if (matcher.find()) {
                toReturn = matcher.group(1)
            }
        } catch (ex: PatternSyntaxException) {
            ex.printStackTrace()
            // error handling
        }
        return toReturn
    }
    fun getQueryType(params: String): String {
        var toReturn = "could not find query params"
        try {
            val p = Pattern.compile("(.*)=")
            val matcher = p.matcher(params)
            if (matcher.find()) {
                toReturn = matcher.group(1)
            }
        } catch (ex: PatternSyntaxException) {
            ex.printStackTrace()
        }
        return toReturn
    }

    fun idleCheck(rate: Long) {
        println("   MN: the idle checker deamon has started")
        /**
         * Inner function, checks if a certain ip port node is reachable. It's a homemade solution as it will use Messages
         */
        fun isRemoteNodeReachable(inet: InetSocketAddress, timeout: Int = 2000) {
            val nodeId = "${inet.hostName}${inet.port}"
            try {
                // If connection is accepted then it means the node is active
                val socket = Socket(inet.hostName, inet.port)
                socket.close()
            } catch (e: Exception) {
                println("   MN: $nodeId isn't reachable, removed")
                ipMap.remove(nodeId)
            }
        }
        while (active) {
            Thread.sleep(rate)
            for ((id, inetSocket) in ipMap) {
                GlobalScope.launch { isRemoteNodeReachable(inetSocket) }
            }
        }
        println("   MN: the idle checker daemon has stopped")
    }

    override fun shutdown() {
        active = false
        server.close()
    }
}

fun main() {
    val master = MasterNode()
    master.run()
}