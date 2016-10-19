#!/bin/bash

# Example usage, local for Mau
#./eraseAndRegenerateBugs.sh /home/mausoto/genprog4j/ /home/mausoto/defects4j/
GENPROGDIR="$1"
DEFECTS4JDIR="$2"

#rm -rf "$DEFECTS4JDIR"/testGenerated/
#rm -rf /tmp/*


#rm -f $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt
#touch $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt

#STARTAT=1

#for (( projectNumb=2 ; projectNumb<=2 ; projectNumb++ ))
#do
#  case "$projectNumb" in
#  "1" )
#    PROJECT="Math"
#    NUMBEROFBUGS=106
#    STARTAT=1

#  ;;
#  "2" ) 
#    PROJECT="Lang"
##    NUMBEROFBUGS=65
#    NUMBEROFBUGS=45
#    STARTAT=45
#  ;;
#  "3" ) 
#    PROJECT="Chart"
#    NUMBEROFBUGS=26
#    STARTAT=1
#  ;;
#  "4" ) 
#    PROJECT="Time"
#    #NUMBEROFBUGS=27
#    NUMBEROFBUGS=7
#    STARTAT=7
#  ;;
#  "5" ) 
#    PROJECT="Closure"
#    NUMBEROFBUGS=133
#    STARTAT=1
#  ;;
#  esac

#  for (( bug=$STARTAT ; bug<=$NUMBEROFBUGS ; bug++ ))
#  do

#erase before creating it again
declare -a arr=("chart21Buggy" "lang59Buggy" "math24Buggy" "math29Buggy" "math49Buggy" "time19Buggy" "closure125Buggy" "closure86Buggy" "math7Buggy" "closure83Buggy" "lang45Buggy" "chart3Buggy" "chart5Buggy" "lang39Buggy" "math40Buggy" "closure66Buggy" "lang43Buggy" "chart1Buggy" "math18Buggy" "lang10Buggy" "math20Buggy" "lang7Buggy" "math73Buggy" "math95Buggy" "math82Buggy" "closure46Buggy" "lang22Buggy" "chart26Buggy" "chart25Buggy" "lang63Buggy" "chart13Buggy" "closure107Buggy" "closure115Buggy" "closure126Buggy" "closure13Buggy" "closure19Buggy" "closure21Buggy" "closure22Buggy" "math28Buggy" "math50Buggy" "math80Buggy" "math81Buggy" "math85Buggy" "math8Buggy")
for i in "${arr[@]}"
do
COM="rm -fr $DEFECTS4JDIR/ExamplesCheckedOut/$i" 
echo $COM
eval $COM
done

  
declare -a arr=("Chart 21" "Lang 59" "Math 24" "Math 29" "Math 49" "Time 19" "Closure 125" "Closure 86" "Math 7" "Closure 83" "Lang 45" "Chart 3" "Chart 5" "Lang 39" "Math 40" "Closure 66" "Lang 43" "Chart 1" "Math 18" "Lang 10" "Math 20" "Lang 7" "Math 73" "Math 95" "Math 82" "Closure 46" "Lang 22" "Chart 26" "Chart 25" "Lang 63" "Chart 13" "Closure 107" "Closure 115" "Closure 126" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Math 28" "Math 50" "Math 80" "Math 81" "Math 85" "Math 8")

for i in "${arr[@]}"
do

#    $GENPROGDIR/defects4j-scripts/prepareBug.sh $PROJECT $bug $GENPROGDIR $DEFECTS4JDIR generated 100 $DEFECTS4JDIR/ExamplesCheckedOut/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /usr/lib/jvm/java-8-oracle/
    COM="$GENPROGDIR/defects4j-scripts/prepareBug.sh $i $GENPROGDIR $DEFECTS4JDIR humanMade 100 $DEFECTS4JDIR/ExamplesCheckedOut/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /usr/lib/jvm/java-8-oracle/"
    echo $COM
    eval $COM

#    echo ""
#    echo "prepareBug.sh finished executing"

#     ./testGeneratedSuite.sh $PROJECT $bug $GENPROGDIR $DEFECTS4JDIR allHuman 100 $DEFECTS4JDIR/ExamplesCheckedOut/ 1 1 false /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /usr/lib/jvm/java-8-oracle/ Randoop
#    $GENPROGDIR/defects4j-scripts/runTestSuite.sh $PROJECT $bug  $GENPROGDIR $DEFECTS4JDIR > $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites/resultsFromRunningGenereatedTestSuites"$PROJECT""$bug".txt

#    echo ""
#    echo "Generated test suite has been ran on the buggy code."
#    echo "Results of $PROJECT $bug stored in: $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites/resultsFromRunningGenereatedTestSuites"$PROJECT""$bug".txt"
#    echo ""

done

#  done
#done








