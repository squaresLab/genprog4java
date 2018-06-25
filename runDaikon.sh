#!/usr/lib/bin

FILENAME=$1
CLASSP=$2



java -cp .:$CLASSPATH:$CLASSP:/home/lvyiwei1/daikon-master/daikon.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/lib/tools.jar daikon.Chicory --ppt-omit-pattern=junit\. --ppt-omit-pattern=proxy\. clegoues.genprog4java.fitness.JUnitTestRunner $FILENAME

java -cp /home/lvyiwei1/daikon-master/daikon.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-1.8.0-openjdk-amd64/lib/tools.jar daikon.Daikon --format=java JUnitTestRunner.dtrace.gz > JUnitTestRunner.wean

java -cp .:$CLASSP ylyu1.wean.WeanParse JUnitTestRunner DEBUG
