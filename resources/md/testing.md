### Clojure

Kit sets up a default test harness found in the `test` directory of the project. It comes supplied with a `test-utils` namespace that has some handy utilities for testing

```clojure
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

#### Running The Tests

Kit uses [Kaocha](https://github.com/lambdaisland/kaocha) as test
runner.

The behavior of your test runner is configured through different
means:

- `tests.edn` config file
- [CLI arguments](https://cljdoc.org/d/lambdaisland/kaocha/1.91.1392/doc/4-running-kaocha-cli) to Kaocha.
  For example passing arguments to `bin/kaocha` script which is a wrapper around Kaocha.
  [(more about the `/bin/kaocha` script)](https://cljdoc.org/d/lambdaisland/kaocha/1.91.1392/doc/2-installing#2-installing)

Read [Kaocha configuration](https://cljdoc.org/d/lambdaisland/kaocha/1.91.1392/doc/3-configuration)
to better understand Kaocha and it's concepts like test suites.

Ways to run test in Kit:

```sh
# Running with bb
bb test --reporter documentation    # Pass CLI args for kaocha
bb test-watch   # re-run the root tests on file changes

# Running with make
make test
make test-watch

# Running with clj
clj -X:test
# ...
```

To know what are the current options that Koacha is using, run
`bin/kaocha --print-config`
