# NGINX

## 什么是正向代理和反向代理？

**正向代理**代替客户端发送请求，正向代理服务器和客户端对外表现为一个客户端，所以正向代理隐藏了真实的客户端；

正向代理服务器有客户端缺少的功能，比如可以上网、翻墙等等。假如公司服务器的软件在内网部署访问不了internet,就可以配置一台正向代理服务器,通过正向代理服务器上网。

**反向代理**代替服务端接受请求，反向代理服务器和真实服务器对外表现为一个服务端，所以反向代理服务器隐藏真实的服务端。

在高并发场景下，一个tomcat服务器可能承受不了那么高的并发量和访问量,所以需要多个服务器分担这个工作,而nginx在高并发的场景下表现是尤为突出的,此时nginx就可以代理多个服务器去接收用户请求,最后交给其中一个服务器处理。

代理服务器还是那个代理服务器，如果替客户端干活就是正向代理，如果替服务端干活就是反向代理。

## 实践

拉取镜像：

```bash
[root@iZ2vc7dxtwpmtamdh6hw0yZ mydata]# docker images
REPOSITORY                                      TAG          IMAGE ID       CREATED         SIZE
registry.openanolis.cn/openanolis/nginx-accel   1.22.1-23    2905ea50a5ff   15 months ago   378MB
registry.openanolis.cn/openanolis/mysql         8.0.30-8.6   f74177ebc092   24 months ago   814MB
```

指定配置文件启容器。

```bash
docker run -d --name game-info-nginx -p 80:80 \
-v /mydata/NGINX/nginx.conf:/etc/nginx/nginx.conf:ro \
registry.openanolis.cn/openanolis/nginx-accel:1.22.1-23
```

- `-d`：表示在后台以守护进程模式运行容器。
- `-p 80:80`：将容器的 80 端口映射到宿主机的 80 端口，以便外部访问。
- `-v /mydata/NGINX/nginx.conf:/etc/nginx/nginx.conf:ro`：将宿主机上的 `/mydata/NGINX/nginx.conf` 文件挂载到容器内的 `/etc/nginx/nginx.conf` 位置，并设置为只读模式（`ro`），以确保容器内的进程不会修改该文件。
- `--name nginx`：为容器命名为 `game-info-nginx`，方便后续管理。
- `registry.openanolis.cn/openanolis/nginx-accel:1.22.1-23`: 这部分指定了要使用的镜像及其标签。这里使用的是来自 `registry.openanolis.cn` 镜像仓库的 `openanolis/nginx-accel` 镜像，版本标签为 `1.22.1-23`。

启动以后，查看配置文件的位置：

```bash
docker exec -it game-info-nginx nginx -t
```

```bash
[root@iZ2vc7dxtwpmtamdh6hw0yZ NGINX]# docker exec -it game-info-nginx nginx -t
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

设置配置文件：

```
    server {
    listen 80;
    server_name 47.108.204.196;

    location / {
        root   /myapp/dist;
        try_files $uri $uri/ /index.html;
    }
```

解释：

**一、`server`块**：

```
listen 80;
```

这一行指定了 NGINX 服务器监听的端口号为 80。当有 HTTP 请求到达服务器的 80 端口时，该 `server` 块将处理相应的请求。

```
server_name 47.108.204.196;
```

定义了该服务器块所服务的域名或 IP 地址。在这里，它明确指出该服务器块将处理发送到 IP 地址 `47.108.204.196` 的请求。如果一个请求的 `Host` 头部与这个 IP 匹配，该服务器块将被选中来处理请求。

**二、`location`块**：

```
location / {
```

这是一个根路径的位置匹配，将匹配所有以 `/` 开头的请求路径。

```
root /myapp/dist;
```

此指令指定了根目录，对于匹配 `location /` 的请求，NGINX 将在 `/myapp/dist` 目录下查找相应的文件。例如，如果请求的是 `/index.html`，NGINX 会在 `/myapp/dist/index.html` 处查找文件。

```
try_files $uri $uri/ /index.html;
```

这是一个文件查找和重定向规则。当 NGINX 收到请求时，它将按照以下顺序查找文件：

1. 首先尝试查找 `$uri` 所表示的文件，例如，如果请求是 `/styles/main.css`，NGINX 会查找 `/myapp/dist/styles/main.css`。
2. 如果该文件不存在，NGINX 将尝试查找目录 `$uri/` ，并寻找该目录下的默认文件（通常是 `index.html` 或 `index.htm` ）。
3. 如果上述两种情况都不成功，将重定向到 `/index.html` 。这对于单页应用（SPA）非常有用，因为在 SPA 中，前端路由通常由 JavaScript 处理，无论 URL 是什么，都应该返回 `index.html` 页面，然后由前端的 JavaScript 来处理路由和显示相应的内容。

总的来说，这个配置文件设置了一个简单的 NGINX 服务器，监听 80 端口，为 `47.108.204.196` 这个 IP 地址提供服务。对于所有的请求，它会尝试在 `/myapp/dist` 目录下查找相应文件或目录，最终会将所有无法找到的请求重定向到 `/index.html` ，这对于部署前端的单页应用程序来说是一个常见的配置，以确保前端路由能够正常工作。
