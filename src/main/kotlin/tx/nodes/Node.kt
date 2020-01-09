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
import tx.nodes.API.ApiPostResponse
import tx.nodes.models.DHT
import tx.nodes.API.PostSnippet
import tx.nodes.http.HttpClientWrapper
import tx.nodes.strategy.DecisionMaking
import tx.nodes.strategy.DefaultStrategy
import tx.nodes.strategy.IncompleteDhtStrategy
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
    private var myParent:String = "masternode"
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
                        val valueToReturn = dataId?.let { it1 -> decisionMaking.handleGet(
                            dataid = it1,
                            datamap = dataMap,
                            dht = distributedHashTable,
                            parent = myParent
                        ) }
                        if (valueToReturn == null) call.respond(
                            status = HttpStatusCode.NotFound,
                            message = "data not found"
                        )
                        else call.respond(valueToReturn)
                    }
                    post {
                        val snippet = call.receive<PostSnippet>()
                        val data = snippet.data.text
                        log("I received a message")
                        decisionMaking.handlePost(
                            data = data,
                            children = myChildren,
                            mn = amIMasterNode,
                            datamap = dataMap,
                            parent = myParent,
                            dht = distributedHashTable
                        )
                        log("was value added ? ${dataMap[data.hashCode()]}")
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
                    }
                }
                /**
                 * The route used to manage dht
                 */
                route("/dht") {
                    post {
                        val senderIp = call.request.origin.remoteHost
                        val dhtDetail = call.receive<ApiDhtDetail>()
                        decisionMaking.handlePostDht(
                            senderIp = senderIp,
                            dhtDetail = dhtDetail,
                            dht = distributedHashTable,
                            children = myChildren
                        )
                    }
                    get {
                        val dataId = call.request.queryParameters["id"]?.toInt()
                        if (dataId == null) {
                            call.respond(HttpStatusCode.BadRequest, "no id specified")
                            return@get
                        }
                        val node = decisionMaking.handleGetDht(
                            children = myChildren,
                            dht = distributedHashTable,
                            parent = myParent,
                            dataid = dataId
                        )
                        val dhtDetail = ApiDhtDetail(dataId = dataId, owner = node)
                        call.respond(dhtDetail)
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
            if(!httpClient.ping(myParent) && !amIMasterNode) {
                // We reconnect to the network going through the MN
                log("parent $myParent isn't reachable")
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
            myParent = call.request.url.host
            log("my new parent is $myParent")
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

    val strategy = IncompleteDhtStrategy() // DefaultStrategy()

    val node = Node(
        debug = true,
        amIMasterNode=(mnVar == "TRUE"),
        decisionMaking = strategy
    )
    node.run()
}
