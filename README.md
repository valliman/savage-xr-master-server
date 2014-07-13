savage-master-server
====================

Master Server for Savage XR.

The server supports the following properties for configuration:

udp listening port of the master server, default: 11236
udp.listening.port = 11236

path and name of the dat file to be written, default: ./gamelist_full.dat
dat.file = ./gamelist_full.dat

the interval the dat file is written, default: 60
dat.file.writing.interval.seconds = 60

time in seconds a server is kept in the server list without sending a heartbeat, default: 90
server.state.max.storage.seconds = 90

Authors:
  valli

Contributers:
  Groentjuh