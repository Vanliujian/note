- 模糊查询

  - 使用${}
  - 使用concat拼接，concat('%',#{username},'%')
  - 使用双引号“%”#{username}“%”

- 批量删除

  - 比如要删除id为1，2，3的三条数据

  - 传入的是String类型

    ```java
    void deleteById(String id);
    
    <delete id="deleteBySeveralId">
        delete from t_user where id in (${id});
    </delete>
    
    @Test
    public void testDeleteById() {
        SqlSession sqlSession = SqlSessionUtils.getSqlSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        mapper.deleteBySeveralId("1,2,3");
    }
    ```

- 动态设置表名

  - 使用${}
