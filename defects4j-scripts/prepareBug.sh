#!/bin/bash
#
# 1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param: bug number (ex: 1,2,3,4,...)
# 3rd param: location of genprog4java (ex: "/home/mau/Research/genprog4java/" )
# 4td param: defects4j installation (ex: "/home/mau/Research/defects4j/" )
# 5th param: testing option (ex: humanMade, generated)
# 6th param: test suite sample size (ex: 1, 100)
# 7th param is the folder where the bug files will be cloned to

# Example usage, local for Mau
#./prepareBug.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ humanMade 100 /home/mau/Research/defects4j/ExamplesCheckedOut/

# Example usage, VM:
#./prepareBug.sh Math 2 /home/ubuntu/genprog4java/ /home/ubuntu/defects4j/ allHuman 100 /home/mau/Research/defects4j/ExamplesCheckedOut/

# OS X note, mostly for CLG: 
# javac has to be version 1.7, and JAVA_HOME must be set accordingly,
# So, don't forget to do the following on OS X:
# export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/
# export PATH=$JAVA_HOME/bin/:$PATH

if [ "$#" -ne 7 ]; then
    echo "This script should be run with 7 parameters: Project name, bug number, location of genprog4java, defects4j installation, testing option, test suite size, bugs folder"
    exit 0
fi

PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
OPTION="$5"
TESTSUITEPERCENTAGE="$6"
BUGSFOLDER="$7"

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$DEFECTS4JDIR"/framework/bin/
export PATH=$PATH:"$DEFECTS4JDIR"/framework/util/
export PATH=$PATH:"$DEFECTS4JDIR"/major/bin/


#copy these files to the source control

mkdir -p $BUGSFOLDER

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGWD=$BUGSFOLDER"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Checkout the buggy and fixed versions of the code (latter to make second testsuite
#THIS IS COMMENTED BECAUSE THIS WAS ALREADY CREATED
#defects4j checkout -p $1 -v "$BUGNUMBER"b -w $BUGWD

##defects4j checkout -p $1 -v "$BUGNUMBER"f -w $BUGSFOLDER/$LOWERCASEPACKAGE"$2"Fixed

#Compile the both buggy and fixed code
for dir in Buggy
do
    pushd $BUGSFOLDER"/"$LOWERCASEPACKAGE$BUGNUMBER$dir
    defects4j compile
    popd
done
# Common genprog libs: junit test runner and the like

CONFIGLIBS=$GENPROGDIR"/lib/junittestrunner.jar:"$GENPROGDIR"/lib/commons-io-1.4.jar:"$GENPROGDIR"/lib/junit-4.12.jar:"$GENPROGDIR"/lib/hamcrest-core-1.3.jar"

cd $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
TESTWD=`defects4j export -p dir.src.tests`
SRCFOLDER=`defects4j export -p dir.bin.classes`
COMPILECP=`defects4j export -p cp.compile`
TESTCP=`defects4j export -p cp.test`
WD=`defects4j export -p dir.src.classes`
cd $BUGWD/$WD

#Create file to run defects4j compile

FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
cd $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
$DEFECTS4JDIR/framework/bin/defects4j compile
EOM

chmod 777 $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh


cd $BUGWD

#Create config file 
FILE=$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
seed = 0
sanity = yes
popsize = 20
javaVM = /usr/bin/java
workingDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/
outputDir = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/tmp
classSourceFolder = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
libs = $CONFIGLIBS
sourceDir = $WD
positiveTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $3/lib/jacocoagent.jar
testClassPath=$TESTCP
srcClassPath=$COMPILECP
compileCommand = $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
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

  JAVALOCATION=$(which java)

  #Create the new test suite
  echo Creating new test suite...
  cd "$DEFECTS4JDIR"/framework/bin/
  #THIS IS COMMENTED BECAUSE THIS WAS ALREADY CREATED
  #perl run_randoop.pl -p "$PROJECT" -v "$BUGNUMBER"f -n 1 -o $BUGWD/"$TESTWD"/outputOfRandoop/ -b 180
  perl "$DEFECTS4JDIR"/framework/util/fix_test_suite.pl -p "$PROJECT" -d $BUGWD/"$TESTWD"/outputOfRandoop/$PROJECT/randoop/1/
  OUTPUT=$(defects4j test -s $BUGWD/"$TESTWD"/outputOfRandoop/$PROJECT/randoop/1/"$PROJECT"-"$BUGNUMBER"f-randoop.1.tar.bz2 -w $BUGWD)
  echo "${OUTPUT:(15)}: tests failed in $PROJECT $BUGNUMBER" >> $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt


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
cd $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/

if [[ $TESTSUITEPERCENTAGE -ne 100 ]]
then
    PERCENTAGETOREMOVE=$(echo "$TESTSUITEPERCENTAGE * 0.01" | bc -l )
    echo "sample = $PERCENTAGETOREMOVE" >> $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/defects4j.config
fi

# get the class names to be repaired


defects4j export -p classes.modified > $BUGWD/bugfiles.txt

echo "This is the working directory: "
echo $BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/$WD
