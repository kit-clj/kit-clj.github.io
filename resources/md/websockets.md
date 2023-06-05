## Websockets

Websocket support is provided by the web server adapters, and you will have to look at the documentation of the adapter you are using for configuration details. Kit defaults to using [ring-undertow-adapter](https://github.com/luminus-framework/ring-undertow-adapter/).
A websocket handler can be configured as follows using this adapter. The handler is created using a Ring handler function that returns a map
containing a `:undertow/websocket` with the configuration map:

* `:on-open` - fn taking a map with the key `:channel` (optional)
* `:on-message` - fn taking map of keys `:channel`, `:data` (optional)
* `:on-close-message` - fn taking map of keys `:channel`, `:message` (optional)
* `:on-error` - fn taking map of keys `:channel`, `:error` (optional)

The handler provides helper functions for doing websocket communication in the [ring.adapter.undertow.websocket](https://github.com/luminus-framework/ring-undertow-adapter/blob/master/src/ring/adapter/undertow/websocket.clj) namespace.
In particular there are `send-text`, `send-binary`, and `send` functions available. The last will try to infer the type of content automatically. An example websocket handler can be seen below:

```clojure
(require '[ring.adapter.undertow.websocket :as ws])

(fn [request]
  {:undertow/websocket 
   {:on-open (fn [{:keys [channel]}] (println "WS open!"))
    :on-message (fn [{:keys [channel data]}] (ws/send "message received" channel))
    :on-close-message (fn [{:keys [channel message]}] (println "WS closeed!"))}})
```

If headers are provided in the map returned from the handler function they are included in the
response to the WebSocket upgrade request. Handlers relevant to the WebSocket handshake (eg
Connection) will be overwritten so that the WebSocket handshake completes correctly:

```clojure
(defn- websocket-handler-with-headers [request]
  {:headers            {"X-Test-Header" "Hello!"}
   :undertow/websocket {}})
```
