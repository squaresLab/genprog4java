#!/bin/bash

# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
# 4th param is the folder where the java 7 instalation is located
# 5th param is the path of the buggy folder 
# 6th param is the path where the test suite is located

#VM:
#./testGeneratedSuite.sh Chart 1 /home/ubuntu/defects4j/ /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /home/ubuntu/defects4j/BugsWithAFix/chart1Buggy/ /home/ubuntu/defects4j/generatedTestSuitesForBugsWeFoundARepairFor


if [ "$#" -ne 6 ]; then
    echo "This script should be run with 6 parameters"
    exit 0
fi


PROJECT="$1"
BUGNUMBER="$2"
DEFECTS4JDIR="$3"
DIROFJAVA7="$4"
PATHOFFIXEDFOLDER="$5"
PATHOFSUITEFOLDER="$6"

export JAVA_HOME=$DIROFJAVA7
export JRE_HOME=$DIROFJAVA7/jre
export PATH=$DIROFJAVA7/bin/:$PATH


echo "Evaluating test suite"
cd $DEFECTS4JDIR/framework/bin
echo ""

SEED=1


rm -f $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"log.txt
 #run_bug_detection.pl -p $PROJECT -d $DEFECTS4JDIR/generatedTestSuitesForBugsWeFoundARepairFor/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -o out_dir [-f include_file_pattern] [-v version_id] [-t tmp_dir] [-D]
COM="./defects4j test -s $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"f-randoop."$SEED".tar.bz2 -w $PATHOFFIXEDFOLDER &>> $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"log.txt"
 
echo "$COM"
echo "Running... Log file located in $PATHOFSUITEFOLDER/"$PROJECT"-"$BUGNUMBER"log.txt"
eval $COM


