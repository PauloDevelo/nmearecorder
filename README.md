# nmearecorder
This is a program Java that runs as a service on my Raspberry Pi 1 revision 2.

It reads a TCP socket (Kplex) where nmea sentences are written.
Then, there is all the "mecanics" to decode any kind of NMEA sentence. In this project, I coded all the NMEA sentences that I receive in my sailboat.

Then, thanks to a NMEA service, I can subscribe to the sentences that I'm interested to for my own needs.

The first of the NMEA listener is the TimeService. TimeService that will be available in about 10 seconds after receiving the first GPRMC sentence.
It can provide a reliable and accurate time whatever the system time is. 
I plan to use this service to synchronize the date and time of my raspberry pi after having a lot of difficulties to setup a gpsd and ntp server ...

Just with NMEAService and TimeService and the library influxdb-java, I was able to create my own classes that push data effortless into Influx.

A Virtuino connector is now available. It can gets data from a microcontroler embeding a Virtuino firmware. The connection is made through a virtual Serial port (In my case over Bluetooth). The Virtuino Connector manages also the re-connection when the micro controler have been disconnected ...

The Virtuino Service can manage several Virtuino connectors. In my installation, I use Virtuino service with 2 Virtuino connectors : One for my diesel engine monitor (Arduino Uno with the Virtuino connector monitoring 2 different temperature points but also the engine age and the RPM), and another Virtuino connector for a motion detector.

After pushing some data into influx, I use Chronograf to make some cool dashboard. However, I figured out that my small Raspberry pi 1 struggles a bit to answer quickly.
After modifying the InfluxDB, I succeeded to make the InfluxDB faster by for example disabling all the logs and some others features not necessary for my project (See InfluxDB.conf in the properties folder).

As an example, there is a package called rtmodel with some classes which subscribe to few nmea sentences and some Virtuino values and just push some data into Influx...
