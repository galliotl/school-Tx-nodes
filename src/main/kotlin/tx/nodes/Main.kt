package tx.nodes

fun main() {

    val masterNode = MasterNode()
    masterNode.run()
    for (i in 0..6) {
        Thread.sleep(100)
        val node = Node(7000 + i)
        node.run()
    }
}