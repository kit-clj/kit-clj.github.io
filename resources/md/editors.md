### Configuring Emacs and CIDER for Kit

In order to pick up correct source paths during development, CIDER needs a `.dir-locals.el` file in the root directory containing the following:

```
((clojure-mode . ((cider-preferred-build-tool . clojure-cli)
(cider-clojure-cli-aliases . ":dev:test"))))
```
