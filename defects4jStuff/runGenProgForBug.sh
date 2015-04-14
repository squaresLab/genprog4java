#!/bin/bash
# 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)

cp runGenProgForBug.bash ./genprog4java/defects4jStuff/

#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE="${1,,}"

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:~/Research/defects4j/framework/bin


#Specific variables per every project
#JAVADIR is the working directory of the project
if [ $LOWERCASEPACKAGE = "chart" ]; then
#NEED TO CHANGE WD BECAUSE TESTS IS IN THE ROOT
  TESTSDIR=tests.org.jfree
  WD=source
  JAVADIR=org/jfree

elif [ $LOWERCASEPACKAGE = "closure" ]; then
#NEED TO CHANGE WD BECAUSE TESTS IS IN THE ROOT
  TESTSDIR=test.com.google
  WD=src
  JAVADIR=com/google

elif [ $LOWERCASEPACKAGE = "lang" ]; then
  TESTSDIR=src.test.java.org.apache.commons.lang3
  WD=src/main/java
  JAVADIR=org/apache/commons/lang3 

elif [ $LOWERCASEPACKAGE = "math" ]; then 
  TESTSDIR=src.test.java.org.apache.commons.math3
  WD=src/main/java
  JAVADIR=org/apache/commons/math3

elif [ $LOWERCASEPACKAGE = "time" ]; then
  TESTSDIR=src.test.java.org.joda.time
  WD=src/main/java
  JAVADIR=org/joda/time

fi


for seed in {0..20..2} #0 to 20, increments of 2
  do

	#CHANGE TO THE WORKING DIRECTORY
	cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

	PACKAGEDIR=${JAVADIR//"/"/"."}

	#Create config file TODO:#FIX THIS FILE
	FILE=~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/configDefects4j
	/bin/cat <<EOM >$FILE
	packageName = $PACKAGEDIR
	targetClassName = NameOfTheTargetFileWithNoExtension
	sourceDir = $JAVADIR
	popsize = 5
	seed = $seed
	testsDir = $TESTSDIR
	javaVM = /usr/bin/java
	workingDir = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD
	outputDir = ./tmp
	libs = $CONFIGLIBS
	classDir = bin/
	sanity = yes
	regenPaths
	positiveTests = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
	negativeTests = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
	jacocoPath = /home/mau/Research/defects4j/framework/projects/lib/jacocoagent.jar
	EOM

	#Run genProg with the new seed
	/usr/lib/jvm/java-7-openjdk-i386/bin/java -ea -Dfile.encoding=UTF-8 -classpath /home/mau/Research/genprog4java/bin:/usr/share/eclipse/dropins/jdt/plugins/org.junit_4.8.2.dist/junit.jar:/usr/share/eclipse/dropins/jdt/plugins/org.hamcrest.core_1.3.0.jar:/home/mau/Research/genprog4java/lib/asm-all-3.3.1.jar:/home/mau/Research/genprog4java/lib/commons-cli-1.1.jar:/home/mau/Research/genprog4java/lib/commons-collections-3.2.1.jar:/home/mau/Research/genprog4java/lib/commons-exec-1.0.0-SNAPSHOT.jar:/home/mau/Research/genprog4java/lib/commons-io-1.4.jar:/home/mau/Research/genprog4java/lib/jstests.jar:/home/mau/Research/genprog4java/lib/junit-4.10.jar:/home/mau/Research/genprog4java/lib/org.eclipse.core.commands_3.6.0.I20100512-1500.jar:/home/mau/Research/genprog4java/lib/org.eclipse.core.contenttype_3.4.100.v20100505-1235.jar:/home/mau/Research/genprog4java/lib/org.eclipse.core.jobs_3.5.1.R36x_v20100824.jar:/home/mau/Research/genprog4java/lib/org.eclipse.core.resources_3.6.0.R36x_v20100825-0600.jar:/home/mau/Research/genprog4java/lib/org.eclipse.core.runtime_3.6.0.v20100505.jar:/home/mau/Research/genprog4java/lib/org.eclipse.core.runtime.compatibility_3.2.100.v20100505.jar:/home/mau/Research/genprog4java/lib/org.eclipse.equinox.common_3.6.0.v20100503.jar:/home/mau/Research/genprog4java/lib/org.eclipse.equinox.preferences_3.3.0.v20100503.jar:/home/mau/Research/genprog4java/lib/org.eclipse.jdt_3.6.1.v201009090800.jar:/home/mau/Research/genprog4java/lib/org.eclipse.jdt.core_3.6.1.v_A68_R36x.jar:/home/mau/Research/genprog4java/lib/org.eclipse.jdt.ui_3.6.1.r361_v20100825-0800.jar:/home/mau/Research/genprog4java/lib/org.eclipse.jface_3.6.1.M20100825-0800.jar:/home/mau/Research/genprog4java/lib/org.eclipse.jface.text_3.6.1.r361_v20100825-0800.jar:/home/mau/Research/genprog4java/lib/org.eclipse.osgi_3.6.1.R36x_v20100806.jar:/home/mau/Research/genprog4java/lib/org.eclipse.osgi.services_3.2.100.v20100503.jar:/home/mau/Research/genprog4java/lib/org.eclipse.osgi.util_3.2.100.v20100503.jar:/home/mau/Research/genprog4java/lib/org.eclipse.text_3.5.0.v20100601-1300.jar:/home/mau/Research/genprog4java/lib/org.jacoco.core-0.5.8.201205221855.jar clegoues.genprog4java.main.Main /home/mau/Research/defects4j/ExamplesCheckedOut/math5Buggy/configDefects4j > logBug$2Seed$seed.txt



 done
