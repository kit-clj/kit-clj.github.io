## Guestbook Application

This tutorial will guide you through building a simple guestbook application using Kit.
The guestbook allows users to leave a message and to view a list of messages left by others.
The application will demonstrate the basics of HTML templating, database access, and
project architecture.

If you don't have a preferred Clojure editor already, then it's recommended that you use [Calva](https://calva.io/getting-started/) to follow along with this tutorial.

### Quickstart Github Codespaces

You can follow the tutorial using browser based environment by starting a dev container from the following repo [https://github.com/kit-clj/playground/](https://github.com/kit-clj/playground/).

### Installing JDK

Clojure runs on the JVM and requires a copy of JDK to be installed. If you don't
have JDK already on your system then OpenJDK is recommended and can be downloaded
[here](http://www.azul.com/downloads/zulu/). Note that Kit requires JDK 11 or greater to
work with the default settings. Alternatively, follow the instructions for installing packages
on your system.

### Installing a Build Tool

For building and running a project, Kit supports [Clojure Deps and CLI](https://clojure.org/guides/deps_and_cli).
Note that Kit requires tools.build version `1.10.3.933` or later.

To install Clojure CLI, follow the steps below depending on your operating system.

MacOS

```
brew install clojure/tools/clojure
```

Linux

```
curl -L -O https://github.com/clojure/brew-install/releases/latest/download/posix-install.sh
chmod +x posix-install.sh
sudo ./posix-install.sh
```

For both macOS and Linux, you will need [`clj-new`](https://github.com/seancorfield/clj-new) installed as follows:

```
clojure -Ttools install-latest :lib io.github.seancorfield/clj-new :as clj-new
```

For information on customization options, for example on how to change your install location, see [the official docs here](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools).

### Creating a new application

Once you have the Clojure CLI installed, you can run the following commands in your terminal to
initialize your application:

```
clojure -Tclj-new create :template io.github.kit-clj :name kit/guestbook
cd guestbook
```

The above will create a new project, named kit/guestbook, based on the kit-clj template.

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
│   │   │   └── kit
│   │   │       └── guestbook
│   │   │           ├── dev_middleware.clj
│   │   │           └── env.clj
│   │   └── resources
│   │       └── logback.xml
│   └── prod
│       ├── clj
│       │   └── kit
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
│       └── kit
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
        └── kit
            └── guestbook
                └── test_utils.clj
```

Let's take a look at what the files in the root folder of the application do:

* `deps.edn` - used to manage the project configuration and dependencies by deps
* `build.clj` - used to manage the build process by Clojure CLI tools
* `Dockerfile` - used to facilitate Docker container deployments
* `README.md` - where documentation for the application is conventionally put
* `resources/system.edn` - used for system configuration
* `.gitignore` - a list of assets, such as build-generated files, to exclude from Git

### The Source Directory

All our code lives under the `src/clj` folder. Since our application is called kit/guestbook, this
is the root namespace for the project. Let's take a look at all the namespaces that have been created for us.

#### guestbook

* `config.clj` - this is the place where your `system.edn` is read in to create an integrant configuration map
* `core.clj` - this is the entry point for the application that contains the logic for starting and stopping the server

#### guestbook.web

The `web` namespace is used to define the edges of your application that deal with server communication, such as receiving HTTP requests and returning responses.

* `handler.clj` - defines the entry points for routing and request handling.

#### guestbook.web.controllers

The `controllers` namespace is where the controllers are located. By default, a healthcheck controller is created for you. When you add more controllers you should create namespaces for them here.

* `healthcheck.clj` - default controller that returns basic statistics about your server

#### guestbook.web.middleware

The `middleware` namespace consists of functions that implement cross-cutting functionality such as session management, coercion, etc. These functions can be wrapped around groups of routes to provide common functionality.

* `core.clj` - an aggregate of default middlewares and environment-specific middleware
* `exception.clj` - logic for classifying exceptions within controllers and returning appropriate HTTP responses
* `formats.clj` - handles coercion of requests data to Clojure data structures, and response data back to strings

#### guestbook.web.routes

The `routes` namespace is where the HTTP routes are defined. By default `/api` routes are created for you. When you add more routes you should create namespaces for them here.

* `api.clj` - a namespace that routes (default = `/api`) with Swagger UI
* `utils.clj` - general purpose helper functions for getting data from requests

### The Env Directory

Environment-specific code and resources are located under the `env/dev`, `env/test`, and the `env/prod` paths.
The `dev` configuration will be used during development and testing, `test` during testing,
while the `prod` configuration will be used when the application is packaged for production.

#### The Dev Directory

Any source code that's meant to be used during development time is placed in the `dev/clj`. The following namespaces will be generated by default when the project is created:

* `user.clj` - a utility namespace for any code you wish to run during REPL development. You start and stop your server from here during development.
* `guestbook/env.clj` - contains the development configuration defaults
* `guestbook/dev_middleware.clj` - contains middleware used for development that should not be compiled in production

Resources that are used during development are placed in `dev/resources`. By default this folder will contain logback configuration tuned for development:

* `logback.xml` file used to configure the development logging profile

Similarly, testing configuration is placed in the `test/resources`:

* `logback.xml` file used to configure the test logging profile

#### The Prod Directory

This directory is a counterpart of the `dev` directory, and contains versions of namespaces and resources that will be used when the application is built for production. The `prod/clj` folder will contain the following namespace when the project is created:

* `guestbook/env.clj` namespace with the production configuration

Meanwhile, `prod/resources` will contain logback configuration tuned for production use:

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

You can alternatively use `clj -M:dev:cider` if you intend to connect to the REPL from Emacs and [CIDER](https://docs.cider.mx/cider/index.html) (this also works with VS Code and [Calva](https://calva.io)). Or do `clj -M:dev:nrepl` if you want to start [nREPL](https://github.com/nrepl/nrepl) but don't need the CIDER middleware. See the [Guestbook example](https://github.com/kit-clj/demo-guestbook) README for more info on connecting the REPL with a Clojure editor.

Once you are in the REPL, you can start the system by running a command provided in `env/dev/user.clj`

```clojure
(go) ;; To start the system

(halt) ;; To stop the system

(reset) ;; To refresh the system after making code changes
```

To confirm that your server is running, visit [http://localhost:3000/api/health](http://localhost:3000/api/health).

### System

System resources such as HTTP server ports, database connections are defined in the `resources/system.edn` file. For example, this key defines HTTP server configuration such as the host, port, and HTTP handler:

```clojure
:server/http
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

;; This will list the available modules
(kit/list-modules)
;; =>
;; :kit/html - adds support for HTML templating using Selmer
;; :kit/sql - adds support for SQL. Available profiles [ :postgres :sqlite ]. Default profile :sqlite
;; :kit/cljs - adds support for cljs using shadow-cljs
;; nil

;; To be able to serve HTML pages, install the :html module
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
;; updating file: src/clj/kit/guestbook/core.clj
;; applying
;; action: :append-requires
;; value: [[kit.guestbook.web.routes.pages]]
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

The module also generated the namespace `kit.guestbook.web.pages.layout` which helps you render HTML pages using [Selmer templating engine](https://github.com/yogthos/Selmer)

#### Routing

The module also helped generate some routes for us under `kit.guestbook.web.routes.pages`. See [routing](/docs/routes.html) documentation for more details.

#### Adding a database

Similarly to the way we installed the HTML module, we can add a SQL module with SQLite called `:kit/sql`. The default profile includes SQLite out of the box, but if we wanted to be explicit we could also write `(kit/install-module :kit/sql {:feature-flag :sqlite})`

```clojure
(kit/install-module :kit/sql)
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
;; updating file: src/clj/kit/guestbook/core.clj
;; applying
;;  action: :append-requires
;;  value: [[kit.edge.db.sql]]
;; sql installed successfully!
;; restart required!
```

Restart again and create your first database migration.

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
 timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
```

The guestbook table will store all the fields describing the message, such as the name of the
commenter, the content of the message, and a timestamp.
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
### Accessing The Database

The SQL queries are found in the `resources/sql` folder.

* `queries.sql` - defines the SQL queries and their associated function names

Let's take a look at the `queries.sql` template file. Its contents should look as follows:

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

The file initially contains some placeholder queries to help remind you of basic SQL syntax.
As we can see, each function is defined using the comment that starts with `-- :name` followed by the name of the function.
The next comment provides the doc string for the function and finally we have the body that's plain SQL. For full documentation of this syntax you can view the [HugSQL documentation](https://www.hugsql.org/). The parameters are
denoted using `:` notation. Let's replace the existing queries with some of our own:


```sql
-- :name save-message! :! :n
-- :doc creates a new message
INSERT INTO guestbook
(name, message)
VALUES (:name, :message)

-- :name get-messages :? :*
-- :doc selects all available messages
SELECT * FROM guestbook
```

Now that our model is all set up, let's reload the application, and test our queries in the REPL:

```clojure
(reset)

(def query-fn (:db.sql/query-fn state/system))

(query-fn :save-message! {:name      "m1"
                          :message   "hello world"})
;; => 1

(query-fn :get-messages {})
;; => [{:id 1, :name "m1", :message "hello world", :timestamp 1636480432353}]
```

In this example, the newly defined `query-fn` function allows you to execute the SQL functions you defined in `queries.sql`. It achieves this using the `:db.sql/query-fn` component that comes with kit-sql (a dependency of kit/sqlite you installed).

As you can see, `query-fn` takes two arguments: name of the SQL query function to call, and a map of parameters required by that function.

For more information on how components like `:db.sql/query-fn` work, see [Accessing Components](/docs/integrant.html#accessing_components).

### Exposing Database Queries in the Router Component

Now that we've added the queries, we'll need to update `resources/system.edn` to make these queries available in the page router component. To do this, add a `:query-fn` key in the component definition as follows:

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

Like in the REPL example towards the end of the [Accessing the Database](#accessing_the_database) section, the `:db.sql/query-fn` component comes from kit-sql. Unlike in that example:

- we pass specific database connection information
- we only make available the query functions from one specific file.

For more information on how components like `:db.sql/query-fn` work, see [Accessing Components](/docs/integrant.html#accessing_components).

### Creating a controller for the guestbook

We'll create a new controller that will be responsible for saving new messages in the database. Let's create a new namespace called `kit.guestbook.web.controllers.guestbook`, this namespace should be placed in
a corresponding file called `guestbook.clj` under the `src/clj/kit/guestbook/web/controllers/` folder. We'll add the following content to the namespace:

```clojure
(ns kit.guestbook.web.controllers.guestbook
  (:require
   [clojure.tools.logging :as log]
   [kit.guestbook.web.routes.utils :as utils]
   [ring.util.http-response :as http-response]))

(defn save-message!
  [{:keys [query-fn]} {{:strs [name message]} :form-params :as request}]
  (log/debug "saving message" name message)
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
          (assoc :flash {:errors {:unknown (.getMessage e)}})))))
```

As you can see, the namespace contains a `save-message!` function that executes the query to add a new message to the guestbook table. The query is accessed from the first argument which is the Integrant system map that's passed to the handler. The `query-fn` key contains a map of the query functions. The names of these functions are inferred from the `-- :name` comments in the SQL templates found in the `resources/sq/queries.sql` file.

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
(defn home [{:keys [query-fn]} {:keys [flash] :as request}]
  (layout/render request "home.html" {:messages (query-fn :get-messages {})
                                      :errors (:errors flash)}))```

The function now renders the `home.html` template, and passes into it the messages from the database (using the `:messages` key), and any errors (using the `:errors` key).

Finally, we'll add the `/save-message` route in the `page-routes` function. This route will pass the request to the `guestbook/save-message!` function we defined above when the form post happens:

```clojure
(defn page-routes [opts]
  [["/" {:get (partial home opts)}]
   ["/save-message" {:post (partial guestbook/save-message! opts)}]])
```

Now that we have our controllers set up, let's open `home.html` template located in the `resources/html` directory. Currently, it simply renders a static page. We'll update our `content` div to iterate over the messages and print each one in a list:

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

As you can see above, we use a `for` iterator to walk through the messages.
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
              <label>
              Name:
              <input class="input" type="text" name="name" value="{{name}}" />
              </label>
          </p>
          {% if errors.name %}
          <div class="notification is-danger">{{errors.name}}</div>
          {% endif %}
          <p>
              <label>
              Message:
              <textarea class="textarea" name="message">{{message}}</textarea>
              </label>
          </p>
          {% if errors.message %}
          <div class="notification is-danger">{{errors.message}}</div>
          {% endif %}
          <input type="submit" class="button is-primary is-outlined has-text-dark" value="comment" />
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
                  <label>
                  Name:
                  <input class="input" type="text" name="name" value="{{name}}" />
                  </label>
              </p>
              {% if errors.name %}
              <div class="notification is-danger">{{errors.name}}</div>
              {% endif %}
              <p>
                  <label>
                  Message:
                  <textarea class="textarea" name="message">{{message}}</textarea>
                  </label>
              </p>
              {% if errors.message %}
              <div class="notification is-danger">{{errors.message}}</div>
              {% endif %}
              <input type="submit" class="button is-primary is-outlined has-text-dark" value="comment" />
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

To learn more about HTML templating options you can use with Kit, see [HTML Templating](/docs/html_templating.html).

## Adding some tests

Tests are found under the `test` source path.

We can now run `clj -M:test` in the terminal to see that our database interaction works
as expected.

## Packaging the application

You can package your application for standalone deployment by running the following command:

clj -Sforce -T:build all

This will create a runnable jar that you can run using the following commands:

```
export JDBC_URL="jdbc:sqlite:guestbook_dev.db"
java -jar target/guestbook-standalone.jar
```

Note that we have to supply the `JDBC_URL` environment variable when running as a jar, as
it's not packaged with the application.

***

Complete source listing for the tutorial is available [here](https://github.com/kit-clj/examples/tree/master/guestbook).
