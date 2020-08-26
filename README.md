# Route Events Directly to a Parallel AsyncEventQueue
## Description

This project provides two functions that route events directly to a parallel AsyncEventQueue without storing them in the partitioned Region.

The **PrimaryRoutingFunction**:

- Retrieves the routing Region
- Creates an EntryEventImpl with the Region, key, value and EventID as parameters
- Uses the key's BucketRegion to set the EntryEventImpl's tail key (which is the key in the AsyncEventQueue queue Region)
- Gets the redundant DistributedMembers for the key
- Invokes the SecondaryRoutingFunction on those DistributedMembers with the Region name, key, value, EventID and tail key as arguments
- Gets the (primary) AsyncEventQueue for the routing Region
- Distributes the EntryEventImpl to the AsyncEventQueue

The **SecondaryRoutingFunction**:

- Retrieves the routing Region
- Creates an EntryEventImpl with the Region, key, value, EventID and tail key as parameters
- Gets the (secondary) AsyncEventQueue for the routing Region
- Distributes the EntryEventImpl to the AsyncEventQueue

The **RoutingAsyncEventListener** is an AsyncEventListener that processes AsyncEvents by logging and counting them.
## Initialization
Modify the **GEODE** environment variable in the *setenv.sh* script to point to a Geode installation directory.
## Build
Build the Spring Boot Client Application and Geode Server Function classes using gradle like:

```
./gradlew clean jar bootJar
```
## Run Example
### Start and Configure Locator and Servers
Start and configure the locator and 3 servers using the *startandconfigure.sh* script like:

```
./startandconfigure.sh
```
### Route Entries
Run the client to route N Trade instances using the *runclient.sh* script like below.

The parameters are:

- operation (route)
- number of entries (10)

```
./runclient.sh route 10
```
### Shutdown Locator and Servers
Execute the *shutdownall.sh* script to shutdown the servers and locators like:

```
./shutdownall.sh
```
### Remove Locator and Server Files
Execute the *cleanupfiles.sh* script to remove the server and locator files like:

```
./cleanupfiles.sh
```
## Example Sample Output
### Start and Configure Locator and Servers
Sample output from the *startandconfigure.sh* script is:

