package tx.nodes.models

import java.io.Serializable

class NodeReference(val ip: String, val port: Int): Serializable {
    override fun toString(): String {
        return ip + port
    }
}