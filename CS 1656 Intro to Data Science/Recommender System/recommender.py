import pandas as pd
import csv
from requests import get
import json
from datetime import datetime, timedelta, date
import numpy as np
from scipy.spatial.distance import euclidean, cityblock, cosine
from scipy.stats import pearsonr

import csv
import re
import pandas as pd
import argparse
import collections
import json
import glob
import math
import os
import requests
import string
import sys
import time
import xml
import random

class Recommender(object):
    def __init__(self, training_set, test_set):
        if isinstance(training_set, str):
            # the training set is a file name
            self.training_set = pd.read_csv(training_set)
        else:
            # the training set is a DataFrame
            self.training_set = training_set.copy()

        if isinstance(test_set, str):
            # the test set is a file name
            self.test_set = pd.read_csv(test_set)
        else:
            # the test set is a DataFrame
            self.test_set = test_set.copy()
    
    def train_user_euclidean(self, data_set, userId):
        
        euclidean_weights = {}

        for user in data_set.columns[1:]:
            if(user!=userId):
                data_subset = data_set[[userId,user]][data_set[userId].notnull() & data_set[user].notnull()]
                dist = euclidean(data_subset[userId],data_subset[user])
                euclidean_weights[user] = 1.0 / (1.0+dist)

        return euclidean_weights 
    
    def train_user_manhattan(self, data_set, userId):

        manhattan_weights = {}

        for user in data_set.columns[1:]:
            if(user!=userId):
                data_subset = data_set[[userId,user]][data_set[userId].notnull() & data_set[user].notnull()]
                dist = cityblock(data_subset[userId],data_subset[user])
                manhattan_weights[user] = 1.0 / (1.0+dist)

        return manhattan_weights

    def train_user_cosine(self, data_set, userId):
        
        cosine_weights = {}

        for user in data_set.columns[1:]:
            if(user!=userId):
                data_subset = data_set[[userId,user]][data_set[userId].notnull() & data_set[user].notnull()]

                cosine_weights[user] = cosine(data_subset[userId],data_subset[user])

        return cosine_weights
        
    def train_user_pearson(self, data_set, userId):
        
        pearson_weights = {}

        for user in data_set.columns[1:]:
            if(user!=userId):
                data_subset = data_set[[userId,user]][data_set[userId].notnull() & data_set[user].notnull()]

                pearson_weights[user] = pearsonr(data_subset[userId],data_subset[user])[0]

        return pearson_weights

    def train_user(self, data_set, distance_function, userId):
        if distance_function == 'euclidean':
            return self.train_user_euclidean(data_set, userId)
        elif distance_function == 'manhattan':
            return self.train_user_manhattan(data_set, userId)
        elif distance_function == 'cosine':
            return self.train_user_cosine(data_set, userId)
        elif distance_function == 'pearson':
            return self.train_user_pearson(data_set, userId)
        else:
            return None

    def get_user_existing_ratings(self, data_set, userId):

        df_user = data_set[['movieId',userId]][data_set[userId].notnull()]

        return [tuple(x) for x in df_user.to_records(index=False)]


    def predict_user_existing_ratings_top_k(self, data_set, sim_weights, userId, k):
        
        df_without = data_set[data_set[userId].notnull()].drop(userId,1)

        ratings = []

        sorted_weights = sorted(sim_weights,key=sim_weights.get, reverse=True)[:k]

        for(index,row) in df_without.iterrows():
            pred_ratings = 0.0
            weights_sum = 0.0

            for(key) in sorted_weights:
                if not(pd.isna(row.loc[key])):
                    pred_ratings += sim_weights[key]*row.loc[key]
                    weights_sum += sim_weights[key]

            if(weights_sum==0):
                pred_ratings=0
            else:
                pred_ratings /= weights_sum
            add_tup = (row[0],pred_ratings)
            ratings.append(add_tup)

        return ratings
    
    def evaluate(self, existing_ratings, predicted_ratings):
        
        rmse = 0
        ratio = 0.0
        non_zero = 0.0



        size = len(existing_ratings)
        valid_ratings = 0

        for i in range(size):
            if(existing_ratings[i][1]!=None):
                if not(pd.isna(existing_ratings[i][1])):
                    if(existing_ratings[i][1]!=0):
                        valid_ratings+=1
        for index,tup in enumerate(existing_ratings):
            for indecs,tups in enumerate(predicted_ratings):
                if(tup[0])==(tups[0]):
                    if(tups[1]!=None and tup[1]!=None):
                        if not(pd.isna(tups[1])):
                            if not(pd.isna(tup[1])):
                                rmse+=((tups[1] - tup[1])**2)
                                non_zero+=1

        rmse /= non_zero
        rmse = math.sqrt(rmse)

        ratio = non_zero/valid_ratings

        return {'rmse':rmse, 'ratio':ratio}
    
    def single_calculation(self, distance_function, userId, k_values):
        user_existing_ratings = self.get_user_existing_ratings(self.test_set, userId)
        print("User has {} existing and {} missing movie ratings".format(len(user_existing_ratings), len(self.test_set) - len(user_existing_ratings)), file=sys.stderr)

        print('Building weights')
        sim_weights = self.train_user(self.training_set[self.test_set.columns.values.tolist()], distance_function, userId)

        result = []
        for k in k_values:
            print('Calculating top-k user prediction with k={}'.format(k))
            top_k_existing_ratings_prediction = self.predict_user_existing_ratings_top_k(self.test_set, sim_weights, userId, k)
            result.append((k, self.evaluate(user_existing_ratings, top_k_existing_ratings_prediction)))
        return result # list of tuples, each of which has the k value and the result of the evaluation. e.g. [(1, {'rmse':1.2, 'ratio':0.5}), (2, {'rmse':1.0, 'ratio':0.9})]

    def aggregate_calculation(self, distance_functions, userId, k_values):
        print()
        result_per_k = {}
        for func in distance_functions:
            print("Calculating for {} distance metric".format(func))
            for calc in self.single_calculation(func, userId, k_values):
                if calc[0] not in result_per_k:
                    result_per_k[calc[0]] = {}
                result_per_k[calc[0]]['{}_rmse'.format(func)] = calc[1]['rmse']
                result_per_k[calc[0]]['{}_ratio'.format(func)] = calc[1]['ratio']
            print()
        result = []
        for k in k_values:
            row = {'k':k}
            row.update(result_per_k[k])
            result.append(row)
        columns = ['k']
        for func in distance_functions:
            columns.append('{}_rmse'.format(func))
            columns.append('{}_ratio'.format(func))
        result = pd.DataFrame(result, columns=columns)
        return result
        
if __name__ == "__main__":
    recommender = Recommender("data/train.csv", "data/small_test.csv")
    print("Training set has {} users and {} movies".format(len(recommender.training_set.columns[1:]), len(recommender.training_set)))
    print("Testing set has {} users and {} movies".format(len(recommender.test_set.columns[1:]), len(recommender.test_set)))

    result = recommender.aggregate_calculation(['euclidean', 'cosine', 'pearson', 'manhattan'], "0331949b45", [1, 2, 3, 4])
    print(result)