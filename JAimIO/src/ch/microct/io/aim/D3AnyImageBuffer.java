package ch.microct.io.aim;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

import ch.microct.util.LogUtil;





public abstract class D3AnyImageBuffer {
	private static final int MAPPING_SIZE = 1 << 30;
	protected final Object[] bufList;
	
    /* 
     * the size of the data type stored in the buffer 
     * -> 1 for byte
     * -> 2 for short
     * -> 4 for int
     * -> 4 for float
     * -> 8 for long
     * -> 8 for double
     */
    protected final int ptrSize;
    
    /* 
     * the total size of all allocated buffers in this D3AnyImageBuffer in bytes
     */
    protected final long bufSize;
    
    /* 
     * the total size of all allocated buffers in this D3AnyImageBuffer as elements for the given datatype
     */
    protected final long bufLength;
    
    /* 
     * the max size of a single allocated buffer in bytes
     */
	protected final int mapSize;
    
    /* 
     * the max size of a single allocated buffer as elements for the given datatype
     */
	protected final int mapLength;
	
	private float dataMin = Float.MAX_VALUE;
	private float dataMax = Float.MIN_VALUE;
	private float typeMin = Float.MAX_VALUE;
	private float typeMax = Float.MIN_VALUE;

    private static LogUtil logger = LogUtil.getLogger(D3AnyImageBuffer.class);
    
	protected D3AnyImageBuffer(int type, long size) throws IOException {
    	this(type, size, MAPPING_SIZE);
    }
    
    protected D3AnyImageBuffer(int type, long size, int mapSize) throws IOException {
       	this.ptrSize = D1Type.sizeOfType(type);
    	this.bufSize = size;
     	this.mapSize = mapSize;
    	this.bufLength = bufSize/ptrSize;
    	this.mapLength = mapSize/ptrSize;
    	int bufs = (int)(bufLength/mapLength) + 1;
    	this.bufList = new Object[bufs];
    	this.typeMin = D1Type.min(type);
    	this.typeMax = D1Type.max(type);
       	logger.sayDeb("Total Buffer Size= "+ bufSize + "Bytes - Total Buffer Elements= " + bufLength);
       	logger.sayDeb("Buffer Type="+ D1Type.nameOfType(type) + "Size of Type = "+ ptrSize);
       	logger.sayDeb("Mapped Buffer Size= "+ mapSize + "Bytes - Mapped Buffer Elements= " + mapLength);
       	logger.sayDeb("Mapped Buffer List= "+ bufs);
    }
    
    public static D3AnyImageBuffer allocate(int typeId, long bufSize, int mapSize, String mapFile) throws IOException {
		D3AnyImageBuffer data;
		switch(typeId) {
		case D1Type.D1Tchar:
		case D1Type.D1TcharCmp:
		case D1Type.D1TbinCmp:
			data = new D3AnyImageByteBuffer(bufSize, mapSize);
			break;
		case D1Type.D1Tshort:
			data = new D3AnyImageShortBuffer(bufSize, mapSize);
			break;
		case D1Type.D1Tint:
			data = null;
			break;
		case D1Type.D1Tfloat:
			data = null;
			break;
		default:
			data = null;	
		}
		
		if (data != null) {
			if (mapFile != null) {
				data.allocate(mapFile);
			} else {
				data.allocate();				
			}
		}
		return data;
	}

	public void allocate(String mapFile) throws IOException {
        RandomAccessFile mraf = new RandomAccessFile(mapFile, "rw");
        int cnt=0;
        for (long offset =0; offset < (bufSize); offset += mapSize) {
             long size2 = Math.min(bufSize - offset, mapSize);
             bufList[cnt]=allocateMappedBuffer(mraf, MapMode.READ_WRITE, offset, size2);
          	 System.out.println("Allocated File Buffer[" +cnt++ + "]: "+ (float)size2/1024f/1024f + " MB.");
         }
 	}

 	public void allocate() throws IOException {
        int cnt=0;
        for (long offset = 0; offset < (bufSize); offset += mapSize) {
             long size2 = Math.min(bufSize - offset, mapSize);
 			 bufList[cnt]=allocateBuffer(size2);
          	 System.out.println("Allocated Memory Buffer[" + cnt++ + "]: "+ (float)size2/1024f/1024f + " MB.");
        }
 	}
    
    protected abstract Object allocateMappedBuffer(RandomAccessFile mraf, MapMode readWrite, long offset, long size) throws IOException;
    protected abstract Object allocateBuffer(long size) throws IOException;
    
	public abstract float getVoxel(long i);	
	public abstract void setVoxel(long i, float v);
	
	public abstract Object getBufferArray(int idx);	
	
    protected int getBufferIndex(long p) {
    	if (!inBounds(p)) {
    		throw new ArrayIndexOutOfBoundsException((int) p);
    	}
        return (int) (p / mapLength);
    }
    
	protected int getBufferOffset(long p) {
    	if (!inBounds(p)) {
    		throw new ArrayIndexOutOfBoundsException((int) p);
    	}
        return (int) (p % mapLength);
    }
    
	protected Object getBuffer(int idx) {
			return bufList[idx];
	}

	protected void setMinMax(float v) {
		dataMin = Math.min(dataMin, v);
		dataMax = Math.max(dataMax, v);
	}

	public void free() throws IOException {
		if (bufList != null && bufList.length != 0) {
			for (int i=0; i<bufList.length; i++) {
				Buffer b = (Buffer)bufList[i];
				b = null;
			}
		}
    }
 
	private boolean inBounds(long p) {
		return  (p >= 0) && (p < bufLength);
	}

	public float getDataMin(){return dataMin;};
	public float getDataMax(){return dataMax;};
	public float getTypeMin(){return typeMin;};
	public float getTypeMax(){return typeMax;};

	public static ByteBuffer allocateByteBuffer(long size) throws IOException {
		byte[] arr = new byte[(int) size];
		ByteBuffer bb = ByteBuffer.wrap(arr);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb;
	}

	public static ShortBuffer allocateShortBuffer(long size) throws IOException {
		short[] arr = new short[(int) (size/2)];
		ShortBuffer bb = ShortBuffer.wrap(arr);
		//bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb;
	}

	public static ByteBuffer allocateMappedByteBuffer(RandomAccessFile raf, MapMode mode, long start, long size) throws IOException {
		FileChannel channel = raf.getChannel();
		MappedByteBuffer mbb = channel.map(mode, start, size);
		mbb.order(ByteOrder.LITTLE_ENDIAN);
		return mbb;
	}
	public static ShortBuffer allocateMappedShortBuffer(RandomAccessFile raf, MapMode mode, long start, long size) throws IOException {
		FileChannel channel = raf.getChannel();
		MappedByteBuffer mbb = channel.map(mode, start, size);
		mbb.order(ByteOrder.LITTLE_ENDIAN);
		return mbb.asShortBuffer();
	}
	
	protected ByteBuffer getByteBuffer(int idx) {
		return (ByteBuffer) getBuffer(idx);
	}



}
 
