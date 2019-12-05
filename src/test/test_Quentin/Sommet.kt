//fichier Sommet.java
package tx.nodes

import projet.ligne
import java.io.IOException

//les imports
//import ligne;
//Les Exception propre a la classe
internal class SommetNullException : Exception()  //##################

//la classe Sommet #
//##################
class Sommet {
    //----------
//proprietes
//----------
    private var robjet //reference vers un objet
            : Any?
    private var key = 0
    private var filsGauche: Int
    private var filsDroit: Int

    //-------------
//constructeurs
//-------------
    constructor() {
        robjet = null
        filsGauche = -1
        filsDroit = -1
    }

    constructor(newRobjet: Any?) {
        robjet = newRobjet
        filsGauche = -1
        filsDroit = -1
    }

    //----------
//accesseurs
//----------
    fun Robjet(newRobjet: Any?) {
        robjet = newRobjet
    }

    fun Robjet(): Any? {
        return robjet
    }

    //--------------------------------------------------------------------------
    fun Key(newKey: Int) {
        key = newKey
    }

    fun Key(): Int {
        return key
    }

    //--------------------------------------------------------------------------
    fun FilsGauche(aInt: Int) {
        filsGauche = aInt
    }

    fun FilsGauche(): Int {
        return filsGauche
    }

    //--------------------------------------------------------------------------
    fun FilsDroit(aInt: Int) {
        filsDroit = aInt
    }

    fun FilsDroit(): Int {
        return filsDroit
    }

    //--------------------------------------------------------------------------
//--------------------------------------------------------------------------
//--------
//methodes
//--------
    fun print() {
        println("---Debut du Sommet----")
        println("l'objet : " + Robjet())
        println("la cle : " + Key())
        println("indice fils gauche:" + FilsGauche())
        println("indice fils droit:" + FilsDroit())
        println("----Fin du Sommet-----")
    }

    //--------------------------------------------------------------------------
    fun print(indice: Int) {
        println("---Debut du $indice ieme Sommet----")
        println("l'objet : " + Robjet())
        println("la cle : " + Key())
        println("indice fils gauche:" + FilsGauche())
        println("indice fils droit:" + FilsDroit())
        println("------------Fin du Sommet------------")
    }

    fun print2() {
        print("l'objet : " + Robjet())
        print(";la cle : " + Key())
        print(";indice fils gauche:" + FilsGauche())
        println(";indice fils droit:" + FilsDroit())
    }

    fun printKey() {
        println("[" + Key() + "]")
    }

    //--------------------------------------------------------------------------
//saisie de la cle
    fun saisieKey() {
        var key = 0
        var saisie: String
        var ok = true
        while (ok) {
            print("Entrez la cle du sommet:")
            try {
                saisie = ligne.lecLigne()
                key = ligne.ToInt(saisie)
                ok = false
                Key(key)
            } catch (e: NumberFormatException) {
                ok = true //recommence la saisie
            } catch (e: IOException) {
            }
        }
    }

    //saisie l'objet
    fun saisieObjet() {
        val key = 0
        var saisie: String
        var ok = true
        while (ok) {
            print("Entrez la chaine de caracteres:")
            try {
                saisie = ligne.lecLigne()
                ok = false
                Robjet(saisie)
            } catch (e: IOException) {
            }
        }
    }

    //--------------------------------------------------------------------------
    @Throws(SommetNullException::class)
    fun superieur(aSommet: Sommet?): Boolean {
        return if (aSommet != null) {
            if (Key() > aSommet.Key()) true else false
        } else throw SommetNullException()
    }

    //--------------------------------------------------------------------------
    @Throws(SommetNullException::class)
    fun inferieur(aSommet: Sommet?): Boolean {
        return if (aSommet != null) {
            if (Key() < aSommet.Key()) true else false
        } else throw SommetNullException()
    }

    //--------------------------------------------------------------------------
    @Throws(SommetNullException::class)
    fun egal(aSommet: Sommet?): Boolean {
        return if (aSommet != null) {
            if (Key() == aSommet.Key()) true else false
        } else throw SommetNullException()
    }

    //--------------------------------------------------------------------------
    @Throws(SommetNullException::class)
    fun different(aSommet: Sommet?): Boolean {
        return if (aSommet != null) {
            if (Key() != aSommet.Key()) true else false
        } else throw SommetNullException()
    }

    //--------------------------------------------------------------------------
    @Throws(SommetNullException::class)
    fun superieurOuEgal(aSommet: Sommet?): Boolean {
        return if (aSommet != null) {
            if (Key() >= aSommet.Key()) true else false
        } else throw SommetNullException()
    }

    //--------------------------------------------------------------------------
    @Throws(SommetNullException::class)
    fun inferieurOuEgal(aSommet: Sommet?): Boolean {
        return if (aSommet != null) {
            if (Key() <= aSommet.Key()) true else false
        } else throw SommetNullException()
    }
} //fin de la classe Sommet
