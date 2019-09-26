import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    val server = ServerSocket(9999)
    println("Server is running on port ${server.localPort}")

    while (true) {
        val node = server.accept()
        println("node connected: ${node.inetAddress.hostAddress}")

        // Run node in it's own thread.
        thread { NodeHandler(node).run() }
    }

}

class NodeHandler(node: Socket) {
    private val node: Socket = node
    private val reader: Scanner = Scanner(node.getInputStream())
    private val writer: OutputStream = node.getOutputStream()
    private var running: Boolean = false

    fun run() {
        running = true
        while (running) {
            try {
                val text = reader.nextLine()
                if (text == "EXIT"){
                    shutdown()
                    continue
                }
                write(text)
            } catch (ex: Exception) {
                // TODO: Implement exception handling
                shutdown()
            } finally {

            }

        }
    }

    private fun write(message: String) {
        writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun shutdown() {
        running = false
        node.close()
        println("${node.inetAddress.hostAddress} closed the connection")
    }
}