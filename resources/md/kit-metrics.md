## Integrant components

### :metrics/prometheus

The component initializes an
in-memory [prometheus](https://prometheus.io/)
registry per instance. The following are the default
options:

```clojure
:metrics/prometheus
{:jvm?        true
 :fn?         true
 :ring?       true
 :definitions []}
```

You can define specific functions to be benchmarked in the
definitions key. Here is a sample definition:

```clojure
{:type   :histogram
 :metric :app/duration-seconds
 :opts   {}}
```

The types available are

- `:histogram`
- `:gauge`
- `:counter`
- `:summary`

See [iapetos](https://github.com/clj-commons/iapetos)
documentation for more information,

## Implementing Ring Metrics

In addition to initializing the registry in the
configuration, for your ring HTTP requests to be measured
you will need to wrap in the iapetos ring middleware along
with your configuration.

For example:

`system.edn`

```clojure
:handler/ring
{
 ;; ... other stuff
 :metrics #ig/ref :metrics/prometheus}
```

Wherever your middleware is chained at the top level

```clojure
(require '[iapetos.collector.ring :as prometheus-ring])

;; ... middleware wrapping
(prometheus-ring/wrap-metrics metrics)
```