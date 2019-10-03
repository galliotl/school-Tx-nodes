package utt.tx

import java.io.Serializable

class Message(val msg: String, val type: String, val senderIp: String, val senderPort: kotlin.Int = 7777): Serializable