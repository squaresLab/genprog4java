#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "This script should be run with 1 parameters: Path where the lastLinesOfAllLogs.txt is$"
    exit 0
fi


dirToWork="$1"
cd "$dirToWork"
  rm -f Results.csv
  lastLinesFile="lastLinesOfAllLogs.txt"
  lookingForRepair=0
  variantNum="Check Manually"
  while read lineInLastLinesFile
  do
    if [[ $lineInLastLinesFile == *"variant"* ]]
	then
      tmp=${lineInLastLinesFile#*variant}
      variantNumber=${tmp%)*}
	  
      #variantNum=$(echo $lineInLastLinesFile | cut -d variant -f 2 | cut -d ')' -f 1)
	fi
    if [[ $lineInLastLinesFile == "Seed"* ]]
    then
	  variantNum="Check Manually"
	  if [[ $lookingForRepair == 0 ]]
      then	  
        lookingForRepair=1
      else 
	    echo "Not found" >> Results.csv
        #printf '%s\n' "Not found" | paste -sd " " >> Results.csv
      fi
	fi
    if [[ $lineInLastLinesFile == "Repair Found:"* ]]
    then 
      lookingForRepair=0
      echo " $variantNumber" >> Results.csv
	  #printf '%s\n' $variantNumber | paste -sd " " >> Results.csv
    fi
  done < $lastLinesFile
  if [[ $lookingForRepair == 0 ]]
  then	  
    echo " $variantNumber" >> Results.csv
  else 
    echo "Not found" >> Results.csv
  fi
cd ..