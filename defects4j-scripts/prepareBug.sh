#!/bin/bash

#The purpose of this script is to set up the environment to run Genprog of a particular defects4j bug.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable GP4J_HOME should be directed to the folder where genprog4java is installed.

#Output
#The output is a txt file with the output of running the coverage analysis of the test suite on each of the folders indicated. 

# 1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param: bug number (ex: 1,2,3,4,...)
# 3th param: testing option (ex: humanMade, generated)
# 4th param: test suite sample size (ex: 1, 100)
# 5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME (Ex: ExamplesCheckedOut)
# 6th param is the folder where the java 7 instalation is located
# 7th param is the folder where the java 8 instalation is located

# Example usage, VM:
#./prepareBug.sh Math 2 allHuman 100 ExamplesCheckedOut /usr/lib/jvm/java-7-oracle/ /usr/lib/jvm/java-8-oracle/

if [ "$#" -ne 9 ]; then
    echo "This script should be run with 7 parameters:"
	echo "1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)"
	echo "2nd param: bug number (ex: 1,2,3,4,...)"
	echo "3th param: testing option (ex: humanMade, generated)"
	echo "4th param: test suite sample size (ex: 1, 100)"
	echo "5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME"
	echo "6th param is the folder where the java 7 instalation is located"
	echo "7th param is the folder where the java 8 instalation is located"
	echo "8th param is the folder where GenProg4Java is located"
	echo "9th param is the path to the probabilistic model"
    exit 0
fi

PROJECT="$1"
BUGNUMBER="$2"
OPTION="$3"
TESTSUITESAMPLE="$4"
BUGSFOLDER="$5"
DIROFJAVA7="$6"
DIROFJAVA8="$7"
GENPROG="$8"
GRAMMARPATH="$9"

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$D4J_HOME"/framework/bin/
export PATH=$PATH:"$D4J_HOME"/framework/util/
export PATH=$PATH:"$D4J_HOME"/major/bin/


#copy these files to the source control

mkdir -p $D4J_HOME/$BUGSFOLDER

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGWD=$D4J_HOME/$BUGSFOLDER"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Checkout the buggy and fixed versions of the code (latter to make second testsuite
defects4j checkout -p $1 -v "$BUGNUMBER"b -w $BUGWD

##defects4j checkout -p $1 -v "$BUGNUMBER"f -w $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE"$2"Fixed

#Compile the both buggy and fixed code
for dir in Buggy
do
    pushd $D4J_HOME/$BUGSFOLDER"/"$LOWERCASEPACKAGE$BUGNUMBER$dir
    defects4j compile
    popd
done
# Common genprog libs: junit test runner and the like

CONFIGLIBS=$GP4J_HOME"/lib/junittestrunner.jar:"$GP4J_HOME"/lib/commons-io-1.4.jar:"$GP4J_HOME"/lib/junit-4.12.jar:"$GP4J_HOME"/lib/hamcrest-core-1.3.jar"

cd $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
TESTWD=`defects4j export -p dir.src.tests`
SRCFOLDER=`defects4j export -p dir.bin.classes`
COMPILECP=`defects4j export -p cp.compile`
TESTCP=`defects4j export -p cp.test`
WD=`defects4j export -p dir.src.classes`
cd $BUGWD/$WD

#Create file to run defects4j compile

FILE=$D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
export JAVA_HOME=$DIROFJAVA7
export PATH=$DIROFJAVA7/bin/:$PATH
#sudo update-java-alternatives -s java-7-oracle
cd $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
$D4J_HOME/framework/bin/defects4j compile
export JAVA_HOME=$DIROFJAVA8
export PATH=$DIROFJAVA8/bin/:$PATH
#sudo update-java-alternatives -s java-8-oracle
EOM

chmod 777 $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh


cd $BUGWD

#Create config file 
FILE=$D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
seed = 0
sanity = no
popsize = 40
javaVM = $DIROFJAVA7/jre/bin/java
workingDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $GP4J_HOME/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
targetClassName = $BUGWD/bugfiles.txt
#sample=0.1
#edits=append;replace;delete;FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
#edits=append;replace;delete
#model=probabilistic
#modelPath=/home/mausoto/probGenProg/genprog4java/overallModel.txt
faultStrategy = entropy
grammarPath = $GRAMMARPATH
genProgDir = $GENPROG
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

  JAVALOCATION=$(which java)

  #Create the new test suite
  echo Creating new test suite...
  SEED=1
  cd "$D4J_HOME"/framework/bin/
  perl run_randoop.pl -p "$PROJECT" -v "$BUGNUMBER"f -n "$SEED" -o $BUGWD/"$TESTWD"/outputOfRandoop/ -b 1800
  perl "$D4J_HOME"/framework/util/fix_test_suite.pl -p "$PROJECT" -d $BUGWD/"$TESTWD"/outputOfRandoop/$PROJECT/randoop/$SEED/
  OUTPUT=$(defects4j test -s $BUGWD/"$TESTWD"/outputOfRandoop/$PROJECT/randoop/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w $BUGWD)
  TOTALEXECUTED=$(wc -l < "$D4J_HOME/totalTestsExecuted.txt")
  FAILEDTESTS=$(echo ${OUTPUT:(15)} | awk '{print $1;}')
  echo "$PROJECT $BUGNUMBER $TOTALEXECUTED $FAILEDTESTS" >> $D4J_HOME/ResultsFromRunningGenereatedTestSuites.txt
  echo "$FAILEDTESTS: tests failed from $TOTALEXECUTED in $PROJECT $BUGNUMBER"
  rm "$D4J_HOME/totalTestsExecuted.txt"



  #PRINT=$(echo "${OUTPUT:(15)}")
  #echo "This is what happened after the substitution: $PRINT"

  #Untar the generated test into the tests folder
  cd $BUGWD/"$TESTWD"/
  tar xvjf outputOfRandoop/$PROJECT/randoop/1/"$PROJECT"-"$BUGNUMBER"f-randoop.1.tar.bz2

  find . -maxdepth 1 -name "*.java" -exec basename \{} .java \; > $BUGWD/pos.tests
  rm $BUGWD/"$TESTWD"/*.java

;;
esac


#Remove a percentage of the positive tests in the test suite
cd $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/

if [[ $TESTSUITESAMPLE -ne 100 ]]
then
    PERCENTAGETOREMOVE=$(echo "$TESTSUITESAMPLE * 0.01" | bc -l )
    echo "sample = $PERCENTAGETOREMOVE" >> $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
fi

# get the class names to be repaired


defects4j export -p classes.modified > $BUGWD/bugfiles.txt

echo "This is the working directory: "
echo $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$WD
