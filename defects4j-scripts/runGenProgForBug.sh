#!/bin/bash
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: /home/mau/Research/genprog4java/ )
# 4td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
# 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)
# 6th param is the percentage of test cases being used to guide genprog's search (ex: 1, 100)

#cp runGenProgForBug.bash ./genprog4java/defects4jStuff/

#Mau runs it like this:
#./runGenProgForBug.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ allHuman 100

#VM:
#./runGenProgForBug.sh Math 2 /home/ubuntu/genprog4java/ /home/ubuntu/defects4j/ allHuman 100

PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
OPTION="$5"
TESTSUITEPERCENTAGE="$6"

#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:~/Research/defects4j/framework/bin

PARENTDIR=$DEFECTS4JDIR"/ExamplesCheckedOut"

# directory with the checked out buggy project
BUGWD=$PARENTDIR"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy



#Compile Genprog and put the class files in /bin
#Go to the GenProg folder
cd "$GENPROGDIR"
ant -buildfile buildGenProg.xml



#Was trying to make it with javac, ended up doing it with ant 
 #echo Compiling GenProg source files...
 #create file to run compilation
 #FILENAME=sources.txt
 #exec 3<>$FILENAME
 # Write to file
 #echo $LIBSMAIN >&3
 #find -name "*.java" >&3
 #echo $EXTRACLASSES >&3
 #exec 3>&-

 #Compile the project
 #javac @sources.txt
 
 #echo Compilation of main java classes successful
 #rm sources.txt


cd "$GENPROGDIR"/defects4jStuff/

./prepareBug.sh $PROJECT $BUGNUMBER $GENPROGDIR $DEFECTS4JDIR $OPTION $TESTSUITEPERCENTAGE






#Specific variables per every project
if [ $LOWERCASEPACKAGE = "chart" ]; then
#NEED TO CHANGE WD BECAUSE TESTS IS IN THE ROOT
#  TESTSDIR=tests.org.jfree
  WD=source
#  JAVADIR=org/jfree

elif [ $LOWERCASEPACKAGE = "closure" ]; then
#NEED TO CHANGE WD BECAUSE TESTS IS IN THE ROOT
#  TESTSDIR=test.com.google
  WD=src
#  JAVADIR=com/google

elif [ $LOWERCASEPACKAGE = "lang" ]; then
#  TESTSDIR=src.test.java.org.apache.commons.lang3
  WD=src/main/java
#  JAVADIR=org/apache/commons/lang3 

elif [ $LOWERCASEPACKAGE = "math" ]; then 
#  TESTSDIR=src.test.java.org.apache.commons.math3
  WD=src/main/java
#  JAVADIR=org/apache/commons/math3

elif [ $LOWERCASEPACKAGE = "time" ]; then
#  TESTSDIR=src.test.java.org.joda.time
  WD=src/main/java
#  JAVADIR=org/joda/time

fi


JAVALOCATION=$(which java)

#Go to the working directory
cd $BUGWD/$WD

#CHANGE TO THE WORKING DIRECTORY
#cd $BUGWD/$WD


for seed in {0..20..2} #0 to 20, increments of 2
  do	
	echo "RUNNING THE BUG: $PROJECT $BUGNUMBER, WITH THE SEED: $seed"
	
	CHANGESEEDCOMMAND="sed -i '2s/.*/seed = $seed/' ""$DEFECTS4JDIR"/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config

	eval $CHANGESEEDCOMMAND


	#Run genProg with the new seed
	$JAVALOCATION -ea -Dfile.encoding=UTF-8 -classpath "$GENPROGDIR"/bin:/home/ubuntu/.p2/pool/plugins/org.junit_4.12.0.v201504281640/junit.jar:/home/ubuntu/.p2/pool/plugins/org.hamcrest.core_1.3.0.v201303031735.jar:"$GENPROGDIR"/lib/asm-all-3.3.1.jar:"$GENPROGDIR"/lib/commons-cli-1.1.jar:"$GENPROGDIR"/lib/commons-collections-3.2.1.jar:"$GENPROGDIR"/lib/commons-exec-1.0.0-SNAPSHOT.jar:"$GENPROGDIR"/lib/commons-io-1.4.jar:"$GENPROGDIR"/lib/jstests.jar:"$GENPROGDIR"/lib/junit-4.10.jar:"$GENPROGDIR"/lib/org.eclipse.core.commands_3.6.0.I20100512-1500.jar:"$GENPROGDIR"/lib/org.eclipse.core.contenttype_3.4.100.v20100505-1235.jar:"$GENPROGDIR"/lib/org.eclipse.core.jobs_3.5.1.R36x_v20100824.jar:"$GENPROGDIR"/lib/org.eclipse.core.resources_3.6.0.R36x_v20100825-0600.jar:"$GENPROGDIR"/lib/org.eclipse.core.runtime_3.6.0.v20100505.jar:"$GENPROGDIR"/lib/org.eclipse.core.runtime.compatibility_3.2.100.v20100505.jar:"$GENPROGDIR"/lib/org.eclipse.equinox.common_3.6.0.v20100503.jar:"$GENPROGDIR"/lib/org.eclipse.equinox.preferences_3.3.0.v20100503.jar:"$GENPROGDIR"/lib/org.eclipse.jdt_3.6.1.v201009090800.jar:"$GENPROGDIR"/lib/org.eclipse.jdt.ui_3.6.1.r361_v20100825-0800.jar:"$GENPROGDIR"/lib/org.eclipse.jface_3.6.1.M20100825-0800.jar:"$GENPROGDIR"/lib/org.eclipse.jface.text_3.6.1.r361_v20100825-0800.jar:"$GENPROGDIR"/lib/org.eclipse.osgi_3.6.1.R36x_v20100806.jar:"$GENPROGDIR"/lib/org.eclipse.osgi.services_3.2.100.v20100503.jar:"$GENPROGDIR"/lib/org.eclipse.osgi.util_3.2.100.v20100503.jar:"$GENPROGDIR"/lib/org.eclipse.text_3.5.0.v20100601-1300.jar:"$GENPROGDIR"/lib/org.jacoco.core-0.5.8.201205221855.jar:"$GENPROGDIR"/lib/org.eclipse.jdt.core_3.6.1.v_A68_R36x.jar:"$GENPROGDIR"/lib/log4j-1.2.17.jar clegoues.genprog4java.main.Main "$DEFECTS4JDIR"/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config | tee "$DEFECTS4JDIR"/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/logBug"$BUGNUMBER"Seed$seed.txt


 done

