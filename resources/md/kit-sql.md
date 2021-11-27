## Integrant components

### :db.sql/connection

This component uses [conman](https://github.com/luminus-framework/conman) to create a pooled connection to your database. It resolves to the conman connection when referenced.

It might be useful if you need to create transactions to reference the connection directly when calling your `query-fn`.

Sample configuration 

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

### :db.sql/migrations

This component uses [migratus](https://github.com/yogthos/migratus) to execute your migrations. It optionally takes a `migrate-on-init?` key that defaults to `true`. When `true` this key ensures your migrations run when the component is initialized. 

The component resolves to the configuration options that are initially passed in.

```clojure
:db.sql/migrations
 {:store                :database
  :migration-dir        "custom-migrations"
  :init-script          "init.sql" ;script should be located in the :migration-dir path
                                   ;defaults to true, some databases do not support
                                   ;schema initialization in a transaction
  :init-in-transaction?  false
  :migration-table-name "foo_bar"
  :db                   {:datasource #ig/ref :db.sql/connection}
  :migrate-on-init?     true}
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