```
./startandconfigure.sh 
1. Executing - start locator --name=locator

.......................
Locator in <working-directory>/locator on xxx.xxx.x.xx[10334] as locator is currently online.
Process ID: 9858
Uptime: 24 seconds
Geode Version: 1.12.0
Java Version: 1.8.0_151
Log File: <working-directory>/locator/locator.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host=xxx.xxx.x.xx, port=1099]

Cluster configuration service is up and running.

2. Executing - set variable --name=APP_RESULT_VIEWER --value=any

Value for variable APP_RESULT_VIEWER is now: any.

3. Executing - configure pdx --read-serialized=true

read-serialized = true
ignore-unread-fields = false
persistent = false
Cluster configuration for group 'cluster' is updated.

4. Executing - start server --name=server-1 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

..............
Server in <working-directory>/server-1 on xxx.xxx.x.xx[51940] as server-1 is currently online.
Process ID: 9903
Uptime: 11 seconds
Geode Version: 1.12.0
Java Version: 1.8.0_151
Log File: <working-directory>/server-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

5. Executing - start server --name=server-2 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

...............
Server in <working-directory>/server-2 on xxx.xxx.x.xx[51965] as server-2 is currently online.
Process ID: 9916
Uptime: 11 seconds
Geode Version: 1.12.0
Java Version: 1.8.0_151
Log File: <working-directory>/server-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

6. Executing - start server --name=server-3 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

.................
Server in <working-directory>/server-3 on xxx.xxx.x.xx[51997] as server-3 is currently online.
Process ID: 9929
Uptime: 12 seconds
Geode Version: 1.12.0
Java Version: 1.8.0_151
Log File: <working-directory>/server-3/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

7. Executing - list members

Member Count : 4

  Name   | Id
-------- | --------------------------------------------------------------
locator  | xxx.xxx.x.xx(locator:9858:locator)<ec><v0>:41000 [Coordinator]
server-1 | xxx.xxx.x.xx(server-1:9903)<v1>:41001
server-2 | xxx.xxx.x.xx(server-2:9916)<v2>:41002
server-3 | xxx.xxx.x.xx(server-3:9929)<v3>:41003

8. Executing - deploy --jar=server/build/libs/server-0.0.1-SNAPSHOT.jar

 Member  |       Deployed JAR        | Deployed JAR Location
-------- | ------------------------- | ---------------------------------------------------------
server-1 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-1/server-0.0.1-SNAPSHOT.v1.jar
server-2 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-2/server-0.0.1-SNAPSHOT.v1.jar
server-3 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-3/server-0.0.1-SNAPSHOT.v1.jar

9. Executing - list functions

 Member  | Function
-------- | ------------------------
server-1 | AssignBucketsFunction
server-1 | PrimaryRoutingFunction
server-1 | SecondaryRoutingFunction
server-2 | AssignBucketsFunction
server-2 | PrimaryRoutingFunction
server-2 | SecondaryRoutingFunction
server-3 | AssignBucketsFunction
server-3 | PrimaryRoutingFunction
server-3 | SecondaryRoutingFunction

10. Executing - create async-event-queue --id=routing_aeq --batch-size=100 --batch-time-interval=100 --dispatcher-threads=1 --parallel=true --listener=example.server.aeq.RoutingAsyncEventListener

 Member  | Status | Message
-------- | ------ | -------
server-1 | OK     | Success
server-2 | OK     | Success
server-3 | OK     | Success

Cluster configuration for group 'cluster' is updated.

11. Executing - list async-event-queues

 Member  |     ID      | Batch Size | Persistent | Disk Store | Max Memory |                   Listener                   | Created with paused event processing | Currently Paused
-------- | ----------- | ---------- | ---------- | ---------- | ---------- | -------------------------------------------- | ------------------------------------ | ----------------
server-1 | routing_aeq | 100        | false      | null       | 100        | example.server.aeq.RoutingAsyncEventListener | false                                | false
server-2 | routing_aeq | 100        | false      | null       | 100        | example.server.aeq.RoutingAsyncEventListener | false                                | false
server-3 | routing_aeq | 100        | false      | null       | 100        | example.server.aeq.RoutingAsyncEventListener | false                                | false

12. Executing - sleep --time=5


13. Executing - create region --name=Router --type=PARTITION_REDUNDANT --async-event-queue-id=routing_aeq

 Member  | Status | Message
-------- | ------ | --------------------------------------
server-1 | OK     | Region "/Router" created on "server-1"
server-2 | OK     | Region "/Router" created on "server-2"
server-3 | OK     | Region "/Router" created on "server-3"

Cluster configuration for group 'cluster' is updated.

14. Executing - execute function --id=AssignBucketsFunction --member=server-1 --arguments=Router

 Member  | Status | Message
-------- | ------ | -------
server-1 | OK     | [true]

15. Executing - list regions

List of regions
---------------
Router

************************* Execution Summary ***********************
Script file: startandconfigure.gfsh

Command-1 : start locator --name=locator
Status    : PASSED

Command-2 : set variable --name=APP_RESULT_VIEWER --value=any
Status    : PASSED

Command-3 : configure pdx --read-serialized=true
Status    : PASSED

Command-4 : start server --name=server-1 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status    : PASSED

Command-5 : start server --name=server-2 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status    : PASSED

Command-6 : start server --name=server-3 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status    : PASSED

Command-7 : list members
Status    : PASSED

Command-8 : deploy --jar=server/build/libs/server-0.0.1-SNAPSHOT.jar
Status    : PASSED

Command-9 : list functions
Status    : PASSED

Command-10 : create async-event-queue --id=routing_aeq --batch-size=100 --batch-time-interval=100 --dispatcher-threads=1 --parallel=true --listener=example.server.aeq.RoutingAsyncEventListener
Status     : PASSED

Command-11 : list async-event-queues
Status     : PASSED

Command-12 : sleep --time=5
Status     : PASSED

Command-13 : create region --name=Router --type=PARTITION_REDUNDANT --async-event-queue-id=routing_aeq
Status     : PASSED

Command-14 : execute function --id=AssignBucketsFunction --member=server-1 --arguments=Router
Status     : PASSED

Command-15 : list regions
Status     : PASSED
```
### Route Entries
Sample output from the *runclient.sh* script is:

