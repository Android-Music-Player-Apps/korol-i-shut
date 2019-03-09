# -*- coding: utf-8 -*-

import io
import os
import json
import mutagen

from collections import OrderedDict
from mutagen.mp3 import MP3

# CONSTANTS
ARTIST = "Король и шут"
GENRE = "Punk Rock"
SITE = "http://www.korol-i-shut.ru/news/"
FIREBASE_START = "https://firebasestorage.googleapis.com/v0/b/korol-i-shut.appspot.com/o/"
FIREBASE_IMAGE = "cover.png"
FIREBASE_END = "?alt=media"

# config
PATH = "/Users/olehka/Desktop/Media/Король и Шут/2010 - Театр Демона (2010, Никитин)/"
ID_PREFIX = "teatr_demona"
ALBUM = "2010 - Театр Демона"
FIREBASE_ALBUM = "2010_Teatr-demona%2F"

track_dict = OrderedDict()
track_dict['id'] = ''
track_dict['title'] = ''
track_dict['album'] = ALBUM
track_dict['artist'] = ARTIST
track_dict['genre'] = GENRE
track_dict['source'] = ''
track_dict['image'] = ''
track_dict['trackNumber'] = ''
track_dict['totalTrackCount'] = ''
track_dict['duration'] = ''
track_dict['site'] = SITE

def set_id(dict, id):
    if id < 10:
        dict['id'] = ID_PREFIX + '_0' + str(id)
    else:
        dict['id'] = ID_PREFIX + '_' + str(id)

def set_title(dict, title):
    dict['title'] = title.replace(".mp3", "").replace(".", "").replace("!", "").replace(",", "")

def set_track_number(dict, num):
    dict['trackNumber'] = num

def set_total_track_count(dict, count):
    dict['totalTrackCount'] = count

def set_source(dict, title):
    dict['source'] = FIREBASE_START + FIREBASE_ALBUM + title.replace(" ", "%20").replace("й", "й").replace("+", "%2B").replace("ё", "ё") + FIREBASE_END

def set_image(dict):
    dict['image'] = FIREBASE_START + FIREBASE_ALBUM + FIREBASE_IMAGE + FIREBASE_END

def set_duration(dict, mp3_file):
    audio = MP3(mp3_file)
    dict['duration'] = int(round(audio.info.length))

mp3_files = []
mp3_paths = []

# r = root, d = directories, f = files
for r, d, f in os.walk(PATH):
    for file in f:
        if ".mp3" in file:
            mp3_files.append(file)
            mp3_paths.append(os.path.join(r, file))

mp3_files.sort()
mp3_paths.sort()

catalog_list = []

index = 0
count = len(mp3_files)
for mp3_f in mp3_files:
    copy = track_dict.copy()
    set_id(copy, index + 1)
    set_title(copy, mp3_f)
    set_track_number(copy, index + 1)
    set_total_track_count(copy, count)
    set_source(copy, mp3_f)
    set_image(copy)
    set_duration(copy, mp3_paths[index])
    catalog_list.append(copy)
    index += 1

# print(json.dumps(catalog_list, ensure_ascii = False, indent = 2))
with io.open('hello.txt', 'w') as outfile:
    temp = json.dumps(catalog_list, ensure_ascii = False, indent = 2)
    outfile.write(temp.decode('utf-8'))