package tx.nodes

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tx.nodes.models.Message
import tx.nodes.models.NodeReference
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.math.floor

open class Node(protected val port: Int, protected val ip: String = "localhost") {
    // Node detail
    protected var ownReference: NodeReference = NodeReference(ip, port)

    // Node activity
    protected var active = true
    protected val server = ServerSocket(port)
    protected val dataMap: HashMap<String, Any> = HashMap()

    // IP tree structure
    private val masterNodeReference = NodeReference("localhost", 7777)
    private val maxOfChild = 5
    var parent:NodeReference = masterNodeReference
    var children:MutableList<NodeReference> = mutableListOf()
    var brothers:MutableList<NodeReference> = mutableListOf()


    open fun run() {
        thread { tcpServer() }
        // we send a connection request to the master Node
        send(masterNodeReference, Message(senderReference = ownReference, type = "connect"))
        thread { idleCheck(10000) }
    }
    open fun shutdown() {
        active = false
        server.close()
    }
    fun getReference(): NodeReference { return ownReference }
    /**
     * Is used to log a message, specifying the node who sent it
     */
    protected fun log(msg: String) {
        // atm we just print it
        println("$ownReference: $msg")
    }
    /**
     * TCP server that listens on a given port and creates a coroutine to handle
     * every connections
     */
    protected fun tcpServer() {
        active = true
        while (active) {
            try {
                val node = server.accept()
                GlobalScope.launch { connectionHandler(node) }
            } catch(ioe: IOException) {
                // was just being scanned
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    protected fun connectionHandler(socket: Socket) {
        try {
            val oos = ObjectOutputStream(socket.getOutputStream())
            val ois = ObjectInputStream(socket.getInputStream())

            // Def of inner functions
            fun addInChildren(node: NodeReference): Boolean {
                if(children.size < maxOfChild) {
                    // we send all our children to the new child -> his brothers
                    send(node, Message(type = "connect confirmed", data = children, senderReference = ownReference))

                    // we send a reference of the node to all of our children so they can populate their neighbour
                    for(child in children) {
                        try {
                            send(child, Message(senderReference = ownReference, type = "add brother", data = node))
                        } catch (ex: Exception) {
                            log("couldn't send the brother reference to $child")
                        }
                    }
                    // then we add him
                    children.add(node)
                    return true
                }
                return false
            }
            fun sendBackData(any: Any) {
                try {
                    oos.writeObject(any)
                } catch(e:Exception) {
                    log("can't send back the data")
                }
            }
            fun getRandomChild(): NodeReference? {
                return children[floor(Math.random() * children.size).toInt()]
            }
            fun handleMessage(msg: Message) {
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
                        if(!addInChildren(msg.senderReference)) {
                            // We send a msg to our child
                            val child = getRandomChild()
                            // is always true, but prevent compilation errors
                            if (child != null) {
                                // we send the exact same msg to our child
                                log("can't add ${msg.senderReference} to my children, passing it to $child")
                                send(child, msg)
                            }
                        }
                    }
                    /**
                     * connect confirm means that the node was accepted by a parent node
                     * which reference can be found in the msg sent
                     * We get the brothers and update our parent
                     */
                    "connect confirmed" -> {
                        parent = msg.senderReference
                        if(msg.data is MutableList<*> && msg.data.size > 0 && msg.data[0] is NodeReference) {
                            brothers = msg.data as MutableList<NodeReference>
                        }
                    }
                    "add brother" -> {
                        if(msg.data is NodeReference) {
                            brothers.add(msg.data)
                        }
                    }
                    "get" -> {
                        if(dataMap[msg.data as String] != null) {
                            dataMap[msg.data]?.let { sendBackData(Message(type="get ok", data=it, senderReference=ownReference)) }
                        } else {
                            oos.writeObject(Message(type="get no", senderReference=ownReference)) // TODO: change, doesn't check for childrens
                        }
                    }
                    "put" -> {

                    }
                    else -> {
                        log("${msg.type} not known, connection will shutdown")
                    }
                }
            }

            try {
                val msg = ois.readObject() as Message
                handleMessage(msg)
                // connection is always closed after msg is dealt with
                oos.close()
                ois.close()
                socket.close()
            } catch (e: Exception) {
                log("couldn't handle the message")
                e.printStackTrace()
                oos.close()
                ois.close()
                socket.close()
            }
        }
        catch (ioe: IOException) {
            // Was just being checked by the parent, a connection was closed before I could read the streams
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun send(node: NodeReference, msg: Message) {
        try {
            val socket = Socket(node.ip, node.port)
            val os = ObjectOutputStream(socket.getOutputStream())
            os.writeObject(msg)
        } catch(ioe: IOException) {
            log("connection was closed by remote host -> received")
        } catch(e: Exception) {
            log("couldn't send the message")
            e.printStackTrace()
        }
    }

    /**
     * Idle check verifies that our parent is still active
     */
    fun idleCheck(rate: Long) {
        /**
         * Inner function, checks if a certain ip port node is reachable. It's a homemade solution as it will use Messages
         */
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
            // verify that our child are connected
            children.filter { isRemoteNodeReachable(it) }

            // we verify that our parent is connected
            if(!isRemoteNodeReachable(parent)) {
                // We reconnect to the network going through the MN
                log("parent $parent isn't reachable")
                send(masterNodeReference, Message(type="connect", senderReference = ownReference))
            }
        }
    }
}

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val node = Node(port) // to change each time
    node.run()
}
