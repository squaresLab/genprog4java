#!/bin/bash
#
# 1st param: project name, sentence case (ex: Lang, Chart, Closure, Math, Time)
# 2nd param: bug number (ex: 1,2,3,4,...)
# 3rd param: location of genprog4java (ex: "/home/mau/Research/genprog4java/" )
# 4td param: defects4j installation (ex: "/home/mau/Research/defects4j/" )
# 5th param: testing option (ex: humanMade, generated)
# 6th param: test suite sample size (ex: 1, 100)

# Example usage, local for Mau
#./prepareBug.sh Math 2 /home/mau/Research/genprog4java/ /home/mau/Research/defects4j/ humanMade 100

# Example usage, VM:
#./prepareBug.sh Math 2 /home/ubuntu/genprog4java/ /home/ubuntu/defects4j/ allHuman 100

# OS X note, mostly for CLG: 
# javac has to be version 1.7, and JAVA_HOME must be set accordingly,
# So, don't forget to do the following on OS X:
# export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/
# export PATH=$JAVA_HOME/bin/:$PATH

if [ "$#" -ne 6 ]; then
    echo "This script should be run with 6 parameters: Project name, bug number, location of genprog4java, defects4j installation, testing option, test suite size"

else

PROJECT="$1"
BUGNUMBER="$2"
GENPROGDIR="$3"
DEFECTS4JDIR="$4"
OPTION="$5"
TESTSUITEPERCENTAGE="$6"

PARENTDIR=$DEFECTS4JDIR"/ExamplesCheckedOut"


#Add the path of defects4j so the defects4j's commands run 
export PATH=$PATH:"$DEFECTS4JDIR"/framework/bin
export PATH=$PATH:"$DEFECTS4JDIR"/major/bin

#copy these files to the source control

mkdir -p $PARENTDIR

LOWERCASEPACKAGE=`echo $PROJECT | tr '[:upper:]' '[:lower:]'`

# directory with the checked out buggy project
BUGWD=$PARENTDIR"/"$LOWERCASEPACKAGE"$BUGNUMBER"Buggy

#Checkout the buggy version of the code
defects4j checkout -p $1 -v "$BUGNUMBER"b -w $BUGWD

#Checkout the fixed version of the code, primarily to make the second test suite
defects4j checkout -p $1 -v "$BUGNUMBER"f -w "$DEFECTS4JDIR/"ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Fixed

#Compile the both buggy and fixed code
for dir in Buggy Fixed
do
    pushd $PARENTDIR"/"$LOWERCASEPACKAGE$BUGNUMBER$dir
    defects4j compile
    popd
done

#Specific variables per project
#TESTWD: location of project test files (relative to root)
#WD: location of project src files (relative to root)
#JAVADIR: path from the WD or TESTWD to location of java files for both source and test files 
#CONFIGLIBS: libraries to be included in the configuration file so that GenProg can run it.
#LIBSTESTS: libraries needed to compile the  tests (dependencies of the project)

# Common genprog libs: junit test runner and the like

GENLIBS=$GENPROGDIR"/lib/junittestrunner.jar:"$GENPROGDIR"/lib/commons-io-1.4.jar:"$GENPROGDIR"/lib/junit-4.10.jar"

# all libs for a package need at least the source jar, test jar, and generic genprog libs
CONFIGLIBS=""

EXTRACLASSES=""

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
        LIBSTESTS="-cp \".:$GENLIBS:$CHARTLIBS\" "
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
        LIBSTESTS="-cp \".:$GENLIBS:$CLOSURELIBS\" "
        EXTRACLASSES="$3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInfo.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInformationMap.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/FunctionInformationMapOrBuilder.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/Instrumentation.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/InstrumentationOrBuilder.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/javascript/jscomp/InstrumentationTemplate.java $3/defects4j/ExamplesCheckedOut/$LOWERCASEPACKAGE"$2"Buggy/gen/com/google/debugging/sourcemap/proto/Mapping.java"

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
        LIBSTESTS="-cp \".:\
$GENPROGDIR/lib/junittestrunner.jar:$GENPROGDIR/lib/commons-io-1.4.jar:\
$DEFECTS4JDIR/framework/projects/lib/junit-4.11.jar:\
$DEFECTS4JDIR/framework/projects/Lang/lib/easymock.jar:\
$DEFECTS4JDIR/framework/projects/lib/easymock-3.3.1.jar\" "

	    SRCFOLDER=target/classes
	    TESTFOLDER=target/tests
        # special handling...
        cp "$3"defects4j-scripts/Utilities/EntityArrays.java $BUGWD/src/main/java/org/apache/commons/lang3/text/translate/

        ;;

