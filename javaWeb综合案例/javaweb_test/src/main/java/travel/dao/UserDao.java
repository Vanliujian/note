package travel.dao;

import travel.domain.User;

public interface UserDao {
    //1.根据用户名查询用户信息
    public User findUserByUsername(String username);

    //2.用户保存
    public void save(User user);

    User findUserByUsernameAndPassword(String username, String password);
}
