package com.win.app.map;

/**
 * Created by Administrator on 2017/12/22 0022.
 */
public class CopyAbstrackMap {
    protected CopyAbstractMap(){

    }

    public abstract Set<Entry<K,V>> entrySet();

    public static  class SimpleEntry<K,V> implements Entry<K,V>,java.io.Serializable{

        private static final long serialVersionUID = -6600744545348714387L;
        private final K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public SimpleEntry(Entry<? extends  K,? extends V> entry){
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V value1 = this.value;
            this.value =value;
            return value1;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Map.Entry)){
                return false;
            }
            Map.Entry<?,?>  e = (Entry<?, ?>) obj;

            return eq(key,e.getKey()) && eq(value,e.getValue());
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 :key.hashCode()) ^ (value == null?0:value.hashCode());
        }
        public String toString(){
            return key+"="+value;
        }
    }
    public static class SimpleImmutableEntry<K,V> implements Entry<K,V>,java.io.Serializable{

        private static final long serialVersionUID = -2984985958916137502L;

        private final K key;
        private final V value;

        public SimpleImmutableEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleImmutableEntry(Entry<? extends K,? extends V> entry) {
            this.key    = entry.getKey();
            this.value = entry.getValue();
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 :key.hashCode())^(value == null? 0 :value.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Map.Entry)){
                return false;
            }
            Map.Entry<?,?> e = (Entry<?, ?>) obj;
            return eq(key,e.getKey()) && eq(value,e.getValue());
        }

        @Override
        public String toString() {
            return key+"="+value;
        }
    }
    @Override
    public int size() {
        return entrySet().size();
    }

    @Override
    public boolean isEmpty() {
        return size()==0;
    }

    @Override
    public boolean containsKey(Object key) {
        Iterator<Entry<K, V>> i     = entrySet().iterator();
        if (key == null){
            while (i.hasNext()){
                Entry<K, V> next = i.next();
                if (next.getKey() == null){
                    return true;
                }
            }
        }
        else {
            while (i.hasNext()){
                Entry<K, V> next = i.next();
                if (key.equals(next.getKey())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value)
    {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (value == null){
            while (i.hasNext()){
                Entry<K, V> e = i.next();
                if (e.getValue() == null ){
                    return true;
                }
            }
        }
        else{
            while (i.hasNext()){
                Entry<K, V> next = i.next();
                if (value.equals(next.getValue())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key)
    {
        Iterator<Entry<K, V>> iterator = entrySet().iterator();
        if (key == null){
            while (iterator.hasNext()){
                Entry<K, V> next = iterator.next();
                if (next.getKey() == null){
                    return next.getValue();
                }
            }
        }
        else{
            while (iterator.hasNext()){
                Entry<K, V> next = iterator.next();
                if (key.equals(next.getKey())){
                    return next.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        Iterator<Entry<K, V>> iterator = entrySet().iterator();
            Entry<K,V> correctEntry = null;
        if (key == null){
            while (correctEntry==null && iterator.hasNext()){
                Entry<K, V> next = iterator.next();
                if (next.getKey() ==null){
                    correctEntry = next;
                }
            }
        }
        else{
            while (correctEntry == null&&iterator.hasNext()){
                Entry<K, V> next = iterator.next();
                if (key.equals(next.getKey())){
                    correctEntry = next;
                }
            }
        }
        V oldValue = null;
        if (correctEntry != null){
            oldValue = correctEntry.getValue();
            iterator.remove();
        }
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K,? extends V> e:m.entrySet()){
            put(e.getKey(),e.getValue());
        }
    }

    @Override
    public void clear() {
        entrySet().clear();
    }

    transient Set<K>        keySet;
    transient Collection<V> values;

    @Override
    public Set<K> keySet() {
       Set<K>  ks =keySet;
       if (ks == null){
           ks = new AbstractSet<K>() {
               public Iterator<K> iterator() {
                   return new Iterator<K>() {
                       private Iterator<Entry<K,V>> i = entrySet().iterator();

                       public boolean hasNext() {
                           return i.hasNext();
                       }

                       public K next() {
                           return i.next().getKey();
                       }

                       public void remove() {
                           i.remove();
                       }
                   };
               }

               public int size() {
                   return CopyAbstractMap.this.size();
               }

               public boolean isEmpty() {
                   return CopyAbstractMap.this.isEmpty();
               }

               public void clear() {
                   CopyAbstractMap.this.clear();
               }

               public boolean contains(Object k) {
                   return CopyAbstractMap.this.containsKey(k);
               }
           };
           keySet = ks;
       }
        return ks;
    }

    @Override
    public Collection<V> values() {
        Collection<V> vals = values;
        if (vals == null){
            vals = new AbstractCollection<V>() {
                @Override
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        private Iterator<Entry<K,V>> i =entrySet().iterator();
                        @Override
                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        @Override
                        public V next() {
                            return i.next().getValue();
                        }
                        public void remove(){
                            i.remove();
                        }
                    };
                }


                @Override
                public int size() {
                    return CopyAbstractMap.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return CopyAbstractMap.this.isEmpty();
                }

                @Override
                public void clear() {
                     CopyAbstractMap.this.clear();
                }
            };
            values = vals;
        }
        return vals;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this){
            return true;
        }
        if (!(o instanceof  Map)){
            return false;
        }
        Map<?,?> m  = (Map<?, ?>) o;
        if (m.size()!= size()){
            return false;
        }
        try{


        Iterator<Entry<K, V>> i = entrySet().iterator();
        while(i.hasNext()) {
            Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            if (value == null) {
                if (!(m.get(key) == null && m.containsKey(key))) {
                    return false;
                }
            } else {
                if (!value.equals(m.get(key))) {
                    return false;
                }
            }
        }
        }catch (ClassCastException e){
            return false;
        }catch (NullPointerException e){
            return false;
        }
        return true;
    }

    public int hashCode(){
        int h = 0;
        Iterator<Entry<K, V>> iterator = entrySet().iterator();
        while (iterator.hasNext()){
            h+=iterator.next().hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        Iterator<Entry<K, V>> iterator = entrySet().iterator();
        if (!iterator.hasNext()){
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for(;;){
            Entry<K, V> next = iterator.next();
            K key = next.getKey();
            V value = next.getValue();
            sb.append(key == this?"(this Map)":key);
            sb.append("=");
            sb.append(value == this ?"(this Map)":value);
            if (!iterator.hasNext()){
                return sb.append("}").toString();
            }
            sb.append(",").append(" ");
        }
    }

    public Object clone() throws CloneNotSupportedException{
        CopyAbstractMap<K,V> clone = (CopyAbstractMap<K, V>) super.clone();
        clone.keySet = null;
        clone.values = null;
        return clone;
    }
    private static boolean eq(Object ob1,Object ob2){
        return ob1==null? ob2 == null:ob1.equals(ob2);
    }
}
