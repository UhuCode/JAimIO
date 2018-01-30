package ch.microct.io.aim;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.io.RoiDecoder;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.microct.util.LogUtil;

public class D3AnyImageIO implements D3AnyImageInterface {
	
	public static final int NEAREST_NEIGHBOR = 0;
	public static final int TRILINEAR_INTERPOLATION = 1;
	public static final int BSPLINES_INTERPOLATION = 2;

	private static boolean debug = true;
	private D3AnyImage aim;
	//private File aimFile;
	//private String aimFileName;
	//private boolean onlyHeader;
	
	private float[] sliceZ;
	private float[] sliceY;
	private float[] sliceX;
	
	private float cmin;
	private float cmax;
	
	private static LogUtil logger = LogUtil.getLogger(D3AnyImageIO.class);
	
	public D3AnyImageIO() {
		aim = null;
		//onlyHeader = false;
	}
	
	public D3AnyImageIO(D3AnyImage aim) {
		this.aim = aim;
		initialize(aim);
		//onlyHeader = false;
	}
	
	private void initialize(D3AnyImage aim) {
		logger.sayOut(" -> Type Min/Max: " + aim.getTypeMin() + "/" + aim.getTypeMax());
		if (aim.getData() != null) {
			logger.sayOut(" -> Data Min/Max: " + aim.getDataMin() + "/" + aim.getDataMax());
			setMinAndMax(0, aim.getTypeMax());
			logger.sayOut(" -> View Min/Max: " + getMin() + "/" + getMax());
		}
		this.sliceZ = new float[(int) (aim.dimx() * aim.dimy())];
		this.sliceY = new float[(int) (aim.dimx() * aim.dimz())];
		this.sliceX = new float[(int) (aim.dimz() * aim.dimy())];
		
	}
	
