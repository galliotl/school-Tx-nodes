//fichier arbre.java
package projet

import tx.nodes.Sommet
import tx.nodes.SommetNullException
import java.io.IOException

//les imports
//Les Exception propre a la classe
internal class SommetNotExistException : Exception()

internal class TableauPleinException :
    Exception()

internal class TableauVideException : Exception()
internal class CaseTableauOccupeeException :
    Exception()  //##############

//classe Arbre #
//##############
class Arbre {
    //----------
//proprietes
//----------
    private var racine: Int
    private var ts //reference sur le tableu de sommets
            : Array<Sommet?>
    private var libre: FileInt
    private var nbMax = 0
    private var nbSommet = 0

    //--------------------------------------------------------------------------
//-------------
//constructeurs
//-------------
    constructor() { //par defaut
        racine = -1
        ts = arrayOfNulls(10) //creation du tableau de sommet
        libre = FileInt(10)
        NbMax(10)
        NbSommet(0)
        for (i in 0..9) {
            libre.`in`(i) //initialisation de la file de sommet libre
            ts[i] = Sommet() //initialisation du tableau de sommet
        }
    }

    //-------------------------------------------------------------------------
    constructor(nb: Int) { //permet de donner le nombre de sommet maxi de l'arbre
        racine = -1
        ts = arrayOfNulls(nb)
        libre = FileInt(nb)
        NbMax(nb)
        NbSommet(0)
        for (i in 0 until nb) {
            libre.`in`(i) //initialisation de la file de sommet libre
            ts[i] = Sommet() //initialisation du tableau de sommet
        }
    }

    //--------------------------------------------------------------------------
//----------
//accesseurs
//----------
    protected fun Racine(newRacine: Int) {
        racine = newRacine
    }

    fun Racine(): Int {
        return racine
    }

    //--------------------------------------------------------------------------
//retourne le sommet correspondant a l'indice
    @Throws(SommetNullException::class)
    fun Ts(indice: Int): Sommet? {
        if (indice == -1) throw SommetNullException()
        return if (ts[indice] == null) throw SommetNullException() else if (ts[indice]!!.Robjet() == null) throw SommetNullException() else ts[indice]
    }

    //retourne l'indice correspondant au Sommet s'il existe
    fun Ts(aSommet: Sommet?): Int {
        for (i in 0 until NbMax()) {
            try {
                if (Ts(i)!!.egal(aSommet)) return i
            } catch (e: SommetNullException) {
                continue
            }
        }
        return -1
    }

    //--------------------------------------------------------------------------
//ajoute le sommet dans le tableau a un emplacement libre
    @Throws(TableauPleinException::class, CaseTableauOccupeeException::class)
    protected fun addTs(newSommet: Sommet?): Int {
        var indice = -1
        if (NbSommet() == NbMax() || libre.out().also({ indice = it }) == -1) { //le tableau est plein
            println("Arbre plein")
            throw TableauPleinException() //exception de tableau plein
        } else {
            try {
                if (Ts(indice)!!.Robjet() != null) {
                    println("tentative de remplacer un sommet existant")
                    throw CaseTableauOccupeeException()
                }
            } catch (e: SommetNullException) {
                ts[indice] = newSommet
                NbSommet(NbSommet() + 1)
                //inserer la fonction d'insertion dans l'arbre et d'equilibrage
                return indice //on retourne l'indice ou a ete mis le sommet
            }
        }
        return indice
    }

    //--------------------------------------------------------------------------
    protected fun NbMax(newNbMax: Int) {
        nbMax = newNbMax
    }

    protected fun NbMax(): Int {
        return nbMax
    }

    //--------------------------------------------------------------------------
    protected fun NbSommet(): Int {
        return nbSommet
    }

    protected fun NbSommet(newNbSommet: Int) {
        nbSommet = newNbSommet
    }

    //--------------------------------------------------------------------------
//--------
//methodes
//--------
//affichage
    fun print() {
        println("------Caracteristique de l'arbre------")
        println("Indice de la racine : " + Racine())
        println("Nombre de place(s) libre(s) : " + (NbMax() - NbSommet()))
        println("|----Contenu du tableau de Sommet----|")
        for (i in 0 until NbMax()) {
            try {
                if (Ts(i)!!.Robjet() != null) {
                    Ts(i)!!.print(i)
                } //System.out.println("ts["+i+"]=null")
            } catch (e: SommetNullException) {
            }
        }
        println("------------Fin de l'arbre------------")
        try {
            ligne.lecLigne()
        } catch (e: IOException) {
        }
    }

