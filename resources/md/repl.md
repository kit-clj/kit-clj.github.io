The typical workflow for developing with Kit is to start up a local REPL and initialize your application from within there. However, if you prefer to alternatively start up the application as a standalone, Kit comes with a socket REPL by default for development that you can connect to.

## Connecting to the REPL

Kit provides an embedded socket REPL that can be used to connect
the editor to a running instance of the server. A default port is set the `system.edn` configuration as `7000`

When you run your application using it will create a network REPL on the port `7000` and you will be
able to connect your editor to it on `localhost:7000`.  This port can also be set using the `REPL_PORT` environment variable.

Note that by default this REPL is available in production and can be used to inspect the application the same way you would in development. You can disable it if desired by using different environment flags in your `system.edn`
