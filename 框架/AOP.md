# AOP

## 基础概念：

### 1.连接点：

是程序执行过程中的具体点，在这些点上可以插入额外的行为。一个方法调用就是一个连接点。

- **举例**：方法 `calculate()`的执行是一个连接点。

### 2.切面：

封装了横切关注点的模块，它定义了何时以及如何应用增强功能。通知是切面必须完成的工作，比如在目标方法之前或之后执行某些操作。

举例：在每个方法执行前后记录日志，“记录日志”的功能就是一个切面。

通知是切面的一部分，它定义了在切点匹配时应该执行的动作。通知有几种类型，包括：

- **前置通知** (`@Before`)：在目标方法执行之前执行。
- **后置通知** (`@After`)：在目标方法执行之后执行，无论方法是否成功。
- **返回通知** (`@AfterReturning`)：在目标方法成功返回后执行。
- **异常通知** (`@AfterThrowing`)：在目标方法抛出异常后执行。 
- **环绕通知** (`@Around`)：在目标方法执行之前和之后运行，可以控制方法的执行。

### 3.切点：

切点用于选择特定的连接点。如要在`OrderService`的所有`placeOrder`方法调用上应用的横切关注点（如日志记录和事务管理），可以定义如下切点：

```
execution(* OrderService.placeOrder(..))
```

每次`OrderService`类中的`placeOrder`方法被调用时，都会触发与这个切点关联的通知。

### 举例：

银行应用有一个`AccountService`类，其中有两个方法：`deposit`和`withdraw`。要实现在每个方法调用前后记录日志。

- **切面**：记录日志的功能。

- **连接点**：`deposit`方法调用和`withdraw`方法调用。连接点就是这两个方法的实际调用点。

- **切点**：定义了一个切入点 `execution(* com.example.AccountService.*(..))`，那么这个切入点就会匹配 `AccountService` 类中的所有方法调用，即 `deposit()` 和 `withdraw()` 都会被视为连接点，并且我们可以在这些方法调用前后加入记录日志的代码。

  ```Java
  @Aspect
  @Component
  public class LoggingAspect {
  
      // 定义一个切入点方法
      @Pointcut("execution(* com.example.AccountService.*(..))")
      public void accountServiceMethods() {}
  
      // 在Advice中引用这个切入点方法
      @Before("accountServiceMethods()")
      public void logBefore(JoinPoint joinPoint) {
          System.out.println("Method " + joinPoint.getSignature().getName() + " begins with [" +
                  Arrays.toString(joinPoint.getArgs()) + "]");
      }
  
      @After("accountServiceMethods()")
      public void logAfter(JoinPoint joinPoint) {
          System.out.println("Method " + joinPoint.getSignature().getName() + " ends");
      }
  }
  ```

  ### `JoinPoint` 参数的作用

  `JoinPoint` 对象提供了以下信息：

  - **签名 (`Signature`)**：描述了连接点的详细信息，如方法名、参数类型等。
  - **参数 (`args`)**：获取方法调用时传递的实际参数值。
  - **目标对象 (`target`)**：获取被代理的目标对象。
  - **代理 (`this`)**：获取当前执行的代理对象。

  ```java
  @Before("serviceLayer()")
  public void beforeServiceLayer(JoinPoint joinPoint) {
      // 获取方法签名
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      String methodName = signature.getName();
      
      // 获取方法参数
      Object[] args = joinPoint.getArgs();
      
      System.out.println("Before advice executing: " + methodName);
      System.out.println("Arguments: " + Arrays.toString(args));
  }
  ```

## 注解和AOP的结合：

创建一个自定义注解 `@RequirePermission`，并在AOP切面中实现权限检查。

### 1. 创建自定义注解

定义一个自定义注解 `@RequirePermission`，用于标记需要进行权限检查的方法。

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequirePermission {
    String value();  // 权限字符串
}
```

**自定义注解 `@RequirePermission`**：

- `@Retention(RetentionPolicy.RUNTIME)` 表示注解在运行时保留。
- `@Target(ElementType.METHOD)` 表示注解可以应用于方法。
- `String value()` 表示注解的值是一个字符串，用于指定所需的权限。

### 2. 创建权限检查切面

创建一个切面类 `PermissionAspect`，在该类中定义切点和通知，实现权限检查逻辑。

```java
@Aspect
public class PermissionAspect {

    // 定义切点，匹配所有带有 @RequirePermission 注解的方法
    @Pointcut("@annotation(com.example.annotation.RequirePermission)")
    public void requirePermissionMethods() {
        // 这是一个空函数，起到给切点命名的作用
    }

