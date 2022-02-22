#### 1. Java集合概述

***

Java集合主要是由2个接口派生而来，一个是`Collection`接口，存放单一元素，一个是`Map`接口，存放键值对，Collections接口下主要存在三个子接口：`List、Queue、Set`

![](E:\git repository\LearningNotes\images\Java集合框架.png)

- `List`(顺序存放)：存储的元素是有序的，可重复的
- `Queue`(实现排队功能的叫号机)：按照特定的排队规则来确定先后顺序，存储的元素是有序的，可重复的
- `Set`(独一无二)：存储的元素是无序的，不可重复的
- `Map`(key->value):使用键值对存储，key是无序的，不可重复的，value是无序的，可重复的

set：

1. 无序性不等于随机性，存储的数据在底层数组中并非按照数组索引的顺序添加，而是根据数据计算的哈希值。
2. 为了保证不可重复性，要求向 Set 中添加的元素，**其所在的类一定要重写 hashCode() 和 equals() 方法（如果是 TreeSet，需要重写 compare() 或 compareTo() 方法）**，重写时应该尽可能保证一致，即相等的对象必须具有相等的散列码。
3. LinkedHashSet 能保证按照添加元素的顺序实现遍历（并非真正有序），是因为它在添加元素的同时，还维护了两个引用，记录此元素的前一个元素和后一个元素。因此，对于频繁的遍历操作，LinkedHashSet 效率高于 HashSet（LinkedHashMap 类似）。

Map：

1. key 是无序、不可重复的，所以使用 Set 存储所有的 key，**且 key 所在的类要重写equals() 和 hashCode() 方法（如果是TreeMap，需要重写compare() 或 compareTo() 方法）**。
2. value 是无序、可重复的，所以使用 Collection 存储所有的 value，且 value 所在的类要重写 equals() 方法，以保证 `containsValue`() 方法能正确返回。
3. 一个键值对 key-value 构成了一个 Entry 对象，Entry是无序、不可重复的，所以使用 Set 存储所有的 Entry。



#### 2. 集合框架底层数据总结

**List接口**

- ArrayList：`Object`数组
- Vector/Stack：`Object`数组
- LinkedList：双向链表

**Queue接口**

- PriorityQueue：`Object`数组来实现二叉堆
- Deque/ArrayDeque：`Object`数组+双指针

**Set接口**

- `HashSet`:基于 `HashMap` 实现的，底层采用 `HashMap` 来保存元素
- `LinkedHashSet`:`LinkedHashSet` 是 `HashSet` 的子类，并且其内部是通过 `LinkedHashMap` 来实现的
- `TreeSet`:红黑树(自平衡的排序二叉树)

**Map接口**

- HashMap：`HashMap`的主体是数组，组成是数组+链表或红黑树，链表主要为了解决哈希冲突，当链表长度大于阈值时（默认是8），这时候会先判断，当前数组大小是否大于64，如果不满足，则会先进行数组扩容，而不是转为红黑树，如果满足，则将链表转化为红黑树，以减少搜索时间。拉链式散列结构

