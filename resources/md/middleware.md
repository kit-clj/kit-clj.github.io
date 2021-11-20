## Adding custom middleware

Since Kit uses Ring and Reitit for routing the application handler
is a standard Ring handler and can be wrapped in middleware
just like you would in any other Ring based application.

Traditionally, you can define Ring middleware as functions, however Reitit also allows us to define middleware as data. All value

The middleware allows wrapping the handlers in functions which can modify the way the request is processed. 
Middleware functions are often used to extend the base functionality of Ring handlers to match the needs of 
the particular application.

A middleware is simply a function which accepts an existing handler with some optional parameters and returns a new handler with some added behaviour. An example of a middleware function would be:

```clojure
(defn wrap-nocache [handler]
  (fn [request]
     (let [response (handler request)]
        (assoc-in response [:headers  "Pragma"] "no-cache"))))
```

As you can see the wrapper accepts the handler and returns a function which in turn accepts the request. Since the returned function was defined in the scope where the handler exists, it can use it internally. When called, it will call the handler with the request and add Pragma: no-cache to the response map. For detailed information please refer to the official [Ring documentation](https://github.com/ring-clojure/ring/wiki).

This same code written as Reitit compatible data middleware is

```clojure
(defn nocache-handler
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers  "Pragma"] "no-cache"))))

(def wrap-nocache
  {:name        ::wrap-nocache
   :description "Calls the handler with the request and add Pragma: no-cache to the response map"
   :wrap        nocache-handler})
```

Any development middleware, such as middleware for showing stacktraces, should be added in the `wrap-dev` function found in the `<app>.dev-middleware` namespace.
This namespace resides in the `env/dev/clj` source path and will only be included during development mode.
 

```clojure
(defn wrap-dev [handler]
  (-> handler
      wrap-reload
      wrap-error-page
      wrap-exceptions))  
```    

Note that the order of the middleware matters as the request is modified by each middleware function. For example, any middleware functions that rely on the session must be placed before the `wrap-defaults` middleware that creates the session. The reason being that the request will pass through the outer middleware functions before reaching the inner ones.

For example, when we have the handler wrapped using `wrap-formats` and `wrap-defaults` as seen below:

```
(-> handler wrap-formats wrap-defaults)
```

The request is passed through these functions in the following order:

```
handler <- wrap-formats <- wrap-defaults <- request
```

On the other hand, any middleware that is set via the `:middleware` key in any Reitit routes is kept in the order as written, i.e.

```clojure
["/api" {:middleware [middleware-1 middleware-2]}]
```

executes the middleware in the order of 

```
middleware-1 -> middleware-2
```

This is much easier to reason about and typically you should set your middleware in the appropriate routes so that they do not apply on all routes of your applicaiton unless necessary.