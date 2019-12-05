//ligne.java
//sans capture d'exception
package projet

import java.io.IOException

//import corejava.*;
object ligne {
    //renvoie un String pointant sur le StringBuffer r
//la lecture clavier se fait caractre par caractre,
//le rsultat est stock dans r
    @Throws(IOException::class)
    fun lecLigne(): String {
        val r = StringBuffer(80)
        var ch: Int
        r.setLength(0)
        var done = false
        while (!done) {
            ch = System.`in`.read()
            if (ch < 0 || ch.toChar() == '\n') done =
                true else if (ch.toChar() != '\r') r.append(ch.toChar())
        }
        return String(r)
    }

    @Throws(IOException::class, NumberFormatException::class)
    fun lecInt(): Int {
        while (true) {
            return Integer.valueOf(lecLigne().trim { it <= ' ' }).toInt()
        }
    }

    fun ToInt(aString: String): Int {
        return Integer.valueOf(aString.trim { it <= ' ' }).toInt()
    }
} //fin de la classe ligne
