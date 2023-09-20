# 多版本并发控制

#### **1.** **什么是MVCC** 

MVCC （Multiversion Concurrency Control），多版本并发控制。

MVCC 是一种并发控制机制，用于在多个并发事务同时读写数据库时保持数据的一致性和隔离性。它是通过在每个数据行上维护多个版本的数据来实现的。当一个事务要对数据库中的数据进行修改时，MVCC 会为该事务创建一个数据快照，而不是直接修改实际的数据行。

换言之，就是为了查询一些正在被另一个事务更新的行，并且可以看到它们被更新之前的值，这样在做查询的时候就不用等待另一个事务释放锁。

MVCC实现依赖于<font color='orange'>**隐藏字段、Undo Log、Read View**</font>。

#### **2.** **快照读与当前读**

MVCC在MySQL InnoDB中的实现主要是为了提高数据库并发性能，用更好的方式去处理`读-写冲突`，做到即使有读写冲突时，也能做到`不加锁`，`非阻塞并发读`，而这个读指的就是`快照读`, 而非`当前读`。当前读实际上是一种加锁的操作，是悲观锁的实现。而MVCC本质是采用乐观锁思想的一种方式。

##### **2.1** **快照读**

在快照读中，事务读取的是数据库在事务开始时的一个快照或某个时间点的版本。

这意味着无论其他事务在执行期间对数据进行了多少次修改，快照读都会返回事务启动时数据的一个一致版本。

快照读通常用于提供一定程度的隔离性，但不会阻止其他事务对数据进行修改。

快照读的前提是隔离级别不是串行级别，串行级别下的快照读会退化成当前读。

##### **2.2** **当前读**

当前读读取的是记录的最新版本（最新数据，而不是历史版本的数据），读取时还要保证其他并发事务不能修改当前记录，会对读取的记录进行加锁。加锁的 SELECT，或者对数据进行增删改都会进行当前读。

#### 3.隐藏字段

InnoDB存储引擎的表来说，它的聚簇索引记录中都包含隐藏列。

- `trx_id`：记录`创建`这条记录/`最后一次修改`该记录的`事务ID`。每次一个事务对某条聚簇索引记录进行改动时，都会把该事务的`事务id`赋值给trx_id 隐藏列。
- `roll_pointer`：`回滚指针`，指向`这条记录`的`上一个版本`（存储于rollback segment里）。每次对某条聚簇索引记录进行改动时，都会把旧的版本写入到 undo日志 中，然后这个隐藏列就相当于一个指针，可以通过它来找到该记录修改前的信息。
- `db_row_id`，隐含的`自增ID`（隐藏主键），如果数据表`没有主键`，InnoDB会自动以db_row_id产生一个`聚簇索引`。
- `删除flag`隐藏字段, 记录被`更新`或`删除`。

#### **4. MVCC实现原理之ReadView** 

MVCC 的实现依赖于：**隐藏字段、Undo Log、Read View**。

##### **4.1** **什么是ReadView**

事务进行`快照读`操作的时候生产的`读视图`(Read View)，在该事务执行的快照读的那一刻，会生成数据库系统当前的一个`快照`。

记录并维护系统当前`活跃事务的ID`(没有commit，当每个事务开启时，都会被分配一个ID, 这个ID是递增的，所以越新的事务，ID值越大)，是系统中当前不应该被`本事务`看到的`其他事务id列表`。

Read View主要是用来做`可见性`判断的, 即当我们`某个事务`执行`快照读`的时候，对该记录创建一个Read View读视图，把它比作条件用来判断`当前事务`能够看到`哪个版本`的数据，既可能是当前`最新`的数据，也有可能是该行记录的undo log里面的`某个版本`的数据。

##### **4.2** **设计思路**

使用`READ UNCOMMITTED`隔离级别的事务，由于可以读到未提交事务修改过的记录，所以直接读取记录的最新版本就好了。

使用`SERIALIZABLE`隔离级别的事务，InnoDB规定使用加锁的方式来访问记录。

