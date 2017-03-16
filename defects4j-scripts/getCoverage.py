#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess
import sys
import shutil

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
			self.srcPath = str(i).split("2>&1")[-1].strip()

	def setPatch(self,patch):
		self.patch=patch #this is the path from d4jHome + where it is + the patch name

	def getPatch(self):
		return str(os.path.join(d4jHome,"/"+self.patch))
	
		
def computeCoverage(listOfChangedLines, coverageFile):
#	if(not (args.coverage is None)):
	print [i for i in listOfChangedLines]
	e = xml.etree.ElementTree.parse(coverageFile).getroot()
	lines = e.findall(".//line")
	changedLinesInXML = []
	lineNumbersCoveredAlready = []
	for line in lines:
		if(line.attrib['number'] in listOfChangedLines):
			if(not (line.attrib['number'] in lineNumbersCoveredAlready)):
				changedLinesInXML.append(line)
				lineNumbersCoveredAlready.append(line.attrib['number'])

	linesCovered=0
	methodsChanged=[]
	for realLine in changedLinesInXML:
		# check if covered
		if(int(realLine.attrib['hits']) != 0):
			linesCovered += 1

	methodsForThisLine=[]
	for changedLine in listOfChangedLines:
		#print "Changed Lines: "+ str(listOfChangedLines)
		#print "Changed line: "+ str(changedLine)
		for m in printMethodCorrespondingToLine(changedLine, e):
			methodsForThisLine.append(m)
	
	methodsChanged= list(set(methodsForThisLine))


	linesChanged=len(listOfChangedLines)
	percentageLinesCovered=round(linesCovered*100/linesChanged,2)
	
	#Class coverage
	classLineCoverage=round(float(e.attrib['line-rate'])*100,2)
	classConditionCoverage=round(float(e.attrib['branch-rate'])*100,2)
	
	ret = str(classLineCoverage) + "," + str(classConditionCoverage) + "," +  str(linesChanged) + "," + str(percentageLinesCovered)+","+str(len(methodsChanged))
	for m in methodsChanged:
		methodLineCov= round(float(m.attrib['line-rate'])*100,2)
		methodBranchCov=round(float(m.attrib['branch-rate'])*100,2)
		ret = ret+","+m.attrib['name']+"," + str(methodLineCov) +"," + str(methodBranchCov)
	return ret

def printMethodCorrespondingToLine(lineNum, tree):
	methodsChanged=[]
	for method in tree.findall(".//method"):
		lines = method.find("lines")
		if int(len(lines)) > 0:
			firstLineOfMethod = lines[0]
#			print str(firstLineOfMethod.attrib['number'])
			highestLineNum=0
			for l in lines:
				if int(l.attrib['number']) > highestLineNum:
					highestLineNum= int(l.attrib['number'])
#			lastLineOfMethod = highestLineNum
#			print str(highestLineNum)
#			print "line number: "+ str(lineNum)
#			print "line num: " + str(lineNum) + " first: "+str(int(firstLineOfMethod.attrib['number']))+ " last: "+str(highestLineNum)
			if int(lineNum) >= int(firstLineOfMethod.attrib['number']) and int(lineNum) <= highestLineNum:
				methodsChanged.append(method)

#			for line in lines:
#				if(line.attrib['number'] == lineNum and not method in methodsChanged):
#					methodsChanged.append(method)
				
	return methodsChanged

def generateCovXML(bug, tool, seed):
	if(tool.lower() == "evosuite"):
		testSuiteName="evosuite-branch"
	elif(tool.lower() == "randoop"):
		testSuiteName="randoop"
	suitePath =  os.path.join(bug.getTestDir(), bug.getProject()+"-"+bug.getBugNum()+"f-"+testSuiteName+"."+str(seed)+".tar.bz2")
	if(os.path.isfile(suitePath)):
		cmd = defects4jCommand + " coverage -w " + bug.getFixPath() + " -s " + str(suitePath)
		subprocess.call(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE) 
		cmd = defects4jCommand + " coverage -w " + bug.getBugPath() + " -s " + str(suitePath)
		subprocess.call(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE) 
	else:
		sys.exit("The script did not find a test suite: " + str(suitePath))
