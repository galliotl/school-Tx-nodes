package tx.nodes

import kotlin.concurrent.thread
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import tx.nodes.models.NodeReference

/**
 * TODO: define
 */
class MasterNode() : Node(ip = "localhost", port = 7777) {
    private val httpPort = 7770

    private val dataMap: HashMap<String, Any> = HashMap()
    private val dataRefMap: HashMap<String, NodeReference> = HashMap()

    override fun run() {
        // init a fake data
        dataMap["lelo"] = "dssj"
        dataRefMap["lelo"] = ownReference

        // Start listening server
        thread { tcpServer() }
        thread{ httpserver() }
        thread{ idleCheck(10000) }
    }

    fun httpserver() {
        val server = embeddedServer(Netty, httpPort) {
            routing {
                get("/") {
                    val dataId = call.request.queryParameters["id"]
                    val value = dataMap[dataId]
                    val node = dataRefMap[dataId]
                    when {
                        value != null -> call.respondText(value.toString(), ContentType.Text.Html)
                        node != null -> call.respondText(node.toString(), ContentType.Text.Html)
                        else -> call.respondText("not known", ContentType.Text.Html)
                    }
                }
            }
        }
        server.start(wait = true)
    }
}

fun main() {
    val master = MasterNode()
    master.run()
}