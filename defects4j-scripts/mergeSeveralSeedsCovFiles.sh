#!/bin/bash

#Example of usage:  ./mergeSeveralSeedsCovFiles.sh G /home/mausoto/defects4j/patchesGenerated/JavaRepair-results/GenProgPatches/ PAR OutPut

HorG="$1"
BUGLISTSOURCE="$2"
ID="$3"
OUTPUT="$4"

#D4JLOCATION="/home/mausoto/defects4j/"
D4JLOCATION="$D4J_HOME"

COVERAGEOUTPUT="$ROOTFOLDER/SuperMergedFiles/"


if [ "$#" -lt 1 ]; then
    echo "This script should be run with 1 parameter: Folder where the patches are located"
    exit 0
fi

export PATH=$PATH:$D4JLOCATION/framework/bin/
export PATH=$PATH:$D4JLOCATION/framework/util/
export PATH=$PATH:$D4JLOCATION/major/bin/

mv "$D4J_HOME/ExamplesCheckedOut$TSSEED/$ID*$TSSEED*/$PROJECTANDBUG"*.ser
FILESTOMERGE=""

if [ HorG == "G" ]; then
	PATCHES=$BUGLISTSOURCE/*.diff
	for PATCH in $PATCHES
	do
		PATCHNAME=${PATCH##*/}
		PROJECTANDBUG=$(echo $PATCHNAME | cut -d'_' -f 1)
		gatherFiles ""
		merge ""
	done

elif [ HorG == "H" ]; then
	while read line; do
		PROJECTANDBUG=$(echo "${line//,}")
		gatherFiles ""
		merge ""
	done < $BUGLISTSOURCE
fi



gatherFiles(){
	PROJECTANDBUG=$(echo ${PROJECTANDBUG^})
	echo $PROJECTANDBUG
	for (( TSSEED=1; TSSEED<=10; TSSEED++ ))
	do	
		N30=$(ls -d "$D4J_HOME/ExamplesCheckedOut$TSSEED/$ID"Seed"$TSSEED/$PROJECTANDBUG"*.ser)
		echo $N30
		FILESTOMERGE="$FILESTOMERGE $N30"
	done
}

merge(){
	COM="bash ~/Cobertura/cobertura-2.1.1-bin/cobertura-2.1.1/cobertura-merge.sh --datafile $D4J_HOME/MergedFiles/$PROJECTANDBUG.ser $FILESTOMERGE"
	echo $COM
	eval $COM
		
	COM="bash ~/Cobertura/cobertura-2.1.1-bin/cobertura-2.1.1/cobertura-report.sh --format xml --destination $COVERAGEOUTPUT/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura --datafile $COVERAGEOUTPUT/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura.ser"
	echo $COM
	eval $COM
	mv $COVERAGEOUTPUT"/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura/coverage.xml" $COVERAGEOUTPUT"/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Coverage.xml"
	rm -r $COVERAGEOUTPUT"/MERGEDEvosuite"$EVOSUITEVER"Budget"$BUDGET$PROJECT$BUG"Patch"$PATCHNAME"Cobertura"
}


