You can work with Kit and REPL in multiple different ways.

## REPL during development

During development, you can:

- Start a local REPL in terminal using `clj -M:dev`. 
- Start a local instance of nREPL using `clj -M:dev:nrepl` and interact with it either from terminal or from your preferred editor. If you want to use nREPL with CIDER, run `clj -M:dev:cider` instead.

Alternatively, you can start your project as a standalone application and connect to it from an external editor as described below.

## Connecting to the REPL in production

If you want a REPL to run when your system is running, for example in production or standalone mode, you can use one of two libraries available for Kit: [kit-repl](/docs/kit-repl.html) or [kit-nrepl](/docs/kit-nrepl.html). You can include either of them in your project using an appropriate [profile](/docs/profiles.html): `+socket-repl` or `+full` for kit-repl, and `+nrepl` for kit-nrepl.

With these libraries in place, running the server using the `(go)` command will also run the REPL, allowing you to connect to it from your preferred editor. Note that by default, the REPL only listens for *local* connections on port 7200 (for socket REPL), and 7000 (for nREPL). You can change this in `system.edn` or by setting the `REPL_PORT` or `NREPL_PORT` environment variable.

