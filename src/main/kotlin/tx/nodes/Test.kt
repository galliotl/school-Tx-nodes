package tx.nodes

import java.util.*

/**
 * A test for the [btree.Btree] implementation.
 */
object Test {
    init {
        start()
    }
    fun start() {
        val `in` = Scanner(System.`in`)
        val bTree = Btree()
        try {
            while (true) {
                print(
                    "Welcome to the B Tree implementation\n"
                            + "1) to insert a number to the B Tree.\n"
                            + "2) to delete a number from the B Tree.\n"
                            + "3) to search the B Tree.\n"
                            + "4) to print the B Tree.\n"
                            + "Note: 0 (zero) will be assumed Null"
                            + "Enter any other number to exit.\n"
                            + "Choose:\n"
                )
                var choose: Int
                var key: Int
                choose = `in`.nextInt()
                when (choose) {
                    1 -> {
                        print("Enter the number to insert in the B tree: ")
                        bTree.insert(`in`.nextInt())
                    }
                    2 -> {
                        print("Enter the number to delete from the B tree: ")
                        bTree.delete(`in`.nextInt())
                    }
                    3 -> {
                        print("Enter the number to search the B tree for: ")
                        key = `in`.nextInt()
                        if (bTree.search(key)) {
                            println("$key is founded")
                        } else {
                            println("$key is NOT founded")
                        }
                    }
                    4 -> {
                        println(
                            "-------\n"
                                    + "Printing The B Tree\n"
                        )
                        bTree.print()
                    }
                    0 -> System.exit(0)
                    else -> true
                }
                println("-----------------------")
            }
        } catch (e: InputMismatchException) {
            println("Accept only numbers... \n Exiting....")
        }
    }
}