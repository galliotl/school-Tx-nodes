//source ArbreMain.java
//compilation: javac file.java fiche.java Menu.java FileInt.java ligne.java Sommet.java ArbreMain.java 
package tx.nodes

import projet.*
import projet.SommetNotExistException
import projet.TableauPleinException
import projet.TableauVideException
import projet.ligne.ToInt
import projet.ligne.lecLigne
import java.io.IOException

object ArbreMain {
    var arbreNotExist = true //permet de savoir si l'arbre a deja ete cree
    init {
        Launch()
    }
    fun Launch() {
        var choix: Int
        val nbSommet = 0
        var cmd: String
        var ok = true
        val menu1 = Menu()
        var monArbre = Arbre(0) //arbre vide
        menu1.Title("ARBRE DE RECHERCHE")
        menu1.Lines(0, "Creation d'un arbre de recherche")
        menu1.Lines(1, "Ajouter un sommet a l'arbre")
        menu1.Lines(2, "Supprimer un sommet")
        menu1.Lines(3, "Rechercher un sommet")
        menu1.Lines(4, "Imprimer l'arborescence de l'arbre")
        menu1.Lines(5, "Imprimer l'arbre lineairement")
        menu1.SubTitle("q- Au revoir")
        do {
            println("") //on saute une ligne avant d'afficher le menu
            choix = menu1.Choix()
            when (choix) {
                0 -> monArbre = createArbre(monArbre)
                1 -> addSommet(monArbre)
                2 -> delSommet(monArbre)
                3 -> recherche(monArbre)
                4 -> {
                    if (ok) {
                        try {
                            monArbre.printArborescence(monArbre.Ts(monArbre.Racine()), 1)
                        } catch (e: SommetNullException) {
                        }
                    } else println("L'arbre n'existe pas")
                    try {
                        lecLigne()
                    } catch (e: IOException) {
                    }
                }
                5 -> {
                    if (ok) monArbre.print() else println("L'arbre n'existe pas")
                    try {
                        lecLigne()
                    } catch (e: IOException) {
                    }
                }
                -2 -> println("Mauvaise saisie, Recommencez")
                -1 -> {
                    println("Au revoir")
                    ok = false
                }
                else -> println("Choix incoherent")
            }
        } while (ok)
    }

    //--------------------------------------------------------------------------
//Creation de l'arbre
    fun createArbre(newArbre: Arbre?): Arbre {
        var newArbre = newArbre
        var ok = true
        var cmd: String
        var nbSommet = 0
        if (arbreNotExist) while (ok) {
            print("Entrez le nombre de sommets a creer:")
            try {
                cmd = lecLigne()
                nbSommet = ToInt(cmd)
                ok = false
            } catch (e: NumberFormatException) {
                ok = true //si le nombre entrer est incoherent
            } catch (e: IOException) {
            }
        } else {
            println("L'arbre existe deja!!!")
            try {
                lecLigne()
            } catch (e: IOException) {
            }
        }
        arbreNotExist = false
        return Arbre(nbSommet).also { newArbre = it }
    }

    //--------------------------------------------------------------------------
//Ajouter un sommet a l'arbre
    fun addSommet(anArbre: Arbre) {
        val indice: Int
        val newSommet: Sommet
        if (arbreNotExist) {
            println("ARBRE NON INITIALISE!!!!!")
            try {
                lecLigne()
            } catch (e: IOException) {
            }
        } else {
            try {
                newSommet = Sommet()
                newSommet.saisieKey()
                newSommet.saisieObjet()
                indice = anArbre.recherche(newSommet.Key())
                if (indice == -1) anArbre.insertion(newSommet) else {
                    println("la cle existe deja, veuillez en choisir une autre")
                    try {
                        lecLigne()
                    } catch (e: IOException) {
                    }
                }
            } catch (f: TableauPleinException) {
                println("ARBRE PLEIN!!!!!")
                try {
                    lecLigne()
                } catch (e: IOException) {
                }
            }
        }
    }

    //--------------------------------------------------------------------------
//supprimer un sommet
    fun delSommet(anArbre: Arbre) {
        var saisie: String
        var ok = true
        var key = -1
        var indice: Int
        if (arbreNotExist) {
            println("ARBRE NON INITIALISE!!!!!")
            try {
                lecLigne()
            } catch (e: IOException) {
            }
        } else {
            while (ok) {
                print("Entrez la cle du sommet:")
                try {
                    saisie = lecLigne()
                    key = ToInt(saisie)
                    ok = false
                } catch (e: NumberFormatException) {
                    ok = true //recommence la saisie
                } catch (e: IOException) {
                }
            }
            try {
                anArbre.supprimer(anArbre.recherche(key))
            } catch (e: TableauVideException) {
                println("Le tableau est VIDE!!!")
                try {
                    lecLigne()
                } catch (f: IOException) {
                }
            } catch (e: SommetNotExistException) {
                println("Le sommet n'existe pas!!!")
                try {
                    lecLigne()
                } catch (f: IOException) {
                }
            }
        }
    }

    //--------------------------------------------------------------------------
//recherche d'un sommet
    fun recherche(anArbre: Arbre) {
        val indice: Int
        var key = -1
        var aSommet: Sommet
        var saisie: String
        var ok = true
        while (ok) {
            print("Entrez la cle a recherche:")
            try {
                saisie = lecLigne()
                ok = false
                key = ToInt(saisie)
            } catch (e: IOException) {
                ok = true
            } catch (e: NumberFormatException) {
                ok = true
            }
        }
        indice = anArbre.recherche(key)
        if (indice == -1) {
            println("")
            println("La cle est introuvable dans l'arbre")
        } else {
            println("")
            println("Bingo le sommet est:")
            try {
                anArbre.Ts(indice)!!.print()
            } catch (e: SommetNullException) {
            }
        }
    }
} //fin de la classe testmain
