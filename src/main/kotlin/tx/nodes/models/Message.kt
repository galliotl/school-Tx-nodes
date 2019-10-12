package tx.nodes.models

import java.io.Serializable

class Message(val data: Any = "", val type: String, val senderReference: NodeReference): Serializable {
    val nodeId: String = "${senderReference.ip}${senderReference.port}"
}