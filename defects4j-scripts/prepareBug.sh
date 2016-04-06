#!/bin/bash
#
# 1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param: bug number (ex: 1,2,3,4,...)
# 3rd param: location of genprog4java (ex: "/home/mau/Research/genprog4java/" )
# 4td param: defects4j installation (ex: "/home/mau/Research/defects4j/" )
# 5th param: testing option (ex: humanMade, generated)
# 6th param: test suite sample size (ex: 1, 100)

# Example usage, local for Mau
#./prepareBug.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ humanMade 100

# Example usage, VM:
#./prepareBug.sh Math 2 /home/ubuntu/genprog4java/ /home/ubuntu/defects4j/ allHuman 100

# OS X note, mostly for CLG: 
# javac has to be version 1.7, and JAVA_HOME must be set accordingly,
# So, don't forget to do the following on OS X:
# export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/
# export PATH=$JAVA_HOME/bin/:$PATH

if [ "$#" -ne 6 ]; then
    echo "This script should be run with 6 parameters: Project name, bug number, location of genprog4java, defects4j installation, testing option, test suite size"
    exit 0
fi

PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
OPTION="$5"
TESTSUITEPERCENTAGE="$6"

PARENTDIR=$DEFECTS4JDIR"/ExamplesCheckedOut"


#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$DEFECTS4JDIR"/framework/bin
export PATH=$PATH:"$DEFECTS4JDIR"/major/bin

#copy these files to the source control

mkdir -p $PARENTDIR

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGWD=$PARENTDIR"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$BUGNUMBER"b -w $BUGWD

#Checkout the fixed version of the code, primarily to make the second test suite
defects4j checkout -p $1 -v "$BUGNUMBER"f -w "$DEFECTS4JDIR/"ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed

#Compile the both buggy and fixed code
for dir in Buggy Fixed
do
    pushd $PARENTDIR"/"$LOWERCASEPACKAGE$BUGNUMBER$dir
    defects4j compile
    popd
done

#Specific variables per project
#TESTWD: location of project test files (relative to root)
#WD: location of project src files (relative to root)
#CONFIGLIBS: libraries to be included in the configuration file so that GenProg can run it.

# Common genprog libs: junit test runner and the like

CONFIGLIBS=$GENPROGDIR"/lib/junittestrunner.jar:"$GENPROGDIR"/lib/commons-io-1.4.jar:"$GENPROGDIR"/lib/junit-4.10.jar"

if [ "$LOWERCASEPACKAGE" = "lang" ] ; then
    # special handling...do we still need this?
    cp "$3"defects4j-scripts/Utilities/EntityArrays.java $BUGWD/src/main/java/org/apache/commons/lang3/text/translate/
fi

cd $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
TESTWD=`defects4j export -p dir.src.tests`
SRCFOLDER=`defects4j export -p dir.bin.classes`
COMPILECP=`defects4j export -p cp.compile`
TESTCP=`defects4j export -p cp.test`
WD=`defects4j export -p dir.src.classes`
cd $BUGWD/$WD

#Create file to run defects4j compile

FILE="$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/
export PATH=$JAVA_HOME/bin/:$PATH
cd $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
$DEFECTS4JDIR/framework/bin/defects4j compile
EOM

chmod 777 "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh


cd $BUGWD

#Create config file 
FILE="$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
popsize = 20
seed = 0
javaVM = /usr/bin/java
workingDir = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
outputDir = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/tmp
libs = $CONFIGLIBS
sanity = yes
regenPaths
sourceDir = $WD
positiveTests = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $3/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
EOM

#  get passing and failing tests as well as files
#info about the bug

defects4j export -p tests.trigger > $BUGWD/neg.tests

case "$OPTION" in
"humanMade" ) 
        defects4j export -p tests.all > $BUGWD/pos.tests

;;
"allHuman" ) 
        defects4j export -p tests.all > $BUGWD/pos.tests
;;

"onlyRelevant" ) 
        defects4j export -p tests.relevant > $BUGWD/pos.tests
        ;;

"generated" )
#Create the new test suite
echo Creating new test suite...
"$DEFECTS4JDIR"/framework/bin/run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n 1 -o $BUGWD/"$TESTWD"/outputOfEvoSuite/ -c branch -b 100 -A

#Untar the generated test into the tests folder
cd $BUGWD/"$TESTWD"/
tar xvjf outputOfEvoSuite/$PROJECT/evosuite-branch/1/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch.1.tar.bz2
;;
esac

# FIXME: make this nicer

#Remove a percentage of the positive tests in the test suite
cd "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/

if [[ $TESTSUITEPERCENTAGE -ne 100 ]]
then
TESTCOUNT=$(cat pos.tests | wc -l)

PERCENTAGETOREMOVE=$(echo "$TESTSUITEPERCENTAGE * 0.01" | bc -l )

PERCENTAGETOREMOVE=$(echo "1-$PERCENTAGETOREMOVE" | bc -l )

TESTCOUNT=$(echo "$TESTCOUNT * $PERCENTAGETOREMOVE" | bc -l )

TESTCOUNT=$(echo "($TESTCOUNT+0.5)/1" | bc)

DELETELINES="sed -i -e 1,"$TESTCOUNT"d pos.tests"

eval $DELETELINES

fi

TESTCOUNT=$(cat pos.tests | wc -l)

echo "The positive tests file has $TESTCOUNT lines. Which is a $TESTSUITEPERCENTAGE% of the original amount of test cases."



# get the class names to be repaired


echo "GETTING CLASS NAMES"

defects4j export -p classes.modified > $BUGWD/bugfiles.txt

echo "This is the working directory: "
echo $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD
