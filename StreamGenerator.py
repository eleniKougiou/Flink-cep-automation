#!/usr/bin/env python
# coding: utf-8

# In[1]:

import sys
import rstr
import string
import random
import secrets

# generate a random stream of characters
def randomstream_generator(size=10, chars=string.ascii_lowercase):
    return ''.join(secrets.choice(chars) for _ in range(size))


# inserts a matching string at a random location in a stream
# note that we might inject a pattern within an already added pattern from a previous call
# this does not matter for our testing purposes, as long as the stream is large
# so that the probability of such a "collision" is small
def inject_one(stream, match):
    pos = secrets.randbelow(len(stream))
    return stream[:pos] + match + stream[pos:]


# more complex version of the above that injects a list of matches with no collisions
def inject_many(stream, matches):
    #print('original stream=', stream)
    pos = [0];
    n = len(matches)
    # create random positions where the matches will be added
    for m in matches:
        pos.append(secrets.randbelow(len(stream)))
    pos = sorted(pos)
    # print(pos)
    out = ''
    for i in range(1, n + 1):
        if verbose:
            print('injection ', i, ' of match', matches[i - 1], ' at position ', pos[i])
        out = out + stream[pos[i - 1]:pos[i]] + matches[i - 1]
    out += stream[pos[n]:]
    return out


# print(inject_many('012',['a','b','c','d']))

# augment a match using the _ character
# useful when testing no_strict policy

def augment_match(m, max_aug=10):
    if verbose:
        print('Augmenting match:', m)
    for i in range(0, secrets.randbelow(max_aug) + 1):
        # print(i)
        loc = secrets.randbelow(len(m) - 1) + 1
        m = m[:loc] + '_' + m[loc:]
    if verbose:
        print('Augmented match:', m)
    return m


# print debug info
verbose = True

# arguments: 1 = pattern, 2 = stream_length, 3 = num_matches, 4 = strict, 5 = output file
pattern = sys.argv[1]
stream_length = int(sys.argv[2])
num_matches = int(sys.argv[3])
strict = int(sys.argv[4]) == 1
output_file = sys.argv[5] #'seq.txt'

# initialize a random stream
stream = randomstream_generator(size=stream_length)
#if verbose:
#    print("initial stream: " + stream)

matches = []
for i in range(num_matches):
    s = rstr.xeger(pattern)
    if verbose:
        print("Generated a new pattern:", s)
    # stream=inject_one(stream,s)
    if not strict:
        s = augment_match(s, 10)
    matches.append(s)

generated_stream = inject_many(stream, matches)
#print(generated_stream)
f = open(output_file, "w")
#f.write(generated_stream)
for event in generated_stream:
    f.write(event + "\n")
f.close()

