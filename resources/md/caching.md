The `kit-redis` library provides us with a nice interface over [clojure.core.cache](https://github.com/clojure/core.cache) when connecting to Redis. Many thanks to [crache](https://github.com/strongh/crache) for the inspiration

## Setup

Let's assume we have Redis setup and running on our local machine. We can configure our integrant component as follows

```clojure
:cache/redis
{:ttl  3600
 :conn {:pool {}
        :spec #profile {:dev  {:host "localhost" :port 6379}
                        :test {:host "localhost" :port 6379}
                        :prod {:uri #env REDIS_URI}}}}
```

## Basic Usage

A typical pattern with a cache is to lookup a key and when missed, execute a separate IO operation to find the value if it exists. If we get back a `nil` we will assume it doesn't exist and not add it to the cache. This may not be applicable in all cases! Sometimes you may wish to cache the nil. For our purposes here let's assume we do not. 

```clojure
(require '[clojure.core.cache :as cache])

(defn cache-lookup-or-add
  [cache key lookup-fn & [ttl]]
  (or (cache/lookup cache key)
      (let [value (lookup-fn)]
        (cache/miss cache key {:val value :ttl ttl})
        value)))
```

Now let's use our function. Say we have a scenario where we want to get users by ID, and cache any users we have found. If we cannot find the user, we will throw an exception with the `:type` `::no-user-found`.

Let's assume we have already written a database lookup function called `:get-user-by-id` that retrieves a single user entry given an ID. We will pass both the `query-fn` and our `cache/redis` (as `cache`) in our `ctx` as the first argument.

```clojure
(defn user-by-id
  [{:keys [query-fn cache] :as _ctx} id]
  (or (cache-lookup-or-add cache
                           ;; key to match in cache on
                           (str "users/" id)
                           ;; miss function to execute when not found in cache
                           #(query-fn :get-user-by-id {:id id}))
      (throw (ex-info "No user found" {:id   id
                                       :type ::no-user-found}))))
```