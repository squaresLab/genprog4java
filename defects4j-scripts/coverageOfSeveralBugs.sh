#!/bin/bash

# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
# 4th param is the folder where the java 7 instalation is located
# 5th param is the path of the buggy folder 
# 6th param is the path where

#VM:
#./coverageOfSeveralBugs.sh /home/ubuntu/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /home/ubuntu/defects4j/generatedTestSuitesForBugsWeFoundARepairFor


if [ "$#" -ne 3 ]; then
    echo "This script should be run with 3 parameters"
    exit 0
fi


DEFECTS4JDIR="$1"
DIROFJAVA7="$2"
PATHOFSUITEFOLDER="$3"



declare -a arr=("Chart 1" "Chart 3" "Chart 5" "Chart 13" "Chart 21" "Chart 25" "Chart 26" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Closure 46" "Closure 66" "Closure 83" "Closure 86" "Closure 107" "Closure 115" "Closure 125" "Closure 126" "Lang 7" "Lang 10" "Lang 22" "Lang 39" "Lang 43" "Lang 45" "Lang 59" "Lang 63" "Math 7" "Math 8" "Math 18" "Math 20" "Math 24" "Math 28" "Math 29" "Math 40" "Math 49" "Math 50" "Math 73" "Math 80" "Math 81" "Math 82" "Math 85" "Math 95" "Time 19")
#declare -a arr=("Lang 63" "Lang 45" "Lang 39")

for i in "${arr[@]}"
do

PROJECT=${i% *}
BUGNUMBER=${i#* }
LOWERCASEPROJECT=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

 #run_bug_detection.pl -p $PROJECT -d $DEFECTS4JDIR/generatedTestSuitesForBugsWeFoundARepairFor/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -o out_dir [-f include_file_pattern] [-v version_id] [-t tmp_dir] [-D]
COM="./checkCoverageOfGeneratedTestSuites.sh $i /home/mausoto/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /home/mausoto/defects4j/BugsWithAFix/"$LOWERCASEPROJECT""$BUGNUMBER"Buggy/ $PATHOFSUITEFOLDER/ &>> $PATHOFSUITEFOLDER/Coverage"$LOWERCASEPROJECT""$BUGNUMBER"log.txt"
#COM="./defects4j coverage -w $PATHOFFIXEDFOLDER/ -s $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 &>> $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"CoverageHumanFixedlog.txt"
 
echo "$COM"
eval $COM




done
