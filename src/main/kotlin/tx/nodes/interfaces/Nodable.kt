package tx.nodes.interfaces

import tx.nodes.models.Message
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.net.Socket
import kotlinx.coroutines.*
import java.net.SocketAddress

interface Nodeable {
    fun run()
    fun send(os: ObjectOutputStream, msg: Message) {
        os.writeObject(msg)
    }
    fun listen()
    fun connectionHandler(socket: Socket)
    fun shutdown()
}