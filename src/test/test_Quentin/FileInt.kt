//fileInt.java version sans capture
package projet

//import corejava.*;
//attention au chemin dans CLASSPATH
//import source.*;
class FileInt(n: Int) {
    var debut = 0
    var fin = 0
    private var nb_fiche = 0
    private val nbmax //NOMBRE DE FICHES PRESENTES, nombre max
            : Int
    private val tab: IntArray //il existe plusieurs ecritures
    //METHODES
    fun `in`(aInt: Int): Int {
        return if (nb_fiche == nbmax) 0 else {
            if (nb_fiche == 0) {
                debut = 0
                fin = 0
            } else fin = (fin + 1) % nbmax
            tab[fin] = aInt
            nb_fiche++
            1
        }
    } //fin in();

    fun out(): Int {
        return if (nb_fiche == 0) -1 else {
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
            println(tab[k])
            nb++
            k = (k + 1) % nbmax
        } else {
            println("file vide \n")
            //                System.out.println( "tab[1]=");
//                System.out.println(tab[1]);
        }
    }

    //CONSTRUCTEUR
    init {
        tab = IntArray(n)
        nbmax = n
    }
}