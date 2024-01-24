## Integrant components

### :db.sql/connection

This component uses [conman](https://github.com/luminus-framework/conman) to create a pooled connection to your database. It resolves to the conman connection when referenced. The component accepts the following keys:

* `conn` - database connection string
* `options` - optional map of configuration options (see conman docs for details)
* `filename` - string with the name of the file with SQL queries relative to `resources`
* `filenames` - a vector of strings with the names of the files with SQL queries relative to `resources`

It might be useful if you need to create transactions to reference the connection directly when calling your `query-fn`.

Sample configuration:

```clojure
:db.sql/connection 
#profile {:prod {:jdbc-url   #env JDBC_URL
                 :init-size  1
                 :min-idle   1
                 :max-idle   8
                 :max-active 32}
          :test {:jdbc-url "jdbc:postgresql://localhost/myapp?user=myapp&password=myapp"}
          :dev  {:jdbc-url "jdbc:postgresql://localhost/myapp?user=myapp&password=myapp"}}
```

### :db.sql/query-fn

This component binds a connection map from the SQL scripts file you provide it.

It resolves to a multi-arity `conman/query` function. The two-arity function takes args `query params`, e.g. `((:db.sql/query-fn) :get-user {:id 123})`. The three+ arity function takes a DB connection as the first argument, and optionally vararg opts, i.e. `conn query params & opts`. This could be useful for test transactions, transactional queries.

Sample configuration:

```clojure
:db.sql/query-fn
 {:conn     #ig/ref :db.sql/connection
  :options  {}
  :filename "queries.sql"}
```

Sample configuration with multiple query files:

```clojure
:db.sql/query-fn
 {:conn     #ig/ref :db.sql/connection
  :options  {}
  :filenames ["queries.sql" "other-queries.sql"]}
```
