#!/bin/bash
# 1st param is the path to genprog for java
# 2nd param is the path to the junit jars on your machine
# claire note to self: junit jars on her machine are: /Applications/eclipse/external-jars/
#Example of how to run it:
#./prepareSimpleExample.sh /home/mau/Research/genprog4java

# Does the compile script build the test files?

PATHTOGENPROG="$1"
JUNITJARS="$PATHTOGENPROG"/lib

PATHTOSIMPLEEXAMPLE=`pwd`

if [ ! -d bin/ ] ; then
    mkdir bin
fi

javac -d bin/ src/packageSimpleExample/SimpleExample.java 
javac -classpath $JUNITJARS/junit-4.12.jar:$JUNITJARS/hamcrest-core-1.3.jar:bin/ -sourcepath src/tests/*java -d bin/ src/tests/*java
#rm -rf bin/packageSimpleExample/

#PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file 
FILE=./simpleExample.config
/bin/cat <<EOM >$FILE
javaVM = /usr/bin/java
popsize = 20
seed = 0
classTestFolder = bin/
workingDir = $PATHTOSIMPLEEXAMPLE/
outputDir = $PATHTOSIMPLEEXAMPLE/tmp/
libs = $PATHTOGENPROG/lib/junit-4.12.jar:$PATHTOGENPROG/lib/junittestrunner.jar:$JUNITJARS/hamcrest-core-1.3.jar:$PATHTOSIMPLEEXAMPLE/bin/
sanity = yes
regenPaths
sourceDir = src/
positiveTests = $PATHTOSIMPLEEXAMPLE/pos.tests
negativeTests = $PATHTOSIMPLEEXAMPLE/neg.tests
jacocoPath = $PATHTOGENPROG/lib/jacocoagent.jar
classSourceFolder = bin/
targetClassName = packageSimpleExample.SimpleExample
EOM
