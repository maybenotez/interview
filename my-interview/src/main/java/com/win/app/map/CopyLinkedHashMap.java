package com.win.app.map;

import java.util.Map;

/**
 * Created by Administrator on 2017/12/22 0022.
 */
public class CopyLinkedHashMap<K,V>  extends CopyHashMap<K,V>
        implements Map<K,V> {
    public static void main(String[] args) {
    }
    static class Entry<K,V> extends CopyHashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
}
