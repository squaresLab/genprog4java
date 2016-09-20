#!/bin/sh
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: /home/mau/Research/genprog4java/ )
# 4td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
# 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)
# 6th param is the percentage of test cases being used to guide genprog's search (ex: 1, 100)
# 7th param is the folder where the bug files will be cloned to
# 8th param is the initial seed. It will then increase the seeds by adding 1 until it gets to the number in the 9th param.
# 9th param is the final seed.
#10th param is on if the purpose is to test only fault loc and not really trying to find a patch
#11th param is the folder where the java 7 instalation is located
#12th param is the folder where the java 8 instalation is located

if [ "$#" -lt 10 ]; then
    echo "This script should be run with 10 to 12 parameters"
    exit 0
fi


PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
OPTION="$5"
TESTSUITEPERCENTAGE="$6"
BUGSFOLDER="$7"
STARTSEED="$8"
UNTILSEED="$9"
JUSTTESTINGFAULTLOC="${10}"
DIROFJAVA7="/usr/lib/jvm/java-1.7.0-openjdk-amd64"
DIROFJAVA8="/usr/lib/jvm/java-1.8.0-openjdk-amd64"

if [ "$#" -eq 12 ]; then
  DIROFJAVA7="${11}"
  DIROFJAVA8="${12}"
fi

RANDOOPOREVOSUITE="Randoop"

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

export JAVA_HOME=$DIROFJAVA8
export JRE_HOME=$DIROFJAVA8/jre
export PATH=$DIROFJAVA8/bin/:$PATH

cd /home/mausoto/genprog4java/defects4j-scripts/
CMD="sh runGenProgForBug.sh $1 $2 $3 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12}"
eval $CMD
echo "GenProg ran for the bug $PROJECT $BUGNUMBER"

#for (( SEED=1 ; SEED<=10 ; SEED++ ))
#do

export JAVA_HOME=$DIROFJAVA7
export JRE_HOME=$DIROFJAVA7/jre
export PATH=$DIROFJAVA7/bin/:$PATH


SEED=1

cd /home/mausoto/defects4j/framework/bin
echo ""
if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
echo "Running command: perl run_randoop.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/ -b 180"
COM1=$(perl run_randoop.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/ -b 180)
elif [ $RANDOOPOREVOSUITE == "Evosusite" ]; then
echo "Running command: perl run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/ -c branch -b 180"
COM1=$(perl run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n $SEED -o /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/ -c branch -b 180)
fi
echo $COM1

cd /home/mausoto/defects4j/framework/util
echo ""
if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
echo "Running command: perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"/"
COM2=$(perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"/)
elif [ $RANDOOPOREVOSUITE == "Evosusite" ]; then
echo "Running command: perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/"
COM2=$(perl fix_test_suite.pl -p $PROJECT -d /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/)
fi
echo $COM2

cd /home/mausoto/defects4j/framework/bin
echo ""
if [ $RANDOOPOREVOSUITE == "Randoop" ]; then
echo "Running command: ./defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$BUGNUMBER-Buggy/$TESTFOLDER/outputOfRandoop/$PROJECT/randoop/$SEED/testOutput.txt"
COM3=$(./defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfRandoop/"$PROJECT"/randoop/"$SEED"/testOutput.txt)
elif [ $RANDOOPOREVOSUITE == "Evosusite" ]; then
echo "Running command: sh defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/testOutput.txt"
COM3=$(sh defects4j test -s /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2 -w /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/ | tee /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/$TESTFOLDER/outputOfEvosuite/"$PROJECT"/evosuite-branch/"$SEED"/testOutput.txt)

fi
echo $COM3


COM4=$(wc -l < /home/mausoto/defects4j/totalTestsExecuted.txt)
echo $COM4

#done
