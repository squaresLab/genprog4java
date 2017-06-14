#!/bin/bash

#The purpose of this script is to test the coverage of a generated test sutie.

#Preconditions:
#There should be a folder called "$LOWERCASEPROJECT""$BUGNUMBER"Buggy/ in the path of fixed folders parameter.
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).
#Manually set the list of bugs to be tested in the list called bugs below.

#Output
#The output is a txt file with the output of running the coverage analysis of the test suite on each of the folders indicated. 

#Parameters:
# 1th param is the generation tool (Randoop or Evosuite)
# 2nd param is the path of the buggy folder, starting from the the D4J_HOME folder (Example: ExamplesCheckedOut or BugsWithAFix)
# 3rd param is the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)

#Example of usage:
#./checkCoverageOfGeneratedTestSuiteList.sh Randoop BugsWithAFix/ generatedTestSuitesForBugsWeFoundARepairFor/

if [ "$#" -ne 3 ]; then
    echo "This script should be run with 3 parameters"
	echo "1th param is the generation tool (Randoop or Evosuite)"
	echo "2nd param is the path of the buggy folder, starting from the the D4J_HOME folder (Example: ExamplesCheckedOut or BugsWithAFix)"
	echo "3rd param is the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)"

    exit 0
fi

RANDOOPOREVOSUITE="$1"
PATHOFFIXEDFOLDER="$2"
PATHOFSUITEFOLDER="$3"

#declare -a bugs=("Chart 1" "Chart 3" "Chart 5" "Chart 13" "Chart 21" "Chart 25" "Chart 26" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Closure 46" "Closure 66" "Closure 83" "Closure 86" "Closure 107" "Closure 115" "Closure 125" "Closure 126" "Lang 7" "Lang 10" "Lang 22" "Lang 39" "Lang 43" "Lang 45" "Lang 59" "Lang 63" "Math 7" "Math 8" "Math 18" "Math 20" "Math 24" "Math 28" "Math 29" "Math 40" "Math 49" "Math 50" "Math 73" "Math 80" "Math 81" "Math 82" "Math 85" "Math 95" "Time 19")
#declare -a bugs=("Chart 13")
declare -a bugs=("Chart 13" "Chart 1" "Chart 7" "Closure 115" "Closure 11" "Closure 126" "Closure 13" "Closure 21" "Closure 22" "Closure 31" "Closure 38" "Closure 45" "Closure 62" "Closure 63" "Closure 64" "Lang 10" "Lang 33" "Lang 44" "Lang 51" "Lang 58" "Lang 59" "Lang 63" "Math 28" "Math 29" "Math 2" "Math 40" "Math 49" "Math 50" "Math 5" "Math 62" "Math 75" "Math 78" "Math 80" "Math 81" "Math 82" "Math 85" "Math 8" "Time 7")

for i in "${bugs[@]}"
do

PROJECT=${i% *}
BUGNUMBER=${i#* }
LOWERCASEPROJECT=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`
COM="./checkCoverageOfGeneratedTestSuite.sh $i $RANDOOPOREVOSUITE $D4J_HOME/$PATHOFFIXEDFOLDER/"$LOWERCASEPROJECT""$BUGNUMBER"Buggy/ $D4J_HOME/$PATHOFSUITEFOLDER/" 
echo "$COM"
eval $COM




done
