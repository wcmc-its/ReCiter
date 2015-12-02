# -*- coding: utf-8 -*-
"""
@author: jil3004
"""
from sklearn.ensemble import RandomForestClassifier
from sklearn.cross_validation import train_test_split
from sklearn.metrics import precision_score
import os
import numpy as np
import warnings
warnings.filterwarnings("ignore", category=DeprecationWarning)

path = "C:\\Users\\Jie\\Documents\\reciter_data\\"

for file in os.listdir(path):
    f = open(path + file)
    f.readline()
    data =  np.loadtxt(fname = f, delimiter = ',')
    length = len(data[0])
    X = data[:, 1:length-1]
    Y = data[:, length-1:length]
    X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=0.33, random_state=42)
    
    clf = RandomForestClassifier(n_estimators=1000)
    clf.fit(X_train, y_train.ravel())
    
    y_pred = []
    for test in X_test:
        y_pred.append(clf.predict(test))
        
    precision = precision_score(y_test, y_pred, average='binary')
    print(file)
    print(precision)
    
#    print(data[0][0])
#    print(clf.predict(X[0]))
#    arr = np.array(clf.feature_importances_)
#    print(arr)
    f.close()