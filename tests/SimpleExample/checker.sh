#!/usr/lib/bin

FILENAME=$1
CLASSP=$2
VARNAME=$3
ORIGORNOT=$4

java -cp .:$CLASSP:/home/lvyiwei1/genprog4java-branch/genprog4java/lib/javassist.jar ylyu1.wean.Modify JUnitTestRunner $VARNAME > $VARNAME.log

java -cp .:$CLASSP clegoues.genprog4java.fitness.JUnitTestRunner $FILENAME > $VARNAME.tuo

java -cp .:$CLASSP:/home/lvyiwei1/genprog4java-branch/genprog4java/lib/javassist.jar ylyu1.wean.Aggregator $VARNAME $ORIGORNOT > $VARNAME.pred

rm -r packageSimpleExample
