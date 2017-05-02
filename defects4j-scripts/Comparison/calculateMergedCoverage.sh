#!/bin/bash

#Example of usage:  ./calculateMergedCoverage.sh /home/mausoto/defects4j/patchesGenerated/JavaRepair-results/GenProgPatches/

PATCHFOLDER="$1"

BUDGET="30"
EVOSUITEVER="020"
D4JLOCATION="/home/mausoto/defects4j/"


TESTSUITEFOLDER="$D4JLOCATION/generatedTestSuites/Evosuite"$BUDGET"MinGenProgFixesEvosuite"$EVOSUITEVER"Comparison/testSuites"
COVERAGEOUTPUT="$D4JLOCATION/generatedTestSuites/Evosuite"$BUDGET"MinGenProgFixesEvosuite"$EVOSUITEVER"Comparison/coverageFiles"

if [ "$#" -lt 1 ]; then
    echo "This script should be run with 1 parameter: Folder where the patches are located"
    exit 0
fi



export PATH=$PATH:$D4JLOCATION/framework/bin/
export PATH=$PATH:$D4JLOCATION/framework/util/
export PATH=$PATH:$D4JLOCATION/major/bin/


PATCHES=$PATCHFOLDER/*.diff
for PATCH in $PATCHES
do
	echo "Working on patch: "$PATCH
	PATCHNAME=${PATCH##*/}
	PROJECTANDBUG=$(echo $PATCHNAME | cut -d'_' -f 1)
	echo $PROJECTANDBUG
	BUG=$(echo $PROJECTANDBUG | tr -dc '0-9') #take only numbers
	echo "Bug: "$BUG
	PROJECT=$(echo $PROJECTANDBUG | tr -dc 'a-z') #take only letters from the string projectandbug
	PROJECT=$(echo ${PROJECT^}) #make upper case the first letter
	echo "Project: "$PROJECT
	LOWERCASEPROJECT=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

	#rm -rf "$D4JLOCATION/ExamplesCheckedOut/"$LOWERCASEPROJECT$BUG"Buggy"
	COM="perl /home/mausoto/defects4j/framework/bin/defects4j checkout -p "$PROJECT" -v "$BUG"b -w $D4JLOCATION/ExamplesCheckedOut/"$LOWERCASEPROJECT$BUG"Buggy"
	eval $COM

	cd "$D4JLOCATION/ExamplesCheckedOut/"$LOWERCASEPROJECT$BUG"Buggy/"
	PATHTOSOURCE=`defects4j export -p dir.src.classes`

	FILESTOMERGE=""

	for (( SEED=1; SEED<=10; SEED++ ))
	do	
		#patch
		prefix="2>&1"
		PATHTOSOURCE=${PATHTOSOURCE#*$prefix} 
		cd $PATHTOSOURCE
		COM="patch -p10 -i $PATCH"
		eval $COM

		COM="perl $D4JLOCATION/framework/bin/defects4j coverage -w $D4JLOCATION/ExamplesCheckedOut/"$LOWERCASEPROJECT$BUG"Buggy -s "$TESTSUITEFOLDER"/"$PROJECT"-"$BUG"f-evosuite-branch."$SEED".tar.bz2"
		eval $COM

		#unpatch
		COM="patch -p10 -R -i $PATCH"
		eval $COM

		cd "$D4JLOCATION/ExamplesCheckedOut/"$LOWERCASEPROJECT$BUG"Buggy/"

		mv "$D4JLOCATION/ExamplesCheckedOut/"$LOWERCASEPROJECT$BUG"Buggy/coverage.xml" $COVERAGEOUTPUT"/Evosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Seed"$SEED"Coverage.xml" 
		mv "$D4JLOCATION/ExamplesCheckedOut/"$LOWERCASEPROJECT$BUG"Buggy/cobertura.ser" $COVERAGEOUTPUT"/Evosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Seed"$SEED"Cobertura.ser" 
	    FILESTOMERGE="$FILESTOMERGE $COVERAGEOUTPUT/Evosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Seed"$SEED"Cobertura.ser"
	done
	
	COM="bash ~/Cobertura/cobertura-2.1.1-bin/cobertura-2.1.1/cobertura-merge.sh --datafile $COVERAGEOUTPUT/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura.ser $FILESTOMERGE"
	echo $COM
	eval $COM
	
	COM="bash ~/Cobertura/cobertura-2.1.1-bin/cobertura-2.1.1/cobertura-report.sh --format xml --destination $COVERAGEOUTPUT/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura --datafile $COVERAGEOUTPUT/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura.ser"
	echo $COM
	eval $COM
	mv $COVERAGEOUTPUT"/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura/coverage.xml" $COVERAGEOUTPUT"/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Coverage.xml"
	rm -r $COVERAGEOUTPUT"/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura"
	
done