```
./runclient.sh route 10

2020-08-23 13:12:06.569  INFO 10173 --- [           main] example.client.Client                    : Starting Client on ...
...
2020-08-23 13:12:12.701  INFO 10173 --- [           main] example.client.Client                    : Started Client in 6.94 seconds (JVM running for 7.693)
2020-08-23 13:12:12.757  INFO 10173 --- [           main] org.apache.geode                         : 
The client metadata contains the following 3 servers with primary buckets for region /Router:
        BucketServerLocation{bucketId=0,host=192.168.1.10,port=51940,isPrimary=true,version=5}->[0, 64, 66, 5, 70, 7, 72, 10, 76, 13, 78, 15, 82, 19, 85, 22, 89, 26, 90, 28, 93, 32, 33, 98, 99, 37, 103, 41, 43, 107, 108, 46, 111, 48, 53, 55, 58, 60]
        BucketServerLocation{bucketId=1,host=192.168.1.10,port=51965,isPrimary=true,version=5}->[1, 65, 67, 4, 69, 8, 74, 11, 75, 14, 79, 16, 81, 18, 84, 21, 88, 25, 92, 29, 31, 95, 96, 34, 101, 38, 40, 104, 105, 42, 109, 47, 112, 50, 51, 56, 57, 61]
        BucketServerLocation{bucketId=2,host=192.168.1.10,port=51997,isPrimary=true,version=5}->[2, 3, 68, 6, 71, 9, 73, 12, 77, 80, 17, 83, 20, 86, 23, 87, 24, 27, 91, 30, 94, 97, 35, 36, 100, 102, 39, 106, 44, 45, 110, 49, 52, 54, 59, 62, 63]
2020-08-23 13:12:12.757  INFO 10173 --- [           main] example.client.service.TradeService      : Routing 10 entries
2020-08-23 13:12:13.433  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=0, cusip=AVGO, shares=14, price=359.47); eventId=EventID[id=31 bytes;threadID=1;sequenceID=0]; result=[true]
2020-08-23 13:12:13.621  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=1, cusip=NKE, shares=11, price=591.88); eventId=EventID[id=31 bytes;threadID=1;sequenceID=1]; result=[true]
2020-08-23 13:12:13.761  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=2, cusip=LLY, shares=25, price=458.68); eventId=EventID[id=31 bytes;threadID=1;sequenceID=2]; result=[true]
2020-08-23 13:12:13.765  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=3, cusip=IBM, shares=46, price=276.90); eventId=EventID[id=31 bytes;threadID=1;sequenceID=3]; result=[true]
2020-08-23 13:12:13.770  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=4, cusip=PFE, shares=75, price=378.49); eventId=EventID[id=31 bytes;threadID=1;sequenceID=4]; result=[true]
2020-08-23 13:12:13.775  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=5, cusip=ADBE, shares=6, price=535.88); eventId=EventID[id=31 bytes;threadID=1;sequenceID=5]; result=[true]
2020-08-23 13:12:13.780  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=6, cusip=ORCL, shares=37, price=514.02); eventId=EventID[id=31 bytes;threadID=1;sequenceID=6]; result=[true]
2020-08-23 13:12:13.788  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=7, cusip=SBUX, shares=22, price=848.16); eventId=EventID[id=31 bytes;threadID=1;sequenceID=7]; result=[true]
2020-08-23 13:12:13.795  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=8, cusip=IBM, shares=53, price=783.03); eventId=EventID[id=31 bytes;threadID=1;sequenceID=8]; result=[true]
2020-08-23 13:12:13.804  INFO 10173 --- [           main] example.client.service.TradeService      : Routed routable=Trade(id=9, cusip=PFE, shares=59, price=557.68); eventId=EventID[id=31 bytes;threadID=1;sequenceID=9]; result=[true]
2020-08-23 13:12:13.804  INFO 10173 --- [           main] example.client.service.TradeService      : Routed 10 entries in 1047 ms
```
### Server Output
#### Normal Usage
The **PrimaryRoutingFunction** and **RoutingAsyncEventListener** in the primary server for each event will log messages like:

