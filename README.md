# Tx-nodes

## run cmd

In order to run the code in the cmd line, you have to compile all the kotlin file in the src directory preferably in one jar file that we'll call Main.jar

```code
kotlinc *.kt -include-runtime -d main.jar
```

Then you'll have to run the main.jar using

```code
java -jar main.jar
```