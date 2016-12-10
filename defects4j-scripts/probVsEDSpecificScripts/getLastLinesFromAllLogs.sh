#!/bin/sh

# ./getLastLinesFromAllLogs.sh Math 2 /home/mausoto/defects4j/ExamplesCheckedOut/math2Buggy/

PROJECT=$1
BUGNUMBER=$2
PATHOFBUG=$3

if [ "$#" -ne 3 ]; then
    echo "This script should be run with 3 parameters: Project name, bug number, Path$"
    exit 0
fi

cd $PATHOFBUG
rm -f lastLinesOfAllLogs.txt
touch lastLinesOfAllLogs.txt

for (( SEED=1 ; SEED<=20 ; SEED++ ))
do

echo "" >> lastLinesOfAllLogs.txt 
echo "" >> lastLinesOfAllLogs.txt 
echo "Seed $SEED:" >> lastLinesOfAllLogs.txt
FILENAME=""
FILENAME+=log
FILENAME+=$PROJECT
FILENAME+=$BUGNUMBER
FILENAME+=Seed
FILENAME+=$SEED
FILENAME+=.txt 
tail $FILENAME >> lastLinesOfAllLogs.txt

done

