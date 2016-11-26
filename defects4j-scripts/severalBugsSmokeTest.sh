#!/bin/bash
#All bugs with fix 
#declare -a arr=("Chart 1" "Chart 3" "Chart 5" "Chart 13" "Chart 25" "Chart 26" "Closure 13" "Closure 19" "Closure 21" "Closure 22" "Closure 25" "Closure 35" "Closure 45" "Closure 46" "Closure 66" "Closure 83" "Closure 102" "Closure 107" "Closure 115" "Closure 125" "Closure 126" "Lang 7" "Lang 10" "Lang 22" "Lang 39" "Lang 43" "Lang 51" "Lang 59" "Lang 63" "Math 7" "Math 8" "Math 18" "Math 20" "Math 24" "Math 28" "Math 29" "Math 40" "Math 49" "Math 50" "Math 53" "Math 73" "Math 80" "Math 81" "Math 82" "Math 84" "Math 85" "Math 95" "Time 19")
#ones in particular
declare -a arr=("Chart 1")

if [ "$#" -ne 1 ]; then
    echo "This script should be run with 1 parameters: Path to defects4j used$"
    exit 0
fi

## now loop through the above array
export D4J_HOME="$1"
export GP4J_HOME=/home/mausoto/probGenProg/genprog4java/
export JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-amd64/

for i in "${arr[@]}"
do
   COMMAND="./runGenProgForBug.sh $i allHuman 100 ExamplesCheckedOut 1 20 false /usr/lib/jvm/java-1.7.0-openjdk-amd64/ /usr/lib/jvm/java-8-oracle/" 
   eval $COMMAND
done
