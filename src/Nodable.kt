package utt.tx

import java.io.OutputStream
import java.net.Socket

interface Nodeable {
    fun run()
    fun send(os: OutputStream, msg: String)
    fun listen()
    fun connectionHandler(socket: Socket)
    fun shutdown()
}