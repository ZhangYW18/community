package com.zhangyw.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

  // JDK's java.util.UUID has flawed implementation of compareTo(), which uses naive comparison of
  // 64-bit values. This does NOT work as expected, given that underlying content is for all
  // purposes unsigned. For example two UUIDs:
  //
  // 7f905a0b-bb6e-11e3-9e8f-000000000000
  // 8028f08c-bb6e-11e3-9e8f-000000000000
  // would be ordered with second one first, due to sign extension (second value is considered to be
  // negative, and hence "smaller").
  //
  // Because of this, you should always use external comparator, such as
  // com.fasterxml.uuid.UUIDComparator, which implements expected sorting order that is simple
  // unsigned sorting, which is also same as lexicographic (alphabetic) sorting of UUIDs (when
  // assuming uniform capitalization).
  public static String generateUUID() {
      return UUID.randomUUID().toString().replaceAll("-","");
  }

  // MD5 encrypt
  public static  String md5Encrypt(String key) {
      if (StringUtils.isBlank(key)) {
          return null;
      }
      return DigestUtils.md5DigestAsHex(key.getBytes());
  }
}
