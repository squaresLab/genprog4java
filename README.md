# README #

Note that the wiki has rather more information than this README: https://github.com/squaresLab/genprog4java/wiki

### Disclaimer ###

GenProg4Java is in active early development and changes a lot.

### What is this repository for? ###

This is a java-based version of GenProg, software for automatically fixing bugs.

* https://squareslab.github.io/genprog-code/

### Running GenProg4J ###

At a high level, GenProg takes as input a program with a bug and test cases.  At
least one test case should be failing; it encodes the bug to be repaired.  At
least one should be passing; it encodes desired functionality that should be
maintained.

At sea level, GenProg4Java takes a configuration file as input.  Below, we provide a 
minimal set that should allow G4J to run using the default genetic programming 
heuristic on a buggy program taken from the IntroClassJava dataset 
(https://github.com/Spirals-Team/IntroClassJava) other options are available 
(comments below are explanatory; I'm not sure  they work in actual config files...).  
You can see what flags are available (and their defaults, when applicable) in
clegoues.genprog4java.main.Configuration.  However, to illustrate:

```
javaVM = /usr/bin/java 
classTestFolder = bin/ # where to find test classes
workingDir = /path/to/IntroClassJava/dataset/median/testmedian/000/ #top level directory
outputDir = /path/to/IntroClassJava/dataset/median/testmedian/000/tmp/ #where temporary files should be stored
libs=/path/to/genprog4java/lib/hamcrest-core-1.3.jar:/path/to/genprog4java/lib/junit-4.12.jar:/path/to/genprog4java/lib/junittestrunner.jar: # to run the program under repair
sourceDir = src/main/java # relative to workingDir; path to top of package-level source for class under repair
positiveTests = /path/to/IntroClassJava/dataset/median/testmedian/000/pos.tests # file listing test classes or methods that should initially pass, one per line
negativeTests = /path/to/IntroClassJava/dataset/median/testmedian/000/neg.tests # file listing test classes or methods that should initially fail, one per line
jacocoPath = /path/to/genprog4java/lib/jacocoagent.jar # path to jacoco; g4j ships with a jar that should work
testClassPath=/path/to/IntroClassJava/dataset/median/testmedian/000/bin/ #classpath for tests, possibly redundant with classTestFolder, I should check
targetClassName = introclassJava.median_d4aae191_000 # class under repair, or file listing classes under repair, one per line
```

Although the other options are, well, optional, you probably want to set a large
number of them, like seed.

Individual classes also have individual configure methods; you can search for
them to find more options.  It is on The List to make this less ridiculous.

In addition to the configuration file, you should specify log4j.properties to
the VM; my VM arguments in my run configuration in Eclipse looks like:

-ea -Dlog4j.configuration=file:${workspace_loc:GenProg4Java}/src/log4j.properties 

I also set the working directory to be the base directory for the project I'm
trying to repair.  Thus, for the Hello, World! example, the Working Directory is
set as: 

${workspace_loc:GenProg4Java/tests/mathTest}

### Hello, World! ###

genprog4java/tests/mathTest serves as a reasonable Hello, World! example.  It
has a miniTestConfiguration that should have flags close to what's necessary to
get it to run, though we do change this file with some regularity so if it
doesn't work out of the gate, double check that it doesn't look completely
wacky.

### Who do I talk to? ###

Repo owner or admin

* Claire Le Goues (clegoues at cs dot cmu dot edu )

* Mauricio Soto (mauriciosoto at cmu dot edu)