'math')
        TESTWD=src/test/java
        WD=src/main/java
        JAVADIR=org/apache/commons/math3
        MATHLIBS=$DEFECTS4JDIR"/framework/projects/Math/lib/commons-discovery-0.5.jar"
        CONFIGLIBS=$CONFIGLIBS":"$GENLIBS":"$MATHLIBS
        LIBSTESTS="-cp \".:$GENLIBS:$MATHLIBS\" "
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
        LIBSTESTS="-cp \".:$TIMELIBS\" "
        ;;
esac

cd $BUGWD/$WD

#Create file to run defects4j compiile

FILE="$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
/bin/cat <<EOM >$FILE
#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/
export PATH=$JAVA_HOME/bin/:$PATH
cd $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
$DEFECTS4JDIR/framework/bin/defects4j compile
EOM

chmod 777 "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh


cd $BUGWD

PACKAGEDIR=${JAVADIR//"/"/"."}

#Create config file 
FILE="$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/defects4j.config
/bin/cat <<EOM >$FILE
popsize = 20
seed = 0
javaVM = /usr/bin/java
workingDir = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/
outputDir = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/tmp
libs = $CONFIGLIBS
sanity = yes
regenPaths
sourceDir = $WD
positiveTests = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/pos.tests
negativeTests = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/neg.tests
jacocoPath = $3/lib/jacocoagent.jar
classSourceFolder = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$SRCFOLDER
classTestFolder = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$TESTFOLDER
compileCommand = $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/runCompile.sh
EOM

# programmatically get passing and failing tests as well as files
#info about the bug
INFO=`defects4j info -p $PROJECT -b $BUGNUMBER`

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


case "$OPTION" in
"humanMade" )
# default
;;

"onlyRelevant" ) 
        echo "not implemented yet."
        ;;

"generated" )
#Create the new test suite
echo Creating new test suite...
"$DEFECTS4JDIR"/framework/bin/run_evosuite.pl -p $PROJECT -v "$BUGNUMBER"f -n 1 -o $BUGWD/"$TESTWD"/outputOfEvoSuite/ -c branch -b 100 -A

#Untar the generated test into the tests folder
cd $BUGWD/"$TESTWD"/
tar xvjf outputOfEvoSuite/$PROJECT/evosuite-branch/1/"$PROJECT"-"$BUGNUMBER"f-evosuite-branch.1.tar.bz2
;;
esac


# get positive tests
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



#Remove a percentage of the positive tests in the test suite
cd "$DEFECTS4JDIR"/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/

if [[ $TESTSUITEPERCENTAGE -ne 100 ]]
then
TESTCOUNT=$(cat pos.tests | wc -l)

PERCENTAGETOREMOVE=$(echo "$TESTSUITEPERCENTAGE * 0.01" | bc -l )

PERCENTAGETOREMOVE=$(echo "1-$PERCENTAGETOREMOVE" | bc -l )

TESTCOUNT=$(echo "$TESTCOUNT * $PERCENTAGETOREMOVE" | bc -l )

TESTCOUNT=$(echo "($TESTCOUNT+0.5)/1" | bc)

DELETELINES="sed -i -e 1,"$TESTCOUNT"d pos.tests"

eval $DELETELINES

fi

TESTCOUNT=$(cat pos.tests | wc -l)

echo "The positive tests file has $TESTCOUNT lines. Which is a $TESTSUITEPERCENTAGE% of the original amount of test cases."



# get the class names to be repaired

echo "GETTING CLASS NAMES"

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
echo $DEFECTS4JDIR/ExamplesCheckedOut/$LOWERCASEPACKAGE$2Buggy/$WD

fi #correct amount of params if

