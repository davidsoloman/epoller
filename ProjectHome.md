ePoller is a very small and simple java application that allows you to monitor a group of similar devices via SNMP v2 with a defined list of parameters (oids).

I created ePoller for monitoring a very low bandwidth/high latency telemetry network wich can not afford multiple requests to be transmitted at the same time, thus ePoller can be configured to send SNMP request sequentially at a defined rate and frequency.

A single .csv output file is created for every device. This file can be plotted using whatever tool you want to use, gnuplot for example.

As an example, my current setup is monitoring 100 devices on a network with an average latency of 300ms very well. ePoller is running on a raspberry pi..

**Licensing**

ePoller uses two external libraries: snmp4j and opencsv (with a small feature added and the unneeded ones removed), this libraries are independent and have their own licences. ePoller itself uses the Apache License 2.0.