# AOP

## 基础概念：

### 1.连接点：

是程序执行过程中的具体点，在这些点上可以插入额外的行为。例如，一个方法调用就是一个连接点。

- **举例**：如果你有一个方法 `calculate()`，那么这个方法的执行可以是一个连接点。

### 2.切面：

封装了横切关注点的模块。举例：如果你正在开发一个应用，并且需要在每个方法执行前后记录日志，那么“记录日志”的功能就是一个切面。

通知是切面的一部分，它定义了在切点匹配时应该执行的动作。通知有几种类型，包括：

- **前置通知** (`@Before`)：在目标方法执行之前执行。
- **后置通知** (`@After`)：在目标方法执行之后执行，无论方法是否成功。
- **返回通知** (`@AfterReturning`)：在目标方法成功返回后执行。
- **异常通知** (`@AfterThrowing`)：在目标方法抛出异常后执行。
- **环绕通知** (`@Around`)：在目标方法执行之前和之后运行，可以控制方法的执行。

### 3.切点：

 是对连接点的一个选择条件，即一组连接点的选择标准。它定义了切面应该在哪些连接点上应用。

### 举例：

假设你有一个银行应用，里面有一个`AccountService`类，其中有两个方法：`deposit`和`withdraw`。你希望在每个方法调用前后记录日志。

- **切面**：记录日志的功能。

- **连接点**：`deposit`方法调用和`withdraw`方法调用。连接点就是这两个方法的实际调用点。

- **切点**：如果我们定义了一个切入点 `execution(* com.example.AccountService.*(..))`，那么这个切入点就会匹配 `AccountService` 类中的所有方法调用，即 `deposit()` 和 `withdraw()` 都会被视为连接点，并且我们可以在这些方法调用前后加入记录日志的代码。

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

在AOP（面向切面编程）中，切入点（Pointcut）定义了切面（Aspect）的哪个部分应当被应用到哪些连接点（Joinpoint）上。连接点通常是应用程序中的方法调用或异常抛出等事件。通过将切入点定义为对特定注解的匹配，您可以精确地控制AOP逻辑何时被触发。

当您使用自定义注解作为切入点时，Spring AOP会检查方法是否带有该注解，如果方法被正确标注，那么对应的AOP逻辑（Advice）就会被执行。这种方式使得AOP更加灵活，因为它允许您在不改变业务逻辑代码的情况下，通过添加或移除注解来启用或禁用特定的行为。

### 举例-自定义注解：记录日志

1.自定义一个注解@Loggable

```Java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecutionTime {
}
```

2.创建切面

```Java
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("Method " + joinPoint.getSignature().getName() +
                    " took " + elapsedTime + " milliseconds to execute.");
        }
    }
}
```

3.实际使用

```Java
import org.springframework.stereotype.Service;

@Service
public class SomeService {

    @LogExecutionTime
    public void someOperation() {
        // 模拟一些耗时操作
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

## aop中的常用注解：

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
