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
    private var connectedToMasterNode = false

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
            val ois = ObjectInputStream(masterNodeSocket.getInputStream())

            val msg = Message(
                msg="attempt to connect to master node",
                senderIp = "localhost",
                senderPort = this.port,
                type = "connect"
            )
            send(osMasterNode, msg)

            println("$NODE_ID: is connected to master node")

            connectedToMasterNode = true
            while (connectedToMasterNode) {
                val msg = ois.readObject() as Message
                handleMessage(msg, osMasterNode)
            }
        } catch (e: Exception) {
            connectedToMasterNode = false
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
        val reader = Scanner(socket.getInputStream())
        
        var connected = true
        while (connected) {
            try {
                val text = reader.nextLine()
                println(text)
            } catch (ex: Exception) {
                connected = false
                println("node $port has lost a connection")
            }
        }
    }

    override fun shutdown() {
        listen = false
        connectedToMasterNode = false
    }
}