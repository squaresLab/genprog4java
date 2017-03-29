#!/bin/bash 

cd /home/mausoto/probDefects4j/

output=/home/mausoto/probDefects4j/RepairsFoundRegVsProbExperiment.csv
rm -f $output

declare -a arr=("ProbAllMut" "ProbGPOnly" "ProbPAROnly" "RegAllMut" "RegGPOnly" "RegPAROnly")
for i in "${arr[@]}"
do
cd defects4j$i/defects4j/ExamplesCheckedOut

ls -d *$i > folders.txt
folderNames=./folders.txt
while read folderName
do
  echo "$folderName" >> $output
  /home/mausoto/probGenProg/genprog4java/defects4j-scripts/probVsEDSpecificScripts/convertToCSVtheLastLines.sh $folderName $output
done < $folderNames
cd ../../..
done
