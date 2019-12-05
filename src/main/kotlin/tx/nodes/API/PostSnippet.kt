package tx.nodes.API

import java.io.Serializable

/**
 * Imposes a request like this :
 * {
 *  data: {
 *      text: "dsdfddf"
 *  }
 * }
 */
data class PostSnippet(val data: Text): Serializable {
    data class Text(val text: String): Serializable
}