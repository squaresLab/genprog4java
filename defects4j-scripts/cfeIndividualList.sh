#!/bin/bash

#The purpose of this script is to Create, Fix and Test test suites for several defects4j bugs.

#Preconditions:
#There should be a folder called generatedTestSuites in the defects4j folder where the test suites and their output will be stored.
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).
#You should manually modify the list of bugs to be ran in the array called "bugs"

#Output
#The output is a txt file with the output of testing the test suite on the folder indicated. The name of the txt file is: EvaluatingTestSuite"$PROJECT"-"$BUGNUMBER"f-evosuite-branch."$SEED".tar.bz2On"$LOWERCASEPACKAGE""$BUGNUMBER"BuggyOutput.txt and it is located in $D4J_HOME/generatedTestSuites/$IDENTIFIER/"$PROJECT"/evosuite-branch/"$SEED"/ for each of the d4j bugs

#Parameters:
# 1th param is the generation tool (Randoop or Evosuite)
# 2th param is the budget of time in seconds the tool has to generate the test suite
# 3th param is weather you want to run only sections of the script: C=create, F=fix. You can run: CF, C, F
# 4th param is the name of the folder the test suite will be stored in. This is located in $D4J_HOME/generatedTestSuites/ . Example: September21

#Example of usage:
#./cfeIndividualList.sh Evosuite 180 CF AllPublic /home/mau/Research/defects4j/ExamplesCheckedOut/math82.patch


RANDOOPOREVOSUITE="$1"
BUDGET="$2"
CFE="$3"
IDENTIFIER="$4"
PATCHFILE="$5"

if [ "$#" -lt 4 ]; then
    echo "This script should be run with 4 parameters: "
	echo " 1th param is the generation tool (Randoop or Evosuite)"
	echo "2th param is the budget of time in seconds the tool has to generate the test suite"
	echo "3th param is weather you want to run only sections of the script: C=create, F=fix. You can run: CF, C, F"
	echo "4th param is the name of the folder the test suite will be stored in. This is located in $D4J_HOME/generatedTestSuites/. Example: September21"
	echo "5th param is an optional patch file Example: /path/to/patch.txt"
    exit 0
fi

LOWERCASERANDOOPOREVOSUITE=`echo $RANDOOPOREVOSUITE | tr '[:upper:]' '[:lower:]'`

mkdir $D4J_HOME/generatedTestSuites/$IDENTIFIER
rm -f $D4J_HOME/generatedTestSuites/$IDENTIFIER/resultsEvaluatingSeveralTestSuites.txt
touch $D4J_HOME/generatedTestSuites/$IDENTIFIER/resultsEvaluatingSeveralTestSuites.txt

#Change this list to the Bugs you want to evaluate
#All bugs with fix found (TRPAutoRepair):
#declare -a bugs=("Chart 1" "Chart 3" "Chart 5" "Chart 13" "Chart 21" "Chart 25" "Chart 26" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Closure 46" "Closure 66" "Closure 83" "Closure 86" "Closure 107" "Closure 115" "Closure 125" "Closure 126" "Lang 7" "Lang 10" "Lang 22" "Lang 39" "Lang 43" "Lang 45" "Lang 59" "Lang 63" "Math 7" "Math 8" "Math 18" "Math 20" "Math 24" "Math 28" "Math 29" "Math 40" "Math 49" "Math 50" "Math 73" "Math 80" "Math 81" "Math 82" "Math 85" "Math 95" "Time 19")
#Allbugs with a fix found (GenProg) Remaining from the list above:
declare -a bugs=("Math 82")
#sprecific ones
#declare -a bugs=("Chart 13" "Chart 1" "Chart 7" "Closure 115" "Closure 11" "Closure 126" "Closure 13" "Closure 21" "Closure 22" "Closure 31" "Closure 38" "Closure 45" "Closure 62" "Closure 63" "Closure 64" "Lang 10" "Lang 33" "Lang 44" "Lang 51" "Lang 58" "Lang 59" "Lang 63" "Math 28" "Math 29" "Math 2" "Math 40" "Math 49" "Math 50" "Math 5" "Math 62" "Math 75" "Math 78" "Math 80" "Math 81" "Math 82" "Math 85" "Math 8" "Time 7")

## now loop through the above array
for i in "${bugs[@]}"
do
  echo "Si:"
  echo ""

  COM="./cfeIndividual.sh "$i" $1 $2 $3 $4 $5" 
  echo "$COM"
  eval $COM
  echo ""
done
