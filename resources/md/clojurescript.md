ClojureScript is an excellent alternative to JavaScript for client-side application logic. Some of the advantages of using ClojureScript include:

* use the same language on both the client and the server
* share common code between the front-end and back-end
* cleaner and more consistent language
* immutable data structures
* powerful standard library

### Quick Start

This section provides a short list of steps that you need to complete to start ClojureScript development with Kit. See the sections below for details.

1. Add the `:kit/cljs` module by running `(kit/install-module :kit/cljs)`. Be sure to first run `(kit/sync-modules)` to download the modules if you haven't done that in your project yet.
2. Restart your application.
3. Install JavaScript dependencies by running `npm install` in your project's root directory.
4. Run the `shadow-cljs` compiler in *watch* mode by executing `npx shadow-cljs watch app`.
5. Connect to you `shadow-cjls` nREPL on port 7002 using your preferred editor.
6. Open your project's root page ([http://localhost:3000](http://localhost:3000) by default) in your preferred browser.
7. In the `shadow-cjls` REPL, run `(shadow.cljs.devtools.api/repl :app)`.
8. Verify that everything is wired correctly by running `(js/alert "Hi")` in your `shadow-cljs` REPL. This should display an alert in your browser window.
9. You can now write your ClojureScript code by editing the `core.cljs` file inside the `src/cljs` directory.

### Adding ClojureScript Support

ClojureScript support can be added via the official `:kit/cljs` module. Run `(kit/sync-modules)`, followed by `(kit/install-module :kit/cljs)` to add the assets. This will add support for compiling ClojureScript using [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html). Be sure to restart the application afterwards.

### Managing JavaScript and ClojureScript dependencies

#### NPM modules

NPM is used to manage JavaScript modules in the project. Make sure that you have NPM installed. Adding the `:kit/cljs` module will create a `package.json` file with the following content:

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

Make sure to run `npm install` to install the modules above before starting the shadow-cljs compiler.

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

The easiest way to develop ClojureScript applications is to run the compiler in `watch` mode. This way any changes you make in your namespaces will be recompiled automatically and become immediately available on the page. 

To start the compiler in this mode, run:

```
npx shadow-cljs watch app
```

This will start shadow-cljs and connect a browser REPL. Any changes you make in ClojureScript source will now be automatically recompiled.

When you run the `uberjar` task, ClojureScript will be compiled with production settings according to the following function from your `build.clj`:

```clojure
 (defn build-cljs []
   (println "npx shadow-cljs release app...")
   (let [{:keys [exit], :as s} (sh "npx" "shadow-cljs" "release" "app")]
     (when-not (zero? exit) (throw (ex-info "could not compile cljs" s)))))
 ```

### shadow-cljs with nREPL

By default, running the `npx shadow-cljs watch app` command will also enable nREPL on port 7002. This is governed by the `:nrepl {:port 7002}` key present in your `shadow-cljs.edn`. 

After running shadow-cljs, connect to nREPL using your preferred editor and run `(shadow.cljs.devtools.api/repl :app)`. You can now test that everything is running correctly by executing `(js/alert "Hi")` in the REPL. This should display an alert in the browser. To exit the ClojureScript nREPL run `:cljs/quit`.

Note that for the JavaScript alert to work, you must have the homepage of your project open in a browser window. Otherwise your REPL will display the following error: `No available JS runtime`. This is because of the code needed in the browser to wire JavaScript runtime with shadow-cljs as explained below. 

Installing the `:kit/cljs` module adds that code to your `home.html`:

```html
  <div id="app"></div>
  <script src="/js/app.js"></script> 
```

