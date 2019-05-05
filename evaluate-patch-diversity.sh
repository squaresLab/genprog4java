#this is a convenience script for using in Zhen's docker containers

$1 = $BUGWD

java -ea =Dlog4j.configurationFile=file:"$GP4J_HOME"/src/log4j.properties -Dfile.encoding=UTF-8 \
	-classpath "$GP4J_HOME"/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar \
	clegoues.genprog4java.main.Main \
	$GP4J_HOME $JAVA_HOME $DAIKONDIR $BUGWD/introclass.config | tee $BUGWD/log-eval-patch-diversity.txt