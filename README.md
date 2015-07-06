# README #

### What is this repository for? ###

* Quick summary
This is a java-based version of GenProg, a software for automatically fixing bugs.

* [Learn Markdown] 
http://dijkstra.cs.virginia.edu/genprog/


### Integration with Defects4j ###

We've setup two scripts for integrating defects4j with genprog.
The main script is the one that prepares the bug to be run. This script is called prepareBug.sh and it is located in genprog4j/defects4JStuff/prepareBug.sh

The overall functionality of the script is to set up everything so that genprog can run on the bug taken from defects4j with the parameters specified by the user.

The scripts takes the following parameters:

* 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
* 2nd param is the bug number (ex: 1,2,3,4,...)
* 3rd param is the folder where the project is (ex: "/home/mau/Research/" )
* 4td param is the folder where defects4j is installed (ex: "/home/mau/Research/defects4j/" )
* 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)

So a typical run would look like this:
./prepareBug.sh Math 2 /home/mau/Research/ /home/mau/Research/defects4j/ allHuman

1st param: Defects4j has five projects: Lang, Chart, Closure, Math, Time, you can choose whichever you like. Lang and Math have worked without any issue so far. The other three projects, we are still working on them.

2nd param: bug number of the selected project.

3rd param: where did you download the bitbucket project.

4th param: where is defects4j folder located.

5th param: there are three ways to run this script: allHuman for running genprog with all the human made tests. oneHuman: to run it with just one of the human made tests. oneGenerated: to run it with just one of the generated tests.

It starts by setting up the different paths per every one of the different projects (Lang, Chart, Closure, Math, Time).
Then uses defects4j's scripts to checkout the buggy and fixed version of the code of the bug indicated in the parameters.
Then it compiles the checkedout versions.

It then makes the difference from the last parameter as explained before.

If it is running the Lang project then it copies a modified file called EntityArrays.java to the working directory. This is because this file contains a lot of non ascii characters in the comments that were causing issues with compilation.

Then it creates a new test suite different from the one that comes with the project using evosuite (integrataed with defects4j), this is because we need two different test suites to run the experiment.

It then compiles all the .java files, and then creates a jar file with all the .class files created from compiling the .java files. This is so genprog can run the different variations without having to compile the whole thing, but just changing one java file and then it takes all the rest of the source from this jar.

Every genprog run needs a config file. This is what it does next. It creates a text file with the configuration needed to be ran by genprog.

Finally it prints out the information about the bug taken from defects4j and opens two files that need to be edited with the info displayed from the bug: neg.tests and configDefects4j
It dispalys some instructions on how to edit these two files with examples.


The second script is still under construction.
It is called runGenProgForBug.sh and its overall purpose is to be able to run genprog on a certain defects4j bug with certain different seeds until it it able to either find a patch or get a to a certain number of generations or wallclock time.

It takes in two parameters:

*  1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
*  2nd param is the bug number (ex: 1,2,3,4,...)

Same as the one before, first parameter is to specify the project from defects4j and the second one is to specify the bug from that particular project.

Starts off by setting up some directories and then it creates a loop from 0 - 20 with increments of 2 to set up the seeds that genprog will use to generate the variations. This is just a particular way of setting up the seeds, we just need seed variety, and way of running it would work.

Then it creates a configuration file everytime that it runs a new configuration with a different seed, and runs it.


### Who do I talk to? ###

Repo owner or admin
* Claire Le Goues (clegoues at cs dot cmu dot edu )
* Mauricio Soto (mauriciosoto at cmu dot edu)