使用`READ COMMITTED`和`REPEATABLE READ`隔离级别的事务，都必须保证读到`已经提交了的`事务修改过的记录。假如另一个事务已经修改了记录但是尚未提交，是不能直接读取最新版本的记录的，核心问题就是需要判断一下版本链中的哪个版本是当前事务可见的，这是ReadView要解决的主要问题。

这个ReadView中主要包含4个比较重要的内容，分别如下：

1. `trx_ids`: 当前系统活跃(`未提交`)事务版本号集合。
2. `low_limit_id`: 创建当前read view 时“当前系统`最大事务版本号`+1”。
3. `up_limit_id`: 创建当前read view 时“系统正处于活跃事务`最小版本号`”
4. `creator_trx_id`: 创建当前read view的事务版本号；

> 注意：low_limit_id并不是trx_ids中的最大值，事务id是递增分配的。比如，现在有id为1， 2，3这三个事务，之后id为3的事务提交了。那么一个新的读事务在生成ReadView时，trx_ids就包括1和2，up_limit_id的值就是1，low_limit_id的值就是4。

举例：

![image-20230327144759114](C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327144759114.png)

##### **4.3 ReadView的规则**

有了这个ReadView，这样在访问某条记录时，只需要按照下边的步骤判断记录的某个版本是否可见。

- 如果被访问版本的trx_id属性值与ReadView中的`creator_trx_id`值相同，意味着当前事务在访问它自己修改过的记录，所以该版本可以被当前事务访问。
- 如果被访问版本的trx_id属性值小于ReadView中的`up_limit_id`值，表明生成该版本的事务在当前事务生成ReadView前已经提交，所以该版本可以被当前事务访问。
- 如果被访问版本的trx_id属性值大于或等于ReadView中的`low_limit_id`值，表明生成该版本的事务在当前事务生成ReadView后才开启，所以该版本不可以被当前事务访问。
- 如果被访问版本的trx_id属性值在ReadView的`up_limit_id`和`low_limit_id`之间，那就需要判断一下trx_id属性值是不是在 trx_ids 列表中。
  - 如果在，说明创建ReadView时生成该版本的事务还是活跃的，该版本不可以被访问。
  - 如果不在，说明创建ReadView时生成该版本的事务已经被提交，该版本可以被访问。

##### **4.4 MVCC整体操作流程**

了解了这些概念之后，我们来看下当查询一条记录的时候，系统如何通过MVCC找到它：

1. 首先获取事务自己的版本号，也就是事务 ID； 

2. 获取 ReadView； 

3. 查询得到的数据，然后与 ReadView 中的事务版本号进行比较；

4. 如果不符合 ReadView 规则，就需要从 Undo Log 中获取历史快照；

5. 最后返回符合规则的数据。

在隔离级别为读已提交（Read Committed）时，一个事务中的每一次 SELECT 查询都会重新获取一次Read View。

如表所示：

<img src="C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327150844116.png" alt="image-20230327150844116" style="zoom: 67%;" />

> 注意，此时同样的查询语句都会重新获取一次 Read View，这时如果 Read View 不同，就可能产生不可重复读或者幻读的情况。

当隔离级别为可重复读的时候，就避免了不可重复读，这是因为一个事务只在第一次 SELECT 的时候会获取一次 Read View，而后面所有的 SELECT 都会复用这个 Read View，如下表所示：

<img src="C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327150943667.png" alt="image-20230327150943667" style="zoom:67%;" />

#### **5.** **举例说明**

<img src="C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327151719952.png" alt="image-20230327151719952" style="zoom:67%;" />

##### **5.1 READ COMMITTED隔离级别下** 

**READ COMMITTED** **：每次读取数据前都生成一个ReadView**。

现在有两个 事务id 分别为 10 、 20 的事务在执行：

```sql
# Transaction 10

BEGIN;

UPDATE student SET name="李四" WHERE id=1;

UPDATE student SET name="王五" WHERE id=1;

# Transaction 20

BEGIN;

# 更新了一些别的表的记录

#说明：事务执行的过程中，只有第一次真正修改记录时（Insert、update等）才会被分配事务ID，且这个ID是递增的。事务2中更新别的表，为的是让它分配事务ID
```

