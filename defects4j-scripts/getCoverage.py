#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess
import sys

d4jHome = os.environ['D4J_HOME']
defects4jCommand = d4jHome + "/framework/bin/defects4j"

class BugInfo(object):
	def __init__(self, project, bugNum, wd, testDir):
		self.project = project
		self.bugNum = bugNum
		self.buggyFolder = wd + "/" + project.lower() + str(bugNum) + "Buggy"
		self.fixedFolder = wd + "/" + project.lower() + str(bugNum) + "Fixed"
		self.testDir = testDir
		self.ensureVersionAreCheckedOut()

	def getProject(self):
		return str(self.project)

	def getBugNum(self):
		return str(self.bugNum)

	def getFixPath(self):
		return str(os.path.join(d4jHome, self.fixedFolder))

	def getBugPath(self):
		return str(os.path.join(d4jHome, self.buggyFolder))

	def getTestDir(self):
		return str(os.path.join(d4jHome, self.testDir))
	
	def ensureVersionAreCheckedOut(self):
		if(not os.path.exists(self.getBugPath())):
			self.checkout(self.getBugPath(), "b")
		if(not os.path.exists(self.getFixPath())):
			self.checkout(self.getFixPath(), "f")

	def checkout(self, folderToCheckout, vers):
		cmd = defects4jCommand + " checkout -p " + self.project + " -v " + self.bugNum + vers + " -w " +folderToCheckout
		p = subprocess.call(cmd, shell=True) #, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		
def computeCoverage(listOfChangedLines, coverageFile):
#	if(not (args.coverage is None)):
	e = xml.etree.ElementTree.parse(coverageFile).getroot()
	lines = e.findall(".//line")
	realLines = []
	lineNumbersCoveredAlready = []
	for line in lines:
		if(line.attrib['number'] in listOfChangedLines):
			if(not (line.attrib['number'] in lineNumbersCoveredAlready)):
				realLines.append(line)
				lineNumbersCoveredAlready.append(line.attrib['number'])

	linesCovered=0
	for realLine in realLines:
		# check if covered
		if(int(realLine.attrib['hits']) != 0):
			linesCovered += 1

		methodsChanged = printMethodCorrespondingToLine(realLine.attrib['number'], e)

	linesChanged=len(realLines)
	percentageLinesCovered=linesCovered*100/linesChanged
	print "Lines modified: " + str(linesChanged) 
	print "Percentage of modified lines covered: " + str(percentageLinesCovered) + "%"
	print "Methods changed and corresponding Line/Branch coverage: " + str(methodsChanged)

def printMethodCorrespondingToLine(lineNum, tree):
	methodsChanged=[]
	for method in tree.findall(".//method"):]
		lines = method.find("lines")
		for line in lines:
			if(line.attrib['number'] == lineNum):
				methodLineCov= float(method.attrib['line-rate'])*100
				methodBranchCov=float(method.attrib['branch-rate'])*100
				methodsChanged.append(method.attrib['name']+": Line:" + str(methodLineCov) + "%" + " Branch:" + str(methodBranchCov) + "%" )
	return methodsChanged

def generateCovXML(bug, tool, seed):
	if(tool.lower() == "evosuite"):
		testSuiteName="evosuite-branch"
	elif(tool.lower() == "randoop"):
		testSuiteName="randoop"
	suitePath =  os.path.join(bug.getTestDir(), bug.getProject()+"-"+bug.getBugNum()+"f-"+testSuiteName+"."+str(seed)+".tar.bz2")
	if(os.path.isfile(suitePath)):
		cmd = defects4jCommand + " coverage -w " + bug.getFixPath() + " -s " + str(suitePath)
		subprocess.call(cmd, shell=True) # this doesn't save the log or do any kind of error checking (yet!)
	else:
		sys.exit("The script did not find a test suite: " + str(suitePath))
