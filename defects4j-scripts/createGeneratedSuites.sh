#!/bin/bash

# Example usage, local for Mau
#./createGeneratedSuites.sh /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ 

GENPROGDIR="$1"
DEFECTS4JDIR="$2"

#rm -rf "$DEFECTS4JDIR"/testGenerated/
#rm -rf /tmp/*


rm -f $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt
touch $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites.txt

STARTAT=1

for (( projectNumb=2 ; projectNumb<=2 ; projectNumb++ ))
do
  case "$projectNumb" in
  "1" )
    PROJECT="Math"
    NUMBEROFBUGS=106
    STARTAT=1

  ;;
  "2" ) 
    PROJECT="Lang"
#    NUMBEROFBUGS=65
    NUMBEROFBUGS=45
    STARTAT=45
  ;;
  "3" ) 
    PROJECT="Chart"
    NUMBEROFBUGS=26
    STARTAT=1
  ;;
  "4" ) 
    PROJECT="Time"
    #NUMBEROFBUGS=27
    NUMBEROFBUGS=7
    STARTAT=7
  ;;
  "5" ) 
    PROJECT="Closure"
    NUMBEROFBUGS=133
    STARTAT=1
  ;;
  esac

  for (( bug=$STARTAT ; bug<=$NUMBEROFBUGS ; bug++ ))
  do
  
    $GENPROGDIR/defects4j-scripts/prepareBug.sh $PROJECT $bug $GENPROGDIR $DEFECTS4JDIR humanMade 100 $DEFECTS4JDIR/ExamplesCheckedOut/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /usr/lib/jvm/java-8-oracle/

#    echo ""
#    echo "prepareBug.sh finished executing"

#     ./testGeneratedSuite.sh $PROJECT $bug $GENPROGDIR $DEFECTS4JDIR allHuman 100 $DEFECTS4JDIR/ExamplesCheckedOut/ 1 1 false /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /usr/lib/jvm/java-8-oracle/ Randoop
#    $GENPROGDIR/defects4j-scripts/runTestSuite.sh $PROJECT $bug  $GENPROGDIR $DEFECTS4JDIR > $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites/resultsFromRunningGenereatedTestSuites"$PROJECT""$bug".txt

#    echo ""
#    echo "Generated test suite has been ran on the buggy code."
#    echo "Results of $PROJECT $bug stored in: $DEFECTS4JDIR/ResultsFromRunningGenereatedTestSuites/resultsFromRunningGenereatedTestSuites"$PROJECT""$bug".txt"
#    echo ""

  done
done








