package com.imminentmeals.android.base.utilities;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class FastObjectPoolTest {

    @Before
    public void setUp() throws Exception {
        _pool = new FastObjectPool<Object>(
                new FastObjectPool.PoolFactory<Object>() {

                    public Object create() {
                        return new Object();
                    }
                }, 4);

    }

    @Test
    public void testPrecondition() throws InterruptedException {
        assertThat(_pool.take().getValue()).isNotEqualTo(new Object());
    }

    @Test
    public void testObjectReusedWhenAvailable() throws InterruptedException {
        final FastObjectPool.Holder<Object> holder = _pool.take();
        final Object reuseable_object = holder.getValue();
        _pool.release(holder);
        assertThat(_pool.take().getValue()).isEqualTo(reuseable_object);
    }

    @Test
    public void testObjectCreatedAsNeeded() throws InterruptedException {
        final FastObjectPool.Holder<Object> first_holder = _pool.take();
        final FastObjectPool.Holder<Object> second_holder = _pool.take();
        assertThat(first_holder.getValue()).isNotEqualTo(second_holder.getValue());
        final Object second_object = second_holder.getValue();
        _pool.release(second_holder);
        assertThat(_pool.take().getValue()).isEqualTo(second_object);
    }

    private FastObjectPool<Object> _pool;
}
