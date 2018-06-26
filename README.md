# async-db-connector
Database connector handling multiple clients query request and routing to the different databases asynchronously.


Goals:
1. Fewer resources: No need of threads for each connection.
2. Less overhead: Less context switching.

Single-threaded approach:
1. event-based, non-blocking paradigm
2. avoiding context switches, locks, and blocking


Changes made:
1. Once submission is completed, query response is sent to the client by selecting thread from the pool.
2. Using threads from the threadpool minimizes the overhead due to thread creation. Thread objects use a significant amount of    memory, and    in a large-scale application, allocating and deallocating many thread objects creates a significant memory     management   overhead.
3. Main-thread task is just to listen query from the client and call async-db-access API.

TODO:
1. Exception handling, sending exception back to the client
