#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "This script should be run with 1 parameters: Path where the lastLinesOfAllLogs.txt is$"
    exit 0
fi

cd "$1"
lastLinesFile=lastLinesOfAllLogs.txt
lookingForRepair=0
while read lineInLastLinesFile
do

if [[ $lineInLastLinesFile == "Seed"* ]] && [[ $lookingForRepair == 0 ]]
then
  lookingForRepair=1
else if [[ $lineInLastLinesFile == "Seed"* ]] &&  [[ $lookingForRepair == 1 ]]
then
printf '%s\n' "Not found" | paste -sd " " >> Results.csv
fi

if [[ $lineInLastLinesFile == "Repair Found:"* ]]
then 
lookingForRepair = 0
tmp=${lineInLastLinesFile#*variant}
variantNumber=${tmp%)*}
printf '%s\n' $variantNumber | paste -sd " " >> Results.csv
fi
done < $lastLinesFile
