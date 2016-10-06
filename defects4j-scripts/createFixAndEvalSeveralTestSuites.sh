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
CFE="$7"
IDENTIFIER="$8"


#./createFixAndEvalSeveralTestSuites.sh /home/mausoto/genprog4java/ /home/mausoto/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64  /usr/lib/jvm/java-8-oracle/ Randoop 180 CF September21

if [ "$#" -ne 8 ]; then
    echo "This script should be run with 7 parameters: For example: ./createFixAndEvalSeveralTestSuites.sh /home/mausoto/genprog4java/ /home/mausoto/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64  /usr/lib/jvm/java-8-oracle/ Randoop 180 CFE TestIfWeFindNow1"
    exit 0
fi

LOWERCASERANDOOPOREVOSUITE=`echo $RANDOOPOREVOSUITE | tr '[:upper:]' '[:lower:]'`

mkdir $DEFECTS4JDIR/generatedTestSuites/$LOWERCASERANDOOPOREVOSUITE/$IDENTIFIER
rm -f $DEFECTS4JDIR/generatedTestSuites/$LOWERCASERANDOOPOREVOSUITE/$IDENTIFIER/resultsEvaluatingSeveralTestSuites.txt
touch $DEFECTS4JDIR/generatedTestSuites/$LOWERCASERANDOOPOREVOSUITE/$IDENTIFIER/resultsEvaluatingSeveralTestSuites.txt

#Change this list to the Bugs you want to evaluate
#All bugs with fix found:
#declare -a arr=("Chart 21" "Lang 59" "Math 24" "Math 29" "Math 49" "Time 19" "Closure 125" "Closure 86" "Math 7" "Closure 83" "Lang 45" "Chart 3" "Chart 5" "Lang 39" "Math 40" "Closure 66" "Lang 43" "Chart 1" "Math 18" "Lang 10" "Math 20" "Lang 7" "Math 73" "Math 95" "Math 82" "Closure 46" "Lang 22" "Chart 26" "Chart 25" "Lang 63" "Chart 13" "Closure 107" "Closure 115" "Closure 126" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Math 28" "Math 50" "Math 80" "Math 81" "Math 85" "Math 8")
#bugs we havent found a fix for yet
#declare -a arr=("Closure 13" "Closure 21" "Closure 83" "Closure 107" "Closure 115" "Closure 125" "Closure 126" "Math 18" "Math 28" "Math 29" "Math 80" "Math 81" "Math 20")
#specific ones
declare -a arr=("Time 19")
## now loop through the above array
for i in "${arr[@]}"
do
  echo "Si:"
  echo ""
  COM="./cfeIndividual.sh "$i" $1 $2 $3 $4 $5 $6 $7 $8 "
#&>> $DEFECTS4JDIR/generatedTestSuites/$LOWERCASERANDOOPOREVOSUITE/$IDENTIFIER/resultsEvaluatingSeveralTestSuites.txt"
  echo "$COM"
  eval $COM
  echo ""
done
