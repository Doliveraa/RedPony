from pymongo import MongoClient
import jwt
import json
import sys

config = json.load(open('config.json'))
client = MongoClient('localhost', 27017)
db = client.astral

def encode(ID):
    return jwt.encode({"id": str(ID)}, config["secret"], algorithm='HS256')

def get_app_token(file, name):
    apps = db.apps
    try: appid = apps.find_one({"name": name})["_id"]
    except: return None
    if file:
        with open(file, 'wb+') as fp: fp.write(encode(appid))
    else: print(encode(appid).decode('utf-8'))

def add_app(file, name):
    print(name)
    apps = db.apps
    if apps.find_one({"name": name}):
        return get_app_token(file, name)

    app = {"name": name}
    try: appid = apps.insert_one(app).inserted_id
    except: print('Error adding {}'.format(name))
    if file:
        with open(file, 'wb+') as fp: fp.write(encode(appid))
    else: print(encode(appid).decode('utf-8'))
