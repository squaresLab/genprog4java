#!/bin/sh

# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
# 4th param is the folder where the java 7 instalation is located
# 5th param is the path of the buggy folder 
# 6th param is the path where the test suite is located

#VM:
#./testGeneratedSuite.sh Chart 1 /home/ubuntu/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /home/ubuntu/defects4j/ExamplesCheckedOut/chart1Buggy/ /home/ubuntu/defects4j/generatedTestSuitesForBugsWeFoundARepairFor


if [ "$#" -ne 6 ]; then
    echo "This script should be run with 6 parameters"
    exit 0
fi


PROJECT="$1"
BUGNUMBER="$2"
DEFECTS4JDIR="$3"
DIROFJAVA7="$4"
PATHOFBUGGYFOLDER="$5"
PATHOFSUITEFOLDER="$6"

export JAVA_HOME=$DIROFJAVA7
export JRE_HOME=$DIROFJAVA7/jre
export PATH=$DIROFJAVA7/bin/:$PATH


echo "Evaluating test suite"
cd $DEFECTS4JDIR/framework/bin
echo ""

SEED=1

#declare -a arr=("chart21Buggy" "lang59Buggy" "math24Buggy" "math29Buggy" "math49Buggy" "time19Buggy" "closure125Buggy" "closure86Buggy" "math7Buggy" "closure83Buggy" "lang45Buggy" "chart3Buggy" "chart5Buggy" "lang39Buggy" "math40Buggy" "closure66Buggy" "lang43Buggy" "chart1Buggy" "math18Buggy" "lang10Buggy" "math20Buggy" "lang7Buggy" "math73Buggy" "math95Buggy" "math82Buggy" "closure46Buggy" "lang22Buggy" "chart26Buggy" "chart25Buggy" "lang63Buggy" "chart13Buggy" "closure107Buggy" "closure115Buggy" "closure126Buggy" "closure13Buggy" "closure19Buggy" "closure21Buggy" "closure22Buggy" "math28Buggy" "math50Buggy" "math80Buggy" "math81Buggy" "math85Buggy" "math8Buggy")

#for i in "${arr[@]}"
#do

rm -f $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"log.txt
 #run_bug_detection.pl -p $PROJECT -d $DEFECTS4JDIR/generatedTestSuitesForBugsWeFoundARepairFor/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -o out_dir [-f include_file_pattern] [-v version_id] [-t tmp_dir] [-D]
COM="./defects4j test -s $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w $PATHOFBUGGYFOLDER &>> $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"log.txt"
 
echo "$COM"
echo "Running... Log file located in $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"log.txt"
eval $COM

#done


