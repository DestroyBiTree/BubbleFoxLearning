# SpringBoot

## 介绍一下SpringBoot

Spring 是重量级企业开发框架 Enterprise JavaBean（EJB） 的替代品，Spring 为企业级 Java 开发提供了一种相对简单的方法，通过 `依赖注入 和 面向切面编程 `，用简单的 Java 对象（Plain Old JavaObject，POJO） 实现了 EJB 的功能。

虽然 Spring 的组件代码是轻量级的，但它的配置却是重量级的（需要大量 XML 配置） 。

**为什么要有SpringBoot？**

Spring 旨在简化 J2EE 企业应用程序开发。Spring Boot 旨在简化 Spring 开发（减少配置文件，开箱即用！）。

**SpringBoot主要优点：**

1. 开发基于 Spring 的应用程序很容易。
2. Spring Boot 项目所需的开发或工程时间明显减少，通常会提高整体生产力。
3. Spring Boot 不需要编写大量样板代码、XML 配置和注释。
4. Spring 引导应用程序可以很容易地与 Spring 生态系统集成，如 Spring JDBC、Spring ORM、
Spring Data、Spring Security 等。
5. Spring Boot 遵循“固执己见的默认配置”，以减少开发工作（默认配置可以修改）。
6. Spring Boot 应用程序提供嵌入式 HTTP 服务器，如 Tomcat 和 Jetty，可以轻松地开发和测试 web
应用程序。（这点很赞！普通运行 Java 程序的方式就能运行基于 Spring Boot web 项目，省事很
多）
7. Spring Boot 提供命令行接口(CLI)工具，用于开发和测试 Spring Boot 应用程序，如 Java 或
Groovy。
8. Spring Boot 提供了多种插件，可以使用内置工具(如 Maven 和 Gradle)开发和测试 Spring Boot
应用程序

**什么是** **Spring Boot Starters?**

`Spring Boot Starters 是一系列依赖关系的集合`，因为它的存在，项目的依赖之间的关系对我们来说变
的更加简单了。
举个例子：在没有 Spring Boot Starters 之前，我们开发 REST 服务或 Web 应用程序时; 我们需要使用像 Spring MVC，Tomcat 和 Jackson 这样的库，这些依赖我们需要手动一个一个添加。但是，有了Spring Boot Starters 我们只需要一个只需添加一个spring-boot-starter-web一个依赖就可以了，这个依赖包含的子依赖中包含了我们开发 REST 服务需要的所有依赖。

## @SpringBootApplication注解

`@SpringBootApplication` 注解是 Spring Boot 应用程序的核心注解之一，它的作用是将一个普通的 Java 类标记为 Spring Boot 应用程序的主类。这个主类通常包含 `main` 方法，用于启动 Spring Boot 应用程序。

具体来说，`@SpringBootApplication` 注解执行以下几项关键任务：

1. **标识主类**：`@SpringBootApplication` 注解告诉 Spring Boot 这是应用程序的主类，即应用程序的入口点。当您运行主类时，Spring Boot 将扫描该类所在的包以及其子包，以查找其他 Spring 组件和配置。
2. **组合注解**：`@SpringBootApplication` 实际上是一个组合注解，它包括了以下三个注解的功能：
   - `@Configuration`：标识该类为 Spring 的配置类，可以定义 Bean 和配置。
   - `@EnableAutoConfiguration`：启用 Spring Boot 的自动配置功能，这允许 Spring Boot 自动配置应用程序的各种功能，例如数据库连接、Web服务器等。
   - `@ComponentScan`：启用组件扫描，以便 Spring Boot 能够自动发现并注册应用程序中的组件，包括控制器、服务、仓库等。
3. **简化应用程序配置**：使用 `@SpringBootApplication` 注解，您可以创建一个极简的 Spring Boot 应用程序，因为它自动进行了许多配置。您无需手动配置大部分常见的 Spring 配置，Spring Boot 会根据您的依赖和类路径中的内容进行自动推测和配置。 

## DI和AOP

**依赖注入（DI）**：

依赖注入是一种设计模式，它是实现控制反转（IoC）的一种方式。在依赖注入中，`对象的依赖关系不再由对象自己来创建或管理，而是由外部容器或框架注入到对象中`。这通常通过构造函数、方法参数或属性注入来实现。

举例：假设你正在建造一家汽车工厂，该工厂可以生产不同类型的汽车，包括电动汽车和燃油汽车。每种类型的汽车都需要引擎来运行。传统方式是在每次制造汽车时，工厂自己制造引擎。然而，使用依赖注入，工厂可以更灵活地管理汽车的引擎依赖关系。

