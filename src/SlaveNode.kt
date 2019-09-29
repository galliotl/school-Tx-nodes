package utt.tx

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

class SlaveNode(val port: kotlin.Int): Nodeable {
    private val MASTER_NODE_IP: String = "localhost"
    private val MASTER_NODE_PORT = 7777

    private var osMasterNode: OutputStream = OutputStream.nullOutputStream()

    private val NODE_IP: String = "localhost"
    private val NODE_ID: String = "$NODE_IP$port"

    private val server: ServerSocket = ServerSocket(port)
    private var listen = true
    private var connectedToMasterNode = false

    override fun run() {
        // Start listening server
        thread{ listen() }

        // Start a connection with the main node
        thread { connectionToMasterNode() } 
    }

    fun connectionToMasterNode() {
        try {
            val masterNodeSocket: Socket = Socket(MASTER_NODE_IP, MASTER_NODE_PORT)
            osMasterNode = masterNodeSocket.getOutputStream()
            val reader: Scanner = Scanner(masterNodeSocket.getInputStream())

            send(osMasterNode, NODE_ID)

            connectedToMasterNode = true
            println("$NODE_ID: is connected to master node")
            while (connectedToMasterNode) {
                val text = reader.nextLine()
                println("$port Message from MN: $text")
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
        // val os: OutputStream = socket.getOutputStream()
        val reader: Scanner = Scanner(socket.getInputStream())
        
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

    /**
     * sends a string to a given outputstream
     */
    override fun send(os: OutputStream, msg: String) {
        os.write((msg + '\n').toByteArray(Charset.defaultCharset()))
    }

    override fun shutdown() {
        listen = false
        connectedToMasterNode = false
    }
}