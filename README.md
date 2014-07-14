![Savage XR](http://savagexr.com/media/savagexr_logo_600.png)
Savage XR Master Server
====================
The repository hosts the code of the Savage XR Master Server. The whole responsibility of the master server is it to 
generate a server list for Savage XR clients. To do that the master server stores state information provided by 
Savage XR gaming servers via heart-beating.

## Requirements

> Java 1.7

## Getting Started
Simply download the [savage-master-server-1.0.5.jar](https://github.com/valliman/savage-master-server/releases/download/1.0.5/savage-master-server-1.0.5.jar)

> Open a command shell and start the server with the following command:
* `java -jar savage-master-server-1.0.5.jar`

## Configuration
The server allows to configure some properties, which are listed below:

> listening port of the master server, default: 11236
* `listening.port = 11236` 

> path and name of the dat file to be written, default: ./gamelist_full.dat
* `dat.file = ./gamelist_full.dat` 

> the interval în seconds the dat file is re-written, default: 60
* `dat.file.writing.interval.seconds = 60` 

> time in seconds a server is kept in the server list without sending a heartbeat, default: 90
* `server.state.max.storage.seconds = 90` 


## The team
Authors:
  valli

Contributors:
  Groentjuh