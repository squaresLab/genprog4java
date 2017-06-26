#!/bin/bash

#The purpose of this script is to test the coverage of a generated test sutie.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).

#Output
#The output is a txt file with the output of running the coverage analysis of the test suite on the folder indicated. 

#Parameters:
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3th param is the generation tool (Randoop or Evosuite)
# 4th param is the path of the buggy folder, starting from the the D4J_HOME folder (Example: ExamplesCheckedOut or BugsWithAFix)
# 5th param is the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)

#Example of usage:
#./checkCoverageOfGeneratedTestSuite.sh Math 2 Randoop BugsWithAFix/lang32Buggy/ generatedTestSuitesForBugsWeFoundARepairFor/

if [ "$#" -ne 5 ]; then
    echo "This script should be run with 5 parameters:"
	echo "1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)"
	echo "2nd param is the bug number (ex: 1,2,3,4,...)"
	echo "3th param is the generation tool (Randoop or Evosuite)"
	echo "4th param is the path of the buggy folder, starting from the the D4J_HOME folder (Example: ExamplesCheckedOut or BugsWithAFix)"
	echo "5th param is the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)"

    exit 0
fi

PROJECT="$1"
BUGNUMBER="$2"
RANDOOPOREVOSUITE="$3"
PATHOFFIXEDFOLDER="$4"
PATHOFSUITEFOLDER="$5"

export JRE_HOME=$JAVA_HOME/jre
export PATH=$JAVA_HOME/bin/:$PATH

echo ""
echo "Evaluating coverage of the test suite $PROJECT $BUGNUMBER"
echo ""
cd $D4J_HOME/framework/bin

SEED=1

rm -fr $D4J_HOME/$PATHOFFIXEDFOLDER/build/gen-tests/
rm -fr $D4J_HOME/$PATHOFFIXEDFOLDER/gen-tests/
rm -fr $D4J_HOME/$PATHOFFIXEDFOLDER/target/gen-tests/

OUTPUTFILE="$PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"CoverageLog.txt"
if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
  COM="./defects4j coverage -w $D4J_HOME/$PATHOFFIXEDFOLDER/ -s $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 &>> $OUTPUTFILE" 
elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
  COM="./defects4j coverage -w $D4J_HOME/$PATHOFFIXEDFOLDER/ -s $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 &>> $OUTPUTFILE" 
fi

echo "$COM"
rm -f $OUTPUTFILE
eval $COM
echo ""
echo "Log file located in $OUTPUTFILE"


