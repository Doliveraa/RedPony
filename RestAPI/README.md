# RESTful API Service for use with Astral Framework 

## Requirments 
1. [Node.js] version 6.9.1+
2. [npm] version 5.6.0+
3. [MongoDB] version 3.2.10+

 ## Guide
 * Uses http protocol to send requests to certain defined routes 

 ## Users of the Rest Api
 * Users will not interact directly with the rest api, but will use the Astral framework
 * Each mobile application will be refered to as a ```home```
 
 ## Authentication
 There will be two layers of authentication
 1. Each mobile application will be given a ```homeid``` which restricts their access to only make requests involving their own data.
 2. Each user will be given a ```userid``` this restricts their access within their respective homes 
 
 All requests must include a ```homeid``` and ```userid``` to enable access to any route, in the following format:
 ```https://[domain_name]/[homeid]/[userid]/ ...```
 
