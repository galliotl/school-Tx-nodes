package tx.nodes

import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import tx.nodes.models.Message

/**
 * Just a paticular node that we know the ip/port of
 * Also, it handles some specific messages compared to our
 * regular Nodes
 */
class MasterNode() : Node(ip = "localhost", port = 7777) {

    override fun run() {
        log("running...")
        val jobs = startMainCoroutines()
        runBlocking { joinAll(*jobs.toTypedArray()) }
    }

    override fun log(msg: String) {
        println("MN: $msg")
    }

    /**
     * The MN has some specific msgs he has to take into consideration
     */
    override fun dealWithMessage(msg: Message) {
        when(msg.type) {
            /**
             * msg.data = hashCode of the stored data
             * msg.senderReference = reference of the node that stores the data
             * We send this information to all of the nodes
             */
            "put ok" -> {
                log("${msg.senderReference} has kept the data ${msg.data}")
                distributedHashTable.add(msg.data as Int, msg.senderReference)
                sendMultiple(children, Message(type="new dht", senderReference=msg.senderReference, data=msg.data))
            }
            else -> super.dealWithMessage(msg)
        }
    }
}

fun main() {
    val master = MasterNode()
    master.run()
}