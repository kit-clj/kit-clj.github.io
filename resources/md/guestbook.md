## Guestbook Application

This tutorial will guide you through building a simple guestbook application using Kit.
The guestbook allows users to leave a message and to view a list of messages left by others.
The application will demonstrate the basics of HTML templating, database access, and
project architecture.

If you don't have a preferred Clojure editor already, then it's recommended that you use [Calva](https://calva.io/getting-started/) to follow along with this tutorial.

### Installing JDK

Clojure runs on the JVM and requires a copy of JDK to be installed. If you don't
have JDK already on your system then OpenJDK is recommended and can be downloaded
[here](http://www.azul.com/downloads/zulu/). Note that Kit requires JDK 11 or greater to
work with the default settings. Alternatively, follow the instructions for installing packages
on your system.

### Installing a Build Tool

For building and running a project, Kit supports [Clojure Deps and CLI](https://clojure.org/guides/deps_and_cli).

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

For both macOS and Linux, you will need [`clj-new`](https://github.com/seancorfield/clj-new) configured either in `~/.clojure/deps.edn` or `~/.config/clojure/deps.edn` as follows:

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

Once you have the Clojure CLI installed, you can run the following commands in your terminal to
initialize your application:

```
clojure -X:new :template io.github.kit-clj :name yourname/guestbook
cd guestbook
```


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

* `deps.edn` - used to manage the project configuration and dependencies by deps
* `build.clj` - used to manage the build process by Clojure CLI tools
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

#### The Dev Directory

Any source code that's meant to be used during development time is placed in the `dev/clj`. Following namespaces will be generated by default when the project is created:

* `user.clj` - a utility namespace for any code you wish to run during REPL development. You start and stop your server from here during development.
* `guestbook/env.clj` - contains the development configuration defaults
* `guestbook/dev_middleware.clj` - contains middleware used for development that should not be compiled in production

Resources that are used during development aree placed in the `dev/resources`. By default this folder will contain logback configuration tuned for development:

* `logback.xml` file used to configure the development logging profile

Similarly, testing configuration is placed in the `test/resources`:

* `logback.xml` file used to configure the test logging profile

#### The Prod Directory

This directory is a counterpart of the `dev` directory, and contains versions of namespaces and resources that will be used when the application is built for production. The `prod/clj` folder will contain the following namespace when the project is created:

* `guestbook/env.clj` namespace with the production configuration

Mwanwhile, `prod/resources` will contain logback configuration tuned for production use:

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

### System

System resources such as HTTP server ports, database connections are defined in the `resources/system.edn` file. For example, this key defines HTTP server configuration such as the host, port, and HTTP handler:

```clojure
:server/undertow
 {:port #long #or [#env PORT 3000]
  :host #or [#env HTTP_HOST "0.0.0.0"]
  :handler #ig/ref :handler/ring}
```

Now that we've looked at the structure of the default project, let's see how we can add some additional functionality via modules.

### Kit Modules

Kit modules consist of templates that can be used to inject code and resources into a Kit project. The modules are defined in the `kit.edn` file. By default, the configuration will point to the official module repository:

```clojure
:modules   {:root         "modules"
            :repositories [{:url  "git@github.com:kit-clj/modules.git"
                            :tag  "master"
                            :name "kit-modules"}]}
```

Since our application needs to serve some HTML content, let's add the official HTML module. In your REPL, you can execute the following: 

```clojure
;; This will download the official Kit modules from git
(kit/sync-modules)

;; Let's list out our available modules
(kit/list-modules)
;; =>
;; :kit/html - adds support for HTML templating using Selmer
;; :kit/sqlite - adds support for SQLite embedded database
;; :kit/cljs - adds support for cljs using shadow-cljs
;; nil

;; We'll want to install the :html module to serve some HTML pages
(kit/install-module :kit/html)
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

We can see from the output of the `kit/install-module` that we need to restart our REPL. Let's do that. Once we are up again, we can test if our module is installed correctly by starting up the server with `(go)` and navigating to [localhost:3000](http://localhost:3000).

#### HTML templates

The module generated the following files under the `resources/html` directory:

* `home.html` - home page
* `error.html` - error page template

This directory is reserved for HTML templates that represent the application pages.

The module also generated the namespace `yourname.guestbook.web.pages.layout` which helps you render HTML pages using [Selmer templating engine](https://github.com/yogthos/Selmer)

#### Routing

The module also helped generate some routes for us under `yourname.guestbook.web.routes.pages`. See [routing](/docs/routes.html) documentation for more details.

#### Adding a database

Similarly to the way we installed the HTML module, we can add a SQLite module called `:kit/sqlite`.

```clojure
(kit/install-module :kit/sqlite)
;; updating file: resources/system.edn
;; injecting
;;  path: [:db.sql/connection] 
;;  value: #profile {:dev {:jdbc-url "jdbc:sqlite:_dev.db"}, :test {:jdbc-url "jdbc:sqlite:_test.db"}, :prod {:jdbc-url #env JDBC_URL}}
;; injecting
;;  path: [:db.sql/query-fn] 
;;  value: {:conn #ig/ref :db.sql/connection, :options {}, :filename "sql/queries.sql"}
;; injecting
;;  path: [:db.sql/migrations] 
;;  value: {:store :database, :db {:datasource #ig/ref :db.sql/connection}, :migrate-on-init? true}
;; updating file: deps.edn
;; injecting
;;  path: [:deps io.github.kit-clj/kit-sql] 
;;  value: #:mvn{:version "0.1.0"}
;; injecting
;;  path: [:deps org.xerial/sqlite-jdbc] 
;;  value: #:mvn{:version "3.34.0"}
;; updating file: src/clj/yourname/guestbook/core.clj
;; applying
;;  action: :append-requires 
;;  value: [[kit.edge.db.sql]]
;; sqlite installed successfully!
;; restart required!
```

Let's restart again and create our first database migration.

```clojure
(migratus.core/create 
  (:db.sql/migrations state/system)
  "add-guestbook-table")
```

This will generate two files under your `resources/migrations` directory. They will look something like this, but with a different prefix:

```
20211109173842-add-guestbook-table.up.sql
20211109173842-add-guestbook-table.down.sql
```

Kit uses [Migratus](https://github.com/yogthos/migratus) for migrations. Migrations are managed using up and down SQL files.
The files are conventionally versioned using the date and will be applied in order of their creation.

Let's add some content to create our messages table under the `<date>-add-guestbook-table.up.sql` file

```sql
CREATE TABLE guestbook
(id INTEGER PRIMARY KEY AUTOINCREMENT,
 name VARCHAR(30),
 message VARCHAR(200),
 timestamp TIMESTAMP(7));
```

The guestbook table will store all the fields describing the message, such as the name of the
commenter, the content of the message and a timestamp.
Next, let's replace the contents of the `<date>-add-guestbook-table.down.sql` file accordingly:

```sql
DROP TABLE IF EXISTS guestbook;
```

Migrations will be run automatically using the configuration found in `system.edn`:

```clojure
:db.sql/migrations {:store :database,
                     :db {:datasource #ig/ref :db.sql/connection},
                     :migrate-on-init? true}
```

#### SQL Queries

The SQL queries are found in the `resources/sql` folder.

* `queries.sql` - defines the SQL queries and their associated function names

The file initially contains some placeholder queries to help remind you of basic SQL syntax.
As we can see each function is defined using the comment that starts with `-- :name` followed by the name of the function.
The next comment provides the doc string for the function and finally we have the body that's plain SQL. For full documentation of this syntax you can view the [HugSQL documentation](https://www.hugsql.org/). The parameters are
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

Now that our model is all setup, let's reload the application, and test our queries in the REPL:

```clojure
(reset)

(def query-fn (:db.sql/query-fn state/system))

(query-fn :save-message! {:name      "m1"
                          :message   "hello world"
                          :timestamp (java.util.Date.)})
;; => 1

(query-fn :get-messages {})
;; => [{:id 1, :name "m1", :message "hello world", :timestamp 1636480432353}]
```

### Accessing The Database

Let's take a look at the `resources/sql/queries.sql` template file. It's contents should look as follows:

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

As we can see each function is defined using a comment that starts with `-- :name` followed by the name of the function.
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

### Exposing Database Queries in the Router Component

Now that we've added the queries, we'll need to update `resources/system.edn` in order to make these queries available in the page router component. This is achieved by adding a `:query-fn` key in the component definition as follows:

```clojure
:reitit.routes/pages
{:base-path "",
 :query-fn #ig/ref :db.sql/query-fn
 :env      #ig/ref :system/env}
```

The key references the `:db.sql/query-fn` component which is responsible for instantiating query functions using the template found in the `resources/sql/queries.sql` file:

```clojure
:db.sql/query-fn
{:conn #ig/ref :db.sql/connection,
 :options {},
 :filename "sql/queries.sql"}
```

Now that our model is all setup, let's start up the application.

### Running the Application

We can run our application in development mode as follows:

```
clj -M:dev -M:repl
#object[clojure.lang.MultiFn 0x34594779 "clojure.lang.MultiFn@34594779"]
```

Once the REPL is ready, simply type `(go)` to start up the application:

```
user=> (go)
2021-12-18 11:35:41,821 [main] INFO  kit.config - Reading config system.edn
2021-12-18 11:35:41,951 [main] DEBUG org.jboss.logging - Logging Provider: org.jboss.logging.Slf4jLoggerProvider
2021-12-18 11:35:41,969 [main] INFO  org.xnio - XNIO version 3.8.4.Final
2021-12-18 11:35:42,113 [main] INFO  org.jboss.threads - JBoss Threads version 3.1.0.Final
2022-01-06 15:37:00,219 [main] INFO  kit.edge.server.undertow - server started on port 3000
2022-01-06 15:37:00,226 [main] INFO  kit.edge.utils.repl - REPL server started on host: 0.0.0.0 port: 7000

user=>
```

Once server starts, you should be able to navigate to [http://localhost:3000](http://localhost:3000) and see
the app running.

### Creating a controller for the guestbook

We'll create a new controller that will be responsible for saving new messages in the database. Let's create a new namespace called `kit.guestbook.web.controllers.guestbook`, and add the following content to it:

```clojure
(ns kit.guestbook.web.controllers.guestbook
  (:require   
   [clojure.tools.logging :as log]
   [kit.guestbook.web.routes.utils :as utils]   
   [ring.util.http-response :as http-response]))

(defn save-message!
  [{{:strs [name message]} :form-params :as request}]
  (log/debug "saving message" name message)
  (let [{:keys [query-fn]} (utils/route-data request)]    
    (try      
      (if (or (empty? name) (empty? message))
        (cond-> (http-response/found "/")
          (empty? name)
          (assoc-in [:flash :errors :name] "name is required")
          (empty? message)
          (assoc-in [:flash :errors :message] "message is required"))
        (do
          (query-fn :save-message! {:name name :message message})
          (http-response/found "/")))
      (catch Exception e
        (log/error e "failed to save message!")
        (-> (http-response/found "/")
            (assoc :flash {:errors {:unknown (.getMessage e)}}))))))
```

As you can see, the namespace contains a `save-message!` function that executes the query to add a new message to the guestbook table. The query is accessed from the request route data using the `kit.guestbook.web.routes.utils/route-data` function. The function returns a map containing the `query-fn` key that in turn contains a map of the query functions. The names of these functions are inferred from the `-- :name` comments in the SQL templates found in the `resources/sq/queries.sql` file.

Our function will grab the `form-params` key from the request that contains the form data and attempt to save the message in the database. The controller will redirect back to the home page, and if set any errors as a flash session on the response.

### Creating Pages and Handling Form Input

The routes for the HTML pages are defined in the `kit.guestbook.web.routes.pages` namespace. Let's reference our `kit.guestbook.web.controllers.guestbook` and `kit.guestbook.web.routes.utils` namespaces in the namespace declaration.

```clojure
(ns kit.guestbook.web.routes.pages
  (:require
    ...
    [kit.guestbook.web.routes.utils :as utils]
    [kit.guestbook.web.controllers.guestbook :as guestbook]))
```

We can now add the logic for rendering the messages from the database by updating the `home-page` handler function to look as follows:

```clojure
(defn home [{:keys [flash] :as request}]
  (let [{:keys [query-fn]} (utils/route-data request)]
    (layout/render request "home.html" {:messages (query-fn :get-messages {})
                                        :errors (:errors flash)})))
```

The function now renders the `home.html` template passing it currently stored messages queried from the database using the `:messages` key and with any errors using the `:errors` key.

Finally, we'll add the `/save-message` route in the `page-routes` function. This route will pass the request to the `guestbook/save-message!` function we defined above when the form post happens:

```clojure
(defn page-routes [base-path]
  [base-path
   ["/" {:get home}]
   ["/save-message" {:post guestbook/save-message!}]])
```

Now that we have our controllers setup, let's open `home.html` template located under the `resources/html` directory. Currently, it simply renders a static page. We'll update our `content` div to iterate over the messages and print each one in a list:

```xml
<div class="content container">
  <div class="columns">
    <div class="column">
      <h3>Messages</h3>
      <ul class="messages">
        {% for item in messages %}
        <li>
          <time>{{item.timestamp}}</time>
          <p>{{item.message}}</p>
          <p> - {{item.name}}</p>
        </li>
        {% endfor %}
      </ul>
    </div>
  </div>
</div>
```

As you can see above, we use a `for` iterator to walk the messages.
Since each message is a map with the message, name, and timestamp keys, we can access them by name.

Finally, we'll create a form to allow users to submit their messages. We'll populate the name and message values if they're supplied and render any errors associated with them. Note that the forms also uses the `csrf-field` tag that's required for cross-site request forgery protection.

```xml
<div class="columns">
  <div class="column">
      {% if errors.unknown %}
      <div class="notification is-danger">{{errors.unknown}}</div>
      {% endif %}
      <form method="POST" action="/save-message">
          {% csrf-field %}
          <p>
              Name:
              <input class="input" type="text" name="name" value="{{name}}" />
          </p>
          {% if errors.name %}
          <div class="notification is-danger">{{errors.name}}</div>
          {% endif %}
          <p>
              Message:
              <textarea class="textarea" name="message">{{message}}</textarea>
          </p>
          {% if errors.message %}
          <div class="notification is-danger">{{errors.message}}</div>
          {% endif %}
          <input type="submit" class="button is-primary" value="comment" />
      </form>
  </div>
</div>
```

Our final `content` div should look as follows:

```xml
<div class="content container">
  <div class="columns">
      <div class="column">
          <h3>Messages</h3>
          <ul class="messages">
              {% for item in messages %}
              <li>
                  <time>{{item.timestamp}}</time>
                  <p>{{item.message}}</p>
                  <p> - {{item.name}}</p>
              </li>
              {% endfor %}
          </ul>
      </div>
  </div>
  <div class="columns">
      <div class="column">
          {% if errors.unknown %}
          <div class="notification is-danger">{{errors.unknown}}</div>
          {% endif %}
          <form method="POST" action="/save-message">
              {% csrf-field %}
              <p>
                  Name:
                  <input class="input" type="text" name="name" value="{{name}}" />
              </p>
              {% if errors.name %}
              <div class="notification is-danger">{{errors.name}}</div>
              {% endif %}
              <p>
                  Message:
                  <textarea class="textarea" name="message">{{message}}</textarea>
              </p>
              {% if errors.message %}
              <div class="notification is-danger">{{errors.message}}</div>
              {% endif %}
              <input type="submit" class="button is-primary" value="comment" />
          </form>
      </div>
  </div>
</div>
```

Our site should now be functional, but it looks a little bland. Let's add a bit of style to it using [Bulma CSS framework](https://bulma.io/). We'll add the following reference in the `head` of our template:

```xml
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css">
```

Finally, we'll update the `screen.css` file located in the `resources/public/css` folder to format our form nicer:

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

Tests are found under the `test` source path. 

We can now run `clj -M:test` in the terminal to see that our database interaction works
as expected.

## Packaging the application

The application can be packaged for standalone deployment by running the following command:

clj -Sforce -T:build all

This will create a runnable jar that can be run as seen below:

```
export JDBC_URL="jdbc:sqlite:guestbook_dev.db"
java -jar target/guestbook-standalone.jar
```

Note that we have to supply the `JDBC_URL` environment variable when running as a jar, as
it's not packaged with the application.

***

Complete source listing for the tutorial is available [here](https://github.com/kit-clj/examples/tree/master/guestbook).
