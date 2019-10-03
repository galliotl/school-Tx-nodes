package utt.tx

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread
import utt.tx.interfaces.Nodeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SlaveNode(val port: kotlin.Int): Nodeable {
    private val MASTER_NODE_IP: String = "localhost"
    private val MASTER_NODE_PORT = 7777

    private var osMasterNode: ObjectOutputStream = ObjectOutputStream(OutputStream.nullOutputStream())

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

    fun connectionToMasterNode() {
        try {
            val masterNodeSocket = Socket(MASTER_NODE_IP, MASTER_NODE_PORT)
            osMasterNode = ObjectOutputStream(masterNodeSocket.getOutputStream())

            val msg = Message(
                msg="attempt to connect to master node",
                senderIp = this.NODE_IP,
                senderPort = this.port,
                type = "connect"
            )
            send(osMasterNode, msg)
            connectionHandler(masterNodeSocket)
        } catch (e: Exception) {
            osMasterNode.close()
        }
    }

    override fun listen() {
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

        val ois = ObjectInputStream(socket.getInputStream())
        /**
         * internal function is only used in a connection handler
         */
        fun handleMessage(msg: Message) {
            when(msg.type) {
                "connect confirm" -> {
                    println("$NODE_ID: MN has confirmed connection")
                }
                else -> {
                    print("$NODE_ID: type not known, connection will shutdown")
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
                println("node $port has lost a connection")
            }
        }
    }

    override fun shutdown() {
        listen = false
        osMasterNode.close()
    }
}


fun main(args: Array<String>) {
    val port = args[0].toInt()
    val slave = SlaveNode(port) // to change each time
    slave.run()
}