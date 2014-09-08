![Savage XR](http://savagexr.com/media/savagexr_logo_600.png)
Savage XR Master Server
====================
The repository hosts the code and the binaries of the Savage XR Master Server. 

## About Savage XR
Savage XR, the successor of the 2003 award winning PC game [Savage: The Battle for Newerth](http://www.savagexr.com/savage-the-battle-for-newerth-download.html), 
takes the best of the Real-time Strategy, First-person Shooter and Third-person Action genres and blends it into a cohesive, 
complex and addicting experience. Aim of the game is to destroy the enemy base. Savage XR is entirely free and non-profit! 
Visit [savagexr.com](http://savagexr.com) for more information.

## About Savage XR Master Server
The whole responsibility of the master server is it to 
generate a server list for Savage XR clients. To do that the master server stores state information provided by 
Savage XR gaming servers via heart-beating.

### Requirements

> Java 1.7

### Getting Started
Simply download the [savage-master-server-1.3.0.jar](https://github.com/valliman/savage-xr-master-server/releases/download/1.3.0/savage-master-server-1.3.0.jar)

> Open a command shell and start the server with the following command:
* `java -jar savage-master-server-1.3.0.jar`

### Configuration
The server allows to configure some properties, which are listed below:

> the listening port of the master server, default: 11236
* `listening.port = 11236` 

> the websocket port of the master server, default: 8080
* `websocket.port = 8080` 

> the path and name of the dat file to be written, default: ./gamelist_full.dat
* `dat.file = ./gamelist_full.dat` 

> the interval in seconds the dat file is re-written, default: 60
* `dat.file.writing.interval.seconds = 60` 

> the time in seconds a server is kept in the server list without sending a heartbeat, default: 90
* `server.state.max.storage.seconds = 90` 

## Team
Authors:
  valli

Contributors:
  Groentjuh,
  biggeruniverse
