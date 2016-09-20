#!/bin/sh

PROJECT=$1
BUGNUMBER=$2

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


SEED=3


rm /home/mausoto/defects4j/totalTestsExecuted.txt
touch /home/mausoto/defects4j/totalTestsExecuted.txt
rm /home/mausoto/defects4j/ResultsFromRunningGenereatedTestSuites.txt
touch /home/mausoto/defects4j/ResultsFromRunningGenereatedTestSuites.txt

cd /home/mausoto/defects4j/framework/bin

echo ""
echo "Running command: perl run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/ -c branch -b 180"

COM1=$(perl run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/ -A -c branch -b 180)
echo $COM1



cd /home/mausoto/defects4j/framework/util

echo ""
echo "Running command: perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/)"

COM2=$(perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/)
echo $COM2



cd /home/mausoto/defects4j/framework/bin

echo ""
echo "Running command: ./defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/testOutput.txt"


COM3=$(./defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/testOutput.txt)
echo $COM3



COM4=$(wc -l < /home/mausoto/defects4j/totalTestsExecuted.txt)
echo $COM4