def getEditedFiles(bug):
	cmd = defects4jCommand + " export -p classes.modified"
	p = subprocess.Popen(cmd, shell=True, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	return [ line.strip().replace(".", "/") + ".java" for line in p.stdout ]


def getADiff(pathToFile, bug):
	cmd = defects4jCommand + " export -p dir.src.classes"
	p = subprocess.Popen(cmd, shell=True, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	for line in p.stdout:
		pathToSource=line

        cmd = "diff --unchanged-line-format=\"\"  --old-line-format=\"%dn \" --new-line-format=\"%dn \" " + bug.getBugPath()+"/"+pathToSource+"/"+pathToFile +" " + bug.getFixPath()+"/"+pathToSource+"/"+pathToFile
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        for line in p.stdout:
			diffLines = line
	return diffLines.split()
	

def getOptions():
	parser = argparse.ArgumentParser(description="This script checks if a test suite is covering the human changes. Example of usage: python getCoverage.py ExamplesCheckedOut generatedTestSuites/Evosuite30MinsPAR/testSuites/ --project Closure --bug 38")
	parser.add_argument("wd", help="working directory to check out project versions, starting from the the D4J_HOME folder")
	parser.add_argument("testDir", help="the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)")
	parser.add_argument("--project", help="the project in upper case (ex: Lang, Chart, Closure, Math, Time)")
	parser.add_argument("--bug", help="the bug number (ex: 1,2,3,4,...)")
	parser.add_argument("--many", help="file listing bugs to process: project,bugNum (one per line). Lines starting with # are skipped")
	parser.add_argument("--tool", help="the generation tool (Randoop or Evosuite)", default="Evosuite")
	parser.add_argument("--seed", help="the seed the test suite was created with", default="1")
	parser.add_argument("--coverage", help="a coverage file")
	return parser.parse_args()

#args.many is assumed to be not None
def getAllBugs(bugs,args):
	if(not os.path.isfile(args.many)):
		sys.exit("The file " + str(args.many) + " does not exist")
	else:
		with open(args.many) as f:
			pairs = [x.strip().split(',') for x in f.readlines() if x[0] != '#']
			for pair in pairs:
				bug = BugInfo(pair[0], int(pair[1]), args.wd, args.testDir)
				bugs.append(bug)
def main():
	args=getOptions()
	if(os.environ['D4J_HOME'] is None):
		sys.exit("Environment variable D4J_HOME is not set")
	if(not os.path.isdir(os.path.join(d4jHome, args.wd))):
		sys.exit("The folder " + str(args.wd) + " does not exist")
	if(not os.path.isdir(os.path.join(d4jHome, args.testDir))):
		sys.exit("The folder " + str(args.testDir) + " does not exist")
	if(args.many is None and (args.project is None or args.bug is None)):
		sys.exit("Either a file with a list of bugs should be provided with the --many parameter, or a particular bug with the --project and --bug parameters")
	if(not(args.tool is None)):
		if(args.tool != "Randoop" and args.tool != "Evosuite"):	
			sys.exit("tool should be Randoop or Evosuite")
	if(not(args.seed is None) and (not (args.seed.isdigit()))):
		sys.exit("Seed should be an integer")
	if(not(args.coverage is None) and (not os.path.isfile(args.coverage))):
		sys.exit("The file " + str(args.coverage) + " does not exist")
	# TODO: line wrap this file at 80 characters or so	
	bugs = []
	if(args.many == None):
		bugs.append(BugInfo(args.project, args.bug, args.wd, args.testDir))
	else:
		getAllBugs(bugs, args)
	for bug in bugs:
		if((args.coverage == None) and (not os.path.exists(bug.getFixPath()+"/coverage.xml"))):
			generateCovXML(bug,args.tool, args.seed)
		for f in getEditedFiles(bug):
			listOfChangedLines = getADiff(f, bug)
			computeCoverage(listOfChangedLines, bug.getFixPath()+"/coverage.xml")

main()
