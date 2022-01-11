## Integrant components

### :nrepl/server

Implements an [nREPL](https://github.com/nrepl/nrepl) server.

Full configuration options look like:

```clojure
:nrepl/server
{:bind "127.0.0.1"
 :port 7000
 :ack  8000}
```