You can work with Kit and REPL in multiple different ways.

During development, you can:

- start a local REPL in terminal using `clj -M:dev` and initialize your application from there. After running the server using the `(go)` command, you will also be able to connect to a socket REPL from your preferred editor.
- start a local instance of nREPL using `clj -M:dev:nrepl` and interact with it either from terminal or from your preferred editor. If you want to use nREPL with CIDER, run `clj -M:dev:cider` instead.

Alternatively, you can start your project as a standalone application and connect to it from a third-party application as described below.

## Connecting to the REPL

Kit provides an embedded socket REPL that you can use to connect
the editor to a running instance of the server. A default port is set the `system.edn` configuration as `7000`

When you run your application, it will create a network REPL on the port `7000` and you will be
able to connect your editor to it on `localhost:7000`.  This port can also be set using the `REPL_PORT` environment variable.

Note that by default this REPL is available in production and can be used to inspect the application the same way you would in development. You can disable it if desired by using different environment flags in your `system.edn`.

If instead of the socket REPL you want to use nREPL, use the `+nrepl` profile when creating your project as described in [Application Profiles](https://kit-clj.github.io/docs/profiles.html).

