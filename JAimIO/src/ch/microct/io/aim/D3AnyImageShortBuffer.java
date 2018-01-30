package ch.microct.io.aim;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel.MapMode;






public class D3AnyImageShortBuffer extends D3AnyImageBuffer {

    public D3AnyImageShortBuffer(long size, int mapSize) throws IOException {
    	super(D1Type.D1Tshort, size, mapSize);
    }
    
 	public float getVoxel(long i) {
		return (float)getShort(i);
	}
	
	public void setVoxel(long i, float v) {
		setShort(i, (short)v);
	}
	
    public short getShort(long p) {
        int idx = getBufferIndex(p);
        int off = getBufferOffset(p);
        return (short) getBuffer(idx).get(off);	
    }
    
     
    public void setShort(long p, short v) {
        int idx = getBufferIndex(p);
        int off = getBufferOffset(p);
        getBuffer(idx).put(off, v);
    	setMinMax((float)v);
    }
   
	@Override
	protected ShortBuffer allocateBuffer(long size) throws IOException {
		return allocateShortBuffer(size);
	}

	@Override
	protected ShortBuffer allocateMappedBuffer(RandomAccessFile mraf, MapMode readWrite, long offset, long size) throws IOException {
		return allocateMappedShortBuffer(mraf, readWrite, offset, size);
	}

	@Override
	public ShortBuffer getBuffer(int idx) {
		return (ShortBuffer)super.getBuffer(idx);
	}
	@Override
	public short[] getBufferArray(int idx) {
		ShortBuffer buf = getBuffer(idx);
		if (buf.hasArray()) {
			return buf.array();			
		} else {
			short[] arr = new short[buf.capacity()];
			buf.get(arr);
			return arr;
		}
	}



}
 
