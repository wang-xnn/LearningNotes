#### Java IO流

***

#### 1. 编码与解码

编码是把字符转换成字节保存起来，而解码是把字符转换成字节

如果编码和解码方式不同，就会出现乱码

常用的编码方式有：

- ASCII：1个字节，最多存储256个符号编码

- GBK：汉字编码，GBK18030，每个字由1个、2个或3个字节组成，英文字符由1个字节编码

- UTF：`unicode transformation format`    

  utf-8使用1-4个字节编码，英文字符1个字节，中文字符三个字节

  utf-16 英文字符和中文字符都用2个字节编码

​	![](E:\git repository\LearningNotes\images\utf-8.png)

String的编码方式

String 可以看成一个字符序列，可以指定一个编码方式将它编码为字节序列，也可以指定一个编码方式将一个字节序列解码为 String

```Java
String s = new String("这是一个中文");
byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
String s1 = new String(bytes, StandardCharsets.UTF_8);
System.out.println(s1);
```

```Java
byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
```

#### 2. IO体系总结

IO流的分类：

- 按照流的流向分：可以分为输入流和输出流
- 按照流的操作单位分：可以分为字节流(8 bits)和字符流(16 bits)
- 按照流的角色分：可以分为节点流和处理流

Java IO流共涉及40多个类，这些类看上去很杂乱，但实际上很有规则，而且彼此之间存在非常紧密的联系， Java IO流的40多个类都是从如下4个抽象类基类中派生出来的。

- **InputStream/Reader**： 所有的输入流的抽象基类，前者是字节输入流，后者是字符输入流。
- **OutputStream/Writer**：所有输出流的抽象基类，前者是字节输出流，后者是字符输出流。

| 分类     | 字节输入流         | 字节输出流          | 字符输入流       | 字符输出流        |
| -------- | ------------------ | ------------------- | ---------------- | ----------------- |
| 抽象基类 | **InputStream(~)** | **OutputStream(-)** | **Reader(#)**    | **Writer($)**     |
| 文件     | **File~**          | **File-**           | **File#**        | **File$**         |
| 数组     | ByteArray~         | ByteArray-          | CharArray#       | CharArray$        |
| 管道     | Piped~             | Piped-              | Piped#           | Piped$            |
| 字符串   |                    |                     | String#          | String$           |
| 缓冲     | **Buffered~**      | **Buffered-**       | **Buffered#**    | **Buffered$**     |
| 转换     |                    |                     | **InputStream#** | **OutputStream$** |
| 对象     | **Object~**        | **Object-**         |                  |                   |
| 过滤     | Filter~            | Filter-             | Filter#          | Filter$           |
| 打印     |                    | PrintStream         |                  | Print$            |
| 推回输入 | Pushback~          |                     | Pushback#        |                   |
| 基本数据 | Data~              | Data-               |                  |                   |



**为什么有了字节流，还要字符流**

```markdown
先明确本质：不管是文件读写还是网络发送接收，信息存储的最小单位都是字节
回答：字符是Java虚拟机通过字节转换得到的，但问题是这个过程十分耗时，并且，如果我们不知道编码类型就很容易出现乱码问题，所以，IO干脆就直接提供给我们一个操作字符的流，方便我们对字符进行操作，如果音频、视频、图片等媒体文件用字节流比较好，如果涉及到字符的话使用字符流比较好。
```

#### 3. 转换流

##### 获取用键盘输入常用的两种方法

1）通过`Scanner`

```Java
Scanner sc=new Scanner(System.in);
String s=sc.nextLine();
sc.close();
int a=Integer.parseInt(s);
```

2)通过`BufferedReader`

```Java
BufferedRead br=new BufferedRead(new InputStreamReader(System.in));
String s=br.readLine();
```

#### 4. IO流的常用方法-缓冲流

O 流的操作一般分为4步：**File类的实例化、流的实例化、读取写入操作、资源的关闭**，为了保证流资源一定可以执行关闭操作，需要使用try-catch-finally进行处理。

InputStream和Reader是所有输入流的抽象基类，本身并不能创建实例来执行输入，但它们的方法是所有输入流都可使用的方法。

- **int read()**：从输入流中读取单个字节/字符，返回所读取的字节/字符数据（字节数据可直接转换为int类型）。
- **int read(byte[]/char[] b)**：从输入流中最多读取`b.length`个字节/字符的数据，并将其存储在字节/字符数组b中，返回实际读取的字节/字符数。
- **int read(byte[]/char[] b, int off, int `len`)**：从输入流中最多读取len个字节/字符的数据，并将其存储在数组b中，放入数组b中时，并不是从数组起点开始，而是从off位置开始，返回实际读取的字节/字符数。

OutputStream和Writer是所有输出流的抽象基类，也有类似用法。不过Writer还可以用字符串来代替字符数组，即以String对象作为参数。

- **void write(byte[]/char[] buff)**：将字节/字符数组中的数据输出到指定输出流中。
- **void write(byte[]/char[] buff, int off, int len )**：将字节/字符数组中从off位置开始，长度为len的字节/字符输出到指定输出流中。
- **void write(String str)**：将str字符串中包含的字符输出到指定输出流中。



缓冲流

以介质是硬盘为例，字节流和字符流的弊端： 在每一次读写的时候，都会访问硬盘。 如果读写的频率比较高的时候，其性能表现不佳。 为了解决以上弊端，采用缓冲流。 缓冲流在读取的时候，**会一次性读较多的数据到缓冲区中**，以后每一次的读取，都是在缓冲中访问，直到缓存中的数据读取完毕，再到硬盘中读取。