	public static void main(String[] args) {
		try {
			String grayscale = "C0011099.AIM";
			@SuppressWarnings("unused")
			String segmented = "C0000258_SEG.AIM";
			String segmented2 = "C0000132_SEG_test_sub.AIM";
			//@SuppressWarnings("unused")
			//D3AnyImageIO aio = read(grayscale, false);
			ImageJ ij = new ImageJ();
			ImagePlus aio = readImageStack(grayscale);
			//ImagePlus aio = readImageStack(segmented2);
			//System.out.println(aio.toString());
		    //aio.show();
		    WindowManager.addWindow(aio.getWindow());
		    aio.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public D3AnyImageIO subselect(String filename, D3int64 pos, D3int64 dim, boolean local) throws IOException {
		D3int64 origPos = aim.getPos();
		D3int64 origDim = aim.getDim();
		float factor = -0.01f;
	
		if (local) {
			if (pos.x == -1) {pos.x = 0;}
			if (pos.y == -1) {pos.y = 0;}
			if (pos.z == -1) {pos.z = 0;}
			if (pos.x < 0) {pos.x = (long)(factor * (float)pos.x * (float)origDim.x);}
			if (pos.y < 0) {pos.y = (long)(factor * (float)pos.y * (float)origDim.y);}
			if (pos.z < 0) {pos.z = (long)(factor * (float)pos.z * (float)origDim.z);}
			
			pos.x += origPos.x;
			pos.y += origPos.y;
			pos.z += origPos.z;
		} else {
			if (pos.x == -1) {pos.x = origPos.x;}
			if (pos.y == -1) {pos.y = origPos.y;}
			if (pos.z == -1) {pos.z = origPos.z;}			
		}
	
		if (dim.x == -1) {dim.x = origDim.x;}
		if (dim.y == -1) {dim.y = origDim.y;}
		if (dim.z == -1) {dim.z = origDim.z;}
		if (dim.x < 0) {dim.x = (long)(factor * (float)dim.x * (float)origDim.x);}
		if (dim.y < 0) {dim.y = (long)(factor * (float)dim.y * (float)origDim.y);}
		if (dim.z < 0) {dim.z = (long)(factor * (float)dim.z * (float)origDim.z);}
		
			D3AnyImage sub = subselect(aim, filename, pos, dim);
			return new D3AnyImageIO(sub);
	}

	public String getAimFileName() {
		return aim.getFilename();
	}
	
	public void free() {
		if (aim != null) {
			aim.free();
		}
	}
	
	
	public BufferedImage createSliceImage(PLANE plane, int slice, int width, int height) {
		return createSliceImage(plane, slice, width, height, depth(plane));
	}
	
	public BufferedImage createSliceImage(PLANE plane, int slice, int width, int height, int depth) {
		D3AnyImageProcessor proc = createSliceProcessor(plane, slice, width, height, depth);
		proc.setMinAndMax(getMin(), getMax());
		return (BufferedImage) proc.createImage();
	}
	
	public BufferedImage createBufferImage(PLANE plane, int from, int to, int inc, int width, int height) {
		return createBufferImage(plane, from, to, inc, width, height, depth(plane));
	}
	
	public BufferedImage createBufferImage(PLANE plane, int from, int to, int inc, int width, int height, int depth) {
		D3AnyImageProcessor proc = createBufferProcessor(plane, from, to, inc, width, height, depth);
		proc.setMinAndMax(getMin(), getMax());
		return (BufferedImage) proc.createImage();
	}
	
	public D3AnyImageProcessor createSliceProcessor(PLANE plane, int slice, int width, int height, int depth) {
		float[] data = getSlice(plane, slice, width, height, depth);
		return new D3AnyImageProcessor(width, height, data);
	}
	
	public D3AnyImageProcessor createBufferProcessor(PLANE plane, int from, int to, int inc, int width, int height, int depth) {
		float[] data = getProjection(plane, from, to, inc, width, height, depth);
		return new D3AnyImageProcessor(width, height, data);
	}
	
	public float[] getSlice(PLANE plane, int slice, int width, int height, int depth) {

		float[] data = new float[width*height];
		
		switch(plane) {
		case XY:
			data = sliceZ(data, slice, width, height, depth);
			break;
		case XZ:
			data = sliceY(data, slice, width, height, depth);
			break;
		case ZY:
			data = sliceX(data, slice, width, height, depth);
			break;
		default:
			data = sliceZ(data, slice, width, height, depth);
		}
		return data;
	}

	public float[] getSlice(PLANE plane, int slice) {
		int width = width(plane);
		int height = height(plane);
		int depth = depth(plane);

		switch(plane) {
		case XY:
			return sliceZ(sliceZ, slice, width, height, depth);
		case XZ:
			return sliceY(sliceY, slice, width, height, depth);
		case ZY:
			return sliceX(sliceX, slice, width, height, depth);
		default:
			return sliceZ(sliceZ, slice, width, height, depth);
		}
	}

	public float[] getProjection(PLANE plane, int from, int to, int inc, int width, int height, int depth) {

		float[] data = new float[width*height];
		
		switch(plane) {
		case XY:
			data = projectZ(data, from, to, inc, width, height, depth);
			break;
		case XZ:
			data = projectY(data, from, to, inc, width, height, depth);
			break;
		case ZY:
			data = projectX(data, from, to, inc, width, height, depth);
			break;
		default:
			data = projectZ(data, from, to, inc, width, height, depth);
		}
		return data;
	}

	public float[] getProjection(PLANE plane, int from, int to, int inc) {
		int width = width(plane);
		int height = height(plane);
		int depth = depth(plane);

		switch(plane) {
		case XY:
			return projectZ(sliceZ, from, to, inc, width, height, depth);
		case XZ:
			return projectY(sliceY, from, to, inc, width, height, depth);
		case ZY:
			return projectX(sliceX, from, to, inc, width, height, depth);
		default:
			return projectZ(sliceZ, from, to, inc, width, height, depth);
		}
	}

	public float getVoxel(int z, int y, int x) {
		return getVoxel(position(x, y, z));
	}

//	public float[] getRay(D3int64 p1, D3int64 p2) {
//		private boolean isRayX = false;
//		private boolean isRayY = false;
//		private boolean isRayZ = false;
//		if (p1.x != p2.x) { isRayX = true;}
//		if (p1.y != p2.y) { isRayY = true;}
//		if (p1.z != p2.z) { isRayZ = true;}
//		
//		return getVoxel(position(x, y, z));
//	}

	public float getVoxel(long p) {
		return aim.getVoxel(p);
	}

	public float calibrate(float v) {
		
		float slope = aim.getInfo().getCalibration_slope();
		float intercept = aim.getInfo().getCalibration_intercept();
		float mu_scaling = aim.getInfo().getMu_scaling();
		float mu_water = aim.getInfo().getMu_water();
		
		int sizeOfType = aim.getInfo().getSizeOfType();
		
//		switch (unitMode) {
//		case CALIBRATION_UNIT_HA:
//			if (sizeOfType >= 2) {
//				return (slope * (v / mu_scaling) + intercept);	
//			}
//			break;
//		case CALIBRATION_UNIT_HU:
//			if (sizeOfType >= 2) {
//				return (((v / mu_scaling) - mu_water) / mu_water * 1000.0f);
//			}
//			break;
//		default:
//			// no calibration
//		}
		return v;
	}
	
	public float getMaxValue(ImageMode normMode, ImageMode valueMode) {
		float max;
		switch(normMode) {
		case MAX_RANGE:	  
			max = getTypeMax();
			break;
		case FIX_RANGE:	  
			max = getMax();
			break;
		case ADAPT_VOLUME:
			max = getDataMax();	
			break;
		default:
			max = getTypeMax();
		}
		
		if (valueMode == ImageMode.POS_VALUES) {
			if (max < 0) {
				max = 0;
			}
		}
		return max;
	}
	
	public float getMinValue(ImageMode normMode, ImageMode valueMode) {
		float min;
		switch(normMode) {
		case MAX_RANGE:	  
			min = getTypeMin();
			break;			
		case FIX_RANGE:	  
			min = getMin();
			break;			
		case ADAPT_VOLUME:
			min = getDataMin();
			break;			
		default:
			min = getTypeMin();
		}
		if (valueMode == ImageMode.POS_VALUES) {
			if (min < 0) {
				min = 0;
			}
		}
		return min;
	}
	
	public float getMin() {
		return cmin;
	}

	public float getMax() {
		return cmax;
	}

	public float getDataMin() {
		return aim.getDataMin();
	}

	public float getDataMax() {
		return aim.getDataMax();
	}

	public float getTypeMin() {
		return aim.getTypeMin();
	}

	public float getTypeMax() {
		return aim.getTypeMax();
	}

	protected long position(int x, int y, int z) {
		return (long)(z * width() * height() + y * width() + x);
	}

	protected float[] sliceZ(float[] data, int slice, int width, int height, int depth){
		PLANE p = PLANE.XY;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float fz = (float)depth / (float)depth(p);
		int kZ = (int) (slice / fz);
		int kY = 0;
		int kX = 0;
		int index = 0;
		long time = System.currentTimeMillis();
		for(int i=0; i<(width*height); i++) {
			kY = (int) ((i / width) / fh);
			kX = (int) ((i % width) / fw);
			data[index++] = getVoxel(kZ, kY, kX);
		}
		time = (System.currentTimeMillis() - time);
		say_deb("SliceZ " + slice + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}

	protected float[] sliceY(float[] data, int slice, int width, int height, int depth) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.XZ;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float fz = (float)depth / (float)depth(p);
		int kZ = 0;
		int kY = (int) (slice / fz);
		int kX = 0;
		int index = 0;
		for(int i=0; i<(width*height); i++) {
			kZ = (int) ((i / width) / fh);
			kX = (int) ((i % width) / fw);
			data[index++] = getVoxel(kZ, kY, kX);
		}
		time = (System.currentTimeMillis() - time);
		say_deb("SliceY " + slice + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}

	protected float[] sliceX(float[] data, int slice, int width, int height, int depth) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.ZY;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float fz = (float)depth / (float)depth(p);
		int kZ = 0;
		int kY = 0;
		int kX = (int) (slice / fz);
		int index = 0;
		for(int i=0; i<(width*height); i++) {
			kY = (int) ((i / width) / fh);
			kZ = (int) ((i % width) / fw);
			data[index++] = getVoxel(kZ, kY, kX);
		}
		time = (System.currentTimeMillis() - time);
		say_deb("SliceX " + slice + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}

	protected float[] projectZ(float[] data, int from, int to, int inc, int width, int height) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.XY;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float c_val;
		float c_sum;
		float c_max;
		float c_ave;
		int c_len;
		int c_pos;
		float[] c_arr;
		
		int kZ = from;
		int kY = 0;
		int kX = 0;
		c_len = to-from;
		c_arr = new float[c_len];
		for(int i=0; i<data.length; i++) {
			kY = (int) ((i / width) / fh);
			kX = (int) ((i % width) / fw);
			c_sum=0;
			c_max=0;
			c_ave=0;
			c_pos =0;
			
			for(int k=from;k<to;k+=inc) {
				kZ = k;
				c_val = getVoxel(kZ, kY, kX);
				c_sum += c_val;
				c_max = Math.max(c_val,c_max);
				c_arr[c_pos++] = c_val;
			}
			c_ave = c_sum/((to-from)/inc);
			for(int j=0;j<c_len;j++) {
				
			}
			data[i] = c_ave;
		}
	
		time = (System.currentTimeMillis() - time);
		say_deb("ProjectionZ " + from + " - " +" to " + to + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}
	
	protected float[] projectY(float[] data, int from, int to, int inc, int width, int height) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.XZ;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float c_val;
		float c_sum;
		float c_max;
		float c_ave;
		
		int kZ = 0;
		int kY = from;
		int kX = 0;
		for(int i=0; i<data.length; i++) {
			kZ = (int) ((i / width) / fh);
			kX = (int) ((i % width) / fw);
			c_sum=0;
			c_max=0;
			c_ave=0;
			for(int k=from;k<to;k+=inc) {
				kY = k;
				c_val = getVoxel(kZ, kY, kX);
				c_sum += c_val;
				c_max = Math.max(c_val,c_max); 
			}
			c_ave = c_sum/((to-from)/inc);
			data[i] = c_ave;
		}
	
		time = (System.currentTimeMillis() - time);
		say_deb("ProjectionY " + from + " - " +" to " + to + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}
	
	protected float[] projectX(float[] data, int from, int to, int inc, int width, int height) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.ZY;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float c_val;
		float c_sum;
		float c_max;
		float c_ave;
		
		int kZ = 0;
		int kY = 0;
		int kX = from;
		for(int i=0; i<data.length; i++) {
			kY = (int) ((i / width) / fh);
			kZ = (int) ((i % width) / fw);
			c_sum=0;
			c_max=0;
			c_ave=0;
			for(int k=from;k<to;k+=inc) {
				kX = k;
				c_val = getVoxel(kZ, kY, kX);
				c_sum += c_val;
				c_max = Math.max(c_val,c_max); 
			}
			c_ave = c_sum/((to-from)/inc);
			data[i] = c_ave;
		}
	
		time = (System.currentTimeMillis() - time);
		say_deb("ProjectionX " + from + " - " +" to " + to + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}
	
	protected float[] projectZ(float[] data, int from, int to, int inc, int width, int height, int depth) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.XY;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float fd = (float)depth / (float)depth(p);
		float c_val;
		float c_sum;
		float c_max;
		float c_ave;
		
		int start = (int)(from / fd);
		int end = (int)(to / fd);
		int step = (int)(inc / fd);
		
		int c_len;
		int c_pos;
		float[] c_arr;
		
		int kZ = start;
		int kY = 0;
		int kX = 0;
		c_len = end-start;
		c_arr = new float[c_len];
		for(int i=0; i<data.length; i++) {
			kY = (int) ((i / width) / fh);
			kX = (int) ((i % width) / fw);
			c_sum=0;
			c_max=0;
			c_ave=0;
			c_pos =0;
			
			for(int k=start;k<end;k+=step) {
				kZ = k;
				c_val = getVoxel(kZ, kY, kX);
				c_sum += c_val;
				c_max = Math.max(c_val,c_max);
				c_arr[c_pos++] = c_val;
			}
			c_ave = c_sum/((end-start)/step);
			data[i] = c_ave;
		}
	
		time = (System.currentTimeMillis() - time);
		say_deb("ProjectionZ " + start + " - " +" to " + end + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}
	
	protected float[] projectY(float[] data, int from, int to, int inc, int width, int height, int depth) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.XZ;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float fd = (float)depth / (float)depth(p);
		float c_val;
		float c_sum;
		float c_max;
		float c_ave;
		
		int start = (int)(from / fd);
		int end = (int)(to / fd);
		int step = (int)(inc / fd);
		
		int kZ = 0;
		int kY = start;
		int kX = 0;
		for(int i=0; i<data.length; i++) {
			kZ = (int) ((i / width) / fh);
			kX = (int) ((i % width) / fw);
			c_sum=0;
			c_max=0;
			c_ave=0;
			for(int k=start;k<end;k+=step) {
				kY = k;
				c_val = getVoxel(kZ, kY, kX);
				c_sum += c_val;
				c_max = Math.max(c_val,c_max); 
			}
			c_ave = c_sum/((end-start)/step);
			data[i] = c_ave;
		}
	
		time = (System.currentTimeMillis() - time);
		say_deb("ProjectionY " + start + " - " +" to " + end + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}
	
	protected float[] projectX(float[] data, int from, int to, int inc, int width, int height, int depth) {
		long time = System.currentTimeMillis();
		PLANE p = PLANE.ZY;
		float fw = (float)width / (float)width(p);
		float fh = (float)height / (float)height(p);
		float fd = (float)depth / (float)depth(p);
		float c_val;
		float c_sum;
		float c_max;
		float c_ave;
		
		int start = (int)(from / fd);
		int end = (int)(to / fd);
		int step = (int)(inc / fd);
		
		int kZ = 0;
		int kY = 0;
		int kX = start;
		for(int i=0; i<data.length; i++) {
			kY = (int) ((i / width) / fh);
			kZ = (int) ((i % width) / fw);
			c_sum=0;
			c_max=0;
			c_ave=0;
			for(int k=start;k<end;k+=step) {
				kX = k;
				c_val = getVoxel(kZ, kY, kX);
				c_sum += c_val;
				c_max = Math.max(c_val,c_max); 
			}
			c_ave = c_sum/((end-start)/step);
			data[i] = c_ave;
		}
	
		time = (System.currentTimeMillis() - time);
		say_deb("ProjectionX " + start + " - " +" to " + end + " (" + width + "x" + height + ") read in " + time/1000.0f + " s");
		return data;
	}
	
	public int center(PLANE plane) {
		return depth(plane)/2;
	}

	public int center() {
		return (int) aim.dimz()/2;
	}

	public int width(PLANE plane) {
		switch(plane) {
		case XY:
			return width();
		case XZ:
			return width();
		case ZY:
			return depth();
		default:
			return width();
		}
	}

	public int width() {
		return (int) aim.dimx();
	}

	public int height(PLANE plane) {
		switch(plane) {
		case XY:
			return height();
		case XZ:
			return depth();
		case ZY:
			return height();
		default:
			return height();
		}
	}

	public int height() {
		return (int) aim.dimy();
	}

	public int depth(PLANE plane) {
		switch(plane) {
		case XY:
			return depth();
		case XZ:
			return height();
		case ZY:
			return width();
		default:
			return depth();
		}
	}

	public int depth() {
		return (int) aim.dimz();
	}
	
	public long voxels() {
		return aim.voxels();
	}

	public D3AnyImageInfo getInfo() {
		return aim.getInfo();
	}

	public void setMinAndMax(float min, float max) {
		this.cmin = min;
		this.cmax = max;
	}

	
	public static D3AnyImage initImage(D3AnyImage aim, int type) throws FileNotFoundException, IOException {
		D3AnyImageInfo info = new D3AnyImageInfo(type);
		aim.setInfo(info);
		return aim;
	}
	
	public static D3AnyImage initImage(int type) throws FileNotFoundException, IOException {
		D3AnyImage aim = new D3AnyImage();
		return initImage(aim, type);
	}
	
	public static D3AnyImageIO read(String filename, boolean onlyHeader) throws IOException {
		D3AnyImage aim = readImage(filename, onlyHeader);		
		D3AnyImageIO aio = new D3AnyImageIO(aim);
		return aio;
	}

	public static ImagePlus readImageStack(String filename) throws IOException {
		say_out("Read ImageStack for: " + filename);
		D3AnyImage aim = new D3AnyImage();
		aim.setFilename(filename);
		say_out("Creating D3AnyImageDecoder for: " + filename);
		D3AnyImageDecoder dec = new D3AnyImageDecoder(filename);
		say_out("Reading D3AnyImageInfo: ");
		dec.readInfo(aim);
		say_out("Creating FileInfo: ");
		FileInfo fi = dec.getFileInfo();
		say_out("Allocating Memory: ");
		allocate(aim, null);
		
		if (fi.fileType == FileInfo.GRAY16_SIGNED) {
//			say_out("File Type GRAY16_SIGNED. Setting flag to convert to unsigned short for ImageJ (adding 32768 to signed values). ");
//			dec.setConvertUnsignedShort(true, false, false);
			say_out("File Type GRAY16_SIGNED. Setting flag to convert to unsigned short for ImageJ (set negative values to 0). ");
			dec.setConvertUnsignedShort(true, true, false);
//			say_out("File Type GRAY16_SIGNED. Setting flag to convert to unsigned short for ImageJ (set negative values to 0 and scale by 2). ");
//			dec.setConvertUnsignedShort(true, true, true);
		}
		say_out("Reading Data:");
		dec.readData(aim);

		//ImagePlus imp = IJ.createImage(fi.fileName, (int)aim.dimx(), (int)aim.dimy(), (int)aim.dimz(), aim.getSizeOfType()*8);
		say_out("Creating ImageStack:");
		ImageStack ims = new ImageStack((int)aim.dimx(), (int)aim.dimy(), (int)aim.dimz());
		//imp.setFileInfo(fi);
		//ImageStack is = imp.getStack();
		say_out("Adding Slices to ImageStack:");
		for (int i=0; i<aim.dimz(); i++) {
			ims.setPixels(aim.getData().getBufferArray(i), i+1);
			//ImageProcessor ip = is.getProcessor(i+1);
			//aim.getData().getPixels(i, ip.getPixels());
			//ip.resetMinAndMax();
			//is.setProcessor(ip, i+1);
		}
		//imp.show();
		say_out("Creating ImagePlus:");
		ImagePlus imp = new ImagePlus(fi.fileName, ims);
		if (fi.fileType == FileInfo.GRAY16_SIGNED) {
//			say_out("Setting Signed16BitCalibration:");
//			imp.getLocalCalibration().setSigned16BitCalibration();
			if (fi.coefficients != null && fi.calibrationFunction!=Calibration.NONE) {
				say_out("Setting Density Calibration:");
				imp.getCalibration().setFunction(fi.calibrationFunction, fi.coefficients, fi.unit);
			}
		}
		
		say_out("Setting FileInfo to ImagePlus:");
		imp.setFileInfo(fi);
		say_out("Resetting Min and Max:");
		imp.getProcessor().resetMinAndMax();
		say_out("Min/Max: " + imp.getProcessor().getMin() + "/" + imp.getProcessor().getMax());
		//imp.show();
		//imp.updateAndDraw();
		return imp;
		
	}

	public static D3AnyImage readImage(String file, boolean onlyHead) throws FileNotFoundException, IOException {
		D3AnyImage aim = new D3AnyImage();
		aim.setFilename(file);
		D3AnyImageDecoder dec = new D3AnyImageDecoder(file);
		dec.readInfo(aim);
		if(!onlyHead) {
			allocate(aim, null);
			dec.readData(aim);
		}
		return aim;
	}
	
	public static D3AnyImage readImage(File file) throws FileNotFoundException, IOException {
		return readImage(file.getAbsolutePath(), false);
	}
	
	public static D3AnyImage readHead(String file) throws FileNotFoundException, IOException {
		return readImage(file, true);
	}
	
	public static D3AnyImage subselect (D3AnyImage aim, String newAimName, D3int64 pos, D3int64 dim) throws FileNotFoundException, IOException {
		D3AnyImage subaim = initImage(aim.getType());
		subaim.setFilename(newAimName);
		
		subaim.setPos(pos);
		subaim.setDim(dim);
		subaim.setOff(aim.getOff());
		subaim.setSuppos(aim.getInfo().suppos);
		subaim.setSupdim(aim.getInfo().supdim);
		subaim.setSubdim(aim.getInfo().subdim);
		subaim.setTestoff(aim.getInfo().testoff);
		subaim.setEl_size_mm(aim.getEl_size_mm());
		copyProcLog(aim, subaim);
		
		allocate(subaim);
		say_out("Sub Image allocated:\n" + subaim.toString());
		
		subselect(aim, subaim);
		say_out("Image subselected.");
		
		StringBuffer sb = new StringBuffer();
		sb.append("Procedure: \tD3AnyImageIO.createImage()\n");
		sb.append("\n");
		sb.append("Orig position:  \t" + aim.getPos().toString() + "\n");
		sb.append("Orig dimension: \t" + aim.getDim().toString() + "\n");
		sb.append("New  position:  \t" + subaim.getPos().toString() + "\n");
		sb.append("New  dimension: \t" + subaim.getDim().toString() + "\n");
		sb.append("----------------------------------------------------\n");
		updateProcLog(subaim, sb.toString());
		return subaim;
	}
	public static void subselect(D3AnyImage aim, D3AnyImage sub) {					
						
		//D3AnyImageBuffer org = aim.getData();
		//D3AnyImageBuffer sub = sub.getData();
		long count = 0;
		D3int64 offset = new D3int64();
		offset.x = sub.getPos().x - aim.getPos().x;
		offset.y = sub.getPos().y - aim.getPos().y;
		offset.z = sub.getPos().z - aim.getPos().z;
		boolean insideX = true;
		boolean insideY = true;
		boolean insideZ = true;
		
		for(long z=0; z<sub.dimz(); z++) {
			insideZ = ((z + offset.z < 0) || (aim.dimz() - offset.z <= z)) ? false : true;
			for(long y=0; y<sub.dimy(); y++) {
				insideY = (y + offset.y < 0 || aim.dimy() - offset.y <= y) ? false : true;
				for(long x=0; x<sub.dimx(); x++) {
					insideX = (x + offset.x < 0 || aim.dimx() - offset.x <= x) ? false : true;
					if (insideX && insideY && insideZ) {
						sub.setVoxel(count, aim.getVoxel((z + offset.z) * aim.dimy() * aim.dimx() + (y + offset.y) * aim.dimx() + (x + offset.x)));
					} else {
						sub.setVoxel(count, 0);
					}
				}
			}
		}
	}

	public static void allocate(D3AnyImage aim) {
		allocate(aim, null);
	}

	public static void allocate(D3AnyImage aim, String mapFile) {
		long bufSize = aim.getDim().size() * aim.getSizeOfType();
		int mapSize = (int) (aim.dimx()*aim.dimy() * aim.getSizeOfType());
		D3AnyImageBuffer data;
		try {
			data = D3AnyImageBuffer.allocate(aim.getType(), bufSize, mapSize, mapFile);
			aim.setData(data);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	public static void updateProcLog(D3AnyImage aim, String string) {
		aim.getInfo().getProcLog().concat(string);
	}

	public static void copyProcLog(D3AnyImage in, D3AnyImage out) {
		out.getInfo().setProcLog(in.getInfo().getProcLog());
	}

	static void say_out(String msg) {
		logger.sayOut(msg);
	}

	static void say_err(String msg) {
		logger.sayErr(msg);
	}
	
	static void say_deb(String msg) {
		logger.sayDeb(msg);
	}

	/** Experimental */
//	public void getNeighborhood(int x, int y, double[][] arr) {
//		int nx=arr.length;
//		int ny=arr[0].length;
//		int nx2 = (nx-1)/2;
//		int ny2 = (ny-1)/2;
//	 	if (x>=nx2 && y>=ny2 && x<width-nx2-1 && y<height-ny2-1) { 
//			int index = (y-ny2)*width + (x-nx2);
//			for (int y2=0; y2<ny; y2++) {
//	 			for (int x2=0; x2<nx; x2++)
//					arr[x2][y2] = getf(index++);			
//				index += (width - nx);
//			}	
//		} else {
//			for (int y2=0; y2<ny; y2++) {
//	 			for (int x2=0; x2<nx; x2++)
//					arr[x2][y2] = getPixelValue(x2, y2);			
//			}	
//		}
//	}
//
//
	public final float getVoxel(float kZ, float kY, float kX) {
		if (kX<0.0 || kX>=width()-1.0 || kY<0.0 || kY>=height()-1.0|| kZ<0.0 || kZ>=depth()-1.0) {
			if (kX<-1.0 || kX>=width() || kY<-1.0 || kY>=height() || kZ<-1.0 || kZ>=depth())
				return 0.0f;
			//else
				//return getInterpolatedEdgeValue(kZ, kY, kX);
		}
		int xbase = (int)kX;
		int ybase = (int)kY;
		int zbase = (int)kZ;
		float xFraction = kX - xbase;
		float yFraction = kY - ybase;
		float zFraction = kZ - zbase;
		if (xFraction<0.0) xFraction = 0.0f;
		if (yFraction<0.0) yFraction = 0.0f;
		if (zFraction<0.0) zFraction = 0.0f;
		float v000 = getVoxel(zbase, ybase, xbase);
		float v100 = getVoxel(zbase, ybase, xbase+1);
		float v010 = getVoxel(zbase, ybase+1, xbase);
		float v110 = getVoxel(zbase, ybase+1, xbase+1);
		float v001 = getVoxel(zbase+1, ybase, xbase);
		float v101 = getVoxel(zbase+1, ybase, xbase+1);
		float v011 = getVoxel(zbase+1, ybase+1, xbase);
		float v111 = getVoxel(zbase+1, ybase+1, xbase+1);
		float vx00 = v000 + xFraction * (v100 - v000);
		float vx10 = v010 + xFraction * (v110 - v010);
		float vx01 = v001 + xFraction * (v101 - v001);
		float vx11 = v011 + xFraction * (v111 - v011);
		float vxy0 = vx00 + yFraction * (vx10 - vx00);
		float vxy1 = vx01 + yFraction * (vx11 - vx01);
		float vxyz = vxy0 + zFraction * (vxy1 - vxy0);
		
		return vxyz;
	}

	/** This method is from Chapter 16 of "Digital Image Processing:
		An Algorithmic Introduction Using Java" by Burger and Burge
		(http://www.imagingbook.com/). */
//	public double getBicubicInterpolatedPixel(double x0, double y0, D3AnyProcessor ip2) {
//		int u0 = (int) Math.floor(x0);	//use floor to handle negative coordinates too
//		int v0 = (int) Math.floor(y0);
//		if (u0<=0 || u0>=width-2 || v0<=0 || v0>=height-2)
//			return ip2.getBilinearInterpolatedPixel(x0, y0);
//		double q = 0;
//		for (int j = 0; j <= 3; j++) {
//			int v = v0 - 1 + j;
//			double p = 0;
//			for (int i = 0; i <= 3; i++) {
//				int u = u0 - 1 + i;
//				p = p + ip2.get(u,v) * cubic(x0 - u);
//			}
//			q = q + p * cubic(y0 - v);
//		}
//		return q;
//	}
	
//	final double getBilinearInterpolatedPixel(double x, double y) {
//		if (x>=-1 && x<width && y>=-1 && y<height) {
//			int method = interpolationMethod;
//			interpolationMethod = BILINEAR;
//			double value = getInterpolatedPixel(x, y);
//			interpolationMethod = method;
//			return value;
//		} else
//			return getBackgroundValue();
//	}
//	
	static final double a = 0.5; // Catmull-Rom interpolation
	public static final double cubic(double x) {
		if (x < 0.0) x = -x;
		double z = 0.0;
		if (x < 1.0) 
			z = x*x*(x*(-a+2.0) + (a-3.0)) + 1.0;
		else if (x < 2.0) 
			z = -a*x*x*x + 5.0*a*x*x - 8.0*a*x + 4.0*a;
		return z;
	}	

	/*
		// a = 0.5
	double cubic2(double x) {
		if (x < 0) x = -x;
		double z = 0;
		if (x < 1)
			z = 1.5*x*x*x + -2.5*x*x + 1.0;
		else if (x < 2)
			z = -0.5*x*x*x + 2.5*x*x - 4.0*x + 2.0;
		return z;
	}
	*/	

//	private final double getInterpolatedEdgeValue(double x, double y) {
//		int xbase = (int)x;
//		int ybase = (int)y;
//		double xFraction = x - xbase;
//		double yFraction = y - ybase;
//		if (xFraction<0.0) xFraction = 0.0;
//		if (yFraction<0.0) yFraction = 0.0;
////		double lowerLeft = getEdgeValue(xbase, ybase);
////		double lowerRight = getEdgeValue(xbase+1, ybase);
////		double upperRight = getEdgeValue(xbase+1, ybase+1);
////		double upperLeft = getEdgeValue(xbase, ybase+1);
//		double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
//		double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
//		return lowerAverage + yFraction * (upperAverage - lowerAverage);
//	}

//	private float getEdgeValue(int x, int y) {
//		if (x<=0) x = 0;
//		if (x>=width) x = width-1;
//		if (y<=0) y = 0;
//		if (y>=height) y = height-1;
//		return getPixelValue(x, y);
//	}

	

}
