## Snippets

Snippets are code templates that can be searched and generated from the REPL. They work like a curated, project-aware
Stack Overflow: you describe what you need, find a matching snippet, and generate the boilerplate code with your own arguments.

Snippets complement [modules](/docs/modules.html) but serve a different purpose. Modules install libraries, add
configuration, and wire components into your project. Snippets generate standalone code fragments that you copy into
your source files.

### Setting up snippets

Snippets are managed via git repositories, similar to modules. To use snippets, add a `:snippets` key to your `kit.edn`:

```clojure
{:ns-name   "myapp"
 :modules   {:root         "modules"
             :repositories [{:url  "https://github.com/kit-clj/modules.git"
                             :tag  "master"
                             :name "kit-modules"}]}
 :snippets  {:root         "snippets"
             :repositories [{:url  "https://github.com/your-org/your-snippets.git"
                             :tag  "master"
                             :name "my-snippets"}]}}
```

Then sync snippets from the REPL:

```clojure
user=> (kit/sync-snippets)
:done
```

### Using snippets

**List all available snippets:**

```clojure
user=> (kit/list-snippets)
:kit/routing
:done
```

**Search for snippets by keyword** (uses fuzzy matching on tags and names):

```clojure
user=> (kit/find-snippets "route")

snippet: :kit/routing
 generates a route definition
takes path and method as arguments
:done
```

**Generate code from a snippet** by passing the snippet ID and its arguments:

```clojure
user=> (kit/snippet :kit/routing "/api/users" :get)
["/api/users"
 {:get
  {:summary "TODO"
   :parameters {}
   :responses {200 {:body map?}}
   :handler
   (fn [request]
     {:body "myapp"})}}]
```

The returned value is a Clojure data structure that you can paste into your route definitions.

### Writing custom snippets

A snippet is a Markdown file with three sections: **tags**, **description**, and **code**.

Here is an example snippet that generates a Reitit route:

```markdown
## tags
reitit routing routes

## description

generates a route definition
takes path and method as arguments

arguments are path string and method keyword

generates a route, e.g:
["/foo" {:get ...}]

## code

```clojure
  ["<<path>>"
   {<<method>>
    {:summary "TODO"
     :parameters {}
     :responses {200 {:body map?}}
     :handler
     (fn [request]
       {:body "<<ns>>"})}}]
```
```

#### Snippet structure

- **tags** - Space-separated keywords used for fuzzy search matching
- **description** - Human-readable explanation of what the snippet does and what arguments it expects
- **code** - A fenced Clojure code block (`` ```clojure ``) containing the template

#### Template variables

Snippet templates use `<<variable-name>>` syntax for arguments. When you call `(kit/snippet :my/snippet "arg1" "arg2")`,
the arguments are matched positionally to the template variables found in the code block.

The following context variables are available automatically:

- `<<ns>>` / `<<ns-name>>` - The project namespace from `kit.edn`
- `<<sanitized>>` - The sanitized project name (used in file paths)
- `<<name>>` - The project name

#### Organizing snippets

Snippet files live in a git repository. The file name and parent directory determine the snippet ID:

| File path | Snippet ID |
|---|---|
| `kit/routing.md` | `:kit/routing` |
| `myapp/db-query.md` | `:myapp/db-query` |

File names are automatically converted to kebab-case (underscores become hyphens, camelCase is split).

To share snippets across projects, host them in a git repository and reference it in `kit.edn` under `:snippets :repositories`.

### API reference

| Function | Description |
|---|---|
| `(kit/sync-snippets)` | Download/update snippet repositories |
| `(kit/list-snippets)` | List all available snippet IDs |
| `(kit/find-snippets "query")` | Fuzzy search snippets by name or tags |
| `(kit/snippet :id & args)` | Generate code from a snippet with the given arguments |
