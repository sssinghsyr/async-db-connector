# async-db-connector
Database connector handling multiple clients query request and routing to the different databases asynchronously.


Goals:
1. Fewer resources: No need of threads for each connection.
2. Less overhead: Less context switching.

Single-threaded approach:
1. event-based, non-blocking paradigm
2. avoiding context switches, locks, and blocking

TODO:
1. Exception handling, sending exception back to the client
