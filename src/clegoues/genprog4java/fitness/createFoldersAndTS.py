#/bin/python

import os
import subprocess
import sys
import argparse
import xml.etree.ElementTree
import shutil
import time
from sets import Set
import itertools

#This script computes the diversity scores between all possible patches in a folder 
#It creates a file (outputSummarized.csv containing columns: patch1, patch2, diversityScore, similarityScore. The diversity score tells you how different they are from each other
#Also creates a file (outputDetailed.csv) detailing test number and failing test number per each test suite.

# Example: python computeDiversityScores.py ExamplesCheckedOut /home/mausoto/diversityProject/scripts/patches/ /home/mausoto/defects4jJava8/defects4j/framework/lib/test_generation/generation/evosuite-1.0.6.jar ./

#WATCH OUT FOR THE JAVA VERSIONS
#java 7 version should be java 7_80, others have shown problems
#Also run this with the java 8 version of defects4j
Java7="/usr/lib/jvm/java-1.7.0-openjdk-amd64"
Java8="/usr/lib/jvm/java-1.8.0-openjdk-amd64"
d4jHome = os.environ['D4J_HOME']
defects4jCommand = d4jHome + "/framework/bin/defects4j"
summarizedFile = "./outputSummarized.csv"
detailedFile = "./outputDetailed.csv"

