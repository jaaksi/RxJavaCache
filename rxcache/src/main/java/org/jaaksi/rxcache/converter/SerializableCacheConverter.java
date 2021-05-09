package org.jaaksi.rxcache.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import org.jaaksi.rxcache.model.RealEntity;
import org.jaaksi.rxcache.util.Utils;

/**
 * <p>描述：序列化对象的转换器</p>
 * 1.使用改转换器，对象&对象中的其它所有对象都必须是要实现Serializable接口（序列化）<br>
 * 暂时不要使用这个Converter
 */
@SuppressWarnings(value = { "unchecked" })
public class SerializableCacheConverter implements ICacheConverter {

  @Override
  public <T> RealEntity<T> load(InputStream source, Type type) {
    //序列化的缓存不需要用到clazz
    RealEntity<T> value = null;
    ObjectInputStream oin = null;
    try {
      oin = new ObjectInputStream(source);
      value = (RealEntity<T>) oin.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      Utils.close(oin);
    }
    return value;
  }

  @Override
  public boolean writer(OutputStream sink, Object data) {
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(sink);
      oos.writeObject(data);
      oos.flush();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      Utils.close(oos);
    }
    return false;
  }
}
