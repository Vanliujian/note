# Lambda表达式

1. 使用Lambda必须有接口，并且接口中有且仅有一个抽象方法。

2. 组成

   - 参数
   - 箭头
   - 代码

   示例：

   ```bash
   （参数列表）->{一些重要方法的代码};
   ():接口中抽象方法的参数列表，没有参数，就空着；有参数就写出参数，多个参数用逗号分隔。
   ->：传递：把参数传递给方法体{}
   {}:重写接口的抽象方法的方法体
   ```

   省略操作

   ```bash
   （参数列表）：括号中参数列表的数据类型，可以省略不写
   （参数列表）：括号中的参数只有一个，那么类型和（）都可以省略
    {一些代码} ：如果{}中的代码只有一行，无论是否有返回值，都可以省略（{}，return，分号）并且只能一起省略
   ```

3. 举例

   ```java
   public class FunctionalTest {
       public static void main(String[] args) {
           test((i)-> i);
       }
   
       public static void test(Animal animal) {
           System.out.println(animal.run(1));
       }
   }
   
   @FunctionalInterface
   interface Animal {
       int run(int k);
   }
   ```

   

# 函数式接口

1. 定义：

   只包含一个抽象方法的接口

2. 使用

   - 接口上可以定义@FunctionalInterface注解来检查这个接口是否是函数式接口
   - 体现在lambda中

# 方法引用/构造器引用

1. 使用

   使用操作符 “**::**” 将类(或对象) 与 方法名分隔开来

   - 对象::实例方法名

   - 类::静态方法名

   - 类::实例方法名

2. 注意点

   `实现接口的抽象方法的参数列表和返回值类型，必须与方法引用的方法的参数列表和返回值类型保持一致(只针对上面的前两种情况)`

   ```java
   //对象::实例方法名
   public static void main(String[] args) {
       Person zhang = new Person("zhang");
       String name = zhang.getName();
       System.out.println(name);
       System.out.println("-------------------");
       Supplier<String> zhangGet = zhang::getName;
       String s = zhangGet.get();
       System.out.println(s);
   }
   ```

   ```java
   //类::静态方法名
   import java.util.function.Supplier;
   
   public class FunctionalTest {
       public static void main(String[] args) {
           Supplier<String> getType = Person::getType;
           String s = getType.get();
           System.out.println(s);
       }
   }
   class Person {
       static String type = "P";
   
       public static String getType() {
           return type;
       }
   }
   ```

   ```java
   //类::实例方法名
   import java.util.function.Function;
   
   public class FunctionalTest {
       public static void main(String[] args) {
           Person zhang = new Person("zhang");
           //返回类型是Function，传递的泛型，第一个是对象的类型，第二个是返回值的类型
           Function<Person,String> getType = Person::getName;
           //调用apply方法执行这个对象对应的方法
           String apply = getType.apply(zhang);
           System.out.println(apply);
       }
   }
   class Person {
       String name;
   
       public Person(String name) {
           this.name = name;
       }
   
       public String getName() {
           return name;
       }
   }
   ```

   Function和BiFunction的区别：Function传递一个参数，BiFunction传递两个参数

3. 构造器引用

   ```java
   public static void test3() {
       Function<String,Person> func1 = (name) -> new Person(name);
       System.out.println(func1.apply("Tom"));
       System.out.println("*******************");
   
       Function<String,Person> func2 = Person :: new;
       System.out.println(func2.apply("Tom"));
   }
   ```

# Stream API

1. 为什么需要Stream

   Stream是对集合对象功能的增强，可以进行复杂的查找，过滤以及映射数据。借助于lambda表达式提高效率。

2. 注意：必须有终结操作，有终结操作前面的中间操作才会有效果，否则不会执行

   中间操作的返回值都是stream，所以可以一直用点调用，终结操作返回的是Optional或者其它，所以不能继续调用。

   流是一次性的

   不会影响原数据的（一般情况下）

3. 代码示例

   原生方式

   ```java
   public class Test {
   
       public static void main(String[] args) {
           List<Cat> cats = getCats();
           cats
                   .stream()
                   .filter(new Predicate<Cat>() {
               @Override
               public boolean test(Cat cat) {
                   return cat.getAge() < 4;
               }
           })
                   .distinct()
                   .forEach(new Consumer<Cat>() {
                       @Override
                       public void accept(Cat cat) {
                           System.out.println(cat.getName());
                       }
                   });
   
       }
       public static List<Cat> getCats() {
           List<Cat> cats = new ArrayList<>();
           for (int i = 0; i < 5; i++) {
               cats.add(new Cat("cat"+i,i));
           }
           cats.add(new Cat("cat5",3));
           return cats;
       }
   }
   ```
   
使用lambda表达式
   
