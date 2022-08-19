# Java基础

## 泛型

1. 从泛型类派生子类

   - 当子类是泛型类，子类泛型标识要和父类一样，或者扩充

   - 当子类不是泛型类，父类是泛型类的时候，父类的泛型标识需要指定具体的类型

     ```java
     public class Son extends Parent<String> {   
     }
     ```

2. 泛型接口

   - 实现泛型接口的类是泛型类，标识要和接口的标识一致，或者扩充
   - 实现泛型接口的类不是泛型类，实现时接口需要明确具体的类型

3. 泛型方法

   - 定义：

     ```java
     [public] [static] <T> 返回值类型 方法名(T 参数列表)
     ```

   - 泛型方法和所在类是否是泛型类没有关系

4. 类型通配符：？

   - 在泛型中没有多态的理念，比如Integer不能赋给Number，这时候就需要通配符了
   - ? extends Number：表示可以传递Number或者其子类型
   - ? super Cat：表示只能是Cat或者是Cat的父类

5. 类型擦除

   因为泛型其实只是在编译器中实现的，而虚拟机并不认识泛型类项，所以要在虚拟机中将泛型类型进行擦除。也就是说，在编译阶段使用泛型，运行阶段取消泛型，即擦除。 擦除是将泛型类型以其父类代替，如String 变成了Object等。其实在使用的时候还是进行带强制类型的转化，只不过这是比较安全的转换，因为在编译阶段已经确保了数据的一致性。

6. 泛型与数组

   - 可以直接声明带泛型的数组引用，但不能直接创建带泛型的数组对象

     ```java
     ArrayList<String>[] list = new ArrayList[3];
     ```

   - 可以通过Array.newInstance()创建泛型数组

7. 泛型和反射

   - 常用的两个类

     - Class<T>
     - Constructor<T>

   - 举例

     ```java
     Class<Person> personClass = Person.class;
     Constructor<Person> constructor = personClass.getConstructor();
     Person s = constructor.newInstance();
     ```

     

8. 注意点

   - 只有返回值为泛型标识符的方法不是泛型方法，他们不能被定义为static的
   - 泛型方法可以被定义为static的

## 序列化

- Java序列化是指把Java对象转换为字节序列的过程，而Java反序列化是指把字节序列恢复为Java对象的过程

- serialVersionUID用于对象的版本控制
- 声明为static和transient的成员变量不能被序列化

1. 使用Serializable接口实现序列化

   这个接口并没有实现任何操作，只是一种标记

   ```java
   import java.io.*;
   
   class Student implements Serializable {
       private String name;
       private int age;
       public static int QQ = 1234;
       private transient String address = "CHINA";
       Student(String name, int age ){
           this.name = name;
           this.age = age;
       }
       public String toString() {
           return "name: " + name + "\n" +"age: " + age + "\n" +"QQ: " + QQ + "\n" + "address: " + address;
       }
       public void SetAge(int age) {
           this.age = age;
       }
   
       public int getAge() {
           return age;
       }
   }
   public class SerializableDemo {
       public static void main(String[] args) throws IOException, ClassNotFoundException {
           Student stu = new Student("Ming", 16);
           System.out.println(stu);
   
           //创建序列化输出流
           ByteArrayOutputStream buff = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(buff);
           //将序列化对象存入缓冲区
           out.writeObject(stu);
           //修改相关值
           Student.QQ = 6666;
           stu.SetAge(18);
   
           //从缓冲区取回被序列化的对象
           ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buff.toByteArray()));
           Student newStu = (Student) in.readObject();
           //输出后发现QQ值改变了，age没变，address为null。
           //说明QQ没有被序列化，address也没有被序列化
           System.out.println(newStu);
       }
   }
   ```

