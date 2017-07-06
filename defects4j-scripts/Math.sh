#!/bin/bash
X=1
while [ $X -le 5 ]
do
	bash prepareBug.sh Mockito $X humanMade 1 /Users/ashleychen/Desktop/reuse/defects4j/ExamplesCheckOut /Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/bin /Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/bin
        X=$((X+1))
done