```java
   public class Test {
   
       public static void main(String[] args) {
           List<Cat> cats = getCats();
           cats
                   .stream()
                   .filter(cat -> cat.getAge() < 4)
               	//这个distinct方法只能过滤同一个地址的对象（如果没有重写equls方法和hashcode方法）
                   .distinct()
                   .forEach(cat -> System.out.println(cat.getName()));
       }
       public static List<Cat> getCats() {
           List<Cat> cats = new ArrayList<>();
           for (int i = 0; i < 5; i++) {
               cats.add(new Cat("cat"+i,i));
           }
           cats.add(new Cat("cat2",3));
           return cats;
       }
   }
   ```
   
4. 创建流

   - 如果是集合，通过集合.stream()

   - 如果是数组arr，通过Arrays.stream(arr)或者Stream.of(arr)

   - 如果是键值对形式，可以通过entrySet方法获取流对象

     ```java
     public static void main(String[] args) {
         HashMap<String, Integer> map = new HashMap<>();
         map.put("s1",1);
         map.put("s2",2);
         map.put("s3",3);
         map.put("s4",4);
         map.put("s5",5);
     
         Set<Map.Entry<String, Integer>> entries = map.entrySet();
         Stream<Map.Entry<String, Integer>> stream = entries.stream();
         stream.filter((entry -> entry.getValue() > 3))
             .forEach(entry -> System.out.println(entry.getValue()));
     }
     ```

5. 中间操作

   - filter：过滤

   - map：对流对象进行操作或者转换

     ```java
     //原生方式
     public static void main(String[] args) {
         ArrayList<Integer> list = new ArrayList<>();
         for (int i = 0; i < 5; i++) {
             list.add(i);
         }
     
         list.stream()
             .map(new Function<Integer, String>() {
                 @Override
                 public String apply(Integer integer) {
                     return String.valueOf(integer);
                 }
             })
             .forEach(new Consumer<String>() {
                 @Override
                 public void accept(String s) {
                     System.out.println(s);
                 }
             });
     }
     
     //lambda表达式方式
     public static void main(String[] args) {
         ArrayList<Integer> list = new ArrayList<>();
         for (int i = 0; i < 5; i++) {
             list.add(i);
         }
     
         list.stream()
             .map(integer -> String.valueOf(integer))
             .forEach(str-> System.out.println(str));
     }
     ```

   - distinct：去重，如果没有重写equals和hashcode就会调用Object类的方法。

   - sorted：进行排序

   - limit：限制行数

   - skip：跳过前n个元素

   - flatMap：当对象里面存list时，将所有list中元素抽出来放到一起，最终 output 的新 Stream 里面已经没有 List 了，都是基础的数据。

     ```java
     public static void main(String[] args) {
         List<String> list1 = new ArrayList<>();
         list1.add("a");
         list1.add("b");
         list1.add("c");
         list1.add("d");
         list1.add("e");
     
         List<String> list2 = new ArrayList<>();
         list2.add("a2");
         list2.add("b2");
         list2.add("c2");
     
         List<String> list3 = new ArrayList<>();
         list3.add("a3");
         list3.add("b3");
     
         List<List<String>> lists = new ArrayList<>();
         lists.add(list1);
         lists.add(list2);
         lists.add(list3);
     
         lists.stream()
             .flatMap(strings -> strings.stream())
             .forEach(str-> System.out.print(str+" "));
     }
     //输出
     a b c d e a2 b2 c2 a3 b3
     ```

