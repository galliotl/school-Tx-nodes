package utt.tx

import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread
import utt.tx.interfaces.Nodeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintWriter
import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer

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
    private val server: ServerSocket = ServerSocket(PORT)
    private val osMap: HashMap<String, ObjectOutputStream> = HashMap<String, ObjectOutputStream>()
    private var listen = true

    override fun run() {
        // Start listening server
        thread{ listen() }
        thread{ httpserver() }
    }

    override fun listen() {
        println("   MN: master node listening on port $PORT")
        listen = true
        while (listen) {
            val node = server.accept()
            thread { connectionHandler(node) }
        }
    }

    /**
     * Handles a connection with a certain socket
     */
    override fun connectionHandler(socket: Socket) {
        val oos = ObjectOutputStream(socket.getOutputStream())
        val ois = ObjectInputStream(socket.getInputStream())
        var nodeId = ""
        /**
         * internal function is only used in a connection handler
         */
        fun handleMessage(msg: Message) {
            when(msg.type) {
                "connect" -> {
                    nodeId = "${msg.senderIp}${msg.senderPort}"
                    osMap[nodeId] = oos
                    println("   MN: connected to $nodeId")
                    send(oos, Message(
                        msg="connection is confirmed",
                        type="connect confirm", // TODO, find better name and write as class
                        senderIp=this.IP,
                        senderPort=PORT
                    ))
                }
                else -> {
                    print("   MN: type not known, connection will shutdown")
                    socket.close()
                }
            }
        }

        var connected = true
        while (connected) {
            try {
                val msg = ois.readObject() as Message
                handleMessage(msg)
            } catch (ex: Exception) {
                connected = false
                oos.close()
                ois.close()
                osMap.remove(nodeId)
                println("   MN node has lost connection to $nodeId")
                println("   MN still is connected to $osMap")
            }
        }
    }

    fun httpserver() {
        HttpServer.create(InetSocketAddress(7770), 0).apply {
            createContext("/request") { http ->
                when (http.requestMethod.toString()) {
                    "GET" -> {
                        println(http.requestURI.query.toString())
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
    override fun shutdown() {
        listen = false
        server.close()
    }
}

fun main() {
    val master = MasterNode()
    master.run()
}