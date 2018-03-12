# RESTful API Service for use with Astral Framework 

<a name="table-of-contents"></a>
## Table of Contents
1. [Requirements](#requirments)
2. [Guide](#guide)
3. [Users of the Rest Api](#api-users)
4. [Authentication](#auth)
5. [Routes](#routes)
   * [Create a homebase](#post-homebase)
   * [Create a user](#post-user)
   * [Retrieve a user's info](#get-user)
   * [Create a file](#post-file)
   * [Retrieve a users created files](#get-created-files)
   * [Retrieve files at a users location](#get-nearby-files)

<a name="requirments"></a>
## Requirments 
1. [Node.js] version 6.9.1+
2. [npm] version 5.6.0+
3. [MongoDB] version 3.2.10+

<a name="guide"></a>
 ## Guide
 * Uses http protocol to send requests to certain defined routes 

<a name="api-users"></a>
 ## Users of the Rest Api
 * Users will not interact directly with the rest api, but will use the Astral framework
 * Each mobile application will be refered to as a ```home```
 
<a name="auth"></a>
 ## Authentication
 There will be two layers of authentication
 1. Each mobile application will be given a ```homeid``` which restricts their access to only make requests involving their own data.
 2. Each user will be given a ```userid``` this restricts their access within their respective homes 
 
 All requests must include a ```homeid``` and ```userid``` to enable access to any route, in the following format:
 ```
 https://api.domain.com/[homeid]/[userid]/ ...
 ```
<a name="routes"></a>
 # Routes 
 
  <a name="post-homebase"></a>
 ## Create a homebase
 * Route: __POST__ https://api.domain.com/homebase
 * Purpose: Create a new homebase
 * Required parameters
   * Request body
     * `appname`
       * Non-empty string containing alphanumeric characters
 
 <a name="post-user"></a>
 ## Create a user
 * Route: __POST__ https://api.domain.com/users
 * Purpose: Create a new user
 * Required parameters
   * Request body
     * `homeid`
       * Non-empty string containing alphanumeric characters
     * `userid`
       * Non-empty string containing alphanumeric characters, dashes and underscores
 
<a name="get-user"></a>
### Retrieve a user's info
* Route: __GET__ https://api.domain.com/users?userid&homeid
* Purpose: Get information about a user: userid,  etc
* Required parameters
  * URL
    * `[homeid]`
      * Non-empty string containing alphanumeric characters
    * `[userid]`
      * Non-empty string containing alphanumeric characters, dashes, or underscore

<a name="post-file"></a>
### Create a file
* Route: __POST__ https://api.domain.com/files
* Purpose: Create a file 
* Required parameters
  * In the request body
    * `homeid`
      * Non-empty string containing alphanumeric characters
    * `userid`
      * Non-empty string containing alphanumeric characters, dashes, or underscores
    * `name`
      * Non-empty string containing alphanumeric characters, dashes, underscores, or whitespace    
    * `location`
      * A json object consisting of fields: Longitude and Latitude
      * For both fields: Non-empty string containing alphanumeric characters, dashes, underscores, or whitespace
    * `experation`
      * Non-negative integer representing a UNIX timestamp in __seconds__
      * Lowest value possible is 0 (January 1st, 1970, 12:00 AM UTC) 
    * `data`
      * An optional json object field
 
<a name="get-created-files"></a>
### Retrieve a users created files
* Route: __GET__ https://api.domain.com/files?userid&homeid
* Purpose: Retrieve all files created by a user 
* Required parameters
  * URL
    * `[homeid]`
      * Non-empty string containing alphanumeric characters
    * `[userid]`
      * Non-empty string containing alphanumeric characters, dashes, or underscores

<a name="get-nearby-files"></a>
### Retrieve files at a users location
* Route: __GET__ https://api.domain.com/[homeid]/users/[userid]/files/nearby
* Purpose: Retrieve all files at users location
* Required parameters
  * URL
    * `[homeid]`
      * Non-empty string containing alphanumeric characters
    * `[userid]`
      * Non-empty string containing alphanumeric characters, dashes, or underscores   
  * In the request body
    * `location`
      * Non-empty string containing alphanumeric characters, dashes, underscores, or whitespace
 
 