The first line indicates the mount point of your [Reagent](#reagent) application, defined in `core.cljs` by default.

The second line ensures the `app.js` file, which contains your ClojureScript code compiled to JavaScript, is loaded when you open this page in your browser. This code is required for your REPL to have the direct connection to your browser window, allowing for interactive coding like with regular Clojure code.

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

Reagent is backed by [React](http://facebook.github.io/react/) and provides an extremely efficient way to manipulate the DOM using [Hiccup](https://github.com/weavejester/hiccup) style syntax. In Reagent, each UI component is a data structure that represents a particular DOM element. By taking a DOM-centric view of the UI, Reagent makes writing composable components simple and intuitive.

A simple Reagent component looks as follows:

```clojure
[:label "Hello World"]
```

Components can also be functions:

```clojure
(defn label [text]
  [:label text])
```

The values of the components are stored in Reagent atoms. These atoms behave just like regular Clojure atoms, except for one important property. When an atom is updated, it causes any components that dereference it to be rerendered. Let's take a look at an example.

**Important:** Make sure that you require Reagent atom in the namespace, otherwise regular Clojure atoms will be used and components will not be rerendered on change.

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

When the `hook-browser-navigation!` is called, it will hook into page events and call the `match-route` function
when the page navigation event is dispatched.

See [Reitit documentation](https://metosin.github.io/reitit/) for further details.

### Ajax

ClojureScript module uses [cljs-ajax](https://github.com/JulianBirch/cljs-ajax) to handle Ajax operations.

#### ajax-request

The `ajax-request` is the base request function that accepts the following parameters:

* uri - the URI for the request
* method - a string representing the HTTP request type, eg: "PUT", "DELETE", etc.
* format - a keyword indicating the response format. Can be either `:raw`, `:json`, `:edn`, or `:transit` and defaults to `:transit`
* handler - success handler, a function that accepts the response as a single argument
* error-handler - error handler, a function that accepts a map representing the error with keys `:status` and `:status-text`
* params - a map of params to be sent to the server

#### GET/POST helpers

The `GET` and `POST` helpers accept a URI followed by a map of options:

* `:handler` - the handler function for successful operation should accept a single parameter which is the deserialized response
* `:error-handler` - the handler function for errors, should accept a map with keys `:status` and `:status-text`
* `:format` - the format for the request can be either `:raw`, `:json`, `:edn`, or `:transit`, and defaults to `:transit`
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

The `error-handler` function is expected to accept a single parameter that contains the error response. The function will receive the entire response map that contains the status and the description of the error along with any data returned by the server.

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

### Websockets

Kit provide support for websockets using an excellent [sente](https://github.com/ptaoussanis/sente) library. To simplify the installation procedure we provide `:kit/sente` module, wich extents existing `:kit/cljs` module to add support for websockets on both server and client.

#### Installation

Installation procedure is the same as for every other kit module. Start the REPL and execute the following:

```clojure
(kit/sync-modules) ;; sync modules from the remote module repository
(kit/list-modules) ;; list available modules
(kit/install-module :kit/sente) ;; install websockets support
```

After that, you need to restart your REPL and your shadow-cljs watch process, and you are ready to go.

#### Server side

Module adds a new route in `<<ns-name>>.web.routes.ws.clj` for handling websockets. Received websocket events are handled using a `on-message` multimethod, which default implementation just logs the event and doesn nothing. There are also two example message handlers.

```clojure
(defmethod on-message :guestbook/echo
  [{:keys [id client-id ?data send-fn] :as message}]
  (let [response "Hello from the server"]
    (send-fn client-id [id response])))
```

Echo handler receives a :guestbook/echo message from the client, and responds to the client that sent the message. Notice the arguents list provided by `sente`, which includes the following:

* `:id` - websocket event id, in this case `:guestbook/echo`
* `:client-id` - id of the connected client. 
* `:?data` - data sent with the event
* `:send-fn` - function to send a message over the socket, provided by sente

For full documentation about keys, please check sente documentation.

Another example functions sends a message through websocket to all connected clients, using `:connected-uids` atom provided as a message key.

```clojure
(defmethod on-message :guestbook/broadcast
  [{:keys [id client-id ?data send-fn connected-uids] :as message}]
  (let [response (str "Hello to everyone from the client " client-id)]
   (doseq [uid (:any @connected-uids)]
     (send-fn uid [id response]))))
```

You are free do implement the message handling for your event and put the wherever in your code.

If you want to add additional processing, like error handling or logging to all event, you can do that inside the `handle-message!` function, which is a general event dispatcher.

#### Client side

The module provides `<<ns-name>>.ws.cljs` file used to connect to the server.

First, you need to require it from your `<<ns-name>>.core.cljs` file.

```clojure
(ns <<ns-name>>.core
  (:require
   [<<ns-name>>.ws :as ws]
   [reagent.core :as r]
   [reagent.dom :as d]))
```

Also, you need to make sure your websockets are initialised and incoming events are handled. For that, you need to define your event handler, and pass it to the websocket initialization function.

```clojure
(defn handler [resp]
  (println "response: " resp))

(defn ^:export ^:dev/once init! []
  (ws/start-router!
   handler)
  ;; (ajax/load-interceptors!)
  (mount-root))
```

After reloading your web page, you will see log entries in your JavaScript console that websocket connection is established.

Sending events through websocket from the client is easy. You just need to call `ws/send-message!` function.

For example, you can modify your `home-page` to render two additional buttons, which will send an even to thes server when clicked. Like this:

```clojure
(defn home-page []
  [:div
   [:h2 "Welcome to Reagent!"]
   [:input {:type :button
            :value "Click to echo"
            :on-click #(ws/send-message! [:guestbook/echo "Hallo Server"])}]
   [:input {:type :button
            :value "Click to broadcast"
            :on-click #(ws/send-message! [:guestbook/broadcast "Hallo everyone"])}]])
```

#### What's next?

We deliberately left event message handling on the client side in this example barebones. It's up to you as a developer to decide how you want to implement it. You may want to use multimethod approach, similar to what we have on the server side. Or, if you are using re-frame, you may want to hook websocket event handling to your reframe message bus.
