## Requests and Responses

### Overview

The base reitit ring handler uses the [environmental middleware](environment.md) and the `ring.middleware.defaults` default middleware as configured in your `system.edn`.

On top of this, the default generated `api` routes in Kit use the following middleware configuration. They are found in your `<<project-ns>>.web.routes.api`

```clojure
[;; query-params & form-params
parameters/parameters-middleware
;; content-negotiation
muuntaja/format-negotiate-middleware
;; encoding response body
muuntaja/format-response-middleware
;; exception handling
coercion/coerce-exceptions-middleware
;; decoding request body
muuntaja/format-request-middleware
;; coercing response bodys
coercion/coerce-response-middleware
;; coercing request parameters
coercion/coerce-request-middleware
;; exception handling
exception/wrap-exception]
```

This configuration handles request and response parameter coercion, exception handling.

### Requests

By default the request parameters, such as those from a form POST, will be automatically parsed
and set as a `:params` key on the request.

The request parameters will be available under the `:params` key
of the request. Note that the middleware will also handle encoding the response bodies when you set the appropriate MIME
type on the response.

### Responses

Ring responses are generated using the [ring-http-response](https://github.com/metosin/ring-http-response) library.
The library provides a number of helpers for producing responses with their respective HTTP Status codes.

For example, the `ring.util.http-response/ok` helper is used to generate a response with the status `200`. The following code will produce a valid response map with the content set as its `:body` key.

```clojure
(ok {:foo "bar"})

;;result of calling response
{:status  200
 :headers {}
 :body    {:foo "bar"}}
```

The response body can be one of a string, a sequence, a file, or an input stream. The body must correspond appropriately with the response's status code.

A string, it will be sent back to the client as is. For a sequence, a string representing each element is sent to the client. Finally, if the response is a file or an input stream, then the server sends its contents to the client.

### Response encoding

By default, the [muuntaja](https://github.com/metosin/muuntaja) middleware library is used to infer the response type when a route returns a map containing the `:body` key:

```clojure
{:body {:foo "bar"}}
```

The middleware is found in the `<project-ns>.web.middleware.formats` namespace of your application:

```clojure
(ns <project-ns>.web.middleware.formats
  (:require
    [luminus-transit.time :as time]
    [muuntaja.core :as m]))

(def instance
  (m/create
    (-> m/default-options
        (update-in
          [:formats "application/transit+json" :decoder-opts]
          (partial merge time/time-deserialization-handlers))
        (update-in
          [:formats "application/transit+json" :encoder-opts]
          (partial merge time/time-serialization-handlers)))))

```

Muuntaja will use the `Content-Type` header to infer the content of the request, and the
`Accept` header to infer the response format.

By default we can see we already have some time serialization and deserialization handlers provided by [luminus-transit.time](https://github.com/luminus-framework/luminus-transit) to help us with reading and writing Java time objects.

### Setting headers

Setting additional response headers is done by calling `ring.util.http-response/header`, and
passing it a map of HTTP headers. Note that the keys **must** be strings.

```clojure
(-> "hello world" response (header "x-csrf" "csrf"))
```

### Setting content type

You can set a custom response type by using the `ring.util.http-response/content-type` function, eg:

```clojure
(defn project-handler [req]
  (-> (clojure.java.io/input-stream "report.pdf")
      (ok)
      (content-type "application/pdf")))
```

### Setting custom status

Setting a custom status is accomplished by passing the content to the `ring.util.http-response/status` function:

```clojure
(defn missing-page [req]
  (-> "your page could not be found"
      (ok)
      (status 404)))
```

### Redirects

Redirects are handled by the `ring.util.http-response/found` function. The function will set a `302` redirect status on the response.

```clojure
(defn old-location []
  (found "/new-location"))
```

Please refer to the [ring-http-response](https://github.com/metosin/ring-http-response/blob/master/src/ring/util/http_response.clj) to see other available helpers.


