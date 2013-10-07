/*
 * Ashkrit Sharma (https://github.com/ashkrit/blog/blob/master/FastObjectPool/FastObjectPool.java)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.imminentmeals.android.base.utilities;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import sun.misc.Unsafe;


public class FastObjectPool<T> {

    private Holder<T>[] objects;

    private volatile int takePointer;
    private int releasePointer;

    private final int mask;
    private final long BASE;
    private final long INDEXSCALE;
    private final long ASHIFT;

    public ReentrantLock lock = new ReentrantLock();
    private ThreadLocal<Holder<T>> localValue = new ThreadLocal<Holder<T>>();

    @SuppressWarnings("unchecked")
    public FastObjectPool(PoolFactory<T> factory, int size)
    {

        int newSize=1;
        while(newSize<size)
        {
            newSize = newSize << 1;
        }
        size = newSize;
        objects = new Holder[size];
        for(int x=0;x<size;x++)
        {
            objects[x] = new Holder<T>(factory.create());
        }
        mask = size-1;
        releasePointer = size;
        BASE = THE_UNSAFE.arrayBaseOffset(Holder[].class);
        INDEXSCALE = THE_UNSAFE.arrayIndexScale(Holder[].class);
        ASHIFT = 31 - Integer.numberOfLeadingZeros((int) INDEXSCALE);
    }

    public Holder<T> take()
    {
        int localTakePointer;

        Holder<T> localObject = localValue.get();
        if(localObject!=null)
        {
            if(localObject.state.compareAndSet(Holder.FREE, Holder.USED))
            {
                return localObject;
            }
        }

        while(releasePointer != (localTakePointer=takePointer) )
        {
            int index = localTakePointer & mask;
            Holder<T> holder = objects[index];
            if(holder!=null && THE_UNSAFE.compareAndSwapObject(objects, (index<<ASHIFT)+BASE, holder, null))
            {
                takePointer = localTakePointer+1;
                if(holder.state.compareAndSet(Holder.FREE, Holder.USED))
                {
                    localValue.set(holder);
                    return holder;
                }
            }
        }
        return null;
    }

    public void release(Holder<T> object) throws InterruptedException
    {
        lock.lockInterruptibly();
        try{
            int localValue=releasePointer;
            long index = ((localValue & mask)<<ASHIFT ) + BASE;
            if(object.state.compareAndSet(Holder.USED, Holder.FREE))
            {
                THE_UNSAFE.putOrderedObject(objects, index, object);
                releasePointer = localValue+1;
            }
            else
            {
                throw new IllegalArgumentException("Invalid reference passed");
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public static class Holder<T>
    {
        private T value;
        public static final int FREE=0;
        public static final int USED=1;

        private AtomicInteger state = new AtomicInteger(FREE);
        public Holder(T value)
        {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }

    public static interface PoolFactory<T>
    {
        public T create();
    }

    public static final Unsafe THE_UNSAFE;
    static
    {
        try
        {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>()
            {
                public Unsafe run() throws Exception
                {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };

            THE_UNSAFE = AccessController.doPrivileged(action);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }
}