#!/usr/lib/bin

FILENAME=$1
CLASSP=$2
GP4JHOME=$3
#variable is deliberately named differently from GP4J_HOME
JAVA8HOME=$4
#variable is deliberately named differently from JAVA_HOME
DAIKONHOME=$5

#if [ $# -ne 5 ]; then
#	echo "Wrong number of arguments"
#	exit 59295
#else

#daikon setup
DAIKONDIR=$DAIKONHOME
JAVA_HOME=$JAVA8HOME
source $DAIKONDIR/scripts/daikon.bashrc

java -cp .:$CLASSPATH:$CLASSP:$GP4JHOME/daikon.jar:$JAVA8HOME/jre/lib/rt.jar:$JAVA8HOME/lib/tools.jar daikon.Chicory --ppt-omit-pattern=junit\. --ppt-omit-pattern=proxy\. ylyu1.wean.MultiTestRunner $FILENAME

java -cp $DAIKONHOME/daikon.jar:$JAVA8HOME/jre/lib/rt.jar:$JAVA8HOME/lib/tools.jar daikon.Daikon --format=java MultiTestRunner.dtrace.gz > MultiTestRunner.wean

