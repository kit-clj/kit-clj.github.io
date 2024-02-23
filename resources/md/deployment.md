## Running Standalone

To create a standalone executable for your application simply run

```bash
clj -Sforce -T:build all
```

The resulting `jar` can be found in the `target/uberjar` folder. It can be run as follows:

```bash
java -jar <app>.jar
```

To specify a custom port you need to set the `$PORT` environment variable, eg:

```
export PORT=8080
java -jar <app>.jar
```

## VPS Deployment

Virtual Private Servers (VPS) such as [DigitalOcean](https://www.digitalocean.com/) provide a cheap hosting option for Clojure applications. 

Follow [this guide](https://www.digitalocean.com/community/tutorials/how-to-create-your-first-digitalocean-droplet-virtual-server) in order to setup your DigitalOcean server. Once the server is created you can install Ubuntu [as described here](https://www.digitalocean.com/community/tutorials/initial-server-setup-with-ubuntu-12-04). Finally, install Java on your Ubuntu instance by following [these instructions](https://help.ubuntu.com/community/Java). The instructions below apply for Ubuntu 15.04 and newer.

The most common approach is to run the `uberjar` and front it using [Nginx](http://wiki.nginx.org/Main).

### Application deployment

In this step, we will deploy your application to the server, and make sure that it is started automatically on boot. We use `systemd` for this.
Create a `deploy` user that will run your application:

```
sudo adduser -m deploy
sudo passwd -l deploy
```

Create a directory for your application on the server such as `/var/myapp` then upload your application to the server using `scp`:

```
$ scp myapp.jar user@<domain>:/var/myapp/
```

You should now test that you're able to run the application. Connect to the server using `ssh` and run the application:

```
java -jar /var/myapp/myapp.jar
```

If everything went well, your application now runs locally. The following command will confirm that the applications runs as expected:
```
curl http://127.0.0.1:3000/
```
Your application should also now be accessible on the server at `http://<domain>:3000`. If your application is not accessible make sure that the firewall is configured to allow access to the port. Depending on your VPS provider, you may need to create an access point for the port 3000.
* [Creating access point on Azure](https://azure.microsoft.com/en-us/documentation/articles/virtual-machines-set-up-endpoints/)
* [Creating access point on Amazon EC2](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html#adding-security-group-rule)

### systemd start configuration

Now, let's stop the application instance and create a `systemd` configuration to manage its lifecycle, especially taking care that the application will be launched on server boot.
Create the file `/lib/systemd/system/myapp.service` with the following content:

```
[Unit]
Description=My Application
After=network.target

[Service]
WorkingDirectory=/var/myapp
EnvironmentFile=-/var/myapp/env
Environment="DATABASE_URL=jdbc:postgresql://localhost/app?user=app_user&password=secret"
ExecStart=/usr/bin/java -jar /var/myapp/myapp.jar
User=deploy

[Install]
WantedBy=multi-user.target
```

The `WantedBy=` is the target level that this unit is a part of. To find the default run level for your system run:

    systemctl get-default

Note that by default JVM is fairly aggressive about memory usage. If you'd like to reduce the amount of memory used then you can add the following line under the `[Service]` configuration:

```
[Service]
...
_JAVA_OPTIONS="-Xmx256m"
ExecStart=/usr/bin/java -jar /var/myapp/myapp.jar
```

This will limit the maximum amount of memory that the JVM is allowed to use.  Now we can tell `systemd` to start the application everytime the system reboots with the following commands:
```
sudo systemctl daemon-reload
sudo systemctl enable myapp.service
```

When the system reboots your application will now start and will be ready to process requests. You may want to test that. Simply reboot your machine, and check the running processes:
```
 ps -ef | grep java
```
This should return something like the line below. Pay attention to the `UID` - it should be `deploy`, since running it as `root` would present a significant security risk.
```
deploy     730     1  1 06:45 ?        00:00:42 /usr/bin/java -jar /var/mysite/mysite.jar
```

### Fronting with Nginx

Install Nginx using the following command:

```
$ sudo apt-get install nginx
```

Next, make a backup of the default configuration in `/etc/nginx/sites-available/default` and replace it with a custom configuration file for the application such as:

```
server{
  listen 80 default_server;
  listen [::]:80 default_server ipv6only=on;
  server_name localhost mydomain.com www.mydomain.com;

  access_log /var/log/myapp_access.log;
  error_log /var/log/myapp_error.log;
  
  location / {
    proxy_pass http://localhost:3000/;
    proxy_set_header Host $http_host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_redirect  off;
  }
}
```

Restart Nginx by running:

```
sudo service nginx restart
```

Then test that the application is available at `http://<domain>`.

Optionally, you can configure Nginx to serve static resources for the application. In order to do that you will need to ensure that all static resources are served using a common prefix such as `static`. Next, upload the `resources/public/static` folder from your application to the server to a location such as `/var/myapp/static` by running the following command from the project folder:

```
scp -r resources/public/static user@<domain>:/var/myapp/static
```

Now add the following additional configuration option under the `server` section of the Ngnix configuration above:

```
location /static/ {
    alias /var/myapp/static/;
  }
```

This will cause Nginx to bypass your application for any requests to `http://<domain>/static` and serve them directly instead.

To enable compression make sure the following settings are present in your `/etc/nginx/nginx.conf`:

```
gzip on;
gzip_disable "msie6";

gzip_vary on;
gzip_proxied any;
gzip_comp_level 6;
gzip_buffers 16 8k;
gzip_http_version 1.1;
gzip_types text/plain text/css application/json application/x-javascript text/xml application/xml application/xml+rss text/javascript;">
```

### Setting up SSL

If your site has any user authentication then you will also want to use HTTPS. You will first need to provide a SSL certificate and its key. We'll call these `cert.crt` and `cert.key` respectively.

#### Setting up SSL Certificate using Let's Encrypt

The easiest way to setup SSL is to use [Certbot](https://certbot.eff.org/) and to follow the instructions on the site.

Download the installation tool and generate the certificate using the following commands:

```
git clone https://github.com/certbot/certbot
cd certbot
./certbot-auto certonly --email <you@email.com> -d <yoursite.com> -d <www.yoursite.com> --webroot --webroot-path /var/www/html
```

Optionally, setup a Cron job to automatically update the certificate by updating crontab by running as `root`:

```
su
crontab -e
```
Add the following line:

```
0 0 1,15 * * /path-to-certbot/certbot-auto certonly --keep-until-expiring --email <you@email.com> -d <yoursite.com> -d <www.yoursite.com> --webroot --webroot-path /var/www/html
```

Alternatively, you could use [Acmetool](https://github.com/hlandau/acme) as a comprehensive solution for keeping certificates up to date.

We'll generate a stronger DHE parameter instead of using OpenSSL's defaults, which include a 1024-bit key for the key-exchange:

```
cd /etc/ssl/certs
openssl dhparam -out dhparam.pem 4096
```

There are two options for handling HTTPS connections. You can either configure the HTTP server in the app itself, or front it with Nginx. We'll look at both approaches below.

##### ring-undertow-adapter SSL config

The Kit framework uses [ring-undertow-adapter](https://github.com/luminus-framework/ring-undertow-adapter).
This adapter supports SSL, and it expects a [*keystore*](https://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html) with your certificate and key.

First, use the **keytool** (packaged for your operating system) to convert your LetsEncrypt certificate and key to a `.p12` file.

Once you have the `.p12` keystore, update the Kit app configuration.
Edit `resources/system.edn` and find the value of `:server/http` key.
To this existing map, add the following keys:

```
  :ssl-port 443
  :keystore "/path/to/your/keystore.jks"
  :key-password "password-for-keystore"
```

Then restart the webapp.

##### Nginx SSL config

To use Nginx as your SSL proxy you'll want to update the configuration in `/etc/nginx/sites-available/default` as follows:

```
server {
    listen 80;
    return 301 https://$host$request_uri;
}

server {

    listen 443;
    server_name localhost mydomain.com www.mydomain.com;

    ssl_certificate           /etc/letsencrypt/live/<yoursite.com>/fullchain.pem;
    ssl_certificate_key       /etc/letsencrypt/live/<yoursite.com>/privkey.pem;

    ssl on;
    ssl_prefer_server_ciphers  on;
    ssl_session_timeout        180m;
    ssl_session_cache  builtin:1000  shared:SSL:10m;
    ssl_protocols  TLSv1 TLSv1.1 TLSv1.2;
    ssl_ciphers 'AES256+EECDH:AES256+EDH';
    ssl_dhparam /etc/ssl/certs/dhparam.pem;
    add_header Strict-Transport-Security 'max-age=31536000';

    access_log /var/log/myapp_access.log;
    error_log /var/log/myapp_error.log;

     # If you use websocket over https, add below two lines.
    proxy_set_header Upgrade $http_upgrade; ###
    proxy_set_header Connection "Upgrade";   ###

    location / {

      proxy_set_header        Host $host;
      proxy_set_header        X-Real-IP $remote_addr;
      proxy_set_header        X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header        X-Forwarded-Proto $scheme;

      # Fix the “It appears that your reverse proxy set up is broken" error.
      proxy_pass          http://localhost:3000;
      proxy_read_timeout  90;

      proxy_redirect      http://localhost:3000 https://mydomain.com;
    }
}
```

The above will cause Nginx to redirect HTTP requests to HTTPS and use the provided certificate to serve them.

Finally, configure your firewall to only allow access to specified ports by running the following commands:

```
$ sudo ufw allow ssh
$ sudo ufw allow http
$ sudo ufw allow https
$ sudo ufw enable
```

You can test the SSL configuration using the [SSL Server Test](https://www.ssllabs.com/ssltest/).

## Heroku Deployment

First, make sure you have [Git](http://git-scm.com/downloads) and [Heroku toolbelt](https://toolbelt.heroku.com/) installed, then simply follow the steps below.

Create a production configuration file in `env/prod/resources/config.edn`. This file will provide base configuration in Heroku environment.

```clojure
{:prod true}
```

Optionally, test that your application runs locally:

```
heroku local
```

Now, you can initialize your git repo and commit your application:

```
git init
git add .
git commit -m "init"
```

Create your app on Heroku:

```
heroku create
```

Optionally, create a database for the application:

```
heroku addons:create heroku-postgresql
```

The connection settings can be found at your
[Heroku dashboard](https://dashboard.heroku.com/apps/) under
the add-ons for the app.

Deploy the application:

```
git push heroku master
```

Your application should now be deployed to Heroku!

For further instructions see the [official documentation](https://devcenter.heroku.com/articles/clojure).

## Enabling Socket REPL

Kit comes set up with a socket REPL, which allows connecting to a REPL on the server. This functionality can useful for debugging as well as hotfixing updates in the running application. To configure the REPL port you can set the `REPL_PORT` environment variable to the desired port. By default it is 7000.

```
export REPL_PORT=7001
```

You can also connect your favorite IDE to a remote REPL just as you would connect to a local one.

When running on a remote server it is recommended to forward the REPL port to the local machine using SSH:

```
ssh -L 7001:localhost:7001 remotehost
```

## Resources

* [Deploying Your First Clojure App ...From the Shadows](http://www.braveclojure.com/quests/deploy/) provides an indepth guide for Clojure web application deployment strategies.
