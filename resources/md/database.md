## Configuring the Database

Kit has two database paradigms supported as libraries: XTDB (formerly known as Crux), and SQL-style database, however you can easily roll your own connection.

### SQL

Kit defaults to using [Migratus](https://github.com/yogthos/migratus) for SQL database migrations and
[HugSQL](http://www.hugsql.org/) for database interaction.

The migrations and a default connection will be set up when using a database profile such as `+sql`. The default SQL implementation used with this profile is [PostgreSQL](https://www.postgresql.org/), however any SQL solution will work.

#### Configuring Migrations

To start running migrations, you first have to configure the database connection in `system.edn`. In the `+sql` profile, for a development database, this is done through a connection string, like `jdbc:postgresql://localhost/<app-name>?user=<app-name>&password=<app-name>`. You can adjust it as necessary.

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

You can then create SQL scripts to migrate the database schema, and to roll the migration back. Migrations are applied using the numeric order of ids. Conventionally, the current date is used to prefix the filename. The files are expected to be present under `resources/migrations`. The template will generate sample migration files for the users table.

```
resources/migrations/20210720004935-add-users-table.down.sql
resources/migrations/20210720004935-add-users-table.up.sql
```

The default configuration runs any new migrations on startup. You can change this by modifying the value for `migrate-on-init?` to `false`.

```clojure
 :db.sql/migrations
 {:store            :database
  :db               {:datasource #ig/ref :db/connection}
  :migrate-on-init? true}
```

You can also run the migrations via the REPL. The `migratus.core` namespace provides the following
helper functions:

* `(migratus.core/reset (:db.sql/migrations state/system))` - resets the state of the database by rolling back all the applied migrations (by using the appropriate down-scripts), and running all migrations (up-scripts)
* `(migratus.core/migrate (:db.sql/migrations state/system))` - runs the pending migrations
* `(migratus.core/rollback (:db.sql/migrations state/system))` - rolls back the last set of migrations
* `(migratus.core/create (:db.sql/migrations state/system) "add-guestbook-table")` - creates the up/down migration files with the given name

**Important**: the database connection must be initialized before migrations can be run in the REPL

### SQL Queries

SQL queries are parsed by HugSQL as defined in your `system.edn` and `resources/queries.sql` file by default. You can update the filename to indicate a different path, e.g. `"sql/queries.sql".

```clojure
:db.sql/query-fn
{:conn     #ig/ref :db.sql/connection
 :options  {}
 :filename "queries.sql"}
```

This Integrant component is a reference to a function that executes the SQL query along with any arguments you wish to pass in. For example, let's say you have following SQL queries defined:

```sql
-- :name get-user-by-id :? :1
-- :doc returns a user object by id, or nil if not present
SELECT *
FROM users
WHERE id = :id

-- :name add-user! :n
insert into users
(id, password)
values (:id, :password)
```

You can run this SQL query using the following `query-fn` call:

```clojure
(query-fn :get-user-by-id {:id 1})
```

To run queries in a transaction you have to use `next.jdbc/with-transaction` as follows:

```clojure
(let [conn (:db.sql/connection system)]
  (next.jdbc/with-transaction [tx conn]
    (query-fn tx :add-user! {:id "foo" :password "secret"})
    (query-fn tx :get-user-by-id {:id "foo"})))
```

Note that you must use `tx` connection created by `with-transaction` in order for the query to be considered within the scope of the transaction. Please see official [nex.jdbc](https://github.com/seancorfield/next-jdbc/blob/develop/doc/transactions.md) documentation on transactions for further examples.

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

As you can see, the two-arity `query-fn` uses the database that you pass in the initial system configuration. However, the three plus-arity variant allows you to pass in a custom connection, allowing for SQL transactions.


### Working with HugSQL

HugSQL takes the approach similar to HTML templating for writing SQL queries. The queries are written using plain SQL, and the
dynamic parameters are specified using Clojure keyword syntax. HugSQL will use the SQL templates to automatically generate the functions for interacting with the database.

Conventionally, the queries are placed in the `resources/sql/queries.sql` file. However, once your application grows you may consider splitting the queries into multiple files.

You can see the format of an example SQL function below:

```sql
-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)
```

You specify the name of the generated function using the `-- :name` comment. The name is followed by the command and the result flags.

The following command flags are available:

* `:?` - query with a result-set (default)
* `:!` - any statement
* `:<!` - support for `INSERT ... RETURNING`
* `:i!` - support for insert and jdbc `.getGeneratedKeys`

The result flags are:

* `:1` - one row as a hash-map
* `:*` - many rows as a vector of hash-maps
* `:n` - number of rows affected (inserted/updated/deleted)
* `:raw` - pass through an untouched result (default)

The query itself is written using plain SQL and the dynamic parameters are denoted by prefixing the parameter name with a colon.

#### Debugging HugSQL queries

The following code illustrates how to use `hugsql.core/hugsql-command-fn` multimethod to log the query that's being generated:

```clojure

(defn log-sqlvec [sqlvec] 
  (log/info (->> sqlvec
                 (map #(clojure.string/replace (or % "") #"\n" ""))
                 (clojure.string/join " ; "))))

(defn log-command-fn [this db sqlvec options]
  (log-sqlvec sqlvec)
  (condp contains? (:command options)
    #{:!} (hugsql.adapter/execute this db sqlvec options)
    #{:? :<!} (hugsql.adapter/query this db sqlvec options)))

(defmethod hugsql.core/hugsql-command-fn :! [_sym] `log-command-fn)
(defmethod hugsql.core/hugsql-command-fn :<! [_sym] `log-command-fn)
(defmethod hugsql.core/hugsql-command-fn :? [_sym] `log-command-fn)
```

See the [official documentation of HugSQL](http://www.hugsql.org/) for more details.
