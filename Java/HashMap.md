# 集合源码分析

## 一. 哈希表

### 常见概念：

1. **哈希函数（Hash Function）**：
   - 哈希函数是将输入数据（键）映射为固定大小的哈希码（哈希值）的函数。
   - 好的哈希函数应该尽量将不同的输入映射到不同的哈希码，以减少哈希冲突的发生。
   - 常见的有：直接定址法（Hash(Key) = A*Key + B）、除留余数法、平方取中法。
2. **哈希表（Hash Table）**：
   - 哈希表是基于哈希函数的数据结构，它用于存储键值对。
   - 哈希表通过将键的哈希码与数组索引关联起来，实现了快速的键值检索。
3. **哈希码（Hash Code）**：
   - 哈希码是哈希函数计算出的键的整数值。
   - 哈希码用于确定键在哈希表中的存储位置。
4. **哈希冲突（Hash Collision）**：
   - 哈希冲突发生在两个或多个不同的键映射到相同的哈希码上。
   - 通常使用开放地址法、链地址法等方法来解决。

在Java中，有许多实现了`Map`接口的类，这些类用于存储键值对（key-value pairs）并提供了一组方法来操作这些键值对。下面是一些常见的实现`Map`接口的类：

1. **HashMap**：
   - `HashMap`是最常用的`Map`实现之一。它使用哈希表来存储键值对，提供了快速的查找和插入操作。
   - `HashMap`不保证元素的顺序，因此不适用于需要有序存储的情况。
   - 示例代码：
   ```java
   Map<String, Integer> hashMap = new HashMap<>();
   hashMap.put("Alice", 25);
   hashMap.put("Bob", 30);
   ```

2. **TreeMap**：
   - `TreeMap`基于红黑树实现，它可以按键的自然顺序或提供的比较器来保持键的有序性。
   - `TreeMap`中的键是有序的，因此可以用于需要按键排序的场景。
   - 示例代码：
   ```java
   Map<String, Integer> treeMap = new TreeMap<>();
   treeMap.put("Alice", 25);
   treeMap.put("Bob", 30);
   ```

3. **LinkedHashMap**：
   - `LinkedHashMap`继承自`HashMap`，它保持了插入顺序或者访问顺序（可以通过构造函数参数指定）。
   - 保持插入顺序的`LinkedHashMap`在迭代时按元素插入的顺序访问。
   - 示例代码：
   ```java
   Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
   linkedHashMap.put("Alice", 25);
   linkedHashMap.put("Bob", 30);
   ```

4. **Hashtable**：
   - `Hashtable`是`Map`接口的古老实现，它类似于`HashMap`，但是线程安全的。然而，由于性能原因，通常不推荐使用`Hashtable`，而更倾向于使用`ConcurrentHashMap`来实现线程安全。
   - 示例代码：
   ```java
   Hashtable<String, Integer> hashtable = new Hashtable<>();
   hashtable.put("Alice", 25);
   hashtable.put("Bob", 30);
   ```

5. **ConcurrentHashMap**：
   - `ConcurrentHashMap`是多线程环境下的线程安全`Map`实现，它通过分割数据结构来提高并发性能。
   - `ConcurrentHashMap`可以安全地在多个线程之间进行读写操作，而不需要显式的锁定。
   - 示例代码：
   ```java
   Map<String, Integer> concurrentHashMap = new ConcurrentHashMap<>();
   concurrentHashMap.put("Alice", 25);
   concurrentHashMap.put("Bob", 30);
   ```

HashMap和HashTable的区别：

**HashMap** **和** **Hashtable** **的区别**

**线程是否安全**: HashMap 是⾮线程安全的, Hashtable 是线程安全的,因为 Hashtable 内部的⽅法基本都经过 synchronized 修饰｡

效率: 因为线程安全的问题, HashMap 要⽐ Hashtable 效率⾼⼀点｡另外, Hashtable 基本被淘汰,不要在代码中使⽤它;

**对 Null key 和 Null value 的⽀持:** HashMap 可以存储 null 的 key 和 value,但 null 作为键只能有⼀个,null 作为值可以有多个;Hashtable 不允许有 null 键和 null 值,否则会抛出NullPointerException ｡

