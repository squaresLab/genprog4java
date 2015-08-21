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
BUGWD=$PARENTDIR"/"$LOWERCASEPACKAGE"$BUG"Buggy

#Specific variables per every project
#TESTWD is the address from the root to the address where JAVADIR starts, for the TEST files 
#WD is the address from the root to the address where JAVADIR starts,  for the SOURCE files 
#JAVADIR is the address from the WD or TESTWD, to the address where all the java files are for both source and test files 
#It is usually used TESTWD/JAVADIR or WD/JAVADIR
#CONFIGLIBS are the libraries to be included in the configuration file so that GenProg can run it.
#LIBSTESTS are the libraries needed to compile the  tests (dependencies of the project)
#LIBSMAIN are the libraries needed to compile the project (dependencies of the project)

SRCJAR=$BUGWD"/"$LOWERCASEPACKAGE"AllSourceClasses.jar"
TESTJAR=$BUGWD"/"$LOWERCASEPACKAGE"AllTestClasses.jar"

# Common genprog libs: junit test runner and the like

GENLIBS=$GENPROG"/lib/junittestrunner.jar:"$GENPROG"/lib/commons-io-1.4.jar:"$GENPROG"/lib/junit-4.10.jar"

# all libs for a package need at least the source jar, test jar, and generic genprog libs
CONFIGLIBS=$SRCJAR":"$TESTJAR


case "$LOWERCASEPACKAGE" in 
'chart') 
        TESTWD=tests
        WD=source
        JAVADIR=org/jfree
        CHARTLIBS="$BUGWD/lib/itext-2.0.6.jar:\
$BUGWD/lib/servlet.jar:\
$BUGWD/lib/junit.jar"
        
        CONFIGLIBS=$CONFIGLIBS":"$GENLIBS":"$CHARTLIBS
        LIBSTESTS="-cp \".:$SRCJAR:$GENLIBS:$CHARTLIBS\" "
        LIBSMAIN="-cp \".:$CHARTLIBS\" "
        ;;
'closure')
        TESTWD=test
        WD=src
        JAVADIR=com/google

        CLOSURELIBS="$BUGWD/lib/ant.jar:$BUGWD/lib/ant-launcher.jar:\
$BUGWD/lib/args4j.jar:$BUGWD/lib/caja-r4314.jar:\
$BUGWD/lib/guava.jar:$BUGWD/lib/jarjar.jar:\
$BUGWD/lib/json.jar:$BUGWD/lib/jsr305.jar:\
$BUGWD/lib/junit.jar:$BUGWD/lib/protobuf-java.jar"
        
        CONFIGLIBS=$CONFIGLIBS":"$GENLIBS":"$CLOSURELIBS

        LIBSTESTS="-cp \".:$SRCJAR:$GENLIBS:$CLOSURELIBS\" "
        LIBSMAIN="-cp \".:$CLOSURELIBS\" "
        ;;

'lang')
        TESTWD=src/test/java
        WD=src/main/java
        JAVADIR=org/apache/commons/lang3 
        # CLAIRE TO MAU: you alternate some of these paths between defects4j/framework/projects and 
        # defects4j/projects...but I don't have a defects4j/projects, only a
        # defects4j/framework/projects.  Are you sure about these paths?  Please check
        # for me.
        LANGLIBS="$GENPROG/lib/junittestrunner.jar:$GENPROG/lib/commons-io-1.4.jar:\
$DEFECTS4J/framework/projects/lib/junit-4.11.jar:\
$DEFECTS4J/projects/Lang/lib/easymock.jar:\
$DEFECTS4J/projects/Lang/lib/asm.jar:\
$DEFECTS4J/projects/Lang/lib/cglib.jar:\
$DEFECTS4J/framework/projects/lib/easymock-3.3.1.jar"
        CONFIGLIBS=$CONFIGLIBS:$LANGLIBS
        LIBSTESTS="-cp \".:$SRCJAR:\
$GENPROG/lib/junittestrunner.jar:$GENPROG/lib/commons-io-1.4.jar:\
$DEFECTS4J/framework/projects/lib/junit-4.11.jar:\
$DEFECTS4J/projects/Lang/lib/easymock.jar:\
$DEFECTS4J/framework/projects/lib/easymock-3.3.1.jar\" "
        LIBSMAIN=""
        ;;

'math')
        TESTWD=src/test/java
        WD=src/main/java
        JAVADIR=org/apache/commons/math3
        MATHLIBS=$DEFECTS4J"/framework/projects/Math/lib/commons-discovery-0.5.jar"
        CONFIGLIBS=$CONFIGLIBS":"$GENLIBS":"$MATHLIBS
        LIBSTESTS="-cp \".:$SRCJAR:$GENLIBS:$MATHLIBS\" "
        LIBSMAIN=""
        ;;

