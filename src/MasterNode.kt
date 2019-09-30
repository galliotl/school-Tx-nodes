package utt.tx

import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread
import utt.tx.interfaces.Nodeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
 * V1: Keeps the connection thread active = highly unscalable
 */
class MasterNode : Nodeable {
    private val PORT = 7777
    private val server: ServerSocket = ServerSocket(PORT)
    private val osMap: HashMap<String, ObjectOutputStream> = HashMap<String, ObjectOutputStream>()
    private var listen = true

    override fun run() {
        // Start listening server
        thread{ listen() }
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

        var connected = true
        while (connected) {
            try {
                // At the moment we just print any msg
                val msg = ois.readObject() as Message
                handleMessage(msg, oos)
                println("   MN received: ${msg.msg}")
            } catch (ex: Exception) {
                connected = false
                println("master node has lost a connection")
            }
        }
    }

    override fun handleMessage(msg: Message, os: ObjectOutputStream) {
        when(msg.type) {
            "connect" -> {
                val nodeId = "${msg.senderIp}${msg.senderPort}"
                osMap[nodeId] = os
                println("   MN: connected to $nodeId")
            }
            else -> {
                print("   MN: type not known")
            }
        }
    }

    override fun shutdown() {
        listen = false
        server.close()
    }
}