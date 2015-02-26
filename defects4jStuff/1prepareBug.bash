#!/bin/bash
# 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)

#copy these files to the source control
cd ~/Research/
cp 1prepareBug.bash ./genprog4java/defects4jStuff/
cp 2prepareBug.bash ./genprog4java/defects4jStuff/
cp -r ./defects4j/ExamplesCheckedOut/Utilities/ ./genprog4java/defects4jStuff/


#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE="${1,,}"

if [ $LOWERCASEPACKAGE = "chart" ]; then
  JAVADIR=tests/org/jfree #CHECK BECAUSE THERE ARE NESTED FOLDERS
elif [ $LOWERCASEPACKAGE = "closure" ]; then
  JAVADIR=test/com/google #CHECK BECAUSE THERE ARE NESTED FOLDERS
elif [ $LOWERCASEPACKAGE = "lang" ]; then
  JAVADIR=src/test/java/org/apache/commons/lang3 
elif [ $LOWERCASEPACKAGE = "math" ]; then 
  JAVADIR=src/test/java/org/apache/commons/math3
elif [ $LOWERCASEPACKAGE = "time" ]; then
  JAVADIR=src/test/java/org/joda/time
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
#defects4j compile

#Run the buggy code
#defects4j test

#Create the file with all the tests names in a file
#find $JAVADIR/ -name "*.java" | tr / . | rev | cut -c 6- | rev  &> ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/pos.tests 
#PREVIOUS CALL REPLACED BY:
#copy the standard list of positive tests to the current bug directory
cp /home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/"$LOWERCASEPACKAGE"Pos.tests /home/mau/Research/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

#Create a file neg.tests
touch /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/neg.tests

#Print bug info
defects4j info -p $1 -v $2

gedit /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/pos.tests 
gedit /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/neg.tests 

#I then go to pos.tests, move the failing tests that appear in the "Root cause in triggering tests" in the console, to the neg.tests
echo Yo Mau, 
echo 1. Go to ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy, look for pos.tests, move the failing tests that appear in the \"Root cause in triggering tests\" in the console, to the neg.tests

echo Finally, run ./2prepareBug.bash "$1" "$2"







