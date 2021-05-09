package org.jaaksi.rxcache.type;

import org.jaaksi.rxcache.util.Utils;
import java.lang.reflect.Type;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 这里要定义为抽象类，supperclass是CacheType。获取泛型通过 cls.getGenericSuperclass()
 * new CacheType<T>() {}.getType().getClass().getGenericSuperclass();
 * 如果是非抽象类，super就是Object
 */

public abstract class CacheType<T> {

  public final Type getType() {
    //获取需要解析的泛型T类型
    return Utils.findNeedClass(getClass());
  }
}
