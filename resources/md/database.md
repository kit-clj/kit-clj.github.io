## Configuring the Database

Kit has two database paradigms supported as libraries: XTDB (formerly known as Crux), and SQL-style database, however you can easily roll your own connection.

### SQL

Kit defaults to using [Migratus](https://github.com/yogthos/migratus) for SQL database migrations and
[HugSQL](http://www.hugsql.org/) for database interaction.

The migrations and a default connection will be setup when using a database profile such as `+sql`. The default SQL implementation used with this profile is [PostgreSQL](https://www.postgresql.org/), however any SQL solution will work.

#### Configuring Migrations

We first have to set the connection configuration for our database in `system.edn`. This is by default in the `+sql` profile for development `jdbc:postgresql://localhost/<app-name>?user=<app-name>&password=<app-name>`, however, you can change it whatever you like

```clojure
 :db.sql/connection
 #profile {:dev  {:jdbc-url "jdbc:postgresql://localhost/<app-name>?user=<app-name>&password=<app-name>"}
           :test {}
           :prod {:jdbc-url   #env JDBC_URL
                  :init-size  1
                  :min-idle   1
                  :max-idle   8
                  :max-active 32}}
```

Then we can create SQL scripts to migrate the database schema, and to roll it back. These are applied using the numeric order of the ids. Conventionally the current date is used to prefix the filename. The files are expected to be present under the `resources/migrations` folder. The template will generate sample migration files for the users table.

```
resources/migrations/20210720004935-add-users-table.down.sql
resources/migrations/20210720004935-add-users-table.up.sql
```

The default configuration runs any new migrations on startup, but this can be changed by updating the value for `migrate-on-init?`.

```clojure
 :db.sql/migrations
 {:store            :database
  :db               {:datasource #ig/ref :db/connection}
  :migrate-on-init? true}
```

Migrations can also be run via the REPL. The `migratus.core` namespace provides the following
helper functions:

* `(migratus.core/reset-db)` - resets the state of the database
* `(migratus.core/migrate)` - runs the pending migrations
* `(migratus.core/rollback)` - rolls back the last set of migrations
* `(migratus.core/create-migration "add-guestbook-table")` - creates the up/down migration files with the given name

**important**: the database connection must be initialized before migrations can be run in the REPL

Please refer to the [Database Migrations](/docs/migrations.html) section for more details.

### SQL Queries

SQL queries are parsed by HugSQL as defined in your `system.edn` and `resources/queries.sql` file by default. You can update the filename to indicate a different path, e.g. `"sql/queries.sql".

```clojure
:db.sql/query-fn
{:conn     #ig/ref :db.sql/connection
 :options  {}
 :filename "queries.sql"}
```

This integrant component is a reference to a function that executes the SQL query along with any arguments you wish to pass in. For example, let's say we had the following SQL:

```sql
-- :name get-user-by-id :? :1
-- :doc returns a user object by id, or nil if not present
SELECT *
FROM users
WHERE id = :id
```

We could simply query this bit with the following `query-fn` call:

```clojure
(query-fn :get-user-by-id {:id 1})
```

For reference, here is the full definition from the Kit SQL edge:

```clojure
(defmethod ig/init-key :db.sql/query-fn
  [_ {:keys [conn options filename]
      :or   {options {}}}]
  (let [queries (conman/bind-connection-map conn options filename)]
    (fn
      ([query params]
       (conman/query queries query params))
      ([conn query params & opts]
       (apply conman/query conn queries query params opts)))))
```

As you can see from this, the two-arity `query-fn` uses the database that you pass in the initial system configuration. However, the three plus-arity variant allows you to pass in a custom connection, allowing for SQL transactions.


### Working with HugSQL

HugSQL takes the approach similar to HTML templating for writing SQL queries. The queries are written using plain SQL, and the
dynamic parameters are specified using Clojure keyword syntax. HugSQL will use the SQL templates to automatically generate the functions for interacting with the database.

Conventionally the queries are placed in the `resources/sql/queries.sql` file. However, once your application grows you may consider splitting the queries into multiple files.

The format for the file can be seen below:

```sql
-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)
```

The name of the generated function is specified using `-- :name` comment. The name is followed by the command and the result flags.

The following command flags are available:

* `:?` - query with a result-set (default)
* `:!` - any statement
* `:<!` - support for `INSERT ... RETURNING`
* `:i!` - support for insert and jdbc `.getGeneratedKeys`

The result flags are:

* `:1` - one row as a hash-map
* `:*` - many rows as a vector of hash-maps
* `:n` - number of rows affected (inserted/updated/deleted)
* `:raw` - passthrough an untouched result (default)

The query itself is written using plain SQL and the dynamic parameters are denoted by prefixing the parameter name with a colon.

The query functions are generated by calling the the `conman/bind-connection` macro. The macro accepts the connection var
and one or more query files such as the one described above.


```clojure
(conman/bind-connection conn "sql/queries.sql")
```

Note that it's also possible to bind multiple query files by providing additional file names to the `bind-connection` function:

```clojure
(conman/bind-connection conn "sql/user-queries.sql" "sql/admin-queries.sql")
```

Once `bind-connection` is run the query we defined above will be mapped to `myapp.db.core/create-user!` function.
The functions generated by `bind-connection` use the connection found in the `conn` atom by default unless one
is explicitly passed in. The parameters are passed in using a map with the keys that match the parameter names specified:

```clojure
(create-user!
  {:id "user1"
   :first_name "Bob"
   :last_name "Bobberton"
   :email "bob.bobberton@mail.com"
   :pass "verysecret"})
```

The generated function can be run without parameters, e.g:

```clojure
(get-users)
```

It can also be passed in an explicit connection, as would be the case for running in a transaction:

```clojure
(def some-other-conn
  (conman/connect! {:jdbc-url "jdbc:postgresql://localhost/myapp_test?user=test&password=test"}))

(create-user!
  some-other-conn
  {:id "user1"
   :first_name "Bob"
   :last_name "Bobberton"
   :email "bob.bobberton@mail.com"
   :pass "verysecret"})
```

The `conman` library also provides a `with-transaction` macro for running statements within a transaction.
The macro rebinds the connection to the transaction connection within its body. Any SQL query functions
generated by running `bind-connection` will default to using the transaction connection withing the `with-transaction`
macro:

```clojure
(with-transaction [conn]
  (jdbc/db-set-rollback-only! conn)
  (create-user!
    {:id         "foo"
     :first_name "Sam"
     :last_name  "Smith"
     :email      "sam.smith@example.com"})
  (get-user {:id "foo"}))
```

See the [official documentation](http://www.hugsql.org/) for more details.