**初始容量⼤⼩和每次扩充容量⼤⼩的不同 :** ① 创建时如果不指定容量初始值, Hashtable 默认的初始⼤⼩为 11,之后每次扩充,容量变为原来的 2n+1｡ HashMap 默认的初始化⼤⼩为16｡之后每次扩充,容量变为原来的 2 倍｡② 创建时如果给定了容量初始值,那么 Hashtable会直接使⽤你给定的⼤⼩,⽽ HashMap 会将其扩充为 2 的幂次⽅⼤⼩( HashMap 中的tableSizeFor() ⽅法保证,下⾯给源代码)｡也就是说 HashMap 总是使⽤ 2 的幂作为哈希表的⼤⼩。

**底层数据结构:** JDK1.8 以后的 HashMap 在解决哈希冲突时有了⼤的变化,当链表⻓度⼤于阈值(默认为 8)时,将链表转化为红⿊树(将链表转换成红⿊树前会判断,如果当前数组的⻓度⼩于 64,那么会选择先进⾏数组扩容,⽽不是转换为红⿊树),以减少搜索时间(后⽂中我会结合源码对这⼀过程进⾏分析)｡ Hashtable 没有这样的机制｡

###  添加数据：

1. **计算哈希码（Hash Code）**：
   - 当你要添加一个键值对（key-value pair）到哈希表时，首先需要计算键的哈希码。
   - 哈希码是一个整数，通常由哈希函数根据键的内容计算出来。
2. **确定存储位置**：
   - 使用哈希码来确定存储位置（桶）在哈希表中的位置。
   - 哈希表内部维护一个数组，每个位置都对应一个桶。哈希码确定了数据应该存储在哪个桶中。
3. **解决哈希冲突**：
   - 如果多个键具有相同的哈希码（哈希冲突），需要使用适当的方法来解决冲突。
   - 常见的解决哈希冲突的方法包括开放地址法（Open Addressing）和链地址法（Chaining）。
4. **存储数据**：
   - 一旦确定了存储位置，将键值对存储在对应的桶中。
   - 如果使用链地址法，可能需要在桶中的链表或其他数据结构中添加新的键值对。

### 查询数据：

1. **计算哈希码**：
   - 要查询某个键的值，首先需要计算该键的哈希码，以确定它应该在哈希表中的哪个桶中查找。
2. **定位桶**：
   - 使用哈希码找到键所在的桶。
3. **在桶中查找**：
   - 如果使用链地址法，需要在对应桶中的链表或其他数据结构中查找键。
   - 如果使用开放地址法，可能需要迭代桶中的元素，直到找到匹配的键或确定键不存在。
4. **返回结果**：
   - 如果找到了匹配的键，可以返回其关联的值。
   - 如果未找到键，通常返回一个指示未找到的特殊值（如null或-1）。

### 各种类型数据的哈希码应该如何获取

**Integer中源码**

```java
  public static int hashCode(int value) {
        return value;
    }
```

**Arrays中源码**

​	1. 如果对象为null，hash码为0.

​	2. 使用31作为hash因子，减少hash碰撞。

```java
public static int hashCode(Object a[]) {
  if (a == null)
    return 0;

  int result = 1;

  for (Object element : a)
    result = 31 * result + (element == null ? 0 : element.hashCode());

  return result;
}
```

**String中源码**

​	给定一个内容，对内容进行hash计算，得到一个hash值。只要内容不变，得到的结果一定是不变的。

​	但是不能通过得到的值反向得到原内容。所以hash算法是单向不可逆的算法。

​	**可能出现问题：**原内容不一样，经过hash计算后得到的结果一样的，这种情况称为hash碰撞。

​	<font style='color:red'>**String类型**</font>中的hashcode()方法。算法中数字31称为hash因子。定义hash因子时尽量选择一个靠近2的n次方的一个质数。可以在一定程度上减少hash碰撞。最后选择了一个不大，不小的hash因子31.

