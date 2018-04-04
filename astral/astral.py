from pymongo import MongoClient
import jwt
import json
import os
import subprocess

file_path = os.path.dirname(os.path.realpath(__file__))

def init():
    path = os.path.join(file_path, 'config.json')
    with open(path) as fp:
        config = json.load(fp)
    client = MongoClient('localhost', 27017)
    db = client.astral
    return config, client, db

def encode(ID, secret):
    return jwt.encode({"id": str(ID)}, secret, algorithm='HS256')

def get_app_token(file, name):
    (config, client, db)= init()
    apps = db.apps
    try: appid = apps.find_one({"name": name})["_id"]
    except: return None
    if file:
        with open(file, 'wb+') as fp:
            fp.write(encode(appid, config["secret"]))
    else: print(encode(appid, config["secret"]).decode('utf-8'))

def add_app(file, name):
    (config, client, db)= init()
    print(name)
    apps = db.apps
    if apps.find_one({"name": name}):
        return get_app_token(file, name)

    app = {"name": name}
    try: appid = apps.insert_one(app).inserted_id
    except: print('Error adding {}'.format(name))
    if file:
        with open(file, 'wb+') as fp:
            fp.write(encode(appid, config["secret"]))
    else: print(encode(appid, config["secret"]).decode('utf-8'))

def setup_api(port, secret, config_file):
    config = {
        'port': int(port),
        'secret': secret
    }
    path = os.path.join(file_path, 'config.json')
    with open(path, 'w+') as fp:
        json.dump(config, fp)

def run_script(script):
    path = os.path.join(file_path, script)
    subprocess.call(path)
