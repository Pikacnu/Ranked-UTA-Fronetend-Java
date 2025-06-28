package com.pikacnu.src;

import com.pikacnu.src.commands.CommandInit;

/**
 * 指令註冊管理類別。
 */
public class Command {
  /**
   * 初始化所有指令。
   */
  public static void init() {
    CommandInit.init();
  }
}
