//fichier Menu.java
package projet

import java.io.IOException

//UTILISATION:creation d'un menu de 3 choix avec un titre et
//Menu menu1 = new Menu(3)
//Class paermettant la gestion d'un menu
class Menu {
    //proprietes
    var title //titre du menu
            : StringBuffer? = null
    var lines: Array<StringBuffer?>
    var subTitle //titre pour quitter le menu defaut: q- pour quitter
            : StringBuffer? = null
    var choice //titre de la commande a saisir
            : StringBuffer? = null
    var nbLines = 10 //par defaut

    //constructeurs
    constructor() {
        lines = arrayOfNulls(nbLines) //allocation du tableau de ligne de choix
        Title("Menu")
        SubTitle("q- pour quitter") //par defaut
        Choice("choix:") //par defaut
    }

    constructor(newNbLine: Int) {
        lines = arrayOfNulls(newNbLine) //allocation du tableau de ligne de choix
        nbLines = newNbLine
        Title("Menu")
        SubTitle("q- pour quitter") //par defaut
        Choice("choix:") //par defaut
    }

    //Accesseurs
    @Throws(IOException::class)
    fun Title(newTitle: String?) { //titre du menu
        title = StringBuffer(newTitle)
    }

    fun Lines(index: Int, newLine: String?) { //rempli la ligne du menu correspondante
        lines[index] = StringBuffer(newLine)
    }

    fun SubTitle(newSubTitle: String?) {
        subTitle = StringBuffer(newSubTitle)
    }

    fun Choice(newChoice: String?) {
        choice = StringBuffer(newChoice)
    }

    //methodes
    @Throws(IOException::class)
    fun Print() { //affichage du menu
        val souligne = StringBuffer(30)
        for (i in 0 until title!!.length) souligne.append("-")
        //affichage du titre
        println(title)
        println(souligne)
        println()
        //affichage des lignes
        for (i in 0 until nbLines) {
            if (lines[i] != null) {
                print("$i-")
                println(lines[i])
            }
        }
        //affichage du titre pour la sortie
        println(subTitle)
        println()
        //affichage du titre de saisie
        print(choice)
    }

    @Throws(IOException::class)
    fun Choix(): Int { //affiche le menu et retourne le choix
        val cmd: String
        var choix: Int
        var done = false
        Print() //affichage du menu
        cmd = ligne.lecLigne() //lecture de la frappe
        choix = try {
            if (cmd.compareTo("q") == 0 || cmd.compareTo("Q") == 0) -1 else ligne.ToInt(cmd)
        } catch (e: NumberFormatException) {
            -2 //si on ne frappe ni 'q' ni 'Q' ni un nombre
        }
        while (!done) {
            if (choix < nbLines) done = true else choix = -2
        }
        return choix
    }
} //fin de la classe Menu
