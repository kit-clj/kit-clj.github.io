Out of the box Kit supports the [Undertow](https://undertow.io/) web server via the [ring-undertow-adapter](https://github.com/luminus-framework/ring-undertow-adapter)

### Undertow Configuration

Undertow allows setting the number of worker and IO threads using the `:worker-threads` and the `:io-threads` keys respectively. For example, we could update the default configuration in the `system.edn` as follows:

```clojure
:server/undertow
{:port #long #or [#env PORT 3000]
 :handler #ig/ref :handler/ring
 :worker-threads 200
 :io-threads 4}
```

If you want to do a custom configuration that includes calculating the `io-threads` at runtime, you can override the default `ig/init-key` for `server/undertow`. This is the definition in `kit.edge.server.undertow`

```clojure
(defmethod ig/prep-key :server/undertow
  [_ config]
  (merge {:port 3000
          :host "0.0.0.0"}
         config))
```

For example,

```clojure
(defmethod ig/prep-key :server/undertow
  [_ config]
  (merge {:port 3000
          :host "0.0.0.0"
          :io-threads (* 2 (.availableProcessors (Runtime/getRuntime)))}
         config))
```

For a full listing of all configuration options, review the [ring-undertow-adapter documentation](https://github.com/luminus-framework/ring-undertow-adapter).

Undertow uses separate thread pools for managing the IO and the worker threads.
The `:dispatch?` flag is used to decide whether the request should be dispatched by the IO thread to a separate worker thread.
Since dispatching the request to a worker carries overhead, it may be more performant to handle some requests, such as hardcoded text responses, directly in the IO thread.
