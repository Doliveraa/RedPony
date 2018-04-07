from pymongo import MongoClient
import jwt
import json
import os
import subprocess

file_path = os.path.dirname(os.path.realpath(__file__))

def init():
    """Sets up MongoDB client."""
    path = os.path.join(file_path, 'config.json')
    with open(path) as fp:
        config = json.load(fp)
    client = MongoClient('localhost', 27017)
    db = client.astral
    return config, client, db

def encode(ID, secret):
    """Creates a JSON Web Token.

    Args:
        ID (str): ID to encode
        secret (str): Secret to encode with

    Returns:
        (bytes): JSON Web Token
    """
    return jwt.encode({"id": str(ID)}, secret, algorithm='HS256')

def get_app_token(name, file=None):
    """Gets the JSON Web Token for an app in the database.

    Args:
        name (str): name of application to get token for
        file (str): path to file to write token to

    Returns:
        (bytes): JSON Web Token
    """
    print(name)
    print(file)
    return
    (config, client, db)= init()
    apps = db.apps
    try: appid = apps.find_one({"name": name})["_id"]
    except: return None
    if file:
        with open(file, 'wb+') as fp:
            fp.write(encode(appid, config["secret"]))
    else: print(encode(appid, config["secret"]).decode('utf-8'))

def add_app(name, file=None):
    """Adds an app to the database.

    Args:
        name (str): name of application to get token for
        file (str): path to file to write token to

    Returns:
        (bytes): JSON Web Token
    """
    print(name)
    print(file)
    return
    (config, client, db)= init()
    print(name)
    apps = db.apps
    if apps.find_one({"name": name}):
        return get_app_token(name, file)

    app = {"name": name}
    try: appid = apps.insert_one(app).inserted_id
    except: print('Error adding {}'.format(name))
    if file:
        with open(file, 'wb+') as fp:
            fp.write(encode(appid, config["secret"]))
    else: print(encode(appid, config["secret"]).decode('utf-8'))

def setup_api(port, secret, config_file):
    """Sets up the API.

    Args:
        port (int): port to setup API at
        secret (str): secret for creating JSON Web Tokens
        config_file (str): path to file to write config to
    """
    config = {
        'port': int(port),
        'secret': secret
    }
    path = os.path.join(file_path, 'config.json')
    with open(path, 'w+') as fp:
        json.dump(config, fp)

def run_script(script):
    """Runs a script.

    Args:
        script (str): script to run
    """
    path = os.path.join(file_path, script)
    subprocess.call(path)
