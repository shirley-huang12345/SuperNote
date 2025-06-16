#!/usr/bin/python
import re
import os
import sys

dpiTable = {'mdpi':{'mdpi':0,'tvdpi':1,'hdpi':2,'xhdpi':3,'xxhdpi':4,'nodpi':5},
                'tvdpi':{'mdpi':3,'tvdpi':0,'hdpi':1,'xhdpi':2,'xxhdpi':4,'nodpi':5},
                'hdpi':{'mdpi':4,'tvdpi':1,'hdpi':0,'xhdpi':2,'xxhdpi':3,'nodpi':5},
                'xhdpi':{'mdpi':5,'tvdpi':3,'hdpi':2,'xhdpi':0,'xxhdpi':1,'nodpi':4},
                'xxhdpi':{'mdpi':5,'tvdpi':4,'hdpi':3,'xhdpi':1,'xxhdpi':0,'nodpi':2}}

def getAllDrawableTable(R):
    f = open(R)
    p = re.compile('int\s+drawable\s+(\S+)\s')
    table = {}
    for line in f.readlines():
        match = p.match(line)
        if match:
            table[match.groups()[0]] = []
    return table

def fillDrawableTable(table, resPath):
    drawablePaths = [item for item in os.listdir(resPath) if item.startswith('drawable')]
    for drawablePath in drawablePaths:
        path = os.path.join(resPath,drawablePath)
        for drawableItem in os.listdir(path):
            key = os.path.splitext(drawableItem)[0]
            if key.endswith('.9'):
                key = key[:-2]
            try:
                table[key].append(os.path.join(path, drawableItem))
            except KeyError:
                print "can't find key of drawable: ", key
    return table

def getAndFillDrawableTable(resPath):
    drawablePaths = [item for item in os.listdir(resPath) if item.startswith('drawable')]
    table = {}
    for drawablePath in drawablePaths:
        path = os.path.join(resPath,drawablePath)
        for drawableItem in os.listdir(path):
            key = os.path.splitext(drawableItem)[0]
            if key.endswith('.9'):
                key = key[:-2]
            if table.has_key(key):
                table[key].append(os.path.join(drawablePath, drawableItem)) 
            else:
                table[key] = [os.path.join(drawablePath, drawableItem)]
    return table


def split2V(item):
    #return like('drawable', ['sw320'], ['xhdpi']]
    dpi = ['mdpi', 'tvdpi','hdpi','xhdpi','xxhdpi','nodpi']
    v = os.path.split(item)[0]
    v = os.path.split(v)[1]
    v = v.split('-')
    v1 = 'drawable'
    v2 = [item for item in v if item.startswith('sw')]
    v2 = len(v2) == 0 and [''] or v2
    v3 = [item for item in v if item in dpi]
    v3 = len(v3) == 0 and ['mdpi'] or v3
    return (v1, v2[0], v3[0])
    
# selectArry
#just define for multi dpi don't support multi swdp  if need add later.
def getBetter(item, otherItems, selectArry):
    v = split2V(item)
    
    #if sw***dp has value and sw***of v > sw***dp of selectArry
    for other in otherItems:
        v1 = split2V(other)
        if v[1] == v1[1]:
            flag = True
            for sv in selectArry:
                flag = flag and (dpiTable[sv[2]][v[2]] > dpiTable[sv[2]][v1[2]])
            if flag:
                return True
            
def getDelFile(table, selectArry):
    delFiles = []
    for key, value in table.items():
        for item in value:
            v = value[:]
            v.remove(item)
            if getBetter(item, v, selectArry):
                delFiles.append(item)
    return delFiles                

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print 'usage: ',sys.argv[0],' resPath ',' destResPath ', 'mdpi,hdpi'
        exit(0)
    table = getAndFillDrawableTable(sys.argv[1])
    delFiles = getDelFile(table, [('drawable', '',item) for item in sys.argv[3].split(',')])
    #print delFiles
    for item in delFiles:
        delFilePath = os.path.join(sys.argv[2], item)
        print 'delete: ', delFilePath
        os.system('rm '+delFilePath)
    exit(0)
