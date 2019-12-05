package tx.nodes

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.origin
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
import tx.nodes.API.ApiDhtDetail
import tx.nodes.API.ApiGetResponse
import tx.nodes.API.ApiPostResponse
import tx.nodes.models.DHT
import tx.nodes.API.PostSnippet
import tx.nodes.http.HttpClientWrapper
import tx.nodes.strategy.DecisionMaking
import tx.nodes.strategy.DefaultStrategy
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// TODO: node crash remove from DHT
/**
 * ! Warning: this cannot be tested outside of a docker container using port forwarding. Use docker compose using the docker-compose.yml file to run this project
 */
open class Node(val amIMasterNode: Boolean = false, private val decisionMaking: DecisionMaking, protected val debug: Boolean = false) {
    /**
     * This is the port used by the server
     */
    private val httpPort = 80

    // Node activity
    private var active = true
    private val dataMap: HashMap<Int, Any> = HashMap()
    private val httpClient = HttpClientWrapper()

    // IP tree structure
    private var parent:String = "masternode"
    private var myChildren: ArrayList<String> = ArrayList()
    private var distributedHashTable = DHT()

    open fun run() {
        val jobs = startMainCoroutines()
        if(!amIMasterNode) {
            log("not a mn")
            jobs += GlobalScope.launch { connectionToMasterNode() }
        }
        log("running")
        // Used to keep the code running. Otherwise, run stops and so does the node
        runBlocking { joinAll(*jobs.toTypedArray()) }
    }

    private fun startMainCoroutines(): MutableList<Job> {
        val jobs = mutableListOf(GlobalScope.launch { httpServer() })
        jobs += GlobalScope.launch { pingCheck() }
        return jobs
    }

    // todo: find a way to get brothers
    private suspend fun httpServer() {
        val server = embeddedServer(Netty, httpPort) {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
                }
            }
            routing {
                /**
                 * The route used to send and receive data
                 */
                route("/") {
                    get {
                        val dataId = call.request.queryParameters["id"]?.toInt()
                        // check if we have the data
                        log("I received a get message for id: $dataId")
                        if(dataMap[dataId] != null) call.respond(ApiGetResponse(data = dataMap[dataId] as String))
                        // check who has the data
                        else {
                            val ip = dataId?.let { it1 -> decisionMaking.dataExists(it1, distributedHashTable) }
                            if(ip == null) call.respond(HttpStatusCode.BadRequest, "Id isn't correct")
                            else {
                                log("I'm transferring the call to $ip, url: http://$ip/?id=$dataId")
                                val data = httpClient.getTransfer(ip=ip, dataId=dataId)
                                call.respond(data)
                            }
                        }
                    }
                    post {
                        val snippet = call.receive<PostSnippet>()
                        val data = snippet.data.text
                        log("I received a message")
                        if(decisionMaking.shouldKeepData(myChildren.size)) {
                            dataMap[data.hashCode()] = data
                            log("I keep the data: \n$dataMap")
                            decisionMaking.notifyDht(data.hashCode(), amIMasterNode, myChildren)
                            log("everyone has been notified")
                        }
                        decisionMaking.transferForReplication(snippet, myChildren)
                        log("I sent the message to my children")
                        // it assumes that at least one node will store it
                        call.respond(ApiPostResponse(data.hashCode()))
                    }
                }
                /**
                 * The route used for node connection
                 */
                route("/connect") {
                    get {
                        val senderIp = call.request.origin.remoteHost

                        log("I received a connection request from $senderIp")

                        // we can add him = we send ok
                        if (decisionMaking.canAddNewChild(senderIp, myChildren)) {
                            log("$senderIp has been added to my children")
                            myChildren.add(senderIp)
                            call.respond(HttpStatusCode.OK)
                        }
                        // we transfer to a node that can accept him
                        else {
                            log("cannot add him to my children...")
                            decisionMaking.childToTransferConnectTo(myChildren)?.let { return@get call.respondRedirect("http://$it/connect") }
                        }
                    }
                }
                /**
                 * The route used for pinging
                 */
                route("/api/ping") {
                    get {
                        call.respond(HttpStatusCode.OK)
                        val dataId = call.receive<Int>()
                    }
                }
                /**
                 * The route used to manage dht
                 */
                route("/dht") {
                    post {
                        val senderIp = call.request.origin.remoteHost
                        val dhtDetail = call.receive<ApiDhtDetail>()

                        // every node is ownIp agnistic so it is possible we receive a detail without an ip, then it means the sender is the owner
                        if(dhtDetail.owner == null) dhtDetail.owner = senderIp
                        log("${dhtDetail.owner} has added ${dhtDetail.dataId} to its db")

                        // I add this new information to my dht
                        distributedHashTable.add(dhtDetail.dataId, dhtDetail.owner!!)
                        log("dht:\n$distributedHashTable")
                        // I delegate the handling of how to transfer it
                        decisionMaking.transferDhtProtocol(dhtDetail, children = myChildren)
                    }
                }
            }
        }
        server.start(wait = true)
    }

    /**
     * ping check verifies that our children and parent are still active
     * children are just removed while parent triggers the reconnection
     * to the network
     */
    private suspend fun pingCheck(rate: Long = 10000) {
        while (active) {
            delay(rate)
            if(!httpClient.ping(parent) && !amIMasterNode) {
                // We reconnect to the network going through the MN
                log("parent $parent isn't reachable")
                connectionToMasterNode()
            }
            if(myChildren.isNotEmpty()) {
                myChildren = ArrayList(myChildren.filter { httpClient.ping(it) })
            }
        }
    }

    /**
     * Allows us to create a node before a masternode
     */
    private suspend fun connectionToMasterNode(rate: Long = 3000) {
        while (!httpClient.ping()) {
            delay(rate)
        }
        val call = httpClient.masternodeConnectGet()
        if(call.response.status == HttpStatusCode.OK) {
            parent = call.request.url.host
            log("my new parent is $parent")
        } else {
            log(call.response.status.toString())
        }
    }

    private fun log(msg: String, write: Boolean = debug) {
        if(write) println("${this.hashCode()}: $msg")
    }
}

fun main() {
    val mnVar: String = System.getenv("MN") ?: "default_value"
    val node = Node(debug = true, amIMasterNode=(mnVar == "TRUE"), decisionMaking = DefaultStrategy())
    node.run()
}
