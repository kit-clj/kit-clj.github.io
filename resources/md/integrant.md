Kit encourages using the [Clean Architecture](https://blog.8thlight.com/uncle-bob/2012/08/13/the-clean-architecture.html) style for writing web applications.

The workflows in web applications are typically driven by the client requests. Since requests will often require interaction with a resource, such as a database, we will generally have to access that resource from the route handling the request. In order to isolate the stateful code, we should have our top level functions deal with managing the side effects.

Consider a route that facilitates user authentication. The client will supply the username and the password in the request. The route will have to pull the user credentials from the database and compare these to the ones supplied by the client. Then a decision is made whether the user logged in successfully or not, and its outcome communicated back to the client.

In this workflow, the code that deals with the external resources should be localized to the namespace that provides the route and the namespace that handles the database access.

The route handler function will be responsible for calling the function that fetches the credentials from the database. The code that determines whether the password and username match represents the core business logic. This code should be pure and accept the supplied credentials along with those found in the database explicitly. This structure can be seen in the diagram below.

```
            pure code
+----------+
| business |
|  logic   |
|          |
+-----+----+
      |
------|---------------------
      |     stateful code
+-----+----+   +-----------+
|  route   |   |           |
| handlers +---+  database |
|          |   |           |
+----------+   +-----------+
```

Keeping the business logic pure ensures that we can reason about it and test it without considering the external resources. Meanwhile, the code that deals with side effects is pushed to a thin outer layer, making it easy for us to manage.

### Integrant Overview

At the core of Kit is [Integrant](https://github.com/weavejester/integrant). It is used to manage component lifecycle In theory, each edge (element that performs input/output operations) of your library should be defined as an integrant component. If you are familiar with [component](https://github.com/stuartsierra/component) or [mount](https://github.com/tolitius/mount) the concepts introduced by integrant will sound similar.

In kit, your integrant components are defined in the `system.edn` file. This file is read in and parsed through [aero](https://github.com/juxt/aero) allowing for some additional reader macros. This configuration tells integrant the parameters to pass each component on initialization. Each key is a separate component and must have an initialize method defined in your code for the system to start properly.

The full lifecycle of an integrant component is:

1) `prep`
2) `init`
3) `suspend` (stop but retain state)
4) `resume`
5) `halt` (stop and discard state)

Each of these have associated multimethod functions in integrant, e.g. from the kit redis cache

```clojure
;; On initialize we create the cache with the initial configuration
(defmethod ig/init-key :cache/redis
  [_ config]
  (cache/seed (RedisCache. {}) config))

;; On suspend, nothing is done
(defmethod ig/suspend-key! :cache/redis [_ _])

;; On resume, we call a function that checks if the new options match the old options
;; and if so, does nothing, otherwise re-initializes the cache
(defmethod ig/resume-key :cache/redis
  [key opts old-opts old-impl]
  (ig-utils/resume-handler key opts old-opts old-impl))
```

For more detail, the [Integrant readme](https://github.com/weavejester/integrant) is well written with additional examples, and summaries.

### REPL workflow

For convenience, the generated `user.clj` file requires in a few helper functions from `integrant.repl`.

The most useful ones are

```clojure
(go) ;; used to start your application from an uninitialized state

(reset) ;; suspend, refresh your configuration, and resume. Useful after making changes and want to hot load them in

(halt) ;; stops the application
```

You also have access to the system state atom, `state/system`.

If you would like to run your tests from the REPL, a helper function is generated in `user.clj`

```clojure
(defn test-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (<project-ns>.config/system-config {:profile :test})
                                  (ig/prep)))))
```

This function uses the test profile regardless of your environment, allowing you to execute tests as if you were in that environment. This is particularly useful if you have a transient set of data sinks (databases, caches, etc.) for your test environment, and a permanent set for development.