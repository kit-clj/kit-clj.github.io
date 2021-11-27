## Integrant components

### :cache/redis

This component connects to a Redis cache cluster using [carmine](https://github.com/ptaoussanis/carmine) and provides a [clojure.core.cache](https://github.com/clojure/core.cache) interface over it.

Sample configuration:

```clojure
{:ttl  3600
 :conn {:pool {}
        :spec #profile {:dev  {:host "localhost" :port 6379}
                        :test {:host "localhost" :port 6379}
                        :prod {:uri #env REDIS_URI}}}}
```
