## Integrant components

### :db.sql/migrations

This component uses [migratus](https://github.com/yogthos/migratus) to execute your migrations. It optionally takes a `migrate-on-init?` key that defaults to `true`. When `true`, this key ensures your migrations run when the component is initialized. 

The component resolves to the configuration options that are initially passed in.

Sample configuration:

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