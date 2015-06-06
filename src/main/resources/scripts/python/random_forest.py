# -*- coding: utf-8 -*-
"""
Created on Wed Feb 18 10:20:13 2015

@author: jil3004
"""

from sklearn.cross_validation import cross_val_score
from sklearn.datasets import load_iris
from sklearn.ensemble import AdaBoostClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn import svm

import pandas as pd
import numpy as np

f = open("data.csv")
f.readline()
data =  np.loadtxt(fname = f, delimiter = ',')
length = len(data[0])
X = data[:, 0:length-1]
Y = data[:, length-1:length]

target = []
for v in Y:
    target.append(v[0])

clf = RandomForestClassifier(n_estimators=1000)
clf.fit(X, target)

print clf.predict(X[4])
#print clf.feature_importances_
max = -1
index = 0
maxIndex = -1
for f in clf.feature_importances_:
    if f > max:
        max = f
        maxIndex = index
    if index == 1044:
        print f
    index += 1

print max
print maxIndex

arr = np.array(clf.feature_importances_)
print arr.argsort()[-10:][::-1]