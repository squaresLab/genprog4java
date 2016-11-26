#!/bin/bash

#The purpose of this script is to Create, Fix and Test a test suite of a particular defects4j bug.

#Preconditions:
#There should be a folder called generatedTestSuites in the defects4j folder where the test suites and their output will be stored.
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).

#Output
#The output is a txt file with the output of testing the test suite on the folder indicated. The name of the txt file is: EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2On"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt and it is located in $D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/

#Parameters:
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3th param is the generation tool (Randoop or Evosuite)
# 4th param is the path to the test suite, starting from the the D4J_HOME folder. Example: generatedTestSuites/Randoop3Min/
# 5th param is the path of the folder to evaluate the test suite on, starting from the the D4J_HOME folder (Example: ExamplesCheckedOut or BugsWithAFix)

#Example of usage:
#./runTestSuite.sh Math 2 Randoop generatedTestSuites/Randoop3Min/ BugsWithAFix


if [ "$#" -ne 5 ]; then
    echo "This script should be run with 5 parameters: "
	echo "1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)"
	echo "2nd param is the bug number (ex: 1,2,3,4,...)"
	echo "3th param is the generation tool (Randoop or Evosuite)"
	echo "4th param is the path to the test suite, starting from the the D4J_HOME folder. Example: generatedTestSuites/Randoop3Min/"
	echo "5th param is the path of the folder to evaluate the test suite on, starting from the the D4J_HOME folder (Example: ExamplesCheckedOut or BugsWithAFix)"

    exit 0
fi


PROJECT="$1"
BUGNUMBER="$2"
RANDOOPOREVOSUITE="$3"
PATHTOTESTSUITE="$4"
PATHOFFIXEDFOLDER="$5"

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

export JRE_HOME=$JAVA_HOME/jre
export PATH=$JAVA_HOME/bin/:$PATH

SEED=1
  echo ""
  echo "Evaluating test suite"
  echo ""
  cd $D4J_HOME/framework/bin
  
  if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
    OUTPUTFILE="$D4J_HOME/$PATHTOTESTSUITE/EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bzOn"$PROJECT""$BUGNUMBER"Buggy.txt"
    COM3="./defects4j test -s $D4J_HOME/$PATHTOTESTSUITE/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w $D4J_HOME/$PATHOFFIXEDFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ &>> $OUTPUTFILE"
  elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
    OUTPUTFILE="$D4J_HOME/$PATHTOTESTSUITE/EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2On"$PROJECT""$BUGNUMBER"Buggy.txt"
    COM3="./defects4j test -s $D4J_HOME/$PATHTOTESTSUITE/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 -w $D4J_HOME/$PATHOFFIXEDFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ &>> $OUTPUTFILE"
  fi
  RMV="rm -f $OUTPUTFILE"
  eval $RMV
  echo "$COM3"
  eval $COM3

echo ""
echo "The output is saved in: $OUTPUTFILE"
