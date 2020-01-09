package tx.nodes.strategy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import tx.nodes.API.ApiDhtDetail
import tx.nodes.API.ApiGetResponse
import tx.nodes.API.PostSnippet
import tx.nodes.http.HttpClientWrapper
import tx.nodes.models.DHT

class IncompleteDhtStrategy: DecisionMaking {

    private val httpClient = HttpClientWrapper()
    override val replicaProbability = 0.5
    override val maxOfChild: Int = 2

    override fun canAddNewChild(newChild: String, currentChildren: List<String>): Boolean {
        return currentChildren.size < maxOfChild
    }

    override fun childToTransferConnectTo(currentChildren: List<String>): String? {
        return getRandom(currentChildren)
    }

    override suspend fun handleGet(dataid: Int, datamap: HashMap<Int, Any>, dht: DHT, parent: String): ApiGetResponse? {
        // In this strategy, if we receive the request, it means we know where the data went
        if(datamap[dataid] != null) return ApiGetResponse(data = datamap[dataid] as String)

        val nodeList = dht[dataid]
        // do we know who has the data
        if (nodeList != null && nodeList.isNotEmpty()) {
            val node = getRandom(nodeList)
            return httpClient.getTransfer(node!!, dataid)
        }
        return null
    }

    // same as the default strategy
    override suspend fun handlePost(data: String, mn: Boolean, children: List<String>, datamap: HashMap<Int, Any>, parent: String, dht: DHT) {
        if(shouldKeepData(children.size)) {
            datamap[data.hashCode()] = data
        }
        transferForReplication(data, children, dht)
    }

    override fun handlePostDht(senderIp: String, dhtDetail: ApiDhtDetail, dht: DHT, children: ArrayList<String>) {
    }

    override fun handleGetDht(dataid: Int, dht: DHT, children: ArrayList<String>, parent: String): String {
        return ""
    }

    private fun shouldKeepData(numberOfChildren: Int): Boolean {
        return Math.random() < 1/(numberOfChildren + 1)
    }

    private suspend fun notifyDht(dataId: Int, mn: Boolean, children: List<String>, parent: String) = GlobalScope.launch {
        return@launch
    }

    private suspend fun transferForReplication(data: String, nodeChildren: List<String>, dht: DHT) = GlobalScope.launch {
        val toSend = PostSnippet(data = PostSnippet.Text(text = data))

        getChildrenFromRepProb(nodeChildren).map { async {
            // we add it to our dht because at least he should know where the data is
            dht.add(data.hashCode(), it)
            println("$it should know where the data is. Adding it to my dht")
            httpClient.postSnippetTo(toSend, it)
        } }.awaitAll()
    }
}
