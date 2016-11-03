#!/bin/bash

#The purpose of this script is to Create, Fix and Test a test suite of a particular defects4j bug.

#Preconditions:
#There should be a folder called generatedTestSuites in the defects4j folder where the test suites and their output will be stored.
#There should be a folder called ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ in the defects4j folder. This is the code the test suite will run on to get tested.
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).

#Output
#The output is a txt file with the output of testing the test suite on the folder indicated. The name of the txt file is: EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2On"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt and it is located in $D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/

#Parameters:
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3th param is the generation tool (Randoop or Evosuite)
# 4th param is the budget of time in seconds the tool has to generate the test suite
# 5th param is weather you want to run only sections of the script: C=create, F=fix, E=evaluate. You can run: CFE, FE, E (To run just a latter steps, the previous should have already been ran in the past)
# 6th param is the name of the folder the test suite will be stored in.

#Example of usage:
#./cfeIndividual.sh Math 2 Randoop 180 CFE September21


if [ "$#" -ne 6 ]; then
    echo "This script should be run with 6 parameters: "
	echo "1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)"
	echo "2nd param is the bug number (ex: 1,2,3,4,...)"
	echo "3th param is the generation tool (Randoop or Evosuite)"
	echo "4th param is the budget of time in seconds the tool has to generate the test suite"
	echo "5th param is weather you want to run only sections of the script: C=create, F=fix, E=evaluate. You can run: CFE, FE, E (To run just a latter steps, the previous should have already been ran in the past)"
	echo "6th param is the name of the folder the test suite will be stored in."

    exit 0
fi


PROJECT="$1"
BUGNUMBER="$2"
RANDOOPOREVOSUITE="$3"
BUDGET="$4"
CFE="$5"
IDENTIFIER="$6"

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

export JRE_HOME=$JAVA_HOME/jre
export PATH=$JAVA_HOME/bin/:$PATH

SEED=1

if [ $CFE == "CFE" ] || [ $CFE == "CF" ] || [ $CFE == "C" ]; then
  echo ""
  echo "Creating test suite"
  echo ""
  cd $D4J_HOME/framework/bin
  if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
    COM1="perl run_randoop.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o $D4J_HOME/generatedTestSuites/$IDENTIFIER/ -b $BUDGET"
  elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
    COM1="perl run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o $D4J_HOME/generatedTestSuites/$IDENTIFIER/ -c branch -b $BUDGET"
  fi
  echo "$COM1"
  eval $COM1
fi


if [ $CFE == "CFE" ] || [ $CFE == "CF" ] || [ $CFE == "F" ]; then
  echo ""
  echo "Fixing test suite"
  echo ""
  cd $D4J_HOME/framework/util
  if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
    COM2="perl fix_test_suite.pl -p $PROJECT -d $D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/randoop/"$SEED"/ -v "$BUGNUMBER"f"
  elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
    COM2="perl fix_test_suite.pl -p $PROJECT -d $D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/ -v "$BUGNUMBER"f"
  fi
  echo "$COM2"
  eval $COM2
fi


if [ $CFE == "CFE" ] || [ $CFE == "FE" ] || [ $CFE == "E" ]; then
  echo ""
  echo "Evaluating test suite"
  echo ""
  cd $D4J_HOME/framework/bin
  
  if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
    OUTPUTFILE="$D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/randoop/"$SEED"/EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bzOn"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt"
    COM3="./defects4j test -s $D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/randoop/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w $D4J_HOME/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ &>> $OUTPUTFILE"
  elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
    OUTPUTFILE="$D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2On"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt"
    COM3="./defects4j test -s $D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 -w $D4J_HOME/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ &>> $OUTPUTFILE"
  fi
  RMV="rm -f $OUTPUTFILE"
  eval $RMV
  echo "$COM3"
  eval $COM3
fi

echo ""
echo "The output is saved in: $OUTPUTFILE"

