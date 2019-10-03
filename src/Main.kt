package utt.tx

fun main() {
    val masterNode = MasterNode()
    masterNode.run()

    val node1 = SlaveNode(7778)
    node1.run()

    val node2 = SlaveNode(7779)
    node2.run()

/*    Thread.sleep(2000)

    node2.shutdown()*/
}