package tx.nodes

fun main() {

    val masterNode = MasterNode()
    masterNode.run()

    for (i in 0..6) {
        Thread.sleep(100)
        val node = Node(7000 + i)
        node.run()
    }
    /*Thread.sleep(200)
    val toStop = Node(8000)
    toStop.run()
    for (i in 4..15) {
        Thread.sleep(100)
        val node = Node(7000 + i)
        node.run()
    }

    Thread.sleep(4000)
    println("Shutdown: ${toStop.getReference()}")
    toStop.shutdown()*/
}