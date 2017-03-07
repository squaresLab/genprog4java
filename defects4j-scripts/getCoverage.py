#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess

#Get the modified classes from defects -p export SOMEATTRIBUTES classes.modified. They are in stdout. 

def getOptions():
	parser = argparse.ArgumentParser(description="")
	parser.add_argument("project", help="1st param is the project in upper case (ex: Lang, Chart, Closure, Math, Time)")
	parser.add_argument("bugNum", help="2nd param is the bug number (ex: 1,2,3,4,...)")
	parser.add_argument("--genTool", help="3th param is the generation tool (Randoop or Evosuite)", default="Randoop")
	parser.add_argument("--buggyFolder", help="4th param is the path of the buggy folder, starting from the the D4J_HOME folder (Example: ExamplesCheckedOut or BugsWithAFix)")
	parser.add_argument("--testSuiteFolder", help="5th param is the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)")
	parser.add_argument("--coverage", help="a coverage file")
        parser.add_argument("--file1", help="test parameter, first file to diff")
        parser.add_argument("--file2", help="test parameter, second file to diff")
	return parser.parse_args()

class BugInfo(object):
	def __init__(self, project, d4jDir, pathToFix):
		self.project = project
		self.d4jDir = d4jDir
		self.pathToFix = pathToFix

	def fixedPath(self):
		return str(os.path.join(self.d4jDir, self.pathToFix))

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
	
# assume that file1, file2 are java files
def getADiff(file1, file2):
        cmd = "diff --unchanged-line-format=\"\" --old-line-format=\"\" --new-line-format=\"\%dn\n " + file1 + file2
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE)
        for line in p.stdout:
                print line
def main():
	args=getOptions()
	# insert error handling/sanity checking to be sure the appropriate environment variables are set and abort with an error/usage message if not
	# also, line wrap this file at 80 characters or so
	# and make your argument description sentences shorter

	d4jHome = os.environ['D4J_HOME']
        if(not(args.file1 is None) and (not (args.file2 is None))):
                getADiff(args.file1, args.file2)
	print "the project you specified is: " + args.project

main()
