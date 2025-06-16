#!/usr/bin/python

import os
import time
import re
import sys

signFolder = '/home/mars_li/AMAX-SIGNED/'


def delOldApk(fileName, signFolder):
    matchDate = time.strftime('%m%d',time.localtime(time.time()))
    files = os.listdir(signFolder)
    p = re.compile(matchDate +'-\d{4}_(?:ORIGIN|SIGNED)_'+fileName) 
    for f in files:
        match = p.match(f)
        if match != None:
            print 'rm ',  match.string
            os.system('rm '+os.path.join(signFolder, match.string))
    


def renameApk(srcFileName, fileName, signFolder):
    cmd = ['mv', os.path.join(signFolder, srcFileName), os.path.join(signFolder,fileName)]
    print 'run ',' '.join(cmd)
    os.system(' '.join(cmd))

def copyApk(filePath, toPath):
    cmd = ['cp', filePath, toPath]
    print 'run ',' '.join(cmd)
    os.system(' '.join(cmd))

def alignApk(filePath):
    print 'align apk file'
    alignFile = filePath[:-4]+'-align'+'.apk'
    cmd = ['zipalign','-v 4',filePath, alignFile]
    os.system(' '.join(cmd))
    return alignFile

def checkGenFile(prefix, fileName, signFolder):
    matchDate = time.strftime('%m%d',time.localtime(time.time()))
    files = os.listdir(signFolder)
    #get lastTime to sign APK
    rVal = None
    p = re.compile(matchDate + '-(\d{4})_'+prefix+'_'+fileName)
    for f in files:
        match = p.match(f)
        if  (match != None) :
            rVal = match.string
            break
    return rVal

def getGenFile(fileName, signFolder, prefix, maxCounter = 1000):
    counter = 0
    genFile = checkGenFile(prefix, fileName, signFolder)
    while counter<maxCounter and genFile == None:
        print 'check gen file.....'
        genFile = checkGenFile(prefix, fileName, signFolder)
        if genFile != None:
            break
        counter += 1
        time.sleep(30)

    if counter>= maxCounter:
        print "can't get gen file"

    return genFile        
    
    
def signApk(filePath, signFolder):
    fileName = os.path.split(filePath)[1]
    #del old apk 
    delOldApk(fileName, signFolder)

    #copy apk to signFilder
    copyApk(filePath, signFolder)    

    #get unsign file
    unsignGenFile = getGenFile(fileName, signFolder, 'ORIGIN')
    if (unsignGenFile == None):
        print "can't get unsigned file"
        exit(1)


    #get sign file 
    signGenFile = getGenFile(fileName, signFolder, 'SIGNED', 4)
    if signGenFile != None:
        return signGenFile
    
    #can't get sign file we need mv unsign file
    renameApk(unsignGenFile, fileName, signFolder)
    signGenFile = getGenFile(fileName, signFolder, 'SIGNED')

    return signGenFile

def signAndAlignApk(apkFile, signFolder,savePath):
    signFile = signApk(apkFile,signFolder)
    srcFile = os.path.join(signFolder, signFile)
    decFile = os.path.join(savePath, os.path.split(apkFile)[1].replace('-unsigned-', '-'))
    cmd = ['cp',srcFile, decFile]
    print 'run ', ' '.join(cmd)
    os.system(' '.join(cmd))
    alignApk(decFile)

if __name__=='__main__':
    apkPath = './build/apk/'
    savePath = '/home/mars_li/share/Release/SuperNote'
    for apkFile in os.listdir(apkPath):
        if apkFile.find('-unsigned-') != -1:
            signAndAlignApk(os.path.join(apkPath,apkFile), signFolder, savePath)
   
    
    
