#!/bin/bash
#Bugs that are fixed with a single line edit by the human developer
declare -a arr=("Chart 1" "Chart 8" "Chart 20" "Chart 24" "Closure 10" "Closure 14" "Closure 18" "Closure 20" "Closure 31" "Closure 51" "Closure 52" "Closure 59" "Closure 62" "Closure 63" "Closure 65" "Closure 70" "Closure 71" "Closure 73" "Closure 77" "Closure 82" "Closure 86" "Closure 92" "Closure 93" "Closure 104" "Closure 111" "Closure 113" "Closure 119" "Closure 130" "Closure 132" "Lang 6" "Lang 16" "Lang 21" "Lang 24" "Lang 26" "Lang 29" "Lang 33" "Lang 38" "Lang 43" "Lang 51" "Lang 57" "Lang 58" "Lang 59" "Math 2" "Math 5" "Math 10" "Math 11" "Math 27" "Math 33" "Math 34" "Math 41" "Math 57" "Math 59" "Math 69" "Math 70" "Math 75" "Math 80" "Math 85" "Math 94" "Math 101" "Math 105" "Time 4" "Time 16" "Time 19")
#Bugs that are fixed with multiple line edit, within the same function by the human developer (Not too far away)
#declare -a arr=("Chart 3" "Chart 4" "Chart 5" "Chart 6" "Chart 7" "Chart 17" "Chart 26" "Closure 1" "Closure 2" "Closure 4" "Closure 5" "Closure 7" "Closure 11" "Closure 13" "Closure 15" "Closure 17" "Closure 19" "Closure 23" "Closure 24" "Closure 25" "Closure 32" "Closure 33" "Closure 35" "Closure 36" "Closure 38" "Closure 39" "Closure 40" "Closure 42" "Closure 44" "Closure 50" "Closure 53" "Closure 56" "Closure 57" "Closure 58" "Closure 61" "Closure 66" "Closure 67" "Closure 81" "Closure 83" "Closure 91" "Closure 94" "Closure 95" "Closure 96" "Closure 97" "Closure 99" "Closure 105" "Closure 112" "Closure 115" "Closure 116" "Closure 117" "Closure 118" "Closure 120" "Closure 121" "Closure 122" "Closure 123" "Closure 126" "Closure 128" "Closure 129" "Closure 131" "Lang 1" "Lang 2" "Lang 3" "Lang 5" "Lang 9" "Lang 10" "Lang 11" "Lang 12" "Lang 14" "Lang 17" "Lang 18" "Lang 19" "Lang 22" "Lang 27" "Lang 28" "Lang 31" "Lang 37" "Lang 39" "Lang 40" "Lang 42" "Lang 44" "Lang 45" "Lang 48" "Lang 49" "Lang 52" "Lang 53" "Lang 54" "Lang 55" "Lang 61" "Lang 65" "Math 3" "Math 7" "Math 8" "Math 9" "Math 19" "Math 21" "Math 26" "Math 28" "Math 30" "Math 31" "Math 32" "Math 39" "Math 40" "Math 42" "Math 43" "Math 44" "Math 45" "Math 48" "Math 50" "Math 51" "Math 53" "Math 55" "Math 56" "Math 58" "Math 60" "Math 72" "Math 74" "Math 78" "Math 79" "Math 82" "Math 84" "Math 86" "Math 87" "Math 89" "Math 91" "Math 95" "Math 96" "Math 97" "Math 99" "Math 102" "Math 103" "Math 106" "Time 5" "Time 7" "Time 8" "Time 11" "Time 14" "Time 17" "Time 18" "Time 20" "Time 21" "Time 23" "Time 24" "Time 25" "Time 27")
#ones in particular
#declare -a arr=("Chart 1")

if [ "$#" -ne 2 ]; then
    echo "This script should be run with 2 parameters: Path to defects4j used, path to genprog used"
    exit 0
fi

## now loop through the above array
export D4J_HOME="$1"
export GP4J_HOME="$2"
export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64/

for i in "${arr[@]}"
do
   COMMAND="./runGenProgForBug.sh $i allHuman 100 ExamplesCheckedOut 1 20 false /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /usr/lib/jvm/java-8-oracle/" 
   eval $COMMAND
done
