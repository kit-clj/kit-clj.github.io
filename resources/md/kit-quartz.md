The `kit-quartz` library adds [aero](https://github.com/juxt/aero) reader support around the [cronut](https://github.com/troy-west/cronut) library for the [quartz]() scheduler.

## Integrant components

See the [cronut readme](https://github.com/troy-west/cronut) for a full overview of the integration component, jobs, triggers, and more.

### :scheduling.quartz/env-properties

This is a means of setting environment properties during runtime. It is useful in case there's a scenario where you can't (for whatever reason) set secrets in your JVM properties.

For example, let's say you are using MongoDB as your Quartz scheduler cluster and fetching the configuration on production from environment variables. For whatever reason, you cannot simply pipe in these environment variables on startup (maybe some devops restriction, who knows), then you can set it as follows. 

```clojure
:quartz/env-properties
      #profile {:default {}
                :prod    {:org.quartz.jobStore.collectionPrefix "my_mongo_coll_prefix"
                          :org.quartz.jobStore.addresses        #env MONGODB_CLUSTERS
                          :org.quartz.jobStore.username         #env MONGODB_USERNAME
                          :org.quartz.jobStore.password         #env MONGODB_PASSWORD}}
```
