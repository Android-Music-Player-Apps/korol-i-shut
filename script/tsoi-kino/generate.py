# -*- coding: utf-8 -*-

import io
import os
import json
import mutagen

from collections import OrderedDict
from mutagen.mp3 import MP3

# CONFIG & CONSTANTS
ARTIST = "Виктор Цой - Кино"
GENRE = "Post Punk"
SITE = "https://www.kinomannia.ru/"
URL_PREFIX = "https://firebasestorage.googleapis.com/v0/b/bucket-tsoi-kino/o/"
URL_SUFFIX = "?alt=media"
IMAGE_COVER = "front.jpg"
LOCAL_PATH = "/Users/olehka/Desktop/Media/Music/Кино (Виктор Цой) - Дискография (1982-1990) [Remastered]/"
ID_PREFIX = "tsoi-kino-id"

track_dict = OrderedDict()
track_dict['id'] = ''
track_dict['title'] = ''
track_dict['album'] = ''
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
        dict['id'] = ID_PREFIX + '_00' + str(id)
    elif id < 100:
        dict['id'] = ID_PREFIX + '_0' + str(id)
    else:
        dict['id'] = ID_PREFIX + '_' + str(id)

def set_title(dict, title):
    dict['title'] = replace_title(title)

def set_album(dict, album):
    dict['album'] = album

def set_track_number(dict, num):
    dict['trackNumber'] = num

def set_total_track_count(dict, count):
    dict['totalTrackCount'] = count

def set_source(dict, album, title):
    dict['source'] = URL_PREFIX + album + "%2F" + title + URL_SUFFIX

def set_image(dict, album):
    dict['image'] = URL_PREFIX + album + "%2F" + IMAGE_COVER + URL_SUFFIX

def set_duration(dict, mp3_file):
    audio = MP3(mp3_file)
    dict['duration'] = int(round(audio.info.length))

def get_filename(path):
    return os.path.basename(path)

def get_dirname(path):
    dir = os.path.dirname(path)
    return os.path.basename(dir)

def replace_title(string):
    return string.replace(".mp3", "").replace(".", "").replace("!", "").replace(",", "")

def replace_url(string):
    return string.replace(" ", "%20").replace("+", "%2B").replace("й", "й").replace("ё", "ё")

mp3_paths = []

# r = root, d = directories, f = files
for r, d, f in os.walk(LOCAL_PATH):
    for file in f:
        if ".mp3" in file:
            path = os.path.join(r, file)
            mp3_paths.append(path)
            print(path)
            print(get_filename(path))
            print(get_dirname(path))

mp3_paths.sort()
catalog_list = []

index = 0
count = len(mp3_paths)
for path in mp3_paths:
    dict = track_dict.copy()
    album = get_dirname(path)
    file = get_filename(path)
    set_id(dict, index + 1)
    set_title(dict, file)
    set_album(dict, album)
    set_track_number(dict, index + 1)
    set_total_track_count(dict, count)
    set_source(dict, replace_url(album), replace_url(file))
    set_image(dict, replace_url(album))
    set_duration(dict, path)
    catalog_list.append(dict)
    index += 1

with io.open('catalog.json', 'w') as outfile:
    out_dict = { "music": catalog_list }
    string = json.dumps(out_dict, ensure_ascii = False, indent = 2)
    outfile.write(string)