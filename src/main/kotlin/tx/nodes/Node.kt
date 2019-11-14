package tx.nodes

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import tx.nodes.models.DHT
import tx.nodes.models.Message
import tx.nodes.models.NodeReference
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlinx.coroutines.GlobalScope
import kotlin.math.floor

open class Node(protected val port: Int, protected val ip: String = "localhost") {
    private val httpPort = port + 1000

    // Node detail
    private var ownReference: NodeReference = NodeReference(ip, port)

    // Node activity
    private var active = true
    private val server = ServerSocket(port)
    private val dataMap: HashMap<Int, Any> = HashMap()
    private val messageQueue: Queue<Message> = ArrayDeque()

    // IP tree structure
    private val masterNodeReference = NodeReference("localhost", 7777)
    private val maxOfChild = 5
    var parent:NodeReference = masterNodeReference
    var children:ArrayList<NodeReference> = ArrayList()
    var brothers:ArrayList<NodeReference> = ArrayList()
    var distributedHashTable = DHT()
    private val replicaProbability: Double = 0.5

    open fun run() {
        val jobs = startMainCoroutines()
        send(masterNodeReference, Message(senderReference = ownReference, type = "connect"))
        runBlocking { joinAll(*jobs.toTypedArray()) }
    }

    protected fun startMainCoroutines(): List<Job> {
        val jobs = mutableListOf(GlobalScope.launch{tcpServer()})
        jobs += GlobalScope.launch { httpServer() }
        jobs += GlobalScope.launch { worker() }
        jobs += GlobalScope.launch { idleCheck(10000) }

        return jobs
    }

