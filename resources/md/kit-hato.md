## Integrant components

### :http.client/hato

Component that creates a [hato](https://github.com/gnarroway/hato) HTTP client for use throughout your application. 

For example, you may configure it as follows in your `system.edn`

```clojure
:http.client.hato/auth0
{:connect-timeout 3000}
```

And as follows in your Clojure code:

```clojure
(derive :http.client.hato/auth0 :http.client/hato)
```

Note the derivation allows you to have multiple clients should you want one per individual service

Now you can use this component throughout the application, e.g.


```clojure
;; system.edn
:reitit.routes/api
{:base-path "/api"
 :env #ig/ref :system/env
 :auth0-http-client :http.client.hato/auth0}

;; clojure (in default template your <<app-name>>.web.routes.api)
(defn auth0-call
  [req]
  (-> (hato/get "https://YOUR_DOMAIN/wsfed/YOUR_CLIENT_ID"
                {:http-client (utils/route-data-key req :auth0-http-client)})
      :body
      (http-response/ok)))

(defn api-routes [base-path]
  [base-path
   ["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title " API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health"
    {:get health/healthcheck!}]
   ;; New code here
   ["/auth0-call"
    {:post auth0-call}]])
```

`http.client/hato` is derived from `http/client`. This is useful should you want to extend behaviour for multiple HTTP clients and want to apply the same logic across all of them.