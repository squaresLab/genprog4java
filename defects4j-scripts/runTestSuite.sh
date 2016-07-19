#!/bin/bash

#This file takes the bug code and runs it on the test suite in $DEFECTS4JDIR/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/pos.tests

# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: "/home/mau/Research/genprog4java/" )
# 4td param is the folder where defects4j is installed (ex: "/home/mau/Research/defects4j/" )

#Mau runs it like this:
#./runTestSuite.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/

#VM:
#./runTestSuite.sh Math 2 /home/ubuntu/genprog4java/ /home/ubuntu/defects4j/

PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

POSTESTS=$DEFECTS4JDIR/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/pos.tests

case "$LOWERCASEPACKAGE" in 
'chart') 
	    SRCFOLDER=build
	    TESTFOLDER=build-tests
        ;;

'closure')
	    SRCFOLDER=build/classes
	    TESTFOLDER=build/test
        ;;

'lang')

	    SRCFOLDER=target/classes/
	    TESTFOLDER=target/test-classes/
        ;;

'math')
	    SRCFOLDER=target/classes
	    TESTFOLDER=target/test-classes
        ;;

'time')
 	    SRCFOLDER=build/classes/
	    TESTFOLDER=build/tests/
        ;;
esac

classSourceFolder=$DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
classTestFolder=$DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$TESTFOLDER

JAVALOCATION=$(which java)


TESTCOUNT=$(cat $POSTESTS | wc -l)

echo "$TESTCOUNT tests in this test suite"

COUNTER=0

while read p; do

COUNTER=$(($COUNTER + 1))

echo ""
echo "Running test $COUNTER out of $TESTCOUNT: $p:"
command=($DEFECTS4JDIR/framework/projects/lib/junit-4.10.jar org.junit.runner.JUnitCore $p)
#echo Command: $command


OUTPUT=$($JAVALOCATION -cp .:$classSourceFolder:$classTestFolder:$DEFECTS4JDIR/framework/projects/lib/junit-4.11.jar org.junit.runner.JUnitCore $p)

#echo $OUTPUT
echo "Last 100 characters of the output:"
#echo "${OUTPUT:(-100)}"
echo $OUTPUT

if [[ $OUTPUT == *"OK (0 tests)"* ]]
then
  echo ""
  #echo "ERROR! IN THE CLASS: " $p
  #break
fi

if [[ $OUTPUT == *"Could not find class"* ]]
then
  echo ""
  #echo "ERROR! IN THE CLASS: " $p
  #break
fi

if [[ $OUTPUT == *"OK ("* ]]
then
  echo ""
  #echo "OK"
else
  echo ""
  #echo "ERROR! IN THE CLASS: " $p
  #break
fi


done <$POSTESTS

if [[ $TESTCOUNT == $COUNTER ]]
then
  echo ""
   #echo "Yey! All tests were executed successfully :D :D :D"
fi



