## Kit Modules

Kit modules are templates that can be applied to an existing project using the [kit-generator](https://github.com/kit-clj/kit/tree/master/libs/kit-generator). In that, they are different from profiles, which you can apply only when creating a new project. 

### Using Modules

Kit embraces the REPL and the generator library is aliased in the `user` namespace as `kit`. Let's see how we can us it to install HTML module in the project. First, we need to sync our module repositories. This is done by running the following command in the REPL:

```clojure
user=> (kit/sync-modules)
2021-11-30 11:42:41,010 [main] DEBUG org.eclipse.jgit.util.FS - readpipe [git, --version],/usr/local/bin
2021-11-30 11:42:41,030 [main] DEBUG org.eclipse.jgit.util.FS - readpipe may return 'git version 2.33.1'
2021-11-30 11:42:41,030 [main] DEBUG org.eclipse.jgit.util.FS - remaining output:
...
2021-11-30 11:42:41,769 [main] DEBUG o.e.jgit.transport.PacketLineOut - git> 0000
2021-11-30 11:42:41,769 [main] DEBUG o.e.jgit.transport.PacketLineOut - git> done

2021-11-30 11:42:41,835 [main] DEBUG o.e.jgit.transport.PacketLineIn - git< NAK
nil
user=>
```

Once the modules are synchronized, we can list the available modules by running `kit/list-modules`:

```clojure
user=> (kit/list-modules)
:kit/sql - adds support for SQL. Available profiles [ :postgres :sqlite ]. Default profile :sqlite
:kit/html - adds support for HTML templating using Selmer
:kit/ctmx - adds support for HTMX using CTMX
:kit/sente - adds support for Sente websockets to cljs
:kit/cljs - adds support for cljs using shadow-cljs
:kit/metrics - adds support for metrics using prometheus through iapetos
:kit/auth - adds support for auth middleware using Buddy
:kit/nrepl - adds support for nREPL
:kit/htmx - adds support for HTMX using hiccup
:done
user=>
```

We can see that the three modules specified in the official modules repository are now available for use. Let's install the HTML module by running `kit/install-module` function and passing it the keyword specifying the module name:

```clojure
user=> (kit/install-module :kit/html)
updating file: resources/system.edn
updating file: deps.edn
updating file: src/clj/kit/guestbook/core.clj
applying
 action: :append-requires
 value: ["[kit.guestbook.web.routes.pages]"]
:kit/html installed successfully!
restart required!
nil
user=>
```

Let's restart the REPL and run `(go)` command again to start the application. We should now be able to navigate to `http://localhost:3000` and see the default HTML page provided by the module.

Generator aims to be idempotent, and will err on the side of safety in case of conflicts. For example, if we attempt to install `:kit/html` module a second time then we'll see he following output:

```clojure
user=> (kit/install-module :kit/html)
:kit/html requires following modules: nil
module :kit/html is already installed!
nil
user=>
```

Generator lets us know that the module already exists and there is nothing to be done.

### Creating Custom Modules

Modules are managed using git repositories. You can find the official modules [here](https://github.com/kit-clj/modules). Let's take a brief look at what a module repository looks like.

A module repository must contain a `modules.edn` file describing the modules that are provided. For example, here are the official modules provided by Kit:

```clojure
{:name "kit-modules"
 :modules
 {:kit/html
  {:path "html"
   :doc "adds support for HTML templating using Selmer"}
  :kit/metrics
  {:path "metrics"
   :doc "adds support for metrics using prometheus through iapetos"}
  :kit/sql
  {:path "sql"
   :doc "adds support for SQL. Available profiles [ :postgres :sqlite ]. Default profile :sqlite"}
  :kit/cljs
  {:path "cljs"
   :doc "adds support for cljs using shadow-cljs"}
  :kit/nrepl
  {:path "nrepl"
   :doc "adds support for nREPL"}}}
```

As you can see above, the official repository contains five modules. Let's take a look at the [`:kit/html`](https://github.com/kit-clj/modules/tree/master/html) module to see how it works. This module contains a `config.edn` file and a folder called `assets`. It has the following configuration:

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

We can see that the module has a `:default` profile. Kit module profiles allow us to provide variations of a module with different configurations. For example, a database module could have different profiles for different types of databases. In case of HTML, we only need a single profile.

The`:require-restart?` key specifies that the runtime needs to be restarted for changes to take effect. This is necessary for modules that add Maven dependencies necessitating JVM restarts to be loaded.

Next, the module specifies the actions that will be performed. The first action, called `:assets`, specifies new assets that will be added to the project. These are template files that will be read from the `assets` folder and injected in the project.

The other configuration action is called `:injections` and specifies code that will be injected into existing files within the project. In order to provide support for rendering HTML templates, the module must update Integrant system configuration by adding a reference for new routes to `system.edn`, add new dependencies to `deps.edn`, and finally require the namespace that contains the routes for the pages in the core namespace of the project. The `:action` values in injections depend on the types of assets being manipulated.

`:edn` injections

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

`:clj` injections

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

HTML injections

* `:append` - appends a Hiccup form to the target identified by enlive selectors in the specified HTML resource

```clojure
{:type   :html
 :path   "resources/html/home.html"
 :action :append
 :target [:body]
 :value  [:div {:id "app"}]}
```

