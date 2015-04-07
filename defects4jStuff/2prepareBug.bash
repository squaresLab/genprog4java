#!/bin/bash
# 1st param is the package in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)

#OVERALL TODO: I THINK WE CAN HAVE A TEMPLATE POS.TESTS AND THE TWO JARS AND JUST COPY PASTE THEM TO EACH BUG

#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
LOWERCASEPACKAGE="${1,,}"

#Specific variables per every project
#JAVADIR is the working directory of the project
if [ $LOWERCASEPACKAGE = "math" ]; then 
  JAVADIRTEST=src/test/java/org/apache/commons/math3
  JAVADIRMAIN=src/main/java/org/apache/commons/math3
#  LIBSMAIN=
#  LIBSTESTS="-cp \".:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allSourceClasses$1$2Bug.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/projects/Math/lib/commons-discovery-0.5.jar\" "
  CONFIGLIBS=" .:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allSourceClasses$1$2Bug.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allTestClasses$1$2Bug.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/projects/Math/lib/commons-discovery-0.5.jar"
#/home/mau/Research/defects4j/projects/Lang/lib/easymock.jar:/home/mau/Research/defects4j/projects/Lang/lib/asm.jar:/home/mau/Research/defects4j/projects/Lang/lib/cglib.jar:/home/mau/Research/defects4j/projects/Lang/lib/org/easymock/easymock/easymock-2.5.2.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/lang1Buggy/easymock-3.3.1.jar

elif [ $LOWERCASEPACKAGE = "lang" ]; then
  JAVADIRTEST=src/test/java/org/apache/commons/lang3
  JAVADIRMAIN=src/main/java/org/apache/commons/lang3
#  LIBSMAIN=
#  LIBSTESTS="-cp \".:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allSourceClasses$1$2Bug.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/projects/Lang/lib/easymock.jar:/home/mau/Research/defects4j/projects/Lang/lib/asm.jar:/home/mau/Research/defects4j/projects/Lang/lib/cglib.jar:/home/mau/Research/defects4j/projects/Lang/lib/org/easymock/easymock/easymock-2.5.2.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/lang1Buggy/easymock-3.3.1.jar\" "
  CONFIGLIBS=" .:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allSourceClasses$1$2Bug.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allTestClasses$1$2Bug.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/projects/Lang/lib/easymock.jar:/home/mau/Research/defects4j/projects/Lang/lib/asm.jar:/home/mau/Research/defects4j/projects/Lang/lib/cglib.jar:/home/mau/Research/defects4j/projects/Lang/lib/org/easymock/easymock/easymock-2.5.2.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/lang1Buggy/easymock-3.3.1.jar"
cp /home/mau/Research/defects4j/ExamplesCheckedOut/Utilities/EntityArrays.java /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/src/main/java/org/$JAVADIR/text/translate/

elif [ $LOWERCASEPACKAGE = "chart" ]; then
  JAVADIRTEST=tests/org/jfree #FILL THIS
  JAVADIRMAIN=source/org/jfree
#  LIBSMAIN=" -cp \".:/home/mau/Research/defects4j/framework/projects/lib/cobertura-lib/servlet-api-2.5-6.1.14.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/chart2Buggy/lib/servlet.jar\" "
#  LIBSTESTS=" -cp \".:/home/mau/Research/defects4j/framework/projects/lib/cobertura-lib/servlet-api-2.5-6.1.14.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/chart2Buggy/lib/servlet.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/chart2Buggy/lib/iText-2.1.4.jar:/home/mau/Research/defects4j/framework/projects/Time/lib/junit-4.11.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/chart2Buggy/chartAllSourceClasses.jar\" "
  CONFIGLIBS=

elif [ $LOWERCASEPACKAGE = "closure" ]; then
  JAVADIRTEST=test/com/google #FILL THIS
  JAVADIRMAIN=src/com/google
#  LIBSMAIN=
#  LIBSTESTS=
  CONFIGLIBS=

elif [ $LOWERCASEPACKAGE = "time" ]; then
  JAVADIRTEST=src/test/java/org/joda/time
  JAVADIRMAIN=src/main/java/org/joda/time
