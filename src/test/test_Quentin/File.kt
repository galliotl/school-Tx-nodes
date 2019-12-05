//file.java version sans capture
package projet

import java.io.IOException

class file(n: Int) {
    var debut = 0
    var fin = 0
    private var nb_fiche = 0
    private val nbmax //NOMBRE DE FICHES PRESENTES, nombre max
            : Int
    private val tab: Array<fiche?> //il existe plusieurs critures
    //METHODES
    @Throws(IOException::class)
    fun `in`(): Int {
        return if (nb_fiche == nbmax) 0 else {
            if (nb_fiche == 0) {
                debut = 0
                fin = 0
            } else fin = (fin + 1) % nbmax
            tab[fin] = fiche()
            tab[fin]?.saisie()
            nb_fiche++
            1
        }
    } //fin in();

    fun out(): fiche? {
        return if (nb_fiche == 0) null else {
            nb_fiche--
            val deb = debut
            if (nb_fiche == 0) {
                fin = 0
                debut = fin
            } else debut = (debut + 1) % nbmax
            tab[deb]
        }
    }

    fun imprime() { //IMPRESSION NOMRE DE FICHE
        println("nb_fiche=$nb_fiche")
        println("debut=$debut")
        println("fin=$fin")
        var nb = 1
        var k = debut
        if (nb_fiche != 0) while (nb <= nb_fiche) {
            tab[k]?.imprime()
            nb++
            k = (k + 1) % nbmax
        } else {
            println("file vide \n")
            println("tab[1]=")
            tab[1]?.imprime()
        }
    }

    //CONSTRUCTEUR
    init {
        tab = arrayOfNulls<fiche>(n)
        nbmax = n
    }
}