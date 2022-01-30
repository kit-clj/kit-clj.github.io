## Integrant components

### :db.sql/hikari-connection

This component uses [hikari-cp](https://github.com/tomekw/hikari-cp) and [next-jdbc](https://github.com/seancorfield/next-jdbc)to create a pooled connection to your database.

Sample configuration:

```clojure
:db.sql/connection 
#profile {:prod {:jdbc-url      #env JDBC_URL
                 :auto-commit        true
                 :read-only          false
                 :connection-timeout 30000
                 :validation-timeout 5000
                 :idle-timeout       600000
                 :max-lifetime       1800000
                 :minimum-idle       10
                 :maximum-pool-size  10
                 :pool-name          "pool-name"
                 :username           "username"
                 :password           "password"
                 :driver-class-name  "com.mysql.jdbc.Driver"}}
```