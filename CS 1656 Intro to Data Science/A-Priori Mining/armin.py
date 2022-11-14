from collections import defaultdict
from pandas import Series, DataFrame
import itertools as it
import pandas as pd
import math
import csv
import sys
import argparse
import collections
import glob
import os
import re
import requests
import string
import sys

class Armin():
    
    def apriori(self, input_filename, output_filename, min_support_percentage, min_confidence):
        """
        Implement the Apriori algorithm, and write the result to an output file

        PARAMS
        ------
        input_filename: String, the name of the input file
        output_filename: String, the name of the output file
        min_support_percentage: float, minimum support percentage for an itemset
        min_confidence: float, minimum confidence for an association rule to be significant
        
        """

        #get total number of transactions in dataset, as well as all transactions into one list
        
        minsup = min_support_percentage
        minconf = min_confidence

        transactions = []

        with open(input_filename, 'r') as file:
            reader = csv.reader(file)

            for row in reader:
                transactions.append(tuple(row[1:]))

        total_transactions = len(transactions)


        #get the different items within the set
        unique_items = ()
        
        for tuples in transactions:
        
            unique_items = unique_items + tuples

        unique_items = list(set(unique_items))
        unique_items.sort()

        total_unique_items = len(unique_items)


        #fill up a dictionary of all the different combinations, with the combo being the key and the frequency being the value
        freq_dict = {}

        for i in range(0, total_unique_items):
            for subset in it.combinations(unique_items,i+1):
                freq_dict[subset] = 0

        #get support percentage of all combinations

        for i in range(0,total_unique_items):
            
            combinations = it.combinations(unique_items,i+1)
            
            for c in combinations:
                combo = set(c)
                count = 0
        
                for t in transactions:
                    tid = set(t)
            
                    if combo.issubset(tid):
                        count+=1
                freq_dict[c] = count/total_transactions

        #remove any combinations that aren't past minsup
        for key, value in dict(freq_dict).items():
            if value < minsup:
                del freq_dict[key]

        #write frequent itemsets to file
        with open(output_filename,"w",newline="") as file:
            for key,value in freq_dict.items():
                subset = str(key)
                subset = subset.replace('(','').replace(')','').replace("'", '').replace(" ",'').rstrip(',')
                sp = value
                line ='S,'
                line = line+str('%.4f'%sp)
                line = line+','+subset+'\n'
                file.write(line)
        

        #now, find association rules. first get all pairs in a list

        freq_pairs = []

        for key,value in freq_dict.items():
            freq_pairs.append(list(key))

        #now get all two combos of stuff in list
        for combo_pair in it.combinations(freq_pairs,2):
            
            left = set(combo_pair[0])
            right = set(combo_pair[1])
            
            u = left.union(right)
            u = list(u)
            u.sort()

            if u in freq_pairs:
                if len(left.intersection(right))==0:

                    sp = freq_dict[tuple(u)]
                    lhp = list(left)
                    lhp.sort()
                    rhp = list(right)
                    rhp.sort()

                    lsp = freq_dict[tuple(lhp)]
                    rsp = freq_dict[tuple(rhp)]

                    conf = sp / lsp
                    other_conf = sp/rsp

                    if conf >= minconf:
                        with open(output_filename,"a",newline="") as file:
                            left = str(lhp).replace('[','').replace(']','').replace("'", '').replace(" ",'')
                            right = str(rhp).replace('[','').replace(']','').replace("'", '').replace(" ",'')
                            line ='R,'
                            line = line+str('%.4f'%sp)
                            line = line +','+str('%.4f'%conf)+','
                            line = line + left +','+"'=>',"+right+'\n'
                            file.write(line)

                    if other_conf >= minconf:
                         with open(output_filename,"a",newline="") as file:
                            left = str(lhp).replace('[','').replace(']','').replace("'", '').replace(" ",'')
                            right = str(rhp).replace('[','').replace(']','').replace("'", '').replace(" ",'')
                            line ='R,'
                            line = line+str('%.4f'%sp)
                            line = line +','+str('%.4f'%other_conf)+','
                            line = line + right +','+"'=>',"+left+'\n'
                            file.write(line)


        file.close()

if __name__ == "__main__":
    armin = Armin()
    armin.apriori('input.csv', 'output.sup=0.5,conf=0.7.csv', 0.5, 0.7)
    armin.apriori('input.csv', 'output.sup=0.5,conf=0.8.csv', 0.5, 0.8)
    armin.apriori('input.csv', 'output.sup=0.6,conf=0.8.csv', 0.6, 0.8)