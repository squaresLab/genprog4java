#!/bin/bash
# 1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param is the bug number (ex: 1,2,3,4,...)
# 3rd param is the folder where the genprog project is (ex: "/home/mau/Research/genprog4java/" )
# 4td param is the folder where defects4j is installed (ex: "/home/mau/Research/defects4j/" )
# 5th param is the option of running it (ex: allHuman, oneHuman, oneGenerated)

#Mau runs it like this:
#./prepareBug.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ allHuman

# in case it helps, in my machine, I Have:
# /home/mau/Research/genprog4j where the source code for genprog is
# /home/mau/Research/defects4j where the defects4j source code is
# /home/mau/Research/defects4j/ExamplesCheckedOut where every time that I check out a bug from defects4j, it goes here

# note to self for CLG: 
# for compilation to work, javac really has to be 1.7 and JAVA_HOME set accordingly,
# even though defects4j ships with a version of the javac compiler.
# So, don't forget to do the following on your OS X laptop:
# export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/
# export PATH=$JAVA_HOME/bin/:$PATH

#2. Add Defects4J's executables to your PATH:
#    - `export PATH=$PATH:"path2defects4j"/framework/bin`
# export PATH="path2defects4j/major/bin"$PATH

# CLG thinks it's nice practice to rename the vars taken from the user to
# something more readable that corresponds to how they're used.  Makes the
# script easier to read.
PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
OPTION="$5"

PARENTDIR=$DEFECTS4JDIR"/ExamplesCheckedOut"

#copy these files to the source control

mkdir -p $PARENTDIR
cp -r $GENPROGDIR"/defects4jStuff/Utilities" $PARENTDIR

#This transforms the first parameter to lower case. Ex: lang, chart, closure, math or time
# CLG changed the way you did this (which was fine for Bash 4!) so it's a bit more platform-independent
LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGWD=$PARENTDIR"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Specific variables per every project
#TESTWD is the address from the root to the address where JAVADIR starts, for the TEST files 
#WD is the address from the root to the address where JAVADIR starts,  for the SOURCE files 
#JAVADIR is the address from the WD or TESTWD, to the address where all the java files are for both source and test files 
#It is usually used TESTWD/JAVADIR or WD/JAVADIR
#CONFIGLIBS are the libraries to be included in the configuration file so that GenProg can run it.
#LIBSTESTS are the libraries needed to compile the  tests (dependencies of the project)
#LIBSMAIN are the libraries needed to compile the project (dependencies of the project)

SRCJAR=$BUGWD"/"$LOWERCASEPACKAGE"AllSourceClasses.jar"
TESTJAR=$BUGWD"/"$LOWERCASEPACKAGE"AllTestClasses.jar"

# Common genprog libs: junit test runner and the like

GENLIBS=$GENPROGDIR"/lib/junittestrunner.jar:"$GENPROGDIR"/lib/commons-io-1.4.jar:"$GENPROGDIR"/lib/junit-4.10.jar"

# all libs for a package need at least the source jar, test jar, and generic genprog libs
CONFIGLIBS=$SRCJAR":"$TESTJAR


case "$LOWERCASEPACKAGE" in 
'chart') 
        TESTWD=tests
        WD=source
        JAVADIR=org/jfree
        CHARTLIBS="$BUGWD/lib/itext-2.0.6.jar:\
$BUGWD/lib/servlet.jar:\
$BUGWD/lib/junit.jar"

	SRCFOLDER=build
	TESTFOLDER=build-tests
        
        CONFIGLIBS=$CONFIGLIBS":"$GENLIBS":"$CHARTLIBS
        LIBSTESTS="-cp \".:$SRCJAR:$GENLIBS:$CHARTLIBS\" "
        LIBSMAIN="-cp \".:$CHARTLIBS\" "
        ;;
