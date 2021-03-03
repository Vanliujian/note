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

2. 