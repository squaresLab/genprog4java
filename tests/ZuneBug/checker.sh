#!/usr/lib/bin

FILENAME=$1
CLASSP=$2
VARNAME=$3
ORIGORNOT=$4

java -cp .:$CLASSP:$CLASSPATH:/home/lvyiwei1/daikon-master/daikon.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/lib/tools.jar:/home/lvyiwei1/genprog4java-branch/genprog4java/lib/javassist.jar ylyu1.wean.Modify JUnitTestRunner $VARNAME > $VARNAME.log

java -cp .:$CLASSP:$CLASSPATH:/home/lvyiwei1/daikon-master/daikon.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/lib/tools.jar clegoues.genprog4java.fitness.JUnitTestRunner $FILENAME > $VARNAME.tuo

java -cp .:$CLASSP:/home/lvyiwei1/genprog4java-branch/genprog4java/lib/javassist.jar ylyu1.wean.Aggregator $VARNAME $ORIGORNOT > $VARNAME.pred

rm -r packageZuneBug
