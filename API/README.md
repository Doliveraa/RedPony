# RESTful API Service for use with Astral Framework

<a name="table-of-contents"></a>
## Table of Contents
1. [Requirements](#requirements)
2. [Guide](#guide)
3. [Users of the Rest Api](#api-users)
4. [Authentication](#auth)
5. [Routes](#routes)
   * [Create a user](#post-user)
   * [Retrieve a user's JSON Web Token](#login-user)
   * [Retrieve a user's info](#get-user)
   * [Create a file](#post-file)
   * [Retrieve a users created files](#get-created-files)
   * [Retrieve files at location](#get-files-by-location)
   * [Update a file](#update-file)

<a name="requirements"></a>
## Requirements
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
 1. Each mobile application will be given a ```appKey``` which restricts their access to only make requests involving their own data.
 2. Each user will have an ```token``` that restricts their access within their respective apps

 All requests must include a ```appKey``` and ```token``` to enable access to any route, in the following format:

<a name="routes"></a>
 # Routes

 <a name="post-user"></a>
 ## Create a user
 * Route: __POST__ https://api.domain.com/users
 * Purpose: Create a new user
 * Required parameters
   * Request header
     * `appKey`
       * JSON Web Token for application
   * Request body
     * `username`
       * User's username
     * `email`
       * User's email address
     * `password`
       * User's password
     * `data`
       * JSON object

<a name="login-user"></a>
### Retrieve a user's JSON Web Token
* Route: __GET__ https://api.domain.com/users
* Purpose: Get information about a user: username,  etc
* Required parameters
  * Request header
    * `appKey`
      * JSON Web Token for application
    * `email`
      * User's email address
    * `password`
      * User's password

<a name="get-user"></a>
### Retrieve a user's info
* Route: __GET__ https://api.domain.com/users
* Purpose: Get information about a user: username,  etc
* Required parameters
 * Request header
   * `appKey`
     * JSON Web Token for application
   * `token`
     * JSON Web Token for user

<a name="post-file"></a>
### Create a file
* Route: __POST__ https://api.domain.com/files
* Purpose: Create a file
* Required parameters
 * Request header
   * `appKey`
     * JSON Web Token for application
   * `token`
     * JSON Web Token for user
  * Request body
    * `name`
      * File name   
    * `longitude`
      * File longitude
    * `latitude`
      * File latitude
    * `expiration`
      * File expiration
      * Non-negative integer representing a UNIX timestamp in __seconds__
      * Lowest value possible is 0 (January 1st, 1970, 12:00 AM UTC)
    * `data`
      * JSON Object

<a name="get-created-files"></a>
### Retrieve a users created files
* Route: __GET__ https://api.domain.com/files
* Purpose: Retrieve all files created by a user
* Required parameters
 * Request header
   * `appKey`
     * JSON Web Token for application
   * `token`
     * JSON Web Token for user

<a name="get-files-by-location"></a>
### Retrieve files within some distance from a given location
* Route: __GET__ https://api.domain.com/files
* Purpose: Retrieve all files created by a user
* Required parameters
  * Request header
    * `appKey`
      * JSON Web Token for application
    * `token`
      * JSON Web Token for user
    * `longitude`
      * File longitude
    * `latitude`
      * File latitude
    * `radius`
      * Radius from latitude and longitude to get files

<a name="update-file"></a>
### Update a users file
* Route: __PUT__ https://api.domain.com/files
* Purpose: Update a user's file using
* Required parameters
  * Request header
    * `appKey`
      * JSON Web Token for application
    * `token`
      * JSON Web Token for user
  * Request body
    * '\_id'
      * File ID (MongoDB id)
    * `name`
      * Optional
      * New file name   
    * `longitude`
      * Optional
      * New file longitude
    * `latitude`
      * Optional
      * New file latitude
    * `expiration`
      * Optional
      * New file expiration
      * Non-negative integer representing a UNIX timestamp in __seconds__
      * Lowest value possible is 0 (January 1st, 1970, 12:00 AM UTC)
    * `data`
      * Optional
      * New JSON Object
