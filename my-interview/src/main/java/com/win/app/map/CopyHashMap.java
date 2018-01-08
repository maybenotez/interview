package com.win.app.map;


import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
/**
 * Created by Administrator on 2017/12/22 0022.
 */
public class CopyHashMap<K,V> extends CopyAbstractMap<K,V> implements Map<K,V>,Serializable{
  
   /**
     * 初始容器大小
     */
    static final int DEFAULT_INITIAL_CAPACITIES = 1<<4;
    /**
     * 最大容器大小
     */
    static final int MAX_CAPACITY = 1<<30;

    static final float DEFUALT_LOAD_FACTOR =0.75F;

    static final int TERRIFY_THRESHOLD =8;

    static final int UNTERRIFY_THRESHOLD =6;

    static final int MIN_TERRIFY_CAPACITY =64;

    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public final K getKey() {
            return key;
        }

        @Override
        public final V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final int hashCode(){
            return Objects.hash(key)^Objects.hash(value);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> es = (Map.Entry<?, ?>) o;

                if (Objects.equals(key, es.getKey()) && Objects.equals(value, es.getValue())) {
                    return true;
                }

            }
            return false;
        }
    }
        static final int hash(Object key){
            int h;
            return (key == null)?0:(h=key.hashCode())^(h>>>16);
        }

        static Class<?> comparableClassFor(Object x){
            if(x instanceof Comparable){
                Class<?> c;
                Type[] ts,as;Type t;
                ParameterizedType p;
                if((c = x.getClass()) ==String.class){
                    return c;
                }
                if((ts =c.getGenericInterfaces()) != null){
                    for (int i = 0; i < ts.length; ++i) {
                        if(((t=ts[i]) instanceof ParameterizedType) &&
                                ((p= (ParameterizedType)t).getRawType() ==Comparable.class) &&
                        (as=p.getActualTypeArguments())!=null && as.length==1&&as[0]==c){
                            return c;
                        }
                    }
                }
            }
            return null;
        }
        static int compareComparable(Class<?> kc,Object k,Object x){
            return (x == null ||x.getClass() != kc?0: ((Comparable)k).compareTo(x));
        }
        static int tableSizeFor(int cap){
            int n = cap -1;
            n |= n>>>1;
            n |= n>>>2;
            n |= n>>>3;
            n |= n>>>4;
            n |= n>>>8;
            n |= n>>>16;
            return (n<0)?1:(n>=MAX_CAPACITY)?MAX_CAPACITY :n+1;
        }

    /**
     * 第一次使用和扩容必须初始化
     * 2的倍数
     */
    transient Node<K,V>[] table;
    /**
     * 保持entrySet()的缓存， 记录AbstractMap 用来keySet() 和values()的属性
      */
    transient Set<Map.Entry<K,V>> entrySet;
    /**
     * map key-value映射的键值队
      */
    transient int size;
    /**
     * 记录map呗构建的次数
     */
    transient int modCount;
    /**
     * 下一次是否扩容的因子
     */
    int threshold;
    /**
     *
     */
    final float loadFactor;

        public CopyHashMap(int initialCapcity,float loadFactor){
            if(initialCapcity<0){
                throw  new IllegalArgumentException("illegal Capacity:"+initialCapcity);
            }
            if(initialCapcity>MAX_CAPACITY){
                initialCapcity =MAX_CAPACITY;
            }
            if(loadFactor<=0 ||Float.isNaN(loadFactor)){
                throw new IllegalArgumentException("illegal loadFactor:"+loadFactor);
            }
            this.loadFactor = loadFactor;
            this.threshold =tableSizeFor(initialCapcity);
        }
    public CopyHashMap(int initialCapcity){
            this(initialCapcity,MAX_CAPACITY);
    }
    public CopyHashMap(){
        this.loadFactor = DEFUALT_LOAD_FACTOR;
    }

    public CopyHashMap(Map<? extends K,? extends V> m){
        this.loadFactor = DEFUALT_LOAD_FACTOR;
    }
    final void putMapEntries(Map<? extends K,?extends V> m,boolean evict){
        int s = m.size();
        if(s>0){
            if(table == null){
                float ft = ((float)s/loadFactor) +1.0F;
                int t = (ft<(float) MAX_CAPACITY)? (int) ft :MAX_CAPACITY;
              if(t >threshold){
                  threshold = tableSizeFor(t);
              }
            }
            else if(s>threshold){
                resize();
            }
            for (Map.Entry<? extends K,? extends V> e:m.entrySet()){
                K key =e.getKey();
                V value = e.getValue();
                putVal(hash(key),key,value,false,evict);
            }
        }
    }

    /**
     * 返回map里的键值队
     * @return
     */
    public int sise(){
        return size;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public V get(Object key){
        Node<K,V> e;
        return (e =getNode(hash(key),key))==null?null:e.value;
    }

    final Node<K,V> getNode(int hash,Object key){
        Node<K,V>[] tab;Node<K,V> first,e;int n;K k;
        if((tab=table)!= null && (n=tab.length)>0 &&
                (first = tab[(n-1)&hash])!=null){
            if(first.hash == hash &&
                    ((k=first.key) == key ||(key !=null && key.equals(k)))){
                return first;
            }
            if((e = first.next)!= null){
                if(first instanceof TreeNode){
                    return ((TreeNode) first).getTreeNode(hash,key);
                }
                do {
                    if(e.hash == hash &&((k = e.key)==key ||(key!= null && key.equals(k)))){
                        return e;
                    }
                }while ((e=e.next)!= null);
            }
        }
        return null;
    }

    public boolean containsKey(Object key){
        return getNode(hash(key),key)!=null;
    }
    public V put(K key,V value){
        return putVal(hash(key),key,value,false,true);
    }
    final V putVal(int hash,K key,V value,boolean  onlyIfAbsent,boolean evict){
        Node<K,V>[] tab; Node<K,V>p;int n,i;
        if((tab= table) ==null ||(n = tab.length)==0){
            n = (tab=resize()).length;
        }
        if((p = tab[i =(n-1)&hash]) == null){
            tab[i] = newNode(hash,key,value,null);
        }
        else{
            Node<K,V> e;K k;
            if(p.hash == hash &&((k=p.key) == key ||(key != null) && key.equals(k))){
                e = p;
            }
            else if (p instanceof  TreeNode){
                  e = ((TreeNode) p).putTreeVal(this,tab,hash,key,value);
            }
            else {
                for (int binCount = 0;;++binCount){
                    if((e = p.next) == null){
                        p.next =newNode(hash,key,value,null);
                        if(binCount>=TERRIFY_THRESHOLD-1){
                            terrfiyBin(tab,hash);
                        }
                        break;
                    }
                    if(e.hash ==hash &&
                            ((k = p.key) == key || (key != null && key.equals(k)))){
                        break;
                    }
                    p =e;
                }
            }
            if(e != null){
                V oldValue = e.value;
                if(!onlyIfAbsent || oldValue == null){
                    e.value =value;
                }
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if(++size >threshold){
            resize();
        }
        afterNodeInsertion(evict);
        return null;
    }

    final void terrfiyBin(Node<K,V>[] tab,int hash){
        int n,index;Node<K,V> e;
        if(tab == null || (n =tab.length)<MIN_TERRIFY_CAPACITY){
            resize();
        }
        else if ((e = tab[index= (n-1)&hash]) != null){
            TreeNode<K,V> hd = null,t1=null;
            do {
                TreeNode<K,V> p = this.replacementTreeNode(e,null);
                if(t1 == null){
                    hd = p;
                }
                else{
                    p.prev = t1;
                    t1.next = p;
                }
                t1 = p;
            }while ((e = e.next)!= null);
            if((tab[index] = hd)!=null){
                hd.treeify(tab);
            }
        }
    }

    final Node<K,V>[] resize(){
        Node<K,V>[] oldTab =  table;
        int oldCap = (oldTab ==null)?0:oldTab.length;
        int oldThr =  threshold;
        int newCap, newThr =0;
        if(oldCap>0) {
            if (oldCap >= MAX_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            } else if ((newCap = oldCap << 1) < MAX_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITIES) {
                newThr = oldThr << 1;
            }
        }
            else if (oldThr > 0) {
                newCap = oldThr;
            } else {
                newCap = DEFAULT_INITIAL_CAPACITIES;
                newThr = (int) (DEFAULT_INITIAL_CAPACITIES * DEFUALT_LOAD_FACTOR);
            }
            if (newThr == 0) {
                float ft = newCap * loadFactor;
                newThr = newCap < MAX_CAPACITY && ft < MAX_CAPACITY ? (int) ft : Integer.MAX_VALUE;
            }
            threshold = newThr;
            Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
            table = newTab;
            if (oldTab != null) {
                for (int j = 0; j < oldCap; ++j) {
                    Node<K, V> e;
                    if ((e = oldTab[j]) != null) {
                        oldTab[j] = null;
                        if (e.next == null) {
                            newTab[e.hash & (newCap - 1)] = e;
                        } else if (e instanceof TreeNode) {
                            ((TreeNode) e).split(this, newTab, j, oldCap);
                        } else {
                            Node<K, V> loHead = null, loTail = null;
                            Node<K, V> hiHead = null, hiTail = null;
                            Node<K, V> next;
                            do {
                                next = e.next;
                                if ((e.hash & oldCap) == 0) {
                                    if (loTail == null) {
                                        loHead = e;
                                    } else {
                                        loTail.next = e;
                                    }
                                    loTail = e;
                                } else {
                                    if (hiTail == null) {
                                        hiHead = e;
                                    } else {
                                        hiTail.next = e;
                                    }
                                    hiTail = e;
                                }
                            } while ((e = next) != null);
                            if (loTail != null) {
                                loTail.next = null;
                                newTab[j] = loHead;
                            }
                            if (hiTail != null) {
                                hiTail.next = null;
                                newTab[j + oldCap] = hiHead;
                            }
                        }
                    }
                }
            }
        return newTab;
    }

    public void putAll(Map<? extends K,? extends V> m){
        putMapEntries(m,true);
    }
    public V remove(Object key){
        Node<K,V> e;
        return (e=removeNode(hash(key),key,null,false,true))==null?null:e.value;
    }

    final Node<K,V> removeNode(int hash,Object key,Object value,boolean matchValue,boolean movable){
        Node<K,V>[] tab; Node<K,V> p; int n,index;
        if((tab = table)!= null && (n = tab.length)>0 &&
                (p=tab[index=(n-1)&hash])!= null){
            Node<K,V> node =null,e;K k;V v;
            if(p.hash == hash &&
                    ((k = p.key)==key||
                    (key !=null && key.equals(k)))){
                node = p;
            }
            else if ((e = p.next)!= null){
                if(p instanceof TreeNode){
                    node = ((TreeNode) p).getTreeNode(hash(hash),key);
                }
                else{
                    do {
                        if(e.hash == hash && ((k = e.key) ==key ||(key != null && key.equals(k)))){
                            node = e;
                            break;
                        }
                        p=e;
                    }while((e= e.next)!= null);
                }
            }
            if(node != null &&(!matchValue || (v = node.value) == value ||
                    (value!= null &&value.equals(v)))){
                if(node instanceof TreeNode){
                    ((TreeNode) node).removeTreeNode(this,tab,movable);
                }
                else if (node == p){
                    tab[index] = node.next;
                }
                else {
                    p.next= node.next;
                }
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

    public void clear(){
        Node<K,V>[] tab;
        modCount++;
        if (((tab= table)!=null) && size>0){
            size= 0;
            for (int i=0;i<tab.length;++i){
                tab[i] = null;
            }
        }
        System.out.println(table);
    }
    public boolean containsValue(Object value){
        Node<K,V>[] tab;V v;
        if ((tab = table)!=null && size>0){
            for (int i=0;i<table.length;++i){
                for (Node<K,V> e= tab[i];e!=null;e=e.next){
                    if ((v=e.value)==value || (value!=null && value.equals(v))){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public Set<K> keySet(){
       Set<K>  ks  = keySet;
       if (ks == null){
        //   ks = new KeySet()
       }
        return null;
    }

    /*
           以下是会被子类重写的方法
      *  */
    void afterNodeInsertion(boolean evict) { }

    void afterNodeRemoval(Node<K,V> p) { }

    void afterNodeAccess(Node<K,V> p) { }
    Node<K,V> newNode(int hash,K key,V value,Node<K,V> next){
        return new Node<K,V>(hash,key,value,next);
    }
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
       Set<Map.Entry<K,V>> es;
        return (es =entrySet)==null?(entrySet= new EntrySet()):es;
    }
    final class EntrySet extends CopyAbstractSet<Map.Entry<K,V>>{
       public final void clear(){
           CopyHashMap.this.clear();
       }

        @Override
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry)){
                return false;
            }
            Map.Entry<?,?> e= (Entry<?, ?>) o;
            Object key = e.getKey();
            Node<K, V> node = getNode(hash(key), key);
            return node!=null && node.equals(o);
        }
        public final boolean remove(Object o){
           if (o instanceof  Map.Entry){
               Map.Entry e = (Entry) o;
               Object key = e.getKey();
               Object value = e.getValue();
               return removeNode(hash(key),key,value,true,true)!=null;
           }
           return false;
        }

        @Override
        public Spliterator<Map.Entry<K,V>> spliterator() {

            return new EntrySpliterator<>(CopyHashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super Map.Entry<K,V>> action){
            Node<K,V>[] tab;
            if (action == null){
                throw new NullPointerException();
            }
            if (size>0 && (tab = table)!= null){
                int mc = modCount;
                for (int i=0;i<tab.length;i++){
                    for (Node<K,V> e = tab[i];e!= null;e= e.next){
                        action.accept(e);
                    }
                }
                if (modCount != mc ){
                    throw new ConcurrentModificationException();
                }
            }

        }

        @Override
        public final int size() {
            return size;
        }
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e=getNode(hash(key),key))==null?defaultValue:e.getValue();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key),key,value,true,true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key),key,value,true,true)!=null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e;V v;
        if ((e=getNode(hash(key),key))!=null &&((v=e.value)==oldValue ||v!=null && v.equals(oldValue))){
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e =getNode(hash(key),key))!=null){
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null){
            throw new NullPointerException();
        }
        int hash = hash(key);
        Node<K,V>[] tab;Node<K,V> first; int n,i;
        int binCount = 0;
        TreeNode<K,V> t= null;
        Node<K,V> old = null;
        if (size>threshold || (tab = table)==null ||( n= tab.length)==0){
            n = (tab = resize()).length;
        }
        if ((first = tab[i=(n-1)&hash])!= null){
            if (first instanceof TreeNode){
                old= (t = (TreeNode<K, V>) first).getTreeNode(hash,key);
            }
            else {
                Node<K,V> e =first;K k;
                do {
                    if (e.hash ==hash && ((k=e.key) == key || (k!= null && k.equals(key)))){
                        old = e;
                        break;
                    }
                    ++binCount;
                }while ((e = e.next)!= null);
            }
            V oldValue;
            if (old != null && (oldValue =old.value)!=null){
                afterNodeAccess(old);
                return oldValue;
            }
        }
        V v = mappingFunction.apply(key);
        if(v == null){
            return null;
        }
        else if (v != null){
            old.value =v;
            afterNodeAccess(old);
            return v;
        }
        else if (t!= null){
            t.putTreeVal(this,tab,hash,key,v);
        }else{
            tab[i]  = newNode(hash,key,v,first);
            if (binCount>= TERRIFY_THRESHOLD-1){
                terrfiyBin(tab,hash);
            }
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null){
            throw new NullPointerException();
        }
        Node<K,V> e;V oldValue;
        int hash  = hash(key);
        if ((e = getNode(hash,key))!= null &&
                (oldValue = e.value)!=null){
            V v = remappingFunction.apply(key, oldValue);
            if (v != null){
                e.value =v;
                afterNodeAccess(e);
                return v;
            }
            else {
                removeNode(hash,key,null,false,true);
            }
        }
        return  null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null){
            throw new NullPointerException();
        }
        int hash = hash(key);
        Node<K,V>[] tab;Node<K,V> first;int n,i;
        int binCount=0;
        TreeNode<K,V> t= null;
        Node<K,V> old= null;
        if (size>threshold || (tab = table)==null ||
                (n= tab.length)==0){
            n = (tab= resize()).length;
        }
        if ((first = tab[i=(n-1)&hash])!=null){
            if (first instanceof TreeNode){
                old  = (t = (TreeNode<K, V>) first).getTreeNode(hash,key);

            }else {
                Node<K,V> e= first;K k;
                do {
                    if (e.hash ==hash && ((k =e.key)==key || (k!=null&&k.equals(key)))){
                        old = e;
                        break;
                    }
                    ++binCount;
                }while ((e=e.next)!=null);
            }

        }
        V oldValue = (old == null)?null:old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (oldValue!= null){
            if (v!= null){
                old.value = v;
                afterNodeAccess(old);
            }
            else {
                removeNode(hash,key,null,false,true);
            }
        }else if (v!= null){
            if (t!= null){
                t.putTreeVal(this,tab,hash,key,v);
            }else {
                tab[i] = newNode(hash,key,v,first);
                if (binCount>= TERRIFY_THRESHOLD-1){
                    terrfiyBin(tab,hash);
                }
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null){
            throw new NullPointerException();
        }
        if (remappingFunction == null){
            throw new NullPointerException();
        }
        int hash = hash(key);
        Node<K,V>[] tab;Node<K,V> first;int n,i;
        int binCount=0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size>threshold || (tab= table)==null || (n=(tab.length))==0){
            n = (tab=resize()).length;
        }
        if ((first = tab[i=(n-1)&hash])!=null){
            if (first instanceof TreeNode){
                old = (t= (TreeNode<K, V>) first).getTreeNode(hash,key);
            }
            else{
                Node<K,V> e = first;K k;
                do {
                    if ((k=e.key) == key ||(k!=null && k.equals(key))){
                        old = e;
                        break;
                    }
                    ++binCount;
                }while ((e = e.next)!=null);
            }
        }
        if (old != null){
                V v;
                if (old.value!= null){
                    v = remappingFunction.apply(old.value,value);
                }
                else{
                    v =value;
                }
                if (v != null){
                    old.value = v;
                    afterNodeAccess(old);
                }
                else {
                    removeNode(hash,key,value,true,true);
                }
                return v;

        }
        if (value!= null){
            if (t!= null){
                t.putTreeVal(this,tab,hash,key,value);
            }
            else {
                tab[i] = newNode(hash,key,value,first);
                if (binCount>=TERRIFY_THRESHOLD-1){
                    terrfiyBin(tab,hash);
                }
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null){
            throw new NullPointerException();
        }
        if (size>0 && (tab=table)!=null){
            int mc = modCount;
            for (int i = 0; i <tab.length ; ++i) {
                for (Node<K,V>e= tab[i];e!=null;e=e.next){
                    action.accept(e.key,e.value);
                }
            }
            if (modCount!=mc){
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function ==null){
            throw new NullPointerException();
        }
        if (size>0 && (tab= table)!=null){
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e=tab[i];e!=null;e=e.next){
                    e.value = function.apply(e.key,e.value);
                }
            }
            if (mc !=modCount){
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CopyHashMap<K,V> result;
        try {
            result = (CopyHashMap<K, V>) super.clone();
        }catch (CloneNotSupportedException e){
            throw new InternalError();
        }
       result.reinitialize();
        result.putMapEntries(this,false);
        return result;
    }
    final float loadFactor(){
        return loadFactor;
    }
    final int capacity(){
        return (table!=null)?table.length:(threshold>0)?threshold:DEFAULT_INITIAL_CAPACITIES;
    }

    private void writeObject(java.io.ObjectOutputStream s)throws IOException{
        int bucket = capacity();
        s.defaultWriteObject();
        s.writeInt(bucket);
        s.writeInt(size);
        internalWriteEntries(s);
    }
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException{
        Node<K,V>[] tab;
        if (size>0 && (tab=table)!=null){
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e=tab[i];e!=null;e=e.next){
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }
    private void readObject(java.io.ObjectInputStream s)
    throws IOException,ClassNotFoundException{
        s.defaultReadObject();
        reinitialize();
        if (loadFactor<=0 || Float.isNaN(loadFactor)){
            throw new InvalidObjectException("");
        }
        s.readInt();
        int mapping = s.readInt();
        if (mapping< 0){
            throw new InvalidObjectException("");
        }
        else if (mapping>0){
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = mapping / (1f + 1.0f);
            int cap = ((fc<DEFAULT_INITIAL_CAPACITIES)?DEFAULT_INITIAL_CAPACITIES:(fc>=MAX_CAPACITY)?MAX_CAPACITY:tableSizeFor((int) fc));
            float ft = cap * lf;
            threshold = (cap<MAX_CAPACITY && ft<MAX_CAPACITY)? (int) ft :Integer.MAX_VALUE;
            Node<K,V>[] tab=(Node<K,V>[])new Node[cap];
        table = tab;
            for (int i = 0; i < mapping; i++) {
                K k = (K) s.readObject();
                V v = (V) s.readObject();
                putVal(hash(k),k,v,false,false);
            }
        }

    }

    void reinitialize(){
        table = null;
        entrySet = null;
        keySet = null;
        values=null;
        modCount = 0;
        threshold=0;
        size = 0;
    }

    static class HashMapSpliterator<K,V> {
        final CopyHashMap<K,V> map;
        Node<K,V> current;
        int index;
        int fence;
        int est;
        int expectedModCount;

        public HashMapSpliterator(CopyHashMap<K, V> map, int origin, int fence, int est, int expectedModCount) {
            this.map = map;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }
        final int getFence(){
            int hi;
            if ((hi =fence)>0){
                CopyHashMap<K,V> m = map;
                est = m.sise();
                expectedModCount = m.modCount;
                Node<K, V>[] table = m.table;
                hi = fence =(table ==null)?0:table.length;
            }
            return hi;
        }
        public final long estimateSize(){
            getFence();
            return (long)est;
        }
    }
    static final class EntrySpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<Map.Entry<K,V>>{

        public EntrySpliterator(CopyHashMap<K, V> map, int origin, int fence, int est, int expectedModCount) {
            super(map, origin, fence, est, expectedModCount);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null){
                throw new NullPointerException();
            }
            Node<K, V>[] tab = map.table;
            if (tab!= null && tab.length>=(hi = getFence()) && index>0){
                while (current != null || index <hi){
                    if (current == null){
                        current =tab[index++];
                    }
                    else {
                        Node<K, V> e = this.current;
                        current = current.next;
                    action.accept(e);
                    if (map.modCount != expectedModCount){
                        throw new ConcurrentModificationException();
                    }
                    return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<Entry<K, V>> trySplit() {
            int hi = getFence(),lo = index,mid = (lo+hi)>>>1;
            return (lo>=mid||current!= null)?null:
                    new EntrySpliterator<K, V>(map,lo,index=mid,est>>>=1,expectedModCount);
        }

        @Override
        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            int i,hi,mc;
            if (action == null){
                throw new NullPointerException();
            }
            CopyHashMap<K, V> m = map;
            Node<K, V>[] tab = m.table;

            if ((hi= fence)<0){
                mc = expectedModCount = map.modCount;
                hi = fence = (tab == null)?0:tab.length;
            }
            else {
                mc =expectedModCount;
            }
            if (tab!= null && tab.length>=hi && (i = index)>0 && (i<(index=hi) || current !=null)){
                Node<K, V> p = this.current;
                current = null;
                do {
                    if (p == null){
                        p = tab[i++];
                    }
                    else{
                        action.accept(p);
                        p= p.next;
                    }
                }while (p!= null ||i<hi);
                if (m.modCount!= mc){
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public int characteristics() {
            return (fence<0 ||est== map.sise()?Spliterator.SIZED:0) |Spliterator.DISTINCT;
        }
    }
    abstract class HashIterator{
        Node<K,V> next;
        Node<K,V> current;
        int expectedModCount;
        int index;

        public HashIterator() {
            this.expectedModCount = modCount;
            Node<K,V>[] t = table;
            current =next = null;
            index = 0;
            if (t!= null&& size()>0){
                do{}while (index<t.length && (next=t[index++])==null);
            }
        }
        public final boolean hasNext(){
            return next!=null;
        }
        final Node<K,V> nextNode(){
            Node<K,V>[] t;
            Node<K,V> e =next;
            if (modCount != expectedModCount){
                throw new ConcurrentModificationException();
            }
            if (e == null){
                throw new NoSuchElementException();
            }
            if ((next = (current=e).next)==null && (t= table)!= null){
                do{}while (index<t.length && (next= t[index++])==null);
            }
            return e;
        }
        public final void remove(){
            Node<K,V> p =current;
            if (p == null){
                throw new IllegalStateException();
            }
            if (modCount!= expectedModCount){
                throw new ConcurrentModificationException();
            }
            current = null;
            K key = p.key;
            removeNode(hash(key),key,null,false,false);
            expectedModCount = modCount;
        }
    }
    final class KeyIterator extends HashIterator implements Iterator<K> {

        public final K next(){
            return nextNode().key;
        }

    }
    final class ValueIterator extends HashIterator implements Iterator<V>{

        @Override
        public V next() {
            return nextNode().value;
        }
    }
    static final class ValueSpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<V>{

        public ValueSpliterator(CopyHashMap<K, V> map, int origin, int fence, int est, int expectedModCount) {
            super(map, origin, fence, est, expectedModCount);
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null){
                throw new NullPointerException();
            }
            Node<K, V>[] tab = map.table;
            if (tab!=null && tab.length>=(hi=getFence())&&index>=0){
                while (current!=null ||index<hi){
                    if (current == null){
                        current = tab[index++];
                    }else {
                        V value = current.value;
                        current = current.next;
                        action.accept(value);
                        if (map.modCount!= expectedModCount){
                            throw new ConcurrentModificationException();
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<V> trySplit() {
            int hi = getFence(),lo = index,mid = (lo+hi)>>>1;
            return (lo>=mid || current!=null)?null:new ValueSpliterator<K, V>(map,lo,index=mid,est,expectedModCount>>>1);
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            int i,hi,mc;
            if (action == null){
                throw new NullPointerException();
            }
            CopyHashMap<K,V> m=map;
            Node<K, V>[] tab = m.table;
            if ((hi=fence)<0){
                mc = expectedModCount = m.modCount;
                hi = fence=(tab==null)?0:tab.length;
            }
            else {
                mc =expectedModCount;

            }
            if (tab!= null && tab.length>=hi && (i=index)>=0 && (i<(index=hi)||current!=null)){
                Node<K, V> p = this.current;
                current = null;
                do {
                    if (p ==null){
                        p = tab[i++];
                    }else{
                        action.accept(p.value);
                        p=p.next;
                    }
                }while (p!=null||i<hi);
                if (map.modCount!=mc){
                    throw new ConcurrentModificationException();
                }
            }

        }

        @Override
        public int characteristics() {
            return (fence<0 ||est==map.sise()?Spliterator.SIZED:0);
        }
    }
    final class EntryIterator extends HashIterator implements Iterator<Map.Entry<K,V>>{
        public final Map.Entry<K,V> next() { return nextNode(); }

    }
    Node<K,V> replacementNode(Node<K,V> p,Node<K,V> next){
        return new Node<>(p.hash,p.key,p.value,next);
    }
    TreeNode<K,V> replacementTreeNode(Node<K,V> p,Node<K,V> next){
        return new TreeNode<K, V>(p.hash,p.key,p.value,next);
    }
    static final class TreeNode<K,V> extends CopyLinkedHashMap.Entry{
        TreeNode<K,V> parent;
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;
        boolean red;
        TreeNode(int hash, Object key, Object value, Node next) {
            super(hash, key, value, next);
        }

        final TreeNode<K,V> root(){
            for (TreeNode<K,V> r =this,p;;){
                if((p =r.parent) ==null){
                    return r;
                }
                r = p;
            }
        }
        static <K,V>void moveRootToFront(Node<K,V>[] tab,TreeNode<K,V> root){
            int n;
            if(root !=null && tab!= null &&(n = tab.length)>0){
                int index = (n-1) &root.hash;
                TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
               if(root != first){
                   Node<K,V> rn;
                   tab[index] =root;
                   TreeNode<K,V> rp = root.prev;
                   if((rn= root.next)!= null){
                       ((TreeNode)rn).prev = rp;
                   }
                   if(rp != null){
                       rp.next = rn;
                   }
                   if(first !=null){
                       first.prev = root;
                   }
                   root.next = first;
                   root.prev =null;
               }
                assert checkInvariants(root);
            }
        }
        final TreeNode<K,V> find(int h,Object k,Class<?> kc){
            TreeNode<K,V> p = this;
            do{
                int ph,dir;K pk;
                TreeNode<K,V> pl = p.left,pr = p.right,q;
                if ((ph = p.hash)>h){
                     p = pl;
                }
                else if(ph<h){
                    p = pr;
                }

                else if ((pk = (K) p.key) == k ||(k!= null) && k.equals(pk)){
                    return p;
                }
                else if (pl == null){
                    p = pr;
                }
                else if (pr == null){
                    p = pl;
                }
                else if ((kc!= null ||(kc =comparableClassFor(k)) != null) && (dir = compareComparable(kc,k,pk))!=0){
                    p =(dir<0)?pl:pr;
                }
                else if ((q= pr.find(h,k,kc)) !=null){
                    return q;
                }
                else{
                    p = pl;
                }
            }while (p != null);
            return null;
        }

        final TreeNode<K,V> getTreeNode(int h,Object k){
            return ((parent!= null)? root():this).find(h,k,null);
        }
        static int tieBreakOrder(Object a,Object b){
            int d;
            if(a == null || b == null ||(d = a.getClass().getName().compareTo(b.getClass().getName()))==0){
                d = System.identityHashCode(a)<=System.identityHashCode(b)?-1:1;
            }
            return d;
        }

        final void treeify(Node<K,V>[] tab){
            TreeNode<K,V> root  =null;
            for (TreeNode<K,V> x =this,next;x!= null;x = next){
                 next = (TreeNode<K, V>) x.next;
                 x.right = x.left = null;
                 if(root == null){
                     x.parent = null;
                     x.red =false;
                     root = x;
                 }
                 else{
                     K k = (K) x.key;
                     int h = x.hash;
                     Class<?> kc = null;
                     for (TreeNode<K,V>  p =root;;){
                         int dir,ph;
                         K pk = (K) p.key;
                     if ((ph =p.hash)>h){
                         dir = -1;
                     }
                     else if (ph<h){
                         dir = 1;
                     }
                     else if ((kc == null &&(kc=comparableClassFor(k))==null) ||
                             (dir =compareComparable(kc,k,pk)) ==0){
                         dir = tieBreakOrder(k,pk);
                     }
                     TreeNode<K,V> xp= p;
                     if((p = (dir<= 0)?p.left:p.right) == null){
                         x.parent = p;
                         if(dir<=0){
                             xp.left =x;
                         }
                         else{
                             xp.right =x;
                         }
                         root = balanceInserction(root,x);
                     break;
                     }
                     }
                 }
            }
            moveRootToFront(tab,root);
        }

        final Node<K,V> unterrify(CopyHashMap<K,V> map){
            Node<K,V> hd =null,tl = null;
            for (Node<K,V> q =this;q!=null;q =q.next){
                Node<K,V>  p = map.replacementNode(q,null);
                if(tl == null){
                    hd = p;
                }else{
                    tl.next = p;
                }
                tl = p;
            }
            return hd;
        }
        final Node<K,V> putTreeVal(CopyHashMap<K,V> map,Node<K,V>[] tab,int h,K k,V v){
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K,V> root = (parent!=null)?root():this;
            for (TreeNode<K,V> p=root;;){
                int dir,ph;K pk;
                if((ph = p.hash)>h){
                    dir =-1;
                }
                else if (ph <h){
                    dir =1;
                }
                else if ((pk= (K) p.key) == k||(k!=null) && k.equals(pk)){
                    return p;
                }
                else if ((kc == null &&
                        (kc = comparableClassFor(k))==null)||
                        (dir = compareComparable(kc,k,pk))==0){
                        if(!searched){
                            TreeNode<K,V>q,ch;
                            searched =true;
                            if(((ch = p.left)!=null &&
                                    (q=ch.find(h,k,kc)) !=null)||
                                    ((ch =p.right) !=null &&
                                            (q=ch.find(h,k,kc))!=null)){
                                return q;
                            }
                        }
                        dir = tieBreakOrder(k,pk);
                }
                TreeNode<K,V> xp =p;
                if ((p = (dir<=0)?p.left:p.right) == null){
                    Node<K,V> xpn =xp.next;
                    TreeNode<K,V> x =map.newTreeNode(h,k,v,xpn);
                    if(dir<=0){
                        xp.left = x;
                    }else{
                        xp.right =x;
                    }
                    xp.next =x;
                    x.parent= x.prev = xp;
                    if(xpn!= null){
                        ((TreeNode)xpn).prev = x;
                    }
                    moveRootToFront(tab,balanceInserction(root,x));
                    return null;
                }
            }
        }

        final void removeTreeNode(CopyHashMap<K,V> map,Node<K,V>[] tab,boolean movable){
            int n;
            if (tab == null|| (n = tab.length)==0){
                return;
            }
            int index = (n-1)&hash;
        TreeNode<K,V>  first  = (TreeNode<K, V>) tab[index],root =first,rl;
        TreeNode<K,V> succ = (TreeNode<K, V>) next,pred =prev;
        if(pred == null){
            tab[index] =first = succ;
        }
        else {
            pred.next = succ;
        }
        if(succ != null){
            succ.prev  = pred;
        }
        if(first == null){
            return;
        }
        if(root.parent !=null){
            root = root.root();
        }
        if(root== null||root.right ==null||
                (rl=root.left) == null||rl.left==null){
            tab[index] = first.unterrify(map);
            return;
        }
        TreeNode<K,V> p =this,pl = left,pr = right,replacement;
        if (pl != null && pl != null){
            TreeNode<K,V> s =pr,sl;
            while ((sl=s.left)!= null){
                s =sl;
            }
            boolean c= s.red;s.red = p.red;p.red =c;
            TreeNode<K,V> sr = s.right;
            TreeNode<K,V> pp = p.parent;
            if(s==pr){
                p.parent = s;
                s.right = p;
            }
            else {
                TreeNode<K,V> sp =s.parent;
                if ((p.parent = sp)!= null){
                    if (s == sp.left){
                        sp.left =p;
                    }
                    else{
                       sp.right =p;
                    }
                }
                if ((s.right =pr)!= null){
                    pr.parent =s;
                }
            }
            p.left = null;
            if ((p.right =sr)!= null){
                sr.parent = p;
            }
            if ((s.left = pl)!=null){
                pl.parent = s;
            }
            if ((s.parent=pp)==null){
                root =s;
            }
            else if(p ==pp.left){
                pp.left =s;
            }
            else {
                pp.right =s;
            }
            if (sr != null){
                replacement = sr;
            }
            else {
                replacement = p;
            }
        }
        else if (pl!= null){
            replacement = pl;
        }
        else if (pr!= null){
            replacement = pr;
        }
        else{
            replacement =p;
        }
        if (replacement != p){
            TreeNode<K,V> pp =replacement.parent = p.parent;
            if (pp ==null){
                root=  replacement;
            }
            else if (p ==pp.left){
                pp.left= replacement;
            }
            else{
                pp.right = replacement;
            }
            p.left= p.right=p.parent =null;
        }
        TreeNode<K,V> r=p.red?root:balanceInserction(root,replacement);
        if (replacement == p){
            TreeNode<K,V> pp = p.parent;
            p.parent= null;
            if (pp!= null){
                if (p == p.left){
                    pp.left= null;
                }
                else if (p == pp.right){
                    pp.right =null;
                }
            }
        }
        if (movable){
            moveRootToFront(tab,r);
        }
        }
        final void split(CopyHashMap map,Node<K,V>[] tab,int index,int bit){
            TreeNode<K,V> b =this;
            TreeNode<K,V> loHead =null,loTail = null;
            TreeNode<K,V> hiHead = null,hiTail = null;

            int lc =0,hc =0;
            for (TreeNode<K,V> e =b,next;e!=null;e= next){
                next = (TreeNode<K, V>) e.next;
                e.next =null;
                if((e.hash & bit) == 0){
                    if ((e.prev=loTail)==null){
                        loHead =e;
                    }else{
                        loTail.next =e;
                    }
                    loTail= e;
                    ++lc;
                }else{
                    if((e.prev=hiTail)==null){
                        hiHead =e;
                    }else{
                        hiTail.next=e;
                        ++hc;
                    }
                }
            }
            if(loHead!=null){
                if(lc<=UNTERRIFY_THRESHOLD){
                    tab[index] = loHead.unterrify(map);
                }
                else {
                    tab[index] = loHead;
                    if(hiHead != null){
                        loHead.treeify(tab);
                    }
                }
            }
            if(hiHead!=null){
                if(hc<=UNTERRIFY_THRESHOLD){
                    tab[index+bit] =hiHead.unterrify(map);
                }
                else{
                    tab[index+bit] = hiHead;
                    if(loHead!=null){
                        hiHead.treeify(tab);
                    }
                }
            }
        }


        final TreeNode<K,V> balanceInserction(TreeNode<K,V> root,TreeNode<K,V> x){
            x.red =true;
            for (TreeNode<K,V> xp,xpp,xppl,xppr;;){
                if((xp = x.parent) == null){
                    x.red = false;
                    return x;
                }
                else if (!xp.red ||(xpp = xp.parent) == null){
                    return root;
                }
                if(xp == (xppl= xpp.left)){
                    if((xppr = xpp.right) != null && xppr.red){
                        xppr.red =false;
                        xp.red=false;
                        xpp.red=true;
                        x =xpp;
                    }
                    else {
                        if(x == xp.right){
                            root = rotateLeft(root,x=xp);
                            xpp =(xp = x.parent) == null?null:xp.parent;
                        }
                        if (xp !=null){
                            xp.red =false;
                            if(xpp !=null){
                                xpp.red =true;
                                    root = rotateRight(root,xpp);
                            }
                        }
                    }
                }
                else{
                    if (xppl!=null && xppl.red){
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x= xpp;
                    }
                    else {
                        if (x == xp.left){
                            root = rotateLeft(root,x=xp);
                            xpp = (xp = x.parent)== null?null:xp.parent;
                        }
                        if(xp != null){
                            xp.red =false;
                            if(xpp != null){
                                xpp.red =true;
                                root = rotateLeft(root,xpp);
                            }
                        }
                    }
                }
            }
        }
        static <K,V>TreeNode<K,V> rotateRight(TreeNode<K,V> root,TreeNode<K,V> p){
            TreeNode l,pp,lr;
            if(p!=null&& (l = p.left)!=null ){
                    if((lr=p.left = l.right)!= null){
                        lr.parent = p;
                    }
                    if((pp = l.parent = p.parent) == null){
                        (root= l).red = false;
                    }
                    else if (pp.right ==p){
                        pp.right = l;
                    }
                    else{
                        pp.left = l;
                    }
                    l.right =p;
                    p.parent =l;
            }
            return root;
        }

        static <K,V>TreeNode<K,V> rotateLeft(TreeNode<K,V> root,TreeNode<K,V> p){
            TreeNode<K,V> r,pp,rl;
            if(p!= null &&(r=p.right)!=null){
                if((rl =p.right = r.left)!=null){
                    rl.parent = p;
                }
                if((pp = r.parent = p.parent)!= null){
                    (root =r).red =false;
                }else if (pp.left ==p){
                    pp.left =r;
                }else {
                    pp.right = r;
                }
                r.left =p;
                p.parent =r;
            }
            return root;
        }
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                    tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}
