package com.sixrr.ejbmetrics;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class BuckettedCount<T> {
    private final Map<T, Integer> buckets = new HashMap<T, Integer>();

    public void createBucket(T bucket) {
        if (!buckets.containsKey(bucket)) {
            buckets.put(bucket, 0);
        }
    }

    public Set<T> getBuckets() {
        return buckets.keySet();
    }

    public void incrementBucketValue(T bucket, int increment) {
        if (!buckets.containsKey(bucket)) {
            buckets.put(bucket, increment);
        }
        buckets.put(bucket, buckets.get(bucket) + increment);
    }

    public void incrementBucketValue(T bucket) {
        incrementBucketValue(bucket, 1);
    }

    public int getBucketValue(T bucket) {
        return buckets.get(bucket);
    }

    public void clear() {
        buckets.clear();
    }
}
