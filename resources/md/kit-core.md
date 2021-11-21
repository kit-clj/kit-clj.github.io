## Integrant components

### :system/env

Component that returns the configuration option passed in when resolved. Useful for passing in environment to share across components. For instance, here is what the kit template produces in `system.edn`

```clojure
:system/env
 #profile {:dev  :dev
           :test :test
           :prod :prod}
```

## Utilities

### kit.config/read-config

Reads a resource using the `aero` edn reader with optional options passed in.

Usage:

```clojure
(config/read-config "system.edn" {:opts {:profile :test}})
```

### kit.ig-utils/resume-handler

Utility to not reset an integrant component during development on hot reload. Checks to see if new configuration options are different from previous ones, and if they are the same, do nothing but return the old implementation.

Usage:

```clojure
(defmethod ig/resume-key :db.sql/connection
  [key opts old-opts old-impl]
  (ig-utils/resume-handler key opts old-opts old-impl))
```