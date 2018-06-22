#!/usr/lib/bin

FILENAME=$1
VARIANTNAME=$2
VARIANTPROG=$3
CLASSP=$4

java daikon.PrintInvariants --format java $FILENAME.inv.gz> $FILENAME.wean 
java -cp .:$CLASSPATH:target/classes/ ylyu1.wean.WeanParse $FILENAME NOTDEBUG
java -cp .:lib/javassist.jar:$CLASSP ylyu1.wean.Modify $FILENAME $VARIANTNAME #> $VARIANTNAME.log 
java -cp .:$CLASSP $VARIANTNAME $VARIANTPROG> $VARIANTNAME.tuo
java -cp .:target/classes/ ylyu1.wean.Aggregator $VARIANTNAME > $VARIANTNAME.pred
