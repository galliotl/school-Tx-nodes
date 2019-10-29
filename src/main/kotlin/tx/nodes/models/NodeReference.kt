package tx.nodes.models

import java.io.Serializable

class NodeReference(val ip: String, val port: Int): Serializable {
    val id = (ip + port).hashCode()
    override fun toString(): String {
        return ip + port
    }
}