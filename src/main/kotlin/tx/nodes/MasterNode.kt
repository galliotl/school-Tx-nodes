package tx.nodes

import com.fasterxml.jackson.databind.SerializationFeature
import kotlin.concurrent.thread
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import tx.nodes.models.Message
import tx.nodes.models.NodeReference
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception
import java.net.Socket

/**
 * TODO: define
 */
class MasterNode() : Node(ip = "localhost", port = 7777) {
    private val httpPort = 7770
    private val dataRefMap: HashMap<String, NodeReference> = HashMap()

    override fun run() {
        // Start listening server
        thread { tcpServer() }
        thread{ httpserver() }
        thread{ idleCheck(10000) }
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
                    val value = dataMap[dataId]
                    if(value == null) {
                        val node = dataRefMap[dataId]
                        if(node == null) call.respondText("error, not known", ContentType.Text.Html)
                        else {
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
                                call.respondText(msg.data.toString(), ContentType.Text.Html)
                            } catch(e:Exception) {
                                log("couldn't send the request")
                                call.respondText("error, host is not reachable", ContentType.Text.Html)
                            }
                            // We have to transfer the request to node
                        }
                    }
                    else call.respondText(value.toString(), ContentType.Text.Html)
                }
                post("/") {
                    val data = call.receive<PostSnippet>()
                    call.respond(mapOf("response" to "OK"))
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
data class PostSnippet(val data: PostSnippet.Text) {
    data class Text(val text: String)
}

fun main() {
    val master = MasterNode()
    master.run()
}