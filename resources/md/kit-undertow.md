## Integrant components

### :server/undertow

Configures and starts an undertow server, defaults to port `3000` on host `"0.0.0.0"`

Sample configuration, does not include all options. Full options for the ring undertow adapter can be found [here](https://github.com/luminus-framework/ring-undertow-adapter)

```clojure
 :server/undertow
 {:host            "0.0.0.0"
  :port            #long #or [#env PORT 3000]
  :handler         #ig/ref :handler/ring
  :http2?          false
  :dispatch?       true
  :websocket?      false
  :async?          false
  :session-manager false}
```
