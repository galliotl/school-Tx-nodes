package tx.nodes.models

class DHT: HashMap<Int, ArrayList<NodeReference>>() {
    fun add(hash: Int, node: NodeReference) {
        if (this[hash] == null) this[hash] = arrayListOf(node)
        // We have to use this weird formula instead of a set and so on because the objects are not the same although their content is
        this[hash]?.let { if(it.find{element -> element.id == node.id} == null) it.add(node) }
    }
    override fun toString(): String {
        var string = "DHT:"
        for ((key, value) in this) string += "\n$key || $value"
        return string
    }
}