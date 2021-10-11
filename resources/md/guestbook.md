## Guestbook Application

This tutorial will guide you through building a simple guestbook application using Kit.
The guestbook allows users to leave a message and to view a list of messages left by others.
The application will demonstrate the basics of HTML templating, database access, and
project architecture.

If you don't have a preferred Clojure editor already, then it's recommended that you use [Calva](https://calva.io/getting-started/) to follow along with this tutorial.

### Installing JDK

Clojure runs on the JVM and requires a copy of JDK to be installed. IF you don't
have JDK already on your system then OpenJDK is recommended and can be downloaded
[here](http://www.azul.com/downloads/zulu/). Note that Kit requires JDK 11 or greater to
work with the default settings.

TODO: Maybe instead of direct download, package manager for macos/linux. idk windows

### Installing a Build Tool

For building and running a project, Kit supports [Clojure Deps and CLI](https://clojure.org/guides/deps_and_cli).

TODO: Future add Leiningen

<div class="deps">
Installing Clojure CLI is accomplished by followings the step below, based on your operating system

MacOS

```
brew install clojure/tools/clojure
```

Linux
```
curl -O https://download.clojure.org/install/linux-install-1.10.3.986.sh
chmod +x linux-install-1.10.3.986.sh
sudo ./linux-install-1.10.3.986.sh
```

For both macOS and Linux, you will need [`clj-new`]() configured in your `~/.clojure/deps.edn` file (or `~/.config/clojure/deps.edn` file) like this:

```
{:aliases
 {:new {:extra-deps {com.github.seancorfield/clj-new {:mvn/version "1.2.362"}}
        :exec-fn clj-new/create
        :exec-args {:template "app"}}}}
```

Note: If you already have configuration in your `deps.edn` file, add the new key under aliases. Make sure the line with `{:aliases` is uncommented, i.e. without `;;`.

For more customization, such as your install location, see [the official docs here](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools)
</div>

### Creating a new application

Once you have <span class="deps">the Clojure CLI</span> installed you can run the following commands in your terminal to
initialize your application:

<div class="deps">
```
clojure -X:new :template kit-clj :name yourname/guestbook
cd guestbook
```
</div>

The above will create a new template project.

### Anatomy of a Kit application

The newly created application has the following structure:

```
├── Dockerfile
├── README.md
├── build.clj
├── deps.edn
├── env
│   ├── dev
│   │   ├── clj
│   │   │   ├── user.clj
│   │   │   └── yourname
│   │   │       └── guestbook
│   │   │           ├── dev_middleware.clj
│   │   │           └── env.clj
│   │   └── resources
│   │       └── logback.xml
│   └── prod
│       ├── clj
│       │   └── yourname
│       │       └── guestbook
│       │           └── env.clj
│       └── resources
│           └── logback.xml
├── kit.edn
├── kit.git-config.edn
├── project.clj
├── resources
│   └── system.edn
├── src
│   └── clj
│       └── yourname
│           └── guestbook
│               ├── config.clj
│               ├── core.clj
│               └── web
│                   ├── controllers
│                   │   └── health.clj
│                   ├── handler.clj
│                   ├── middleware
│                   │   ├── core.clj
│                   │   ├── exception.clj
│                   │   └── formats.clj
│                   └── routes
│                       ├── api.clj
│                       └── utils.clj
└── test
    └── clj
        └── yourname
            └── guestbook
                └── test_utils.clj
```

Let's take a look at what the files in the root folder of the application do:

<div class="deps">
* `deps.edn` - used to manage the project configuration and dependencies by
  deps
* `build.clj` - used to manage the build process by Clojure CLI tools
</div>

* `Dockerfile` - used to facilitate Docker container deployments
* `README.md` - where documentation for the application is conventionally put
* `resources/system.edn` - used for system configuration
* `.gitignore` - a list of assets, such as build generated files, to exclude from Git

### The Source Directory

All our code lives under the `src/clj` folder. Since our application is called yourname/guestbook, this
is the root namespace for the project. Let's take a look at all the namespaces that have been created for us.

#### guestbook

* `config.clj` - this is the place where your `system.edn` is read in to create an immutant configuration map
* `core.clj` - this is the entry point for the application that contains the logic for starting and stopping the server

#### guestbook.web

The `web` namespace is used to define the edges of your application that deal with server communication, such as receiving HTTP requests and returning responses.

* `handler.clj` - defines the entry points for routing and request handling.

#### guestbook.web.controllers

The `controllers` namespace is where the controllers are located. By default, a healthcheck controller is created for you. When you add more controllers you should create namespaces for them here.

* `healthcheck.clj` - default controller that returns basic statistics about your server

#### guestbook.web.middleware

The `middleware` namespace consists of functions that implement cross-cutting functionality such as session management, coercion, etc. These functions can be wrapped around groups of routes to provide common functionality.

* `core.clj` - an aggregate of default middlewares and environment specific middleware
* `exception.clj` - logic for classifying exceptions within controllers and returning appropriate HTTP responses
* `formats.clj` - handles coercion of requests data to Clojure data structures, and response data back to strings

#### guestbook.web.routes

The `routes` namespace is where the HTTP routes are defined. By default `/api` routes are created for you. When you add more routes you should create namespaces for them here.

* `api.clj` - a namespace that routes (default = `/api`) with Swagger UI
* `utils.clj` - general purpose helper functions for getting data from requests

### The Env Directory

Environment specific code and resources are located under the `env/dev`, `env/test`, and the `env/prod` paths.
The `dev` configuration will be used during development and test, `test` during testing,
while the `prod` configuration will be used when the application is packaged for production.

#### `dev/clj`

* `user.clj` - a utility namespace for any code you wish to run during REPL development. You start and stop your server from here during development.
* `guestbook/env.clj` - contains the development configuration defaults
* `guestbook/dev_middleware.clj` - contains middleware used for development that should not be compiled in production

#### `dev/resources`

* `logback.xml` file used to configure the development logging profile

#### `test/resources`

* `logback.xml` file used to configure the test logging profile

#### `prod/clj`

* `guestbook/env.clj` namespace with the production configuration

#### `prod/resources`

* `logback.xml` - default production logging configuration

### The Test Directory

Here is where we put tests for our application. Some test utilities have been provided.

### The Resources Directory

This is where we put all the resources that will be packaged with our application. Anything in the `public` directory under `resources` will be served to the clients by the server.

### Starting Our Server

Your REPL is your best friend in Clojure. Let's start our local development REPL by running

```
clj -M:dev
```

Once we're in to our REPL, we can start our system up by running a command provided in our `env/dev/user.clj`

```clojure
(go) ;; To start the system

(halt) ;; To stop the system

(reload) ;; To refresh the system after making code changes
```

To confirm your server is running, visit [http://localhost:3000/api/health](http://localhost:3000/api/health).

### Kit Modules

Since our application needs to serve some HTML content, let's add the official HTML module. In your REPL, you can execute the following 

```clojure
;; This will download the official Kit modules from git
(kit/sync-modules)

;; Let's list out our available modules
(kit/list-modules)
;; =>
;; :html - adds support for HTML templating using Selmer
;; :sqlite - adds support for SQLite embedded database
;; :cljs - adds support for cljs using shadow-cljs
;; nil

;; We'll want to install the :html module to serve some HTML pages
(kit/install-module :html)
;; =>
;; updating file: resources/system.edn
;; injecting
;; path: [:reitit.routes/pages]
;; value: {:base-path "", :env #ig/ref :system/env}
;; updating file: deps.edn
;; injecting
;; path: [:deps selmer/selmer]
;; value: #:mvn{:version "1.12.44"}
;; injecting
;; path: [:deps ring/ring-defaults]
;; value: #:mvn{:version "0.3.3"}
;; injecting
;; path: [:deps luminus/ring-ttl-session]
;; value: #:mvn{:version "0.3.3"}
;; updating file: src/clj/yourname/guestbook/core.clj
;; applying
;; action: :append-requires
;; value: [[yourname.guestbook.web.routes.pages]]
;; html installed successfully!
;; restart required!
;; nil
```

If you have issues with syncing your modules, refer to [TODO DOCS HERE]()


#### HTML templates

The `resources/html` directory is reserved for the [Selmer](https://github.com/yogthos/Selmer) templates
that represent the application pages.

* `about.html` - about page
* `base.html` - base layout for the site
* `home.html` - home page
* `error.html` - error page template

#### SQL Queries

The SQL queries are found in the `resources/sql` folder.

* `queries.sql` - defines the SQL queries and their associated function names

#### The Migrations Directory

Luminus uses [Migratus](https://github.com/yogthos/migratus) for migrations. Migrations are managed using up and down SQL files.
The files are conventionally versioned using the date and will be applied in order of their creation.

* `20150718103127-add-users-table.up.sql` - migrations file to create the tables
* `20150718103127-add-users-table.down.sql` - migrations file to drop the tables


### The Project File
<div class="lein">
As was noted above, all the dependencies are managed via updating the `project.clj` file.
The project file of the application we've created is found in its root folder and should look as follows:

```clojure
(defproject guestbook "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[cheshire "5.8.1"]
                 [clojure.java-time "0.3.2"]
                 [com.h2database/h2 "1.4.197"]
                 [conman "0.8.3"]
                 [cprop "0.1.13"]
                 [funcool/struct "1.3.0"]
                 [luminus-immutant "0.2.5"]
                 [luminus-migrations "0.6.4"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.7"]
                 [metosin/muuntaja "0.6.3"]
                 [metosin/reitit "0.2.13"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.webjars.npm/bulma "0.7.4"]
                 [org.webjars.npm/material-icons "0.3.0"]
                 [org.webjars/webjars-locator "0.36"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.6"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot guestbook.core

  :plugins [[lein-immutant "2.1.0"]]

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "guestbook.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[expound "0.7.2"]
                                 [pjstadig/humane-test-output "0.9.0"]
                                 [prone "1.6.1"]
                                 [ring/ring-devel "1.7.1"]
                                 [ring/ring-mock "0.3.2"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.23.0"]]

                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
```

As you can see the `project.clj` file is simply a Clojure list containing key/value pairs describing different aspects of the application.

The most common task is adding new libraries to the project. These libraries are specified using the `:dependencies` vector.
In order to use a new library in our project we simply have to add its dependency here.

The items in the `:plugins` vector can be used to provide additional functionality.

The `:profiles` contain a map of different project configurations that are used to initialize it for either development or production builds.

Note that the project sets up composite profiles for `:dev` and `:test`. These profiles contain the variables from `:project/dev` and `:project/test` profiles,
as well as from `:profiles/dev` and `:profiles/test` found in the `profiles.clj`. The latter can be used for additional local configuration that is not meant to be checked into the shared code repository.

Please refer to the [official Leiningen documentation](http://leiningen.org/#docs) for further details on structuring the `project.clj` build file.
</div>
<div class="boot">
```
(set-env!
  :dependencies [[cheshire "5.8.1"]
                 [clojure.java-time "0.3.2"]
                 [com.h2database/h2 "1.4.197"]
                 [conman "0.8.3"]
                 [cprop "0.1.13"]
                 [funcool/struct "1.3.0"]
                 [luminus-immutant "0.2.5"]
                 [luminus-migrations "0.6.4"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.7"]
                 [metosin/muuntaja "0.6.3"]
                 [metosin/reitit "0.2.13"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.webjars.npm/bulma "0.7.4"]
                 [org.webjars.npm/material-icons "0.3.0"]
                 [org.webjars/webjars-locator "0.36"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.6"]]
 :source-paths #{"src/clj"}
 :resource-paths #{"resources"})

(deftask dev
  "Enables configuration for a development setup."
  []
  (set-env!
   :source-paths #(conj % "env/dev/clj")
   :resource-paths #(conj % "env/dev/resources")
   :dependencies #(concat % '[[prone "1.1.4"]
                              [ring/ring-mock "0.3.0"]
                              [ring/ring-devel "1.6.1"]
                              [pjstadig/humane-test-output "0.8.2"]]))
  (task-options! repl {:init-ns 'user})
  (require 'pjstadig.humane-test-output)
  (let [pja (resolve 'pjstadig.humane-test-output/activate!)]
    (pja))
  identity)

(deftask testing
  "Enables configuration for testing."
  []
  (dev)
  (set-env! :resource-paths #(conj % "env/test/resources"))
  identity)

(deftask prod
  "Enables configuration for production building."
  []
  (merge-env! :source-paths #{"env/prod/clj"}
              :resource-paths #{"env/prod/resources"})
  identity)

(deftask start-server
  "Runs the project without building class files.

  This does not pause execution. Combine with a wait task or use the \"run\"
  task."
  []
  (require 'guestbook.core)
  (let [m (resolve 'guestbook.core/-main)]
    (with-pass-thru _
      (m))))

(deftask run
  "Starts the server and causes it to wait."
  []
  (comp
   (start-server)
   (wait)))

(deftask uberjar
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (comp
   (prod)
   (aot :namespace #{'guestbook.core})
   (uber)
   (jar :file "guestbook.jar" :main 'guestbook.core)
   (sift :include #{#"guestbook.jar"})
   (target)))
```

As you can see the build.boot file is simple a clojure file which defines a
series of tasks and other environment settings needed to set up a project.

The most common change is adding new dependencies to the dependency list in the
`set-env!` call.

Each task defined can be called using `boot task-name` on the command line or
`(boot (task-name))` on a REPL (which can be started with `boot repl` on the commandline).
</div>

### Creating the Database

First, we will create a model for our application, to do that we'll open up the `<date>-add-users-table.up.sql`
file located under the `migrations` folder. The file has the following contents:

```sql
CREATE TABLE users
(id VARCHAR(20) PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30),
 admin BOOLEAN,
 last_login TIME,
 is_active BOOLEAN,
 pass VARCHAR(300));
```

We'll replace the `users` table with one that's more appropriate for our application:

```sql
CREATE TABLE guestbook
(id INTEGER PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(30),
 message VARCHAR(200),
 timestamp TIMESTAMP(7));
```

The guestbook table will store all the fields describing the message, such as the name of the
commenter, the content of the message and a timestamp.
Next, let's replace the contents of the `<date>-add-users-table.down.sql` file accordingly:

```sql
DROP TABLE guestbook;
```

We can now run the migrations using the following command from the root of our project:

<div class="lein">
```
lein run migrate
```
</div>
<div class="boot">
```
boot dev [ run migrate ]
```
</div>

If everything went well we should now have our database initialized.

### Accessing The Database

Next, we'll take a look at the `src/clj/guestbook/db/core.clj` file.
Here, we can see that we already have the definition for our database connection.

```clojure
(ns guestbook.db.core
  (:require
    [conman.core :as conman]
    [mount.core :refer [defstate]]
    [guestbook.config :refer [env]]))

(defstate ^^:dynamic *db*
           :start (conman/connect! {:jdbc-url (env :database-url)})
           :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")
```

The database connection is read from the environment map at runtime. By default, the `:database-url` key points to
a string with the connection URL for the database.
 This variable is populated from the `dev-config.edn` file during development and has to be set as an environment variable for production, e.g:

```
export DATABASE_URL="jdbc:h2:./guestbook.db"
```

Since we're using the embedded H2 database, the data is stored in a file specified in the URL that's found in the path relative to where the project is run.

The functions that map to database queries are generated when `bind-connection` is called. As we can see it references the `sql/queries.sql` file.
This location is found under the `resources` folder. Let's open up this file and take a look inside.

```sql
-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id
```

As we can see each function is defined using the comment that starts with `-- :name` followed by the name of the function.
The next comment provides the doc string for the function and finally we have the body that's plain SQL. The parameters are
denoted using `:` notation. Let's replace the existing queries with some of our own:


```sql
-- :name save-message! :! :n
-- :doc creates a new message
INSERT INTO guestbook
(name, message, timestamp)
VALUES (:name, :message, :timestamp)

-- :name get-messages :? :*
-- :doc selects all available messages
SELECT * FROM guestbook
```

Now that our model is all setup, let's start up the application.

### Running the Application

We can run our application in development mode as follows:

```
<lein-div>
>lein run</lein-div><boot-div>
>boot dev run</boot-div>
2019-03-17 09:01:03,709 [main] DEBUG org.jboss.logging - Logging Provider: org.jboss.logging.Slf4jLoggerProvider
2019-03-17 09:01:04,614 [main] INFO  guestbook.env -
-=[guestbook started successfully using the development profile]=-
2019-03-17 09:01:04,709 [main] INFO  luminus.http-server - starting HTTP server on port 3000
2019-03-17 09:01:05,047 [main] INFO  org.projectodd.wunderboss.web.Web - Registered web context /
2019-03-17 09:01:05,048 [main] INFO  guestbook.nrepl - starting nREPL server on port 7000
2019-03-17 09:01:05,075 [main] INFO  guestbook.core - #'guestbook.db.core/*db* started
2019-03-17 09:01:05,076 [main] INFO  guestbook.core - #'guestbook.handler/init-app started
2019-03-17 09:01:05,076 [main] INFO  guestbook.core - #'guestbook.handler/app started
2019-03-17 09:01:05,076 [main] INFO  guestbook.core - #'guestbook.core/http-server started
2019-03-17 09:01:05,076 [main] INFO  guestbook.core - #'guestbook.core/repl-server started
-=[guestbook started successfully using the development profile]=-
```

Once server starts, you should be able to navigate to [http://localhost:3000](http://localhost:3000) and see
the app running. The server can be started on an alternate port by either passing it as a parameter as seen below,
or setting the `PORT` environment variable.

<div class="lein">
```
lein run -p 8000
```
</div><div class="boot">
```
boot dev [ run -- -p 8000 ]
```
</div>

Alternatively, you can start the application from the REPL using `start` function defined in the `user` namespace, e.g:

```
<lein-div>
lein repl
</lein-div><boot-div>
boot repl
</boot-div>
2018-01-30 15:48:31,147 [main] DEBUG org.jboss.logging - Logging Provider: org.jboss.logging.Slf4jLoggerProvider
nREPL server started on port 51655 on host 127.0.0.1 - nrepl://127.0.0.1:51655
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_45-b14
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user=>(start)
018-01-30 15:48:58,211 [nREPL-worker-0] INFO  guestbook.env -
-=[guestbook started successfully using the development profile]=-
2018-01-30 15:48:58,505 [nREPL-worker-0] INFO  luminus.http-server - starting HTTP server on port 3000
2018-01-30 15:48:58,547 [nREPL-worker-0] DEBUG io.undertow - starting undertow server io.undertow.Undertow@115503d9
2018-01-30 15:48:58,593 [nREPL-worker-0] INFO  org.xnio - XNIO version 3.3.6.Final
2018-01-30 15:48:58,707 [nREPL-worker-0] DEBUG io.undertow - Configuring listener with protocol HTTP for interface 0.0.0.0 and port 3000
2018-01-30 15:48:58,745 [nREPL-worker-0] INFO  org.projectodd.wunderboss.web.Web - Registered web context /
{:started ["#'guestbook.config/env" "#'guestbook.handler/init-app" "#'guestbook.handler/app" "#'guestbook.core/http-server"]}
```

Note that the page is prompting us to run the migrations in order to initialize the database. However, we've already done that earlier, so we won't need to do that again.

### Creating Pages and Handling Form Input

Our routes are defined in the `guestbook.routes.home` namespace. Let's open it up and add the logic for
rendering the messages from the database. We'll first need to add a reference to our `db` namespace along with
references for [Bouncer](https://github.com/leonardoborges/bouncer) validators and [ring.util.response](http://ring-clojure.github.io/ring/ring.util.response.html)

```clojure
(ns guestbook.routes.home
  (:require
   [guestbook.layout :as layout]
   [guestbook.db.core :as db]
   [clojure.java.io :as io]
   [guestbook.middleware :as middleware]
   [ring.util.http-response :as response]))
```

Next, we'll create a schema that defines the form parameters
and add a function to validate them. We'll first have to update the namespace declaration above to require [Struct](http://funcool.github.io/struct/latest/) library:

```
(ns guestbook.routes.home
  (:require
   ...
   [struct.core :as st])
```

```clojure
(def message-schema
  [[:name
    st/required
    st/string]

   [:message
    st/required
    st/string
    {:message "message must contain at least 10 characters"
     :validate #(> (count %) 9)}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))
```

The function uses the `validate` function from Struct to check that the `:name` and the `:message` keys conform to the rules we specified.
Specifically, the name is required and the message must contain at least
10 characters. Struct uses a vector to specify the fields being validated where each field is itself a vector starting
with the keyword pointing to the value being validated followed by one or more validators. Custom validators can be specified using a map as seen with with the validator for the character count in the message.

We'll now add a function to validate and save messages:

```clojure
(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (response/found "/")
        (assoc :flash (assoc params :errors errors)))
    (do
      (db/save-message!
       (assoc params :timestamp (java.util.Date.)))
      (response/found "/"))))
```

The function will grab the `:params` key from the request that contains the form parameters. When the `validate-message` functions returns errors we'll redirect back to `/`, we'll associate a `:flash` key with the response where we'll put the supplied parameters along with the errors. Otherwise, we'll save the message in our database and redirect.

We can now change the `home-page` handler function to look as follows:

```clojure
(defn home-page [{:keys [flash] :as request}]
  (layout/render
   request
   "home.html"
   (merge {:messages (db/get-messages)}
          (select-keys flash [:name :message :errors]))))
```

The function renders the home page template and passes it the currently stored messages along with any parameters from the `:flash` session, such as validation errors.

Recall that the database accessor functions were automatically generated for us by the `(conman/bind-connection *db* "sql/queries.sql")` statement ran in the `guestbook.db.core` namespace. The names of these functions are inferred from the `-- :name` comments in the SQL templates found in the `resources/sq/queries.sql` file.

Our routes will now have to pass the request to both the `home-page` and the `save-message!` handlers:

```clojure
(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page
         :post save-message!}]
   ["/about" {:get about-page}]])
```

Now that we have our controllers setup, let's open the `home.html` template located under the `resources/html` directory. Currently, it simply renders the contents of the `content` variable inside the content block:

```xml
{% extends "base.html" %}
{% block content %}
  <div class="content">
  {{docs|markdown}}
  </div>
{% endblock %}
```

We'll update our `content` block to iterate over the messages and print each one in a list:

```xml
{% extends "base.html" %}
{% block content %}
<div class="content">
  <div class="columns">
    <div class="column">
      <h3>Messages</h3>
      <ul class="messages">
        {% for item in messages %}
        <li>
          <time>{{item.timestamp|date:"yyyy-MM-dd HH:mm"}}</time>
          <p>{{item.message}}</p>
          <p> - {{item.name}}</p>
        </li>
        {% endfor %}
      </ul>
    </div>
  </div>
</div>
{% endblock %}
```

As you can see above, we use a `for` iterator to walk the messages.
Since each message is a map with the message, name, and timestamp keys, we can access them by name.
Also, notice the use of the `date` filter to format the timestamps into a human readable form.

Finally, we'll create a form to allow users to submit their messages. We'll populate the name and message values if they're supplied and render any errors associated with them. Note that the forms also uses the `csrf-field` tag that's required for cross-site request forgery protection.

```xml
<div class="columns">
    <div class="column">
      <form method="POST" action="/">
        {% csrf-field %}
        <p>
          Name:
          <input class="input" type="text" name="name" value="{{name}}" />
        </p>
        {% if errors.name %}
        <div class="notification is-danger">{{errors.name|join}}</div>
        {% endif %}
        <p>
          Message:
          <textarea class="textarea" name="message">{{message}}</textarea>
        </p>
        {% if errors.message %}
        <div class="notification is-danger">{{errors.message|join}}</div>
        {% endif %}
        <input type="submit" class="button is-primary" value="comment" />
      </form>
    </div>
  </div>
```

Our final `home.html` template should look as follows:

```xml
{% extends "base.html" %}
{% block content %}
<div class="content">
  <div class="columns">
    <div class="column">
      <h3>Messages</h3>
      <ul class="messages">
        {% for item in messages %}
        <li>
          <time>{{item.timestamp|date:"yyyy-MM-dd HH:mm"}}</time>
          <p>{{item.message}}</p>
          <p> - {{item.name}}</p>
        </li>
        {% endfor %}
      </ul>
    </div>
  </div>
  <div class="columns">
    <div class="column">
      <form method="POST" action="/">
        {% csrf-field %}
        <p>
          Name:
          <input class="input" type="text" name="name" value="{{name}}" />
        </p>
        {% if errors.name %}
        <div class="notification is-danger">{{errors.name|join}}</div>
        {% endif %}
        <p>
          Message:
          <textarea class="textarea" name="message">{{message}}</textarea>
        </p>
        {% if errors.message %}
        <div class="notification is-danger">{{errors.message|join}}</div>
        {% endif %}
        <input type="submit" class="button is-primary" value="comment" />
      </form>
    </div>
  </div>
</div>
{% endblock %}
```

Finally, we can update the `screen.css` file located in the `resources/public/css` folder to format our form nicer:

```
ul {
	list-style: none;
}

ul.messages li {
	position: relative;
	font-size: 16px;
	padding: 5px;
	border-bottom: 1px dotted #ccc;
}

li:last-child {
	border-bottom: none;
}

li time {
	font-size: 12px;
	padding-bottom: 20px;
}

form, .error {
	padding: 30px;
	margin-bottom: 50px;
	position: relative;
}
```

When we reload the page in the browser we should be greeted by the guestbook page.
We can test that everything is working as expected by adding a comment in our comment form.

## Adding some tests

Now that we have our application working we can add some tests for it.
Let's open up the `test/clj/guestbook/test/db/core.clj` namespace and update it as follows:

```clojure
(ns guestbook.test.db.core
  (:require
   [guestbook.db.core :refer [*db*] :as db]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [clojure.java.jdbc :as jdbc]
   [guestbook.config :refer [env]]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'guestbook.config/env
      #'guestbook.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-message
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (let [timestamp (java.time.LocalDateTime/now)]
      (is (= 1 (db/save-message!
                t-conn
                {:name "Bob"
                 :message "Hello, World"
                 :timestamp timestamp}
                {:connection t-conn})))
      (is (=
           {:name "Bob"
            :message "Hello, World"
            :timestamp timestamp}
           (-> (db/get-messages t-conn {})
               (first)
               (select-keys [:name :message :timestamp])))))))
```

We can now run <div class="lein">`lein test`</div><div class="boot">`boot
testing test`</div> in the terminal to see that our database interaction works
as expected.

<div class="lein">
Luminus comes with [lein-test-refresh](https://github.com/jakemcc/lein-test-refresh) enabled by default. This plugin allows running tests continuously
whenever a change in a namespace is detected. We can start a test runner in a new terminal using the `lein test-refresh` command.
</div>
<div class="boot">
An auto test can be easily enabled by using the `watch` task in boot. We
encourage you to start a test runner in a new terminal using `boot testing watch
test`
</div>

## Packaging the application

The application can be packaged for standalone deployment by running the following command:

<div class="lein">
```
lein uberjar
```
</div>
<div class="boot">
```
boot uberjar
```
</div>

This will create a runnable jar that can be run as seen below:

<div class="lein">
```
export DATABASE_URL="jdbc:h2:./guestbook_dev.db"
java -jar target/uberjar/guestbook.jar
```
</div>
<div class="boot">
```
export DATABASE_URL="jdbc:h2:./guestbook_dev.db"
java -jar target/guestbook.jar
```
</div>

Note that we have to supply the `DATABASE_URL` environment variable when running as a jar, as
it's not packaged with the application.

***

Complete source listing for the tutorial is available [here](https://github.com/luminus-framework/examples/tree/master/guestbook).