'closure')
        TESTWD=test
        WD=src
        JAVADIR=com/google

        CLOSURELIBS="$BUGWD/lib/ant.jar:$BUGWD/lib/ant-launcher.jar:\
$BUGWD/lib/args4j.jar:$BUGWD/lib/caja-r4314.jar:\
$BUGWD/lib/guava.jar:$BUGWD/lib/jarjar.jar:\
$BUGWD/lib/json.jar:$BUGWD/lib/jsr305.jar:\
$BUGWD/lib/junit.jar:$BUGWD/lib/protobuf-java.jar:\
$BUGWD/build/lib/rhino.jar:"

	SRCFOLDER=build/classes
	TESTFOLDER=build/test
        
        CONFIGLIBS=$CONFIGLIBS":"$GENLIBS":"$CLOSURELIBS


 #LIBSTESTS="-cp \".:"$3"genprog4java/tests/mathTest/lib/junittestrunner.jar:"$3"genprog4java/tests/mathTest/lib/commons-io-1.4.jar:"$3"genprog4java/tests/mathTest/lib/junit-4.10.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant-launcher.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/args4j.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/caja-r4314.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/guava.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jarjar.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/json.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jsr305.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/junit.jar:"$3"defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/protobuf-java.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/build/lib/rhino.jar\" "
#Add a comment to this line
#LIBSMAIN="-cp \".:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/ant-launcher.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/args4j.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/caja-r4313.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/guava.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jarjar.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/json.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/jsr305.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/junit.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/lib/protobuf-java.jar:"$3"/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/build/lib/rhino.jar\" "
 

        LIBSTESTS="-cp \".:$SRCJAR:$GENLIBS:$CLOSURELIBS\" "
        LIBSMAIN="-cp \".:$CLOSURELIBS\" "
        ;;

'lang')
        TESTWD=src/test/java
        WD=src/main/java
        JAVADIR=org/apache/commons/lang3 

        LANGLIBS="$GENPROGDIR/lib/junittestrunner.jar:$GENPROGDIR/lib/commons-io-1.4.jar:\
$DEFECTS4JDIR/framework/projects/lib/junit-4.11.jar:\
$DEFECTS4JDIR/framework/projects/Lang/lib/easymock.jar:\
$DEFECTS4JDIR/framework/projects/Lang/lib/asm.jar:\
$DEFECTS4JDIR/framework/projects/Lang/lib/cglib.jar:\
$DEFECTS4JDIR/framework/projects/lib/easymock-3.3.1.jar"
        CONFIGLIBS=$CONFIGLIBS:$LANGLIBS
        LIBSTESTS="-cp \".:$SRCJAR:\
$GENPROGDIR/lib/junittestrunner.jar:$GENPROGDIR/lib/commons-io-1.4.jar:\
$DEFECTS4JDIR/framework/projects/lib/junit-4.11.jar:\
$DEFECTS4JDIR/framework/projects/Lang/lib/easymock.jar:\
$DEFECTS4JDIR/framework/projects/lib/easymock-3.3.1.jar\" "
        LIBSMAIN=""

	SRCFOLDER=target/classes
	TESTFOLDER=target/tests
        ;;

'math')
        TESTWD=src/test/java
        WD=src/main/java
        JAVADIR=org/apache/commons/math3
        MATHLIBS=$DEFECTS4JDIR"/framework/projects/Math/lib/commons-discovery-0.5.jar"
        CONFIGLIBS=$CONFIGLIBS":"$GENLIBS":"$MATHLIBS
        LIBSTESTS="-cp \".:$SRCJAR:$GENLIBS:$MATHLIBS\" "
        LIBSMAIN=""

	SRCFOLDER=target/classes
	TESTFOLDER=target/test-classes
        ;;

'time')
        TESTWD=src/test/java
        WD=src/main/java
        JAVADIR=org/joda/time
        TIMELIBS=$DEFECTS4JDIR"/framework/projects/Time/lib/joda-convert-1.2.jar:"$GENLIBS":"$DEFECTS4JDIR/"framework/projects/lib/easymock-3.3.1.jar"
        CONFIGLIBS=$CONFIGLIBS":"$TIMELIBS

	SRCFOLDER=target/classes
	TESTFOLDER=target/test-classes

        LIBSTESTS="-cp \".:$SRCJAR:$TIMELIBS\" "
        LIBSMAIN="-cp \".:$DEFECTS4JDIR/framework/projects/Time/lib/joda-convert-1.2.jar\" "
        ;;
