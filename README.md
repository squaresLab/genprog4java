# README #

### Disclaimer ###

GenProg4Java is in active early development and changes a lot.

### What is this repository for? ###

* Quick summary
This is a java-based version of GenProg, a software for automatically fixing bugs.

* http://dijkstra.cs.virginia.edu/genprog/

### Running GenProg4J ###

At a high level, GenProg takes as input a program with a bug and test cases.  At
least one test case should be failing; it encodes the bug to be repaired.  At
least one should be passing; it encodes desired functionality that should be
maintained.

At sea level, GenProg4Java takes a configuration file as input.  You can see
what flags are available (and their defaults, when applicable) in
clegoues.genprog4java.main.Configuration

You must, at minimum, provide:

* javaVM - path to java

* libs - classpath to compile the project

* targetClassName - fully-qualified class name (with package), no .java at the
end, OR a .txt file with a list of such class names (when multiple files are to
be considered for repair), one per line

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

### Integration with Defects4j ###

There is one main script for integrating defects4J with GenProg.  Located in Genprog4Java/defects4j-scripts

The first thing this script does, is to call a second script to prepared the bug execution called prepareBug.sh.

prepareBug.sh sets up a defect for a repair attempt, including a complete GenProg4Java config file. 

The script creates a directory in the defects4j folder called ExamplesCheckedOut, in which it checks out the buggy and fixed versions of the project/bug number you specify.  It compiles them both. 

If specified, the script calls evosuite to generate test suites.

It then queries defects4j to determine the positive and negative test files, creates a compile script, and generates a config for GenProg; byproducts (pos.tests,neg.tests, and defects4j.config) appear in the ProjectBugNumBuggy/ folder. 

To run GenProg manually with this config file, the working directory should be the checked-out buggy version
(e.g., /Users/clegoues/research/defects4j/ExamplesCheckedOut/lang1Buggy/), and specify the config file to the main GenProg4Java executable.  (e.g., /Users/clegoues/research/defects4j/ExamplesCheckedOut/lang1Buggy/defects4j.config).  Pass the log4j config to the VM (-ea -Dlog4j.configuration=file:${workspace_loc:GenProg4Java}/src/log4j.properties).  CLG tends to run from within Eclipse, but you can do what you like.

runGenProgForBug.sh´s overall purpose is to be able to run genprog on a certain defects4j bug with certain different seeds until it it able to either find a patch or get a to a certain number of generations or wall clock time.

It takes the following parameters:

* 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
* 2nd param is the bug number (ex: 1,2,3,4,...)
* 3rd param is the folder where the project is (ex: "/home/mau/Research/" )
* 4td param is the folder where defects4j is installed (ex: "/home/mau/Research/defects4j/" )
* 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)
* 6th param is the percentage of test cases being used to guide genprog's search (ex: 1, 100)

Starts off by setting up some directories and then it creates a loop from 0 - 20 with increments of 2 to set up the seeds that genprog will use to generate the variations. This is just a particular way of setting up the seeds, we just need seed variety, and way of running it would work.

Then it creates a configuration file everytime that it runs a new configuration with a different seed, and runs it.

Finally it saves the folder with all the variations into a tar file per each of the seeds.

### Who do I talk to? ###

Repo owner or admin

* Claire Le Goues (clegoues at cs dot cmu dot edu )

* Mauricio Soto (mauriciosoto at cmu dot edu)