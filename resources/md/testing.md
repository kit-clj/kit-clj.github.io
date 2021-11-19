### Clojure

Kit sets up a default test harness found in the `test` directory of the project. It comes supplied with a `test-utils` namespace that has some handy utilities for testing

```
(ns <app-ns>.test-utils
  (:require
    [<app-ns>.core :as core]))

(defn system-state []
  (or @core/system state/system))

(defn system-fixture
  []
  (fn [f]
    (when (nil? (system-state))
      (core/start-app {:opts {:profile :test}}))
    (f)))

```

The `system-fixture` can be used as a fixture before any tests that require your system to be started. It ensures that the system boots with the test environment.

You can use `system-state` to get the current running state of your system. This is handy if you need to reference, access, or override components for individual tests.
