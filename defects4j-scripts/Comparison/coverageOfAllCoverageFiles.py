#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess
import sys
import shutil
import time

#USAGE
# python coverageOfAllCoverageFiles.py /home/mausoto/defects4j/generatedTestSuites/Evosuite3MinGenProgFixesEvosuite020Comparison/coverageFiles/

budget=3

def getMethodsNonCovered(coverageFile):
	e = xml.etree.ElementTree.parse(coverageFile).getroot()
	ret = ""	
	for mxml in e.findall(".//method"):
		if (float(mxml.attrib['line-rate']) == 0):
			ret +=str(mxml.attrib['name'])+";"
			ret = ret.replace("/bin/sh","000")
	#print ret
	return ret

def getInitialCoverageMetrics(coverageFile):
	#Class coverage
	e = xml.etree.ElementTree.parse(coverageFile).getroot()
	classLineCoverage=round(float(e.attrib['line-rate']),2)
	classConditionCoverage=round(float(e.attrib['branch-rate']),2)
	#print str(classLineCoverage) + "," + str(classConditionCoverage)
	ret = str(classLineCoverage) + "," + str(classConditionCoverage)
	return ret
	
def getBugsFromPatchNames(coverageFilesFolder):

	cmd = "echo \"Version,Bugdet, Merged/Averaged,Bug,Patch,Avg Line Coverage, Avg Branch Coverage,LineCov Variance, BranchCov Variance\" >> "+ coverageFilesFolder+"/0CoveragesFromAllFiles.csv"
	p = subprocess.call(cmd, shell=True) #, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		

	cmd = "ls -d *Coverage.xml"
	toPrint=""
	#print "antes"
	#print "file"+coverageFilesFolder
	p = subprocess.Popen(cmd, shell=True, cwd=coverageFilesFolder, stdout=subprocess.PIPE, stderr=subprocess.PIPE)	
	#print "depois"
	oldSignature=""
	LineCovs = []
	BranchCovs = []

	for line in p.stdout:
		line=line.strip()

		if not line.startswith( 'MERGED' ):
		
			lastIndexWordSeed=line.rfind('Seed')
			signature=line[:lastIndexWordSeed]
			
			if oldSignature == signature:
				#print "they are the same"
				landb = getInitialCoverageMetrics(coverageFilesFolder+"/"+line)
				LineCovs.append(float(landb.split(",")[0]))
				BranchCovs.append(float(landb.split(",")[1]))
			else:
				shift=0
				merged="Averaged"
				
				lastIndexWordCoverage=line.rfind('Coverage')
				seed=line[lastIndexWordSeed+4:lastIndexWordCoverage]
				
				version=line[shift+8:shift+11]
				indexWordPatch=line.find('Patch')
				indexWordDiff=line.rfind('patch')
				patch=line[indexWordPatch+5:indexWordDiff]
				indexUnderscore=line.find('_')
				bug=line[indexWordPatch+5:indexUnderscore]
				
				#print "Signature:"+signature
				#print "OLDSignature:"+oldSignature
				#wraps up the one that just past
				#print line  
				
				if not oldSignature == "":
					print LineCovs
					print BranchCovs
					toPrint=str(oldVer)+","+str(oldBud)+"," + str(merged) +"," +str(oldBug)+","+str(oldPatch)
					toPrint+=","+str(round(mean(LineCovs),2))+","+str(round(mean(BranchCovs),2))+","+str(round(variance(LineCovs),2))+","+str(round(variance(BranchCovs),2))
					print toPrint
					cmd = "echo \""+str(toPrint)+ "\" >> "+ coverageFilesFolder+"/0CoveragesFromAllFiles.csv"
					p = subprocess.call(cmd, shell=True) #, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
				
				#starts a new one
				LineCovs = []
				BranchCovs = []
		
				landb = getInitialCoverageMetrics(coverageFilesFolder+"/"+line)
				LineCovs.append(float(landb.split(",")[0]))
				BranchCovs.append(float(landb.split(",")[1]))
				
			oldSignature=signature
			oldVer=version
			oldBud=budget
			oldBug=bug
			oldPatch=patch
			#oldSeed=seed
		else:
			shift=6
			merged="Merged"
			
			version=line[shift+8:shift+11]
			indexWordPatch=line.find('Patch')
			indexWordDiff=line.rfind('patch')
			patch=line[indexWordPatch+5:indexWordDiff]
			indexUnderscore=line.find('_')
			bug=line[indexWordPatch+5:indexUnderscore]

			#print patch
			toPrint=str(version)+","+str(budget)+","+ str(merged)+","+str(bug) +","+str(patch)+","+str(getInitialCoverageMetrics(coverageFilesFolder+"/"+line))+","
			unCoveredMethods=getMethodsNonCovered(coverageFilesFolder+"/"+line)
			toPrint+=unCoveredMethods
			print toPrint
			cmd = "echo \""+str(toPrint)+ "\" >> "+ coverageFilesFolder+"/0CoveragesFromAllFiles.csv"
			p = subprocess.call(cmd, shell=True) #, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			
			
def variance(mylist):
    
        mymean = mean(mylist)
        mylen = len(mylist)
        temp = 0
        
        for i in range(mylen):
            temp += (float(mylist[i]) - mymean) * (float(mylist[i]) - mymean) 
        return temp / mylen;
		
		
def sumfunc(num):
     return float(num) 
 
def sumlist(mylist, sumfunc):
    sum = 0
    for i in mylist:
        sum = sum + sumfunc(i)
    return sum
	
def mean(mylist):
    meanval = 0
 
    if(len(mylist) == 0):
       return 0
    else:
       meanval = sumlist(mylist,sumfunc)/len(mylist)
       return meanval
			
def getOptions():
	parser = argparse.ArgumentParser(description="This script checks if a test suite is covering the human changes. Example of usage: python getCoverage.py ExamplesCheckedOut generatedTestSuites/Evosuite30MinsPAR/testSuites/ --project Closure --bug 38")
	parser.add_argument("coverageFilesFolder", help="folder where the coverage files are located")
	return parser.parse_args()

def main():
	args=getOptions()
	getBugsFromPatchNames(args.coverageFilesFolder)
	
main()
