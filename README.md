# tx-nodes

## Run

Il est possible de lancer le programme avec gradle grâce à la commande suivante:

```bash
gradle run
```

Cela nous permet de ne pas être dépendant d'un IDE et de pouvoir run depuis n'importe quel IDE. Il est aussi possible d'installer facilement pour kotlin n'importe quelle dependencies.

## Connexion entre Nodes

Le master node se crée, 

- il écoute sur un port TCP pour la connexion avec les autres node
- il lance un serveur HTTP pour la communication avec un client externe

Ensuite un node esclave se connecte,

- il écoute sur un port pour la connection avec les autres node
- il crée un socket vers le master node


- le master node accepte la connexion et store l'informaton dans sa map