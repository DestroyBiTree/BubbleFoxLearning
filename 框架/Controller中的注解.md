# Controller中的注解

### 1.`@RequestParam`

- **作用**: 用于从请求的查询参数或表单参数中获取值，并将其绑定到控制器方法的参数上。

- **使用场景**: `GET`请求和`POST`请求的表单提交。

- 示例

  ```java
  @GetMapping("/greet")
  public String greet(@RequestParam String name) {
      return "Hello, " + name;
  }
  ```

  在这个例子中，name的值是通过请求的查询参数（如/greet?name=John）传递的。

### 2. `@PathVariable`

- **作用**: 用于从请求URL路径中获取值，并将其绑定到控制器方法的参数上。

- **使用场景**: 常用于`GET`请求。

- 示例

  ```Java
  @GetMapping("/users/{id}")
  public String getUserById(@PathVariable Long id) {
      return "User ID: " + id;
  }
  ```

  在这个例子中，id的值是从URL路径中提取的（如/users/1230）。


### 3. `@RequestBody`

- **作用**: 用于将请求体中的JSON或XML数据反序列化为Java对象，并将其绑定到控制器方法的参数上。

- **使用场景**: 一般用于`POST`、`PUT`等需要客户端发送复杂数据（如JSON对象）的请求。

- 示例

  ```java
  @PostMapping("/users")
  public String createUser(@RequestBody User user) {
      return "User created: " + user.getName();
  }
  ```

  在这个例子中，user对象是从请求体中的JSON数据反序列化而来的。

### 4. 与 `GET` 和 `POST` 的对应关系

- **GET**: 通常用于从服务器获取数据，常使用`@RequestParam`和`@PathVariable`。
  - `@RequestParam`: 获取查询参数。
  - `@PathVariable`: 从URL路径中获取动态部分。
- **POST**: 通常用于向服务器发送数据，常使用`@RequestBody`和有时`@RequestParam`。
  - `@RequestBody`: 从请求体中获取数据并反序列化为Java对象。
  - `@RequestParam`: 用于获取表单数据。