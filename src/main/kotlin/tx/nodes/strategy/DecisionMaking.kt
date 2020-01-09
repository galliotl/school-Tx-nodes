package tx.nodes.strategy

import tx.nodes.API.ApiDhtDetail
import tx.nodes.API.ApiGetResponse
import tx.nodes.models.DHT
import kotlin.math.floor

interface DecisionMaking {
    val replicaProbability: Double
    val maxOfChild: Int

    // connection methods
    fun canAddNewChild(newChild: String, currentChildren: List<String>): Boolean
    fun childToTransferConnectTo(currentChildren: List<String>): String?
    // data methods
    suspend fun handleGet(dataid: Int, datamap: HashMap<Int, Any>, dht: DHT, parent: String): ApiGetResponse?
    suspend fun handlePost(data: String, mn: Boolean=false, children: List<String>, datamap: HashMap<Int, Any>, parent: String, dht: DHT)
    // dht methods
    fun handlePostDht(senderIp: String, dhtDetail: ApiDhtDetail, dht: DHT, children: ArrayList<String>)
    fun handleGetDht(dataid: Int, dht: DHT, children: ArrayList<String>, parent: String): String
    // helper methods
    fun getChildrenFromRepProb(nodeChildren: List<String>): List<String> {
        val toReturn = nodeChildren.filter { Math.random() > replicaProbability }.toMutableList()
        if(toReturn.isEmpty()) getRandom(nodeChildren)?.let {toReturn.add(it)}
        return toReturn
    }
    fun getRandom(list: List<String>): String? {
        return if(list.isNotEmpty()) {
            list[floor(Math.random() * list.size).toInt()]
        } else null
    }

}