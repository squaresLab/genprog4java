#!/bin/bash

#The purpose of this script is to answer weather a particular test suite covers the changes performed by the human in the human fix and by the automatic approach when fixed by genprog.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).
#The variable GP4J_HOME should be directed to the folder where genprog is installed

#Output
#Outputs if the human and generated changes are covered by the test suite in $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt

#Parameters:
# 1th param is the generation tool (Randoop or Evosuite)
# 2th param is the path where the test suite is located
# 3th param is the path of the folder where the bugs will be checked out. Starting in $D4J_HOME
# 4th param is the path of the folder where the patches are. Starting in $D4J_HOME

#Example of usage:
#./applyPatchAndTestCoverageOfChanges.sh Evosuite /generatedTestSuites/Evosuite30MinGenProgFixes/testSuites/ ExamplesCheckedOut patchesGenerated/JavaRepair-results/GenProgPatches/

RANDOOPOREVOSUITE="$1"
PATHOFSUITEFOLDER="$2"
PATHTOCHECKOUTFOLDERS="$3"
PATHOFPATCHES="$4"

if [ "$#" -ne 4 ]; then
    echo "This script should be run with 3 parameters: "
	echo "1th param is the generation tool (Randoop or Evosuite)"
	echo "2th param is the path where the test suite is located"
	echo "3th param is the path of the folder where the bugs will be checked out. Starting in $D4J_HOME"
	echo "4th param is the path of the folder where the patches are. Starting in $D4J_HOME"
    exit 0
fi

rm $D4J_HOME/$PATHTOCHECKOUTFOLDERS/CoverageOfGeneratedRepairs.txt
cd $D4J_HOME/$PATHOFPATCHES
rm listOfPatches.txt
ls -d *.diff > listOfPatches.txt

patches=./listOfPatches.txt
while read patch
do
  defect=$(echo $patch| cut -d'_' -f 1)
  project=$(echo $defect | sed 's/[^a-z]*//g')
  bugNum=$(echo $defect | sed 's/[^0-9]*//g')
  #capitalize first letter
  projectUC="$(tr '[:lower:]' '[:upper:]' <<< ${project:0:1})${project:1}"
  
  rm -rf $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$project"$bugNum"Fixed
  sleep 0
  echo "$D4J_HOME/$PATHTOCHECKOUTFOLDERS/$project"$bugNum"Fixed"
  defects4j checkout -p $projectUC -v "$bugNum"b -w $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$project"$bugNum"Fixed
  if [ $project = "chart" ]
  then
    srcFold="source"
  elif [ $project = "closure" ]
  then
    srcFold="src"  
  elif [ $project = "lang" ]
  then
    srcFold="src/java/"
  elif [ $project = "math" ] || [ $project = "time" ]
  then
    srcFold="src/main/java/"
  fi
  
  cd $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$project"$bugNum"Fixed/$srcFold
 # echo "$D4J_HOME/$PATHOFPATCHES/$patch"
  patch -p10 -i $D4J_HOME/$PATHOFPATCHES/$patch
  echo "Patch: $patch" >> $D4J_HOME/$PATHTOCHECKOUTFOLDERS/CoverageOfGeneratedRepairs.txt
  echo "$projectUC $bugNum"
  cd $GP4J_HOME/defects4j-scripts/
  COM="./isTestSuiteCoveringChanges.sh $projectUC $bugNum $1 $2 $3 " 
  echo "$COM"
  eval $COM
done < $patches
