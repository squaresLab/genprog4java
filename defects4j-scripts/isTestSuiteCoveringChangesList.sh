#!/bin/bash

#The purpose of this script is to answer weather a particular test suite covers the changes performed by the human in the human fix and by the automatic approach when fixed by genprog.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).

#Output
#Outputs if the human and generated changes are covered by the test suite in $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt

#Parameters:
# 1th param is the generation tool (Randoop or Evosuite)
# 2th param is the path where the test suite is located
# 3th param is the path of the folder where the bugs will be checked out. Starting in $D4J_HOME

#Example of usage:
#./isTestSuiteCoveringChangesList.sh Evosuite generatedTestSuites/Evosuite30MinTRPFixes/testSuites/ ExamplesCheckedOut

RANDOOPOREVOSUITE="$1"
PATHOFSUITEFOLDER="$2"
PATHTOCHECKOUTFOLDERS="$3"

if [ "$#" -ne 3 ]; then
    echo "This script should be run with 3 parameters: "
	echo "1th param is the generation tool (Randoop or Evosuite)"
	echo " 2th param is the path where the test suite is located"
	echo " 3th param is the path of the folder where the bugs will be checked out. Starting in $D4J_HOME"
    exit 0
fi

LOWERCASERANDOOPOREVOSUITE=`echo $RANDOOPOREVOSUITE | tr '[:upper:]' '[:lower:]'`

rm -f $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt

#Change this list to the Bugs you want to evaluate
#All bugs with fix found:
#declare -a bugs=("Chart 1" "Chart 3" "Chart 5" "Chart 13" "Chart 21" "Chart 25" "Chart 26" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Closure 46" "Closure 66" "Closure 83" "Closure 86" "Closure 107" "Closure 115" "Closure 125" "Closure 126" "Lang 7" "Lang 10" "Lang 22" "Lang 39" "Lang 43" "Lang 45" "Lang 59" "Lang 63" "Math 7" "Math 8" "Math 18" "Math 20" "Math 24" "Math 28" "Math 29" "Math 40" "Math 49" "Math 50" "Math 73" "Math 80" "Math 81" "Math 82" "Math 85" "Math 95" "Time 19")
#sprecific ones
declare -a bugs=("Closure 115")

## now loop through the above array
for i in "${bugs[@]}"
do
  COM="./isTestSuiteCoveringChanges.sh "$i" $1 $2 $3 $4 " 
  echo "$COM"
  eval $COM
  echo ""
done
echo "Results in $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt"
