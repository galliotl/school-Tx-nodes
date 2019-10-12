package tx.nodes

fun main() {

    val masterNode = MasterNode()
    masterNode.run()
    Thread.sleep(100)
    val node1 = Node(7778)
    node1.run()

    Thread.sleep(100)
    val node2 = Node(7779)
    node2.run()

    Thread.sleep(100)
    val node3 = Node(7800)
    node3.run()

    Thread.sleep(100)
    val node4 = Node(7801)
    node4.run()

    Thread.sleep(100)
    val node5 = Node(7802)
    node5.run()

    Thread.sleep(100)
    val node6 = Node(7803)
    node6.run()
}