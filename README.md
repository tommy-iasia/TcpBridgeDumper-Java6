# TcpBridgeDumper-Java6
Bridge TCP traffic and dump data or statistics

![preview](https://raw.githubusercontent.com/tommy-iasia/TcpBridgeDumper-Java6/master/preview/main.png)

# Configuration
The configuration is inside **Program.properties**

| Group | Property | Descrption |
| ---- | ---- | ---- |
|Inward|InwardHost|inward server accepts clients|
| |InwardPort| |
|Outward|OutwardHost|outward connect back to originally destinated server|
| |OutwardPort| |

# Usage

1. Edit **Program.properties**
2. Configure the outward address to originally destinated server address.
3. Configure the testing client to connect to inward address instead of original server address.
4. Turn on the dumper using **Program.cmd**
5. Start testing client

# Statistics

The dumper outputs statistics every 5 seconds. One row for one active client.

> [20210205153323] [0, /10.112.125.164 < /10.112.125.164:53592] right { read { content 0MB / 2412B / 0kx / 4x / 2ms / 2349900ns, zero 0kx / 0x / 0ms / 0ns }, write { content 0MB / 2412B / 0kx / 4x / 0ms / 192000ns, zero 0kx / 0x / 0ms / 0ns}, buffer 0MB / 0KB / 0B, dump read, dump write }, left { read { content 0MB / 0B / 0kx / 0x / 0ms / 0ns, zero 0kx / 0x / 0ms / 0ns }, write { content 0MB / 0B / 0kx / 0x / 0ms / 0ns, zero 0kx / 0x / 0ms / 0ns}, buffer 0MB / 0KB / 0B, dump read, dump write }

The above record row can be splitted into 4 parts

| time | addresses | right | left |
| ---- | ---- | ---- | ---- |
| [20210205153323] | [0, /10.112.125.164 < /10.112.125.164:53592] | right { read { content ... } ... } | left { read { content ... } ... } |
| time in ms | index, server address and client address | rightward traffic from server to client | leftward traffic from client to server |

For right and left traffic, it can be further splitted into

| read | write | buffer | dump |
| ---- | ---- | ---- | ---- |
| { content ... } | { content ... } | data left in buffer | dump enabled |

And the traffic statistics are

| Property | Unit | Description
| ---- | ---- | ---- |
| content length | MB / B | data length |
| read/write count | kx / x | count for read/write with content |
| used time on content | ms / ns | socket time used when content is successfully read or written |
| zero read/write count | kx / x | count for read/write without content |
| used time on zero content | ms / ns | socket time used when zero byte is read or written |

Then you can calculate the package size by dividing content length with count, or the average socket time by dividing used time with count, etc.

# Dump Data

If statistics does not satisfy you. You may choose to dump data by *pressing **1** and enter*.

![dump](https://raw.githubusercontent.com/tommy-iasia/TcpBridgeDumper-Java6/master/preview/dumping.png)

Four files will be created for **read server, write client, read client, write server**.

The files are in binary format. And a file contains packages of the following format.

| Time | Payload Length | Payload Data |
| ---- | ---- | ---- |
| Long/millisecond | Int/number of bytes | (according to payload length) |

When you want to stop dumping data, *press **0** and enter*.
