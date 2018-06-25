#!/usr/lib/bin

FILENAME=$1
CLASSP=$2
VARNAME=$3

java -cp .:$CLASSP:/home/lvyiwei1/genprog4java-branch/genprog4java/lib/javassist.jar ylyu1.wean.Modify JUnitTestRunner $VARNAME > $VARNAME.log

java -cp .:$CLASSP clegoues.genprog4java.fitness.JUnitTestRunner $FILENAME > $VARNAME.tuo

java -cp .:$CLASSP ylyu1.wean.Aggregator $VARNAME > $VARNAME.pred

rm -r packageSimpleExample