2. 实现Externalizable接口

   这个接口继承Serializable接口，必须重写两个方法，writeExternal和readExternal

   必须有一个无参的构造方法，因为在反序列化时会默认调用无参构造方法实例化对象

   ```java
   package JavaBaseStudy;
   
   import java.io.*;
   
   class Student implements Externalizable {
       private String name;
       private int age;
       public static int QQ = 1234;
       private transient String address = "CHINA";
   
       public Student() {
       }
   
       public Student(String name, int age ){
           this.name = name;
           this.age = age;
       }
       public String toString() {
           return "name: " + name + "\n" +"age: " + age + "\n" +"QQ: " + QQ + "\n" + "address: " + address;
       }
   
       @Override
       public void writeExternal(ObjectOutput out) throws IOException {
           out.writeObject(name);
           out.writeInt(age);
       }
   
       public void setAge(int age) {
           this.age = age;
       }
   
       @Override
       public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
           name = (String) in.readObject();
           age = in.readInt();
       }
   }
   public class SerializableDemo {
       public static void main(String[] args) throws IOException, ClassNotFoundException {
           Student stu = new Student("Ming", 16);
           System.out.println(stu);
   
           //创建序列化输出流
           ByteArrayOutputStream buffer = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(new ObjectOutputStream(buffer));
           out.writeObject(stu);
           out.close();
           stu.QQ = 1;
           stu.setAge(20);
           ObjectInputStream in = new ObjectInputStream(new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray())));
           stu = (Student) in.readObject();
           System.out.println(stu);
       }
   }
   ```

   ```shell
   //输出
   name: Ming
   age: 16
   QQ: 1234
   address: CHINA
   
   name: Ming
   age: 16
   QQ: 1
   address: CHINA
   ```

3. 实现Serializable接口+writeObject和readObject方法

   1. 方法必须要被 private 修饰才能被调用
   2. 第一行调用默认的 defaultRead/WriteObject，隐式序列化非 static 和 transient
   3. 调用 read/writeObject() 将获得的值赋给相应的值，显式序列化

   ```java
   package JavaBaseStudy;
   import java.io.*;
   
   public class SerDemo implements Serializable {
       public transient int age = 23;
       public String name;
   
       public SerDemo() {
       }
   
       public SerDemo(String name) {
           this.name = name;
       }
   
       private void writeObject(ObjectOutputStream stream) throws IOException {
           stream.defaultWriteObject();
           stream.writeInt(age);
       }
   
       private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {
           stream.defaultReadObject();
           age = stream.readInt();
       }
   
       public String toString() {
           return "年龄" + age + " " + name;
       }
   
       public static void main(String[] args) throws IOException, ClassNotFoundException {
           SerDemo stu = new SerDemo("Ming");
           ByteArrayOutputStream bout = new ByteArrayOutputStream();
           ObjectOutputStream out = new ObjectOutputStream(bout);
           out.writeObject(stu);
           ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
           SerDemo stu1 = (SerDemo) in.readObject();
           System.out.println(stu1);
       }
   }
   ```

## 集合

> Iterable接口

1. Collection接口

   1. List
      - ArrayList
      - LinkedList
      - Vector
      - Stack
   2. Queue
      - PriorityQueue
      - Deque
        - ArrayDeque
   3. Set
      - HashSet
      - LinkedHashSet
      - SortedSet
        - TreeSet

   Vector集合和ArrayList一样，但是Vector是线程安全的

2. Map接口

   1. Hashtable

      Properties

   2. HashMap

      LinkedHashMap

   3. SortedMap

      TreeMap

Properties继承于Hashtable

格式：键=值

SortedMap接口提供了有序的map实现，其实现类TreeMap，默认根据key进行升序排序。也可以重写comparator方法根据value进行排序。

## IO

File类

- 获取/删除/创建文件夹
- 判断文件夹是否存在
- 对文件夹的遍历

File类属性

| 属性                          | 描述                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| static String pathSeparator   | 与系统有关的路径分隔符字符，出于方便考虑，它被表示为一个字符串。 此字段被初始化为包含系统属性 file.separator的值的第一个字符。在 UNIX 系统上，此字段的值为 '/';在 Microsoft Windows 系统上，它为 '\\'。 |
| static char pathSeparatorChar | 与系统有关的默认路径分隔符字符。                             |
| static String separator       | 与系统有关的默认名称分隔符，出于方便考虑，它被表示为一个字符串。在 UNIX 系统上，此字段为 ':';在 Microsoft Windows 系统上，它为 ';'。 |
| static char separatorChar     | 与系统有关的默认名称分隔符。                                 |

常用的方法

**public String getName()**
返回由此抽象路径名表示的文件或目录的名称。

**public String getPath()**
将此抽象路径名转换为一个路径名字符串。

**public boolean isDirectory()**
测试此抽象路径名表示的文件是否是一个目录。

**public boolean isFile()**
测试此抽象路径名表示的文件是否是一个标准文件。

**public boolean createNewFile() throws IOException**
当且仅当不存在具有此抽象路径名指定的名称的文件时，原子地创建由此抽象路径名指定的一个新的空文件。

**public boolean delete()**
删除此抽象路径名表示的文件或目录。

**public boolean mkdir()**
创建此抽象路径名指定的目录。



字节输入输出流 InputStream	OutputStream

字符输入输出流 Reader	Writer