class PatchInfo(object):
	def __init__(self, project, bugNum, wd):
		self.project = project
		self.bugNum = bugNum
		self.buggyFolder = wd + "/" 
		self.fixedFolder = wd + "/"  
		self.srcPath=""
		self.patch=""
		self.modifClassAndPath = ""
		self.modifClassPath = ""
		self.compiledSuccessful = 0

	def getProject(self):
		return str(self.project)

	def getBugNum(self):
		return str(self.bugNum)

	def getFixPath(self):
		return str(self.fixedFolder)

	def getBugPath(self):
		return str(self.buggyFolder)

	def getSrcPath(self):
		return str(self.srcPath)

	def getModifClass(self):
		return str(self.modifClass)
		
	def getModifClassPath(self):
		return str(self.modifClassPath)

	def getCompiledSuccessful(self):
		return str(self.compiledSuccessful)

	def compile(self):
		cmd = defects4jCommand + " compile"
		#print("\n\n\n"+cmd+" ran in "+self.getBugPath()+"\n\n\n\n\n")
		p = subprocess.Popen(cmd, shell=True, cwd=self.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		out, err = p.communicate()
		for i in err:
			if "BUILD FAILED" in str(i):
				self.compiledSuccessful = -1
		if self.compiledSuccessful != -1:
			self.compiledSuccessful = 1
	
	#this needs to be called after the buggy folder has been checked out
	def setScrPath(self):
		print "defects4jCommand:"+str(defects4jCommand)
		cmd = defects4jCommand + " export -p dir.src.classes"
		#print "bugpath:"+str(self.getBugPath())
		p = subprocess.Popen(cmd, shell=True, cwd=self.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		out, err = p.communicate()
		builder = ""
		for i in out:
			builder = builder+str(i)
		#self.srcPath = str(i).split("2>&1")[-1].strip().replace('.','/')	
		self.srcPath=builder
		#print "srcPath:" + str(self.srcPath)
		
		cmd = defects4jCommand + " export -p classes.modified"
		p = subprocess.Popen(cmd, shell=True, cwd=self.getBugPath(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		out, err = p.communicate()
		builder = ""
		for i in out:
			builder = builder+str(i)
		#self.modifClass = str(i).split("2>&1")[-1].strip().replace('.','/')		
		builder = str(builder).strip().replace('.','/')
		self.modifClass = str(builder).split("/")[-1]+".java"
		self.modifClassPath = str(builder).replace(str(builder).split("/")[-1] , '')
		#print("modifClassPath:" + str(self.modifClassPath)+ " modifClass:"+ str(self.modifClass))
						
	def setPatch(self,patch):
		self.patch=patch
		self.buggyFolder = str(self.buggyFolder) + str(self.patch.split('.')[0])
		self.fixedFolder = str(self.fixedFolder) + str(self.patch.split('.')[0])
        
	def getPatch(self):
		return str(self.patch)
		
def evaluateQuality(bug,combinedPatchFolderName,folderToKeep):
	#os.environ["JAVA_HOME"] = Java8
	for seed in range(1,11):
		suitePath =  os.path.join(bug.getTestDir(), bug.getProject()+"-"+bug.getBugNum()+"f-evosuite-branch."+str(seed)+".tar.bz2")
		#capture number of failing test cases
		#print "When evaluating quality the test command is ran here: "+ combinedPatchFolderName
		p = subprocess.call("rm all_tests", shell=True, cwd=combinedPatchFolderName)
	
		cmd = defects4jCommand+" test -s "+ suitePath
		#print cmd
		p = subprocess.Popen(cmd, shell=True, cwd=combinedPatchFolderName, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		out, err = p.communicate()
		failingTests=""
		for line in out:
			if "Failing tests:" in line: 
				#print line
				failingTests=line.split("Failing tests:")[-1].strip() 
		#capture total number of test cases ran
		numberOfTests=getNumberOfTC(suitePath,combinedPatchFolderName).strip() 
		#print numberOfTests

		p = subprocess.call(cmd, shell=True)
		
		recordInOutputFile = "echo \"" + str(bug.getProject()) + ","+ str(bug.getBugNum()) + "," + str(combinedPatchFolderName.split('/')[-2]) +"," +str(seed) + "," + str(numberOfTests) + ","+ str(failingTests) + "\" >> "+ folderToKeep+"/qualityEval.csv"
		p = subprocess.call(recordInOutputFile, shell=True)

def getNumberOfTC(testSuite,whereToCall):
	cmd="wc -l < all_tests"
	#print whereToCall
	p = subprocess.Popen(cmd, shell=True, cwd=whereToCall, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	out, err = p.communicate()
	#print out
	return out
	
#def saveFileAndRemoveFolder(folderToRemove, bug, whereToSave):
#	#save file
#	cmd = "cp " + folderToRemove + "/" + bug.getSrcPath() +"/" + bug.getModifClass() + ".java" + " "+ whereToSave
#	print cmd
#	p = subprocess.call(cmd, shell=True)
#		
#	#remove folder
#	if(os.path.exists(folderToRemove)):
#		shutil.rmtree(folderToRemove)
	
#def createSetOfPatchesFromDir(args):
#	patches = []	
#	defects = set()
#	cmd = "ls -d *.patch"
#	p = subprocess.Popen(cmd, shell=True, cwd=args.patches, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
#	for line in p.stdout:
#		#defects is project and bugNum: chart1, closure22, etc
#		defect=line.split('_')[0]
#		bug=int(filter(str.isdigit, defect))
#		project=str(filter(str.isalpha, defect)).title()
#		patch = PatchInfo(project, int(bug), args.wd)
#		patch.setPatch(str(line).strip())
#		patches.append(patch)
#		defects.add(defect)
#		
#		#patches = patchMap[defect]
#		#patches.add(bug)
#		#patchMap.update(defect:patches)
#	return [defects,patches]

def runD4jCommand(fixedFolder, d4jCommand):
	cmd = defects4jCommand + d4jCommand
	print(cmd)
	p = subprocess.Popen(cmd, shell=True, cwd=fixedFolder, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	return [ line.split("2>&1")[-1].strip() for line in p.stdout ]

def checkout(folderToCheckout, project, bugNum, vers):
	cmd = defects4jCommand + " checkout -p " + str(project) + " -v " + str(bugNum) + str(vers) + " -w " + str(folderToCheckout)
	print cmd
	p = subprocess.call(cmd, shell=True)
	
def checkoutAndPatchFile(bug,args):
	#print "pathToSource:"+ str(pathToSource)
	#Removing the fixed file
	if not(os.path.exists(bug.getFixPath())):
		#shutil.rmtree(bug.getFixPath())
		checkout(bug.getFixPath(),bug.getProject(), bug.getBugNum(), "b")
		
		#set source path
		bug.setScrPath()
		pathToSource=bug.getSrcPath()
		
		#Patching the fixed file
		#print("fixPath: "+str(bug.getFixPath())+ "  pathToSource: "+ str(pathToSource))
		whereToCallPatch=str(bug.getFixPath())+"/"+str(pathToSource)
		cmd ="patch -p2 -i "+args.patches+"/"+bug.getPatch() 
		#print "Creating version for patch: "+ str(bug.getPatch())
		#print(cmd +" called in "+ whereToCallPatch)
		p = subprocess.Popen(cmd, shell=True, cwd=whereToCallPatch, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		out, err = p.communicate()
		#print out
		#for line in out:
		#	print line.strip()
	
def tsNameFromFolder(patchedFolder,args):
	#print("!!!!patchedFolder"+str(patchedFolder))
	laterHalf = str(patchedFolder.split("variant")[-1])
	name = laterHalf[:laterHalf.find("/")]
	name = "variant"+str(name)
	name = str(args.project)+str(args.bugNum)+".GPSeed"+str(args.gpSeed)+"."+str(name)+".TSSeed0.tar.bz2"
	#print("NAME:"+ name)
	return name
	
def variantNum(patchedFolder,args):
	laterHalf = str(patchedFolder.split("variant")[-1])
	num = laterHalf[:laterHalf.find("/")]
	return num
	
def createTS(args,patchedFolder, testSuiteName):
	targetClass = runD4jCommand(patchedFolder, " export -p classes.modified")[0]
	pathToBinary = runD4jCommand(patchedFolder, " export -p cp.compile")[0]
	#timeout 6s java -jar 
	cmd = " java -jar "+str(args.evosuite)+" -class "+str(targetClass)+" -projectCP "+str(pathToBinary)+" -criterion line -seed 0 -Dsearch_budget=5"		
	print(cmd)
	#print("Folder where it is being called: "+str(patchedFolder))		
	p = subprocess.call(cmd, shell=True, cwd=patchedFolder)			
	cmd = "tar -cvjSf "+str(testSuiteName)+" "+str(targetClass).split('.')[0]+"/"
	print(cmd)
	p = subprocess.call(cmd, shell=True, cwd=str(patchedFolder)+"/evosuite-tests/")
	#defect = (testSuiteName.split(".")[0]).split("_")[0]
	project = str(args.project).title()
	bugNum = str(args.bugNum)
	fixTS(patchedFolder, testSuiteName, project, bugNum)
	
def fixTS(fixedFolder, testSuiteName, project, bugNum):
	cmd="perl "+str(d4jHome) +"framework/util/fix_test_suite.pl -p "+str(project)+" -d "+str(fixedFolder)+"/evosuite-tests/ -v "+str(bugNum)+"f"
	#print(cmd)
	p = subprocess.call(cmd, shell=True)
	
#def crossEvaluateTS(patchedFolderFirst,patchedFolderSecond):
#	firstTSName = tsNameFromFolder(patchedFolderFirst)
#	secondTSName = tsNameFromFolder(patchedFolderSecond)
#	#print("\nfirstTSName: "+str(firstTSName)+ " secondTSName: " + str(secondTSName))
#	[numTestsInTSFromFirstPatch, numFailingTestsInTSFromFirstPatch] = evaluateTSOnPatch(patchedFolderFirst+"/"+firstTSName,patchedFolderSecond)
#	print("\nWhen evaluating test suite "+str(firstTSName)+" in folder "+str(patchedFolderSecond)+" there are "+ str(numTestsInTSFromFirstPatch)+" tests from which "+str(numFailingTestsInTSFromFirstPatch)+ " fail.")
#	
#	[numTestsInTSFromSecondPatch, numFailingTestsInTSFromSecondPatch] = evaluateTSOnPatch(patchedFolderSecond+"/"+secondTSName,patchedFolderFirst)
#	print("\nWhen evaluating test suite "+str(secondTSName)+" in folder "+str(patchedFolderFirst)+" there are "+ str(numTestsInTSFromSecondPatch)+" tests from which "+str(numFailingTestsInTSFromSecondPatch)+ " fail.")
#	
#	return [numTestsInTSFromFirstPatch, numFailingTestsInTSFromFirstPatch, numTestsInTSFromSecondPatch, numFailingTestsInTSFromSecondPatch]
	
def evaluateTSOnPatch(testsuite,folderToEvaluate):
	#os.environ["JAVA_HOME"] = Java8
	#cmd = "echo \"Project,Bug,Patch,TSSeed,NumberOfTests,NumberOfFailingTests\" >> "+ folderToKeep+"/qualityEval.csv"
	seed = 0
	#capture number of failing test cases
	#print "When evaluating quality the test command is ran here: "+ combinedPatchFolderName
	#print("folderToEvaluate: "+folderToEvaluate)
	p = subprocess.call("rm -f all_tests", shell=True, cwd=folderToEvaluate)
	
	cmd = defects4jCommand+" test -s "+ testsuite
	print(cmd+ " is being called in "+ str(folderToEvaluate))
	p = subprocess.Popen(cmd, shell=True, cwd=folderToEvaluate, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	failingTests=""
	for line in p.stdout:
		if "Failing tests:" in line: 
			#print line
			failingTests=line.split("Failing tests:")[-1].strip() 
	#capture total number of test cases ran
	numberOfTests=getNumberOfTC(testsuite,folderToEvaluate).strip() 
	#print numberOfTests

	#p = subprocess.call(cmd, shell=True)
		
	#recordInOutputFile = "echo \"" + str(bug.getProject()) + ","+ str(bug.getBugNum()) + "," + str(folderToEvaluate.split('/')[-2]) +"," +str(seed) + "," + str(numberOfTests) + ","+ str(failingTests) + "\" >> "+ folderToKeep+"/qualityEval.csv"
	#p = subprocess.call(recordInOutputFile, shell=True)
	
	return [float(numberOfTests), float(failingTests)]

def getNumberOfTC(testSuite,whereToCall):
	cmd="wc -l < all_tests"
	#print whereToCall
	p = subprocess.Popen(cmd, shell=True, cwd=whereToCall, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	out, err = p.communicate()
	#print out
	return out
	
def writeToFile(lineToStore, outputFile):
	cmd = "echo \""+str(lineToStore)+"\" >> "+str(outputFile)
	p = subprocess.call(cmd, shell=True)
	
def createAndMoveTS(variantFolder,tsName,args):
	if not os.path.exists(str(args.output+"/"+tsName)):
		#if the test suite doesnt exist
		print(str(args.output+"/"+tsName)+ " doesnt exist, creating it now\n")
		createTS(args,variantFolder,tsName)
		subprocess.Popen("mv "+tsName+" " +args.output+"/"+tsName, shell=True, cwd=str(variantFolder)+"/evosuite-tests/")
	else:
		print(str(args.output+"/"+tsName)+ " exists, using the existing one\n")
	
#def setOutputFiles(output):
#	global summarizedFile 
#	summarizedFile = str(output)+"/outputSummarized.csv"
#	global detailedFile 
#	detailedFile = str(output)+"/outputDetailed.csv"
#	subprocess.call("rm -f "+summarizedFile, shell=True)
#	subprocess.call("rm -f "+detailedFile, shell=True)
#	writeToFile("Project,BugNum,TestSuite,FolderEvaluated,NumberOfTests,NumberOfFailingTests", detailedFile)
#	writeToFile("Patch1,Patch2,DiversityScore,SimilarityScore", summarizedFile)
	
#def crossEvaluateOld(pair,patch,args):
#	firstPatch= str((pair[0].getPatch()).split('.')[0])
#	secondPatch = str((pair[1].getPatch()).split('.')[0])
#	patchDir=patch.getBugPath()
#	#print "patchDir:"+str(patchDir)
#	workingDir = str(patchDir[:patchDir.rindex("/")]) + "/"
#	patchedFolderFirst = str(workingDir+"/"+firstPatch)
#	patchedFolderSecond = str(workingDir+"/"+secondPatch)
#	
#	firstTSName = tsNameFromFolder(patchedFolderFirst)
#	secondTSName = tsNameFromFolder(patchedFolderSecond)
#	
#	#Create TS in Patch 1 and Evaluate in Patch 2
#	createAndMoveTS(workingDir,firstPatch,args)
#	[numTestsInTSFromFirstPatch, numFailingTestsInTSFromFirstPatch] = evaluateTSOnPatch(patchedFolderFirst+"/"+firstTSName,patchedFolderSecond)
#	writeToFile(args.project+","+args.bugNum+","+str(firstTSName)+","+str(secondPatch)+","+str(numTestsInTSFromFirstPatch)+","+str(numFailingTestsInTSFromFirstPatch), detailedFile)
#	print("\nWhen evaluating test suite "+str(firstTSName)+" in folder "+str(secondPatch)+" there are "+ str(numTestsInTSFromFirstPatch)+" tests from which "+str(numFailingTestsInTSFromFirstPatch)+ " fail.\n")
#		
#	#Create TS in Patch 2 and Evaluate in Patch 1
#	createAndMoveTS(workingDir,secondPatch,args)
#	[numTestsInTSFromSecondPatch, numFailingTestsInTSFromSecondPatch] = evaluateTSOnPatch(patchedFolderSecond+"/"+secondTSName,patchedFolderFirst)
#	writeToFile(args.project+","+args.bugNum+","+str(secondTSName)+","+str(firstPatch)+","+str(numTestsInTSFromSecondPatch)+","+str(numFailingTestsInTSFromSecondPatch), detailedFile)
#	print("\nWhen evaluating test suite "+str(secondTSName)+" in folder "+str(firstPatch)+" there are "+ str(numTestsInTSFromSecondPatch)+" tests from which "+str(numFailingTestsInTSFromSecondPatch)+ " fail.\n")
#		
#	#store results
#	diversityScore = (float(numFailingTestsInTSFromFirstPatch)+float(numFailingTestsInTSFromSecondPatch))/(float(numTestsInTSFromFirstPatch)+float(numTestsInTSFromSecondPatch))
#	similarityScore = (100.0 - diversityScore)
#	writeToFile(str(firstPatch)+","+str(secondPatch)+","+str(diversityScore)+","+str(similarityScore), summarizedFile)
	

#def crossEvaluate(args,var1,var2):
#	#patchDir=patch.getBugPath()
#	#workingDir = str(patchDir[:patchDir.rindex("/")]) + "/"
#	
#	variantFolderFirst = "/tmp/"+str(var1)+"/"
#	variantFolderSecond = "/tmp/"+str(var2)+"/"
#	
#	firstTSName = tsNameFromFolder(args.variant1Loc, args)
#	secondTSName = tsNameFromFolder(args.variant2Loc, args)
#	
#	#Create TS in Patch 1 and Evaluate in Patch 2
#	createAndMoveTS(variantFolderFirst,firstTSName,args)
#	[numTestsInTSFromFirstPatch, numFailingTestsInTSFromFirstPatch] = evaluateTSOnPatch(args.output+"/"+firstTSName,variantFolderSecond)
#	writeToFile(args.project+","+args.bugNum+","+str(firstTSName)+","+str(variantFolderSecond)+","+str(numTestsInTSFromFirstPatch)+","+str(numFailingTestsInTSFromFirstPatch), detailedFile)
#	print("\nWhen evaluating test suite "+str(firstTSName)+" in folder "+str(variantFolderSecond)+" there are "+ str(numTestsInTSFromFirstPatch)+" tests from which "+str(numFailingTestsInTSFromFirstPatch)+ " fail.\n")
		
#	#Create TS in Patch 2 and Evaluate in Patch 1
#	createAndMoveTS(variantFolderSecond,secondTSName,args)
#	[numTestsInTSFromSecondPatch, numFailingTestsInTSFromSecondPatch] = evaluateTSOnPatch(args.output+"/"+secondTSName,variantFolderFirst)
#	writeToFile(args.project+","+args.bugNum+","+str(secondTSName)+","+str(variantFolderFirst)+","+str(numTestsInTSFromSecondPatch)+","+str(numFailingTestsInTSFromSecondPatch), detailedFile)
#	print("\nWhen evaluating test suite "+str(secondTSName)+" in folder "+str(variantFolderFirst)+" there are "+ str(numTestsInTSFromSecondPatch)+" tests from which "+str(numFailingTestsInTSFromSecondPatch)+ " fail.\n")
		
	#store results
#	print("numFailingTestsInTSFromFirstPatch:"+str(numFailingTestsInTSFromFirstPatch)+" numFailingTestsInTSFromSecondPatch:"+str(numFailingTestsInTSFromSecondPatch)+" numTestsInTSFromFirstPatch:"+str(numTestsInTSFromFirstPatch)+" numTestsInTSFromSecondPatch:"+str(numTestsInTSFromSecondPatch))
	
#	diversityScore = (numFailingTestsInTSFromFirstPatch+numFailingTestsInTSFromSecondPatch)/(numTestsInTSFromFirstPatch+numTestsInTSFromSecondPatch)
#	print("diversityScore:"+str(diversityScore))
#	similarityScore = (float(100.0) - diversityScore)
#	print("similarityScore:" + str(similarityScore))
#	writeToFile(str(variantFolderFirst)+","+str(variantFolderSecond)+","+str(diversityScore)+","+str(similarityScore), summarizedFile)
	
	
def getOptions():
	parser = argparse.ArgumentParser(description="Example of usage: python /home/mausoto/diversityProject/DiversityGenProg/genprog4java/src/clegoues/genprog4java/fitness/diversityScores.py Chart 1 1 /home/mauosto/pleaseRemove/home/mausoto/diversityProject/slicingMutOps/defects4j/ExamplesCheckedOutAppend/chart1Buggy/tmp/variant6/ /home/mausoto/defects4jJava8/defects4j/framework/lib/test_generation/generation/evosuite-1.0.6.jar /home/mausotog/pleaseRemove/")
	parser.add_argument("project", help="Project of defects4j (Chart, Math, etc)")
	parser.add_argument("bugNum", help="Bug number from defects4j")
	parser.add_argument("gpSeed", help="Seed used by GenProg to run its repair algorithm")
	parser.add_argument("variant1Loc", help="Location of variant 1")
	parser.add_argument("evosuite", help="Location of evosuite jar")
	parser.add_argument("output", help="output folder")
	
	return parser.parse_args()
	
#var is a string, either var1 or var2
def createFolderForVariant(var, project, bugNum, variantLoc):
	p = subprocess.call("rm -rf /tmp/"+str(var)+"/", shell=True)
	p = subprocess.call("mkdir /tmp/"+str(var)+"/", shell=True)
	checkout("/tmp/"+str(var)+"/", str(project), str(bugNum), "f")
	patch = PatchInfo(project, bugNum, "/tmp/"+str(var))
	patch.setScrPath()
	cmd = "cp "+variantLoc+"/"+str(patch.getModifClassPath())+"/"+str(patch.getModifClass()) + " /tmp/"+str(var)+"/"+str(patch.getSrcPath())+"/"+str(patch.getModifClassPath())
	print(cmd + " ")
	p = subprocess.call(cmd, shell=True)
	patch.compile()
	return patch
	

def main():
	args=getOptions()
	varNum1=variantNum(args.variant1Loc,args)
	tmpFolderToCheckoutProject1=str(args.project)+str(args.bugNum)+"GPSeed"+str(args.gpSeed)+"variant"+str(varNum1)+"/"
	variant1 = createFolderForVariant(tmpFolderToCheckoutProject1,args.project, args.bugNum, args.variant1Loc)
	if int(variant1.getCompiledSuccessful()) == -1:
		print("Variant 1 did not compile. Diversity Score cannot be computed")
		return 0
	variantFolderFirst = "/tmp/"+str(tmpFolderToCheckoutProject1)+"/"
	firstTSName = tsNameFromFolder(args.variant1Loc, args)
	createAndMoveTS(variantFolderFirst,firstTSName,args)
	

main()