缓冲流在写入数据的时候，会先把数据写入到缓冲区，直到缓冲区达到一定的量，才把这些数据**一起写入到硬盘中去**。按照这种操作模式，就不会像字节流，字符流那样**每写一个字节都访问硬盘**，从而减少了IO操作。下面使用缓冲流实现复制视频等大文件的功能：

```Java
package com.wangxnn.IO;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class IOtest01 {
    public static void main(String[] args) {
        try(BufferedInputStream bis=new BufferedInputStream(new FileInputStream("D:\\javalearn\\modelDesign\\src\\com\\wangxnn\\simple.txt"));
            BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream("D:\\javalearn\\modelDesign\\src\\com\\wangxnn\\another.txt"));
        ) {
            byte[] bytes = new byte[1024];
            int len;//len表示实际读取的byte的长度
            while((len=bis.read(bytes))!=-1){
                bos.write(bytes,0,len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

#### 5. 序列化和反序列化

如果我们需要持久化 Java 对象比如将 Java 对象保存在文件中，或者在网络传输 Java 对象，这些场景都需要用到序列化。

简单来说：

- **序列化**： 将数据结构或对象转换成二进制字节流的过程
- **反序列化**：将在序列化过程中所生成的二进制字节流转换成数据结构或者对象的过程

对于 Java 这种面向对象编程语言来说，我们序列化的都是对象（Object）也就是实例化后的类(Class)

![](E:\git repository\LearningNotes\images\序列化.png)

**Java 序列化中如果有些字段不想进行序列化，怎么办？**

对于不想进行序列化的变量，使用 `transient` 关键字修饰。以及被`static`修饰的变量不会被序列化

`transient` 关键字的作用是：阻止实例中那些用此关键字修饰的的变量序列化；当对象被反序列化时，被 `transient` 修饰的变量值不会被持久化和恢复。

关于 `transient` 还有几点注意：

- `transient` 只能修饰变量，不能修饰类和方法。
- `transient` 修饰的变量，在反序列化后变量值将会被置成类型的默认值。例如，如果是修饰 `int` 类型，那么反序列后结果就是 `0`。
- `static` 变量因为不属于任何对象(Object)，所以无论有没有 `transient` 关键字修饰，均不会被序列化。



一个类若想被序列化，则需要实现 `java.io.Serializable` 接口，该接口中没有定义任何方法，是一个标识性接口。在序列化时，**static 和 transient 修饰的变量是无法序列化的**。如果A包含了B的引用，那么在序列化A时也会将B一并序列化；如果此时A可以序列化，B无法序列化，那么在序列化A时就会发生异常。这时就需要将B的引用设为`transient`，该关键字只能修饰变量，不能修饰类和方法。

```Java
package com.wangxnn.IO;

import java.io.Serializable;

public class Person implements Serializable {
    // 序列化ID，Java序列化的机制是通过判断类的serialVersionUID来验证版本一致的。
    // 序列化操作时会把系统当前类的serialVersionUID写入到序列化文件中；
    // 当进行反序列化时，JVM会把传来的字节流中的serialVersionUID于本地相应实体类的serialVersionUID进行比较。如果相同说明是一致的，可以进行反序列化，否则会出现反序列化版本一致的异常。
    private static final long serialVersionUID=112345678991L;
    //String和基本数据类型都实现了Serializable
    private String name;
    private int age;
    private transient String hobby; //transient修饰

    public Person(String name, int age, String hobby) {
        this.name = name;
        this.age = age;
        this.hobby = hobby;
    }
    
    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", hobby='" + hobby + '\'' +
                '}';
    }
}

```

```Java
package com.wangxnn.IO;

import java.io.*;

public class IOtest02 {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //使用ObjectOutputStream序列化
        ObjectOutputStream oop = new ObjectOutputStream(new FileOutputStream("object.txt"));
        oop.writeObject("test serialization");
        oop.flush();
        oop.writeObject(new Person("wangxnn",22,"biking"));
        oop.flush();
        oop.close();
        //使用ObjectInputStream反序列化，反序列化顺序必须和序列化顺序一致
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("object.txt"));
        String str = (String) ois.readObject();
        Person person = (Person) ois.readObject();
        System.out.println(str);
        System.out.println(person);
    }
}

```

#### 6. BIO、NIO、AIO

https://javaguide.cn/java/basis/io/#

**BIO 属于同步阻塞 IO 模型** 。

同步阻塞 IO 模型中，应用程序发起 read 调用后，会一直阻塞，直到内核把数据拷贝到用户空间。数据的读取写入必须阻塞在一个线程内等待其完成。在客户端连接数量不高的情况下，是没问题的。但是，当面对十万甚至百万级连接的时候，传统的 BIO 模型是无能为力的。因此，我们需要一种更高效的 I/O 处理模型来应对更高的并发量。

![](E:\git repository\LearningNotes\images\BIO.jpg)

NIO

Java 中的 NIO 于 Java 1.4 中引入，对应 `java.nio` 包，提供了 `Channel` , `Selector`，`Buffer` 等抽象。NIO 中的 N 可以理解为 Non-blocking，不单纯是 New。它支持面向缓冲的，基于通道的 I/O 操作方法。 对于高负载、高并发的（网络）应用，应使用 NIO 。

Java 中的 NIO 可以看作是 **I/O 多路复用模型**。也有很多人认为，Java 中的 NIO 属于同步非阻塞 IO 模型。

AIO （Asynchronous I/O)

AIO 也就是 NIO 2。Java 7 中引入了 NIO 的改进版 NIO 2,它是异步 IO 模型。

异步 IO 是基于事件和回调机制实现的，也就是应用操作之后会直接返回，不会堵塞在那里，当后台处理完成，操作系统会通知相应的线程进行后续的操作。

 

#### 7. RandomAccessFile