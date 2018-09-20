package com.makesailing.neo.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 17:56
 */
@Data
public class UserEntity implements Serializable {

  private static final long serialVersionUID = -2703945921213927662L;

  /**
   * 用户编号
   */
  private Long id;
  /**
   * 用户名称
   */
  private String userName;
  /**
   * 姓名
   */

  private String name;
  /**
   * 年龄
   */
  private int age;
  /**
   * 余额
   */
  private BigDecimal balance;


}