| 类名                  | 描述信息                                                     |
| --------------------- | ------------------------------------------------------------ |
| BufferedInputStream   | BufferedInputStream 为另一个输入流添加一些功能，即缓冲输入以及支持 mark 和 reset 方法的能力。 |
| BufferedOutputStream  | 该类实现缓冲的输出流。                                       |
| BufferedReader        | 从字符输入流中读取文本，缓冲各个字符，从而实现字符、数组和行的高效读取。 |
| BufferedWriter        | 将文本写入字符输出流，缓冲各个字符，从而提供单个字符、数组和字符串的高效写入。 |
| ByteArrayInputStream  | ByteArrayInputStream 包含一个内部缓冲区，该缓冲区包含从流中读取的字节。 |
| ByteArrayOutputStream | 此类实现了一个输出流，其中的数据被写入一个 byte 数组。       |
| CharArrayReader       | 此类实现一个可用作字符输入流的字符缓冲区。                   |
| CharArrayWriter       | 此类实现一个可用作 Writer 的字符缓冲区。                     |
| Console               | 此类包含多个方法，可访问与当前 Java 虚拟机关联的基于字符的控制台设备（如果有）。 |
| DataInputStream       | 数据输入流允许应用程序以与机器无关方式从底层输入流中读取基本 Java 数据类型。 |
| DataOutputStream      | 数据输出流允许应用程序以适当方式将基本 Java 数据类型写入输出流中。 |
| File                  | 文件和目录路径名的抽象表示形式。                             |
| FileDescriptor        | 文件描述符类的实例用作与基础机器有关的某种结构的不透明句柄，该结构表示开放文件、开放套接字或者字节的另一个源或接收者。 |
| FileInputStream       | FileInputStream 从文件系统中的某个文件中获得输入字节。       |
| FileOutputStream      | 文件输出流是用于将数据写入 File 或 FileDescriptor 的输出流。 |
| FilePermission        | 此类表示对文件和目录的访问。                                 |
| FileReader            | 用来读取字符文件的便捷类。                                   |
| FileWriter            | 用来写入字符文件的便捷类。                                   |
| FilterInputStream     | FilterInputStream 包含其他一些输入流，它将这些流用作其基本数据源，它可以直接传输数据或提供一些额外的功能。 |
| FilterOutputStream    | 此类是过滤输出流的所有类的超类。                             |
| FilterReader          | 用于读取已过滤的字符流的抽象类。                             |
| FilterWriter          | 用于写入已过滤的字符流的抽象类。                             |
| InputStream           | 此抽象类是表示字节输入流的所有类的超类。                     |
| InputStreamReader     | InputStreamReader 是字节流通向字符流的桥梁：它使用指定的 charset 读取字节并将其解码为字符。 |



> OutPutStream类

- 所有输出字节流的超类

- 五个方法

      void write(int b)： 向文件中写入一个字节。
      void write(byte[] b)： 向文件中写入一个字节数组。
      void write(byte[] b, int off, int len)： 向文件中写入字节输入的一部分。
      void close()： 释放资源.
      void flush(), 刷新此输出流并强制任何缓冲的输出字节被写出

> FileOutPutStream

- 把内存中的数据写入到硬盘文件中



> ObjectOutPutStream

- 把对象以流的方式写入到文件中保存
- 构造方法需要传入一个字节输出流
- 特有的方法：writeObject，把对象写入
- 通常可以使用这个类进行序列化

## JDBC

- 接口

  Connection：特定数据库的连接（会话）。在连接上下文中执行SQL语句并返回结果。
  PreparedStatement：表示预编译的 SQL 语句的对象。
  Statement：用于执行静态 SQL 语句并返回它所生成结果的对象。
  ResultSet ：表示数据库结果集的数据表，通常通过执行查询数据库的语句生成 。
  CallableStatement ：用于执行 SQL 存储过程的接口 。

- 实现流程

  1. 加载驱动（通过反射的方式）
  2. 获取连接
  3. 创建Statement对象
  4. 执行sql语句
  5. 获取结果集
  6. 关闭连接

## 反射

- 反射在Java.lang.reflect.*;包下
- 在运行状态中，对于任意一个类都知道这个类的所有属性和方法

| 类                            | 含义                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| java.lang.Class               | 代表整个字节码。代表一个类型，代表整个类。                   |
| java.lang.reflect.Method      | 代表字节码中的方法字节码。代表类中的方法。                   |
| java.lang.reflect.Constructor | 代表字节码中的构造方法字节码。代表类中的构造方法。           |
| java.lang.reflect.Field       | 代表字节码中的属性字节码。代表类中的成员变量（静态变量+实例变量）。 |

