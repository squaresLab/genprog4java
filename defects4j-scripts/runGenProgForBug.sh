#!/bin/bash
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: /home/mau/Research/genprog4java/ )
# 4td param is the folder where defects4j is installed (ex: /home/mau/Research/defects4j/ )
# 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)
# 6th param is the percentage of test cases being used to guide genprog's search (ex: 1, 100)
# 7th param is the folder where the bug files will be cloned to
# 8th param is the initial seed. It will then increase the seeds by adding 1 until it gets to the number in the 9th param.
# 9th param is the final seed.
#10th param is on if the purpose is to test only fault loc and not really trying to find a patch
#11th param is the folder where the java 7 instalation is located
#12th param is the folder where the java 8 instalation is located


#cp runGenProgForBug.bash ./genprog4java/defects4jStuff/

#Mau runs it like this:
#./runGenProgForBug.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ allHuman 100 /home/mau/Research/defects4j/ExamplesCheckedOut/ 1 5 false /usr/lib/jvm/java-7-oracle/ /usr/lib/jvm/java-8-oracle/

#VM:
#./runGenProgForBug.sh Math 2 /home/ubuntu/genprog4java/ /home/ubuntu/defects4j/ allHuman 100 /home/ubuntu/defects4j/ExamplesCheckedOut/ 1 5 false


if [ "$#" -lt 10 ]; then
    echo "This script should be run with 10 to 12 parameters: Project name, bug number, location of genprog4java, defects4j installation, testing option, test suite size, bugs folder, initial seed, final seed, just testing fault localization, [java 7 installation folder], [java 8 installation folder]"

else

PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
OPTION="$5"
TESTSUITEPERCENTAGE="$6"
BUGSFOLDER="$7"
STARTSEED="$8"
UNTILSEED="$9"
JUSTTESTINGFAULTLOC="${10}"
DIROFJAVA7="/usr/lib/jvm/java-1.7.0-openjdk-amd64"
DIROFJAVA8="/usr/lib/jvm/java-1.8.0-openjdk-amd64"

if [ "$#" -eq 12 ]; then
  DIROFJAVA7="${11}"
  DIROFJAVA8="${12}"
fi


#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:$DEFECTS4JDIR/framework/bin

# directory with the checked out buggy project
BUGWD=$BUGSFOLDER"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy
export JAVA_HOME=$DIROFJAVA8
export JRE_HOME=$DIROFJAVA8/jre
export PATH=$DIROFJAVA8/bin/:$PATH
#sudo update-java-alternatives -s $DIROFJAVA8

#Compile Genprog and put the class files in /bin
#Go to the GenProg folder
if [ -d "$GENPROGDIR" ]; then
  cd "$GENPROGDIR"
  mvn package
  if [[ $? -ne 0 ]]; then
      echo "error building GenProg; exiting"
      exit 1
  fi

  export JAVA_HOME=$DIROFJAVA7
  export JRE_HOME=$DIROFJAVA7/jre
  export PATH=$DIROFJAVA7/bin/:$PATH
  #sudo update-java-alternatives -s $DIROFJAVA7

  if [ -d "$GENPROGDIR/defects4j-scripts/" ]; then
    cd "$GENPROGDIR"/defects4j-scripts/

    ./prepareBug.sh $PROJECT $BUGNUMBER $GENPROGDIR $DEFECTS4JDIR $OPTION $TESTSUITEPERCENTAGE $BUGSFOLDER $DIROFJAVA7 $DIROFJAVA8

    if [ -d "$BUGWD/$WD" ]; then
      #Go to the working directory
      cd $BUGWD/$WD

      for (( seed=$STARTSEED; seed<=$UNTILSEED; seed++ ))
      do	
	echo "RUNNING THE BUG: $PROJECT $BUGNUMBER, WITH THE SEED: $seed"

	#Running until fault loc only
	if [ $JUSTTESTINGFAULTLOC == "true" ]; then
	  echo "justTestingFaultLoc = true" >> $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	fi

	#Changing the seed
	CHANGESEEDCOMMAND="sed -i '1s/.*/seed = $seed/' "$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	eval $CHANGESEEDCOMMAND

	if [ $seed != $STARTSEED ]; then
	  REMOVESANITYCOMMAND="sed -i 's/sanity = yes/sanity = no/' "$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	  eval $REMOVESANITYCOMMAND

	  REMOVEREGENPATHS="sed -i '/regenPaths/d' "$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config
	  eval $REMOVEREGENPATHS
	fi
    
	export JAVA_HOME=$DIROFJAVA8
	export JRE_HOME=$DIROFJAVA8/jre
  	export PATH=$DIROFJAVA8/bin/:$PATH
	#sudo update-java-alternatives -s $DIROFJAVA8

	JAVALOCATION=$(which java)
	$JAVALOCATION -ea -Dlog4j.configurationFile=file:"$GENPROGDIR"/src/log4j.properties -Dfile.encoding=UTF-8 -classpath "$GENPROGDIR"/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar clegoues.genprog4java.main.Main $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/defects4j.config | tee $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/log"$PROJECT""$BUGNUMBER"Seed$seed.txt


	#Save the variants in a tar file
	tar -cvf $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/variants"$PROJECT""$BUGNUMBER"Seed$seed.tar $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	mv $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/original/ $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/
	rm -r $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	mkdir $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	mv $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/original/ $BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
	
      done
    fi
  fi
fi

fi #correct number of params

