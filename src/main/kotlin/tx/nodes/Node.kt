package tx.nodes


class Node internal constructor() {
    /**
     * number of nodes
     */
    var numberOfNodes // number of nodes
            = 0
    /**
     * the array that holds the keys value.
     */
    var key: IntArray // the array that holds the keys value.
    /**
     * the array that holds the references of the keys in the node.
     */
    var children: Array<Node?> // the array that holds the references of the keys in the node.
    /**
     * the variable to deterimed if the node is is Leaf or not.
     */
    var isLeaf // the variable to deterime if the node is is Leaf or not.
            : Boolean

    /**
     * The constructor of the node class
     * The node can have at most 3 keys. We have 4 references for each node, and assign the node to be isLeaf.
     */
    init {
        key = IntArray(3) // The node can have at most 3 keys
        children = arrayOfNulls(4) // We have 4 references for each node
        isLeaf = true // assign the node to be Leaf.
    }
}