6. 终结操作

   - forEach：遍历流中的所有数据

   - count：获取当前流中元素的个数

   - max&min：求流中的最值

   - collect：获取集合

     ```java
     import java.util.ArrayList;
     import java.util.List;
     import java.util.Map;
     import java.util.function.Function;
     import java.util.stream.Collectors;
     
     public class Test {
     
         public static void main(String[] args) {
             List<Cat> allCat = getAllCat();
             //转换为一个map集合，以猫的名称为key，猫所吃的食物为value
             Map<String, List<Food>> collect = allCat.stream()
                     .distinct()
                     .collect(Collectors.toMap(new Function<Cat, String>() {
                         @Override
                         public String apply(Cat cat) {
                             return cat.getName();
                         }
                     }, new Function<Cat, List<Food>>() {
                         @Override
                         public List<Food> apply(Cat cat) {
                             return cat.getFood();
                         }
                     }));
             System.out.println(collect);
         }
         //主方法改用lambda表达式
         public static void main(String[] args) {
             List<Cat> allCat = getAllCat();
             //转换为一个map集合，以猫的名称为key，猫所吃的食物为value
             Map<String, List<Food>> collect = allCat.stream()
                 .distinct()
                 .collect(Collectors.toMap(cat -> cat.getName(),cat -> cat.getFood()));
             System.out.println(collect);
         }
         
         public static List<Cat> getAllCat() {
             Food fish = new Food("鱼");
             Food rice = new Food("米饭");
             Food water = new Food("水");
             ArrayList<Food> foodList1 = new ArrayList<>();
             ArrayList<Food> foodList2 = new ArrayList<>();
             ArrayList<Food> foodList3 = new ArrayList<>();
             foodList1.add(fish);
             foodList1.add(rice);
     
             foodList2.add(water);
             foodList2.add(fish);
     
             foodList3.add(fish);
             foodList3.add(rice);
             foodList3.add(water);
     
             Cat cat1 = new Cat("tom1",foodList1);
             Cat cat2 = new Cat("tom2",foodList2);
             Cat cat3 = new Cat("tom3", foodList3);
     
             List<Cat> cats = new ArrayList<>();
             cats.add(cat1);
             cats.add(cat2);
             cats.add(cat3);
             return cats;
         }
     }
     ```

   - anyMatch：是否有符合的

   - allMatch：是否所有的都符合条件

   - noneMatch：是否都不符合条件

   - findAny：查找流中任意一个元素

   - findFirst：获取流中的第一个元素

   - reduce：归并

     ```java
     public static void main(String[] args) {
         List<Cat> allCat = getAllCat();
         //输出所有食物的种类
         Food t = new Food("All Kinds Of Food:");
         Food reduce = allCat.stream()
             .map(cat -> cat.getFood())
             .flatMap(foods -> foods.stream())
             .distinct()
             .reduce(t, new BinaryOperator<Food>() {
                 @Override
                 public Food apply(Food food, Food food2) {
                     food.setName(food.getName()+food2.getName());
                     return food;
                 }
             });
         System.out.println(reduce.getName());
     }
     //输出
     All Kinds Of Food:鱼米饭水
     ```

     ```java
     //也可以这样改，把他的返回值改成String类型的
     public static void main(String[] args) {
         List<Cat> allCat = getAllCat();
         //输出所有食物的种类
         String t = "All Kinds Of Food:";
         String reduce = allCat.stream()
             .map(cat -> cat.getFood())
             .flatMap(foods -> foods.stream())
             .distinct()
             .map(food -> food.getName())
             .reduce(t, (s1, s2) -> s1 + s2);
         System.out.println(reduce);
     }
     ```

     reduce归并一个参数的时候

     ```java
     //一个参数与两个参数的不同之处在于，一个参数的把初始值封装在内部，类型和处理的数据类型相同
     //返回值变为Optional
     public static void main(String[] args) {
         List<Cat> allCat = getAllCat();
         //输出所有食物的种类
         Optional<String> reduce = allCat.stream()
             .map(cat -> cat.getFood())
             .flatMap(foods -> foods.stream())
             .distinct()
             .map(food -> food.getName())
             .reduce((s1, s2) -> s1 + s2);
         System.out.println(reduce.orElse("为空"));
     }
     ```

   - Stream流对基本数据类型的优化

     由于频繁的使用自动装箱自动拆箱会导致性能的下降，所以Stream流提供了mapToInt和mapToLong等一系列方法不用再自动拆箱

   - 并行流

     如果要实现并行流，需要在流后面添加parallel方法

     或者直接通过parallelStream方法实现并行流

     ```java
     //peek方法是用来测试的
     public static void main(String[] args) {
         List<Cat> allCat = getAllCat();
         //输出所有食物的种类
         //也可以用allcat.parallelStream()直接获取并行流
         Optional<String> reduce = allCat.stream().parallel()
             .peek(cat->System.out.println(cat.getName()+Thread.currentThread().getName()))
             .map(cat -> cat.getFood())
             .flatMap(foods -> foods.stream())
             .distinct()
             .map(food -> food.getName())
             .reduce((s1, s2) -> s1 + s2);
         System.out.println(reduce.orElse("为空"));
     }
     //输出
     tom1ForkJoinPool.commonPool-worker-3
     tom3ForkJoinPool.commonPool-worker-5
     tom2main
     鱼米饭水
     ```

     

# 接口的增强

1.8的接口可以定义全局常量，抽象方法，默认方法以及静态方法

# Optional类

1. 为什么需要Optional类

   可以避免空指针异常，Optional类把具体的数据存储到Optional的属性中

2. 创建Optional对象

   通过ofNullable方法创建

3. Optional的使用

   empty：初始化一个空的对象

   of：传递一个参数来初始化对象

   ifPresent，如果存在值，则使用该值执行给定的操作，否则不执行任何操作。

   获取值：get方法（如果是null，那么会抛出异常）

   orElseGet方法与get方法的区别：如果是null，会返回一个默认值（自己设置的）

   orElseThrow方法：如果内部没有值就让它抛出一个异常

   filter方法：过滤操作
   
   isPresent方法：是否存在值（推荐使用ifPresent操作）
   
   map方法：进行映射

