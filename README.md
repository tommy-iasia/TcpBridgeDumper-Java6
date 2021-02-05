# TcpBridgeDumper-Java6
Bridge TCP traffic and dump data or statistics

![preview](https://raw.githubusercontent.com/tommy-iasia/TcpBridgeDumper-Java6/master/preview/main.png)

# Configuration
The configuration is inside **Program.config**

| Group | Property | Descrption |
| ---- | ---- | ---- |
|Inward|InwardHost|inward server accepts clients|
| |InwardPort| |
|Outward|OutwardHost|outward connect back to originally destinated server|
| |OutwardPort| |

# Usage

1. Edit **Program.config**
2. Configure the outward address to originally destinated server address.
3. Configure the testing client to connect to inward address instead of original server address.
4. Turn on the dumper using **Program.cmd**
5. Start testing client

Then the dumper will dump statistics every 5 seconds.

# Dump Data

If statistics does not satisfy you. You may choose to dump data by *pressing **1** and enter*.

![dump](https://raw.githubusercontent.com/tommy-iasia/TcpBridgeDumper-Java6/master/preview/dumping.png)

Four files will be created for **read server, write client, read client, write server**.

The files are in binary format. And a file contains packages of the following format.

| Time | Payload Length | Payload Data |
| ---- | ---- | ---- |
| Long/millisecond | Int/number of bytes | (according to payload length) |

When you want to stop dumping data, *press **0** and enter*.
