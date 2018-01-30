package ch.microct.io.aim;


import ij.IJ;
import ij.io.FileInfo;
import ij.measure.Calibration;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;



public class D3AnyImageDecoder {
	
	public static final int HEAD_INT_FIELDS_020 = 6;
	public static final int HEAD_GEO_FIELDS_020 = 7;
	public static final int HEAD_RES_FIELDS_020 = 1;
	public static final int HEAD_ITM_FIELDS_020 = 5;
	
	public static final int HEAD_INT_FIELDS_030 = 4;
	public static final int HEAD_GEO_FIELDS_030 = 8;
	public static final int HEAD_ITM_FIELDS_030 = 4;
	
	private static final String S_VERSION_030 = "AIMDATA_V030   ";
	private static final byte[] B_VERSION_030 = S_VERSION_030.getBytes();
	private static byte[] B_VERSION_AIM = new byte[B_VERSION_030.length];
	
	public static final int CODE_FIELD_SIZE_030 = 16;
	public static final int CODE_FIELD_SIZE_020 = 0;
	public static final int META_FIELD_SIZE_030 = 8;
	public static final int META_FIELD_SIZE_020 = 4;
	
	
	private String filename;
	private RandomAccessFile raf;
	private boolean is030;
	
	private long metaOff;
	private long metaLen;
	private long headOff;
	private long headLen;
	private long procOff;
	private long procLen;
	private long dataOff;
	private long dataLen;
	private long itemOff;
	private long itemLen;
	
	private boolean debug = true;
	
	private D3AnyImageInfo	info;		
	private String directory;
	private String name;
	private String url;
	protected boolean debugMode;
	private boolean littleEndian;
	private String dInfo;
	private int ifdCount;
	private int[] metaDataCounts;
	private String tiffMetadata;
	private int photoInterp;
	private boolean toUnsignedShort = false;
	private boolean zeroClip = true;
	private boolean doScale = false;
		