```
[info 2020/08/23 13:12:13.750 HST <ServerConnection on port 51965 Thread 1> tid=0x5b] PrimaryRoutingFunction processing args= [PDX[14273398,example.client.domain.Trade]{id=2}, EventID[id=31 bytes;threadID=1;sequenceID=2]]

[info 2020/08/23 13:12:13.762 HST <ServerConnection on port 51965 Thread 1> tid=0x5b] PrimaryRoutingFunction processing args= [PDX[14273398,example.client.domain.Trade]{id=3}, EventID[id=31 bytes;threadID=1;sequenceID=3]]

[info 2020/08/23 13:12:13.789 HST <ServerConnection on port 51965 Thread 1> tid=0x5b] PrimaryRoutingFunction processing args= [PDX[14273398,example.client.domain.Trade]{id=8}, EventID[id=31 bytes;threadID=1;sequenceID=8]]

[info 2020/08/23 13:12:13.797 HST <ServerConnection on port 51965 Thread 1> tid=0x5b] PrimaryRoutingFunction processing args= [PDX[14273398,example.client.domain.Trade]{id=9}, EventID[id=31 bytes;threadID=1;sequenceID=9]]

[info 2020/08/23 13:12:14.412 HST <Event Processor for GatewaySender_AsyncEventQueue_routing_aeq_0> tid=0x47] RoutingAsyncEventListener: Processing 4 events (total=4; possibleDuplicate=0)
	key=2; value=PDX[14273398,example.client.domain.Trade]{id=2}; possibleDuplicate=false
	key=3; value=PDX[14273398,example.client.domain.Trade]{id=3}; possibleDuplicate=false
	key=8; value=PDX[14273398,example.client.domain.Trade]{id=8}; possibleDuplicate=false
	key=9; value=PDX[14273398,example.client.domain.Trade]{id=9}; possibleDuplicate=false
```
The **SecondaryRoutingFunction** in the secondary servers will log messages like:

```
[info 2020/08/23 13:12:13.756 HST <Function Execution Processor2> tid=0x3d] SecondaryRoutingFunction processing args= [/Router, 2, PDX[14273398,example.client.domain.Trade]{id=2}, EventID[id=31 bytes;threadID=1;sequenceID=2], 163]

[info 2020/08/23 13:12:13.763 HST <Function Execution Processor2> tid=0x3d] SecondaryRoutingFunction processing args= [/Router, 3, PDX[14273398,example.client.domain.Trade]{id=3}, EventID[id=31 bytes;threadID=1;sequenceID=3], 164]

[info 2020/08/23 13:12:13.790 HST <Function Execution Processor2> tid=0x3d] SecondaryRoutingFunction processing args= [/Router, 8, PDX[14273398,example.client.domain.Trade]{id=8}, EventID[id=31 bytes;threadID=1;sequenceID=8], 169]

[info 2020/08/23 13:12:13.799 HST <Function Execution Processor2> tid=0x39] SecondaryRoutingFunction processing args= [/Router, 9, PDX[14273398,example.client.domain.Trade]{id=9}, EventID[id=31 bytes;threadID=1;sequenceID=9], 170]
```
#### Killed Server
If a server is killed while routing events, the server logs will contain messages like below.

In this case, the last log messages from the **PrimaryRoutingFunction** in the killed server were:

```
[info 2020/08/23 14:17:05.681 HST <ServerConnection on port 56033 Thread 1> tid=0x5a] PrimaryRoutingFunction processing args= [PDX[8290614,example.client.domain.Trade]{id=756}, EventID[id=31 bytes;threadID=1;sequenceID=756]]

[info 2020/08/23 14:17:05.691 HST <ServerConnection on port 56033 Thread 1> tid=0x5a] PrimaryRoutingFunction processing args= [PDX[8290614,example.client.domain.Trade]{id=758}, EventID[id=31 bytes;threadID=1;sequenceID=758]]

[info 2020/08/23 14:17:05.709 HST <ServerConnection on port 56033 Thread 1> tid=0x5a] PrimaryRoutingFunction processing args= [PDX[8290614,example.client.domain.Trade]{id=764}, EventID[id=31 bytes;threadID=1;sequenceID=764]]

[info 2020/08/23 14:17:05.717 HST <ServerConnection on port 56033 Thread 1> tid=0x5a] PrimaryRoutingFunction processing args= [PDX[8290614,example.client.domain.Trade]{id=766}, EventID[id=31 bytes;threadID=1;sequenceID=766]]

[info 2020/08/23 14:17:05.742 HST <ServerConnection on port 56033 Thread 1> tid=0x5a] PrimaryRoutingFunction processing args= [PDX[8290614,example.client.domain.Trade]{id=769}, EventID[id=31 bytes;threadID=1;sequenceID=769]]

[info 2020/08/23 14:17:05.757 HST <ServerConnection on port 56033 Thread 1> tid=0x5a] PrimaryRoutingFunction processing args= [PDX[8290614,example.client.domain.Trade]{id=772}, EventID[id=31 bytes;threadID=1;sequenceID=772]]
```
This server was killed before its AsyncEventQueue processed those events.

