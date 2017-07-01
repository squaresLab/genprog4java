#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess
import sys
import shutil
import time

#USAGE
# python methodCoverageFromMergedXML.py /home/mausoto/defects4jJava8/defects4j/MergedCoverages/GP/ /home/mausoto/defects4jJava8/defects4j/MergedCoverages/GP/GPTSSeed1/GPTSSeed1Generated.csv /home/mausoto/defects4jJava8/defects4j/MergedCoverages/GP/MergedCovsReport.csv

def getMethodCoverage(path,coverageFile,methodName, minimumLCov, minimumBCov):
	#print coverageFile
	e = xml.etree.ElementTree.parse(path+coverageFile).getroot()
	classLineCoverage=round(float(e.attrib['line-rate'])*100,2)
	classConditionCoverage=round(float(e.attrib['branch-rate'])*100,2)
	ret = ","+ str(classLineCoverage) + "," + str(classConditionCoverage)
	for mxml in e.findall(".//method"):
		methodBeingAnalyzed=str(mxml.attrib['name'])
		#print methodBeingAnalyzed + "==" + methodName + str(str(methodBeingAnalyzed) == str(methodName))
		if (methodBeingAnalyzed == methodName):
			methodLineCoverage=round(float(mxml.attrib['line-rate'])*100,2)
			methodConditionCoverage=round(float(mxml.attrib['branch-rate'])*100,2)
			if float(minimumLCov) > methodLineCoverage or float(minimumBCov) > methodConditionCoverage:
				print "Check this case: "+ coverageFile
				methodLineCoverage=minimumLCov
				methodConditionCoverage=minimumBCov
			#print str(methodLineCoverage) + "," + str(methodConditionCoverage)
			ret += ","+methodName+","+str(methodLineCoverage) + "," + str(methodConditionCoverage)
			break
	print "method: "+ ret
	return ret
	
def getOptions():
	parser = argparse.ArgumentParser(description="This script tells the class and method coverage of the changed methods from a file. Example of usage:   python methodCoverageFromMergedXML.py /home/mausoto/defects4jJava8/defects4j/MergedCoverages/GP/ /home/mausoto/defects4jJava8/defects4j/MergedCoverages/GP/GPTSSeed1/GPTSSeed1Generated.csv /home/mausoto/defects4jJava8/defects4j/MergedCoverages/GP/MergedCovsReport.csv")
	parser.add_argument("coverageFilesFolder", help="folder where the coverage files are located")
	parser.add_argument("individualCovList", help="file with list of methods modificed by patches")
	parser.add_argument("destinationFile", help="full path of file for output")
	parser.add_argument("--human", help="is it human or not")
	return parser.parse_args()

def main():
	args=getOptions()
	subprocess.call("rm "+ args.destinationFile, shell=True)
	if args.human is None:
		cmd = "echo \"Project,Bug, Patch Seed, Mutation, Class Line Cov, Class Branch Cov, Method changed, Method Line Cov, Method Branch Cov\" >> "+ args.destinationFile
	else:
		cmd = "echo \"Project,Bug, Class Line Cov, Class Branch Cov, Method changed, Method Line Cov, Method Branch Cov, Method changed, Method Line Cov, Method Branch Cov, Method changed, Method Line Cov, Method Branch Cov, Method changed, Method Line Cov, Method Branch Cov\" >> "+ args.destinationFile
	p = subprocess.call(cmd, shell=True) #, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	
	with open(args.individualCovList) as f:
		for line in f:
			line=line.strip()
			if "," in line and not ("Project,Bug" in line):
				bugName= str(line.split(',')[0]) + str(line.split(',')[1]) 
				if args.human is None:
					methodList=line.split(',')[6:]
					patchSeed=line.split(',')[2]
				else: 
					methodList=line.split(',')[4:]
				while len(methodList)>0:
					print methodList
					methodName= methodList[0]
					minimumMethodLCov= methodList[1]
					minimumMethodBCov= methodList[2]
					#print methodName+","+minimumMethodLCov+","+minimumMethodBCov
					#print "methodList before changing: "+ str(methodList)
					methodList=methodList[3:]
					#print "methodList after changing: "+ str(methodList)
					#print bugName +","+ methodName
					methodName=methodName.strip()
					cmd = "ls -d *"+bugName
					if args.human is None:
						cmd +="PatchSeed"+ str(patchSeed)
					cmd+=".xml"
					p = subprocess.Popen(cmd, shell=True, cwd=args.coverageFilesFolder, stdout=subprocess.PIPE, stderr=subprocess.PIPE)	
					for nameOfFile in p.stdout:
						#print nameOfFile
						nameOfFile=nameOfFile.strip()
						changedMethodInfo = str(getMethodCoverage(args.coverageFilesFolder,nameOfFile,methodName,minimumMethodLCov,minimumMethodBCov))
						firstSeveralAttributes= str(line.split(',')[0])+","+str(line.split(',')[1])
						if args.human is None:
							firstSeveralAttributes+=","+str(line.split(',')[2])+","+str(line.split(',')[3])
						cmd = "echo \""+firstSeveralAttributes+changedMethodInfo+ "\" >> "+ args.destinationFile
						print str(firstSeveralAttributes+changedMethodInfo)
						p = subprocess.call(cmd, shell=True) #, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			else:
				cmd = "echo \""+str(line) +"\" >> "+ args.destinationFile
				p = subprocess.call(cmd, shell=True)
main()