```java
public static int hashCode(byte[] value) {
  int h = 0;
  for (byte v : value) {
    h = 31 * h + (v & 0xff);
  }
  return h;
}
```

### 装填因子/加载因子

哈希表的长度和表中的记录数的比例--装填因子： 

装填因子=表中的记录数/哈希表的长度， 4/ 16  =0.25  8/ 16=0.5

如果装填因子越小，表明表中还有很多的空单元，则添加发生冲突的可能性越小；而装填因子越大，则发生冲突的可能性就越大，在查找时所耗费的时间就越多。 有相关文献证明当装填因子在0.5左右时候，Hash性能能够达到最优。 

<span style="color:red">因此，一般情况下，装填因子取经验值0.5</span>。 



## 二. HashMap 常见问题

 	在JDK1.8中有一些变化，当链表的存储数据个数大于等于8的时候，不再采用链表存储，而采用红黑树存储结构。这么做主要是查询的时间复杂度上，链表为O(n)，而红黑树一直是O(logn)。如果冲突多，并且超过8长度小于6 会自动转成链表结构，采用红黑树来提高效率

### 常见问题

**问题一**

​	存储在Node中的hash值, 是否就是key的hashCode()？

```java
static final int hash(Object key) {
  int h;
  //hashCode和右移16进行按位异或运算
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

​	答案：不是。存储的是对Key先做hashCode()计算, 然后再无符号右位移16, 再按位异或

**问题二**

​	如何知道一个节点到底存储在Hash表(散列表)的哪个位置？（如何查询数据）

​	答案：根据key计算相关的hash值(并不是简单的hashCode()), (数组长度-1) & hash进行计算得出具体的下标, 如果下标只有这一个节点, 直接返回, 非一个节点, 继续在链表或者红黑树中查找 。

链表中查找的方法是equals，而红黑树的调用的方式是compareTo

**问题三**

​	什么时候需要把链表转为红黑树？

​	答案：链表的节点数大于8(从0开始的, 多以判断条件为 >=7), 数组的长度必须大于等于64,这个时候就会转成红黑树 要么就会数组的扩容。

**问题四**

​	什么时候扩容？

​	答案：

​		情况一：

​        HashMap的Size达到Hash中数组长度*loadFactor(扩容因子)时扩容。即比threshold大, 进行扩容。每次扩容为原数组长度的一倍(<< 1)

​		情况二：

​			Hash表中某个链表长度到达8，且Hash表中数组的长度小于64.

**问题五**

​	Hash表中数组最大长度为多少？

​	答案：最大长度为 1<<30. 即：2的30次方法。

​	计算操作时，发现Hash表中数组长度为2的倍数效率最高，需要一直保持长度为2的倍数。数组长度最大取值为2的31次方减一。所以里面最大的2的倍数为2的30次方。

**问题六**

​	1. Hash表中使用的是单向链表还是双向链表?

​	答案：单项链表

​	2. 数组扩容时, 链表使用的是尾加还是头加?

​	答案：JDK1.8尾插法   JDK1.7及以前采用的是头插法

**问题七**

​	链表转为红黑树时，数组中是所有的链表都转为红黑树，还是什么情况？

​	答案：只有数组里某个下标中的节点个数>8, 并且数组长度>=64, 该下标中的链表转换为红黑树

**问题八**

​	为什么java8中长度超过8以后将链表变为红黑树？

​	答案：红黑树的查询效率高于链表

**问题九**

​	为什么选择8作为转换值？

​	答案：元素个数为8的红黑树中，高度为：4.最多查找4次就能找到需要的的值，长度为8的链表，最多找7次。

​	例如长度为4就转换。红黑树高度为3，最多找3次。链表最多3次。

​	例如长度为7就转换。红黑树高度3，最多找3次。链表最多6次。多找3次和转换的性能消耗比较不值得。

​	在源码上可以看出，在理想状态下，受随机分布的 hashCode 影响，链表中的节点遵循泊松分布，而且根据统计，链表中节点数是 8 的概率已经接近千分之一，而且此时链表的性能已经很差了，所以在这种比较罕见和极端的情况下，才会把链表转变为红黑树。

**问题十**

为什么jdk1.8后改为尾插法？

主要是因为头插法在多线程扩容情况下会引起链表环。那什么是链表环呢？

线程1，第一节点为A，第二节点为B后面就没有了，遍历过程为A->B然后B没有后面节点即遍历结束。

这时线程1挂起。线程2引发扩容，扩容后为B->A。这时线程1遍历就会发现A的下一节点是B，会发现遍历B时B还有后续的节点为A，这样就出样链表环了。

### 总结HashMap底层原理（特别常见面试问题）

​	从Java8开始HashMap底层由数组+链表变成数组+链表+红黑树。

​	使用HashMap时，当使用无参构造方法实例化时，设置扩容因子为默认扩容因子0.75。

​	当向HashMap添加内容时，会对Key做Hash计算，把得到的Hash值和数组长度-1按位与，计算出存储的位置。

​	如果数组中该没有内容, 直接存入数组中(Node节点对象), 该下标中有Node对象了, 把内容添加到对应的链表或红黑树中。

​	如果添加后链表长度大于等于8，会判断数组的长度是否大于等于64，如果小于64对数组扩容，扩容长度为原长度的2倍，扩容后把原Hash表内容重新放入到新的Hash表中。如果Hash长度大于等于64会把链表转换红黑树。

​	最终判断HashMap中元素个数是否已经达到扩容值(threshold)，如果达到扩容值，需要进行扩容，扩容一倍。

​	反之，如果删除元素后，红黑树的元素个数小于等于6，由红黑树转换为链表。

| JDK1.7    | JDK1.8                               |                                     |
| --------- | ------------------------------------ | ----------------------------------- |
| 存储      | 数组+链表                            | 数组+链表+红黑树                    |
| 位置算法  | h & (length-1)                       | h & (length-1)                      |
| 链表超过8 | 链表                                 | 红黑对(链表超过8且数组长度超64)     |
| 节点结构  | Entry<K,V> implements Map.Entry<K,V> | Node<K,V> implements Map.Entry<K,V> |
| 插法      | 头插法(扩容环化造成死循环)           | 尾插法                              |

## 三. HashMap 底层源码分析（1.7）

### 1.结构简介

JDK1.7及其之前，HashMap底层是一个table数组+链表实现的哈希表存储结构 

链表的每个节点就是一个Entry，其中包括：键key、值value、键的哈希码hash、执行下一个节点的引用next四部分

~~~java
static class Entry<K, V> implements Map.Entry<K, V> {
    final K key; //key
    V value;//value
    Entry<K, V> next; //指向下一个节点的指针
    int hash;//哈希码
}
~~~

### 2.内部成员变量含义

JDK1.7中HashMap的主要成员变量及其含义

~~~java
public class HashMap<K, V> implements Map<K, V> {
//哈希表主数组的默认长度
    static final int DEFAULT_INITIAL_CAPACITY = 16; 
//默认的装填因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f; 
//主数组的引用！！！！
    transient Entry<K, V>[] table; 
    int threshold;//界限值  阈值
    final float loadFactor;//装填因子
    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }
}
~~~

### 3.put()方法

 调用put方法添加键值对。哈希表三步添加数据原理的具体实现；是计算key的哈希码，和value无关。特别注意：

1. 第一步计算哈希码时，不仅调用了key的hashCode()，还进行了更复杂处理，目的是尽量保证不同的key尽量得到不同的哈希码

2. 第二步根据哈希码计算存储位置时，使用了位运算提高效率。同时也要求主数组长度必须是2的幂）

3. 第三步添加Entry时添加到链表的第一个位置，而不是链表末尾

4. 第三步添加Entry是发现了相同的key已经存在，就使用新的value替代旧的value，并且返回旧的value

~~~java
public class HashMap {
    public V put(K key, V value) {
       //如果key是null，特殊处理
        if (key == null) return putForNullKey(value);
        //1.计算key的哈希码hash 
        int hash = hash(key);
        //2.将哈希码代入函数，计算出存储位置  y= x%16；
        int i = indexFor(hash, table.length);
        //如果已经存在链表，判断是否存在该key，需要用到equals()
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
            //如找到了,使用新value覆盖旧的value，返回旧value
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) { 
                V oldValue = e.value;// the United States
                e.value = value;//America
                e.recordAccess(this);
                return oldValue;
            }
        }
        //添加一个结点
        addEntry(hash, key, value, i);
        return null;
    }
final int hash(Object k) {
    int h = 0;
    h ^= k.hashCode();
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
static int indexFor(int h, int length) {
//作用就相当于y = x%16,采用了位运算，效率更高
    return h & (length-1);
 }
}
~~~

### 4.addEntry()方法

添加元素时如达到了阈值，需扩容，每次扩容为原来主数组容量的2倍

~~~java
void addEntry(int hash, K key, V value, int bucketIndex) {
    //如果达到了门槛值，就扩容，容量为原来容量的2位 16---32
    if ((size >= threshold) && (null != table[bucketIndex])) {
        resize(2 * table.length);
        hash = (null != key) ? hash(key) : 0;
        bucketIndex = indexFor(hash, table.length);
    }
    //添加节点
    createEntry(hash, key, value, bucketIndex);
}
~~~

### 5.get()方法

调用get方法根据key获取value。

哈希表三步查询数据原理的具体实现

 其实是根据key找Entry，再从Entry中获取value即可

~~~java
public V get(Object key) {
    //根据key找到Entry（Entry中有key和value）
    Entry<K,V> entry = getEntry(key);
    //如果entry== null,返回null，否则返回value
    return null == entry ? null : entry.getValue();
}
~~~

## 四. HashMap 底层源码分析（1.8）

### **1.** 基本属性

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {
  //序列化和反序列化时使用相同的id
  private static final long serialVersionUID = 362498820763181265L;
  //初始化容量
  static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
  //最大容量
  static final int MAXIMUM_CAPACITY = 1 << 30;
  //默认负载因子
  static final float DEFAULT_LOAD_FACTOR = 0.75f;
  //树形阈值
  static final int TREEIFY_THRESHOLD = 8;
  //取消阈值
  static final int UNTREEIFY_THRESHOLD = 6;
  //最小树形容量
  static final int MIN_TREEIFY_CAPACITY = 64;
  //节点
  transient Node<K,V>[] table;
  //存储键值对的个数
  transient int size;
  //散列表被修改的次数
  transient int modCount; 
  //扩容临界值
  int threshold;
  //负载因子
  final float loadFactor;
}
```

