#!/bin/bash
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: /home/mau/Research/genprog4java/ )
# 4td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
#5th param is the folder where the java 7 instalation is located
#6th param is the folder where the java 8 instalation is located
#7th param is the generation tool (Randoop or Evosuite)
#8th param is the budget

#VM:
#./cfeIndividual.sh Math 2 /home/ubuntu/genprog4java/ /home/ubuntu/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64  /usr/lib/jvm/java-1.8.0-openjdk-amd64 Randoop 180 CFE September21


if [ "$#" -ne 10 ]; then
    echo "This script should be run with 10 parameters"
    exit 0
fi


PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
DIROFJAVA7="$5"
DIROFJAVA8="$6"
RANDOOPOREVOSUITE="$7"
BUDGET="$8"
CFE="$9"
IDENTIFIER="${10}"

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

case "$LOWERCASEPACKAGE" in 
'chart') 
	    TESTFOLDER=tests
        ;;

'closure')
	    TESTFOLDER=test
        ;;

'lang')
	    TESTFOLDER=src/test/java
        ;;

'math')
	    TESTFOLDER=src/test/java
        ;;

'time')
	    TESTFOLDER=src/test/java
        ;;
esac

#export JAVA_HOME=$DIROFJAVA8
#export JRE_HOME=$DIROFJAVA8/jre
#export PATH=$DIROFJAVA8/bin/:$PATH

#cd $GENPROGDIR/defects4j-scripts/
#CMD="sh runGenProgForBug.sh $1 $2 $3 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12}"
#eval $CMD
#echo "GenProg ran for the bug $PROJECT $BUGNUMBER"

#for (( SEED=1 ; SEED<=10 ; SEED++ ))
#do


rm -f $DEFECTS4JDIR/totalTestsExecuted.txt
touch $DEFECTS4JDIR/totalTestsExecuted.txt
#echo "Removed: $DEFECTS4JDIR/totalTestsExecuted.txt"
#rm -f $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt
#touch $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt
#echo "Removed: $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt"

export JAVA_HOME=$DIROFJAVA7
export JRE_HOME=$DIROFJAVA7/jre
export PATH=$DIROFJAVA7/bin/:$PATH

SEED=1

if [ $CFE == "CFE" ] || [ $CFE == "CF" ] || [ $CFE == "C" ]; then
  echo "Creating test suite"
  cd $DEFECTS4JDIR/framework/bin
  if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
    COM1="perl run_randoop.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/ -b $BUDGET"
  elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
    COM1="perl run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/ -c branch -b $BUDGET"
  fi
  echo "$COM1"
  #START=$(date +%s.%N)
  eval $COM1
  #END=$(date +%s.%N)
  #DIFF=$(echo "$END - $START" | bc)
  #echo "Time elapsed creating the test suite: $DIFF"
fi


if [ $CFE == "CFE" ] || [ $CFE == "CF" ] || [ $CFE == "F" ]; then
  echo "Fixing test suite"
  cd $DEFECTS4JDIR/framework/util
  if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
    COM2="perl fix_test_suite.pl -p $PROJECT -d $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/"$PROJECT"/randoop/"$SEED"/ -v "$BUGNUMBER"f"
  elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
    COM2="perl fix_test_suite.pl -p $PROJECT -d $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/ -v "$BUGNUMBER"f"
  fi
  echo "$COM2"
  #START=$(date +%s.%N)
  eval $COM2
  #END=$(date +%s.%N)
  #DIFF=$(echo "$END - $START" | bc)
  #echo "Time elapsed fixing the test suite: $DIFF"
fi


if [ $CFE == "CFE" ] || [ $CFE == "FE" ] || [ $CFE == "E" ]; then
  echo "Evaluating test suite"
  cd $DEFECTS4JDIR/framework/bin
  if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
    COM3="./defects4j test -s $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/"$PROJECT"/randoop/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w $DEFECTS4JDIR/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/"
# &>> $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/"$PROJECT"/randoop/"$SEED"/EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bzOn"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt"
  elif [ $RANDOOPOREVOSUITE == "Evosuite" ]; then
    COM3="./defects4j test -s $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 -w $DEFECTS4JDIR/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ &>> $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2On"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt"
  fi
  echo "$COM3"
  eval $COM3
fi

#Counting the total test cases created
COM4=$(wc -l < $DEFECTS4JDIR/totalTestsExecuted.txt)
#echo "Total test cases created: $COM4"
echo "Total test cases created: $COM4" &>> $DEFECTS4JDIR/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2On"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt


#done