esac

#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$DEFECTS4JDIR"/framework/bin

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$BUGNUMBER"b -w $BUGWD

#Checkout the fixed version of the code to make the seccond test suite
defects4j checkout -p $1 -v "$BUGNUMBER"f -w "$DEFECTS4JDIR/"ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed


#Compile the buggy and fixed code
for dir in Buggy Fixed
do
    pushd $PARENTDIR"/"$LOWERCASEPACKAGE$BUGNUMBER$dir
    defects4j compile
    popd
done

#for the lang project copy a fixed file
if [ $LOWERCASEPACKAGE = "lang" ]; then
cp "$3"defects4jStuff/Utilities/EntityArrays.java $BUGWD/src/main/java/org/apache/commons/lang3/text/translate/
fi



#Create the new test suite
#echo Creating new test suite...
#"$4"/framework/bin/run_evosuite.pl -p $1 -v "$2"f -n 1 -o $BUGWD/"$TESTWD"/outputOfEvoSuite/ -c branch => 100s

#Untar the generated test into the tests folder
#cd $BUGWD/"$TESTWD"
#tar xvjf outputOfEvoSuite/$1/evosuite-branch/1/"$1"-"$2"f-evosuite-branch.1.tar.bz2

EXTRACLASSES=""
if [ $LOWERCASEPACKAGE = "closure" ]; then
EXTRACLASSES="$3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInfo.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInformationMap.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInformationMapOrBuilder.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/Instrumentation.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/InstrumentationOrBuilder.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/InstrumentationTemplate.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/debugging/sourcemap/proto/Mapping.java"
fi
 
 #Jar all the .class's
cd "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE"$BUGNUMBER"Buggy/"$SRCFOLDER"/ 
jar cf "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE"$BUGNUMBER"Buggy/"$LOWERCASEPACKAGE"AllSourceClasses.jar "$JAVADIR"/* 

echo "Jar of source files created successfully."
 
 #Jar all the test class's
cd "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE"$BUGNUMBER"Buggy/"$TESTFOLDER"/
jar cf "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE"$BUGNUMBER"Buggy/"$LOWERCASEPACKAGE"AllTestClasses.jar "$JAVADIR"/* 

echo "Jar of test files created successfully."


cd $BUGWD/$WD

#Create file to run defects4j compiile
# FIXME: This won't work on not-Claire's machine, but she's testing for now
FILE="$4"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/
export PATH=$JAVA_HOME/bin/:$PATH
cd $4/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
$4/framework/bin/defects4j compile
EOM

chmod 777 "$4"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh


cd $BUGWD

PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file 
FILE="$4"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
popsize = 5
seed = 0
testsDir = $TESTWD/$JAVADIR
javaVM = /usr/bin/java
workingDir = $4/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
outputDir = $4/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/tmp
libs = $CONFIGLIBS
sanity = yes
regenPaths
sourceDir = $WD
positiveTests = $4/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $4/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $3/lib/jacocoagent.jar
classSourceFolder = $SRCFOLDER
compileCommand = $4/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
EOM



# programmatically get passing and failing tests as well as files
#info about the bug
INFO=`defects4j info -p $PROJECT -v $BUGNUMBER`

# gets the content starting at the list of tests
JUSTTEST=`echo $INFO | sed -n -e 's/.*Root cause in triggering tests: - //p'`
#gets rid of the information about which assertions are failing, between test class names
JUSTTEST=`echo $JUSTTEST | sed -e "s/\([a-zA-Z0-9_\.]*\)\(::\)\([a-zA-Z0-9_\.]* --> \)\([a-zA-Z0-9<>: _\.]* - \)/\1 /g"`
# gets rid of the training bit of info at the end of the test list
JUSTTEST=`echo $JUSTTEST | sed -n -e 's/\([a-zA-Z0-9_\.]*\)\(::\)\(.*\)/\1/p'`

