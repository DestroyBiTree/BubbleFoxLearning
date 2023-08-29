# 集合源码分析

**主要内容**

- 哈希表

- HashMap源码分析
- TreeMap源码分析
- HashSet源码分析
- TreeSet源码分析

**学习目标**

| 知识点          | 要求 |
| --------------- | ---- |
| 哈希表          | 掌握 |
| HashMap源码分析 | 掌握 |
| TreeMap源码分析 | 掌握 |
| HashSet源码分析 | 掌握 |
| TreeSet源码分析 | 掌握 |



## 一. 哈希表

### 1.引入hash表

在无序数组中按照内容查找，效率低下，时间复杂度是O（n）

![image-20220322191941583](images/image-20220322191941583.png) 

在有序数组中按照内容查找，可以使用折半查找，时间复杂度O（log2n）

![image-20220322192002213](images/image-20220322192002213.png) 

**问题**：按照内容查找，能否也不进行比较，而是通过计算得到地址，实现类似数组按照索引查询的高效率呢O（1）

有！！！哈希表来实现

### 2.哈希表的结构和特点

 hash表 也叫散列表；特点：快  很快  神奇的快 

  结构：结构有多种。最流行、最容易理解：顺序表+链表 

  主结构：顺序表，每个顺序表的节点在单独引出一个链表

![image-20220322192130733](images/image-20220322192130733.png) 

### 3. 哈希表是如何添加数据

1) 计算哈希 码(调用hashCode(),结果是一个int值，整数的哈希码取自身即可) 

2) 计算在哈希表中的存储位置  y=k(x)=x%11

    x:哈希码  k(x) 函数y：在哈希表中的存储位置 

3) 存入哈希表 

    情况1：一次添加成功 

    情况2：多次添加成功（出现了冲突，调用equals()和对应链表的元素进行比较，比较到最后，结果都是false，创建新节点，存储数据，并加入链表末尾） 

    情况3：不添加（出现了冲突，调用equals()和对应链表的元素进行比较， 经过一次或者多次比较后，结果是true，表明重复，不添加） 

 **结论1**：哈希表添加数据快（3步即可，不考虑冲突） 

 **结论2**：唯一、无序 

![image-20220322192317300](images/image-20220322192317300.png) 

### 4. 哈希表更多

#### 4.1 如何查询数据 

  添加数据的过程是相同的 

 	情况1：一次找到  23 
 	
 	情况2：多次找到  67  

​	 情况3：找不到  100 200 

  结论1：哈希表查询数据快  

#### 4.2 hashCode和equals到底有什么神奇的作用

hashCode():计算哈希码，是一个整数，根据哈希码可以计算出数据在哈希表中的存储位置 

equals()：添加时出现了冲突，需要通过equals进行比较，判断是否相同；查询时也需要使用equals进行比较，判断是否相同  

#### 4.3 各种类型数据的哈希码应该如何获取 hashCode()

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

  **处理冲突(hash碰撞)的方法** 

​     链地址法(Java hashmap就是这么做的)/ 再散列法(重新的计算hash) /  建立一个公共溢出区 

### 5. 装填因子/加载因子

哈希表的长度和表中的记录数的比例--装填因子： 

   如果Hash表的空间远远大于最后实际存储的记录个数，则造成了很大的空间浪费， 如果选取小了的话，则容易造成冲突。 在实际情况中，一般需要根据最终记录存储个数和关键字的分布特点来确定Hash表的大小。还有一种情况时可能事先不知道最终需要存储的记录个数，则需要动态维护Hash表的容量，此时可能需要重新计算Hash地址。 

   装填因子=表中的记录数/哈希表的长度， 4/ 16  =0.25  8/ 16=0.5

   如果装填因子越小，表明表中还有很多的空单元，则添加发生冲突的可能性越小；而装填因子越大，则发生冲突的可能性就越大，在查找时所耗费的时间就越多。 有相关文献证明当装填因子在0.5左右时候，Hash性能能够达到最优。 

<span style="color:red">因此，一般情况下，装填因子取经验值0.5</span>。 



## 二. HashMap 底层源码分析(JDK1.7及以前)（特别常见面试题）

### 1.结构简介

JDK1.7及其之前，HashMap底层是一个table数组+链表实现的哈希表存储结构 

![image-20220322193701533](images/image-20220322193701533.png) 

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



