#!/bin/bash
# 1st param is the path to genprog for java
# 2nd param is the path to the junit jars on your machine
# claire note to self: junit jars on her machine are: /Applications/eclipse/external-jars/
#Example of how to run it:
#./prepareOffByOne.sh /home/mau/Research/genprog4java

# Does the compile script build the test files?

PATHTOGENPROG="$1"
JUNITJARS="$2"

PATHTOOFFBYONE=`pwd`

if [[ ! -d bin/ ]] ; then
    mkdir bin
fi

javac -d bin/ src/packageOffByOne/OffByOne.java 
javac -classpath $JUNITJARS/junit.jar:$JUNITJARS/hamcrest-core-1.3.jar:bin/ -sourcepath src/tests/*java -d bin/ src/tests/*java
rm -rf bin/packageOffByOne/

PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file 
FILE=./offByOne.config
/bin/cat <<EOM >$FILE
javaVM = /usr/bin/java
popsize = 20
seed = 0
classTestFolder = bin/
workingDir = $PATHTOOFFBYONE/
outputDir = $PATHTOOFFBYONE/tmp/
libs = $PATHTOGENPROG/lib/junit-4.10.jar:$PATHTOGENPROG/lib/junittestrunner.jar
sanity = yes
regenPaths
sourceDir = src/
positiveTests = $PATHTOOFFBYONE/pos.tests
negativeTests = $PATHTOOFFBYONE/neg.tests
jacocoPath = $PATHTOGENPROG/lib/jacocoagent.jar
classSourceFolder = bin/
targetClassName = packageOffByOne.OffByOne
EOM