#  LIBSMAIN="-cp \".:/home/mau/Research/defects4j/framework/projects/Time/lib/joda-convert-1.2.jar\" "
#  LIBSTESTS="-cp \".:/home/mau/Research/defects4j/framework/projects/Time/lib/joda-convert-1.2.jar:/home/mau/Research/defects4j/framework/projects/Time/lib/junit-4.11.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allSourceClasses$1$2Bug.jar\" "
  CONFIGLIBS=" .:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allSourceClasses$1$2Bug.jar:/home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/allTestClasses$1$2Bug.jar:/home/mau/Research/defects4j/framework/projects/Time/lib/joda-convert-1.2.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/junittestrunner.jar:/home/mau/Research/genprog4java/tests/mathTest/lib/commons-io-1.4.jar:/home/mau/Research/defects4j/framework/projects/lib/junit-4.11.jar:/home/mau/Research/defects4j/framework/projects/lib/easymock-3.3.1.jar "

fi


#Go to the bug folder
#cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$JAVADIRMAIN/

#create file to run compilation
#FILENAME=sources.txt
#exec 3<>$FILENAME
# Write to file
#echo $LIBSMAIN >&3
#find -name "*.java" >&3
#" -Xlint:unchecked" >&3
# close fd # 3
#exec 3>&-

#Compile the project
#javac @sources.txt

#echo Compilation of main java classes successful
# -d AlreadyCompiled/ -Xlint:unchecked
#rm sources.txt

#cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/main/java

#where the .class files are
#DIROFCLASSFILES=org/$JAVADIR

#Jar all the .class's
#TODO maybe: change this to insert only the class files recursively, NOT the .java files also. Same thing in tests
#jar cf ../../../allSourceClasses"$1""$2"Bug.jar $DIROFCLASSFILES/* #$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 


#Compile test classes
#cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/test/java/org/$JAVADIR/

#FILENAME=sources.txt
#exec 3<>$FILENAME
# Write to file
#echo $LIBSTESTS >&3
#find -name "*.java" >&3
#echo " -Xlint:unchecked" >&3
#echo " -Xlint:deprecation" >&3
# close fd # 3
#exec 3>&-

#javac @sources.txt

#echo Compilation of test java classes successful
#rm sources.txt

#javac *.java */*.java */*/*.java */*/*/*.java */*/*/*/*.java -Xlint:unchecked

#cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/src/test/java

#DIROFCLASSFILES=org/$JAVADIR

#Jar all the test class's
#jar cf ../../../allTestClasses"$1""$2"Bug.jar $DIROFCLASSFILES/* #$DIROFCLASSFILES/*/*.class $DIROFCLASSFILES/*/*/*.class $DIROFCLASSFILES/*/*/*/*.class $DIROFCLASSFILES/*/*/*/*/*.class 

cd ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy

PACKAGEDIR=${DIROFCLASSFILES//"/"/"."}

#Create config file TODO:#FIX THIS FILE
FILE=~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/configDefects4j
/bin/cat <<EOM >$FILE
popsize = 5
seed = 0
javaVM = /usr/bin/java
workingDir = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/src/main/java/
sourceDir = $DIROFCLASSFILES/ FILL THIS OUT WHERE THE FILE TO MUTATE IS
outputDir = ./tmp
libs = $CONFIGLIBS
classDir = bin/
packageName = $PACKAGEDIR. FILL THIS OUT WHERE THE PACKAGE WHERE THE FILE TO MUTATE IS
targetClassName = NumberUtils FILL THIS OUT WITH THE NAME OF THE CLASS TO MUTATE
sanity = yes
regenPaths
positiveTests = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = /home/mau/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = /home/mau/Research/defects4j/framework/projects/lib/jacocoagent.jar
EOM

gedit ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/configDefects4j"$1""$2"

export PATH=$PATH:~/Research/defects4j/framework/bin
defects4j info -p $1 -v $2

echo Yo Mau, 

echo Now go to the config file in ~/Research/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/configDefects4j"$1""$2" and change the three necessary info where there are capital letters


#Para que funcionara las dependencias empece el folder jar en org y no en org/apache/commons/lang3

#Para crear los .class:
#javac DireccionDeLosArchivosJava/*.java -d FolderDestinoParaLosClass/
#Ejemplo:
#javac math/NumberUtils.java *.java */*.java */*/*.java -d AlreadyCompiled/ -Xlint:unchecked



#javac math/NumberUtils.java *.java */*.java */*/*.java -d AlreadyCompiled/ 

#Para crear el jar:
#jar cf nombreDelJar.jar direccionDeLosClass/*.class
#Ejemplo:
#jar cf allTestClassesLang1Bug.jar org/apache/commons/lang3/*.class org/apache/commons/lang3/*/*.class org/apache/commons/lang3/*/*/*.class 


