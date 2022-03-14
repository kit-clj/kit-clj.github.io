## Sessions

Kit defaults to using cookie-based sessions.

The session middleware is configured in the `:cookie-session-config` of the `:handler/ring` component. Session timeout is specified in seconds and defaults to 24 hours (i.e. 86400 seconds) of inactivity. Here is the default configuration. The business logic is provided by `ring.middleware.session.cookie/cookie-store`.

```clojure
{:cookie-secret #or [#env COOKIE_SECRET "16charsecrethere"]
 :cookie-name "<project-ns>"
 :cookie-default-max-age 86400}
```

### Accessing the session

Ring tracks sessions using the request map and the current session will be found under the `:session` key.
Below we have a simple example of interaction with the session.

```clojure
(ns myapp.home
  (:require
   [ring.util.response :refer [response]]))

(defn set-user! [id {session :session}]
  (-> (response (str "User set to: " id))
      (assoc :session (assoc session :user id))
      (assoc :headers {"Content-Type" "text/plain"})))

(defn remove-user! [{session :session}]
  (-> (response "User removed")
      (assoc :session (dissoc session :user))
      (assoc :headers {"Content-Type" "text/plain"})))

(defn clear-session! []
  (-> (response "Session cleared")
      (dissoc :session)
      (assoc :headers {"Content-Type" "text/plain"})))

(def app-routes
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/login/:id" {:get (fn [{:keys [path-params] :as req}]
                         (set-user! (:id path-params) req))}]
   ["/remove" {:get remove-user!}]
   ["/logout" {:get clear-session!]])
```

### Flash sessions

Flash sessions have a lifespan of a single request, these can be accessed using the `:flash` key instead of the `:session` key used for regular sessions.

## Cookies

Cookies are found under the `:cookies` key of the request, eg:

```clojure
{:cookies {"username" {:value "Bob"}}}

```

Conversely, to set a cookie on the response we simply update the response map with the desired cookie value:

```clojure
(-> "response with a cookie" response (assoc-in [:cookies "username" :value] "Alice"))
```

Cookies can contain the following additional attributes in addition to the `:value` key:

* :domain - restrict the cookie to a specific domain
* :path - restrict the cookie to a specific path
* :secure - restrict the cookie to HTTPS URLs if true
* :http-only - restrict the cookie to HTTP if true (not accessible via e.g. JavaScript)
* :max-age - the number of seconds until the cookie expires
* :expires - a specific date and time the cookie expires

### Cookie encoding

Java objects such as dates must be explicitly encoded when stored in cookie sessions.
The following example illustrates how to use [tick](https://github.com/juxt/tick) library
to add a reader for a zoned date-time:

```clojure
(defn wrap-base
  [{:keys [cookie-opts]}]
  (let [{:keys [cookie-secret cookie-name cookie-default-max-age]} cookie-opts
        cookie-store (session.cookie/cookie-store {:key     (.getBytes ^String cookie-secret)
                                                   :readers {'inst                 (fn [x]
                                                                                     (some-> x (tick/parse) (tick/inst)))
                                                             'time/zoned-date-time #'tick/zoned-date-time}})]
    (fn [handler]
      (cond-> handler
              true (session/wrap-session {:store        cookie-store
                                          :cookie-name  cookie-name
                                          :cookie-attrs {:max-age cookie-default-max-age}})
              true (cookies/wrap-cookies)))))
```