此刻，表student 中 id 为 1 的记录得到的版本链表如下所示：

![image-20230327152236020](C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327152236020.png)

假设现在有一个使用 READ COMMITTED 隔离级别的事务开始执行：

```sql
# 使用READ COMMITTED隔离级别的事务

BEGIN;

# SELECT1：Transaction 10、20未提交

SELECT * FROM student WHERE id = 1; # 得到的列name的值为'张三'
```

这个<font color='orange'>SELECT1</font>的执行过程如下：

（1）执行<font color='orange'>SELECT</font>的时候会先生成一个<font color='orange'>ReadView</font>，其中<font color='orange'>trx_ids</font>列表的内容就是[10,20]，<font color='orange'>up_limit_id为10</font>，<font color='orange'>low_limit_id为21</font>，<font color='orange'>creator_trx_id为0</font>；

（2）从版本链中挑选可见的记录，从图中看出，最新版本的列<font color='orange'>name</font>的内容是'<font color='orange'>王五</font>，该版本的<font color='orange'>trx_id</font>值为<font color='orange'>10</font>，在<font color='orange'>trx_ids</font>列表内，所以不符合可见性要求，根据<font color='orange'>roll_pointer</font>跳到下一个版本。

（3）下一个版本的列<font color='orange'>name</font>的内容是'李四’，该版本的<font color='orange'>trx_id</font>值也为10，也在<font color='orange'>trx_ids</font>列表内，所以也不符合要求，继续跳到下一个版本。

（4）下一个版本的列<font color='orange'>name</font>的内容是'张三’，该版本的<font color='orange'>trx_id</font>值为8，小于<font color='orange'>ReadView </font>中的up_limit_id值10，所以这个版本是符合要求的，最后返回给用户的版本就是这条列name为‘<font color='orange'>张三</font>的记录。

之后，我们把 事务id 为 10 的事务提交一下：

```sql
# Transaction 10

BEGIN;

UPDATE student SET name="李四" WHERE id=1;

UPDATE student SET name="王五" WHERE id=1;

COMMIT;
```

然后再到 事务id 为 20 的事务中更新一下表 student 中 id 为 1 的记录：

```sql
# Transaction 20

BEGIN;

# 更新了一些别的表的记录

...

UPDATE student SET name="钱七" WHERE id=1;

UPDATE student SET name="宋八" WHERE id=1;
```

此刻，表student中 id 为 1 的记录的版本链就长这样：

![image-20230327154233043](C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327154233043.png)

然后再到刚才使用 READ COMMITTED 隔离级别的事务中继续查找这个 id 为 1 的记录，如下：

```sql
# 使用READ COMMITTED隔离级别的事务

BEGIN;

# SELECT1：Transaction 10、20均未提交

SELECT * FROM student WHERE id = 1; # 得到的列name的值为'张三'

# SELECT2：Transaction 10提交，Transaction 20未提交

SELECT * FROM student WHERE id = 1; # 得到的列name的值为'王五'
```

这个<font color='orange'>SELECT2</font>的执行过程如下：

（1）在执行SELECT语句时又会生成一个<font color='orange'>ReadView</font>，该ReadView的<font color='orange'>trx_ids</font>列表的内容就是[20]，因为事务10已经提交了，<font color='orange'>up_limit_id</font>为20，<font color='orange'>low_limit_id</font>为21，<font color='orange'>creator_trx_id</font>为0。

（2）从版本链中挑选可见的记录。从图中看出，最新<font color='orange'>name</font>的内容是<font color='orange'>宋八</font>，该版本的<font color='orange'>trx_id</font>值为<font color='orange'>20</font>，在trx_ids列表内，不符合要求，根据<font color='orange'>roll_pointer</font>跳转到下一个版本。

（3）下一个版本的列<font color='orange'>trx_id</font>的内容是20，继续跳过。

（4）下一个版本的列<font color='orange'>trx_id</font>的内容是10，小于<font color='orange'>ReadView</font>中<font color='orange'>up_limit_id</font>值20，所以这个版本是符合要求的，最后返回<font color='orange'>王五</font>。

