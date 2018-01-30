package ch.microct.io.aim;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;






public class D3AnyImageByteBuffer extends D3AnyImageBuffer {

 
    public D3AnyImageByteBuffer(long size, int mapSize) throws IOException {
    	super(D1Type.D1Tchar, size, mapSize);
    }
    
	public float getVoxel(long i) {
		return (float)getByte(i);
	}
	
	public void setVoxel(long i, float v) {
		setByte(i, (byte)v);
	}
	
	public byte getByte(long p) {
		int idx = getBufferIndex(p);
        int off = getBufferOffset(p);
        return (byte) getBuffer(idx).get(off);	
    }
    
    public void setByte(long p, byte v) {
		int idx = getBufferIndex(p);
        int off = getBufferOffset(p);
        getBuffer(idx).put(off, v);
    	setMinMax((float)v);
    }
    
     
 	@Override
	protected ByteBuffer allocateBuffer(long size) throws IOException {
		return allocateByteBuffer(size);
	}

	@Override
	protected ByteBuffer allocateMappedBuffer(RandomAccessFile mraf, MapMode readWrite, long offset, long size) throws IOException {
		return allocateMappedByteBuffer(mraf, readWrite, offset, size);
	}

	public ByteBuffer getBuffer(int idx) {
		return (ByteBuffer)super.getBuffer(idx);
	}

	@Override
	public byte[] getBufferArray(int idx) {
		ByteBuffer buf = getBuffer(idx);
		if (buf.hasArray()) {
			return buf.array();			
		} else {
			byte[] arr = new byte[buf.capacity()];
			buf.get(arr);
			return arr;
		}
	}


}
 
