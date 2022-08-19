# MapStruct

1. MapStruct是什么

   mapstruct是一种实体类映射框架，通过Java注解将一个实体类的属性安全的赋值给另一个实体类

2. 为什么需要MapStruct

   在进行实体之间的转换的时候通常有几种做法

   - 手动赋值
   - 通过反序列化，先转成json字符串，再转回来
   - 使用Spring的BeanUtils提供的克隆方法

   但是现在有更好的方法，用MapStruct映射

3. MapStruct对于BeanUtils的优势

   - 复杂属性赋值
   - 效率高，在编译时直接给你生成代码，相当与帮你手动去一个个赋值
   - 支持不同字段间的赋值，通过注解实现

4. MapStruct的使用

   - 导入依赖

     ```xml
     <dependency>
         <groupId>org.mapstruct</groupId>
         <artifactId>mapstruct</artifactId>
         <version>1.5.1.Final</version>
     </dependency>
     <dependency>
         <groupId>org.mapstruct</groupId>
         <artifactId>mapstruct-processor</artifactId>
         <version>1.5.1.Final</version>
     </dependency>
     ```

   - 定义几个实体类用来测试

     ```java
     //第一个拷贝对象
     public class CompareA {
         
         private String name;
     
         private Integer age;
     
         private LocalDateTime createTime;
     
         private double score;
     
         private Double totalScore;
     
         private Date updateTIme;
     
         private ChildCompare childCompare;
     
         private Long days;
     
         //get/set/toString
     }
     ```

     ```java
     //第二个实体类对象
     public class CompareB {
     
         private String name;
     
         private Integer age;
     
         private String createTime;
     
         private double score;
     
         private Double totalScore;
     
         private Date updateTIme;
     
         private ChildCompare childCompare;
         
         //get/set/toString
     ```

     ~~~java
      //这是CompareA和CompareB共有的对象,深度拷贝对象
      public class ChildCompare {
         private String childName;
  	private long childAge;
      	//get/set/toString
      }
     ~~~
     
     ```java
      //MapStruct接口
      @Mapper
 public interface CompareMapper {
      
          CompareMapper INSTANCE = Mappers.getMapper(CompareMapper.class);
      
          @Mappings({
              @Mapping(target = "day",source = "days"),
              @Mapping(target = "createTime",source = "createTime",dateFormat = "yyyy-MM-dd HH:mm:ss"),
              @Mapping(target = "childCompare",source = "childCompare",mappingControl = DeepClone.class)
          })
          CompareB mapper(CompareA compareA);
      }
     ```
     
     ```java
      //测试类
       @Test
  void testMapStruct() {
           ChildCompare childCompare = new ChildCompare();
           childCompare.setChildAge(3);
           childCompare.setChildName("child");
       
           CompareA compareA = new CompareA();
           compareA.setChildCompare(childCompare);
           compareA.setName("zhang");
           compareA.setAge(23);
           compareA.setDays(13L);
           compareA.setCreateTime(LocalDateTime.now());
           compareA.setScore(100);
           compareA.setUpdateTIme(new Date());
           CompareB mapper = CompareMapper.INSTANCE.mapper(compareA);
           System.out.println(mapper);
       }
     ```
     
     - 测试后会生成一个实现类
     
       这个实现类会自动进行手动赋值，深度拷贝也会生成一个方法实现拷贝。

     ```java
//
       // Source code recreated from a .class file by IntelliJ IDEA
  // (powered by Fernflower decompiler)
       //
       
       package com.van.springbootmybatis.mapper;
       
       import com.van.springbootmybatis.domain.ChildCompare;
       import com.van.springbootmybatis.domain.CompareA;
       import com.van.springbootmybatis.domain.CompareB;
       import java.time.format.DateTimeFormatter;
       
       public class CompareMapperImpl implements CompareMapper {
           private final DateTimeFormatter dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
       
           public CompareMapperImpl() {
           }
       
           public CompareB mapper(CompareA compareA) {
               if (compareA == null) {
                   return null;
               } else {
                   CompareB compareB = new CompareB();
                   compareB.setDay(compareA.getDays());
                   if (compareA.getCreateTime() != null) {
                       compareB.setCreateTime(this.dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168.format(compareA.getCreateTime()));
                   }
       
                   compareB.setChildCompare(this.childCompareToChildCompare(compareA.getChildCompare()));
                   compareB.setName(compareA.getName());
                   compareB.setAge(compareA.getAge());
                   compareB.setScore(compareA.getScore());
                   compareB.setTotalScore(compareA.getTotalScore());
                   compareB.setUpdateTIme(compareA.getUpdateTIme());
                   return compareB;
               }
           }
       
           protected ChildCompare childCompareToChildCompare(ChildCompare childCompare) {
               if (childCompare == null) {
                   return null;
               } else {
                   ChildCompare childCompare1 = new ChildCompare();
                   childCompare1.setChildName(childCompare.getChildName());
                   childCompare1.setChildAge(childCompare.getChildAge());
                   return childCompare1;
               }
           }
       }
     ```
     
     - 通过mapstruct提供的注解
     
     - @Mapping的target和source属性，传入目标属性和原属性以保证不同属性名也能映射

     - dataFormat：可以设置传递数据的格式

     - mappingControl：设置深度拷贝

     

