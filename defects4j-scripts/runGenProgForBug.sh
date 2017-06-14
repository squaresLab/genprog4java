#!/bin/bash

#The purpose of this script is to run Genprog of a particular defects4j bug.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable GP4J_HOME should be directed to the folder where genprog4java is installed.

#Output
#The output is a txt file with the output of running the coverage analysis of the test suite on each of the folders indicated. 

#Parameters:
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3th param is the option of running the test suite (ex: allHuman, oneHuman, oneGenerated)
# 4th param is the test suite sample size (ex: 1, 100)
# 5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME (Ex: ExamplesCheckedOut)
# 6th param is the initial seed. It will then increase the seeds by adding 1 until it gets to the number in the next param.
# 7th param is the final seed.
# 8th param is on if the purpose is to test only fault loc and not really trying to find a patch. When it has reached the end of fault localization it will stop.
# 9th param is the folder where the java 7 instalation is located
# 10th param is the folder where the java 8 instalation is located

#Example of usage:
#./runGenProgForBug.sh Math 2 allHuman 100 ExamplesCheckedOut 1 5 false /usr/lib/jvm/java-1.7.0-openjdk-amd64 /usr/lib/jvm/java-1.8.0-openjdk-amd64


if [ "$#" -lt 8 ]; then
    echo "This script should be run with 10 parameters:"
	echo " 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)"
	echo " 2nd param is the bug number (ex: 1,2,3,4,...)"
	echo " 3th param is the option of running the test suite (ex: allHuman, oneHuman, oneGenerated)"
	echo " 4th param is the test suite sample size (ex: 1, 100)"
	echo " 5th param is the folder where the bug files will be cloned to. Starting from $D4J_HOME (Ex: ExamplesCheckedOut)"
	echo " 6th param is the initial seed. It will then increase the seeds by adding 1 until it gets to the number in the next param."
	echo " 7th param is the final seed."
	echo " 8th param is on if the purpose is to test only fault loc and not really trying to find a patch. When it has reached the end of fault localization it will stop."
	echo " 9th param is the folder where the java 7 instalation is located"
	echo " 10th param is the folder where the java 8 instalation is located"

else

PROJECT="$1"
BUGNUMBER="$2"
OPTION="$3"
TESTSUITESAMPLE="$4"
BUGSFOLDER="$5"
STARTSEED="$6"
UNTILSEED="$7"
JUSTTESTINGFAULTLOC="$8"
DIROFJAVA7="/usr/lib/jvm/java-1.7.0-openjdk-amd64"
DIROFJAVA8="/usr/lib/jvm/java-1.8.0-openjdk-amd64"

if [ "$#" -eq 10 ]; then
  DIROFJAVA7="$9"
  DIROFJAVA8="${10}"
fi


#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:$D4J_HOME/framework/bin

# directory with the checked out buggy project
BUGWD=$D4J_HOME/$BUGSFOLDER"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy
export JAVA_HOME=$DIROFJAVA8
export JRE_HOME=$DIROFJAVA8/jre
export PATH=$DIROFJAVA8/bin/:$PATH
#sudo update-java-alternatives -s $DIROFJAVA8

#Compile Genprog and put the class files in /bin
#Go to the GenProg folder
if [ -d "$GP4J_HOME" ]; then
  cd "$GP4J_HOME"
  mvn package
  if [[ $? -ne 0 ]]; then
      echo "error building GenProg; exiting"
      exit 1
  fi

  export JAVA_HOME=$DIROFJAVA7
  export JRE_HOME=$DIROFJAVA7/jre
  export PATH=$DIROFJAVA7/bin/:$PATH
  #sudo update-java-alternatives -s $DIROFJAVA7

  if [ -d "$GP4J_HOME/defects4j-scripts/" ]; then
    cd "$GP4J_HOME"/defects4j-scripts/

    ./prepareBug.sh $PROJECT $BUGNUMBER $OPTION $TESTSUITESAMPLE $BUGSFOLDER $DIROFJAVA7 $DIROFJAVA8

    if [ -d "$BUGWD/$WD" ]; then
      #Go to the working directory
      cd $BUGWD/$WD

      for (( seed=$STARTSEED; seed<=$UNTILSEED; seed++ ))
      do	
	echo "RUNNING THE BUG: $PROJECT $BUGNUMBER, WITH THE SEED: $seed"

	#Running until fault loc only
	if [ $JUSTTESTINGFAULTLOC == "true" ]; then
	  echo "justTestingFaultLoc = true" >> $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	fi

	#Changing the seed
	CHANGESEEDCOMMAND="sed -i '1s/.*/seed = $seed/' "$D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	eval $CHANGESEEDCOMMAND

	if [ $seed != $STARTSEED ]; then
	  REMOVESANITYCOMMAND="sed -i 's/sanity = yes/sanity = no/' "$D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	  eval $REMOVESANITYCOMMAND

	  REMOVEREGENPATHS="sed -i '/regenPaths/d' "$D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	  eval $REMOVEREGENPATHS
	fi
    
	export JAVA_HOME=$DIROFJAVA8
	export JRE_HOME=$DIROFJAVA8/jre
  	export PATH=$DIROFJAVA8/bin/:$PATH
	#sudo update-java-alternatives -s $DIROFJAVA8

	JAVALOCATION=$(which java)
	timeout -sHUP 4h $JAVALOCATION -ea -Dlog4j.configurationFile=file:"$GP4J_HOME"/src/log4j.properties -Dfile.encoding=UTF-8 -classpath "$GP4J_HOME"/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar clegoues.genprog4java.main.Main $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config | tee $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/log"$PROJECT""$BUGNUMBER"Seed$seed.txt


	#Save the variants in a tar file
	tar -cvf $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/variants"$PROJECT""$BUGNUMBER"Seed$seed.tar $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	mv $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/original/ $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/
	rm -r $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	mkdir $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	mv $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/original/ $D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	
      done
    fi
  fi
fi

fi #correct number of params

#For use only of the Probabilistic vs Equally distributed comparisson
#WHICHMODE="RegAllMut"
#mv $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2Buggy/ $D4J_HOME/$BUGSFOLDER/"ResultsRegvsProbMultiLine"/$LOWERCASEPACKAGE$2$WHICHMODE/
#cd $GP4J_HOME/defects4j-scripts/
#GETLASTLINESFROMLOGS="./probVsEDSpecificScripts/getLastLinesFromAllLogs.sh $PROJECT $BUGNUMBER $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2$WHICHMODE/"
#eval $GETLASTLINESFROMLOGS
#CONVERTTOCSV="./probVsEDSpecificScripts/convertToCSVtheLastLines.sh $D4J_HOME/$BUGSFOLDER/$LOWERCASEPACKAGE$2$WHICHMODE/"
#eval $CONVERTTOCSV