## 三. HashMap 底层源码分析(JDK1.8及以后)（特别常见面试题）

 	在JDK1.8中有一些变化，当链表的存储数据个数大于等于8的时候，不再采用链表存储，而采用红黑树存储结构。这么做主要是查询的时间复杂度上，链表为O(n)，而红黑树一直是O(logn)。如果冲突多，并且超过8长度小于6 会自动转成链表结构，采用红黑树来提高效率

![image-20220322194031114](images/image-20220322194031114.png) 

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

### 5. 问题

#### **5.1** 问题一

​	存储在Node中的hash值, 是否就是key的hashCode()？

```java
static final int hash(Object key) {
  int h;
  //hashCode和右移16进行按位异或运算
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

​	答案：不是。存储的是对Key先做hashCode()计算, 然后再无符号右位移16, 再按位异或

#### 5.2 问题二

​	如何知道一个节点到底存储在Hash表(散列表)的哪个位置？（如何查询数据）

​	答案：根据key计算相关的hash值(并不是简单的hashCode()), (数组长度-1) & hash进行计算得出具体的下标, 如果下标只有这一个节点, 直接返回, 非一个节点, 继续在链表或者红黑树中查找 。

链表中查找的方法是equals，而红黑树的调用的方式是compareTo

#### 5.3 问题三

​	什么时候需要把链表转为红黑树？

​	答案：链表的节点数大于8(从0开始的, 多以判断条件为 >=7), 数组的长度必须大于等于64,这个时候就会转成红黑树 要么就会数组的扩容。

#### 5.4 问题四

​	什么时候扩容？

​	答案：

​		情况一：

​        HashMap的Size达到Hash中数组长度*loadFactor(扩容因子)时扩容。即比threshold大, 进行扩容。每次扩容为原数组长度的一倍(<< 1)

​		情况二：

​			Hash表中某个链表长度到达8，且Hash表中数组的长度小于64.

#### 5.5 问题五

​	Hash表中数组最大长度为多少？

​	答案：最大长度为 1<<30. 即：2的30次方法。

​	计算操作时，发现Hash表中数组长度为2的倍数效率最高，需要一直保持长度为2的倍数。数组长度最大取值为2的31次方减一。所以里面最大的2的倍数为2的30次方。

#### 5.6 问题六

​	1. Hash表中使用的是单向链表还是双向链表?

​	答案：单项链表

​	2. 数组扩容时, 链表使用的是尾加还是头加?

​	答案：JDK1.8尾插法   JDK1.7及以前采用的是头插法

#### 5.7 问题七

​	链表转为红黑树时，数组中是所有的链表都转为红黑树，还是什么情况？

​	答案：只有数组里某个下标中的节点个数>8, 并且数组长度>=64, 该下标中的链表转换为红黑树

#### 5.8 问题八

​	为什么java8中长度超过8以后将链表变为红黑树？

​	答案：红黑树的查询效率高于链表

#### 5.9 问题九

​	为什么选择8作为转换值？

​	答案：元素个数为8的红黑树中，高度为：4.最多查找4次就能找到需要的的值，长度为8的链表，最多找7次。

​	例如长度为4就转换。红黑树高度为3，最多找3次。链表最多3次。

​	例如长度为7就转换。红黑树高度3，最多找3次。链表最多6次。多找3次和转换的性能消耗比较不值得。

​	在源码上可以看出，在理想状态下，受随机分布的 hashCode 影响，链表中的节点遵循泊松分布，而且根据统计，链表中节点数是 8 的概率已经接近千分之一，而且此时链表的性能已经很差了，所以在这种比较罕见和极端的情况下，才会把链表转变为红黑树

![image-20220322195032472](images/image-20220322195032472.png) 

### 6. 总结HashMap底层原理（特别常见面试问题）

​	从Java8开始HashMap底层由数组+链表变成数组+链表+红黑树。

​	使用HashMap时，当使用无参构造方法实例化时，设置扩容因子为默认扩容因子0.75。

​	当向HashMap添加内容时，会对Key做Hash计算，把得到的Hash值和数组长度-1按位与，计算出存储的位置。

​	如果数组中该没有内容, 直接存入数组中(Node节点对象), 该下标中有Node对象了, 把内容添加到对应的链表或红黑树中。

​	如果添加后链表长度大于等于8，会判断数组的长度是否大于等于64，如果小于64对数组扩容，扩容长度为原长度的2倍，扩容后把原Hash表内容重新放入到新的Hash表中。如果Hash长度大于等于64会把链表转换红黑树。

​	最终判断HashMap中元素个数是否已经达到扩容值(threshold)，如果达到扩容值，需要进行扩容，扩容一倍。

​	反之，如果删除元素后，红黑树的元素个数小于等于6，由红黑树转换为链表。



## 三. TreeMap底层原理

### 1. 介绍

​	TreeMap是数据结构中红黑树的具体实现。	

![image-20220322195158046](images/image-20220322195158046.png) 

### 2. 基本属性

```java
public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable
{
  //比较器，是自然排序，还是定制排序 ，使用final修饰，表明一旦赋值便不允许改变
  private final Comparator<? super K> comparator;
  //红黑树的根节点
  private transient Entry<K,V> root;
  //TreeMap中存放的键值对的数量
  private transient int size = 0;
  //修改的次数
  private transient int modCount = 0;
}
```

### 3. 节点

![image-20220322195211052](images/image-20220322195211052.png) 

```java
static final class Entry<K,V> implements Map.Entry<K,V> {
  K key; //键
  V value; //值
  Entry<K, V> left = null; //左孩子节点
  Entry<K, V> right = null;//右孩子节点
  Entry<K, V> parent; //父节点
  boolean color = BLACK; //节点的颜色，在红黑树中，只有两种颜色，红色和黑色
  //省略 有参构造 无参构造 equals()和hashCode() getter和setter
}
```

### 4. 构造方法

```java
//构造方法，comparator比较器
public TreeMap() {
  comparator = null;
}
//构造方法，提供比较器，用指定比较器排序
public TreeMap(Comparator<? super K> comparator) {
  this.comparator = comparator;
}
```

### 5. 添加键值

#### 5.1 put()方法

```java
public V put(K key, V value) {
  //红黑树的根节点
  Entry<K,V> t = root; 
  //红黑树是否为空
  if (t == null) {
    //检查比较器
    compare(key, key); // type (and possibly null) check
		//创建根节点，因为根节点没有父节点，传入null值。 
    root = new Entry<>(key, value, null);
    //size值=1
    size = 1;
    //改变修改的次数
    modCount++;
    //返回null 
    return null;
  }
  int cmp;
  //声明节点
  Entry<K,V> parent;
  // split comparator and comparable paths
  //获取比较器
  Comparator<? super K> cpr = comparator;
  //如果定义了比较器，采用自定义比较器进行比较
  if (cpr != null) {
    do {
      //将红黑树根节点赋值给parent
      parent = t;
      //添加的key与根节点的值比较大小
      cmp = cpr.compare(key, t.key);
      //如果key < t.key , 指向左子树
      if (cmp < 0)
        t = t.left;
      //如果key > t.key , 指向右子树
      else if (cmp > 0)
        t = t.right;
      //如果它们相等
      else
        //新值替换旧值
        return t.setValue(value);
    } while (t != null);
  }
  //自然排序方式，没有指定比较器
  else {
    //key不能为null
    if (key == null)
      throw new NullPointerException();
    @SuppressWarnings("unchecked")
    //类型转换
    Comparable<? super K> k = (Comparable<? super K>) key;
    do {
      parent = t;
      //添加的key与根节点的值比较大小
      cmp = k.compareTo(t.key);
      // key < t.key
      if (cmp < 0)
        t = t.left;//左孩子
      // key > t.key 
      else if (cmp > 0)
        t = t.right;//右孩子
      else//如果它们相等
        //新值替换旧值
        return t.setValue(value);
    } while (t != null);
  }
  //创建新节点，并指定父节点
  Entry<K,V> e = new Entry<>(key, value, parent);
  //根据比较结果，决定新节点作为父节点的左孩子或右孩子
  if (cmp < 0)
    parent.left = e;
  else
    parent.right = e;
  //新插入节点后重新调整红黑树 
  fixAfterInsertion(e);
  size++;
  modCount++;
  return null;
}
```

#### 5.2 Comparator默认比较器

```java
//比较方法，如果comparator==null ,采用comparable.compartTo进行比较(执行添加key的类型重写之后的比较方法)，否则采用指定比较器比较大小
final int compare(Object k1, Object k2) {
  return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
    : comparator.compare((K)k1, (K)k2);
}
```

#### 5.3 fixAfterInsertion()方法

红黑树在新增节点过程中比较复杂，复杂归复杂它同样必须要依据上面提到的五点规范

[1]每个节点都只能是红色或者黑色。

[2]根节点是黑色。

[3]每个叶节点（NIL节点，NULL空节点）是黑色的。

[4]每个红色节点的两个子节点都是黑色 (从每个叶子到根的路径上不会有两个连续的红色节点) 。

[5]从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。

由于规则1、2、3基本都会满足，下面我们主要讨论规则4、5。

假设我们这里有一棵最简单的树，我们规定新增的节点为N、它的父节点为P、P的兄弟节点为U、P的父节点为G。

对于新节点的插入有如下三个关键地方：

1、插入新节点总是红色节点。

2、如果插入节点的父节点是黑色，能维持性质 。

3、如果插入节点的父节点是红色，破坏了性质。

故插入算法就是通过重新着色或旋转，来维持性质，可能出现的情况如下：

【情况一】为根节点

若新插入的节点N没有父节点，则直接当做根据节点插入即可，同时将颜色设置为黑色。

【情况二】父结点为黑色

那么插入的红色节点将不会影响红黑树的平衡，直接插入即可。

【情况三】父节点和叔节点都为红色

当叔父结点为红色时，无需进行旋转操作，只要将父和叔结点变为黑色，将祖父结点变为红色即可

 但是经过上面的处理，可能G节点的父节点也是红色，这个时候我们需要将G节点当做新增节点递归处理。

![image-20220322195839659](images/image-20220322195839659.png) 

【情况四】父红，叔黑，并且新增节点和父节点都为左子树

 对于这种情况先已P节点为中心进行右旋转，在旋转后产生的树中，节点P是节点N、G的父节点。

但是这棵树并不规范，所以我们将P、G节点的颜色进行交换，使之其满足规范。（这个位置的U可能不存在，因为NULL节点也是黑色）

![image-20220323192157943](images/image-20220323192157943.png) 

【情况五】父红，叔黑，并且新增节点和父节点都为右子树

对于这种情况先已P节点为中心进行左旋转，在旋转后产生的树中，节点P是节点G、N的父节点。但是这棵树并不规范，所以我们将P、G节点的颜色进行交换，使之其满足规范。

![image-20220322195910373](images/image-20220322195910373.png) 

【情况六】父红，叔黑，并且新增节点为左子树，父节点为右子树

对于这种情况先以N节点为中心进行右旋转，在旋转后产生的树中，节点N是节点P、X的父节点。然后再以N节点为中心进行左旋转，在旋转后产生的树中，节点N是节点P、G的父节点。但是这棵树并不规范，所以我们将N、G节点的颜色进行交换，使之其满足规范。

![image-20220322195943988](images/image-20220322195943988.png) 

【情况七】父红，叔黑，并且新增节点为右子树，父节点为左子树

 对于这种情况先以N节点为中心进行左旋转，在旋转后产生的树中，节点N是节点P、Y的父节点。然后再以N节点为中心进行右旋转，在旋转后产生的树中，节点N是节点P、G的父节点。但是这棵树并不规范，所以我们将N、G节点的颜色进行交换，使之其满足规范。

![image-20220322200015003](images/image-20220322200015003.png) 

```java
  private void fixAfterInsertion(Entry<K, V> entry) {
        // 循环直到entry不是根节点，并且entry的父节点是红色
        while (null != entry && entry != root && colorOf(entry.parent) == RED) {
            // 当entry的父节点属于左侧节点时
            if (parentOf(entry) == leftOf(parentOf(parentOf(entry)))) {
                // 获取entry的右侧叔叔节点
                Entry<K, V> uncle = rightOf(parentOf(parentOf(entry)));
                // 当叔叔节点为红色时(父红&叔红)
                if (colorOf(uncle) == RED) { // 【情况三】
                    // 将父节点设置为黑色
                    setColor(parentOf(entry), BLACK);
                    // 将叔节点设置为黑色
                    setColor(uncle, BLACK);
                    // 将父节点的父节点设置为红色
                    setColor(parentOf(uncle), RED);
                    // 更新entry，通过循环继续遍历处理
                    // 因为有可能“父节点的父节点的父节点”还是为红色
                    entry = parentOf(parentOf(entry));
                }
                // 当叔叔节点为黑色时(父红&叔黑)
                else { // 【情况四】和【情况七】
                    // 1.当新增节点为右子树时（父红&叔黑&且新增节点为右子树&父左子树）
                    if (entry == rightOf(parentOf(entry))) { // 【情况七】
                        // 把entry的父节点进行左旋
                        rotateLeft(entry.parent);
                    }
                    // 2.新增节点为左子树时
                    // 父红&叔黑&并且新增节点和父节点都为左子树 【情况四】
                    // 设置entry的父节点为黑色
                    setColor(parentOf(entry), BLACK);
                    // 设置entry的父节点的父节点为红色
                    setColor(parentOf(parentOf(entry)), RED);
                    // 设置entry父节点的父节点右旋
                    rotateRight(parentOf(parentOf(entry)));
                }
            }
            // 当entry的父节点属于右侧节点时
            else {
                // 获取entry的左侧叔叔节点
                Entry<K, V> uncle = leftOf(parentOf(parentOf(entry)));
                // 当叔叔节点为红色时(父红&叔红)
                if (colorOf(uncle) == RED) {// 【情况三】
                    // 将父节点设置为黑色
                    setColor(parentOf(entry), BLACK);
                    // 将叔节点设置为黑色
                    setColor(uncle, BLACK);
                    // 将父节点的父节点设置为红色
                    setColor(parentOf(uncle), RED);
                    // 更新entry，通过循环继续遍历处理
                    // 因为有可能“父节点的父节点的父节点”还是为红色
                    entry = parentOf(parentOf(entry));
                }
                // 当叔叔节点为黑色时(父红&叔黑)
                else { // 【情况五】和【情况六】
                    // 1.当新增节点为左子树时（父红&叔黑&且新增节点为左子树&父右子树）
                    if (entry == leftOf(parentOf(entry))) { // 【情况六】
                        // 把entry的父节点进行右旋
                        rotateRight(entry.parent);
                    }
                    // 2.新增节点为右子树时
                    // 父红&叔黑&并且新增节点和父节点都为右子树 【情况五】
                    // 设置entry的父节点为黑色
                    setColor(parentOf(entry), BLACK);
                    // 设置entry的父节点的父节点为红色
                    setColor(parentOf(parentOf(entry)), RED);
                    // 设置entry父节点的父节点左旋
                    rotateLeft(parentOf(parentOf(entry)));
                }
            }
        }
        // 将根节点强制设置为黑色
        setColor(root, BLACK);
    }
    // 此处省略Entry节点类
}
```

### 6. 总结

​	1. 按照红黑树要求，将节点插入到树中。

​	2. 新增节点默认为红色，父子节点出现两个红色, 需要进行左旋转或右旋转, 旋转可以理解为父节点向左转动还是向右转动, 必须保证最终根节点为黑色。

## 四. TreeSet和HashSet

### 1.源码分析

​    TreeSet和HashSet底层是TreeMap和HashMap。

​	把Set的值当做Map的Key，Map中Value存储new Object()

## 五.三代集合对比

### 1.第一代(旧的集合类)

**Vector**

• 实现原理和ArrayList相同，功能相同，都是长度可变的数组结构，很多情况下可以互用

• 两者的主要区别如下

• Vector是早期JDK接口，ArrayList是替代Vector的新接口

• Vector线程安全，效率低下；ArrayList重速度轻安全，线程非安全

• 长度需增长时，Vector默认增长一倍，ArrayList增长50% 

**Hashtable类** 

• 实现原理和HashMap相同，功能相同，底层都是哈希表结构，查询速度快，很多情况下可互用

• 两者的主要区别如下

• Hashtable是早期JDK提供，HashMap是新版JDK提供

• Hashtable继承Dictionary类，HashMap实现Map接口

• Hashtable线程安全，HashMap线程非安全

Hashtable不允许key的null值，HashMap允许null值

~~~java
public class TestVector {
    public static void main(String[] args) {
        //泛型是1.5开始的，重新改写了Vector，ArrayList
        Vector<Integer> v = new Vector<Integer>();        
        v.addElement(123);
        v.addElement(456);
        v.addElement(345);
        v.addElement(100);        
        Enumeration<Integer> en = v.elements();
        while(en.hasMoreElements()){
            Integer elem = en.nextElement();
            System.out.println(elem);
        }
    }
}
~~~

### 2.第二代

我们学习的  List Set  和Map属于第二代

### 3.第三代

在大量并发情况下如何提高集合的效率和安全呢？ (后面线程安全中会讲解)

提供了新的线程同步集合类，位于java.util.concurrent包下，使用Lock锁或者volatile+CAS的无锁化。 

ConcurrentHashMap

CopyOnWriteArrayList 

CopyOnWriteArraySet
