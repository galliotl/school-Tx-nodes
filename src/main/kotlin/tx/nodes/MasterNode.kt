package tx.nodes

import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.concurrent.thread
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tx.nodes.models.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.Exception
import java.net.Socket

/**
 * TODO: define
 */
class MasterNode() : Node(ip = "localhost", port = 7777) {
    private val httpPort = 7770
    // private val dataRefMap: HashMap<String, NodeReference> = HashMap()
    private val responseTimeout = 2000L

    override fun run() {
        // Start listening server
        thread { tcpServer() }
        thread { httpserver() }
        thread { idleCheck(10000) }
    }

    fun httpserver() {
        val server = embeddedServer(Netty, httpPort) {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
                }
            }
            routing {
                get("/") {
                    val dataId = call.request.queryParameters["id"]
                    var returned = false
                    val job = launch {
                        // get the data
                        val node = getRandomChild()
                        if (node != null) {
                            try {
                                val socket = Socket(node.ip, node.port)
                                val ois = ObjectInputStream(socket.getInputStream())
                                ObjectOutputStream(socket.getOutputStream()).writeObject(dataId?.let { it1 ->
                                    Message(
                                        type = "get",
                                        data = it1,
                                        senderReference = ownReference
                                    )
                                })
                                val msg = ois.readObject() as Message
                                when(msg.type) {
                                    "get ok" -> {
                                        call.respondText(msg.data.toString(), ContentType.Text.Html)
                                        returned = true
                                    }
                                }
                            } catch(e:Exception) {
                                log("couldn't send the request")
                                call.respondText("error, host is not reachable", ContentType.Text.Html)
                                e.printStackTrace()
                            }
                        }
                    }
                    delay(responseTimeout)
                    job.cancelAndJoin()
                    if(!returned) call.respond(HttpStatusCode.Accepted, mapOf("response" to "timeout"))
                }
                /**
                 * We are going to use the data hashcode as an index
                 */
                post("/") {
                    val data = call.receive<PostSnippet>() // Todo: adapt to our api
                    call.respond(mapOf("uid" to data.hashCode()))
                    var childrenFromRepProb = getChildrenFromRepProb()
                    log("put request to ${childrenFromRepProb.size} children")
                    for(child in childrenFromRepProb) {
                        send(child, Message(type="put", senderReference=ownReference, data=data))
                    }
                }
            }
        }
        server.start(wait = true)
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

fun main() {
    val master = MasterNode()
    master.run()
}