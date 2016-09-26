#!/bin/sh

# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: /home/mau/Research/genprog4java/ )
# 4td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
#5th param is the folder where the java 7 instalation is located
#6th param is the folder where the java 8 instalation is located
#7th param is the generation tool (Randoop or Evosuite)
#8th param is the budget

GENPROGDIR="$1"
DEFECTS4JDIR="$2"
DIROFJAVA7="$3"
DIROFJAVA8="$4"
RANDOOPOREVOSUITE="$5"
BUDGET="$6"

#./evaluateSeveralTestSuites.sh /home/mausoto/genprog4java/ /home/mausoto/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64  /usr/lib/jvm/java-8-oracle/ Randoop 1

rm -f $DEFECTS4JDIR/resultsEvaluatingSeveralTestSuites.txt
touch $DEFECTS4JDIR/resultsEvaluatingSeveralTestSuites.txt

#Change this list to the Bugs you want to evaluate
declare -a arr=("Math 8" "Math 28" "Math 29" "Math 80" "Math 81" "Chart 25" "Chart 26" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Closure 46" "Closure 66" "Closure 83" " Closure 86" "Closure 107" "Closure 115" "Closure 125" "Closure 126")
## now loop through the above array
for i in "${arr[@]}"
do
  echo "Si:"
  echo ""
  COM="./testGeneratedSuite.sh "$i" $1 $2 $3 $4 $5 $6 >> $DEFECTS4JDIR/resultsEvaluatingSeveralTestSuites.txt"
  echo "$COM"
  eval $COM
  echo ""
done
