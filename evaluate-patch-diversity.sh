#this is a convenience script for using in Zhen's docker containers
if [ "$#" -ne 1 ]; then
	echo "1st param is the path to the bug working directory"
else

BUGWD=$1
GP4J_PDE=/home/user/gp4j-patch-div-eval

BASEDIR=$PWD
cd $BUGWD
java -ea -Dlog4j.configurationFile=file:"$GP4J_PDE"/src/log4j.properties -Dfile.encoding=UTF-8 \
	-classpath "$GP4J_PDE"/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar \
	clegoues.genprog4java.main.Main \
	$GP4J_PDE $JAVA_HOME $DAIKONDIR $BUGWD/introclass.config
cd $BASEDIR
fi