传统方式：自己创造引擎

```Java
public class CarFactory {

    public Car createElectricCar() {
        ElectricEngine engine = new ElectricEngine();
        return new ElectricCar(engine);
    }
    
    public Car createGasCar() {
        GasEngine engine = new GasEngine();
        return new GasCar(engine);
    }

}
```

DI方式：接受一个引擎作为参数。

```Java
public class CarFactory {

    private Engine engine;
    
    public CarFactory(Engine engine) {
        this.engine = engine;
    }
    
    public Car createElectricCar() {
        return new ElectricCar(engine);
    }
    
    public Car createGasCar() {
        return new GasCar(engine);
    }

}
```

**AOP：面向切面编程**

面向切面编程是一种用于处理横切关注点（Cross-cutting Concerns）的编程范式。**横切关注点是那些在应用程序中多个不同部分都存在的功能**，例如日志记录、事务管理、安全性等。在传统的面向对象编程中，这些横切关注点可能会散布在应用程序的各个部分，导致重复代码和难以维护的问题。

AOP的目标是将横切关注点与核心业务逻辑分离开来，以提高代码的可维护性和可重用性。它通过定义切面（Aspect）来实现这一目标，切面是一组横切关注点的模块化单元，可以在多个地方重复使用。

在Java中，Spring框架也支持AOP，它允许你定义切面并将其应用到应用程序中的各个部分，以实现诸如日志记录、事务管理等横切关注点的功能，从而提高代码的可维护性。

举例：

假设你正在开发一个在线商店的电子商务应用程序，你想要记录每次用户提交订单时的日志。你可以使用AOP来实现这个需求。首先，你需要创建一个切面（Aspect）类，用于定义你要在哪些地方应用日志记录逻辑。

```Java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.example.ecommerce.service.OrderService.placeOrder(..))")
    public void logOrder() {
        System.out.println("订单已提交：记录订单日志...");
    }
}

```

上面的代码中，我们创建了一个名为`LoggingAspect`的切面类，使用`@Aspect`注解标记它，并使用`@Before`注解定义了一个切点（pointcut），它会在`OrderService`的`placeOrder`方法执行之前执行。在这里，我们只是简单地打印一条日志，实际应用中你可以执行更复杂的操作，如将日志写入文件或数据库。

接下来，确保在Spring Boot的配置类中启用了AOP，可以使用`@EnableAspectJAutoProxy`注解来实现：

```Java
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
    // 配置类内容
}
```

最后，确保你的`OrderService`类中有一个`placeOrder`方法，该方法会触发切面的执行：

```Java
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public void placeOrder() {
        // 处理订单逻辑
        System.out.println("订单已提交");
    }
}

```

当你调用`OrderService`的`placeOrder`方法时，AOP会在方法执行之前先执行`LoggingAspect`中的`logOrder`方法，从而实现了日志记录的功能。

## 开发restful常用注解有哪些？

Spring Bean 相关：

- @Autowired : 自动导入对象到类中，被注入进的类同样要被 Spring 容器管理。
- @RestController : @RestController注解是@Controller和@ResponseBody的合集,表示这是个控制器 bean,并且是将函数的返回值直 接填入 HTTP 响应体中,是 REST 风格的控制器。
- @Component ：通用的注解，可标注任意类为 Spring 组件。如果一个 Bean 不知道属于哪个层，可以使用@Component 注解标注。
- @Repository : 对应持久层即 Dao 层，主要用于数据库相关操作。
- @Service : 对应服务层，主要涉及一些复杂的逻辑，需要用到 Dao 层。
- @Controller : 对应 Spring MVC 控制层，主要用于接受用户请求并调用 Service 层返回数据给前端页面。

处理常见的 HTTP 请求类型：

- @GetMapping : GET 请求、
- @PostMapping : POST 请求。
- @PutMapping : PUT 请求。
- @DeleteMapping : DELETE 请求。

前后端传值：

- @RequestParam以及@Pathvairable ：@PathVariable用于获取路径参数，@RequestParam用于获取查询参数。
- @RequestBody ：用于读取 Request 请求（可能是 POST,PUT,DELETE,GET 请求）的 body 部分并且 Content-Type 为 application/json 格式的数据，接收到数据之后会自动将数据绑定到 Java 对象上去。系统会使用HttpMessageConverter或者自定义的HttpMessageConverter将请求的 body中的 json 字符串转换为 java 对象。