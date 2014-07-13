![Savage XR](http://savagexr.com/media/savagexr_logo_600.png)
savage-master-server
====================
Master Server for Savage XR.

## Getting Started
Simply download the savage-master-server-1.0.0.jar

> Open a command shell and start the server with the following command
* `java -jar savage-master-server-1.0.0.jar`

## Configuration
The server allows to configure some properties, listed below.

> udp listening port of the master server, default: 11236
* `udp.listening.port = 11236` 

> path and name of the dat file to be written, default: ./gamelist_full.dat
* `dat.file = ./gamelist_full.dat` 

> the interval the dat file is written, default: 60
* `dat.file.writing.interval.seconds = 60` 

> time in seconds a server is kept in the server list without sending a heartbeat, default: 90
* `server.state.max.storage.seconds = 90` 


## The team
Authors:
  valli

Contributers:
  Groentjuh