	public D3AnyImageDecoder(String aimfile)  {
		filename = aimfile;
		try {
			raf = new RandomAccessFile(filename, "r");
			init();
			info = readInfo();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public String toString() {
		String meta = 
		"Meta:\t" + metaOff + "\t" + metaLen + "\n" +
		"Head:\t" + headOff + "\t" + headLen + "\n" +
		"Proc:\t" + procOff + "\t" + procLen + "\n" +
		"Data:\t" + dataOff + "\t" + dataLen + "\n" +			
		"Item:\t" + itemOff + "\t" + itemLen + "\n";	
		return meta;
	}

	private boolean init() throws IOException {
		ByteBuffer buf = map(raf, MapMode.READ_ONLY, 0, 1024);
		buf.position(0);
		buf.get(B_VERSION_AIM);
		if (debug)
			System.out.println("Version String:\t" + new String(B_VERSION_AIM));
	
		ByteBuffer vbuf = ByteBuffer.wrap(B_VERSION_AIM);
		ByteBuffer v030 = ByteBuffer.wrap(B_VERSION_030);
		if (debug)
			System.out.print("64bit Version:\t");
		int v = vbuf.compareTo(v030);
		if (debug)
			System.out.println((v == 0) ? "true" : "false");
	
		buf.clear();	
		if (v==0) {
			metaOff = CODE_FIELD_SIZE_030;
			buf.position((int) metaOff);
			metaLen = readLong(buf);
			headLen = readLong(buf);
			procLen = readLong(buf);
			dataLen = readLong(buf);
			itemLen = readLong(buf);
			is030 = true;
		} else {
			metaOff = CODE_FIELD_SIZE_020;
			buf.position((int) metaOff);
			metaLen = readInt(buf);
			headLen = readInt(buf);
			procLen = readInt(buf);
			dataLen = readInt(buf);
			itemLen = readInt(buf);
			is030 = false;
		}
	
		headOff = metaOff + metaLen;
		procOff = headOff + headLen;
		dataOff = procOff + procLen;
		itemOff = dataOff + dataLen;
		
		if (debug)
			System.out.println(toString());
	
		buf.clear();
		buf = null;
		return true;
	}
	
	public void setConvertUnsignedShort(boolean toUnsigned, boolean zeroClip, boolean doScale) {
		toUnsignedShort = toUnsigned;
		this.zeroClip = zeroClip;
		this.doScale = doScale;
	}

//	public FileInfo getAimInfo() throws IOException {
//		long ifdOffset;
//		if (in==null)
//			in = new RandomAccessStream(new RandomAccessFile(new File(directory, name), "r"));
//		ifdOffset = openFileHeader();
//		if (ifdOffset<0L) {
//			in.close();
//			return null;
//		}
//		return null;
//	}
//	
//	private int openFileHeader() throws IOException {
//		ByteBuffer buf = map(raf, MapMode.READ_ONLY, 0, 1024);
//		buf.position(0);
//		buf.get(B_VERSION_AIM);
//		if (debug)
//			System.out.println("Version String:\t" + new String(B_VERSION_AIM));
//	
//		ByteBuffer vbuf = ByteBuffer.wrap(B_VERSION_AIM);
//		ByteBuffer v030 = ByteBuffer.wrap(B_VERSION_030);
//		if (debug)
//			System.out.print("64bit Version:\t");
//		int v = vbuf.compareTo(v030);
//		if (debug)
//			System.out.println((v == 0) ? "true" : "false");
//	
//		buf.clear();	
//		if (v==0) {
//			metaOff = CODE_FIELD_SIZE_030;
//			buf.position((int) metaOff);
//			metaLen = readLong(buf);
//			headLen = readLong(buf);
//			procLen = readLong(buf);
//			dataLen = readLong(buf);
//			itemLen = readLong(buf);
//			is030 = true;
//		} else {
//			metaOff = CODE_FIELD_SIZE_020;
//			buf.position((int) metaOff);
//			metaLen = readInt(buf);
//			headLen = readInt(buf);
//			procLen = readInt(buf);
//			dataLen = readInt(buf);
//			itemLen = readInt(buf);
//			is030 = false;
//		}
//	
//		headOff = metaOff + metaLen;
//		procOff = headOff + headLen;
//		dataOff = procOff + procLen;
//		itemOff = dataOff + dataLen;
//		
//		if (debug)
//			System.out.println(toString());
//	
//		buf.clear();
//		buf = null;
//		return (int) dataOff;
//	}

	public D3AnyImageInfo readInfo() throws IOException {
		if (info != null) {
			return info;
		}
		//info = new D3AnyImageInfo(filename);
		info = new D3AnyImageInfo();
		ByteBuffer hb = map(raf, MapMode.READ_ONLY, headOff, headLen);
		if (is030) {
			readHeadInfo030(hb, info);
		} else {
			readHeadInfo020(hb, info);
		}
		ByteBuffer pb = map(raf, MapMode.READ_ONLY, procOff, procLen);
		readProcInfo(pb, info);
		if (debug)
			System.out.println(info.toString());
		hb = null;
		pb = null;
		System.out.println("Type: "+info.getType()+ " Name: " + info.getDataType());
		
		return info;
	}

	public void readInfo(D3AnyImage aim) throws IOException {
		aim.setInfo(readInfo());
	}

	public D3AnyImageBuffer readData(D3AnyImageBuffer dst) throws IOException {
		ByteBuffer fb = map(raf, MapMode.READ_ONLY, dataOff, dataLen);
		//System.out.println("FileBuffer Position: "+fb.position()+ " Limit: " + fb.limit());
		
		if (info.getType() == D1Type.D1Tshort) {
			readShortVolume((D3AnyImageShortBuffer)dst, fb);
		} else {
			readByteVolume((D3AnyImageByteBuffer)dst, fb);
		}
		return dst;
	}

	public void readData(D3AnyImage aim) throws IOException {
		D3AnyImageBuffer dst = aim.getData();
		dst = readData(dst);
	}

	private long readShortVolume(D3AnyImageShortBuffer dst, ByteBuffer src) {
		long t0 = System.currentTimeMillis();
		long ut0 = System.currentTimeMillis();
		long dt = 0;
		long udt = 0;
		long UPDATE_INTERVAL = 1000;
		int bufferSize = 4096;
		short[] buffer = new short[bufferSize];
		long totalRead = 0L;
		int base = 0;
		
		System.out.println("Reading Short Volume...");
		
		ShortBuffer sb = src.asShortBuffer();
		int byteCount = sb.capacity();
		
		while (sb.remaining()>0) {
			if (sb.remaining()<bufferSize) {
				bufferSize = sb.remaining();
			}
			sb.get(buffer, 0, bufferSize);
			totalRead += bufferSize;
			
				for (int i=base,j=0; i<(base+bufferSize); i++,j++) {
					short s= buffer[j];
					if (toUnsignedShort) {
						s =cvtToUShort(s, zeroClip, doScale);
					}
					dst.setShort(i, s); 
				}
				
			base += bufferSize;
			dt = System.currentTimeMillis() - t0;
			udt = System.currentTimeMillis() - ut0;
			if (udt >= UPDATE_INTERVAL) {
				showProgress(totalRead*2, byteCount*2, dt);
				ut0 = System.currentTimeMillis();
			}
		}
		dt = System.currentTimeMillis() - t0;
		showProgress(totalRead*2, byteCount*2, dt);
		System.out.println("Reading Short Volume completed.");
		return totalRead;
	}

	private void showProgress(long cnt, long len, double dt) {
			System.out.println("Read Progress: "+ cnt / (1024*1024) + " MB (" + (100*cnt/len) + "%) Elapsed Time: " + (float)dt/1000f + " sec. Rate: "+ cnt/(1024*1024)/((float)dt/1000f) + " MB/sec");
	}
	
	private long readByteVolume(D3AnyImageByteBuffer dst, ByteBuffer src) {
		System.out.println("Type: "+info.getType()+ " Name: " + info.getDataType() + " Size: "+info.getDataSize());
		if(info.getType()==D1Type.D1TbinCmp) {
			uncompressBinCmp(dst, src);
			return src.capacity();
		} else if(info.getType()==D1Type.D1TcharCmp) {
			uncompressCharCmp(dst, src);
			return src.capacity();
				
		} else {
			info.setType(D1Type.D1Tchar);			
			long cnt=0;
			byte val;
			while(src.remaining()>0) {
				val = src.get();
				if ((cnt % (1024*1024*20))==0 && cnt>0) {
					System.out.println("FileBuffer Position: "+src.position()+ " Limit: " + src.limit() + " value: "+val);
				}
				dst.setByte(cnt, val);
				cnt++;
			}
			return cnt;
			
		}
}

	private void uncompressBinCmp(D3AnyImageByteBuffer dst, ByteBuffer src) {
		int VALUE_1_OFF;
		int VALUE_2_OFF;
		int VALUES_OFFS;
		
		if (!is030) {
			VALUE_1_OFF = 4;
			VALUE_2_OFF = 5;
			VALUES_OFFS = 6;
		} else {
			VALUE_1_OFF = 8;
			VALUE_2_OFF = 9;
			VALUES_OFFS = 10;
		}
		
		boolean change_val;
		boolean is_value_1;
		long t0 = System.currentTimeMillis();
		long dt = 0;
		
		info.setType(D1Type.D1Tchar);			
	
		System.out.println("Uncompressing Format: D1TbinCmp ...");
	
		byte c_value_1 = src.get(VALUE_1_OFF);
		byte c_value_2 = src.get(VALUE_2_OFF);
		int field_offs = VALUES_OFFS;
		//System.out.println("!>  c_value_1: " + c_value_1);
		//System.out.println("!>  c_value_2: " + c_value_2);
	
		int cur_len = toUByte(src.get(field_offs));
		//System.out.println("!>  cur_len: " + cur_len);
	
		if( cur_len == 255) {
			cur_len = 254;
			change_val = false;
		} else { 
			change_val = true;
		}
	
		byte cur_val    = c_value_1;
		is_value_1 = true;
		int offs = 0;
	
		for( int kz=0; kz<info.getDim().z; kz++ ) {
			for( int ky=0; ky<info.getDim().y; ky++ ) {
				offs = (int) ((kz*info.getDim().y + ky)*info.getDim().x);
				for( int kx=0; kx<info.getDim().x; kx++, offs++) {
					if ((offs % (1024*1024*20))==0 && offs>0) {
						dt = System.currentTimeMillis() - t0;
						System.out.println("Processed: "+ offs / (1024*1024) + " MB. Elapsed Time: " + (float)dt/1000f + " sec. Rate: "+ offs/(1024*1024)/((float)dt/1000f) + " MB/sec");
					}
					dst.setByte(offs, cur_val);
					cur_len--;
					if( cur_len == 0 ){
						if( change_val ){
							is_value_1 = !is_value_1;
							cur_val = (is_value_1)? c_value_1 : c_value_2;
							//System.out.println(" ["+ offs+"] " + cur_val);
						}
						field_offs++;
						if (src.capacity() != field_offs) {
							cur_len = toUByte(src.get(field_offs));
							if( cur_len == 255) {
								cur_len = 254;
								change_val = false;
							} else {  
								change_val = true;
							}
						}
					}
				}	
			}
		}
		dt = System.currentTimeMillis() - t0;
		System.out.println("Finished: "+ offs / (1024*1024) + " MB. Elapsed Time: " + (float)dt/1000f + " sec. Rate: "+ offs/(1024*1024)/((float)dt/1000f) + " MB/sec");
	
	
		System.out.println("Uncompressing done.");
	
	}
	private void uncompressCharCmp(D3AnyImageByteBuffer dst, ByteBuffer src) {
		int VALUE_LEN;
		int VALUE_VAL;
		int VALUES_OFFS;
		
		if (!is030) {
			VALUES_OFFS = 4;
		} else {
			VALUES_OFFS = 8;
		}
		
		byte cur_val;
		int cur_len;
		int field_offs= VALUES_OFFS;
		
		long t0 = System.currentTimeMillis();
		long dt = 0;
		
		info.setType(D1Type.D1Tchar);			
	
		System.out.println("Uncompressing Format: D1TcharCmp ...");
		
		cur_val = src.get(field_offs);
		cur_len = toUByte(src.get(field_offs+1));
		
		//System.out.println("!>  cur_val: " + cur_val);
		//System.out.println("!>  cur_len: " + cur_len);
		
		int offs = 0;
	
		for( int kz=0; kz<info.getDim().z; kz++ ) {
			for( int ky=0; ky<info.getDim().y; ky++ ) {
				offs = (int) ((kz*info.getDim().y + ky)*info.getDim().x);
				for( int kx=0; kx<info.getDim().x; kx++, offs++) {
					if ((offs % (1024*1024*20))==0 && offs>0) {
						dt = System.currentTimeMillis() - t0;
						System.out.println("Processed: "+ offs / (1024*1024) + " MB. Elapsed Time: " + (float)dt/1000f + " sec. Rate: "+ offs/(1024*1024)/((float)dt/1000f) + " MB/sec");
					}
					dst.setByte(offs, cur_val);
					cur_len--;
					if( cur_len == 0){
						//System.out.println("!>  offs: " + offs);
						//System.out.println("!>  field_offs: " + field_offs);
						field_offs += 2;
						if (src.capacity() != field_offs) {
						cur_val = src.get(field_offs);
						cur_len = toUByte(src.get(field_offs+1));
						//System.out.println("!>  cur_val: " + cur_val);
						//System.out.println("!>  cur_len: " + cur_len);
						} else {
							//System.out.println("!>  cur_val: " + cur_val);
							//System.out.println("!>  cur_len: " + cur_len);
							
						}
						
					}
				}	
			}
		}
		dt = System.currentTimeMillis() - t0;
		System.out.println("Finished: "+ offs / (1024*1024) + " MB. Elapsed Time: " + (float)dt/1000f + " sec. Rate: "+ offs/(1024*1024)/((float)dt/1000f) + " MB/sec");
	
	
		System.out.println("Uncompressing done.");
	
	}



	public void close() throws IOException {
        //raf.close();
    }
 
	private void readHeadInfo020(ByteBuffer buf, D3AnyImageInfo hi) {
		int[] ids = new int[HEAD_INT_FIELDS_020];
		ids = decode(buf, ids);
		
		D3int[] geo = new D3int[HEAD_GEO_FIELDS_020];
		geo = decode(buf, geo);
		
		D3float els = new D3float();
		els = decode(buf, els);
		
		hi.version = (short)ids[0];
		hi.id = ids[3];
		hi.ref = ids[4];
		hi.type = ids[5];
		hi.pos = (new D3int64(geo[0]));
		hi.dim = (new D3int64(geo[1]));
		hi.off = (new D3int64(geo[2]));
		hi.supdim = (new D3int64(geo[3]));
		hi.suppos = (new D3int64(geo[4]));
		hi.subdim = (new D3int64(geo[5]));
		hi.testoff = (new D3int64(geo[6]));	
		hi.el_size_mm = els;
		
	}

	private void readHeadInfo030(ByteBuffer buf, D3AnyImageInfo hi) {
		int[] ids = new int[HEAD_INT_FIELDS_030];
		ids = decode(buf, ids);
		
		D3int64[] geo = new D3int64[HEAD_GEO_FIELDS_030];
		geo = decode(buf, geo);
		
		hi.version = (short)ids[0];
		hi.id = ids[1];
		hi.ref = ids[2];
		hi.type = ids[3];
		hi.pos = geo[0];
		hi.dim = geo[1];
		hi.off = geo[2];
		hi.supdim = geo[3];
		hi.suppos = geo[4];
		hi.subdim = geo[5];
		hi.testoff = geo[6];
		
		D3float els = new D3float();
		els.x = geo[7].x / 1000f;
		els.y = geo[7].y / 1000f;
		els.z = geo[7].z / 1000f;
		
		hi.el_size_mm = els;
		
	}

	private void readProcInfo(ByteBuffer buf, D3AnyImageInfo hi) throws IOException {
		byte[] arr = new byte[(int) buf.capacity()];
		buf.get(arr);
		String procLog = new String(arr);
		hi.setProcLog(procLog);
	}
	
	public static long readLong(ByteBuffer buf) {
		return buf.getLong();
	}
	
	public static int readInt(ByteBuffer buf) {
		return buf.getInt();
	}
	
	public static short readShort(ByteBuffer buf) {
		return buf.getShort();
	}
	
	public static byte readByte(ByteBuffer buf) {
		return buf.get();
	}
	
	public static float readFloat(ByteBuffer buf) {
		return intToVaxFloat(buf.getInt());
	}
	
	public static D3int decode(ByteBuffer buf, D3int val) {
		D3int cval;
		if (val != null) {
			cval = val;
		} else {
			cval = new D3int();
		}
		cval.x = buf.getInt();
		cval.y = buf.getInt();
		cval.z = buf.getInt();
		return cval;
	}
	
	public static D3int64 decode(ByteBuffer buf, D3int64 val) {
		D3int64 cval;
		if (val != null) {
			cval = val;
		} else {
			cval = new D3int64();
		}
		cval.x = buf.getLong();
		cval.y = buf.getLong();
		cval.z = buf.getLong();
		return cval;
	}
	
	public static D3float decode(ByteBuffer buf, D3float val) {
		D3float cval;
		if (val != null) {
			cval = val;
		} else {
			cval = new D3float();
		}
		cval.x = readFloat(buf);
		cval.y = readFloat(buf);
		cval.z = readFloat(buf);
		return cval;
	}
	
	public static int[] decode(ByteBuffer buf, int[] vals) {
		if (vals == null || vals.length==0) {
			return new int[0];
		}
		for(int i=0; i<vals.length; i++) {
			vals[i] = buf.getInt();
		}
		return vals;
	}
	
	public static D3int[] decode(ByteBuffer buf, D3int[] vals) {
		D3int[] cvals = vals;
		if (vals == null) {
			cvals = new D3int[vals.length];
		}
		for(int i=0; i<vals.length; i++) {
			D3int val = new D3int();
			decode(buf,val);
			cvals[i] = val;
		}
		return cvals;
	}
	
	public static D3int64[] decode(ByteBuffer buf, D3int64[] vals) {
		D3int64[] cvals = vals;
		if (vals == null) {
			cvals = new D3int64[vals.length];
		}
		for(int i=0; i<vals.length; i++) {
			D3int64 val = new D3int64();
			decode(buf,val);
			cvals[i] = val;
		}
		return cvals;
	}
	
	public static int toUByte(byte b) {
		return b & 0xFF;
	}

	public static int toUShort(short s) {
		return s & 0xFFFF;
	}
	public static short cvtToUShort(short s, boolean zeroClip, boolean doScale) {
		int i = (int)s;
		if (zeroClip) {
			if (i<0) {i=0;}
			if (doScale) {i = i*2;}
		} else {
			i = (int)i + 32768;
		}
		return (short)i;
	}

	public static short cvtToUShort(short s, boolean zeroClip) {
		return cvtToUShort(s, zeroClip, false);
	}

	public static short cvtToUShort(short s) {
		return cvtToUShort(s, false, false);
	}

	public static ByteBuffer encode(ByteBuffer buf, D3int64 val) {
		buf.putLong(val.x);
		buf.putLong(val.y);
		buf.putLong(val.z);
		return buf;
	}

	public static ByteBuffer encode(ByteBuffer buf, D3int val) {
		buf.putInt(val.x);
		buf.putInt(val.y);
		buf.putInt(val.z);
		return buf;
	}

	public static ByteBuffer encode(ByteBuffer buf, D3float val) {
		return D3float.encode(buf, val);
	}

	public static ByteBuffer encode(ByteBuffer buf, int val) {
		buf.putInt(val);
		return buf;
	}

	public static void swapInt16ToLittleEndian(byte[] b) {
		byte b0 = b[0];
		byte b1 = b[1];
		b[0] = b1;
		b[1] = b0;
	}

	public static void swapInt32ToLittleEndian(byte[] b) {
		byte b0 = b[0];
		byte b1 = b[1];
		byte b2 = b[2];
		byte b3 = b[3];
		b[0] = b3;
		b[1] = b2;
		b[2] = b1;
		b[3] = b0;
	}

	public static void swapInt64ToLittleEndian(byte[] b) {
		byte b0 = b[0];
		byte b1 = b[1];
		byte b2 = b[2];
		byte b3 = b[3];
		byte b4 = b[4];
		byte b5 = b[5];
		byte b6 = b[6];
		byte b7 = b[7];
		b[0] = b7;
		b[1] = b6;
		b[2] = b5;
		b[3] = b4;
		b[4] = b3;
		b[5] = b2;
		b[6] = b1;
		b[7] = b0;
	}

	public static void swapVAXToIEEEFloat(byte[] b) {
		byte b0 = b[0];
		byte b1 = b[1];
		byte b2 = b[2];
		byte b3 = b[3];
		b[0] = b1;
		b[1] = b0;
		b[2] = b3;
		b[3] = b2;
	}

	public static short toShort(byte[] b) {
		short bits = (short)((((b[0] & 0xf) << 8) | ((b[1] & 0xf) <<  0)));
		return bits;
	}

	public static short toUShort(byte[] b) {
		short bits = (short)((((b[0] & 0xff) << 8) | ((b[1] & 0xff) <<  0)));
		return bits;
	}

//	public static short toUShort(short s) {
//		byte b0 = (byte) (s >> 8);
//		byte b1 = (byte) (s >> 0);
//		return toUShort(new byte[]{b0,b1});
//	}

	public static int toInt(byte[] b) {
		int bits = (int)(
				((b[0] & 0xff) << 24) |
			    ((b[1] & 0xff) << 16) |
			    ((b[2] & 0xff) <<  8) |
			    ((b[3] & 0xff) <<  0));
		return bits;
	}

	public static long toLong(byte[] b) {
		long bits = (long)(
				  ((b[0] & 0xffff) << 56) |
			      ((b[1] & 0xffff) << 48) |
			      ((b[2] & 0xffff) << 40) |
			      ((b[3] & 0xffff) << 32) |
			      ((b[4] & 0xffff) << 24) |
			      ((b[5] & 0xffff) << 16) |
			      ((b[6] & 0xffff) <<  8) |
			      ((b[7] & 0xffff) <<  0));
		return bits;
	}

    public static float intToVaxFloat(int i) {
    	byte b0 = (byte) (i >> 24);
		byte b1 = (byte) (i >> 16);
		byte b2 = (byte) (i >> 8);
		byte b3 = (byte) (i >> 0);
		
		return bytesToVaxFloat(new byte[]{b0, b1, b2, b3});
	}

    public static float bytesToVaxFloat(byte[] b) {
		int bits = (int)(
				((b[2] & 0xff) << 24) |
			    ((b[3] & 0xff) << 16) |
			    ((b[0] & 0xff) <<  8) |
			    ((b[1] & 0xff) <<  0));
		
		float f = Float.intBitsToFloat(bits);
		f = f / 4.0f;
		return f;
	}

    public static byte[] vaxFloatToBytes(float f) {
    	byte[] b = new byte[4];
		f = f * 4.0f;
		int bits = Float.floatToIntBits(f);
		b[2] = (byte) (bits >> 24);
		b[3] = (byte) (bits >> 16);
		b[0] = (byte) (bits >> 8);
		b[1] = (byte) (bits >> 0);
		return b;
	}
    
    public static void printShort(short s) {
    	System.out.println("short=" + s + ", toUnsignedInt=" + Short.toUnsignedInt(s) +", castToShort=" + (short)Short.toUnsignedInt(s));
    	//printIntBytes(val & 0xff);
    	//printIntBytes(val & 0xffff);
    }
    
    public static void printIntBytes(int val) {
        System.out.printf ("%,14d%n", val);
    }
    
	public static MappedByteBuffer map(File file, MapMode mode, long start, long size)
			throws FileNotFoundException, IOException {

		RandomAccessFile raf = new RandomAccessFile(file, mode == MapMode.READ_ONLY ? "r" : "rw");
		return map(raf, mode, start, size);
	}

	public static MappedByteBuffer map(RandomAccessFile raf, MapMode mode,
			long start, long size) throws IOException {
		FileChannel channel = raf.getChannel();
		MappedByteBuffer mbb = channel.map(mode, start, size);
		mbb.order(ByteOrder.LITTLE_ENDIAN);
		return mbb;
	}

//	public static D3AnyImageBuffer readBlock(D3AnyImageBuffer dst, RandomAccessFile raf, long start) throws IOException {
//		FileChannel channel = raf.getChannel();
//		channel.position(start);
//		channel.read(dst.getBuffers());
//		return dst;
//	}
//
	public int getDataOffset() {
		return (int) dataOff;
	}
	
	public FileInfo getFileInfo() throws IOException {
		info = readInfo();
		FileInfo fi = new FileInfo();
		int bitsAllocated = info.getSizeOfType()*8;
		fi.fileFormat = fi.RAW;
		fi.fileName = this.filename;
		fi.width = (int)info.getDim().x;
		fi.height = (int)info.getDim().y;
		fi.nImages = (int)info.getDim().z;
		fi.offset = getDataOffset();
		fi.intelByteOrder = true;
		switch(info.getSizeOfType()) {
		case D1Type.SIZE_OF_BYTE:
			fi.fileType = FileInfo.GRAY8;
			break;
		case D1Type.SIZE_OF_SHORT:
			fi.fileType =FileInfo.GRAY16_SIGNED;
			break;
		}
		fi.pixelDepth = info.getEl_size_mm().z;
		fi.pixelWidth = info.getEl_size_mm().x;
		fi.pixelHeight = info.getEl_size_mm().y;
		fi.whiteIsZero = false;
		fi.calibrationFunction = Calibration.STRAIGHT_LINE;
		double[] coeffs = new double[2];
		coeffs[0] = info.getCalibration_intercept();
		coeffs[1] = 1.0f/info.getMu_scaling()*info.getCalibration_slope();
		fi.coefficients = coeffs;
		fi.unit = info.getCalibration_unit();
				
		return fi;
	}
	


}
