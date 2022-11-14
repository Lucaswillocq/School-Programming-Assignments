import argparse
import collections
import csv
import json
import glob
import math
import os
import pandas as pd
import re
from requests import get
import string
import sys
import time
import xml



class Bike():
    def __init__(self, baseURL, station_info, station_status):
        # initialize the instance
        
        
        station_infoURL = baseURL+station_info
        # for each docking station - station_id, name, lattitude / longitude, total capacity
        station_statusURL = baseURL+station_status
        # for each station Id - how many bikes and how many docks are 
        
        info = get(station_infoURL)
        status = get(station_statusURL)

        info_dictionary = json.loads(info.content)
        status_dictionary = json.loads(status.content)

        info_data = info_dictionary['data']
        status_data = status_dictionary['data']

        info_list = info_data['stations']
        status_list = status_data['stations']

        self.info_df = pd.DataFrame(info_list)
        self.status_df = pd.DataFrame(status_list)

        


    def distance(self, lat1, lon1, lat2, lon2):
        p = 0.017453292519943295
        a = 0.5 - math.cos((lat2-lat1)*p)/2 + math.cos(lat1*p)*math.cos(lat2*p) * (1-math.cos((lon2-lon1)*p)) / 2
        return 12742 * math.asin(math.sqrt(a))

    def total_bikes(self):
       
        totalbikes = self.status_df['num_bikes_available'].sum()
        return totalbikes

    def total_docks(self):
        
        totaldocks = self.status_df['num_docks_available'].sum()
        return totaldocks

    def percent_avail(self, station_id):
       
        found = False

        station_id = str(station_id)
       
        for i in range(len(self.status_df['station_id'])):
            if(self.status_df['station_id'][i]==station_id):
                found = True
                row = self.status_df['station_id']

        if found==False:
            return ""

        row = self.status_df.loc[self.status_df['station_id']==station_id]

        bikes = row['num_bikes_available']

        docks = row['num_docks_available']
    

        return str(math.floor(100*(docks/(bikes+docks))))+'%'

    def closest_stations(self, latitude, longitude):
        # return the stations closest to the given coordinates
        #  return a dictionary with station_ids and names of 3 closest (strings mapped to strings)
        # go through list of stations, using distance method to calculate smallest distance()
        # 

        

        station_distance = {}

        result = {}

        for i in range(len(self.info_df)):

            rowlat = self.info_df['lat'][i] 
            rowlon = self.info_df['lon'][i]
            distance = self.distance(latitude,longitude,rowlat,rowlon)
            station_distance[distance] = self.info_df['station_id'][i]

      
        distances = sorted(station_distance.keys())
        closest = station_distance[distances[0]]
        second_closest = station_distance[distances[1]]
        third_closest = station_distance[distances[2]]

        row = self.info_df.loc[self.info_df['station_id']==closest]
        second_row =  self.info_df.loc[self.info_df['station_id']==second_closest]
        third_row =   self.info_df.loc[self.info_df['station_id']==third_closest]

        name = row['name']
        second_name = second_row['name']
        third_name = third_row['name']

        result[closest] = name.values[0]
        result[second_closest] = second_name.values[0]
        result[third_closest] = third_name.values[0]

        return result


    def closest_bike(self, latitude, longitude):
        # return the station with available bikes closest to the given coordinates
        # return dictionary value of string mapped to strings, station_id and name 
        
        result = {}

        first_time = True

        for i in range(len(self.info_df)):

            rowlat = self.info_df['lat'][i]
            rowlon = self.info_df['lon'][i]
            distance = self.distance(latitude,longitude,rowlat,rowlon)

            if first_time == True:

                small_distance = distance
                small_id = self.info_df['station_id'][i]
                small_name = self.info_df['name'][i]

                first_time = False

            else:

                if distance < small_distance:

                    small_distance = distance
                    small_id = self.info_df['station_id'][i]
                    small_name = self.info_df['name'][i]

        result[small_id] = small_name

        return result
        
    def station_bike_avail(self, latitude, longitude):
        
        # sreturn the station id and available bikes that correspond to the station with the given coordinates
        
        
        result = {}

        found = False

        for i in range(len(self.info_df)):
            if(self.info_df['lat'][i] == latitude):
                if(self.info_df['lon'][i] == longitude):
                    found = True
                    station_id = self.info_df['station_id'][i]
                    result[station_id] = self.status_df['num_bikes_available'][i]

        if found == False:
            return result
        
        return result
  


# testing and debugging the Bike class

if __name__ == '__main__':
    instance = Bike('https://api.nextbike.net/maps/gbfs/v1/nextbike_pp/en', '/station_information.json', '/station_status.json')
    print('------------------total_bikes()-------------------')
    t_bikes = instance.total_bikes()
    print(type(t_bikes))
    print(t_bikes)
    print()

    print('------------------total_docks()-------------------')
    t_docks = instance.total_docks()
    print(type(t_docks))
    print(t_docks)
    print()

    print('-----------------percent_avail()------------------')
    p_avail = instance.percent_avail(342885) # replace with station ID
    print(type(p_avail))
    print(p_avail)
    print()

    print('----------------closest_stations()----------------')
    c_stations = instance.closest_stations(40.444618, -79.954707) # replace with latitude and longitude
    print(type(c_stations))
    print(c_stations)
    print()

    print('-----------------closest_bike()-------------------')
    c_bike = instance.closest_bike(40.444618, -79.954707) # replace with latitude and longitude
    print(type(c_bike))
    print(c_bike)
    print()

    print('---------------station_bike_avail()---------------')
    s_bike_avail = instance.station_bike_avail(40.445834, -79.954707) # replace with exact latitude and longitude of station
    print(type(s_bike_avail))
    print(s_bike_avail)
