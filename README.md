# README #

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

### Who do I talk to? ###

Repo owner or admin

* Claire Le Goues (clegoues at cs dot cmu dot edu )

* Mauricio Soto (mauriciosoto at cmu dot edu)
