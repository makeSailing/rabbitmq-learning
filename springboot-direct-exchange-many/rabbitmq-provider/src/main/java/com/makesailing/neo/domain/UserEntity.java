package com.makesailing.neo.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * #
 *
 * @Author: jamie.li
 * @Date: Created in  2018/9/9 17:56
 */
@Data
@Entity
@Table(name = "user_info")
public class UserEntity implements Serializable {

  private static final long serialVersionUID = -2703945921213927662L;

  /**
   * 用户编号
   */
  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;
  /**
   * 用户名称
   */
  @Column(name = "user_name")
  private String userName;
  /**
   * 姓名
   */
  @Column(name = "name")
  private String name;
  /**
   * 年龄
   */
  @Column(name = "age")
  private int age;
  /**
   * 余额
   */
  @Column(name = "balance")
  private BigDecimal balance;

}
