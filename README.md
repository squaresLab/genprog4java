# README #

### What is this repository for? ###

* Quick summary
This is a java-based version of GenProg, a software for automatically fixing bugs.

* [Learn Markdown] 
http://dijkstra.cs.virginia.edu/genprog/


### Integration with Defects4j ###

We've setup two scripts for integrating defects4j with genprog.
The main script is the one that prepares the bug to be run. This script is called prepareBug.sh and it is located in genprog4j/defects4JStuff/prepareBug.sh
The scripts takes the following parameters:

* 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
* 2nd param is the bug number (ex: 1,2,3,4,...)
* 3rd param is the folder where the project is (ex: "/home/mau/Research/" )
* 4td param is the folder where defects4j is installed (ex: "/home/mau/Research/defects4j/" )
* 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)

So a typical run would look like this:
./prepareBug.sh Math 2 /home/mau/Research/ /home/mau/Research/defects4j/ allHuman

It starts by setting up the different paths per every one of the different projects (Lang, Chart, Closure, Math, Time).
Then uses defects4j's scripts to checkout the buggy and fixed version of the code of the bug indicated in the parameters.
Then it compiles the checkedout versions.

It then makes the difference from the last parameter which goes as follows:
allHuman:
oneHuman:
oneGenerated:

If it is running the Lang project then it copies a modified file called EntityArrays.java to the working directory. This is because this file contains a lot of non ascii characters in the comments that were causing issues with compilation.

Then it creates a new test suite different from the one that comes with the project using evosuite (integrataed with defects4j), this is because we need two different test suites to run the experiment.

It then compiles all the .java files, and then creates a jar file with all the .class files created from compiling the .java files. This is so genprog can run the different variations without having to compile the whole thing, but just changing one java file and then it takes all the rest of the source from this jar.

Every genprog run needs a config file. This is what it does next. It creates a text file with the configuration needed to be ran by genprog.

Finally it prints out the information about the bug taken from defects4j and opens two files that need to be edited with the info displayed from the bug: neg.tests and configDefects4j
It dispalys some instructions on how to edit these two files with examples.







### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact