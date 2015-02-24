#!/bin/bash
# 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)

#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE="${1,,}"

if [ $LOWERCASEPACKAGE = "math" ]; then 
  JAVADIR=apache/commons/math3
elif [ $LOWERCASEPACKAGE = "lang" ]; then
  JAVADIR=apache/commons/lang3 #FILL THIS
elif [ $LOWERCASEPACKAGE = "chart" ]; then
  JAVADIR=chart #FILL THIS
elif [ $LOWERCASEPACKAGE = "closure" ]; then
  JAVADIR=closure #FILL THIS
elif [ $LOWERCASEPACKAGE = "time" ]; then
  JAVADIR=joda/time
fi

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:~/Research/defects4j/framework/bin

#Print info about the project
#defects4j info -p $1

#Print info about this bug in particular
#defects4j info -p $1 -v $2

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$2"b -w ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

#Go to the created folder
cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

#Compile the buggy code
defects4j compile

#Run the buggy code
#defects4j test

#Go inside the project
cd src/test/java/

#Create the file with all the tests names in a file
find org/$JAVADIR/ -name "*.java" | tr / . | rev | cut -c 6- | rev  &> ../../../pos.tests 

#Create a file neg.tests
touch ../../../neg.tests

#Print bug info
defects4j info -p $1 -v $2

#I then go to pos.tests, move the failing tests that appear in the "Root cause in triggering tests" in the console, to the neg.tests
echo Yo Mau, 
echo 1. Go to ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy, look for pos.tests, move the failing tests that appear in the \"Root cause in triggering tests\" in the console, to the neg.tests

echo Finally, run ./2prepareBug.bash "$1" "$2"