### 2. 构造方法

```java
       //和1.7区别不大
       //无参构造器，加载因子默认为0.75
	public HashMap() {
           this.loadFactor = DEFAULT_LOAD_FACTOR;
       }
       //指定容量大小的构造器，但调用了双参的构造器，加载因子0.75
	public HashMap(int initialCapacity) {
           this(initialCapacity, DEFAULT_LOAD_FACTOR);
       }
       //全参构造器
	public HashMap(int initialCapacity, float loadFactor) {
           if (initialCapacity < 0)
               throw new IllegalArgumentException("Illegal initial capacity: " +
                       initialCapacity);
           //HashMap 的最大容量只能是 MAXIMUM_CAPACITY，哪怕传入的数值大于最大容量，也按照最大容量赋值
           if (initialCapacity > MAXIMUM_CAPACITY)
               initialCapacity = MAXIMUM_CAPACITY;
           //加载因子必须大于0
           if (loadFactor <= 0 || Float.isNaN(loadFactor))
               throw new IllegalArgumentException("Illegal load factor: " +
                       loadFactor);
           this.loadFactor = loadFactor;
           //设置扩容阈值和1.7类似，目前该阈值不是正真的阈值
           this.threshold = tableSizeFor(initialCapacity);
       }
       //将传入的子Map中的全部元素逐个添加到HashMap中
	public HashMap(Map<? extends K, ? extends V> m) {
           this.loadFactor = DEFAULT_LOAD_FACTOR;
           putMapEntries(m, false);
       }
```