One of the remaining servers became primary for three of those events and processed them as possible duplicates:

```
[info 2020/08/23 14:17:05.682 HST <Function Execution Processor2> tid=0x3a] SecondaryRoutingFunction processing args= [/Router, 756, PDX[8290614,example.client.domain.Trade]{id=756}, EventID[id=31 bytes;threadID=1;sequenceID=756], 990]

[info 2020/08/23 14:17:05.692 HST <Function Execution Processor2> tid=0x3a] SecondaryRoutingFunction processing args= [/Router, 758, PDX[8290614,example.client.domain.Trade]{id=758}, EventID[id=31 bytes;threadID=1;sequenceID=758], 766]

[info 2020/08/23 14:17:05.718 HST <Function Execution Processor2> tid=0x3a] SecondaryRoutingFunction processing args= [/Router, 766, PDX[8290614,example.client.domain.Trade]{id=766}, EventID[id=31 bytes;threadID=1;sequenceID=766], 908]

[info 2020/08/23 14:17:11.505 HST <Event Processor for GatewaySender_AsyncEventQueue_routing_aeq_0> tid=0x45] RoutingAsyncEventListener: Processing 5 events (total=266; possibleDuplicate=3)
	key=758; value=PDX[8290614,example.client.domain.Trade]{id=758}; possibleDuplicate=true
	key=766; value=PDX[8290614,example.client.domain.Trade]{id=766}; possibleDuplicate=true
	key=756; value=PDX[8290614,example.client.domain.Trade]{id=756}; possibleDuplicate=true
```
The other remaining server became primary for the other three events and processed them as possible duplicates:

```
[info 2020/08/23 14:17:05.710 HST <Function Execution Processor2> tid=0x37] SecondaryRoutingFunction processing args= [/Router, 764, PDX[8290614,example.client.domain.Trade]{id=764}, EventID[id=31 bytes;threadID=1;sequenceID=764], 793]

[info 2020/08/23 14:17:05.744 HST <Function Execution Processor2> tid=0x37] SecondaryRoutingFunction processing args= [/Router, 769, PDX[8290614,example.client.domain.Trade]{id=769}, EventID[id=31 bytes;threadID=1;sequenceID=769], 572]

[info 2020/08/23 14:17:05.758 HST <Function Execution Processor2> tid=0x37] SecondaryRoutingFunction processing args= [/Router, 772, PDX[8290614,example.client.domain.Trade]{id=772}, EventID[id=31 bytes;threadID=1;sequenceID=772], 822]

[info 2020/08/23 14:17:11.490 HST <Event Processor for GatewaySender_AsyncEventQueue_routing_aeq_0> tid=0x45] RoutingAsyncEventListener: Processing 4 events (total=266; possibleDuplicate=3)
	key=769; value=PDX[8290614,example.client.domain.Trade]{id=769}; possibleDuplicate=true
	key=764; value=PDX[8290614,example.client.domain.Trade]{id=764}; possibleDuplicate=true
	key=772; value=PDX[8290614,example.client.domain.Trade]{id=772}; possibleDuplicate=true
```
### Shutdown Locator and Servers
Sample output from the *shutdownall.sh* script is:

```
./shutdownall.sh 

(1) Executing - connect

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=192.168.1.11, port=1099] ..
Successfully connected to: [host=192.168.1.11, port=1099]


(2) Executing - shutdown --include-locators=true

Shutdown is triggered
```
