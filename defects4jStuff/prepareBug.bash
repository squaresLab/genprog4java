#!/bin/bash
# 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)

#copy these files to the source control
cd ~/Research/
#cp 1prepareBug.bash ./genprog4java/defects4jStuff/
#cp 2prepareBug.bash ./genprog4java/defects4jStuff/
cp prepareBug.bash ./genprog4java/defects4jStuff/
cp -r ./defects4j/ExamplesCheckedOut/Utilities/ ./genprog4java/defects4jStuff/


#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE="${1,,}"

#Specific variables per every project
#JAVADIR is the working directory of the project
if [ $LOWERCASEPACKAGE = "chart" ]; then
  TESTSDIR=tests.org.jfree
  WD=source
  JAVADIR=org/jfree
  CONFIGLIBS="/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/chartAllSourceClasses.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/chartAllTestClasses.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar"

elif [ $LOWERCASEPACKAGE = "closure" ]; then
  TESTSDIR=test.com.google
  WD=src
  JAVADIR=com/google
  CONFIGLIBS="/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/closureAllSourceClasses.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/closureAllTestClasses.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant-launcher.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/args4j.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/caja-r4314.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/guava.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jarjar.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/json.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jsr305.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/junit.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/protobuf-java.jar"

elif [ $LOWERCASEPACKAGE = "lang" ]; then
  TESTSDIR=src.test.java.org.apache.commons.lang3
  WD=src/main/java
  JAVADIR=org/apache/commons/lang3 
  CONFIGLIBS="/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/langAllSourceClasses.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/langAllTestClasses.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/projects/Lang/lib/easymock.jar:/home/mau/Research/defects4j/projects/Lang/lib/asm.jar:/home/mau/Research/defects4j/projects/Lang/lib/cglib.jar:/home/mau/Research/defects4j/projects/Lang/lib/org/easymock/easymock/easymock-2.5.2.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/lang1Buggy/easymock-3.3.1.jar"

cp /home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/EntityArrays.java /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/src/main/java/org/apache/commons/lang3/text/translate/

elif [ $LOWERCASEPACKAGE = "math" ]; then 
  TESTSDIR=src.test.java.org.apache.commons.math3
  WD=src/main/java
  JAVADIR=org/apache/commons/math3
  CONFIGLIBS="/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/mathAllSourceClasses.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/mathAllTestClasses.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/projects/Math/lib/commons-discovery-0.5.jar"

elif [ $LOWERCASEPACKAGE = "time" ]; then
  TESTSDIR=src.test.java.org.joda.time
  WD=src/main/java
  JAVADIR=org/joda/time
  CONFIGLIBS="/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/timeAllSourceClasses.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/timeAllTestClasses.jar:/home/mau/Research/defects4j/framework/projects/Time/lib/joda-convert-1.2.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/framework/projects/lib/easymock-3.3.1.jar "

fi

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:~/Research/defects4j/framework/bin

#Print info about the project
#defects4j info -p $1

#Print info about this bug in particular
#defects4j info -p $1 -v $2

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$2"b -w ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

#Go to the created folder
cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

#Compile the buggy code
defects4j compile

#Run the buggy code
#defects4j test

#Create the file with all the tests names in a file
#find $JAVADIR/ -name "*.java" | tr / . | rev | cut -c 6- | rev  &> ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/pos.tests 

#copy the standard list of all tests to the current bug directory
cp /home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/"$LOWERCASEPACKAGE"Pos.tests /home/mau/Research/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

#cd /home/mau/Research/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/

#create new list of Passing tests 
#touch /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/passingTests.tests

#go through all the entries of the file and the test passes insert it in the passing list (sanitize out of scope tests)
#while read e; do
#  echo $e
#java -cp .:/home/mau/Research/defects4j/ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllSourceClasses.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllTestClasses.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/#junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/#Research/defects4j/framework/projects/lib/junit-4.11.jar org.junit.runner.JUnitCore $e

#if the test passes, added to the passing list
#if [ $? -eq 0 ]
#then
#    echo $e >> /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/#passingTests.tests
#fi
#done < pos.tests

#replace the list with all the tests, with the one with just the passing tests
#mv /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/passingTests.tests /home/mau/Research/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests



#Create a file neg.tests
touch /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/neg.tests

cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file TODO:#FIX THIS FILE
FILE=~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/configDefects4j
/bin/cat <<EOM >$FILE
packageName = $PACKAGEDIR
targetClassName = NameOfTheTargetFileWithNoExtension
sourceDir = $JAVADIR
popsize = 5
seed = 0
testsDir = $TESTSDIR
javaVM = /usr/bin/java
workingDir = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD
outputDir = ./tmp
libs = $CONFIGLIBS
classDir = bin/
sanity = yes
regenPaths
positiveTests = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = /home/mau/Research/defects4j/framework/projects/lib/jacocoagent.jar
EOM

#info about the bug
defects4j info -p $1 -v $2

#Need to modify these three files
gedit ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/configDefects4j
gedit /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/neg.tests 
#gedit /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/pos.tests 


#PASSSINGTESTS=/home/mau/Research/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

#if [[ -s $PASSSINGTESTS ]] ; then
#echo "Passing tests file has data, all good :D"
#else
#echo "ERROR!!! $PASSSINGTESTS is empty, means that all unit tests failed, so the file of the positive tests at $PASSSINGTESTS is empty. ERROR!!!"
#fi ;


#I then go to pos.tests, move the failing tests that appear in the "Root cause in triggering tests" in the console, to the neg.tests
echo Yo Mau, 
echo 1. Go to ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy, insert the failing tests that appear in the \"Root cause in triggering tests\" in the console, to the neg.tests
echo 2. Now go to the config file in ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/configDefects4j and change the first three lines with the data in "List of modified sources"
echo 3. Copy paste the working directory from the config file to eclipse.








