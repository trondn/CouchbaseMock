/**
 *     Copyright 2011 Membase, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.couchbase.mock.memcached;

import java.util.Date;

/**
 * @author Trond Norbye
 */
public class Item {
    private final KeySpec keySpec;
    private final int flags;
    private int expiryTime;
    private byte[] value;
    private long cas;
    private long modificationTime;

    /** When the lock expires, if any */
    private int lockExpiryTime;

    public Item(KeySpec ks, int flags, int expiryTime, byte[] value, long cas) {
        this.keySpec = ks;
        this.flags = flags;
        this.value = value;
        this.cas = cas;
        this.expiryTime = VBucketStore.convertExpiryTime(expiryTime);
    }

    public Item(KeySpec ks) {
        this.keySpec = ks;
        this.flags = -1;
        this.expiryTime = -1;
        this.value = null;
        this.cas = -1;
        this.modificationTime = -1;
    }

    /**
     * Copy constructor
     * @param src Item whose contents we should copy
     */
    public Item(Item src) {
        this.keySpec = src.keySpec;
        this.flags = src.flags;
        this.expiryTime = src.expiryTime;
        this.value = src.value;
        this.cas = src.cas;
        this.modificationTime = src.modificationTime;
        this.lockExpiryTime = src.lockExpiryTime;
    }

    public int getExpiryTime() {
        return expiryTime;
    }

    public long getExpiryTimeInMillis() {
        return (long) expiryTime * 1000L;
    }

    public void setExpiryTime(int e) {
        expiryTime = VBucketStore.convertExpiryTime(e);
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public int getFlags() {
        return flags;
    }

    public KeySpec getKeySpec() {
        return this.keySpec;
    }

    public byte[] getValue() {
        return value;
    }

    public long getCas() {
        return cas;
    }

    void setCas(long l) {
        modificationTime = new Date().getTime();
        cas = l;
    }

    void setLockExpiryTime(int e) {
        lockExpiryTime = VBucketStore.convertExpiryTime(e);
    }

    int getLockExpiryTime() {
        return lockExpiryTime;
    }

    public boolean isLocked() {
        if (lockExpiryTime == 0) {
            return false;
        }

        long now = new Date().getTime() / 1000;
        return now <= lockExpiryTime;
    }

    /**
     * Given a cas, ensure that the item is unlocked.
     * Will succeed if cas matches the existing cas, or if the item is not
     * locked
     * @param cas the cas value used to perform the operation
     * @return true if item is *not* locked.
     */
    public boolean ensureUnlocked(long cas)
    {
        if (cas == this.cas) {
            lockExpiryTime = 0;
            return true;
        } else {
            return !isLocked();
        }
    }

    public void append(Item i) {
        byte[] s1 = value;
        byte[] s2 = i.getValue();
        byte[] dst = new byte[s1.length + s2.length];

        System.arraycopy(s1, 0, dst, 0, s1.length);
        System.arraycopy(s2, 0, dst, s1.length, s2.length);
        value = dst;
    }

    public void prepend(Item i) {
        byte[] s1 = value;
        byte[] s2 = i.getValue();
        byte[] dst = new byte[s1.length + s2.length];

        System.arraycopy(s2, 0, dst, 0, s2.length);
        System.arraycopy(s1, 0, dst, s2.length, s1.length);
        value = dst;
    }

}
