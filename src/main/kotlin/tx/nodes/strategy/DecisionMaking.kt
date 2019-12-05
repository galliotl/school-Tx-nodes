package tx.nodes.strategy

import kotlinx.coroutines.Job
import tx.nodes.API.ApiDhtDetail
import tx.nodes.API.PostSnippet
import tx.nodes.models.DHT

interface DecisionMaking {
    val replicaProbability: Double
    val maxOfChild: Int

    fun dataExists(dataId: Int, dht: DHT): String?
    suspend fun transferForReplication(data: PostSnippet, nodeChildren: List<String>): Job
    suspend fun notifyDht(dataId: Int, mn: Boolean, children: List<String>): Job
    fun shouldKeepData(numberOfChildren: Int): Boolean
    fun canAddNewChild(newChild: String, currentChildren: List<String>): Boolean
    fun childToTransferConnectTo(currentChildren: List<String>): String?
    suspend fun transferDhtProtocol(apiDhtDetail: ApiDhtDetail, children: List<String>): Job
}