用Class对象获取Method、Constructor、Field对象

获取Class的三种方式

1. Class.forName("全限定类名")
2. 对象.getClass()
3. 类型.class

newInstance方法内部调用了无参构造，可以实例化一个对象

> Class类的一些方法

- newInstance() 创建新对象
- getName() 返回全限定类名
- getSimpleName() 返回类名
- getFields() 返回public修饰的所有属性
- getDeclaredFields() 返回类中所有属性
- getDeclaredField(String name) 根据属性名获取指定属性
- getDeclaredMethods() 返回类中的所有实例方法
- getDeclaredMethod(String name, Class<?>… parameterTypes) 根据方法名和形参获取指定方法

> Field类方法

| 方法名                                    | 备注                                                         |
| ----------------------------------------- | ------------------------------------------------------------ |
| public String getName()                   | 返回属性名                                                   |
| public int getModifiers()                 | 获取属性的修饰符列表,返回的修饰符是一个数字，每个数字是修饰符的代号【一般配合Modifier类的toString(int x)方法使用】 |
| public Class<?> getType()                 | 以Class类型，返回属性类型【一般配合Class类的getSimpleName()方法使用】 |
| public void set(Object obj, Object value) | 设置属性值                                                   |
| public Object get(Object obj)             | 读取属性值                                                   |

> Method类方法

| 方法名                                           | 备注                                                         |
| ------------------------------------------------ | ------------------------------------------------------------ |
| public String getName()                          | 返回方法名                                                   |
| public int getModifiers()                        | 获取方法的修饰符列表,返回的修饰符是一个数字，每个数字是修饰符的代号【一般配合Modifier类的toString(int x)方法使用】 |
| public Class<?> getReturnType()                  | 以Class类型，返回方法类型【一般配合Class类的getSimpleName()方法使用】 |
| public Class<?>[] getParameterTypes()            | 返回方法的修饰符列表（一个方法的参数可能会有多个。）【结果集一般配合Class类的getSimpleName()方法使用】 |
| `public Object invoke(Object obj, Object… args)` | 调用方法                                                     |

> 反射举例

```java
public static void main(String[] args) throws Exception {
    Person zhangsan = new Person("zhangsan", 23);
    Class<Person> personClass = Person.class;
    Method getName = personClass.getMethod("getName");
    Object invoke = getName.invoke(zhangsan);
    System.out.println(invoke);

    Constructor<?>[] constructors = personClass.getConstructors();
    for (int i = 0; i < constructors.length; i++) {
        System.out.println(constructors[i]);
    }
}
//输出
zhangsan
public JavaBaseStudy.Person()
public JavaBaseStudy.Person(java.lang.String,int)
```

```java
通过getDeclaredFields获取所有属性，包括private修饰的
带有Declared的都是获取所有属性
```

## 注解

为什么需要注解？

- 对Java中的类、方法、成员变量做标记，用于一些特定的处理需求。

一些常用的注解

- @Override 检查是否是重写方法
- @Deprecated 标记过时方法。会报编译警告。
- @SuppressWarnings 让编译器忽略警告
- @Inherited 标记这个注解是继承于哪个注解类
- @Documented 标记这些注解是否包含在用户文档中。
- @Retention 表明该注解的生命周期（这个注解是不是必须要写？）
  - Retention的取值
    - source：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；被编译器忽略
    - class：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期
    - runtime：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在

自定义注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MyAnno {
    String value();
    String des() default "自定义注解";
}
```

```java
@MyAnno(value = "自定义")
class TestAnnotation {
    public static void main(String[] args) {
        TestAnnotation serDemo = new TestAnnotation();
        Class<? extends TestAnnotation> aClass = serDemo.getClass();
        if (aClass.isAnnotationPresent(MyAnno.class)) {
            System.out.println("SerDemo上有注解MyAnno");
            MyAnno annotation = aClass.getAnnotation(MyAnno.class);
            System.out.println(annotation.value());
            System.out.println(annotation.des());
        } else {
            System.out.println("SerDemo上没有注解MyAnno");
        }
    }
}
```

针对@Retention注解，他的生命周期应该在什么情况下取何值

- 一般如果需要在运行时去动态获取注解信息，那只能用 RUNTIME 注解；
- 如果要在编译时进行一些预处理操作，比如生成一些辅助代码，就用 CLASS注解；
- 如果只是做一些检查性的操作，比如@Override和 @SuppressWarnings，则可选用 SOURCE 注解。