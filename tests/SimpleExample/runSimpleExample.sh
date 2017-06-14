#!/bin/bash

rm /home/mau/Research/genprog4java/tests/SimpleExample/runSimpleExample.txt

COUNTER=0
while [  $COUNTER -lt 20 ]; do

echo "" >> runSimpleExample.txt
echo "SEED: $COUNTER" >> runSimpleExample.txt

rm -f /home/mau/Research/genprog4java/tests/SimpleExample/testcache.ser

rm -f /home/mau/Research/genprog4java/tests/SimpleExample/simpleExample.config

cd /home/mau/Research/genprog4java/tests/SimpleExample/

#Create config file 
FILE=/home/mau/Research/genprog4java/tests/SimpleExample/simpleExample.config
/bin/cat <<EOM >$FILE
javaVM = /usr/bin/java
popsize = 20
seed = $COUNTER
classTestFolder = bin/
workingDir = /home/mau/Research/genprog4java/tests/SimpleExample/
outputDir = /home/mau/Research/genprog4java/tests/SimpleExample/tmp/
libs = /home/mau/Research/genprog4java/lib/junit-4.12.jar:/home/mau/Research/genprog4java/lib/junittestrunner.jar:/home/mau/Research/genprog4java/lib/hamcrest-core-1.3.jar:/home/mau/Research/genprog4java/tests/SimpleExample/bin/
sanity = yes
regenPaths
sourceDir = src/
positiveTests = /home/mau/Research/genprog4java/tests/SimpleExample/pos.tests
negativeTests = /home/mau/Research/genprog4java/tests/SimpleExample/neg.tests
jacocoPath = /home/mau/Research/genprog4java/lib/jacocoagent.jar
classSourceFolder = bin/
targetClassName = packageSimpleExample.SimpleExample
edits=FUNREP;PARREP;PARADD;PARREM;EXPREP;EXPADD;EXPREM;NULLCHECK;OBJINIT;RANGECHECK;SIZECHECK;CASTCHECK;LBOUNDSET;UBOUNDSET;OFFBYONE;SEQEXCH;CASTERMUT;CASTEEMUT
model=probabilistic
modelPath=/home/mau/Research/genprog4java/overallModel.txt
EOM

/usr/lib/jvm/java-8-openjdk-amd64/bin/java -ea -Dlog4j.configuration=file:/home/mau/Research/genprog4java/src/log4j.properties -Dfile.encoding=UTF-8 -classpath /home/mau/Research/genprog4java/target/classes:/home/mau/.p2/pool/plugins/org.junit_4.12.0.v201504281640/junit.jar:/home/mau/.p2/pool/plugins/org.hamcrest.core_1.3.0.v201303031735.jar:/home/mau/Research/genprog4java/lib/junit-4.12.jar:/home/mau/.m2/repository/org/eclipse/tycho/org.eclipse.jdt.core/3.11.1.v20150902-1521/org.eclipse.jdt.core-3.11.1.v20150902-1521.jar:/home/mau/.m2/repository/org/eclipse/core/runtime/3.10.0-v20140318-2214/runtime-3.10.0-v20140318-2214.jar:/home/mau/.m2/repository/org/eclipse/osgi/3.10.0-v20140606-1445/osgi-3.10.0-v20140606-1445.jar:/home/mau/.m2/repository/org/eclipse/equinox/common/3.6.200-v20130402-1505/common-3.6.200-v20130402-1505.jar:/home/mau/.m2/repository/org/eclipse/core/jobs/3.6.0-v20140424-0053/jobs-3.6.0-v20140424-0053.jar:/home/mau/.m2/repository/org/eclipse/equinox/registry/3.5.400-v20140428-1507/registry-3.5.400-v20140428-1507.jar:/home/mau/.m2/repository/org/eclipse/equinox/preferences/3.5.200-v20140224-1527/preferences-3.5.200-v20140224-1527.jar:/home/mau/.m2/repository/org/eclipse/core/contenttype/3.4.200-v20140207-1251/contenttype-3.4.200-v20140207-1251.jar:/home/mau/.m2/repository/org/eclipse/equinox/app/1.3.200-v20130910-1609/app-1.3.200-v20130910-1609.jar:/home/mau/.m2/repository/org/eclipse/text/org.eclipse.text/3.5.101/org.eclipse.text-3.5.101.jar:/home/mau/.m2/repository/org/eclipse/core/org.eclipse.core.commands/3.6.0/org.eclipse.core.commands-3.6.0.jar:/home/mau/.m2/repository/org/eclipse/equinox/org.eclipse.equinox.common/3.6.0/org.eclipse.equinox.common-3.6.0.jar:/home/mau/.m2/repository/org/jacoco/org.jacoco.core/0.7.6.201602180812/org.jacoco.core-0.7.6.201602180812.jar:/home/mau/.m2/repository/org/ow2/asm/asm-debug-all/5.0.4/asm-debug-all-5.0.4.jar:/home/mau/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar:/home/mau/.m2/repository/org/apache/commons/commons-exec/1.3/commons-exec-1.3.jar:/home/mau/.m2/repository/commons-cli/commons-cli/20040117.000000/commons-cli-20040117.000000.jar:/home/mau/.m2/repository/org/eclipse/core/resources/3.3.0-v20070604/resources-3.3.0-v20070604.jar:/home/mau/.m2/repository/org/eclipse/core/expressions/3.3.0-v20070606-0010/expressions-3.3.0-v20070606-0010.jar:/home/mau/.m2/repository/org/eclipse/core/filesystem/1.1.0-v20070606/filesystem-1.1.0-v20070606.jar:/home/mau/.m2/repository/org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar:/home/mau/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/home/mau/.m2/repository/org/junit/junit4-runner/5.0.0-ALPHA/junit4-runner-5.0.0-ALPHA.jar:/home/mau/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/mau/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/mau/.m2/repository/org/junit/junit-launcher/5.0.0-ALPHA/junit-launcher-5.0.0-ALPHA.jar:/home/mau/.m2/repository/org/junit/junit-engine-api/5.0.0-ALPHA/junit-engine-api-5.0.0-ALPHA.jar:/home/mau/.m2/repository/org/junit/junit-commons/5.0.0-ALPHA/junit-commons-5.0.0-ALPHA.jar:/home/mau/.m2/repository/org/opentest4j/opentest4j/1.0.0-ALPHA/opentest4j-1.0.0-ALPHA.jar clegoues.genprog4java.main.Main /home/mau/Research/genprog4java/tests/SimpleExample/simpleExample.config >> runSimpleExample.txt

((COUNTER++))

done
