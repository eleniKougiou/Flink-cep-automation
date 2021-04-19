#!/usr/bin/env python3.7
# coding: utf-8

# In[1]:

import sys
import rstr
import string
import random
import pandas as pd
from numpy.random import default_rng
import numpy as np
import time


#####INPUT PARAMETERS #####
## pattern
## stream_length
## num_sub_streams
## window_size
## num_matches
## strict
###########################
#regular exrpession
pattern= sys.argv[1]  #'ab{0,2}c'

#length of generated stream (#events)
#this value include all individual substreams
#defined with the num_streams parameter (discussed next)
#the actual size of the stream is slightly larger because of
#the multi-event patterns we generate

stream_length = int(sys.argv[2])  #1000000

#num_sub_streams affects the number of sub-streams to create
#events will be distributed among sub-streams using UNI or ZIPF distribution
#use a value num_sub_streams > 0 for UNIFORM allocation of events acrorss num_sub_streams with ids in range[0,x)
#if num_sub_streams = 0 then a zipf distribution will be used with alpha = 2
num_sub_streams = int(sys.argv[3])  #2


#size of count-based window
window_size = int(sys.argv[4])  #5

#number of matches to generate
num_matches = int(sys.argv[5])  #5
strict = sys.argv[6]  #True #if false then it will pad generated matches with random characters \

#file name
fileName = sys.argv[7]  #"data.txt"

###############################
#note assuming uniform distribution each sub-stream will have about stream_length/(num_sub_streams*window_size) windows

verbose = False



# In[2]:


rng = default_rng()
alpha = 2
#build an array with id values based on selected distribution
if num_sub_streams > 0:
    Stream_IDs = [random.choice(range(num_sub_streams)) for i in range(stream_length)]
else:
    Stream_IDs = rng.zipf(alpha, stream_length)


# In[3]:


def get_stream_id():
    return random.choice(Stream_IDs)

def randomstream_generator_df_old(size,chars = string.ascii_lowercase):
    stream = pd.DataFrame(columns = ['pos', 'stream_id', 'event'])
    for i in range(size):
        stream_id = get_stream_id()
        event = random.choice(chars)
        stream.loc[i] = [i,stream_id,event]
    return stream

def randomstream_generator_df(size, chars = string.ascii_lowercase):
    data = []
    for i in range(size):
        stream_id = get_stream_id()
        event = random.choice(chars)
        data.append((i, stream_id,event))
        #stream.loc[i] = [i,stream_id,event]
    stream = pd.DataFrame(data,columns = ['pos','stream_id', 'event'])
    return stream

def augment_match(m, max_aug = 10):
    if verbose:
        print('Augmenting match:', m)
    for i in range(0,random.randrange(max_aug) + 1):
        #print(i)
        loc = random.randrange(len(m) - 1) + 1
        m = m[:loc] + '_' + m[loc:]
    if verbose:
        print('Augmented match:', m)
    return m


# In[4]:


start_time = time.time()
#create a stream of random events
stream=randomstream_generator_df(stream_length)
#print(stream)
end_time = time.time()
print('Execution time = %.6f seconds' % (end_time - start_time))

#stream


# In[5]:


#create a list of matches
matches=[]
for i in range(num_matches):
    s = rstr.xeger(pattern)
    if not strict:
        s = augment_match(s,10)
    matches.append(s)
if verbose:
    print(matches)


# In[6]:


#append matches at random positions
for s in matches:
    m_pos = random.randint(0, stream_length - 1)
    stream.loc[len(stream)] = [m_pos, get_stream_id(), s]

#print(stream)
#sort stream by pos so that injected matches are placed at their proper position
stream.sort_values(by='pos', inplace = True)
#print(stream)


# In[7]:


window_count = np.zeros(1 + max(Stream_IDs), dtype = np.uint32)
window_id = np.zeros(1 + max(Stream_IDs), dtype = np.uint32)

if True:
    f = open(fileName, "w")
    for index, row in stream.iterrows():
        stream_id = row['stream_id']
        if window_count[stream_id] >= window_size:
            window_id[stream_id] += 1
            window_count[stream_id] = 0
        for e in row['event']:
            window_count[stream_id] += 1
            #print(str(stream_id) + ',' + str(window_id[stream_id]) + ',' + e)
            f.write(str(stream_id) + ',' + str(window_id[stream_id]) + ',' + e + '\n')
#print('-1, -1, KILL')
f.write('-1, -1, KILL')
f.close()