    /**
     * TCP server that listens on a given port and creates a coroutine to handle
     * every connections
     */
    private fun tcpServer() {
        while (active) {
            try {
                val node = server.accept()
                val msg = ObjectInputStream(node.getInputStream()).readObject() as Message
                dealWithMessage(msg)
                node.close()
            } catch(ioe: IOException) {
                // was just being scanned
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun worker() {
        /**
         * Tends to accumulate data in the later nodes aka leafs
         */
        fun shouldKeepData(): Boolean {
            return Math.random() < 1/(children.size + 1)
        }
        while(active) {
            val msg = messageQueue.poll()
            if(msg != null) {
                when (msg.type) {
                    /**
                     * Connect is the first msg a node sends. It contains its details.
                     * We check if we can still host a child and if yes everything is cool.
                     * If not, we transfer the responsibility to one of our children.
                     *
                     * The only way addInChildren returns false is if there's more than the accepted limit
                     * of children already. Contrary to getRandomChild who returns null when there's exactly 0 children in the list
                     * The 2 functions are therefore on opposition and getRandomChild cannot return a null value in this case.
                     */
                    "connect" -> {
                        if (!addInChildren(msg.senderReference)) {
                            val child = getRandomChild()
                            if (child != null) send(child, msg)
                        }
                    }
                    /**
                     * connect confirm means that the node was accepted by a parent node
                     * which reference can be found in the msg sent
                     * We get the brothers and update our parent
                     */
                    "connect confirmed" -> {
                        parent = msg.senderReference
                        if (msg.data is ArrayList<*> && msg.data.size > 0 && msg.data[0] is NodeReference) {
                            brothers = msg.data as ArrayList<NodeReference>
                        }
                    }
                    "add brother" -> {
                        if (msg.data is NodeReference) {
                            brothers.add(msg.data)
                        }
                    }
                    "put" -> {
                        val uid = msg.data.hashCode()
                        if (shouldKeepData() && dataMap[uid] == null) {
                            log("I keep data $uid")
                            dataMap[uid] = msg.data
                            send(masterNodeReference, Message(type = "put ok", senderReference = ownReference, data = uid))
                        }
                        sendMultiple(getChildrenFromRepProb(), msg)
                    }
                    /**
                     * msg.senderReference = reference of the node that stored
                     * msg.data = hashCode of the stored data
                     * We add the info to our dht and pass it on to our children
                     */
                    "new dht" -> {
                        distributedHashTable.add(msg.data as Int, msg.senderReference)
                        log(distributedHashTable.toString())
                        sendMultiple(children, msg)
                    }
                    else -> {
                        log("${msg.type} not known, connection will shutdown")
                    }
                }
            }

            }
        }

    private suspend fun httpServer() {
        val server = embeddedServer(Netty, httpPort) {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
                }
            }
            routing {
                route("/") {
                    get {
                        val dataId = call.request.queryParameters["id"]?.toInt()
                        // check if we have the data
                        if(dataMap[dataId] != null) call.respond(mapOf("data" to dataMap[dataId]))
                        // check who has the data
                        else {
                            val nodeList = distributedHashTable[dataId]
                            log("$dataId -> $nodeList")
                            if(nodeList.isNullOrEmpty()) call.respond(HttpStatusCode.BadRequest, "Id isn't correct")
                            else {
                                val randomChild = nodeList[floor(Math.random() * nodeList.size).toInt()]
                                val url = "http://${randomChild.ip}:${randomChild.port + 1000}/?id=$dataId" // TODO: find a more reliable way to fin http port
                                log(url)
                                call.respondRedirect(url)
                            }
                        }
                    }
                    post {
                        val data = call.receive<PostSnippet>() // Todo: adapt to our api
                        call.respond(mapOf("uid" to data.hashCode()))
                        sendMultiple(getChildrenFromRepProb(), Message(type="put", senderReference=ownReference, data=data))
                    }
                }
            }
        }
        server.start(wait = true)
    }

    /**
     * Idle check verifies that our children and parent are still active
     * children are just removed while parent triggers the reconnection
     * to the network
     */
    private fun idleCheck(rate: Long) {
        fun isRemoteNodeReachable(node: NodeReference): Boolean {
            return try {
                // If connection is accepted then it means the node is active
                val socket = Socket(node.ip, node.port)
                socket.close()
                true
            } catch (e: Exception) {
                false
            }
        }
        while (active) {
            Thread.sleep(rate)
            children = ArrayList(children.filter { isRemoteNodeReachable(it) })

            if(!isRemoteNodeReachable(parent)) {
                // We reconnect to the network going through the MN
                log("parent $parent isn't reachable")
                send(masterNodeReference, Message(type="connect", senderReference = ownReference))
            }
        }
    }



    /**
     * Is used to log a message, specifying the node who sent it
     */
    protected open fun log(msg: String) {
        // atm we just print it
        println("$ownReference: $msg")
    }

    /**
     * On the Simple node side, we just add it to the message queue
     */
    open fun dealWithMessage(msg: Message) {
        messageQueue.add(msg)
    }

    /* Children related-functions */
    /**
     * Returns a list of at least 1 nodeReference among our children given by the
     * probabililty of replicability
     */
    private fun getChildrenFromRepProb(): List<NodeReference> {
        val toReturn = children.filter { Math.random() > replicaProbability }.toMutableList()
        if(toReturn.isEmpty()) getRandomChild()?.let {toReturn.add(it)}
        return toReturn
    }
    private fun getRandomChild(): NodeReference? {
        return if(children.isNotEmpty()) {
            children[floor(Math.random() * children.size).toInt()]
        } else null
    }
    /**
     * The process of adding a node in our children is as follows:
     * - We check whether we have some available spaces in our children
     * if yes:
     * - we send to the node all of our children -> his brothers
     * - we send to all of our children the new node reference
     * - we add the node to our children
     * if no:
     * return false
     */
    private fun addInChildren(node: NodeReference): Boolean {
        if(children.size < maxOfChild) {
            send(node, Message(type = "connect confirmed", data = children, senderReference = ownReference))
            sendMultiple(children, Message(senderReference = ownReference, type = "add brother", data = node))
            children.add(node)
            return true
        }
        return false
    }

    /* TCP post functions */
    private fun send(node: NodeReference, msg: Message) {
        try {
            val socket = Socket(node.ip, node.port)
            val os = ObjectOutputStream(socket.getOutputStream())
            os.writeObject(msg)
        }
        catch(ioe: IOException) {
            log("connection was closed by remote host -> received")
            ioe.printStackTrace()
        } catch(e: Exception) {
            log("couldn't send the message")
            e.printStackTrace()
        }
    }
    protected fun sendMultiple(nodes: List<NodeReference>, msg: Message) = runBlocking {
        nodes.map { async { send(it, msg) } }.awaitAll()
    }

    fun shutdown() {
        active = false
    }
}

/**
 * Imposes a request like this :
 * {
 *  snippet: {
 *      text: "dsdfddf"
 *  }
 * }
 */
data class PostSnippet(val data: Text): Serializable {
    data class Text(val text: String): Serializable
}

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val node = Node(port) // to change each time
    node.run()
}
