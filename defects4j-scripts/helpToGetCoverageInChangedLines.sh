#!/bin/bash

P="Closure"
B="25"
F="com"
TF="Evosuite30MinGenProgFixes"

LOWERCASEPACKAGE=`echo $P | tr '[:upper:]' '[:lower:]'`

clear
cd /home/mausoto/defects4j/generatedTestSuites/$TF/testSuites/
C0="tar -cjvf $P-"$B"f-evosuite-branch.1.tar.bz2 $F/"
eval $C0
diff --exclude=.git --exclude=.defects4j.config --exclude=defects4j.build.properties --exclude=.svn -r /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$B"Buggy /home/mausoto/defects4j/ExamplesCheckedOut/"$LOWERCASEPACKAGE""$B"Fixed
cd /home/mausoto/genprog4java/defects4j-scripts/
C1="sed -i '43s/.*/declare -a bugs=(\"$P $B\")/' "isTestSuiteCoveringChangesList.sh
eval $C1
echo ""
echo "-------------------------------------------------------------------------"
./isTestSuiteCoveringChangesList.sh Evosuite BugsWithAFix generatedTestSuites/$TF/testSuites/ ExamplesCheckedOut
echo ""
echo "-------------------------------------------------------------------------"
cat /home/mausoto/defects4j/generatedTestSuites/$TF/testSuites/$P-"$B"CoverageLog.txt