def getEditedFiles(bug):
	cmd = defects4jCommand + " export -p classes.modified"
	p = subprocess.Popen(cmd, shell=True, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	return [ line.split("2>&1")[-1].strip().replace(".", "/") + ".java" for line in p.stdout ]


def getADiff(pathToFile, bug):
	pathToSource=bug.getSrcPath()
	cmd = "diff --unchanged-line-format=\"\"  --old-line-format=\"%dn \" --new-line-format=\"%dn \" " + bug.getBugPath()+"/"+pathToSource+"/"+pathToFile +" " + bug.getFixPath()+"/"+pathToSource+"/"+pathToFile
	print cmd
	p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	diffLines=""
	for line in p.stdout:
		diffLines = line
	diffLines=diffLines.strip().split(" ")
	print [i for i in list(set(diffLines))]
	return list(set(diffLines))

#args.many is assumed to be not None
def getAllBugs(bugs,args):
	if(not os.path.isfile(args.many)):
		sys.exit("The file " + str(args.many) + " does not exist")
	else:
		with open(args.many) as f:
			pairs = [x.strip().split(',') for x in f.readlines() if x[0] != '#']
			if pairs is None:
				sys.exit("There has been a problem reading the file with the bugs")
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

	#Removing the fixed file
	shutil.rmtree(bug.getFixPath())
	checkout(bug.getFixPath(),bug.getProject(), bug.getBugNum(), "b")
		
	#Patching the fixed file
	whereToCallPatch=str(bug.getFixPath())+"/"+str(pathToSource)
	cmd ="patch -p10 -i "+d4jHome+"/"+bug.getPatch()
#	print whereToCallPatch
	p = subprocess.Popen(cmd, shell=True, cwd=whereToCallPatch, stdout=subprocess.PIPE, stderr=subprocess.PIPE)


def ensureVersionAreCheckedOut(bug):
	#Remove and re clone versions
	if(not os.path.exists(bug.getBugPath())):
		shutil.rmtree(bug.getBugPath())
	checkout(bug.getBugPath(), bug.getProject(), bug.getBugNum(), "b")
	if(not os.path.exists(bug.getFixPath())):
		shutil.rmtree(bug.getFixPath())
	checkout(bug.getFixPath(), bug.getProject(), bug.getBugNum(), "f")

def checkout(folderToCheckout, project, bugNum, vers):
	cmd = defects4jCommand + " checkout -p " + str(project) + " -v " + str(bugNum) + str(vers) + " -w " + str(folderToCheckout)
	p = subprocess.call(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

def getOptions():
	parser = argparse.ArgumentParser(description="This script checks if a test suite is covering the human changes. Example of usage: python getCoverage.py ExamplesCheckedOut generatedTestSuites/Evosuite30MinsPAR/testSuites/ --project Closure --bug 38")
	parser.add_argument("wd", help="working directory to check out project versions, starting from the the D4J_HOME folder")
	parser.add_argument("testDir", help="the path where the test suite is located, starting from the the D4J_HOME folder (Example: generatedTestSuites)")
	parser.add_argument("--project", help="the project in upper case (ex: Lang, Chart, Closure, Math, Time)")
	parser.add_argument("--bug", help="the bug number (ex: 1,2,3,4,...)")
	parser.add_argument("--many", help="Absolute path, the file listing bugs to process: project,bugNum (one per line). Lines starting with # are skipped")
	parser.add_argument("--patches", help="the folder where the patches are located, starting from the the D4J_HOME folder")
	parser.add_argument("--tool", help="the generation tool (Randoop or Evosuite)", default="Evosuite")
	parser.add_argument("--seed", help="the seed the test suite was created with", default="1")
	parser.add_argument("--coverage", help="a coverage file")
	return parser.parse_args()

def errorHandling(args):
	if os.environ['D4J_HOME'] is None:
		sys.exit("Environment variable D4J_HOME is not set")
	if not os.path.isdir(os.path.join(d4jHome, args.wd)):
		sys.exit("The folder " + str(os.path.join(d4jHome, args.wd)) + " does not exist")
	if not os.path.isdir(os.path.join(d4jHome, args.testDir)):
		sys.exit("The folder " + str(os.path.join(d4jHome, args.testDir)) + " does not exist")
	if not(args.many is None) and not os.path.exists(args.many):
		sys.exit("The file " + str(args.many) + " does not exist")
	if not(args.many is None) and (not(args.project is None) or not(args.bug is None) or not(args.patches is None)):
		sys.exit("There should be just one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if not(args.patches is None) and (not(args.project is None) or not(args.bug is None) or not(args.many is None)):
		sys.exit("There should be just one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if (not(args.project is None) and not(args.bug is None)) and (not(args.patches is None)  or not(args.many is None)):
		sys.exit("There should be just one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if args.project is None and args.bug is None and args.patches is None and args.many is None:
		sys.exit("There should be one of these three options: 1) A file with a list of bugs should be provided with the --many parameter, 2) a particular bug with the --project and --bug parameters, 3) A location with patches with the --patches parameter")
	if not(args.tool is None):
		if args.tool != "Randoop" and args.tool != "Evosuite":	
			sys.exit("tool should be Randoop or Evosuite")
	if not(args.seed is None) and (not (args.seed.isdigit())):
		sys.exit("Seed should be an integer")
	if not(args.patches is None) and (not os.path.isdir(os.path.join(d4jHome, args.patches))):
		sys.exit("The folder " + str(os.path.join(d4jHome, args.patches)) + " does not exist")
	if not(args.coverage is None) and (not os.path.isfile(args.coverage)):
		sys.exit("The file " + str(args.coverage) + " does not exist")


def main():
	args=getOptions()
	errorHandling(args)
	# TODO: line wrap this file at 80 characters or so	

	#removes outputfile if exists
	outputFile= str(d4jHome)+ str(args.wd) + "/coverageOfBugs.csv"
	if(os.path.isfile(outputFile)):
		os.remove(outputFile)
	cmd = "echo \"Project,Bug,Seed,Edits,Variant,Class line cov,Class condition cov,Num of lines edited,Percentage of edited lines covered,Number of methods changed, Methods changed,Method line coverage,Method Branch coverage\" >> "+ outputFile
	p = subprocess.call(cmd, shell=True)#, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
				

	
	#fill bug list
	bugs = []
	if(not(args.project is None)):
		bugs.append(BugInfo(args.project, args.bug, args.wd, args.testDir))
	elif(not(args.many is None)):
		getAllBugs(bugs, args)
	elif(not(args.patches is None)):
		getBugsFromPatchNames(bugs,args)

	for bug in bugs:
		print "Defect: "+bug.project + " " + str(bug.bugNum)
		#ensureVersionAreCheckedOut(bug)
		bug.setScrPath()

		#if we are doing the patches flag, patch the fixed version
		if not args.patches is None:
			patchFixedFile(bug)

		#creating xml file
		if((args.coverage != None) or (not os.path.exists(bug.getFixPath()+"/coverage.xml"))):
			generateCovXML(bug,args.tool, args.seed)
		if not os.path.exists(bug.getFixPath()+"/coverage.xml"):
			sys.exit("There is no coverage file in "+bug.getFixPath()+"/coverage.xml")
		
		for f in getEditedFiles(bug):
			print "Working on file "+f
			listOfChangedLines = getADiff(f, bug)
			print "before entereing computeCoverage"
			allCoverageMetrics=computeCoverage(listOfChangedLines, bug.getFixPath()+"/coverage.xml")
			#pipes the result to a csv file
			#Generated patch
			if not args.patches is None:
				#patchName=str(bug.getPatch().split('/')[-1].strip())
				diffName=str(bug.getPatch().split('/')[-1].strip())
				defect=diffName.split('_')[0]
				bug=int(filter(str.isdigit, defect))
				project=str(filter(str.isalpha, defect)).title()
				seed=int(filter(str.isdigit, diffName.split('_')[1]))
				edits=diffName.split('_')[2:-1]
				edits=str(edits).replace("['","").replace("']","")#.replace("', '","(")
				#print "diffName: "+diffName
				#variant=int(filter(str.isdigit, diffName.split('_')[-1]))
				variant=""
				allCoverageMetrics=str(project)+","+str(bug)+","+str(seed)+","+str(edits)+","+str(variant)+","+str(allCoverageMetrics)
			#Human made patch
			if not args.many is None or not args.project is None:
				patchName=str(bug.getProject() + bug.getBugNum() + "HumanGeneratedPatch")
				allCoverageMetrics=patchName+","+allCoverageMetrics
			print allCoverageMetrics
			cmd = "echo \""+str(allCoverageMetrics)+ "\" >> "+ outputFile
			p = subprocess.call(cmd, shell=True)#, cwd=bug.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
			print ""
	print "Results in "+outputFile
main()
