# Redis的问题

1. ==hash中的hincrby是存在的，不存在hdecrby，自减的话用的负数吗？==

```bash
127.0.0.1:6379> hset hash k1 6
(integer) 1
127.0.0.1:6379> hincrby hash k1 1
(integer) 7
127.0.0.1:6379> hdecrby hash k1 1
(error) ERR unknown command 'hdecrby'   #显示没有hdecrby
127.0.0.1:6379>
```

- 回答：确实没有hdecrby，自减用的就是负数。

# Bitcount的问题

**BITCOUNT key [start] [end]**

计算给定字符串中，被设置为 `1` 的比特位的数量。

一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 `start` 或 `end` 参数，可以让计数只在特定的位上进行。

`start` 和 `end` 参数的设置和 [*GETRANGE*](http://doc.redisfans.com/string/getrange.html#getrange) 命令类似，都可以使用负数值：比如 `-1` 表示最后一个位，而 `-2` 表示倒数第二个位，以此类推。

不存在的 `key` 被当成是空字符串来处理，因此对一个不存在的 `key` 进行 `BITCOUNT` 操作，结果为 `0` 。

```bash
############################################################
127.0.0.1:6379> setbit sign 1 1
(integer) 0
127.0.0.1:6379> setbit sign 2 0
(integer) 0
127.0.0.1:6379> setbit sign 3 1
(integer) 0
127.0.0.1:6379> setbit sign 4 1
(integer) 0
127.0.0.1:6379> setbit sign 5 0
(integer) 0
127.0.0.1:6379> setbit sign 6 0
(integer) 0
127.0.0.1:6379> setbit sign 7 0
(integer) 0
127.0.0.1:6379> bitcount sign 
(integer) 3
127.0.0.1:6379> bitcount sign -3 -1   # 这儿结果咋变成3了
(integer) 3
127.0.0.1:6379> setbit sign 0 1  # 以为是没有0的问题，加了一个0，发现还是不行
(integer) 0
127.0.0.1:6379> bitcount sign 0 5
(integer) 4
127.0.0.1:6379> bitcount sign 1 5 # 这儿的结果怎么是0
(integer) 0
############################################################
```



`answer：bitcount是对字节操作，而一个字节是8位，看下面的例子`

```bash
setbit key 0 1 # 10000000
setbit key 1 1 # 11000000
setbit key 4 1 # 11001000
setbit key 9 1 # 11001000 01000000    超出范围就自动加一字节
setbit key 18 1# 11001000 01000000 00100000
127.0.0.1:6379> bitcount key 0 1 # 前两个字节的1的总数
(integer) 4
127.0.0.1:6379> bitcount key
(integer) 5
127.0.0.1:6379> bitcount key -2 -1 # 负数照用
(integer) 2
```