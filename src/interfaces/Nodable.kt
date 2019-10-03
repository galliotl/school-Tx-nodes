package utt.tx.interfaces

import utt.tx.Message
import java.io.ObjectOutputStream
import java.net.Socket

interface Nodeable {
    fun run()
    fun send(os: ObjectOutputStream, msg: Message) {
        os.writeObject(msg)
    }    fun listen()
    fun connectionHandler(socket: Socket)
    fun shutdown()
}