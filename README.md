# UDP-Client
Client requests a URL which is then sent by UDP to UDP-Server. Client acks all expected seq numbers.
It sends the appropriate ack for an unexpected seq number. It only considers one frame at a time, i.e. the sliding window has size = 1.
All packet sizes from the server are 1024 with the possible exception of the last packet. Any packet less than 1024 is deemed to be the last packet.
After the last packet has been received, the web page is saved to a file "./something.html" and compared to a file created by the server "../Server/something1.html".