    //affichage different parcours symetrique de l'arbre
    fun printArbre(start: Int) {
        val filsDroit: Int
        val filsGauche: Int
        val currentSommet: Sommet?
        if (start != -1) {
            try {
                currentSommet = Ts(start)
                filsDroit = currentSommet!!.FilsDroit()
                filsGauche = currentSommet.FilsGauche()
                currentSommet.print2()
                printArbre(filsDroit)
                printArbre(filsGauche)
            } catch (e: SommetNullException) {
            }
        }
    }

    //affichage des cles de chaque sommet avec parcours symetrique en fonction du niveau
    fun printArborescence(aSommet: Sommet?, k: Int) {
        if (aSommet != null) { //PARCOURS A GAUCHE
            try {
                printArborescence(Ts(aSommet.FilsDroit()), k + 1)
            } catch (e: SommetNullException) {
                printArborescence(null, k + 1)
            }
            //affichage des espaces correspondant au niveau de profondeur
            for (i in 1 until k) print("   ")
            //impression de la cle du sommet
            aSommet.printKey()
            //parcours a droite
            try {
                printArborescence(Ts(aSommet.FilsGauche()), k + 1)
            } catch (e: SommetNullException) {
                printArborescence(null, k + 1)
            }
        }
    }

    //--------------------------------------------------------------------------
//methode retournant le pere du sommet ou -1 si c'est la racine
    protected fun getPere(aSommet: Sommet?): Int {
        var indicePere = -1
        val indiceSommet: Int
        indiceSommet = Ts(aSommet)
        for (i in 0 until NbMax()) {
            try {
                if (Ts(i)!!.Robjet() != null) {
                    if (Ts(i)!!.FilsGauche() == indiceSommet) indicePere = i else {
                        if (Ts(i)!!.FilsDroit() == indiceSommet) indicePere = i
                    }
                }
            } catch (e: SommetNullException) {
            }
        }
        return indicePere
    }

    //--------------------------------------------------------------------------
//recherche d'un sommet
    fun recherche(key: Int): Int {
        var indice = Racine()
        val sommetTemp = Sommet()
        sommetTemp.Key(key)
        var ok = false //flag de sortie de la boucle
        while (!ok && indice != -1) {
            try { //si la cle du sommetTemp est inferieure au sommet parcours a gauche
                if (Ts(indice)!!.superieur(sommetTemp)) indice = Ts(indice)!!.FilsGauche() else if (Ts(indice)!!.egal(
                        sommetTemp
                    )
                ) ok = true else indice = Ts(indice)!!.FilsDroit()
            } catch (e: SommetNullException) {
                println("tentative de comparaison avec un sommet null!!!")
            }
        }
        //on retourne l'indice
        return indice
    }

    //--------------------------------------------------------------------------
//insertion d'un sommet en maintenant l'arbre trie
    @Throws(TableauPleinException::class)
    fun insertion(newSommet: Sommet): Int {
        var indicePosition = 0
        val indiceLibre = 0
        var indicePere = Racine()
        var indiceFils = Racine()
        //y a t il encore de la place dans le tableau?
        try {
            indicePosition = addTs(newSommet)
        } catch (e: TableauPleinException) { //non le tableau est plein!!!!
            println("Impossible d'ajouter le sommet le tableau est plein!!!")
            throw e
        } catch (e: CaseTableauOccupeeException) { //erreur de traitement on essai d'effacer un sommet existant
        }
        //si pas de racine ce sommet devient racine
        if (Racine() == -1) {
            Racine(indicePosition)
        } else { //on cherche ou le nouveau sommet va etre mis, o cherche l'indice du pere
//pour pouvoir inserer l'indice dedans
            indiceFils = Racine() //on part de la racine
            while (indiceFils != -1) {
                indicePere = indiceFils
                //on compare la cle du nouveau sommet avec celles de l'arbre
//pour trouver sa place
                try {
                    indiceFils =
                        if (newSommet.superieur(Ts(indicePere))) Ts(indicePere)!!.FilsDroit() else Ts(indicePere)!!.FilsGauche()
                } catch (e: SommetNullException) { //on essai d'utiliser un sommet inexistant
                    println("Le sommet en cours n'existe pas")
                }
            }
            //on a trouver le pere qui se trouve a indicePere
//ou met on notre fils?
            try {
                if (newSommet.superieur(Ts(indicePere))) Ts(indicePere)!!.FilsDroit(indicePosition) else Ts(indicePere)!!.FilsGauche(
                    indicePosition
                )
            } catch (e: SommetNullException) { //on essai d'utiliser un sommet inexistant
                println("Le sommet en cours n'existe pas")
            }
        }
        //on retourne l'indice du nouveau sommet
        return indicePosition
    }

