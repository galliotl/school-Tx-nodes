package tx.nodes.strategy

import kotlinx.coroutines.*
import tx.nodes.API.ApiDhtDetail
import tx.nodes.API.ApiGetResponse
import tx.nodes.API.PostSnippet
import tx.nodes.http.HttpClientWrapper
import tx.nodes.models.DHT

class DefaultStrategy(): DecisionMaking {

    private val httpClient = HttpClientWrapper()
    override val replicaProbability: Double = 0.5
    override val maxOfChild: Int = 5


    override suspend fun handleGet(dataid: Int, datamap: HashMap<Int, Any>, dht: DHT, parent: String): ApiGetResponse? {
        return if(datamap[dataid] != null) ApiGetResponse(data = datamap[dataid] as String)
        // check who has the data
        else {
            val ip = whoHasTheData(dataid, dht)
            if(ip == null) null
            else {
                val data = httpClient.getTransfer(ip=ip, dataId=dataid)
                data
            }
        }
    }

    override suspend fun handlePost(data: String, mn: Boolean, children: List<String>, datamap: HashMap<Int, Any>, parent: String, dht: DHT) {
        if(shouldKeepData(children.size)) {
            datamap[data.hashCode()] = data
            notifyDht(data.hashCode(), mn, children)
        }
        transferForReplication(data, children)
    }

    private suspend fun notifyDht(dataId: Int, mn: Boolean, children: List<String>) = GlobalScope.launch {
        val apiDhtDetail = ApiDhtDetail(null, dataId)
        if (mn) {
            transferDhtProtocol(apiDhtDetail, children)
        } else {
            httpClient.postDhtUpdateTo(apiDhtDetail, httpClient.masterNodeReference)
        }
    }

    private fun shouldKeepData(numberOfChildren: Int): Boolean {
        return Math.random() < 1/(numberOfChildren + 1)
    }

    private fun whoHasTheData(dataId: Int, dht: DHT): String? {
        val nodeList = dht[dataId]
        println(nodeList)
        return if(nodeList.isNullOrEmpty()) null
        else getRandom(nodeList)
    }

    private suspend fun transferForReplication(data: String, nodeChildren: List<String>) = GlobalScope.launch {
        val toSend = PostSnippet(data = PostSnippet.Text(text = data))
        // we send to all the node we chose regarding our
        getChildrenFromRepProb(nodeChildren).map { async {
            httpClient.postSnippetTo(toSend, it)
        } }.awaitAll()
    }

    override fun canAddNewChild(newChild: String, currentChildren: List<String>): Boolean {
        return currentChildren.size < maxOfChild
    }

    override fun childToTransferConnectTo(currentChildren: List<String>): String? {
        return getRandom(currentChildren)
    }

    /**
     * In this strategy, every dht information comes from the masternode and we transfer it to our children
     */
    private suspend fun transferDhtProtocol(apiDhtDetail: ApiDhtDetail, children: List<String>) = GlobalScope.launch {
        children.map {
            async {
                httpClient.postDhtUpdateTo(apiDhtDetail, it)
            }
        }.awaitAll()
    }

    override fun handlePostDht(senderIp: String, dhtDetail: ApiDhtDetail, dht: DHT, children: ArrayList<String>) {
        // every node is ownIp agnostic so it is possible we receive a detail without an ip, then it means the sender is the owner
        if(dhtDetail.owner == null) dhtDetail.owner = senderIp
        dht.add(dhtDetail.dataId, dhtDetail.owner!!)
        // I delegate the handling of how to transfer it
        GlobalScope.launch { transferDhtProtocol(dhtDetail, children = children)}
    }

    override fun handleGetDht(dataid: Int, dht: DHT, children: ArrayList<String>, parent: String): String {
        return ""
    }
}