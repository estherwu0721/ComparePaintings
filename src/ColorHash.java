import java.io.PrintStream;

public class ColorHash {
    HashEntry[] theTable;
    int tableSize;
    int bitsPerPixel;
    String collisionResolutionMethod;
    boolean useQuadraticProbing;
    double rehashLoadFactor;
    int collisionCount;
    int resizeCollisionCount;
    int n;
    double loadFactor;
    boolean incrementing;
    boolean debugging;
    HashEntry foundEntry;

    public ColorHash(int tableSize, int bitsPerPixel, String collisionResolutionMethod, double rehashLoadFactor) {
        this.tableSize = tableSize;
        this.bitsPerPixel = bitsPerPixel;
        this.collisionResolutionMethod = collisionResolutionMethod;
        this.useQuadraticProbing = collisionResolutionMethod.equals("Quadratic Probing");
        this.rehashLoadFactor = rehashLoadFactor;
        this.theTable = new HashEntry[tableSize];
        this.n = 0;
        this.loadFactor = 0.0;
        this.debugging = false;
    }

    public ResponseItem colorHashPut(ColorKey key, long value) {
        int putCollisionCount = 0;
        boolean resized = false;
        if (((double)this.n + 1.0) / (double)this.tableSize > this.rehashLoadFactor) {
            int location = this.findLocation(this, key);
            putCollisionCount += this.collisionCount;
            if (this.foundEntry == null) {
                if (this.debugging) {
                    System.out.println("Before resize, number of collisions from findLocation: " + this.collisionCount);
                }
                this.resize();
                resized = true;
                if (this.debugging) {
                    System.out.println("During resize, number of collisions: " + this.resizeCollisionCount);
                }
                if (this.debugging) {
                    System.out.println("Resized table prior to insertion:");
                    System.out.println(this.showWholeTable());
                }
                putCollisionCount += this.resizeCollisionCount;
            } else {
                this.foundEntry.value = this.incrementing ? ++this.foundEntry.value : value;
                return new ResponseItem(-1, this.collisionCount, false, true);
            }
        }
        ResponseItem ri = this.helpPut(this, key, value);
        ri.didRehash = resized;
        if (this.debugging) {
            System.out.println("Number of collisions from helpPut: " + ri.nCollisions);
        }
        ri.nCollisions += putCollisionCount;
        return ri;
    }

    public ResponseItem increment(ColorKey key) {
        this.incrementing = true;
        ResponseItem ri = this.colorHashPut(key, 1);
        this.incrementing = false;
        return ri;
    }

    public ResponseItem colorHashGet(ColorKey key) throws Exception {
        return this.helpGet(this, key);
    }

    public long getCount(ColorKey key) {
        try {
            ResponseItem ri = this.helpGet(this, key);
            return ri.value;
        }
        catch (Exception e) {
            return 0;
        }
    }

    public ColorKey getKeyAt(int tableIndex) {
        HashEntry e = this.theTable[tableIndex];
        if (e == null) {
            return null;
        }
        return e.key;
    }

    public long getValueAt(int tableIndex) {
        HashEntry e = this.theTable[tableIndex];
        if (e == null) {
            return -1;
        }
        return e.value;
    }

    public double getLoadFactor() {
        return this.loadFactor;
    }

    public int getTableSize() {
        return this.tableSize;
    }

    public void resize() {
        int newTableSize = 2 * this.tableSize;
        while (!IsPrime.isPrime((long)newTableSize)) {
            ++newTableSize;
        }
        ColorHash newHash = new ColorHash(newTableSize, this.bitsPerPixel, this.collisionResolutionMethod, this.rehashLoadFactor);
        this.resizeCollisionCount = 0;
        int i = 0;
        while (i < this.tableSize) {
            HashEntry e = this.theTable[i];
            if (e != null) {
                ResponseItem ri = this.helpPut(newHash, e.key, e.value);
                this.resizeCollisionCount += ri.nCollisions;
            }
            ++i;
        }
        this.theTable = newHash.theTable;
        this.tableSize = newHash.tableSize;
        this.loadFactor = (double)this.n / (double)this.tableSize;
    }

    private int findLocation(ColorHash whichHashTable, ColorKey key) {
        if (this.debugging) {
            System.out.println("Entering findLocation with key=" + (Object)key);
        }
        int hashcode = key.hashCode();
        this.collisionCount = 0;
        boolean found = false;
        int probeLocation = -1;
        int i = 0;
        while (!found) {
            int offset = i;
            if (this.useQuadraticProbing) {
                offset = i * i;
            }
            probeLocation = (hashcode + offset) % whichHashTable.tableSize;
            HashEntry e = whichHashTable.theTable[probeLocation];
            if (this.debugging) {
                System.out.println("probeLocation = " + probeLocation);
            }
            if (e == null || e.key.equals(key)) {
                found = true;
                this.foundEntry = e;
                break;
            }
            ++i;
            ++this.collisionCount;
        }
        return probeLocation;
    }

    private ResponseItem helpPut(ColorHash whichHashTable, ColorKey key, long value) {
        if (this.debugging) {
            System.out.println("Entering helpPut with key=" + (Object)key);
        }
        int location = this.findLocation(whichHashTable, key);
        boolean isUpdate = false;
        if (this.foundEntry != null) {
            isUpdate = true;
        } else {
            ++this.n;
            this.loadFactor = (double)this.n / (double)this.tableSize;
        }
        if (this.incrementing && isUpdate) {
            value = this.foundEntry.value + 1;
        }
        whichHashTable.theTable[location] = new HashEntry(key, value, false);
        return new ResponseItem(-1, this.collisionCount, false, isUpdate);
    }

    private ResponseItem helpGet(ColorHash whichHashTable, ColorKey key) throws Exception {
        int location = this.findLocation(whichHashTable, key);
        if (location == -1 || this.foundEntry == null) {
            throw new Exception("ColorKey not found during ColorHash get operation.");
        }
        return new ResponseItem(this.foundEntry.value, this.collisionCount, false, false);
    }

    public String showWholeTable() {
        String result = "ColorHash: tableSize=" + this.tableSize + "\n";
        int i = 0;
        while (i < this.tableSize) {
            HashEntry e = this.theTable[i];
            result = String.valueOf(result) + i + ": ";
            result = e == null ? String.valueOf(result) + " ... \n" : String.valueOf(result) + (Object)e.key + "," + e.value + "\n";
            ++i;
        }
        return result;
    }

    public void setDebugging(boolean on) {
        this.debugging = on;
    }

    class HashEntry {
        ColorKey key;
        long value;
        boolean deleted;

        HashEntry(ColorKey key, long value, boolean deleted) {
            this.key = key;
            this.value = value;
            this.deleted = deleted;
        }
    }

}