### 3.Node 结点

前 1.7 是 Entry 结点，1.8 则是 Node 结点，其实相差不大，因为都是实现了 Map.Entry （Map 接口中的 Entry 接口）接口，即，实现了 getKey() ， getValue() ， equals(Object o )和 hashCode() 等方法；

~~~java
static class Node<K,V> implements Map.Entry<K,V> {
    //hash 值
    final int hash;
    //键
    final K key;
    //值
    V value;
    //后继，链表下一个结点
    Node<K,V> next;
    //全参构造器
    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
    //返回与此项对应的键
    public final K getKey()        { return key; }
    //返回与此项对应的值
    public final V getValue()      { return value; }
    public final String toString() { return key + "=" + value; }
    //hash 值
    public final int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }
    public final V setValue(V newValue) {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }
    //判断2个Entry是否相等，必须key和value都相等，才返回true  
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof Map.Entry) {
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;
            if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                return true;
        }
        return false;
    }
}
~~~

### 4. 添加键值对

#### 4.1 put()方法

```java
//添加键值对
public V put(K key, V value) {
  /*
   *参数一: 调用hash()方法
   *参数二: 键
   *参数三: 值
   **/
  return putVal(hash(key), key, value, false, true);
}
```

#### 4.2 hash()方法

```java
static final int hash(Object key) {
  int h;
  //hashCode和h移位右移16位进行按位异或运算
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

#### 4.3 putVal()方法

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
boolean evict) {
    //申明tab 和 p 用于操作原数组和结点
    Node<K,V>[] tab; Node<K,V> p;
    int n, i;
    //如果原数组是空或者原数组的长度等于0，那么通过resize()方法进行创建初始化
    if ((tab = table) == null || (n = tab.length) == 0)
        //获取到创建后数组的长度n
        n = (tab = resize()).length;

    //通过key的hash值和 数组长度-1 计算出存储元素结点的数组中位置（和1.7一样）
    //并且，如果该位置为空时，则直接创建元素结点赋值给该位置，后继元素结点为null
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        //否则，说明该位置存在元素
        Node<K,V> e; K k;
        //判断table[i]的元素的key是否与添加的key相同，若相同则直接用新value覆盖旧value
        if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
            //判断是否是红黑树的结点，如果是，那么就直接在树中添加或者更新键值对
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            //否则，就是链表，则在链表中添加或替换
        else {
            //遍历table[i]，并判断添加的key是否已经存在，和之前判断一样，hash和equals
            //遍历完毕后仍无发现上述情况，则直接在链表尾部插入数据
            for (int binCount = 0; ; ++binCount) {
                //如果遍历的下一个结点为空，那么直接插入
                //该方法是尾插法（与1.7不同）
                //将p的next赋值给e进行以下判断
                if ((e = p.next) == null) {
                    //直接创建新结点连接在上一个结点的后继上
                    p.next = newNode(hash, key, value, null);
				//如果插入结点后，链表的结点数大于等7（8-1，即大于8）时，则进行红黑树的转换
				//注意:不仅仅是链表大于8，并且会在treeifyBin方法中判断数组是否为空或数组长度是否小于64
				//如果小于64则进行扩容，并且不是直接转换为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    //完成后直接退出循环
                    break;
                }
                //不退出循环时，则判断两个元素的key是否相同
                //若相同，则直接退出循环，进行下面替换的操作
                if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                //否则，让p指向下一个元素结点
                p = e;
            }
        }
        //接着上面的第二个break，如果e不为空，直接用新value覆盖旧value并且返回旧value
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    //添加成功后，判断实际存在的键值对数量size是否大于扩容阈值threshold（第一次时为12）
    if (++size > threshold)
        //若大于，扩容
        resize();
    //添加成功时会调用的方法（默认实现为空）
    afterNodeInsertion(evict);
    return null;
}
```