'time')
        TESTWD=src/test/java
        WD=src/main/java
        JAVADIR=org/joda/time
        TIMELIBS=$DEFECTS4J"/framework/projects/Time/lib/joda-convert-1.2.jar:"$GENLIBS":"$DEFECTS4J/"framework/projects/lib/easymock-3.3.1.jar"
        CONFIGLIBS=$CONFIGLIBS":"$TIMELIBS

        LIBSTESTS="-cp \".:$SRCJAR:$TIMELIBS\" "
        LIBSMAIN="-cp \".:$DEFECTS4J/framework/projects/Time/lib/joda-convert-1.2.jar\" "
        ;;
esac

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$DEFECTS4J"/framework/bin

#Print info about the project
defects4j info -p $1

#Print info about this bug in particular
defects4j info -p $1 -v $2

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$BUG"b -w $BUGWD

#Checkout the fixed version of the code to make the seccond test suite
defects4j checkout -p $1 -v "$BUG"f -w "$DEFECTS4J"ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed

#Go to the created folder
cd $BUGWD

#Compile the buggy code
defects4j compile

#Go to the created folder
cd $BUGWD

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
cp "$3"defects4jStuff/Utilities/EntityArrays.java $BUGWD/src/main/java/org/apache/commons/lang3/text/translate/
fi

#cd "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/

#create new list of Passing tests 
#touch $BUGWD/passingTests.tests

#go through all the entries of the file and the test passes insert it in the passing list (sanitize out of scope tests)
#while read e; do
#  echo $e
#java -cp .:"$4"ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllSourceClasses.jar:"$4"ExamplesCheckedOut/#Utilities/"$LOWERCASEPACKAGE"AllTestClasses.jar:"$GENPROG/lib/#junittestrunner.jar:"$GENPROG/lib/commons-io-1.4.jar:/home/mau/#Research/defects4j/framework/projects/lib/junit-4.11.jar org.junit.runner.JUnitCore $e

#if the test passes, added to the passing list
#if [ $? -eq 0 ]
#then
#    echo $e >> $BUGWD/#passingTests.tests
#fi
#done < pos.tests

#replace the list with all the tests, with the one with just the passing tests
#mv $BUGWD/passingTests.tests "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests





#UNCOMMENT!!!!!!!!!
#Create the new test suite
#echo Creating new test suite...
#"$4"framework/bin/run_evosuite.pl -p $1 -v "$2"f -n 1 -o $BUGWD/"$TESTWD"/outputOfEvoSuite/ -c branch => 100s

#Untar the generated test into the tests folder
#cd $BUGWD/"$TESTWD"
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
jar cf $BUGWD/"$LOWERCASEPACKAGE"AllSourceClasses.jar "$JAVADIR"* 
#$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 

echo Jar of source files created successfully.


#--------------------------------

#Compile test classes
cd $BUGWD/$TESTWD

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
jar cf $BUGWD/"$LOWERCASEPACKAGE"AllTestClasses.jar "$JAVADIR"* 
#$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 

echo Jar of tests created successfully.








cd $BUGWD/$WD

#Create file to run defects4j compiile
FILE="$4"ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
cd $4ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
$4framework/bin/defects4j compile
EOM

chmod 777 runCompile.sh



#Create a file neg.tests
touch $BUGWD/neg.tests

cd $BUGWD

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
gedit $BUGWD/configDefects4j
gedit $BUGWD/neg.tests 
#gedit $BUGWD/pos.tests 


#PASSSINGTESTS="$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

#if [[ -s $PASSSINGTESTS ]] ; then
#echo "Passing tests file has data, all good :D"
#else
#echo "ERROR!!! $PASSSINGTESTS is empty, means that all unit tests failed, so the file of the positive tests at $PASSSINGTESTS is empty. ERROR!!!"
#fi ;


#I then go to pos.tests, move the failing tests that appear in the "Root cause in triggering tests" in the console, to the neg.tests
echo 
echo Dear user: 
echo 1. It has been created and opened a file called neg.tests in this directory: $BUGWD, please insert the package of the failing tests that appear in the \"Root cause in triggering tests\" above in this console, and copy that into the file that has been opened.
echo Example: org.apache.commons.math3.distribution.HypergeometricDistributionTest
echo 
echo 2. Now it has been created and opened a second file called configDefects4j in this location: $BUGWD/ . Please go to that file and change the first line with the data in the section "List of modified sources" above in this console.
echo For example:
echo targetClassName = org.apache.commons.math3.distribution.HypergeometricDistribution
echo
echo 3. If running on Eclipse, copy paste the working directory from the config file into the working directory in the configuration of eclipse.









