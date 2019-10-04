package utt.tx

fun main() {

    val masterNode = MasterNode()
    masterNode.run()

    val slaveNode = SlaveNode(7778)
    slaveNode.run()

    val slaveNode2 = SlaveNode(7779)
    slaveNode2.run()

    val slaveNode3 = SlaveNode(7780)
    slaveNode3.run()

    val slaveNode4 = SlaveNode(7781)
    slaveNode4.run()

    Thread.sleep(4000)

    slaveNode.shutdown()
    slaveNode2.shutdown()

    Thread.sleep(5000)

    slaveNode3.shutdown()

}