# Astral Interface
[addUserType(name, data)](#addusertype)  
[addUser(type, username, password, data): bool](#adduser)  
[getUser(username): dict](#getuser)  
[authUser(username, password): dict](#authuser)  
[addFileType(name, data)](#getuser)  
[addFile(type, name, location, expiration, data, username): bool](#addfile)  
[getFiles(username, location): list](#getfiles)  

### addUserType
* **parameters**
  * name (string): name of user type
  * data (dict): dictionary of user data where keys are names of fields (e.g. age, sex) and values are an array: [dtype (string: int, float, string, bool), required (bool) , default]
* **returns**: None
* **Example**
```javascript
addUserType(name='student',  
	{'name': [dtype='string', required=True],  
	 'id': [dtype='int', required=True],  
	 'gpa': [dtype='float', required=False, default=4.0]  
	})  
```

### addUser
* **parameters**
  * type (string): type of user to add
  * username (string): username (must be unique)
  * password (string): password (hash of password is stored)
  * data (dict): dictionary of user data (required fields not provided with throw an error, invalid fields will be ignored)
* **returns**
  * false if there was an error in creating the user
  * true if user was created with success
* **Example**
```javascript  
addUser(type='student', username='jaredraycoleman', password='1234',  
	{'name': 'Jared',  
	 'id': 011779753  
	})   
```  

### getUser
* **parameters**
  * username (string): username
* **returns**: dict of *public* user attributes
* **Example**
```javascript
user = getUser('jaredraycoleman')  
```

### authUser
* **parameters**
  * username (string): username (must be unique)
  * password (string): password (hash is stored)
* **returns**: dict of all user data
  ##### Example
```javascript
user = authUser('jaredraycoleman', '1234')  
```

### addFileType
* parameters
  * name (string): name of file type
  * data (dict): dictionary of file data where keys are names of fields (e.g. age, sex) and values are an array: [dtype (string: int, float, string, bool), required (bool) , default]
* returns: None
  ##### Example
```javascript
addFileType(name='photo', {'path': [dtype='string', required=True]})  
```

### addFile
* **parameters**
  * type (string): type of user to add
  * name (string): name of file
  * location (string): coordinates of file
  * expration (datetime): expiration of file 
  * username (string): owner of file
  * data (dict): dictionary of file data (required fields not provided with throw an error, invalid fields will be ignored)
* **returns**
  * false if there was an error in creating the file
  * true if file was created with success
* **Example**
```javascript  
addFile(type='photo', name='toast', location='33.7760589 -118.2000163',
        expiration='02/30/2018, 'username='jaredraycoleman', 
	{'path': 'imgs/toast.jpg'})   
```  

### getFiles 
* **parameters**
  * username (string): username (for permissions)
  * location (string): user location for file query
* **returns**: list of files matching query
* **Example**
```javascript
files = getFiles('jaredraycoleman', location='33.7760589 -118.2000163')
```

