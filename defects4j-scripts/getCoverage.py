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

	def getSrcPath(self):
		return str(self.srcPath)

	#this needs to be called after the buggy folder has been checked out
	def setScrPath(self):
		cmd = defects4jCommand + " export -p dir.src.classes"
		p = subprocess.Popen(cmd, shell=True, cwd=self.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		for i in p.stdout:
			self.srcPath = i

	def setPatch(self,patch):
		self.patch=patch #this is the path from d4jHome + the patch name

	def getPatch(self):
		return str(os.path.join(d4jHome,"/"+self.patch))
	
		
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
	#print "Lines modified: " + str(linesChanged) 
	#print "Percentage of modified lines covered: " + str(percentageLinesCovered) + "%"
	#print "Methods changed and corresponding Line/Branch coverage: " + str(methodsChanged)

	classLineCoverage=0
	classConditionCoverage=0

	for coverage in e.findall("coverage"):
		classLineCoverage=coverage.attrib['line-rate']
		classConditionCoverage=coverage.attrib['branch-rate']

	classLineCoverage*=100
	classConditionCoverage*=100
	
	ret = str(classLineCoverage) + "%," + str(classConditionCoverage) + "%," +  str(linesChanged) + "," + str(percentageLinesCovered)+"%,"
	for m in methodsChanged:
		ret = ret+str(m)+" " 
	return ret

def printMethodCorrespondingToLine(lineNum, tree):
	methodsChanged=[]
	for method in tree.findall(".//method"):
		lines = method.find("lines")
		for line in lines:
			if(line.attrib['number'] == lineNum):
				methodLineCov= float(method.attrib['line-rate'])*100
				methodBranchCov=float(method.attrib['branch-rate'])*100
				methodsChanged.append(method.attrib['name']+":{Line:" + str(methodLineCov) + "%" + " Branch:" + str(methodBranchCov) + "%}" )
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
	pathToSource=bug.getSrcPath()
	cmd = "diff --unchanged-line-format=\"\"  --old-line-format=\"%dn \" --new-line-format=\"%dn \" " + bug.getBugPath()+"/"+pathToSource+"/"+pathToFile +" " + bug.getFixPath()+"/"+pathToSource+"/"+pathToFile
	p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
	for line in p.stdout:
		diffLines = line
	return diffLines.split()

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

def getBugsFromPatchNames(bugs,args):
	cmd = "ls -d *.diff"
	p = subprocess.Popen(cmd, shell=True, cwd=d4jHome+args.patches, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	for line in p.stdout:
		defect=line.split('_')[0]
		bug=int(filter(str.isdigit, defect))
		project=str(filter(str.isalpha, defect)).title()
		bug = BugInfo(project, int(bug), args.wd, args.testDir)
		bug.setPatch(args.patches+"/"+line)
		bugs.append(bug)

def patchFixedFile(bug):
	pathToSource=bug.getSrcPath()

	if(not os.path.exists(bug.getFixPath())):
		os.remove(bug.getFixPath())
	checkout(bug.getFixPath(),bug.getProject(), bug.getBugNum(), "b")
		
	whereToCallPatch=str(bug.getFixPath())+"/"+str(pathToSource)
	cmd ="patch -p10 -i "+d4jHome+"/"+bug.getPatch()
	p = subprocess.Popen(cmd, shell=True, cwd=whereToCallPatch, stdout=subprocess.PIPE, stderr=subprocess.PIPE)


def ensureVersionAreCheckedOut(bug):
	if(not os.path.exists(bug.getBugPath())):
		checkout(bug.getBugPath(), bug.getProject(), bug.getBugNum(), "b")
	if(not os.path.exists(bug.getFixPath())):
		checkout(bug.getFixPath(), bug.getProject(), bug.getBugNum(), "f")

def checkout(folderToCheckout, project, bugNum, vers):
	cmd = defects4jCommand + " checkout -p " + str(project) + " -v " + str(bugNum) + str(vers) + " -w " + str(folderToCheckout)
	p = subprocess.call(cmd, shell=True) #, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	

def getOptions():
	parser = argparse.ArgumentParser(description="This script checks if a test suite is covering the human changes. Example of usage: python getCoverage.py ExamplesCheckedOut generatedTestSuites/Evosuite30MinsPAR/testSuites/ --project Closure --bug 38")
	parser.add_argument("wd", help="working directory to check out project versions, starting from the the D4J_HOME folder")
	parser.add_argument("testDir", help="the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)")
	parser.add_argument("--project", help="the project in upper case (ex: Lang, Chart, Closure, Math, Time)")
	parser.add_argument("--bug", help="the bug number (ex: 1,2,3,4,...)")
	parser.add_argument("--many", help="file listing bugs to process: project,bugNum (one per line). Lines starting with # are skipped")
	parser.add_argument("--patches", help="the folder where the patches are located, starting from the the D4J_HOME folder")
	parser.add_argument("--tool", help="the generation tool (Randoop or Evosuite)", default="Evosuite")
	parser.add_argument("--seed", help="the seed the test suite was created with", default="1")
	parser.add_argument("--coverage", help="a coverage file")
	return parser.parse_args()


def main():
	args=getOptions()
	if(os.environ['D4J_HOME'] is None):
		sys.exit("Environment variable D4J_HOME is not set")
	if(not os.path.isdir(os.path.join(d4jHome, args.wd))):
		sys.exit("The folder " + str(os.path.join(d4jHome, args.wd)) + " does not exist")
	if(not os.path.isdir(os.path.join(d4jHome, args.testDir))):
		sys.exit("The folder " + str(os.path.join(d4jHome, args.testDir)) + " does not exist")
	if(not(args.many is None) and ((not(args.project is None) or not(args.bug is None) or not(args.patch is None)))):
		sys.exit("There should be just one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if(not(args.patches is None) and ((not(args.project is None) or not(args.bug is None) or not(args.many is None)))):
		sys.exit("There should be just one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if((not(args.project is None) and not(args.bug is None)) and (not(args.patches is None)  or not(args.many is None))):
		sys.exit("There should be just one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if(args.project is None and args.bug is None and args.patches is None and args.many is None):
		sys.exit("There should be one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if(not(args.tool is None)):
		if(args.tool != "Randoop" and args.tool != "Evosuite"):	
			sys.exit("tool should be Randoop or Evosuite")
	if(not(args.seed is None) and (not (args.seed.isdigit()))):
		sys.exit("Seed should be an integer")
	if(not(args.patches is None) and (not os.path.isdir(os.path.join(d4jHome, args.patches)))):
		sys.exit("The folder " + str(os.path.join(d4jHome, args.patches)) + " does not exist")
	if(not(args.coverage is None) and (not os.path.isfile(args.coverage))):
		sys.exit("The file " + str(args.coverage) + " does not exist")
	# TODO: line wrap this file at 80 characters or so	

	#removes outputfile if exists
	outputFile= str(d4jHome)+ str(args.wd) + "/coverageOfBugs.csv"
	if(os.path.isfile(outputFile)):
		os.remove(outputFile)
	
	#fill bug list
	bugs = []
	if(not(args.project is None)):
		bugs.append(BugInfo(args.project, args.bug, args.wd, args.testDir))
	elif(not(args.many is None)):
		getAllBugs(bugs, args)
	elif(not(args.patches is None)):
		getBugsFromPatchNames(bugs,args)

	for bug in bugs:
		ensureVersionAreCheckedOut(bug)
		bug.setScrPath()
		if not args.patches is None:
			patchFixedFile(bug)
		if((args.coverage == None) and (not os.path.exists(bug.getFixPath()+"/coverage.xml"))):
			generateCovXML(bug,args.tool, args.seed)
		for f in getEditedFiles(bug):
			listOfChangedLines = getADiff(f, bug)
			allCoverageMetrics=computeCoverage(listOfChangedLines, bug.getFixPath()+"/coverage.xml")
			#pipes the result to a csv file
			cmd = "echo "+str(allCoverageMetrics)+ " >> "+ outputFile
			p = subprocess.call(cmd, shell=True)#, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			
	print "Results in "+outputFile
main()
