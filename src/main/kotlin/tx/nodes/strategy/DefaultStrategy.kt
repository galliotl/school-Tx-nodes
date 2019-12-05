package tx.nodes.strategy

import kotlinx.coroutines.*
import tx.nodes.API.ApiDhtDetail
import tx.nodes.API.PostSnippet
import tx.nodes.http.HttpClientWrapper
import tx.nodes.models.DHT
import kotlin.math.floor

class DefaultStrategy(): DecisionMaking {

    private val httpClient = HttpClientWrapper()
    override val replicaProbability: Double = 0.5
    override val maxOfChild: Int = 5

    override suspend fun notifyDht(dataId: Int, mn: Boolean, children: List<String>) = GlobalScope.launch {
        val apiDhtDetail = ApiDhtDetail(null, dataId)
        if (mn) {
            transferDhtProtocol(apiDhtDetail, children)
        } else {
            httpClient.postDhtUpdateTo(apiDhtDetail, httpClient.masterNodeReference)
        }
    }

    override fun shouldKeepData(numberOfChildren: Int): Boolean {
        return Math.random() < 1/(numberOfChildren + 1)
    }

    override fun dataExists(dataId: Int, dht: DHT): String? {
        val nodeList = dht[dataId]
        println(nodeList)
        return if(nodeList.isNullOrEmpty()) null
        else getRandom(nodeList)
    }

    private fun getRandom(list: List<String>): String? {
        return if(list.isNotEmpty()) {
            list[floor(Math.random() * list.size).toInt()]
        } else null
    }

    private fun getChildrenFromRepProb(nodeChildren: List<String>): List<String> {
        val toReturn = nodeChildren.filter { Math.random() > replicaProbability }.toMutableList()
        if(toReturn.isEmpty()) getRandom(nodeChildren)?.let {toReturn.add(it)}
        return toReturn
    }

    override suspend fun transferForReplication(data: PostSnippet, nodeChildren: List<String>) = GlobalScope.launch {
        // we send to all the node we chose regarding our
        getChildrenFromRepProb(nodeChildren).map { async {
            httpClient.postSnippetTo(data, it)
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
    override suspend fun transferDhtProtocol(apiDhtDetail: ApiDhtDetail, children: List<String>) = GlobalScope.launch {
        children.map {
            async {
                httpClient.postDhtUpdateTo(apiDhtDetail, it)
            }
        }.awaitAll()
    }
}