#!/bin/bash

#The purpose of this script is to answer weather a particular test suite covers the changes performed by the human in the human fix and by the automatic approach when fixed by genprog.

#Preconditions:
#The variable D4J_HOME should be directed to the folder where defects4j is installed.
#The variable JAVA_HOME should be directed to the folder where java 7 is installed (It must be Java 7).

#Output
#Outputs if the human and generated changes are covered by the test suite in $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt

#Parameters:
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3th param is the generation tool (Randoop or Evosuite)
# 4th param is the path where the test suite is located
# 5th param is the path of the folder where the bugs will be checked out. Starting in $D4J_HOME

#Example of usage:
#./isTestSuiteCoveringChanges.sh Math 2 Randoop generatedTestSuites/Randoop3Min/ ExamplesCheckedOut

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$D4J_HOME"/framework/bin/
export PATH=$PATH:"$D4J_HOME"/framework/util/
export PATH=$PATH:"$D4J_HOME"/major/bin/
export JRE_HOME=$JAVA_HOME/jre
export PATH=$JAVA_HOME/bin/:$PATH

PROJECT="$1"
BUGNUMBER="$2"
RANDOOPOREVOSUITE="$3"
PATHOFSUITEFOLDER="$4"
PATHTOCHECKOUTFOLDERS="$5"

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

#rm -f $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"Fixed/coverage.xml
rm -rf $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"Buggy
rm -rf $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"Fixed


#Checkout the buggy and fixed versions of the code 
defects4j checkout -p $PROJECT -v "$BUGNUMBER"b -w $D4J_HOME/$PATHTOCHECKOUTFOLDERS"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy
defects4j checkout -p $PROJECT -v "$BUGNUMBER"f -w $D4J_HOME/$PATHTOCHECKOUTFOLDERS"/"$LOWERCASEPACKAGE"$BUGNUMBER"Fixed

#Run coverage of the indicated test suite on the human fix
#./checkCoverageOfGeneratedTestSuite.sh $PROJECT $BUGNUMBER $RANDOOPOREVOSUITE $PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"Fixed/ $PATHOFSUITEFOLDER
#Changed for AllPublic
./checkCoverageOfGeneratedTestSuite.sh $PROJECT $BUGNUMBER $RANDOOPOREVOSUITE $PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"FixedPatched/ $PATHOFSUITEFOLDER

#Diff between buggy version and human fix
diff --exclude=.git --exclude=.defects4j.config --exclude=defects4j.build.properties --exclude=.svn -qr $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"Buggy $D4J_HOME/$PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"Fixed &>> $D4J_HOME/$PATHTOCHECKOUTFOLDERS/filesChanged.txt

filesChanged=$D4J_HOME/$PATHTOCHECKOUTFOLDERS/filesChanged.txt
while read lineOfFilesChanged
do
#echo "Analyzing this line from the diff: $lineOfFilesChanged"
word1=$(echo $lineOfFilesChanged | cut -d " " -f 1)
word5=$(echo $lineOfFilesChanged | cut -d " " -f 5)
if [ $word1 = "Files" ] && [ $word5 = "differ" ]; then
  #echo "New File: $lineOfFilesChanged"
  file1=$(echo $lineOfFilesChanged | cut -d " " -f 2)
  file2=$(echo $lineOfFilesChanged | cut -d " " -f 4)
  echo ""
  echo "File: $file2"
  diff --unchanged-line-format="" --old-line-format="" --new-line-format="%dn
" $file1 $file2 2>&1 | tee -a $D4J_HOME/$PATHTOCHECKOUTFOLDERS/lineNumbersChanged.txt #Think about the cases where the fix is a delete

  atLeastOneNonZero="false"
  atLeastOneZero="false"

  linesChanged=$D4J_HOME/$PATHTOCHECKOUTFOLDERS/lineNumbersChanged.txt
  while read lineChanged
  do
    echo "Line changed: $lineChanged"
    alreadyFound="false"  
#    coveragePath=$D4J_HOME/$PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"Fixed/coverage.xml

#Changed for AllPublic
    coveragePath=$D4J_HOME/$PATHTOCHECKOUTFOLDERS/$LOWERCASEPACKAGE"$BUGNUMBER"FixedPatched/coverage.xml
    while read coverageXMLLine
    do
	  if [[ $coverageXMLLine = *"<line number=\"$lineChanged\""* ]]
	  then
	  if [[ $alreadyFound = "false" ]]
	  then
	    word3=$(echo $coverageXMLLine | cut -d " " -f 3)
		hits=${word3#hits=\"} 
		hits=${hits%\"} 
		alreadyFound="true"
		#echo "Hits: $hits"
		
		if [ $hits != "0" ]; then
		  echo "Covered"
		  atLeastOneNonZero="true"
		fi
		if [ $hits = "0" ]; then
		  echo "Not covered"
		  atLeastOneZero="true"
		fi
	  fi
	  fi
    done < $coveragePath
	if [[ $alreadyFound = "false" ]]; then
	  echo "Not covered"
	  atLeastOneZero="true"
	fi
  done < $linesChanged
 
  rm $D4J_HOME/$PATHTOCHECKOUTFOLDERS/lineNumbersChanged.txt
fi
done < $filesChanged
rm $D4J_HOME/$PATHTOCHECKOUTFOLDERS/filesChanged.txt

echo ""
echo "$PROJECT $BUGNUMBER:" >> $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt
if [ $atLeastOneNonZero = "true" ] && [ $atLeastOneZero = "false" ]; then
  echo "HUMAN CHANGES FULLY COVERED"
  echo "HUMAN CHANGES FULLY COVERED" >> $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt
fi
if [ $atLeastOneNonZero = "false" ] && [ $atLeastOneZero = "true" ]; then
  echo "HUMAN CHANGES NOT COVERED"
  echo "HUMAN CHANGES NOT COVERED" >> $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt
fi
if [ $atLeastOneNonZero = "true" ] && [ $atLeastOneZero = "true" ]; then
  echo "HUMAN CHANGES PARTIALLY COVERED"
  echo "HUMAN CHANGES PARTIALLY COVERED" >> $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt
fi
if [ $atLeastOneNonZero = "false" ] && [ $atLeastOneZero = "false" ]; then
  echo "HUMAN CHANGES NOT COVERED"
  echo "HUMAN CHANGES NOT COVERED" >> $D4J_HOME/$PATHTOCHECKOUTFOLDERS/ChangesCovered.txt
fi

#----------------------------------------------------------------------------------------------------------------------------------------------------------------
