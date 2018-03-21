from pymongo import MongoClient
import jwt
import json
import sys

config = json.load(open('config.json'))
client = MongoClient('localhost', 27017)
db = client.astral

def encode(ID):
    return jwt.encode({"id": str(ID)}, config["secret"], algorithm='HS256')

def get_app_token(name):
    apps = db.apps
    try: appid = apps.find_one({"name": name})["_id"]
    except: return None
    return encode(appid)

def add_app(name):
    apps = db.apps
    if apps.find_one({"name": name}):
        return get_app_token(name)

    app = {"name": name}
    try: appid = apps.insert_one(app).inserted_id
    except: return None
    return encode(appid)

def main():
    if len(sys.argv) < 2:
        print("Usage: python astral.py <command>")
        print("   eg: python astral.py help")
        return

    if sys.argv[1] == "help":
        print("--valid commands--")
        print("add <app name>")
        print("get <app name>")
    if sys.argv[1] == "add":
        if len(sys.argv) < 3:
            print("Usage: python astral.py add <app name>")
        else:
            print("token:",add_app(sys.argv[2]).decode('utf-8'))
    if sys.argv[1] == "get":
        if len(sys.argv) < 3:
            print("Usage: python astral.py get <app name>")
        else:
            print("token:",get_app_token(sys.argv[2]).decode('utf-8'))

if __name__ == '__main__': main()
