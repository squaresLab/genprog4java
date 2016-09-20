#!/bin/sh

#./runScriptWithNewRandoop.sh Math 2 180

PROJECT=$1
BUGNUMBER=$2
BUDGET=$3

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


#for (( SEED=1 ; SEED<=10 ; SEED++ ))
#do
SEED=1

rm /home/mausoto/defects4j/totalTestsExecuted.txt
touch /home/mausoto/defects4j/totalTestsExecuted.txt
rm /home/mausoto/defects4j/ResultsFromRunningGenereatedTestSuites.txt
touch /home/mausoto/defects4j/ResultsFromRunningGenereatedTestSuites.txt

cd /home/mausoto/defects4j/framework/bin

echo ""
echo "Running command: perl run_randoop.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/ -b $BUDGET"

COM1=$(perl run_randoop.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/ -b $BUDGET)
echo $COM1


cd /home/mausoto/defects4j/framework/util

echo ""
echo "Running command: perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/"

COM2=$(perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/)
echo $COM2



cd /home/mausoto/defects4j/framework/bin

echo ""
echo "Running command: ./defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/"$PROJECT"/randoop/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/testOutput.txt"
COM3=$(./defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/"$PROJECT"/randoop/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"WithNewRandoop/testOutput.txt)
echo $COM3



COM4=$(wc -l < /home/mausoto/defects4j/totalTestsExecuted.txt)
echo $COM4

#done
