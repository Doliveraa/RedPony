# Astral 
## Table of Contents
1. [Summary](#summary)  
1. [Interface Documentation](#interface-documentation)

## Summary
Astral is a framework that allows developers to create file-sharing and social applications with strict location restrictions. 

## Interface Documentation
[addFileType(name, data)](#addfiletype)  
[addFile(type, name, location, expiration, data, username): bool](#addfile)  
[getFiles(username, location): list](#getfiles)  

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

