package tx.nodes.models

import java.io.Serializable

class Message(val msg: String = "", val type: String, val senderIp: String, val senderPort: Int = 7777): Serializable {
    val nodeId: String = "$senderIp$senderPort"
}