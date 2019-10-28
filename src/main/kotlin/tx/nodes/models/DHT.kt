package tx.nodes.models

class DHT: HashMap<Int, ArrayList<NodeReference>>() {
    fun add(hash: Int, node: NodeReference) {
        if (this[hash] == null) { this[hash] = ArrayList() }
        this[hash]?.add(node)
    }
    override fun toString(): String {
        var string = ""
        for ((key, value) in this) {
            string += "\n$key || ${value}"
        }
        return string
    }
}