# nmearecorder
This is a program Java that runs as a service on my Raspberry Pi 1 revision 2.

It reads a TCP socket (Kplex) where nmea sentences are written.
Then, there is all the "mecanics" to decode any kind of NMEA sentence. In this project, I coded all the NMEA sentences that I receive in my sailboat.

Then, thanks to a NMEA service, I can subscribe to the sentences that I'm interested to for my own needs.

The first of the NMEA listener is the TimeService. TimeService that will be available in about 10 seconds after receiving the first GPRMC sentence.
It can provide a reliable and accurate time whatever the system time is. 
I plan to use this service to synchronize the date and time of my raspberry pi after having a lot of difficulties to setup a gpsd and ntp server ...

Just with NMEAService and TimeService and the library influxdb-java, I was able to create my own classes that push data effortless into Influx.

After pushing some data into influx, I use Chronograf to make some cool dashboard. However, I figured out that my small Raspberry pi 1 struggles a bit to answer quickly.
So, the idea will be to develop my own dashboard more restrictive but more respectful of my tiny headless server...

As an example, there is a class called Vessel, which subscribes to few nmea sentences and just push some data into Influx.