# I really wish I could come up with a better way to do this, but have not. 
if [[ -f tmp.txt ]]
then
    rm tmp.txt
fi
touch tmp.txt

# tests in this var are separated by a space, so this will enumerate over each
for foo in `echo $JUSTTEST`
do
    echo $foo >> tmp.txt
done

# gets the unique test classes in the list
UNIQTESTS=`cat tmp.txt | sort -n | uniq`

for FOO in `echo $UNIQTESTS`
do
    echo $FOO >> $BUGWD/neg.tests
done


# get positive tests
case "$OPTION" in
"allHuman" )
    pushd $BUGWD
    if [[ -f "print.xml" ]] 
        then
        rm "print.xml"
    fi
    echo "<project name=\"Ant test\">" >> print.xml
    echo "<import file=\"$DEFECTS4JDIR/framework/projects/defects4j.build.xml\"/>" >> print.xml
    echo "<import file=\"$DEFECTS4JDIR/framework/projects/"$PROJECT"/"$PROJECT".build.xml\"/>" >> print.xml
    echo "<echo message=\"Fileset is: \${toString:all.manual.tests}\"/>" >> print.xml
    echo "</project>" >> print.xml
    ANTOUTPUT=`ant -buildfile print.xml -Dd4j.home=$DEFECTS4JDIR`
    rm print.xml

    postests=`echo $ANTOUTPUT | sed -n -e 's/.*Fileset is: //p'`
    postests=`echo $postests | sed -n -e 's/\(.*\)\( BUILD SUCCESSFUL.*\)/\1/p'`
    postests=`echo $postests | sed -e 's/;/ /g'`

    suffix1=".java"
    suffix2=".class"

    if [[ -f pos.tests ]]
    then
        rm pos.tests
    fi
    for i in $postests
    do
        i=`echo "$i" | tr '/' '.'`
        i=`echo $i | sed "s/$suffix1$//" | sed "s/$suffix2$//"` 
	echo "$i" >> pos.tests
    done

    for i in $UNIQTESTS
    do
        echo $i
        grep -v "$i" pos.tests > tmp.txt
        mv tmp.txt pos.tests
    done
  popd
;;

"oneHuman" )
  echo "write in this file: "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests, the human made test in the bug info"
  gedit "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

;;

"onlyRelevant" ) 
        echo "not implemented yet."
        ;;

"oneGenerated" )
  echo "write in this file: "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests, the generated test called NAMEOFTHETARGETFILEEvoSuite_Branch.java"
  gedit "$4"ExamplesCheckedOut/"$LOWERCASEPACKAGE""$2"Buggy/pos.tests

;;

esac

# get the class names to be repaired

JUSTSOURCE=`echo $INFO | sed -n -e 's/.*List of modified sources: - //p'`

JUSTSOURCE=`echo $JUSTSOURCE | sed -e 's/ - / /g'`
JUSTSOURCE=`echo $JUSTSOURCE | cut -d '-' -f1`

if [[ -f tmp.txt ]]
then
    rm tmp.txt
fi

for foo in `echo $JUSTSOURCE`
do
    echo $foo >> tmp.txt
done

UNIQFILES=`cat tmp.txt | sort -n | uniq`
rm tmp.txt

for FOO in `echo $UNIQFILES`
do
    echo $FOO >> tmp.txt
done

NUM=`wc -l tmp.txt | xargs | cut -d ' ' -f1`

if [[ $NUM -gt 1 ]]
then
    mv tmp.txt $BUGWD/bugfiles.txt
    echo "targetClassName = $BUGWD/bugfiles.txt" >> $BUGWD/defects4j.config
else
    rm tmp.txt
    echo "targetClassName = "$UNIQFILES >> $BUGWD/defects4j.config
fi

echo "This is the working directory: "
echo $4/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD

