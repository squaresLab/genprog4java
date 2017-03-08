#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess

d4jHome = os.environ['D4J_HOME']
defects4jCommand = d4jHome + "/framework/bin/defects4j"

class BugInfo(object):
	def __init__(self, project, bugNum, buggyFolder, fixedFolder, testSuitePath):
		self.project = project
		self.bugNum = bugNum
		self.buggyFolder = buggyFolder
		self.fixedFolder = fixedFolder
		self.testSuitePath = testSuitePath
		self.ensureVersionAreCheckedOut()

	def getProject(self):
		return str(self.project)

	def getBugNum(self):
		return str(self.bugNum)

	def getFixPath(self):
		return str(os.path.join(d4jHome, self.fixedFolder))

	def getBugPath(self):
		return str(os.path.join(d4jHome, self.buggyFolder))

	def getTestSuitePath(self):
		return str(os.path.join(d4jHome, self.testSuitePath))
	
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
	#print e
	lines = e.findall(".//line")
	#print listOfChangedLines
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
		#print realLine.attrib['hits']
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
	for method in tree.findall(".//method"):
		#print method.attrib['name']
		lines = method.find("lines")
		for line in lines:
			#print line.attrib['number']
			#print lineNum
			if(line.attrib['number'] == lineNum):
				methodLineCov= float(method.attrib['line-rate'])*100
				methodBranchCov=float(method.attrib['branch-rate'])*100
				methodsChanged.append(method.attrib['name']+": Line:" + str(methodLineCov) + "%" + " Branch:" + str(methodBranchCov) + "%" )
	return methodsChanged

def generateCovXML(bug, tool, seed):
	if(tool == "Evosuite"):
		testSuiteName="evosuite-branch"
	elif(tool == "Randoop"):
		testSuiteName="randoop"
	cmd = defects4jCommand + " coverage -w " + bug.getFixPath() + " -s " +  bug.getTestSuitePath()+bug.getProject()+"-"+bug.getBugNum()+"f-"+testSuiteName+"."+str(seed)+".tar.bz2"
	subprocess.call(cmd, shell=True) # this doesn't save the log or do any kind of error checking (yet!)

def getEditedFiles(bug):
	cmd = defects4jCommand + " export -p classes.modified"
	p = subprocess.Popen(cmd, shell=True, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	realpaths = [ line.strip().replace(".", "/") + ".java" for line in p.stdout ]
	return realpaths

# assume that file1, file2 are java files
def getADiff(buggyPath, fixedPath, pathToFile, bug):
	cmd = defects4jCommand + " export -p dir.src.classes"
	p = subprocess.Popen(cmd, shell=True, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	for line in p.stdout:
		pathToSource=line
	#print pathToSource

        cmd = "diff --unchanged-line-format=\"\"  --old-line-format=\"%dn \" --new-line-format=\"%dn \" " + buggyPath+"/"+pathToSource+"/"+pathToFile +" " + fixedPath+"/"+pathToSource+"/"+pathToFile
        #print cmd
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        for line in p.stdout:
                diffLines = line
	#print "Diff lines: "+diffLines
	return diffLines.split()
	

def getOptions():
	parser = argparse.ArgumentParser(description="This script checks if a test suite is covering the human changes")
	parser.add_argument("project", help="the project in upper case (ex: Lang, Chart, Closure, Math, Time)")
	parser.add_argument("bugNum", help="the bug number (ex: 1,2,3,4,...)")
	parser.add_argument("buggyFolder", help="folder to check out buggy version of the bug")
	parser.add_argument("fixedFolder", help="folder to check out fixed version of the bug")
	parser.add_argument("testSuiteFolder", help="the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)")
	parser.add_argument("--genTool", help="the generation tool (Randoop or Evosuite)", default="Evosuite")
	parser.add_argument("--seed", help="the seed the test suite was created with", default="1")
	parser.add_argument("--coverage", help="a coverage file")
	parser.add_argument("--file1", help="test parameter, first file to diff")
	parser.add_argument("--file2", help="test parameter, second file to diff")
	return parser.parse_args()


def main():
	args=getOptions()
	# TODO: insert error handling/sanity checking to be sure the appropriate environment variables are set and abort with an error/usage message if not
	# TODO: line wrap this file at 80 characters or so
	# TODO: make argument description sentences shorter

	bug = BugInfo(args.project, args.bugNum, args.buggyFolder, args.fixedFolder, args.testSuiteFolder)
	if(not os.path.exists(bug.getFixPath()+"/coverage.xml")):
		generateCovXML(bug,args.genTool, args.seed)
	for f in getEditedFiles(bug):
		listOfChangedLines = getADiff(bug.getBugPath(),bug.getFixPath(), f, bug)
		computeCoverage(listOfChangedLines, bug.getFixPath()+"/coverage.xml")

	if(not(args.file1 is None) and ( not (args.file2 is None))):
		getADiff(args.file1, args.file2)
	#print "the project you specified is: " + args.project

main()