    // 在方法调用前执行的通知
    @Before("requirePermissionMethods()")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission != null) {
            String requiredPermission = requirePermission.value();

            // 权限检查逻辑
            boolean hasPermission = checkUserPermission(requiredPermission);

            if (!hasPermission) {
                throw new SecurityException("User does not have the required permission: " + requiredPermission);
            }

            System.out.println("User has the required permission: " + requiredPermission);
        }
    }

    // 权限检查逻辑
    private boolean checkUserPermission(String requiredPermission) {
        // 
        // 这里只是简单地返回 true 或 false
        return "ADMIN".equals(requiredPermission);  // 假设用户只有管理员权限
    }
}
```

**权限检查切面 `PermissionAspect`**：

- `@Pointcut("@annotation(com.example.annotation.RequirePermission)")` 定义了一个切点，匹配所有带有 `@RequirePermission` 注解的方法。
- `@Before("requirePermissionMethods()")` 定义了一个前置通知，用于在匹配的方法调用之前执行权限检查。
- `checkPermission(JoinPoint joinPoint)` 方法实现了权限检查逻辑，获取方法上的注解，并检查用户是否有所需的权限。
- `checkUserPermission(String requiredPermission)` 方法模拟了权限检查逻辑，可以根据实际情况实现真实的权限检查。

### 3. 服务类

服务类 `MyService`，其中包含一些需要进行权限检查的方法。

```java
public class MyService {

    @RequirePermission("USER")
    public void doSomething() {
        System.out.println("Doing something...");
    }

    @RequirePermission("ADMIN")
    public void doAnotherThing() {
        System.out.println("Doing another thing...");
    }
}
```

**服务类 `MyService`**：

- `doSomething` 方法需要 `USER` 权限。
- `doAnotherThing` 方法需要 `ADMIN` 权限。

### 4. 测试

创建一个主类来测试权限检查切面。

```java
public class Main {
    public static void main(String[] args) {
        MyService myService = new MyService();
        
        try {
            myService.doSomething();  // 抛出 SecurityException
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        }

        myService.doAnotherThing();  // 正常执行
    }
}
```

### 输出

运行上述代码，将看到以下输出：

```
User does not have the required permission: USER
User does not have the required permission: USER
User has the required permission: ADMIN
Doing another thing...
```

## AOP中的常用注解：

### @Aspect
- **使用场景**：用于声明一个类是切面类，即这个类包含了AOP的逻辑，如定义切入点和通知。
- **例子**：
  ```java
  @Aspect
  public class LoggingAspect {
      @Before("execution(* com.example.service.*.*(..))")
      public void logBefore() {
          System.out.println("Before method execution");
      }
  }
  ```

### @Component
- **使用场景**：通常与@Aspect结合使用，将切面类作为Spring容器管理的Bean，使其能够被Spring自动检测并注册。
- **例子**：
  ```java
  @Aspect
  @Component
  public class LoggingAspect {
      // AOP logic
  }
  ```

### @Pointcut
- **使用场景**：用于定义一个切入点，指定哪些方法可以被AOP逻辑拦截。可以基于方法的执行、方法的调用者、抛出的异常等来定义。
- **例子**：
  ```java
  @Pointcut("execution(* com.example.service.*.*(..))")
  public void serviceMethods() {
  }
  ```

### @Before
- **使用场景**：用于定义前置通知，它会在目标方法执行之前运行。
- **例子**：
  ```java
  @Before("serviceMethods()")
  public void logBefore() {
      System.out.println("Before method execution");
  }
  ```

### @After
- **使用场景**：用于定义后置通知，它会在目标方法执行之后运行，无论方法是否成功。
- **例子**：
  ```java
  @After("serviceMethods()")
  public void logAfter() {
      System.out.println("After method execution");
  }
  ```

### @AfterReturning
- **使用场景**：用于定义返回通知，它会在目标方法成功返回后运行。
- **例子**：
  ```java
  @AfterReturning(pointcut = "serviceMethods()", returning = "result")
  public void logAfterReturning(Object result) {
      System.out.println("Method returned: " + result);
  }
  ```

### @AfterThrowing
- **使用场景**：用于定义异常通知，它会在目标方法抛出异常后运行。
- **例子**：
  ```java
  @AfterThrowing(pointcut = "serviceMethods()", throwing = "error")
  public void logAfterThrowing(Throwable error) {
      System.out.println("Method threw exception: " + error.getMessage());
  }
  ```

### @Around
- **使用场景**：用于定义环绕通知，它会在目标方法执行之前和之后运行，并且可以控制目标方法的执行。
- **例子**：
  ```java
  @Around("serviceMethods()")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
      System.out.println("Before method execution");
      Object result = joinPoint.proceed();
      System.out.println("After method execution");
      return result;
  }
  ```

### @Order
- **使用场景**：用于指定切面的执行顺序，当有多个切面时，可以通过@Order注解来控制它们的执行顺序。
- **例子**：
  ```java
  @Aspect
  @Order(1)
  public class FirstAspect {
      // AOP logic
  }
  ```

### @EnableAspectJAutoProxy
- **使用场景**：用于在Spring Boot应用中启用AOP代理。这个注解通常在应用的主类或者配置类上使用，以确保Spring AOP代理是激活的。
- **例子**：
  ```java
  @SpringBootApplication
  @EnableAspectJAutoProxy
  public class Application {
      public static void main(String[] args) {
          SpringApplication.run(Application.class, args);
      }
  }
  ```

### @Target 和 @Retention
- **使用场景**：@Target用于指定注解可以应用的Java元素类型（如方法、类等），@Retention用于指定注解的保留策略，例如在运行时是否可用。
- **例子**：
  ```java
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface CustomAnnotation {
  }
  ```

### @Documented
- **使用场景**：表示被它修饰的注解将被javadoc工具提取成文档，这有助于在生成API文档时包含注解的信息。
- **例子**：
  ```java
  @Documented
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface CustomAnnotation {
  }
  ```