    //--------------------------------------------------------------------------
//suppression d'un sommet dans l'arbre en conservant le tri
    @Throws(TableauVideException::class, SommetNotExistException::class)
    fun supprimer(indiceSommet: Int) {
        var indiceDroit: Int
        val indiceGauche: Int
        val indicePere: Int
        val aSommet: Sommet?
        //si indiceSommet egal -1, le sommet n'existe pas
        if (Racine() == -1) throw TableauVideException() else {
            if (indiceSommet == -1) throw SommetNotExistException() else { //recuperation du sommet
                aSommet = try {
                    Ts(indiceSommet)
                } catch (e: SommetNullException) {
                    throw SommetNotExistException()
                }
                //on met indiceSommet dans la file libre
                libre.`in`(indiceSommet)
                //on recupere les indice des fils droit et gauche du sommet
                indiceDroit = aSommet!!.FilsDroit()
                indiceGauche = aSommet.FilsGauche()
                //si le sommet etait la racine
                if (indiceSommet == Racine()) {
                    if (indiceDroit == -1) //la racine n'a pas de fils droit
                        if (indiceGauche == -1) //la racine n'a qu'un fils gauche, il devient la racine
                            Racine(indiceGauche) else  //la racine n'avait pas de fils
                            Racine(-1) else { //la racine a un fils droit
                        Racine(indiceDroit)
                        //et on met le fils gauche du fils de la racine a gauche du dernier
//element gauche de l'arbre commencant par la nouvelle racine
                        try {
                            while (Ts(indiceDroit)!!.FilsGauche() != -1) indiceDroit = Ts(indiceDroit)!!.FilsGauche()
                            Ts(indiceDroit)!!.FilsGauche(indiceGauche)
                        } catch (e: SommetNullException) {
                        }
                    }
                } else {
                    try { //le sommet n'est pas la racine, on recherche alors le pere du sommet
                        indicePere = getPere(aSommet)
                        //le sommet est le fils droit de son pere
                        if (Ts(indicePere)!!.FilsDroit() == indiceSommet) //on met le fils droit du sommet a sa place au niveau du pere
                            Ts(indicePere)!!.FilsDroit(indiceDroit) else if (Ts(indicePere)!!.FilsGauche() == indiceSommet) //on met le fils droit du sommet a sa place au niveau du pere
                            Ts(indicePere)!!.FilsGauche(indiceDroit) else println("le sommet a supprime n'est pas la racine et n'a pas de pere")
                        //s'il n'y a pas de sommet droit on met le sommet gauche a la place du
//sommet a supprime
                        if (indiceDroit == -1) if (Ts(indicePere)!!.FilsDroit() == indiceSommet) Ts(indicePere)!!.FilsDroit(
                            indiceGauche
                        ) else Ts(indicePere)!!.FilsGauche(indiceGauche) else { //sinon on met le fils gauche du sommet a supprime le plus a gauche du
//fils droit
                            while (Ts(indiceDroit)!!.FilsGauche() != -1) indiceDroit = Ts(indiceDroit)!!.FilsGauche()
                            Ts(indiceDroit)!!.FilsGauche(indiceGauche)
                        }
                    } catch (e: SommetNullException) {
                        println("Tentative d'utilise un sommet inexistant!!")
                    }
                }
                //on reinitialise le sommet supprime
//on reinitialise la reference de l'objet
                aSommet.Robjet(null)
                aSommet.FilsDroit(-1)
                aSommet.FilsGauche(-1)
                NbSommet(NbSommet() - 1)
            }
        }
    } //--------------------------------------------------------------------------
} //fin de la classe Arbre
