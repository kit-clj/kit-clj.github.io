## Kit Modules

Kit modules are templates that can be applied to an existing project using [kit-generator](https://github.com/kit-clj/kit/tree/master/libs/kit-generator) library. Modules are managed using git repositories, and official modules can be found [here](https://github.com/kit-clj/modules). Let's take a brief look at what a module repository looks like.

A module repository must contain a `modules.edn` file describing the modules that are provided. For example, here are the official modules provided by Kit:

```clojure
{:name "kit-modules"
 :modules
 {:kit/html
  {:path "html"
   :doc "adds support for HTML templating using Selmer"}
  :kit/sqlite
  {:path "sqlite"
   :doc "adds support for SQLite embedded database"}
  :kit/cljs
  {:path "cljs"
   :doc "adds support for cljs using shadow-cljs"}}}
```

As we can see above, the official repository contains three modules. Let's take a look at the [`:kit/html`](https://github.com/kit-clj/modules/tree/master/html) module to see how it works. This module contains a `config.edn` file and a folder called `assets`. Let's take a look at the configuration for the module:

```clojure
{:default
 {:require-restart? true
  :actions
  {:assets           [["assets/home.html"    "resources/html/home.html"]
                      ["assets/error.html"    "resources/html/error.html"]
                      ["assets/css/screen.css"    "resources/public/css/screen.css"]
                      ["assets/img/kit.png" "resources/public/img/kit.png"]
                      ["assets/src/pages.clj"    "src/clj/<<sanitized>>/web/routes/pages.clj"]
                      ["assets/src/layout.clj"   "src/clj/<<sanitized>>/web/pages/layout.clj"]]
   :injections       [{:type   :edn
                       :path   "resources/system.edn"
                       :target []
                       :action :merge
                       :value  {:reitit.routes/pages
                          {:base-path ""
                             :env       #ig/ref :system/env}}}
                      {:type   :edn
                       :path   "deps.edn"
                       :target [:deps]
                       :action :merge
                       :value  {selmer/selmer {:mvn/version "1.12.49"}
                                luminus/ring-ttl-session {:mvn/version "0.3.3"}}}
                      {:type   :clj
                       :path   "src/clj/<<sanitized>>/core.clj"
                       :action :append-requires
                       :value  ["[<<ns-name>>.web.routes.pages]"]}]}}}
```

We can see that the module has a `:default` profile. Kit module profiles allow providing variations of a module with different configurations. For example, a database module could have different profiles for different types of databases. In case of HTML, we only need a single profile.

The`:require-restart?` key specifies that the runtime needs to be restarted for changes to take effect. This is necessary for modules that add Maven dependencies necessitating JVM restarts to be loaded.

Next, the module specifies the actions that will be performed. The first action called `:assets` specifies new assets that will be added to the project. These are template files that will be read from the `assets` folder and injected in the project.

The other configuration action is called `:injections` and specifies code that will be injected into existing files within the project. In order to provide support for rendering HTML templates, the module must update Integrant system configuration by adding a reference for new routes to `system.edn`, add new dependencies to `deps.edn`, and finally require the namespace that contains the routes for the pages in the core namespace of the project. The `:action` values in injections depend on the types of assets being manipulated.

### `:edn` injections

* `:append` - appends the value at the specified path, the value at the path is assumed to be a collection

```clojure
{:type :edn
 :path "deps.edn"
 :target [:paths]
 :action :append
 :value "target/classes/cljsbuild"}
```

* `:merge` - merges value with the value found at the path, the value at the path is assumed to be a map

```clojure
{:type   :edn
 :path   "deps.edn"
 :target [:deps] ; use [] to merge with the top level map
 :action :merge
 :value  {selmer/selmer {:mvn/version "1.12.49"}
         luminus/ring-ttl-session {:mvn/version "0.3.3"}}}
```

### `:clj` injections

* `:append-requires` - appends a require in the specified namespace

```clojure
{:type   :clj
 :path   "src/clj/<<sanitized>>/core.clj"
 :action :append-requires
 :value  ["[<<ns-name>>.web.routes.pages]"]}
```

* `:append-build-task` - appends a build task in `build.clj`

```clojure
{:type   :clj
 :path   "build.clj"
 :action :append-build-task
 :value  (defn build-cljs []
          (println "npx shadow-cljs release app...")
          (let [{:keys [exit]
                 :as   s} (sh "npx" "shadow-cljs" "release" "app")]
            (when-not (zero? exit)
            (throw (ex-info "could not compile cljs" s)))))}
```

* `:append-build-task-call` - appends a function call to the `uber` function in `build.clj`

```clojure
{:type   :clj
 :path   "build.clj"
 :action :append-build-task-call
 :value  (build-cljs)}
```

### `:html` injections

* `:append` - appends a Hiccup form to the target identified by enlive selectors in the specified HTML resource
```clojure
{:type   :html
 :path   "resources/html/home.html"
 :action :append
 :target [:body]
 :value  [:div {:id "app"}]}
```

