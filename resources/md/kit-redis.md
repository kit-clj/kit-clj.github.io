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

Now, in order to be able to use component-based connection in our routes we need to refer it in `:reitit.routes/pages` configuration key.

```clojure
:reitit.routes/pages {:base-path "",
                      :redis #ig/ref :cache/redis
                      :env #ig/ref :system/env}
```

Now, let's create some controller that would use a redis from opts and returns all items available in the DB:

```clojure
(ns <<your-app-ns>>.web.controllers.todo
  (:require [ring.util.http-response :refer [content-type ok]]
            [taoensso.carmine :refer [wcar] :as car]
            [<<your-app-ns>>.web.controllers.components :as c]
            [<<your-app-ns>>.web.pages.layout :as layout]))

(defn home [opts request]
  (let [redis (:cache/redis opts)
        items (wcar redis (car/keys "*"))]
    (layout/render request "home.html" {:items items})))
```

Make sure that you pass the `opts` parameter to your controller. Normally it can be done by the partial application:

```clojure
(ns <<your-app-ns>>.web.routes.pages
  (:require
   [<<your-ns>>.web.controllers.todo :as todo]
   # ...
   ))

(defn page-routes [opts]
  [["/" {:get (partial todo/home opts)}]])
```

Go to `user.clj` namespace and put some records to Redis:

```
(comment
  (require '[taoensso.carmine :refer [wcar] :as car])
  (def rs (:cache/redis state/system))
  (wcar rs (car/set "foo" "bar"))
  (wcar rs (car/set "baz" "zoo"))
  )
```

Last thing that you need to do is to simply change `home.html` to iterate over items and print them in a list:

```html
<div class="content container">
  <div class="columns">
    <div class="column">
      <h3>Items</h3>
      <ul class="items">
        {% for item in items %}
        <li>{{ item }}</li>
        {% endfor %}
      </ul>
    </div>
  </div>
</div>
```

Head over to http://localhost:3000/ to see the results. For examplary web application, please reach out to [todolist created with redis and htmx](https://github.com/kit-clj/kit-examples/tree/master/todolist-with-redis-and-htmx) where you can see how redis usage can be extended to serve as an in-memory database for small todo-list application enriched with HTMX interactions.