#### 4.4 resize()方法

```java
   //该函数有两种使用情况：初始化哈希表或前数组容量过小，需要扩容
        final Node<K,V>[] resize() {
            //获取原数组
            Node<K,V>[] oldTab = table;
            //获取到原数组的容量oldCap
            int oldCap = (oldTab == null) ? 0 : oldTab.length;
            //获取原扩容阈值
            int oldThr = threshold;
            //新的容量和阈值目前都为0
            int newCap, newThr = 0;
            if (oldCap > 0) {
                //如果原数组容量大于等于最大容量，那么不再扩容
                if (oldCap >= MAXIMUM_CAPACITY) {
                    threshold = Integer.MAX_VALUE;
                    return oldTab;
                }
                //而没有超过最大容量，那么扩容为原来的2倍
                else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                        oldCap >= DEFAULT_INITIAL_CAPACITY)
                    //扩容为原2倍
                    newThr = oldThr << 1; // double threshold
            }
            //经过上面的if，那么这步为初始化容量（使用有参构造器的初始化）
            else if (oldThr > 0) // initial capacity was placed in threshold
                newCap = oldThr;
            else {               // zero initial threshold signifies using defaults
                //否则，使用的无参构造器
                //那么，容量为16，阈值为12（0.75*16）
                newCap = DEFAULT_INITIAL_CAPACITY;
                newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
            }
            //计算新的resize的上限
            if (newThr == 0) {
     float ft = (float)newCap * loadFactor;
 newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
          (int)ft : Integer.MAX_VALUE);
            }
            threshold = newThr;
            @SuppressWarnings({"rawtypes","unchecked"})
            //使用新的容量床架一个新的数组
             Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
            //将新的数组引用赋值给table
            table = newTab;
            //如果原数组不为空，那么就进行元素的移动
            if (oldTab != null) {
                //遍历原数组中的每个位置的元素
                for (int j = 0; j < oldCap; ++j) {
                    Node<K,V> e;
                    if ((e = oldTab[j]) != null) {
                        //如果该位置元素不为空，那么上一步获取元素接着置为空
                        oldTab[j] = null;
                        //判断该元素上是否有链表
                        if (e.next == null)
        //如果无链表，确定元素存放位置，
//扩容前的元素位置为 (oldCap - 1) & e.hash ,所以这里的新的位置只有两种可能：1.位置不变，
//2.变为 原来的位置+oldCap，下面会详细介绍
                            newTab[e.hash & (newCap - 1)] = e;
                            //判断是否是树结点，如果是则执行树的操作
                        else if (e instanceof TreeNode)
                            ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                        else { // preserve order
                            //否则，说明该元素上存在链表，那么进行元素的移动
                            //根据变化的最高位的不同，也就是0或者1，将链表拆分开
                            Node<K,V> loHead = null, loTail = null;
                            Node<K,V> hiHead = null, hiTail = null;
                            Node<K,V> next;
                            do {
                                next = e.next;
                                //最高位为0时，则将节点加入 loTail.next
                                if ((e.hash & oldCap) == 0) {
                                    if (loTail == null)
                                        loHead = e;
                                    else
                                        loTail.next = e;
                                    loTail = e;
                                }
                                //最高位为1，则将节点加入 hiTail.next
                                else {
                                    if (hiTail == null)
                                        hiHead = e;
                                    else
                                        hiTail.next = e;
                                    hiTail = e;
                                }
                            } while ((e = next) != null);
//通过loHead和hiHead来保存链表的头结点，然后将两个头结点放到newTab[j]与newTab[j+oldCap]上面去
                            if (loTail != null) {
                                loTail.next = null;
                                newTab[j] = loHead;
                            }
                            if (hiTail != null) {
                                hiTail.next = null;
                                newTab[j + oldCap] = hiHead;
                            }
                        }
                    }
                }
            }
            return newTab;
        }
```

### 

## 三. TreeMap底层原理

​	TreeMap是数据结构中红黑树的具体实现。	

​	1. 按照红黑树要求，将节点插入到树中。

​	2. 新增节点默认为红色，父子节点出现两个红色, 需要进行左旋转或右旋转, 旋转可以理解为父节点向左转动还是向右转动, 必须保证最终根节点为黑色。

## 四. TreeSet和HashSet

​    TreeSet和HashSet底层是TreeMap和HashMap。

​	把Set的值当做Map的Key，Map中Value存储new Object()



