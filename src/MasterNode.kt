package utt.tx

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

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
    private val osMap: HashMap<String, OutputStream> = HashMap<String, OutputStream>()
    private var listen = true
    private var connectedToMasterNode = false

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
        val os: OutputStream = socket.getOutputStream()
        val reader: Scanner = Scanner(socket.getInputStream())

        // The first msg we send is the nodeID
        val nodeId: String = reader.nextLine()
        osMap.put(nodeId, os)
        print(osMap)
        println("   MN: connected to $nodeId")

        Thread.sleep(1000)
        osMap.get(nodeId)?.let { send(it, "$nodeId, you better disconnect") }
        var connected = true
        while (connected) {
            try {
                // At the moment we just print any msg
                val text = reader.nextLine()
                println("   MN received: $text")
            } catch (ex: Exception) {
                connected = false
                println("master node has lost a connection")
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
        server.close()
    }
}