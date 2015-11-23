#!/bin/bash
# 1st param is the path to the SimpleExample folder
# 2nd param is the path to genprog for java

#Example of how to run it:
#./prepareSimpleExample.sh /home/mau/Research/genprog4java


PATHTOGENPROG="$1"
PATHTOSIMPLEEXAMPLE=$PATHTOGENPROG"/SimpleExample"

#Compile
cd $PATHTOSIMPLEEXAMPLE/
ant -buildfile buildSimpleExample.xml

#Create jar file
cd $PATHTOSIMPLEEXAMPLE/bin/
jar cf $PATHTOSIMPLEEXAMPLE/SimpleExample.jar ./* 


#Create config file
cd $PATHTOSIMPLEEXAMPLE

PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file 
FILE="$PATHTOSIMPLEEXAMPLE"/simpleExample.config
/bin/cat <<EOM >$FILE
popsize = 20
seed = 0
testsDir = tests/
javaVM = /usr/bin/java
workingDir = $PATHTOSIMPLEEXAMPLE/
outputDir = $PATHTOSIMPLEEXAMPLE/tmp/
libs = $PATHTOSIMPLEEXAMPLE/SimpleExample.jar:$PATHTOGENPROG/lib/junit-4.10.jar:$PATHTOGENPROG/lib/junittestrunner.jar
sanity = yes
regenPaths
sourceDir = src/
positiveTests = $PATHTOSIMPLEEXAMPLE/pos.tests
negativeTests = $PATHTOSIMPLEEXAMPLE/neg.tests
jacocoPath = $PATHTOGENPROG/lib/jacocoagent.jar
classSourceFolder = target/classes
targetClassName = packageSimpleExample.SimpleExample
EOM





