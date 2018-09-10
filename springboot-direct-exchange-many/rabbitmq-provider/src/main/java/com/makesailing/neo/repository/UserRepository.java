package com.makesailing.neo.repository;

import com.makesailing.neo.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 17:59
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {


}
