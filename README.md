# Astral
Astral is a framework that allows developers to create file-sharing and social applications with strict location restrictions.

## Table of Contents
1.  [Installation](#installation)  
1.  [Usage](#usage)  
    *  [Setup](#setup)  
    *  [Deployment](#deployment)
    *  [Apps](#apps)

## Installation
To install, use the provided setup script:

```bash
./setup.sh
```

## Usage
You can now use astral as a command-line tool. For help, use:

```bash
astral --help
```

For help with a specific command, use:

```bash
astral <command> --help
```

### Setup
The setup script mentioned in the [Installation](#installation) section does this for you, but if you would like to set up your API using astral, run:

```bash
astral setup-api --savedir <directory to save configs to>
```

### Deployment
To deploy your API, run:
```bash
astral start
```

To stop your API, run:
```bash
astral stop
```

To restart your API, run:
```bash
astral restart
```

### Apps
Astral helps you manage your users and other files on an app-by-app basis. To create an app, run:

```bash
astral add-app <app_name>
```

Astral will print the json web token that can be used to access a given app's database.
Optionally, you can specify a file to write the token to:

```bash
astral add-app <app_name> --file <filename>
```
