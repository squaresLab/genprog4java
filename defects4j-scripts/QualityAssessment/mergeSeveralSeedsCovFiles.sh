#!/bin/bash

#this script is meant to be ran right after getCoverage.py
#It copies all the folders with results, with xml and ser files to a central folder in D4J_HOME/MergedCoverages/

#Example of usage:  ./mergeSeveralSeedsCovFiles.sh G /home/mausoto/defects4j/patchesGenerated/JavaRepair-results/PARPatches/ PAR
#Example of usage:  ./mergeSeveralSeedsCovFiles.sh H /home/mausoto/QualityEvaluationDefects4jGenProg/AllBugsFixedByAllApproaches.csv HUMAN

D4JLOCATION="$D4J_HOME"
HorG="$1"
BUGLISTSOURCE="$2"
ID="$3"
OUTPUTFOLDER=$D4JLOCATION"/MergedCoverages/"$ID"/"
FILESTOMERGE=""

moveFoldersToOutputFolder(){
	for (( TSSEED=1; TSSEED<=10; TSSEED++ ))
	do	
		com="ls -d "$D4JLOCATION"/ExamplesCheckedOut"$TSSEED"/"$ID"*TSSeed"$TSSEED"*/"
		FOLDERTOMOVE=$($com)
		com="mv "$FOLDERTOMOVE" "$OUTPUTFOLDER"/"$ID"TSSeed"$TSSEED
		#echo $com
		#TO MOVE FOLDERS FROM EXAMPLESCHECKEDOUT TO MERGEDCOVERAGES UNDOCUMENT THIS
		#eval $com
	done
}

gatherFiles(){
	PROJECTANDBUG="$1"
	PATCHSEED="$2"
	PROJECTANDBUG=$(echo ${PROJECTANDBUG^})
	FILESTOMERGE=""
	#echo $PROJECTANDBUG
	for (( TSSEED=1; TSSEED<=10; TSSEED++ ))
	do	
		com="ls -d "$OUTPUTFOLDER"/"$ID"TSSeed"$TSSEED"/"$PROJECTANDBUG
		if [ $HorG == "G" ]; then
			com+="Patch"$PATCHSEED
		fi
		com+=".ser"
		SERFILE=$($com)
		FILESTOMERGE="$FILESTOMERGE $SERFILE"
	done
	echo $FILESTOMERGE
}

merge(){
	PROJECTANDBUG="$1"
	PATCHSEED="$2"
	#merge all .ser files
	COM="bash ~/Cobertura/cobertura-2.1.1-bin/cobertura-2.1.1/cobertura-merge.sh --datafile "$OUTPUTFOLDER"/"$PROJECTANDBUG
	if [ $HorG == "G" ]; then
			COM+="Patch"$PATCHSEED
		fi
	COM+=".ser "$FILESTOMERGE
	#echo $COM
	eval $COM 
	
	echo ""
	
	#turn .ser into .xml
	FILEDIR=$OUTPUTFOLDER"/"$PROJECTANDBUG
	if [ $HorG == "G" ]; then
		FILEDIR+="Patch"$PATCHSEED
	fi
	#creates folder in $FILEDIR with a file named coverage.xml
	COM="bash ~/Cobertura/cobertura-2.1.1-bin/cobertura-2.1.1/cobertura-report.sh --format xml --destination "$FILEDIR" --datafile "$FILEDIR".ser"
	
	#echo $COM
	eval $COM
	mv $FILEDIR"/coverage.xml" $FILEDIR".xml"
	rm -r $FILEDIR
}

if [ "$#" -lt 3 ]; then
    echo "This script should be run with 3 parameter: Folder where the patches are located"
    exit 0
fi

export PATH=$PATH:$D4JLOCATION/framework/bin/
export PATH=$PATH:$D4JLOCATION/framework/util/
export PATH=$PATH:$D4JLOCATION/major/bin/

#rm -rf $OUTPUTFOLDER
mkdir $OUTPUTFOLDER
#mv "$D4JLOCATION/ExamplesCheckedOut$TSSEED/$ID*$TSSEED*/$PROJECTANDBUG"*.ser

moveFoldersToOutputFolder
if [ $HorG == "G" ]; then
	PATCHES=$BUGLISTSOURCE/*.diff
	for PATCH in $PATCHES
	do
		PATCHNAME=${PATCH##*/}
		PROJECTANDBUG=$(echo $PATCHNAME | cut -d'_' -f 1)
		PATCHSEED=$(echo $PATCHNAME | cut -d'_' -f 2)
		gatherFiles $PROJECTANDBUG $PATCHSEED
		merge $PROJECTANDBUG $PATCHSEED
		
	done

elif [ $HorG == "H" ]; then
	while read line; do
		PROJECTANDBUG=$(echo "${line//,}")
		#echo "$PROJECTANDBUG"
		gatherFiles $PROJECTANDBUG $PATCHSEED
		merge $PROJECTANDBUG $PATCHSEED
	done < $BUGLISTSOURCE
fi



