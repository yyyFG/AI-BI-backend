package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yy.springbootinit.model.entity.User;
import generator.service.UserService;
import com.yy.springbootinit.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author DCX
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-03-19 16:01:02
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




