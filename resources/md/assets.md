The assets for the application are located under the `resources` folder. Assets include any non-source files that you may
wish to include in the application. These may be database migration files, SQL query files, and so on. 

By default, the `resources/public` folder is reserved for assets that will be served directly by the HTTP server. This is where you would place assets such as images and CSS. This path can be customized should you wish to place publicly available assets under a different directory. This is configured in your `system.edn` under the `:handler/ring` component's `:site-defaults-config` key.

### Caching

Kit sets a `Cache-Control` header on the files served from `resources/public`, so browsers never keep using stale files after a deploy:

* Files with a content hash in their name, such as `app.0123456789ABCDEF0123456789ABCDEF.js`, are served with `Cache-Control: public, max-age=31536000, immutable`. The name changes whenever the content does, so the browser keeps the file without asking the server again.
* Every other file is served with `Cache-Control: no-cache`. The browser keeps its copy but revalidates it on each use, and an unchanged file comes back as a `304 Not Modified` with no body.

This is done by the `wrap-static-cache-control` middleware in `<app>.web.middleware.core`. Responses that already set their own `Cache-Control` are left alone, so you can give a route or a resource a different policy.
