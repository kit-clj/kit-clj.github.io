ClojureScript is an excellent alternative to JavaScript for client side application logic. Some of the advantages of using ClojureScript include:

* use the same language on both the client and the server
* share common code between the front-end and back-end
* cleaner and more consistent language
* immutable data structures
* powerful standard library

### Adding ClojureScript Support

ClojureScript support can be added via the official `:kit/cljs` module. Simply run `(kit/install-module :kit/cljs)` in order to add the assets. this will add support for compiling ClojureScript using [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html).

### Managing JavaScript and ClojureScript dependencies

#### NPM modules

NPM is used to manage JavaScript modules used in the project. Make sure that you have NPM tools installed for doing that. When the module is added, a `package.json` file
will be created with the following content:

```
{
  "devDependencies": {
    "shadow-cljs": "^2.14.3"
  },
  "dependencies": {
    "react": "^17.0.2",
    "react-dom": "^17.0.2"
  }
}
```

Make sure to run `npm install` in order to install the modules above before starting shadow-cljs compiler.

#### ClojureScript libraries

ClojureScript libraries are managed using the `:dependencies` key in the `shadow-cljs.edn`. The module will have generated the following content for this file:

```clojure
{:nrepl {:port 7002}
 :source-paths ["src/cljs"]
 :dependencies [[binaryage/devtools "1.0.3"]
                [nrepl "0.8.3"]
                [reagent "1.1.0"]
                [cljs-ajax "0.8.4"]]
 :builds       {:app {:target     :browser
                      :output-dir "target/classes/cljsbuild/public/js"
                      :asset-path "/js"
                      :modules    {:app {:entries [kit.guestbook.app]}}
                      :devtools   {:after-load kit.guestbook.core/mount-root}}}}
```
### Running the Compiler

The easiest way to develop ClojureScript applications is to run the compiler in `watch` mode. This way any changes you make in your namespaces will be recompiled automatically and become immediately available on the page. To start the compiler in this mode simply run:

```
npx shadow-cljs watch app
```

This will start shadow-cljs compiler and connect a browser REPL. Any changes you make in ClojureScript source will now be automatically reloaded on the page.

ClojureScript will be compiled with production settings when the `uberjar` task is run. This task will run the following function to compile ClojureScript for release:

```
 (defn build-cljs []
   (println "npx shadow-cljs release app...")
   (let [{:keys [exit], :as s} (sh "npx" "shadow-cljs" "release" "app")]
     (when-not (zero? exit) (throw (ex-info "could not compile cljs" s)))))
 ```

### shadow-cljs with nREPL

To connect your editor to a ClojureScript REPL make sure that you have the `:nrepl` key in `shadow-cljs.edn`. This key defaults to port `7002`. When the compiler starts, it will open nREPL on the specified port.

Once you run `npx shadow watch app`, then you'll be able to connect to its nREPL at `localhost:7002`. Once connected, you simply have to run `(shadow.cljs.devtools.api/repl :app)` and the ClojureScript nREPL will become available. You can test that everything is working correctly by running `(js/alert "Hi")` in the REPL. This should pop up an alert in the browser.

To exit the ClojureScript nREPL you have to run `:cljs/quit` in the nREPL.

### Interacting with JavaScript

All the global JavaScript functions and variables are available via the `js` namespace.

#### Method Calls

```clojure
(.method object params)

(.log js/console "hello world!")
```

#### Accessing Properties

```clojure
(.-property object)

(.-style div)
```

#### Setting Properties

```clojure
(set! (.-property object))

(set! (.-color (.-style div) "#234567"))
```

