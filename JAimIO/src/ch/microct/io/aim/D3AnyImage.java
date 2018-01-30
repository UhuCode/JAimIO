/**
 * 
 */
package ch.microct.io.aim;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;

import ch.microct.util.LogUtil;














/**
 * @author kohler
 *
 */
public class D3AnyImage {
	
	private DecimalFormat df = new DecimalFormat("0.0000");
	
	
	private LogUtil logger = LogUtil.getLogger(getClass());
	
	
	
	//private D3AnyMeta meta;
	//private D3AnyHead head;
	//private D3AnyProc proc;
	//private D3AnyData data;
	//private D3AnyProcessor ctrl;
	
	private String 		filename;
	//private D3AnyImageDecoder dec;
	private D3AnyImageInfo	info;
	private D3AnyImageBuffer data;








//
//	
	public D3AnyImage() {
		this.filename = "";
		//this.dec = null;
		this.info = null;
		this.data = null;
	}
	
	public D3AnyImage(String filename) {
		this.filename = filename;
		//this.dec = new D3AnyImageDecoder(filename);
		this.info = null;
		this.data = null;
	}
	
	public D3AnyImage(String filename, int type) {
		this.filename = filename;
		this.info = new D3AnyImageInfo(type);
		this.data = null;
	}
	
	public static void main(String[] args) {
		try {
			String grayscale = "C0011099.AIM";
			@SuppressWarnings("unused")
			String segmented = "C0000258_SEG.AIM";
			String segmented2 = "C0000132_SEG_test_sub.AIM";
			//@SuppressWarnings("unused")
			D3AnyImage aim = D3AnyImageIO.readImage(grayscale, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public D3AnyImage(D3AnyImage aim) {
//		this.vol = aim.getVolume();
//		this.filename = aim.getFilename();
//		this.dec = aim.getDecoder();
//	}
//
	public void write(String file) throws FileNotFoundException, IOException {
		//writeBlocks(file);
	}
	
	public D3AnyImageInfo getInfo() {
		return info;
	}
	
	public void setInfo(D3AnyImageInfo info) {
		this.info = info;
	}

	public D3AnyImageBuffer getData() {
		return data;
	}
	
	public void setData(D3AnyImageBuffer data) {
		this.data = data;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}


//	public static void initBlocks(D3AnyImage aim, boolean onlyHead) throws FileNotFoundException, IOException {
//		long blockOffset;
//		long metaLength, headLength, procLength, dataLength, itemLength;
//		
//		RandomAccessFile raf = new RandomAccessFile(aim.filename, "r");
//		FileChannel c = raf.getChannel();
//		
//		ByteBuffer buf = ByteBuffer.allocate(512);
//		buf.order(ByteOrder.LITTLE_ENDIAN);
//		
//		int nr = c.read(buf);
//		buf.flip();
//		
//		buf.position(0);
//		buf.get(B_VERSION_AIM);
//		System.out.println("Version String:\t" + new String(B_VERSION_AIM));
//		
//		ByteBuffer vbuf = ByteBuffer.wrap(B_VERSION_AIM);
//		System.out.print("64bit Version:\t");
//		int v = vbuf.compareTo(ByteBuffer.wrap(B_VERSION_030));
//		System.out.println((v == 0) ? "true" : "false");
//		
//		buf.clear();	
//		if (v==0) {
//			blockOffset = CODE_FIELD_SIZE_030;
//			buf.position((int) blockOffset);
//			LongBuffer lbuf = buf.asLongBuffer();
//			metaLength = lbuf.get();
//			System.out.println("Meta Offset:\t" + blockOffset);
//			System.out.println("Meta Length:\t" + metaLength);
//			int nrBlocks = (int) (metaLength / META_FIELD_SIZE_030);
//			System.out.println("Meta Blocks:\t" + nrBlocks);
//			long[] blockSizes = new long[nrBlocks-1];
//			lbuf.get(blockSizes);
//			headLength = blockSizes[0];
//			procLength = blockSizes[1];
//			dataLength = blockSizes[2];
//			if (blockSizes.length > 3) {
//				itemLength = blockSizes[3];
//			} else {
//				itemLength = 0;
//			}
//			aim.is030 = true;
//		} else {
//			blockOffset = CODE_FIELD_SIZE_020;
//			buf.position((int) blockOffset);
//			IntBuffer ibuf = buf.asIntBuffer();
//			metaLength = ibuf.get();
//			System.out.println("Meta Offset:\t" + blockOffset);
//			System.out.println("Meta Length:\t" + metaLength);
//			int nrBlocks = (int) (metaLength / META_FIELD_SIZE_020);
//			System.out.println("Meta Blocks:\t" + nrBlocks);
//			int[] blockSizes = new int[nrBlocks-1];
//			ibuf.get(blockSizes);
//			headLength = blockSizes[0];
//			procLength = blockSizes[1];
//			dataLength = blockSizes[2];
//			if (blockSizes.length > 3) {
//				itemLength = blockSizes[3];
//			} else {
//				itemLength = 0;
//			}
//			aim.is030 = false;
//		}
//
//		
//		ArrayList<D3AnyBlock> blocks = new ArrayList<D3AnyBlock>();
//		
//		blockOffset += metaLength;
//		D3AnyBlock headBlock = D3AnyBlock.initDirect(c, (int)blockOffset, (int)headLength);
//		blocks.add(headBlock);
//		System.out.println("Head Offset:\t" + blockOffset);
//		System.out.println("Head Length:\t" + headBlock.size());
//		
//		blockOffset += headLength;
//		D3AnyBlock procBlock = D3AnyBlock.initDirect(c, (int)blockOffset, (int)procLength);
//		blocks.add(procBlock);
//		System.out.println("Proc Offset:\t" + blockOffset);
//		System.out.println("Proc Length:\t" + procBlock.size());
//		
//		blockOffset += procLength;
//		System.out.println("Data Offset:\t" + blockOffset);
//		aim.offset = (int) (blockOffset);
//		
//		if (!onlyHead) {
//			D3AnyBlock dataBlock = D3AnyBlock.initMapped(c, (int)blockOffset, (int)dataLength);
//			blocks.add(dataBlock);
//			System.out.println("Data Length:\t" + dataBlock.size());
//
//			blockOffset += dataLength;
//			if (itemLength > 0) {
//				D3AnyBlock itemBlock = D3AnyBlock.initMapped(c, (int)blockOffset, (int)itemLength);
//				blocks.add(itemBlock);
//				System.out.println("Item Offset:\t" + blockOffset);
//				System.out.println("Item Length:\t" + itemBlock.size());
//			} else {
//				D3AnyBlock itemBlock = null;
//			}
//		}
//		
//		ByteBuffer hbuf = blocks.get(0).data();
//		ByteBuffer pbuf = blocks.get(1).data();
//		
//		aim.head = D3AnyHead.create(hbuf, aim);
//		aim.proc = D3AnyProc.create(pbuf);
//		
//		if (!onlyHead) {
//			ByteBuffer dbuf = blocks.get(2).data();		
//			aim.data = D3AnyData.create(aim.head, dbuf);
//		}
//		
//		//c.close();
//
//	}
//
	//public static ByteBuffer initData(D3AnyImage aim) throws FileNotFoundException, IOException {
		
	//	RandomAccessFile raf = new RandomAccessFile(aim.filename, "r");
	//	FileChannel c = raf.getChannel();
		
	//	System.out.println("Data Offset:\t" + aim.getOffset());
		//D3AnyBlock dataBlock = D3AnyBlock.initMapped(c, (int)aim.getOffset(), (int)aim.getHead().getDataSize());
		//System.out.println("Data Length:\t" + dataBlock.size());

	    //return dataBlock.data();		

	//}

//	public int center(PLANE plane) {
//		return data.center(plane);
//	}
//	
//	public int depth(PLANE plane) {
//		return data.depth(plane);
//	}
//	
//	public int height(PLANE plane) {
//		return data.height(plane);
//	}
//	
//	public int width(PLANE plane) {
//		return data.width(plane);
//	}
//
	public void free() {
		// TODO Auto-generated method stub
		
	}

	public float getVoxel(long p) {
		return data.getVoxel(p);
	}

	public void setVoxel(long p, float val) {
		data.setVoxel(p, val);
	}
	
	public void setType(int type) {
		info.setType(type);
	}

	public int getType() {
		return info.getType();
	}

	public String getNameOfType() {
		return D1Type.nameOfType(getType());
	}

	public int getSizeOfType() {
		return D1Type.sizeOfType(getType());
	}
	
	public D3int64 getDim() {
		return info.getDim();
	}

	public void setDim(D3int64 dim) {
		info.setDim(dim);
	}

	public D3float getEl_size_mm() {
		return info.getEl_size_mm();
	}

	public void setEl_size_mm(D3float el_size_mm) {
		info.setEl_size_mm(el_size_mm);
	}

	public D3int64 getOff() {
		return info.getOff();
	}

	public void setOff(D3int64 off) {
		info.setOff(off);
	}

	public D3int64 getPos() {
		return info.getOff();
	}

	public void setPos(D3int64 pos) {
		info.setPos(pos);
	}

	public void setSubdim(D3int64 d3int64) {
		info.setSubdim(d3int64);
		
	}

	public void setSuppos(D3int64 d3int64) {
		info.setSuppos(d3int64);
		
	}

	public void setSupdim(D3int64 d3int64) {
		info.setSupdim(d3int64);
		
	}

	public void setTestoff(D3int64 d3int64) {
		info.setTestoff(d3int64);
		
	}

	public long dimx() {
		return info.getDim().x;
	}

	public long dimy() {
		return info.getDim().y;
	}

	public long dimz() {
		return info.getDim().z;
	}

	public long voxels() {
		return info.getDim().size();
	}

	public float getDataMin() {
		return data.getDataMin();
	}

	public float getDataMax() {
		return data.getDataMax();
	}

	public float getTypeMin() {
		return data.getTypeMin();
	}

	public float getTypeMax() {
		return data.getTypeMax();
	}


 
}