5. 如果有的属性在传递过来的对象中有，有的属性没有会出现什么情况

   ```java
   public class CompareA {
   
       private String name;
   
       private LocalDateTime createTime;
   
       private ChildCompare childCompare;
       //get/set/toString
   }
   ```

   ```java
   public class CompareB {
   
       private Integer age;
   
       private String createTime;
   
       private ChildCompare childCompare;
       //get/set/toString
   }
   ```

   ```java
   //
   // Source code recreated from a .class file by IntelliJ IDEA
   // (powered by Fernflower decompiler)
   //
   
   package com.van.springbootmybatis.mapper;
   
   import com.van.springbootmybatis.domain.ChildCompare;
   import com.van.springbootmybatis.domain.CompareA;
   import com.van.springbootmybatis.domain.CompareB;
   import java.time.format.DateTimeFormatter;
   
   public class CompareMapperImpl implements CompareMapper {
       private final DateTimeFormatter dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   
       public CompareMapperImpl() {
       }
   
       public CompareB mapper(CompareA compareA) {
           if (compareA == null) {
               return null;
           } else {
               CompareB compareB = new CompareB();
               if (compareA.getCreateTime() != null) {
                   compareB.setCreateTime(this.dateTimeFormatter_yyyy_MM_dd_HH_mm_ss_11333195168.format(compareA.getCreateTime()));
               }
   
               compareB.setChildCompare(this.childCompareToChildCompare(compareA.getChildCompare()));
               return compareB;
           }
       }
   
       protected ChildCompare childCompareToChildCompare(ChildCompare childCompare) {
           if (childCompare == null) {
               return null;
           } else {
               ChildCompare childCompare1 = new ChildCompare();
               childCompare1.setChildName(childCompare.getChildName());
               childCompare1.setChildAge(childCompare.getChildAge());
               return childCompare1;
           }
       }
   }
   
   ```

   两者对于不同名的属性且没有对应注解映射的属性，并不会进行映射。都默认为null

6. 映射集合

   可以默认的对集合映射，且不是映射地址，是映射值

   默认映射的两个类中的属性名要相同

   ```java
   @Mappings({})
   CompareB mapper(CompareA compareA);
   ```

   ```java
   @Test
   void testSelectAll() {
       CompareA compareA = new CompareA();
       compareA.setName("zhangsan");
       LinkedList<Integer> numbers = new LinkedList<>();
       numbers.add(1);
       numbers.add(2);
       numbers.add(3);
       compareA.setList(numbers);
   
       CompareB mapper = CompareMapper.INSTANCE.mapper(compareA);
       System.out.println(compareA);
       System.out.println(mapper);
       System.out.println("--------------");
       compareA.getList().add(4);
       System.out.println(compareA);
       System.out.println(mapper);
   }
   ```

   ```java
   //自动生成的实现类
   public CompareB mapper(CompareA compareA) {
       if (compareA == null) {
           return null;
       } else {
           CompareB compareB = new CompareB();
           compareB.setName(compareA.getName());
           List<Integer> list = compareA.getList();
           if (list != null) {
               compareB.setList(new ArrayList(list));
           }
           return compareB;
       }
   }
   ```

   - 如果属性名相同，返回值类型不同，它也会自动进行类型转换

     //这里设置compareA的name为int，CompareB的name为String

     ```java
     public CompareB mapper(CompareA compareA) {
         if (compareA == null) {
             return null;
         } else {
             CompareB compareB = new CompareB();
             compareB.setName(String.valueOf(compareA.getName()));
             List<Integer> list = compareA.getList();
             if (list != null) {
                 compareB.setList(new ArrayList(list));
             }
     
             return compareB;
         }
     }
     ```

   - 类型转换的规则：

     MapStruct提供了一些定义好的类型转换

     - 基本类型及其对应的包装类型之间的转换；比如int和Integer、float和Float、long和Long、boolean和Boolean等；

     - 任意基本类型和任意包装类型之间的转换；比如int和long、byte和Integer等；

     - 任意基本类型、包转类型和String之间的转换；比如boolean和String、Integer和String；

     - 枚举和String；

     - 大数类型（BigInteger、BigDecimal）、基本类型、基本类型包装类型、String之间的相互转换；

     - 其它一些场景，参考MapStruct官网

       - 测试了一下，子类向父类转换是可以的

         ```java
         public Parent mapper2(Person person) {
             if (person == null) {
                 return null;
             } else {
                 Parent parent = new Parent();
                 parent.setAge(person.getAge());
                 return parent;
             }
         }
         ```

7. 依赖注入

   前面的例子中都需要Mappers.getMapper来创建实例，如果是Spring工程，我们可以把这个实例交给Spring来管理

   实现方法

   - 在映射接口前的@Mapper注解中添加参数@Mapper(componentModel = "spring")

   使用方法

   - 既然交给了Spring来管理，那我们获取的时候自然是通过@Autowired来获取

8. 设置默认值

   默认值的设置有两种情况

   1. 无论原属性是否为空，都使用这个默认值
      - 在@Mapping注解中，使用constant属性
   2. 如果原属性为空，我们才使用这个默认值
      - defaultValue 属性

9. 设置前置/后置方法

   ```java
   @Mapper(componentModel = "spring")
   public interface CompareMapper {
   
       @BeforeMapping
       default void beforeMethod() {
           System.out.println("this is beforeMapping");
       }
       @AfterMapping
       default void afterMethod() {
           System.out.println("this is afterMapping");
       }
       @Mappings({})
       Parent mapper2(Person person);
   }
   ```

   ```java
   //可以在生成的实现类中看见，在执行前会先调用前置方法，如果不为null还会调用后置方法
   public Parent mapper2(Person person) {
       this.beforeMethod();
       if (person == null) {
           return null;
       } else {
           Parent parent = new Parent();
           parent.setAge(person.getAge());
           this.afterMethod();
           return parent;
       }
   }
   ```

   