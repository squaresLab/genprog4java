#!/bin/bash
# 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: "/home/mau/Research/genprog4java/" )
# 4td param is the folder where defects4j is installed (ex: "/home/mau/Research/defects4j/" )
# 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)

#Mau runs it like this:
#./prepareBug.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ allHuman

# in case it helps, in my machine, I Have:
# /home/mau/Research/genprog4j where the source code for genprog is
# /home/mau/Research/defects4j where the defects4j source code is
# /home/mau/Research/defects4j/ExamplesCheckedOut where every time that I check out a bug from defects4j, it goes here


# CLG thinks it's nice practice to rename the vars taken from the user to
# something more readable that corresponds to how they're used.  Makes the
# script easier to read.
PACKAGE="$1"
BUG="$2"
GENPROG="$3"
DEFECTS4J="$4"
OPTION="$5"

PARENTDIR=$DEFECTS4J"/ExamplesCheckedOut"

#copy these files to the source control

mkdir -p $PARENTDIR
cp -r $GENPROG"/defects4jStuff/Utilities" $PARENTDIR

#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
# CLG changed the way you did this (which was fine for Bash 4!) so it's a bit more platform-independent
LOWERCASEPACKAGE=`echo $PACKAGE | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGPRE=$PARENTDIR"/"$LOWERCASEPACKAGE"$BUG"Buggy

#Specific variables per every project
#TESTWD is the address from the root to the address where JAVADIR starts, for the TEST files 
#WD is the address from the root to the address where JAVADIR starts,  for the SOURCE files 
#JAVADIR is the address from the WD or TESTWD, to the address where all the java files are for both source and test files 
#It is usually used TESTWD/JAVADIR or WD/JAVADIR
#CONFIGLIBS are the libraries to be included in the configuration file so that GenProg can run it.
#LIBSTESTS are the libraries needed to compile the  tests (dependencies of the project)
#LIBSMAIN are the libraries needed to compile the project (dependencies of the project)

SRCJAR=$BUGPRE"/"$LOWERCASEPACKAGE"AllSourceClasses.jar"
TESTJAR=$BUGPRE"/"$LOWERCASEPACKAGE"AllTestClasses.jar"

# Common genprog libs: junit test runner and the like

GENLIBS=$GENPROG"/lib/junittestrunner.jar:"$GENPROG"/lib/commons-io-1.4.jar:"$GENPROG"/lib/junit-4.10.jar"

# all libs for a package need at least the source jar, test jar, and generic genprog libs
CONFIGLIBS=$SRCJAR":"$TESTJAR":"$GENLIBS


if [ $LOWERCASEPACKAGE = "chart" ]; then
  TESTWD=tests
  WD=source
  JAVADIR=org/jfree
  CHARTLIBS=$BUGPRE"/lib/itext-2.0.6.jar:"$BUGPRE"/lib/servlet.jar:"$BUGPRE"/lib/junit.jar"

  CONFIGLIBS=$CONFIGLIBS":"$CHARTLIBS
  # CLG wonders if the escaped quote is necessary?  OH I know why!
  # FIXME check if we need a space after the classpath where these are
  # eventually used
  LIBSTESTS="-cp \".:"$SRCJAR":"$GENLIBS":"$CHARTLIBS\"
  LIBSMAIN="-cp \".:"$CHARTLIBS\"

elif [ $LOWERCASEPACKAGE = "closure" ]; then
  TESTWD=test
  WD=src
  JAVADIR=com/google
  CONFIGLIBS=$BUGPRE"/closureAllSourceClasses.jar:"$BUGPRE"/closureAllTestClasses.jar:"$GENPROG"/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$GENPROG/lib/junit-4.10.jar:$BUGPRE/lib/ant.jar:$BUGPRE/lib/ant-launcher.jar:$BUGPRE/lib/args4j.jar:$BUGPRE/lib/caja-r4314.jar:$BUGPRE/lib/guava.jar:$BUGPRE/lib/jarjar.jar:$BUGPRE/lib/json.jar:$BUGPRE/lib/jsr305.jar:$BUGPRE/lib/junit.jar:$BUGPRE/lib/protobuf-java.jar"
  LIBSTESTS="-cp \".:$BUGPRE/closureAllSourceClasses.jar:"$GENPROG/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$GENPROG/lib/junit-4.10.jar:$BUGPRE/lib/ant.jar:$BUGPRE/lib/ant-launcher.jar:$BUGPRE/lib/args4j.jar:$BUGPRE/lib/caja-r4314.jar:$BUGPRE/lib/guava.jar:$BUGPRE/lib/jarjar.jar:$BUGPRE/lib/json.jar:$BUGPRE/lib/jsr305.jar:$BUGPRE/lib/junit.jar:$BUGPRE/lib/protobuf-java.jar:$BUGPRE/build/lib/rhino.jar\" "
  LIBSMAIN="-cp \".:$BUGPRE/lib/ant.jar:$BUGPRE/lib/ant-launcher.jar:$BUGPRE/lib/args4j.jar:$BUGPRE/lib/caja-r4313.jar:$BUGPRE/lib/guava.jar:$BUGPRE/lib/jarjar.jar:$BUGPRE/lib/json.jar:$BUGPRE/lib/jsr305.jar:$BUGPRE/lib/junit.jar:$BUGPRE/lib/protobuf-java.jar:$BUGPRE/build/lib/rhino.jar\" "

elif [ $LOWERCASEPACKAGE = "lang" ]; then
  TESTWD=src/test/java
  WD=src/main/java
  JAVADIR=org/apache/commons/lang3 
  CONFIGLIBS="$BUGPRE/langAllSourceClasses.jar:$BUGPRE/langAllTestClasses.jar:"$GENPROG/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$4"framework/projects/lib/junit-4.11.jar:"$4"projects/Lang/lib/easymock.jar:"$4"projects/Lang/lib/asm.jar:"$4"projects/Lang/lib/cglib.jar:"$4"framework/projects/lib/easymock-3.3.1.jar"
  LIBSTESTS="-cp \".:"$4"ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/"$LOWERCASEPACKAGE"AllSourceClasses.jar:"$GENPROG/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$4"framework/projects/lib/junit-4.11.jar:"$4"projects/Lang/lib/easymock.jar:"$4"framework/projects/lib/easymock-3.3.1.jar\" "
  LIBSMAIN=""

elif [ $LOWERCASEPACKAGE = "math" ]; then 
  TESTWD=src/test/java
  WD=src/main/java
  JAVADIR=org/apache/commons/math3
  CONFIGLIBS=""$4"ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/mathAllSourceClasses.jar:$BUGPRE/mathAllTestClasses.jar:"$GENPROG/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$GENPROG/lib/junit-4.10.jar:"$4"framework/projects/Math/lib/commons-discovery-0.5.jar"
  LIBSTESTS="-cp \".:"$4"ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/mathAllSourceClasses.jar:"$GENPROG/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$GENPROG/lib/junit-4.10.jar:"$4"framework/projects/Math/lib/commons-discovery-0.5.jar\" "
  LIBSMAIN=""

elif [ $LOWERCASEPACKAGE = "time" ]; then
  TESTWD=src/test/java
  WD=src/main/java
  JAVADIR=org/joda/time
  CONFIGLIBS="$BUGPRE/timeAllSourceClasses.jar:$BUGPRE/timeAllTestClasses.jar:"$4"framework/projects/Time/lib/joda-convert-1.2.jar:"$GENPROG/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$GENPROG/lib/junit-4.10.jar:"$4"framework/projects/lib/easymock-3.3.1.jar"
  LIBSTESTS="-cp \".:$BUGPRE/timeAllSourceClasses.jar:"$4"framework/projects/Time/lib/joda-convert-1.2.jar:"$GENPROG/lib/junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:"$GENPROG/lib/junit-4.10.jar:"$4"framework/projects/lib/easymock-3.3.1.jar\" "
  LIBSMAIN="-cp \".:"$4"framework/projects/Time/lib/joda-convert-1.2.jar\" "

fi


#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$4"framework/bin

#Print info about the project
#defects4j info -p $1

#Print info about this bug in particular
#defects4j info -p $1 -v $2

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$2"b -w $BUGPRE

#Checkout the fixed version of the code to make the seccond test suite
defects4j checkout -p $1 -v "$2"f -w "$4"ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed

#Go to the created folder
cd "$4"ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed

#Compile the buggy code
defects4j compile

#Go to the created folder
cd $BUGPRE

#Compile the buggy code
defects4j compile

#Run the buggy code
#defects4j test

#Create the file with all the tests names in a file
#find $JAVADIR/ -name "*.java" | tr / . | rev | cut -c 6- | rev  &> ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/pos.tests 

if [ "$5" = "allHuman" ]; then
  #copy the standard list of all tests to the current bug directory
  cp "$4"ExamplesCheckedOut/Utilities/"$LOWERCASEPACKAGE"Pos.tests "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests
elif [ "$5" = "oneHuman" ]; then
  echo write in this file: "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests, the human made test in the bug info
  gedit "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests
elif [ "$5" = "oneGenerated" ]; then
  echo write in this file: "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests, the generated test called NAMEOFTHETARGETFILEEvoSuite_Branch.java
  gedit "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests
fi

#for the lang project copy a fixed file
if [ $LOWERCASEPACKAGE = "lang" ]; then
cp "$3"defects4jStuff/Utilities/EntityArrays.java $BUGPRE/src/main/java/org/apache/commons/lang3/text/translate/
fi

#cd "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/

#create new list of Passing tests 
#touch $BUGPRE/passingTests.tests

#go through all the entries of the file and the test passes insert it in the passing list (sanitize out of scope tests)
#while read e; do
#  echo $e
#java -cp .:"$4"ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllSourceClasses.jar:"$4"ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllTestClasses.jar:"$GENPROG/lib/#junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:/home/mau/#Research/defects4j/framework/projects/lib/junit-4.11.jar org.junit.runner.JUnitCore $e

#if the test passes, added to the passing list
#if [ $? -eq 0 ]
#then
#    echo $e >> $BUGPRE/#passingTests.tests
#fi
#done < pos.tests

#replace the list with all the tests, with the one with just the passing tests
#mv $BUGPRE/passingTests.tests "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests





#UNCOMMENT!!!!!!!!!
#Create the new test suite
#echo Creating new test suite...
#"$4"framework/bin/run_evosuite.pl -p $1 -v "$2"f -n 1 -o $BUGPRE/"$TESTWD"/outputOfEvoSuite/ -c branch => 100s

#Untar the generated test into the tests folder
#cd $BUGPRE/"$TESTWD"
#tar xvjf outputOfEvoSuite/$1/evosuite-branch/1/"$1"-"$2"f-evosuite-branch.1.tar.bz2

EXTRACLASSES=""
if [ $LOWERCASEPACKAGE = "closure" ]; then
EXTRACLASSES="$3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInfo.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInformationMap.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInformationMapOrBuilder.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/Instrumentation.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/InstrumentationOrBuilder.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/InstrumentationTemplate.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/debugging/sourcemap/proto/Mapping.java"
fi

#Go to the bug folder
cd "$4"ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD/

echo Compiling source files...
#create file to run compilation
FILENAME=sources.txt
exec 3<>$FILENAME
# Write to file
echo $LIBSMAIN >&3
find -name "*.java" >&3
echo $EXTRACLASSES >&3
exec 3>&-


#Compile the project
javac @sources.txt


echo Compilation of main java classes successful

rm sources.txt


#where the .class files are
#DIROFCLASSFILES=org/$JAVADIR


#Jar all the .class's
#TODO maybe: change this to insert only the class files recursively, NOT the .java files also. Same thing in tests
jar cf $BUGPRE/"$LOWERCASEPACKAGE"AllSourceClasses.jar "$JAVADIR"* 
#$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 

echo Jar of source files created successfully.


#--------------------------------

#Compile test classes
cd $BUGPRE/$TESTWD

echo Compiling test files...

FILENAME=sources.txt
exec 3<>$FILENAME
# Write to file
echo $LIBSTESTS >&3
find -name "*.java" >&3
echo $EXTRACLASSES >&3
exec 3>&-

javac @sources.txt

echo Compilation of test java classes successful
#rm sources.txt

#javac *.java */*.java */*/*.java */*/*/*.java */*/*/*/*.java -Xlint:unchecked

#cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/test/java



#Jar all the test class's
jar cf $BUGPRE/"$LOWERCASEPACKAGE"AllTestClasses.jar "$JAVADIR"* 
#$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 

echo Jar of tests created successfully.








cd $BUGPRE/$WD

#Create file to run defects4j compiile
FILE="$4"ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
cd $4ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
$4framework/bin/defects4j compile
EOM

chmod 777 runCompile.sh



#Create a file neg.tests
touch $BUGPRE/neg.tests

cd $BUGPRE

PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file TODO:#FIX THIS FILE
FILE="$4"ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/configDefects4j
/bin/cat <<EOM >$FILE
targetClassName = PackageAndNameOfTheTargetFileWithNoExtension
popsize = 5
seed = 0
testsDir = $TESTWD/$JAVADIR
javaVM = /usr/bin/java
workingDir = $4ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD
outputDir = $4ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/tmp
libs = $CONFIGLIBS
classDir = bin/
sanity = yes
regenPaths
positiveTests = $4ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $4ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $4framework/projects/lib/jacocoagent.jar
defects4jFolder = $4framework/bin/
defects4jBugFolder = $4ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy
EOM

#info about the bug
defects4j info -p $1 -v $2

#Need to modify these three files
gedit $BUGPRE/configDefects4j
gedit $BUGPRE/neg.tests 
#gedit $BUGPRE/pos.tests 


#PASSSINGTESTS="$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

#if [[ -s $PASSSINGTESTS ]] ; then
#echo "Passing tests file has data, all good :D"
#else
#echo "ERROR!!! $PASSSINGTESTS is empty, means that all unit tests failed, so the file of the positive tests at $PASSSINGTESTS is empty. ERROR!!!"
#fi ;


#I then go to pos.tests, move the failing tests that appear in the "Root cause in triggering tests" in the console, to the neg.tests
echo 
echo Dear user: 
echo 1. It has been created and opened a file called neg.tests in this directory: $BUGPRE, please insert the package of the failing tests that appear in the \"Root cause in triggering tests\" above in this console, and copy that into the file that has been opened.
echo Example: org.apache.commons.math3.distribution.HypergeometricDistributionTest
echo 
echo 2. Now it has been created and opened a second file called configDefects4j in this location: $BUGPRE/ . Please go to that file and change the first line with the data in the section "List of modified sources" above in this console.
echo For example:
echo targetClassName = org.apache.commons.math3.distribution.HypergeometricDistribution
echo
echo 3. If running on Eclipse, copy paste the working directory from the config file into the working directory in the configuration of eclipse.