- LinkedHashMap：`LinkedHashMap`继承自HashMap，所以它也是拉链式散列结构，它的组成也是数组+链表或者红黑树，只不过按照以上的机构基础上，增加了一条双向链表，从而保证按照添加元素的顺序遍历HashMap.

  [《LinkedHashMap 源码详细分析（JDK1.8）》](https://www.imooc.com/article/22931)

- TreeMap:红黑树(自平衡的排序二叉树)

- HashTable：数组+链表组成的，数组是 `Hashtable` 的主体，链表则是主要为了解决哈希冲突而存在的



#### 3. **Collection子接口之List**

***

##### 3.1 ArrayList和Vector的区别

`ArrayList` 是 `List` 的主要实现类，底层使用 `Object[ ]`存储，适用于频繁的查找工作，线程不安全 ；

`Vector` 是 `List` 的古老实现类，底层使用`Object[ ]` 存储，线程安全的。

1. **是否保证线程安全**：Vector是线程安全的，这个类中所有的方法都是同步的，可以多个线程同时安全的访问一个Vector对象，但是单个线程访问时，会在同步操作在耗费大量的时间；ArrayList不是线程安全的，不需要保证线程安全时，推荐使用ArrayList.
2. **扩容机制**：当存储空间不足时，Vector默认为原来的2倍，ArrayList默认为原来的1.5倍。

##### 3.2 ArrayList和LinkedList的区别

1. **是否保证线程安全：** `ArrayList` 和 `LinkedList` 都是不同步的，也就是不保证线程安全；

2. **底层数据结构：** `Arraylist` 底层使用的是 **`Object` 数组**；`LinkedList` 底层使用的是 **双向链表** 数据结构

3. **插入和删除是否受元素位置的影响：**

-  `ArrayList` 采用数组存储，所以插入和删除元素的时间复杂度受元素位置的影响。 比如：执行`add(E e)`方法的时候， `ArrayList` 会默认在将指定的元素追加到此列表的末尾，这种情况时间复杂度就是 O(1)。但是如果要在指定位置 i 插入和删除元素的话（`add(int index, E element)`）时间复杂度就为 O(n-i)。因为在进行上述操作的时候集合中第 i 和第 i 个元素之后的(n-i)个元素都要执行向后位/向前移一位的操作。
- `LinkedList` 采用链表存储，所以，如果是在头尾插入或者删除元素不受元素位置的影响（`add(E e)`、`addFirst(E e)`、`addLast(E e)`、`removeFirst()` 、 `removeLast()`），近似 O(1)，如果是要在指定位置 `i` 插入和删除元素的话（`add(int index, E element)`，`remove(Object o)`） 时间复杂度近似为 O(n) ，因为需要先移动到指定位置再插入。

4. **是否支持快速随机访问：** `LinkedList` 不支持高效的随机元素访问，而 `ArrayList` 支持。快速随机访问就是通过元素的序号快速获取元素对象(对应于`get(int index)`方法)。

5. **内存空间占用：** ArrayList 的空 间浪费主要体现在在 list 列表的结尾会预留一定的容量空间，而 LinkedList 的空间花费则体现在它的每一个元素都需要消耗比 ArrayList 更多的空间（因为要存放直接后继和直接前驱以及数据）。

##### 3.3 补充内容：RandomAccess 接口

```Java
public interface RandomAccess {
}
```

查看源码我们发现，`RandomAccess` 接口中什么都没有定义，所以 `RandomAccess` 接口是一个标识性接口。标识什么？ **标识实现这个接口的类具有随机访问功能。**

在 Collections 工具类的 `binarySearch()`方法中，它要判断传入的 list 是否为 `RamdomAccess` 的实例，如果是，调用`indexedBinarySearch()`方法，如果不是，那么调用`iteratorBinarySearch()`方法。

```Java
jpublic static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key) {
        if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
            return Collections.indexedBinarySearch(list, key);
        else
            return Collections.iteratorBinarySearch(list, key);
    }
```

这也是为什么`ArrayList` 实现了 `RandomAccess` 接口， 而 `LinkedList` 没有实现。`ArrayList` 底层是数组，而 `LinkedList` 底层是链表。数组天然支持随机访问，时间复杂度为 O(1)，所以称为快速随机访问。链表需要遍历到特定位置才能访问特定位置的元素，时间复杂度为 O(n)，所以不支持快速随机访问。 `RandomAccess` 接口只是标识，并不是说 `ArrayList` 实现 `RandomAccess` 接口才具有快速随机访问功能的！

**下面再总结一下 list 的遍历方式选择：**

- 实现了 `RandomAccess` 接口的 list，优先选择普通 for 循环 ，其次 `foreach`
- 未实现 `RandomAccess`接口的 list，优先选择 iterator 遍历（`foreach` 遍历底层也是通过 iterator 实现的），大size的数据，千万不要使用普通for循环



#### 4. Collection子接口之Set

***

##### 4.1 `Comparator`和`Comparable`接口的比较

`Comparable`是排序接口

如果一个类实现了`Comparable`接口，就意味着该类支持排序，如果list列表包含了这个类的对象，可以直接使用`Collections.sort(list)`进行排序，数组也是如此，可以直接使用`Arrays.sort(nums)`;对于`TreeSet`和`TreeMap`，可以直接作为元素加入，不需要指定别的比较器

```Java
package java.lang;
import java.util.*;

public interface Comparable<T> {
    public int compareTo(T o);
}
```

`Comparator` 是比较器接口。

我们若需要控制某个类的次序，而该类本身不支持排序(即没有实现`Comparable`接口)；那么，我们可以建立一个“该类的比较器”来进行排序。这个“比较器”只需要实现`Comparator`接口即可。

```Java
package java.util;

public interface Comparator<T> {

    int compare(T o1, T o2);

    boolean equals(Object obj);
}
```

```Java
// 定制排序的用法
Collections.sort(arrayList, new Comparator<Integer>() {

    @Override
    public int compare(Integer o1, Integer o2) {
        return o2.compareTo(o1);
    }
});
```



Comparable是排序接口；若一个类实现了Comparable接口，就意味着“该类支持排序”。
而Comparator是比较器；我们若需要控制某个类的次序，可以建立一个“该类的比较器”来进行排序。

Comparable相当于“内部比较器”，而Comparator相当于“外部比较器”。

##### 4.2 无序性和不可重复性

1、什么是无序性？无序性不等于随机性 ，无序性是指存储的数据在底层数组中并非按照数组索引的顺序添加 ，而是根据数据的**哈希值**决定的。

2、什么是不可重复性？不可重复性是指添加的元素需要按照 `equals()`判断 ，并且返回 false，需要同时重写 `equals()`方法和 `HashCode()`方法。由HashSet添加元素的过程决定

##### 4.3 HashSet添加元素

当向 HashSet 中添加元素 A 时，**首先调用 hash() 方法计算元素 A 的哈希值，其中 hash() 又调用元素 A 所在类的 hashCode() 方法**，然后此==哈希值==通过某种算法计算出在 HashSet 底层数组中的存放位置（即索引位置），判断此位置是否已经有元素：

1. 如果此位置上没有其他元素，则元素 A 添加成功；
2. 如果此位置上有其他元素 B（或以链表或红黑树形式存在的多个元素），则比较元素 A 与元素 B 的哈希值：如果哈希值不同，则添加元素 A 成功；
3. 如果哈希值相同，则**进一步调用元素 A 所在类的 equals() 方法**：如果返回 false，则添加元素 A 成功；如果返回 true，则添加元素 A 失败。

对于添加成功的情况 2 和情况 3 而言，当采用“拉链法”解决冲突时，在 JDK 7中，元素 A 放到数组中，指向原来的元素（头插法）；而在 JDK 8中，原来的元素在数组中（尾插法），指向元素 A，即“七上八下”。



##### 4.4 比较 HashSet、LinkedHashSet 和 TreeSet 三者的异同

- `HashSet`、`LinkedHashSet` 和 `TreeSet` 都是 `Set` 接口的实现类，都能保证元素唯一，并且都**不是线程安全**的。
- `HashSet`、`LinkedHashSet` 和 `TreeSet` 的主要区别在于底层数据结构不同。`HashSet` 的底层数据结构是哈希表（基于 `HashMap` 实现）。`LinkedHashSet` 的底层数据结构是链表和哈希表，元素的插入和取出顺序满足 FIFO。`TreeSet` 底层数据结构是红黑树，元素是有序的，排序的方式有自然排序和定制排序。
- 底层数据结构不同又导致这三者的应用场景不同。`HashSet` 用于不需要保证元素插入和取出顺序的场景，`LinkedHashSet` 用于保证元素的插入和取出顺序满足 FIFO 的场景，`TreeSet` 用于支持对元素自定义排序规则的场景。



违反 hashCode 和 equals 约定，会导致该类无法与**散列表集合类（HashSet、HashMap、HashTable）**一起正常使用；类似的，违反 compareTo 约定，会导致该类无法与**有序集合类（TreeSet、TreeMap）**，**以及工具类 Collections 和 Arrays** 一起正常使用，它们内部包含了搜索和排序算法。



#### 5. Collection子接口之Queue

***

##### 5.1 Queue 与 Deque 的区别

`Queue` 是单端队列，只能从一端插入元素，另一端删除元素，实现上一般遵循 **先进先出（FIFO）** 规则。

`Queue` 扩展了 `Collection` 的接口，根据 **因为容量问题而导致操作失败后处理方式的不同** 可以分为两类方法: 一种在操作失败后会抛出异常，另一种则会返回特殊值。

| `Queue` 接口 | 抛出异常  | 返回特殊值 |
| ------------ | --------- | ---------- |
| 插入队尾     | add(E e)  | offer(E e) |
| 删除队首     | remove()  | poll()     |
| 查询队首元素 | element() | peek()     |

`Deque` 是双端队列，在队列的两端均可以插入或删除元素。

`Deque` 扩展了 `Queue` 的接口, 增加了在队首和队尾进行插入和删除的方法，同样根据失败后处理方式的不同分为两类：

| `Deque` 接口 | 抛出异常      | 返回特殊值      |
| ------------ | ------------- | --------------- |
| 插入队首     | addFirst(E e) | offerFirst(E e) |
| 插入队尾     | addLast(E e)  | offerLast(E e)  |
| 删除队首     | removeFirst() | pollFirst()     |
| 删除队尾     | removeLast()  | pollLast()      |
| 查询队首元素 | getFirst()    | peekFirst()     |
| 查询队尾元素 | getLast()     | peekLast()      |

事实上，`Deque` 还提供有 `push()` 和 `pop()` 等其他方法，可用于模拟栈。

##### 5.2 ArrayDeque 与 LinkedList 的区别

`ArrayDeque` 和 `LinkedList` 都实现了 `Deque` 接口，两者都具有队列的功能，但两者有什么区别呢？

- `ArrayDeque` 是基于可变长的数组和双指针来实现，而 `LinkedList` 则通过链表来实现。
- `ArrayDeque` 不支持存储 `NULL` 数据，但 `LinkedList` 支持。
- `ArrayDeque` 插入时可能存在扩容过程, 不过均摊后的插入操作依然为 O(1)。虽然 `LinkedList` 不需要扩容，但是每次插入数据时均需要申请新的堆空间，均摊性能相比更慢。

从性能的角度上，选用 `ArrayDeque` 来实现队列要比 `LinkedList` 更好。此外，`ArrayDeque` 也可以用于实现栈。

##### 5.3 说一说 PriorityQueue

`PriorityQueue` 是在 JDK1.5 中被引入的, 其与 `Queue` 的区别在于元素出队顺序是与优先级相关的，即总是优先级最高的元素先出队。

这里列举其相关的一些要点：

- `PriorityQueue` 利用了二叉堆的数据结构来实现的，底层使用可变长的数组来存储数据
- `PriorityQueue` 通过堆元素的上浮和下沉，实现了在 O(logn) 的时间复杂度内插入元素和删除堆顶元素。
- `PriorityQueue` 是非线程安全的，且不支持存储 `NULL` 和 `non-comparable` 的对象。
- `PriorityQueue` 默认是小顶堆，但可以接收一个 `Comparator` 作为构造参数，从而来自定义元素优先级的先后。

`PriorityQueue` 在面试中可能更多的会出现在手撕算法的时候，典型例题包括堆排序、求第K大的数、带权图的遍历等，所以需要会熟练使用才行。



#### 6. Collection子接口之Map

##### 6.1 HashMap和Hashtable的区别

1. 线程是否安全：HashMap 是非线程安全的，Hashtable 是线程安全的；HashTable 内部的方法基本都经过`synchronized` 修饰（如果要保证线程安全的话推荐使用 ConcurrentHashMap ）
2. 效率：因为线程安全的问题，HashMap 要比 Hashtable 效率高一点。另外，Hashtable 基本被淘汰，不要在代码中使用它。
3. 对null key和null value是否支持：HashMap 中，null 可以作为键，这样的键只有一个，可以有一个或多个键所对应的值为 null。但是在 Hashtable 中 put 进的键值只要有一个 null，直接抛出 NullPointerException。
4. 初始容量大小和扩容机制大小：①创建时如果不指定容量初始值，Hashtable 默认的初始大小为11，之后每次扩充，容量变为原来的 2n+1。HashMap 默认的初始化大小为16。之后每次扩充，容量变为原来的 2 倍。②创建时如果给定了容量初始值，那么 Hashtable 会直接使用你给定的大小，而 HashMap 会将其扩充为2的幂次方大小，也就是说 HashMap 总是使用2的幂作为哈希表的大小。
5. 底层数据结构：JDK 8 以后的 HashMap 在解决哈希冲突时有了较大的变化，当链表长度大于阈值（默认为8）时，将链表转化为红黑树，以减少搜索时间。Hashtable 没有这样的机制。

##### 6.2 HashMap的底层实现

JDK 8 之前 HashMap 底层是 **数组和链表** 结合在一起使用也就是 **链表散列**。**HashMap 通过 key 的 hashCode 经过扰动函数处理过后得到 hash 值，然后通过 (n - 1) & hash 判断当前元素存放的位置（ n 指的是数组的长度），如果当前位置存在元素的话，就判断该元素与要存入的元素的 hash 值以及 key 是否相同，如果相同的话，直接覆盖，不相同就通过拉链法解决冲突。**

**所谓扰动函数指的就是 HashMap 的 hash 方法。使用 hash 方法也就是扰动函数是为了防止一些实现比较差的 hashCode() 方法，换句话说使用扰动函数之后可以减少碰撞。**

**JDK 8 HashMap 的 hash 方法源码:**

JDK 8 的 hash方法 相比于 JDK 7 hash 方法更加简化，但是原理不变。

```Java
static final int hash(Object key) {
      int h;
      // key.hashCode()：返回散列值也就是hashcode
      // ^ ：按位异或
      // >>>:无符号右移，忽略符号位，空位都以0补齐
      return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }
```

相比于之前的版本， JDK 8 之后在解决哈希冲突时有了较大的变化，当链表长度大于阈值（默认为 8）时，将链表转化为红黑树，以减少搜索时间。

> TreeMap、TreeSet 以及 JDK 8 之后的 HashMap 底层都用到了红黑树。红黑树就是为了解决二叉查找树的缺陷，因为二叉查找树在某些情况下会退化成一个线性结构。



##### 6.3 曾经死循环问题

##### 6.4 ConcurrentHashMap 线程安全的具体实现方式/底层具体实现

###### JDK 7

首先将数据分为一段一段的存储，然后给每一段数据配一把锁，当一个线程占用锁访问其中一个段数据时，其他段的数据也能被其他线程访问。

**ConcurrentHashMap 是由 Segment 数组结构和 HashEntry 数组结构组成**。

Segment 实现了 ReentrantLock,所以 Segment 是一种可重入锁，扮演锁的角色。HashEntry 用于存储键值对数据。

```
static class Segment<K,V> extends ReentrantLock implements Serializable {
}
```

一个 ConcurrentHashMap 里包含一个 Segment 数组。Segment 的结构和HashMap类似，是一种数组和链表结构，一个 Segment 包含一个 HashEntry 数组，每个 HashEntry 是一个链表结构的元素，每个 Segment 守护着一个HashEntry数组里的元素，当对 HashEntry 数组的数据进行修改时，必须首先获得对应的 Segment的锁。

###### JDK 8

ConcurrentHashMap取消了Segment分段锁，采用CAS和synchronized来保证并发安全。数据结构跟HashMap8的结构类似，数组+链表/红黑二叉树。Java 8在链表长度超过一定阈值（8）时将链表（寻址时间复杂度为O(N)）转换为红黑树（寻址时间复杂度为O(log(N))）

synchronized只锁定当前链表或红黑二叉树的首节点，这样只要hash不冲突，就不会产生并发，效率又提升N倍。