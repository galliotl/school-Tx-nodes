package tx.nodes

import kotlinx.coroutines.*

suspend fun main() {
    val masterNode = GlobalScope.launch {
        MasterNode().run()
    }
    val jobs: MutableList<Job> = mutableListOf(masterNode)
    for (i in 0..6) {
        delay(100)
        jobs += GlobalScope.launch { Node(7000 + i).run() }
    }
    val job2 = GlobalScope.launch { delay(5000) }
    job2.join()
    println("done ${jobs.size}")
    joinAll(*jobs.toTypedArray())
}