##### **5.2 REPEATABLE READ隔离级别下**

使用`REPEATABLE READ`隔离级别的事务来说，只会在第一次执行查询语句时生成一个 ReadView ，之后的查询就不会重复生成了。

##### **5.3** **如何解决幻读**

幻读的隔离级别首先是<font color='orange'>REPEATABLE READ</font>

假设现在表 student 中只有一条数据，数据内容中，主键 id=1，隐藏的 trx_id=10，它的 undo log 如下图所示。

<img src="C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327163644519.png" alt="image-20230327163644519" style="zoom:67%;" />

假设现在有事务 A 和事务 B 并发执行，`事务 A`的事务 id 为`20`，`事务 B`的事务 id 为`30`。

步骤1：事务 A 开始第一次查询数据，查询的 SQL 语句如下。

```mysql
select * from student where id >= 1;
```

在开始查询之前，MySQL 会为事务 A 产生一个 ReadView，此时 ReadView 的内容如下：`trx_ids= [20,30]`，`up_limit_id=20`，`low_limit_id=31`，`creator_trx_id=20`。

由于此时表 student 中只有一条数据，且符合 where id>=1 条件，因此会查询出来。然后根据 ReadView机制，发现该行数据的trx_id=10，小于事务 A 的 ReadView 里 up_limit_id，这表示这条数据是事务 A 开启之前，其他事务就已经提交了的数据，因此事务 A 可以读取到。

结论：事务 A 的第一次查询，能读取到一条数据，id=1。

步骤2：接着事务 B(trx_id=30)，往表 student 中新插入两条数据，并提交事务。

```mysql
insert into student(id,name) values(2,'李四'); 
insert into student(id,name) values(3,'王五');
```

此时表student 中就有三条数据了，对应的 undo 如下图所示：

<img src="C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327165616391.png" alt="image-20230327165616391" style="zoom:67%;" />

步骤3：接着事务 A 开启第二次查询，根据可重复读隔离级别的规则，此时事务 A 并不会再重新生成ReadView。此时表 student 中的 3 条数据都满足 where id>=1 的条件，因此会先查出来。然后根据ReadView 机制，判断每条数据是不是都可以被事务 A 看到。

1）首先 id=1 的这条数据，前面已经说过了，可以被事务 A 看到。

2）然后是 id=2 的数据，它的 trx_id=30，此时事务 A 发现，这个值处于 up_limit_id 和 low_limit_id 之间，因此还需要再判断 30 是否处于 trx_ids 数组内。由于事务 A 的 trx_ids=[20,30]，因此在数组内，这表示 id=2 的这条数据是与事务 A 在同一时刻启动的其他事务提交的，所以这条数据不能让事务 A 看到。

3）同理，id=3 的这条数据，trx_id 也为 30，因此也不能被事务 A 看见。

<img src="C:\Users\SalmonRun\AppData\Roaming\Typora\typora-user-images\image-20230327165631327.png" alt="image-20230327165631327" style="zoom:50%;" />

结论：最终事务 A 的第二次查询，只能查询出 id=1 的这条数据。这和事务 A 的第一次查询的结果是一样的，因此没有出现幻读现象，所以说在 MySQL 的可重复读隔离级别下，不存在幻读问题。

#### **6.** **总结**

这里介绍了`MVCC`在`READ COMMITTD`、`REPEATABLE READ`这两种隔离级别的事务在执行快照读操作时访问记录的版本链的过程。这样使不同事务的`读-写`、`写-读`操作并发执行，从而提升系统性能。

核心点在于 ReadView 的原理，`READ COMMITTD`、`REPEATABLE READ`这两个隔离级别的一个很大不同就是生成ReadView的时机不同：

- `READ COMMITTD`在每一次进行普通SELECT操作前都会生成一个ReadView 
- `REPEATABLE READ`只在第一次进行普通SELECT操作前生成一个ReadView，之后的查询操作都重复使用这个ReadView就好了。