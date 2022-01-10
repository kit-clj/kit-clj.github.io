Kit aims to facilitate developing [12 factor](http://12factor.net/) style applications.
The 12 factor approach states that the configuration should be kept separate from the code. The application
should not have to be packaged differently for each environment that it's deployed in.

Kit utilizes [aero](https://github.com/juxt/aero) to add reader macros for loading in environment variables in your `system.edn` configuration. For example, say we want to load in the environment variable `PORT`, and if it's not present default back to `3001`. We can do this thanks to aero in our EDN file like so

```clojure
#long #or [#env PORT 3001]
; ^    ^    ^-- tells aero to load the next value from the environment, defaulting to `nil` (falsey value in Clojure)
; |    |------- tells aero to return the first truthy value from the list of values that follow
; |------------ tells aero to parse the value as a long
```

### Default Environment Variables

Kit projects use the following environment variables by default:

* `PORT` - HTTP port that the application will attempt to bind to, defaults to `3000`
* `REPL_PORT` - when set the application will run the REPL socket server on the specified port, defaults to `7000`
* `REPL_HOST` - the URL for the database connection
* `COOKIE_SECRET` - the 16-character secret session cookies will be encrypted with, defaults to `16charsecrethere`. **IMPORTANT** for any production environment you should change this

### The Config Namespace

By default, we load all our system configuration in one file, `system.edn`, however your application may warrant loading multiple files, or even merging based off of tenant configurations. This can be extended in the `<project-ns>.config` namespace of your project. By default, it is quite simple, only loading in the configuration of that one file as the system-config that integrant will use.

```clojure
(ns <project-ns>.config
  (:require
    [kit.config :as config]))

(def ^:const system-filename "system.edn")

(defn system-config
  [options]
  (config/read-config system-filename options))
```

## Environment Specific Code

Some code, such as development middleware, is dependent on the mode the application
runs in.

Kit uses `env/dev/clj` and `env/prod/clj` source paths for this purpose. By default the source path will contain the
`<app>.env` namespace that has the environment specific configuration. The `dev` config looks as follows:

```clojure
(ns <project-ns>.env
  (:require
    [clojure.tools.logging :as log]
    [<project-ns>.dev-middleware :refer [wrap-dev]]
    ))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[ starting using the development or test profile]=-"))
   :started    (fn []
                 (log/info "\n-=[ started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[ has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})

```

The config references the `<app>.dev-middleware` namespace found in the same source path. Any development specific middleware
should be placed there.

Meanwhile, the `prod` config will not 
 
```clojure
(ns <project-ns>.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[ starting]=-"))
   :started    (fn []
                 (log/info "\n-=[ started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[ has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
```

Only the middleware defined in the `<app>.middleware` namespace is run during production.
