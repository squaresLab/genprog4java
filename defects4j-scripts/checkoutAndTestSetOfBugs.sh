#!/bin/bash

# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
# 4th param is the folder where the java 7 instalation is located
# 5th param is the path of the buggy folder 
# 6th param is the path where the test suite is located

#VM:
#./checkoutAndTestSetOfBugs.sh /home/ubuntu/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /home/ubuntu/defects4j/generatedTestSuitesForBugsWeFoundARepairFor


if [ "$#" -ne 3 ]; then
    echo "This script should be run with 3 parameters"
    exit 0
fi

DEFECTS4JDIR="$1"
DIROFJAVA7="$2"
PATHOFSUITEFOLDER="$3"

export JAVA_HOME=$DIROFJAVA7
export JRE_HOME=$DIROFJAVA7/jre
export PATH=$DIROFJAVA7/bin/:$PATH

cd $DEFECTS4JDIR/framework/bin
echo ""
SEED=1

#rm -f $PATHOFSUITEFOLDER/UniqueLog.txt

declare -a arr=("Chart 21" "Lang 59" "Math 24" "Math 29" "Math 49" "Time 19" "Closure 125" "Closure 86" "Math 7" "Closure 83" "Lang 45" "Chart 3" "Chart 5" "Lang 39" "Math 40" "Closure 66" "Lang 43" "Chart 1" "Math 18" "Lang 10" "Math 20" "Lang 7" "Math 73" "Math 95" "Math 82" "Closure 46" "Lang 22" "Chart 26" "Chart 25" "Lang 63" "Chart 13" "Closure 107" "Closure 115" "Closure 126" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Math 28" "Math 50" "Math 80" "Math 81" "Math 85" "Math 8")
#declare -a arr=("Lang 39" "Lang 45" "Lang 63")
for i in "${arr[@]}"
do

PROJECT=${i% *}
BUGNUMBER=${i#* }
LOWERCASEPROJECT=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

PATHOFBUGGYFOLDER="$DEFECTS4JDIR/ExamplesCheckedOut/"$LOWERCASEPROJECT""$BUGNUMBER"Buggy/"
COM0="./defects4j checkout -p $PROJECT -v "$BUGNUMBER"b -w $PATHOFBUGGYFOLDER &>> $PATHOFSUITEFOLDER/UniqueLog.txt"
echo "$COM0"
eval $COM0

#run_bug_detection.pl -p $PROJECT -d $DEFECTS4JDIR/generatedTestSuitesForBugsWeFoundARepairFor/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -o out_dir [-f include_file_pattern] [-v version_id] [-t tmp_dir] [-D]
COM="./defects4j test -s $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w $PATHOFBUGGYFOLDER &>> $PATHOFSUITEFOLDER/UniqueLog.txt"
 
echo "$COM"
echo "Running... Log file located in $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"log.txt"
eval $COM

done


