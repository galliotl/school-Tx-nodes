//fichier fiche.java version sans capture
package projet

import projet.ligne.lecLigne
import java.io.IOException

//attention au chemin dans CLASSPATH
//import corejava.*;
//CLASSE MERE sans modificateur d'accs:
//n'est accessible que dans le package
open class fiche {
    private var pnom: StringBuffer
    private val pt_suivant: fiche? = null

    //CONSTRUCTEURS
    constructor() {
        pnom = StringBuffer(80)
    }

    constructor(pfi: fiche) {
        pnom = StringBuffer(80)
        ecrit_pnom(pfi.lit_pnom().toString())
    }

    //ACCESSEUR LECTURE
    fun lit_pnom(): StringBuffer {
        return pnom
    }

    //ACCESSEUR ECRITURE
    fun ecrit_pnom(pcar: String?) {
        pnom.setLength(0)
        pnom.append(pcar)
    }

    //METHODE
    open fun imprime() { //virtual inutile en JAVA
        println(lit_pnom().toString() + "<-f\n")
    }

    @Throws(IOException::class)
    open fun saisie() {
        println("entrer un nom:")
        ecrit_pnom(lecLigne())
    }
} //FIN CLASSE fiche

internal class fiche_plus : fiche //toujours public en JAVA
{
    private var pnom_plus: StringBuffer

    constructor() : super() //appel du constructeur de la classe mere
    //CONSTRUCTEUR
    {
        pnom_plus = StringBuffer(80)
    }

    constructor(pfi: fiche_plus) : super(pfi) //APPEL DU CONSTRUCTEUR DE LA CLASSE MERE
    //CONSTRUCTEUR
    {
        pnom_plus = StringBuffer(80)
        ecrit_pnom_plus(pfi.lit_pnom_plus().toString())
    }

    //ACCESSEUR LECTURE
    fun lit_pnom_plus(): StringBuffer {
        return pnom_plus
    }

    fun ecrit_pnom_plus(pcar: String?) {
        pnom_plus.setLength(0)
        pnom_plus.append(pcar)
    }

    //METHODE
    override fun imprime() {
        super.imprime()
        println(lit_pnom_plus().toString() + "<-f+\n")
    }

    @Throws(IOException::class)
    override fun saisie() //SIGNALE QUE LA METHODE PEUT LANCER UNE IOExeption
//a declarer dans l'en tete car il n'y a pas de capture
    {
        super.saisie()
        println("entrer un nom_plus:")
        ecrit_pnom_plus(lecLigne())
    }
}