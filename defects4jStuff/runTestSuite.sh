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

JAVALOCATION=$(which java)


TESTCOUNT=$(cat $POSTESTS | wc -l)

echo This test suite has $TESTCOUNT tests

COUNTER=0

while read p; do

COUNTER=$(($COUNTER + 1))

  echo Running test $COUNTER out of $TESTCOUNT: $p
OUTPUT=$($JAVALOCATION -cp .:$DEFECTS4JDIR/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/"$LOWERCASEPACKAGE"AllSourceClasses.jar:$DEFECTS4JDIR/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/"$LOWERCASEPACKAGE"AllTestClasses.jar:$DEFECTS4JDIR/framework/projects/lib/junit-4.11.jar org.junit.runner.JUnitCore $p)

echo $OUTPUT

if [[ $OUTPUT == *"OK (0 tests)"* ]]
then
  echo "ERROR! IN THE CLASS: " $p
  break
fi

if [[ $OUTPUT == *"Could not find class"* ]]
then
  echo "ERROR! IN THE CLASS: " $p
  break
fi

if [[ $OUTPUT == *"OK ("* ]]
then
  echo "OK"
else
  echo "ERROR! IN THE CLASS: " $p
  break
fi


done <$POSTESTS

if [[ $TESTCOUNT == $COUNTER ]]
then
   echo "Yey! All tests were executed successfully :D :D :D"
fi



