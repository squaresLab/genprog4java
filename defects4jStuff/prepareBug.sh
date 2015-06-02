#!/bin/bash
# 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the project is (ex: "/home/mau/Research/" )
# 4td param is the folder where defects4j is installed (ex: "/home/mau/Research/defects4j/" )
# 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)

#Mau runs it like this:
#./prepareBug.sh Math 2 /home/mau/Research/ /home/mau/Research/defects4j/ allHuman

# in case it helps, in my machine, I Have:
# /home/mau/Research/genprog4j where the source code for genprog is
# /home/mau/Research/defects4j where the defects4j source code is
# /home/mau/Research/defects4j/ExamplesCheckedOut where every time that I check out a bug from defects4j, it goes here


#copy these files to the source control
#cd "$3"
#cp prepareBug.sh ./genprog4java/defects4jStuff/
#cp -r ./genprog4java/defects4jStuff/Utilities/ ./defects4j/ExamplesCheckedOut/


#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE="${1,,}"

#Specific variables per every project
#JAVADIR is the working directory of the project
if [ $LOWERCASEPACKAGE = "chart" ]; then
  TESTSDIR=tests.org.jfree
  WD=source
  JAVADIR=org/jfree
  CONFIGLIBS="$2/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/chartAllSourceClasses.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/chartAllTestClasses.jar:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"genprog4java/tests/mathTest/lib/junit-4.10.jar"

elif [ $LOWERCASEPACKAGE = "closure" ]; then
  TESTSDIR=test.com.google
  WD=src
  JAVADIR=com/google
  CONFIGLIBS=""$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/closureAllSourceClasses.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/closureAllTestClasses.jar:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"genprog4java/tests/mathTest/lib/junit-4.10.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant-launcher.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/args4j.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/caja-r4314.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/guava.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jarjar.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/json.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jsr305.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/junit.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/protobuf-java.jar"

elif [ $LOWERCASEPACKAGE = "lang" ]; then
  TESTSDIR=src.test.java.org.apache.commons.lang3
  WD=src/main/java
  JAVADIR=org/apache/commons/lang3 
  CONFIGLIBS=""$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/langAllSourceClasses.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/langAllTestClasses.jar:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"defects4j/framework/projects/lib/junit-4.11.jar:"$3"defects4j/projects/Lang/lib/easymock.jar:"$3"defects4j/projects/Lang/lib/asm.jar:"$3"defects4j/projects/Lang/lib/cglib.jar:"$3"defects4j/framework/projects/lib/easymock-3.3.1.jar"
  LIBSTESTS="-cp \".:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/"$LOWERCASEPACKAGE"AllSourceClasses.jar:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"defects4j/framework/projects/lib/junit-4.11.jar:"$3"defects4j/projects/Lang/lib/easymock.jar:"$3"defects4j/framework/projects/lib/easymock-3.3.1.jar\" "


elif [ $LOWERCASEPACKAGE = "math" ]; then 
  TESTSDIR=src.test.java.org.apache.commons.math3
  WD=src/main/java
  JAVADIR=org/apache/commons/math3
  CONFIGLIBS=""$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/mathAllSourceClasses.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/mathAllTestClasses.jar:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"genprog4java/tests/mathTest/lib/junit-4.10.jar:"$3"defects4j/projects/Math/lib/commons-discovery-0.5.jar"
  LIBSTESTS="-cp \".:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/"$LOWERCASEPACKAGE"AllSourceClasses.jar:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"genprog4java/tests/mathTest/lib/junit-4.10.jar:"$3"defects4j/projects/Math/lib/commons-discovery-0.5.jar\" "

elif [ $LOWERCASEPACKAGE = "time" ]; then
  TESTSDIR=src.test.java.org.joda.time
  WD=src/main/java
  JAVADIR=org/joda/time
  CONFIGLIBS=""$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/timeAllSourceClasses.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/timeAllTestClasses.jar:"$3"defects4j/framework/projects/Time/lib/joda-convert-1.2.jar:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"genprog4java/tests/mathTest/lib/junit-4.10.jar:"$3"defects4j/framework/projects/lib/easymock-3.3.1.jar "

fi


#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$4"framework/bin

#Print info about the project
#defects4j info -p $1

#Print info about this bug in particular
#defects4j info -p $1 -v $2

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$2"b -w "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

#Checkout the fixed version of the code to make the seccond test suite
defects4j checkout -p $1 -v "$2"f -w "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed

#Go to the created folder
cd "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed

#Compile the buggy code
defects4j compile

#Go to the created folder
cd "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

#Compile the buggy code
defects4j compile

#Run the buggy code
#defects4j test

#Create the file with all the tests names in a file
#find $JAVADIR/ -name "*.java" | tr / . | rev | cut -c 6- | rev  &> ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/pos.tests 

if [ $5 = "allHuman" ]; then
  #copy the standard list of all tests to the current bug directory
  cp "$3"defects4j/ExamplesCheckedOut/Utilities/"$LOWERCASEPACKAGE"Pos.tests "$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests
elif [ $5 = "oneHuman" ]; then
  echo write in this file: "$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests, the human made test in the bug info
  gedit "$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests
elif [ $5 = "oneGenerated" ]; then
  echo write in this file: "$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests, the generated test called NAMEOFTHETARGETFILEEvoSuite_Branch.java
  gedit "$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests
fi

#for the lang project copy a fixed file
if [ $LOWERCASEPACKAGE = "lang" ]; then
cp "$3"genprog4java/defects4jStuff/Utilities/EntityArrays.java "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/main/java/org/apache/commons/lang3/text/translate/
fi

#cd "$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/

#create new list of Passing tests 
#touch "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/passingTests.tests

#go through all the entries of the file and the test passes insert it in the passing list (sanitize out of scope tests)
#while read e; do
#  echo $e
#java -cp .:"$3"defects4j/ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllSourceClasses.jar:"$3"defects4j/ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllTestClasses.jar:"$3"genprog4java/tests/mathTest/lib/#junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/#Research/defects4j/framework/projects/lib/junit-4.11.jar org.junit.runner.JUnitCore $e

#if the test passes, added to the passing list
#if [ $? -eq 0 ]
#then
#    echo $e >> "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/#passingTests.tests
#fi
#done < pos.tests

#replace the list with all the tests, with the one with just the passing tests
#mv "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/passingTests.tests "$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests








if [ $LOWERCASEPACKAGE = "lang" ]; then
SOURCES="org/apache/commons/lang3/"
elif [ $LOWERCASEPACKAGE = "math" ]; then
SOURCES="org/apache/commons/math3/"
fi

#Create the new test suite
echo Creating new test suite...
"$4"framework/bin/run_evosuite.pl -p $1 -v "$2"f -n 1 -o "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/test/java/outputOfEvoSuite/ -c branch => 100s

#Untar the generated test into the tests folder
cd "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/test/java/
tar xvjf outputOfEvoSuite/$1/evosuite-branch/1/"$1"-"$2"f-evosuite-branch.1.tar.bz2

#Go to the bug folder
cd "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD/

#create file to run compilation
FILENAME=sources.txt
exec 3<>$FILENAME
# Write to file
#echo $LIBSMAIN >&3
find -name "*.java" >&3
exec 3>&-

#Compile the project
javac @sources.txt

echo Compilation of main java classes successful

rm sources.txt

#cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/main/java

#where the .class files are
#DIROFCLASSFILES=org/$JAVADIR




#Jar all the .class's
#TODO maybe: change this to insert only the class files recursively, NOT the .java files also. Same thing in tests
jar cf ../../../"$LOWERCASEPACKAGE"AllSourceClasses.jar "$SOURCES"* 
#$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 

echo Jar of source files created successfully.


#--------------------------------

#Compile test classes
cd "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/test/java/

FILENAME=sources.txt
exec 3<>$FILENAME
# Write to file
echo $LIBSTESTS >&3
find -name "*.java" >&3
exec 3>&-

javac @sources.txt

echo Compilation of test java classes successful
#rm sources.txt

#javac *.java */*.java */*/*.java */*/*/*.java */*/*/*/*.java -Xlint:unchecked

#cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/test/java



#Jar all the test class's
jar cf ../../../"$LOWERCASEPACKAGE"AllTestClasses.jar "$SOURCES"* 
#$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 

echo Jar of tests created successfully.












#Create a file neg.tests
touch "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/neg.tests

cd "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file TODO:#FIX THIS FILE
FILE="$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/configDefects4j
/bin/cat <<EOM >$FILE
packageName = $PACKAGEDIR
targetClassName = NameOfTheTargetFileWithNoExtension
sourceDir = $JAVADIR
popsize = 5
seed = 0
testsDir = $TESTSDIR
javaVM = /usr/bin/java
workingDir = $3defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD
outputDir = ./tmp
libs = $CONFIGLIBS
classDir = bin/
sanity = yes
regenPaths
positiveTests = $3defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $3defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $4framework/projects/lib/jacocoagent.jar
EOM

#info about the bug
defects4j info -p $1 -v $2

#Need to modify these three files
gedit "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/configDefects4j
gedit "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/neg.tests 
#gedit "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/pos.tests 


#PASSSINGTESTS="$3"defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

#if [[ -s $PASSSINGTESTS ]] ; then
#echo "Passing tests file has data, all good :D"
#else
#echo "ERROR!!! $PASSSINGTESTS is empty, means that all unit tests failed, so the file of the positive tests at $PASSSINGTESTS is empty. ERROR!!!"
#fi ;


#I then go to pos.tests, move the failing tests that appear in the "Root cause in triggering tests" in the console, to the neg.tests
echo Dear user: 
echo 1. I have created and opened a file called neg.tests in this directory: "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy, please insert the package of the failing tests that appear in the \"Root cause in triggering tests\" above in this console, and copy that into the file that I have opened.
echo Example: org.apache.commons.math3.distribution.HypergeometricDistributionTest
echo 2. Now I have created and opened a second file called configDefects4j in this location: "$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/ . Please go to that file and change the first three lines with the data in the section "List of modified sources" above in this console.
echo In the package name, add the package where the target file is. In the sourceDir, do the same but with slashes instead of periods. And in the targetClassName, insert the name of the class without the extension of the file.
echo For example:
echo packageName = org.apache.commons.math3.distribution
echo targetClassName = HypergeometricDistribution
echo sourceDir = org/apache/commons/math3/distribution
echo 3. If running on Eclipse, copy paste the working directory from the config file into the working directory in the configuration of eclipse.