For more examples of ClojureScript synonyms of common JavaScript operations see the [ClojureScript Synonyms](http://kanaka.github.io/clojurescript/web/synonym.html).

### Reagent

[Reagent](http://reagent-project.github.io/) is the recommended approach for building ClojureScript applications with Kit.

Reagent is backed by [React](http://facebook.github.io/react/) and provides an extremely efficient way to manipulate the DOM using [Hiccup](https://github.com/weavejester/hiccup) style syntax. In Reagent, each UI component is simply a data structure that represents a particular DOM element. By taking a DOM-centric view of the UI, Reagent makes writing composable components simple and intuitive.

A simple Reagent component looks as follows:

```clojure
[:label "Hello World"]
```

Components can also be functions:

```clojure
(defn label [text]
  [:label text])
```

The values of the components are stored in Reagent atoms. These atoms behave just like regular Clojure atoms, except for one important property. When an atom is updated it causes any components that dereference it to be repainted. Let's take a look at an example.

**Important:** Make sure that you require Reagent atom in the namespace, otherwise regular Clojure atoms will be used and components will not be repainted on change.

```clojure
(ns myapp
  (:require [reagent.core :as reagent]))

(def state (reagent/atom nil))

(defn input-field [label-text]
  [:div
    [label label-text]
    [:input {:type "text"
             :value @state
             :on-change #(reset! state (-> % .-target .-value))}]])
```

Above, the `input-field` component consists of a `label` component we defined earlier and an `:input` component. The input will update the `state` atom and render it as its value.

Notice that even though `label` is a function we're not calling it, but instead we're putting it in a vector. The reason for this is that we're specifying the component hierarchy. The components will be run by Reagent when they need to be rendered.

This is behavior makes it trivial to implement the [React Flux](http://facebook.github.io/react/docs/flux-overview.html) pattern.

```
Views--->(actions) --> Dispatcher-->(callback)--> Stores---+
Ʌ                                                          |
|                                                          V
+--(event handlers update)--(Stores emit "change" events)--+
```

Our view components dispatch updates to the atoms, which represent the stores. The atoms in turn notify any components that dereference them when their state changes.

In the previous example, we used a global atom to hold the state. While it's convenient for small applications this approach doesn't scale well. Fortunately, Reagent allows us to have localized states in our components. Let's take a look at how this works.

```clojure
(defn input-field [label-text id]
  (reagent/with-let [value (reagent/atom nil)]
    [:div
     [label "The value is: " @value]
      [:input {:type "text"
               :value @value
               :on-change #(reset! value (-> % .-target .-value))}]]))
```

All we have to do is create a local binding for the atom inside a closure. The returned function is what's going to be called by Reagent when the value of the atom changes.

Finally, rendering components is accomplished by calling the `render-component` function:

```clojure
(defn render-simple []
  (reagent/render-component [input-field] (.-body js/document))
```

### Client Side Routing

Reitit is used to handle both client and server routes.
We'd need to require Reitit in the routing namespace along
with Google Closure history and events helpers.

```clojure
(ns <app>.core
 (:require
  [reagent.core :as r]
  [reitit.core :as reitit]
  [goog.events :as events]
  [goog.history.EventType :as HistoryEventType])
 (:import goog.History))
```

We'll now add a session atom to hold the selected page along with a couple of pages:

```clojure
(def session (r/atom {:page :home}))

(defn home-page []
  [:div "Home"])
  
(defn about-page []
  [:div "About"])

(def pages
  {:home #'home-page
   :about #'about-page})
```

We can now create a `page` function that will check the state of the session and render
the appropriate page:

```clojure
(defn page []
  [(pages (:page @session))])
```

We can now add a route that will dispatch the key associated with each page when the route is selected:

```clojure
(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]]))     
```

Finally, we'll add functions to match routes and hook into browser navigation:

```clojure
(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))
```

When the `hook-browser-navigation!` is called it will hook into page events and call the `match-route` function
when the page navigation event is dispatched.

Please refer to the [official documentation](https://metosin.github.io/reitit/) for further details.

### Ajax

ClojureScript module uses [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) for handling Ajax operations.

#### ajax-request

The `ajax-request` is the base request function that accepts the following parameters:

* uri - the URI for the request
* method - a string representing the HTTP request type, eg: "PUT", "DELETE", etc.
* format - a keyword indicating the response format, can be either `:raw`, `:json`, `:edn`, or `:transit` and defaults to `:transit`
* handler - success handler, a function that accepts the response as a single argument
* error-handler - error handler, a function that accepts a map representing the error with keys `:status` and `:status-text`
* params - a map of params to be sent to the server

#### GET/POST helpers

The `GET` and `POST` helpers accept a URI followed by a map of options:

* `:handler` - the handler function for successful operation should accept a single parameter which is the deserialized response
* `:error-handler` - the handler function for errors, should accept a map with keys `:status` and `:status-text`
* `:format` - the format for the request can be either `:raw`, `:json`, `:edn`, or `:transit` and defaults to `:transit`
* `:response-format` - the response format. If you leave this blank, it will detect the format from the Content-Type header
* `:params` - a map of parameters that will be sent with the request
* `:timeout` - the ajax call's timeout. 30 seconds if left blank
* `:headers` - a map of the HTTP headers to set with the request
* `:finally` - a function that takes no parameters and will be triggered during the callback in addition to any other handlers


```clojure
(ns foo
  (:require [ajax.core :refer [GET POST]]))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console
    (str "something bad happened: " status " " status-text)))

(GET "/hello")

(GET "/hello" {:handler handler})

(POST "/hello")

(POST "/send-message"
        {:headers {"Accept" "application/transit+json"}
         :params {:message "Hello World"
                  :user    "Bob"}
         :handler handler
         :error-handler error-handler})
```

In the example above, the `handler` will be invoked when the server responds with a success status. The response handler function should accept a single parameter. The parameter will contain the deserialized response from the server.

The library attempts to automatically discover the encoding based on the response headers, however the response format can be specified explicitly using the `:response-format` key.

The `error-handler` function is expected to to accept a single parameter that contains the error response. The function will receive the entire response map that contains the status and the description of the error along with any data returned by the server.

* `:status` - contains the HTTP status code
* `:status-text` - contains the textual description of the status
* `:original-text` - contains the server response text
* `:response` - contains the deserialized response when if deserialization was successful

When no handler function is supplied then no further action is taken after the request is sent to the server.

The request body will be interpreted using the [ring-middleware-format](https://github.com/ngrunwald/ring-middleware-format) library. The library will deserialize the request based on the `Content-Type` header and serialize the response using the `Accept` header that we set above.

The route should simply return a response map with the body set to the content of the response:

```clojure
(ns <app>.routes.services
  (:require
   [ring.util.response :refer [response status]]))

(defn save-message! [{:keys [params]}]
  (println params)
  (response {:status :success}))

(defn service-routes []
  [""
   ["/send-message" {:post save-message!}]])
```

Note that CSRF middleware is enabled by default. The middleware wraps the `home-routes` of
your application. It will intercept any request to the server that isn't a `HEAD` or `GET`.

```clojure
(defn home-routes [base-path]
  [base-path
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]])
```

We would now need to pass the CSRF token along with the request. One way to do this is to pass the token in the `x-csrf-token` header in the request with the value of the token.

To do that we'll first need to set the token as a hidden field on the page:

```xml
<input id="csrf-token" type="hidden" value="{{csrf-token}}"></input>
```

Then we'll have to set the header in the request:

```clojure
(POST "/send-message"
        {:headers {"Accept" "application/transit+json"
                   "x-csrf-token" (.-value (.getElementById js/document "csrf-token"))}
         :params {:message "Hello World"
                  :user    "Bob"}
         :handler handler
         :error-handler error-handler})
```



