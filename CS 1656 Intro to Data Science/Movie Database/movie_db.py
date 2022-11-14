import sqlite3 as lite
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
import sqlite3
import sys
import time
import xml


class Movie_db(object):
    def __init__(self, db_name):
        #db_name: "cs1656-public.db"
        self.con = lite.connect(db_name)
        self.cur = self.con.cursor()
    
    #q0 is an example 
    def q0(self):
        query = '''SELECT COUNT(*) FROM Actors'''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q1(self):
        query = '''
           SELECT fname,lname FROM Actors 
           INNER JOIN Cast c1 ON c1.aid = actors.aid 
           INNER JOIN Cast c2 ON C2.aid = actors.aid 
           INNER JOIN movies m1 ON m1.mid = c1.mid 
           INNER JOIN movies m2 on m2.mid = c2.mid 
           WHERE (m1.year <= 1990 AND m1.year >= 1980) AND m2.year >=2000 ORDER BY lname,fname
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows
        

    def q2(self):
        query = ''' 
            SELECT title,year FROM movies WHERE year = (SELECT YEAR FROM movies WHERE title = 'Rogue One: A Star Wars Story') AND rank > (SELECT rank FROM movies WHERE title = 'Rogue One: A Star Wars Story') ORDER BY title 
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q3(self):


        query = '''SELECT fname,lname, count(distinct mid) as movies from actors natural join cast natural join movies where title like '%Star Wars%' group by aid order by movies desc,lname,fname'''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows


    def q4(self):
        query = '''
          SELECT fname,lname frOM Actors natural join cast natural join movies group by aid having max(year) < 1980 order by lname,fname
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q5(self):
        query = '''
            SELECT fname, lname, count(distinct mid) as films from directors natural join Movie_Director group by did order by films desc, lname, fname limit 10
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q6(self):
        query = '''
           SELECT title,count(aid) as num from cast natural join movies group by mid having num >= (SELECT min(num) from (SELECT count(aid) AS num from Movies natural join Cast group by mid
ORDER BY num DESC LIMIT 10))
    ORDER BY num DESC
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q7(self):

        men = '''CREATE VIEW MEN AS select mid,title, count(*) as males from actors natural join cast natural join movies where gender='Male' group by mid
        '''
        women = '''CREATE VIEW WOMEN AS select mid,title, count(*) as women from actors natural join cast natural join movies where gender='Female' group by mid
        '''
        self.cur.execute(men)
        self.cur.execute(women)
        query = '''
            select title,women,males from men natural join women where (women>males) group by mid order by title
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q8(self):
        query = '''
           SELECT a.fname, a.lname,count(distinct md.did) as dcount from cast c, actors a, Movie_Director md, Directors d 
where c.mid = md.mid and c.aid = a.aid and d.did = md.did and not (a.fname = d.fname AND a.lname = d.lname) group by c.aid having dcount >=7 order by dcount desc 
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows


    def q9(self):

        debuts = '''CREATE VIEW DEBUTS AS
SELECT aid,mid,fname,lname,min(year) as debut,
SUBSTR(fname, 1, 1) AS firstletter FROM actors natural join cast natural join movies group by aid having firstletter = "D"'''

        self.cur.execute(debuts)
        query = '''
    SELECT fname,lname,count(c.mid) as total from cast c, debuts d, movies m where d.aid = c.aid and d.mid = c.mid and m.year = d.debut group by c.mid order by total desc,fname,lname
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q10(self):
        query = '''
            SELECT a.lname, m.title FROM Actors a, cast c, movies m, Movie_Director md, Directors d where a.aid = c.aid and c.mid = m.mid and c.mid = md.mid and d.did = md.did and a.lname = d.lname order by a.lname,m.title
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q11(self):

        bacon = '''CREATE VIEW BACON AS
SELECT distinct m.mid,m.title FROM Actors a, cast c, movies m, Movie_Director md, Directors d where a.aid = c.aid and c.mid = m.mid and m.mid = md.mid and d.did = md.did and (a.fname= 'Kevin' AND a.lname = 'Bacon')
'''
        self.cur.execute(bacon)
        query = '''
            SELECT a2.fname,a2.lname from bacon b, actors a1, actors a2, cast c1, cast c2, cast c3, movies m where c1.mid = b.mid and a1.aid = c1.aid and c2.aid = a1.aid and m.mid = c2.mid and c3.mid = m.mid and a2.aid = c3.aid and c3.mid!=b.mid and a1.aid != c3.aid and a1.fname != "Kevin" and a1.lname!="Bacon" order by a2.lname,a2.fname
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

    def q12(self):
        query = '''
            SELECT fname, lname, COUNT(*) as ct, AVG(rank) as averagerank from Actors a natural join cast natural join Movies group by fname, lname order by averagerank desc limit 20
        '''
        self.cur.execute(query)
        all_rows = self.cur.fetchall()
        return all_rows

if __name__ == "__main__":
    task = Movie_auto("cs1656-public.db")
    rows = task.q0()
    print(rows)
    print()
    rows = task.q1()
    print(rows)
    print()
    rows = task.q2()
    print(rows)
    print()
    rows = task.q3()
    print(rows)
    print()
    rows = task.q4()
    print(rows)
    print()
    rows = task.q5()
    print(rows)
    print()
    rows = task.q6()
    print(rows)
    print()
    rows = task.q7()
    print(rows)
    print()
    rows = task.q8()
    print(rows)
    print()
    rows = task.q9()
    print(rows)
    print()
    rows = task.q10()
    print(rows)
    print()
    rows = task.q11()
    print(rows)
    print()
    rows = task.q12()
    print(rows)
    print()
