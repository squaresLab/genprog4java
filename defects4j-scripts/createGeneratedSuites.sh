#!/bin/bash

# Example usage, local for Mau
#./createGeneratedSuites.sh /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ 

GENPROGDIR="$1"
DEFECTS4JDIR="$2"

rm -rf "$DEFECTS4JDIR"/testGenerated/
rm -rf /tmp/*

rm -rf $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites
mkdir $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites

for (( projectNumb=1 ; projectNumb<=5 ; projectNumb++ ))
do
  case "$projectNumb" in
  "1" )
    PROJECT="Math"
    NUMBEROFBUGS=106
  ;;
  "2" ) 
    PROJECT="Lang"
    NUMBEROFBUGS=65
  ;;
  "3" ) 
    PROJECT="Chart"
    NUMBEROFBUGS=26
  ;;
  "4" ) 
    PROJECT="Time"
    NUMBEROFBUGS=27
  ;;
  "5" ) 
    PROJECT="Closure"
    NUMBEROFBUGS=133
  ;;
  esac

  for (( bug=1 ; bug<=$NUMBEROFBUGS ; bug++ ))
  do
  
    $GENPROGDIR/defects4j-scripts/prepareBug.sh $PROJECT $bug $GENPROGDIR $DEFECTS4JDIR generated 100 $DEFECTS4JDIR/ExamplesCheckedOut/

    echo ""
    echo "prepareBug.sh finished executing"

    $GENPROGDIR/defects4j-scripts/runTestSuite.sh $PROJECT $bug  $GENPROGDIR $DEFECTS4JDIR > $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites/resultsFromRunningGenereatedTestSuites"$PROJECT""$bug".txt

    echo ""
    echo "Generated test suite has been ran on the buggy code."
    echo "Results of $PROJECT $bug stored in: $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites/resultsFromRunningGenereatedTestSuites"$PROJECT""$bug".txt"
    echo ""

  done
done








