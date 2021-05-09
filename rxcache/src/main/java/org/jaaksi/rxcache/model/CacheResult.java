/*
 * Copyright (C) 2017 zhouyou(478319399@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jaaksi.rxcache.model;

import java.io.Serializable;

/**
 * 缓存对象，可区分是否来自缓存
 */
public class CacheResult<T> implements Serializable {
  /** 是否来自缓存 */
  public boolean isFromCache;
  /** 用来记录，是否缓存数据（无效数据不缓存） */
  public boolean cacheable;
  public T data;

  public CacheResult() {
  }

  public CacheResult(boolean isFromCache, T data) {
    this.isFromCache = isFromCache;
    this.data = data;
  }
}
