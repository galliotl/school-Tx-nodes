package tx.nodes

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tx.nodes.models.Message
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread
import tx.nodes.interfaces.Nodeable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress

class SlaveNode(val port: Int): Nodeable {
    private val MASTER_NODE_IP: String = "0.0.0.0"
    private val MASTER_NODE_PORT = 7777

    private val NODE_IP: String = "localhost"
    private val NODE_ID: String = "$NODE_IP$port"

    private val server: ServerSocket = ServerSocket(port)
    private var listen = true

    private val data: HashMap<String, String> = HashMap<String, String>()

    override fun run() {
        // Start listening server
        thread{ listen() }

        // Start a connection with the main node
        thread { connectionToMasterNode() }
    }

    /**
     * TODO: define how it's done
     */
    fun connectionToMasterNode() {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(MASTER_NODE_IP, MASTER_NODE_PORT), 1000)
            ObjectOutputStream(socket.getOutputStream()).writeObject(Message(senderPort = port, senderIp = NODE_IP, type = "connect"))
            socket.close()
            println("$NODE_ID: MN has received my IP/PORT")
        } catch (ioe: IOException) {
            // connection was closed by MN
            println("$NODE_ID: MN has received my IP/PORT, but he closed the connection before")
        } catch (e: Exception) {
            println("$NODE_ID: couldn't connect to master node")
            e.printStackTrace()
        }
    }

    override fun listen() = runBlocking{
        listen = true
        while (listen) {
            val node = server.accept()
            GlobalScope.launch {
                try {
                    connectionHandler(node)
                } catch (e: Exception) {
                    // connection was closed before receiving anything. Either it was idle checked or the remote node crashed
                }
            }
        }
    }

    /**
     * Handles a connection with a certain socket and reads one msg from the connection
     */
    override fun connectionHandler(socket: Socket) {
        val ois = ObjectInputStream(socket.getInputStream())
        val oos = ObjectOutputStream(socket.getOutputStream())
        /**
         * internal function is only used in a connection handler
         */
        fun handleMessage(msg: Message) {
            when(msg.type) {
                // ATM we don't have any use case
                else -> {
                    print("$NODE_ID: type not known, connection will shutdown")
                }
            }
        }
        try {
            val msg = ois.readObject() as Message
            handleMessage(msg)
            ois.close()
            oos.close()
            socket.close()
        } catch (e: IOException) {
            // Connection was closed on the remote side
            ois.close()
            oos.close()
            socket.close()
        }
    }

    override fun shutdown() {
        listen = false
    }
}


fun main(args: Array<String>) {
    val port = args[0].toInt()
    val slave = SlaveNode(port) // to change each time
    slave.run()
}