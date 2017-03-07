#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess

d4jHome = os.environ['D4J_HOME']
defects4jCommand = d4jHome + "/framework/bin/defects4j"

class BugInfo(object):
	def __init__(self, project, bugNum, buggyFolder, fixedFolder):
		self.project = project
		self.bugNum = bugNum
		self.buggyFolder = buggyFolder
		self.fixedFolder = fixedFolder
		self.ensureVersionAreCheckedOut()

	def getFixPath(self):
		return str(os.path.join(d4jHome, self.fixedFolder))

	def getBugPath(self):
		return str(os.path.join(d4jHome, self.buggyFolder))
	
	def ensureVersionAreCheckedOut(self):
		if(not os.path.exists(self.buggyFolder)):
			self.checkout(self.buggyFolder, "b")
		if(not os.path.exists(self.fixedFolder)):
			self.checkout(self.fixedFolder, "f")

	def checkout(self, folderToCheckout, vers):
		cmd = defects4jCommand + " checkout -p " + self.project + " -v " + self.bugNum + vers + " -w " + d4jHome + "/" + folderToCheckout
		p = subprocess.call(cmd, shell=True) #, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		


def computeCoverage(args):
        numberOfLinesChanged=0
	numberOfLinesCovered=0

	if(not (args.coverage is None)):
		e = xml.etree.ElementTree.parse(args.coverage).getroot()
		print e
		lines = e.findall(".//line")
		linesWereLookingFor = [103, 105, 106]
		realLines = [line for line in lines if int(line.attrib['number']) in linesWereLookingFor]
		for realLine in realLines:
			# check if covered
			print realLine

def generateCovXML(d4j, bug, tool):
	cmd = d4j + " coverage -w " + bug.fixedPath() + " -s " +  bug.suitePath(tool) # note that suitepath doesn't exist yet
	subprocess.call(cmd, shell=True) # this doesn't save the log or do any kind of error checking (yet!)
	
def getEditedFiles():
	cmd = defects4jCommand + " export -p classes.modified"
	p = subprocess.Popen(cmd, shell=True, cwd="/home/mau/Research/defects4j/ExamplesCheckedOut/closure35FixedPatched", stdout=subprocess.PIPE)
	realpaths = [ line.strip().replace(".", "/") + ".java" for line in p.stdout ]
	return realpaths

# assume that file1, file2 are java files
def getADiff(file1, file2):
        cmd = "diff  --unchanged-line-format=\"\"  --old-line-format=\"%dn \" --new-line-format=\"%dn \" " + file1 +" " + file2
        print cmd
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        for line in p.stdout:
		print line

def getOptions():
	parser = argparse.ArgumentParser(description="")
	parser.add_argument("project", help="1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)")
	parser.add_argument("bugNum", help="2nd param is the bug number (ex: 1,2,3,4,...)")
	parser.add_argument("buggyFolder", help="folder to check out buggy version of the bug")
        parser.add_argument("fixedFolder", help="folder to check out fixed version of the bug")
	parser.add_argument("--genTool", help="3th param is the generation tool (Randoop or Evosuite)", default="Randoop")
	parser.add_argument("--testSuiteFolder", help="5th param is the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)")
	parser.add_argument("--coverage", help="a coverage file")
        parser.add_argument("--file1", help="test parameter, first file to diff")
        parser.add_argument("--file2", help="test parameter, second file to diff")
       
	return parser.parse_args()


def main():
	args=getOptions()
	# insert error handling/sanity checking to be sure the appropriate environment variables are set and abort with an error/usage message if not
	# also, line wrap this file at 80 characters or so
	# and make your argument description sentences shorter

	bug = BugInfo(args.project, args.bugNum, args.buggyFolder, args.fixedFolder)

        for f in getEditedFiles():
                bugVersion = bug.getBugPath() + "/" + f
                fixedVersion = bug.getFixPath() + "/" + f
                gedtADiff(bugVersion,fixedVersion)

        if(not(args.file1 is None) and (not (args.file2 is None))):
                getADiff(args.file1, args.file2)
	print "the project you specified is: " + args.project

main()
