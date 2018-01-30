package ch.microct.io.aim;


import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;





public class D3AnyImageProcessor {


	private static final int COLOR_BAND_MAX = 255;
	private static final int COLOR_BAND_MIN = 0;
	public static final double NO_THRESHOLD = -808080.0;
	static public final int RED_LUT=0, BLACK_AND_WHITE_LUT=1, NO_LUT_UPDATE=2, OVER_UNDER_LUT=3;
	static final int INVERT=0, FILL=1, ADD=2, MULT=3, AND=4, OR=5,
			XOR=6, GAMMA=7, LOG=8, MINIMUM=9, MAXIMUM=10, SQR=11, SQRT=12, EXP=13, ABS=14, SET=15;


	protected int width, snapshotWidth;
	protected int height, snapshotHeight;
	protected double minThreshold=NO_THRESHOLD, maxThreshold=NO_THRESHOLD;
	private int lutUpdateMode;
	private byte[] rLUT1;
	private byte[] gLUT1;
	private byte[] bLUT1;
	private byte[] rLUT2;
	private byte[] gLUT2;
	private byte[] bLUT2;
	private static int overRed, overGreen=255, overBlue;
	private static int underRed, underGreen, underBlue=255;
	int fgColor = 0;
	protected MemoryImageSource source;
	protected Image img;
	protected boolean newPixels;
	protected ColorModel baseCM; // base color model
	protected ColorModel cm;
	protected WritableRaster raster;
	protected BufferedImage image;
	protected BufferedImage fmImage;
	protected ColorModel cm2;
	protected SampleModel sampleModel;
	protected static IndexColorModel defaultColorModel;
	protected boolean minMaxSet;
	private float min, max, snapshotMin, snapshotMax;
	private float[] pixels;
	protected byte[] pixels8;
	private float[] snapshotPixels = null;
	private float fillColor =  Float.MAX_VALUE;
	private boolean fixedScale = false;
	private boolean lutAnimation = false;


	public D3AnyImageProcessor(int width, int height, float[] pixels, ColorModel cm) {
		this.width = width;
		this.height = height;
		this.pixels = pixels;
		this.cm = cm;
	}

	public D3AnyImageProcessor(int width, int height, float[] pixels) {
		this(width, height, pixels, null);
	}

	public D3AnyImageProcessor(int width, int height) {
		this(width, height, null, null);
	}

	public void setPixels(float[] pixels) {
		if (pixels.length == width * height) {
			this.pixels = pixels;
			resetMinAndMax();
		}
	}

	/** Create an 8-bit AWT image by scaling pixels in the range min-max to 0-255. */
	public Image createImage() {
		boolean firstTime = pixels8==null;
		if (firstTime || !lutAnimation )
			create8BitImage();
		if (cm==null)
			makeDefaultColorModel();
		return createBufferedImage();
	}
	
	// scale from float to 8-bits
	protected byte[] create8BitImage() {
		int size = width*height;
		if (pixels8==null)
			pixels8 = new byte[size];
		float value;
		int ivalue;
		float min2=(float)getMin(), max2=(float)getMax();
		float scale = 255f/(max2-min2);
		for (int i=0; i<size; i++) {
			value = pixels[i]-min2;
			if (value<0f) value = 0f;
			ivalue = (int)((value*scale)+0.5f);
			if (ivalue>255) ivalue = 255;
			pixels8[i] = (byte)ivalue;
		}
		return pixels8;
	}

	Image createBufferedImage() {
		if (raster==null) {
			SampleModel sm = getIndexSampleModel();
			DataBuffer db = new DataBufferByte(pixels8, width*height, 0);
			raster = Raster.createWritableRaster(sm, db, null);
		}
		if (image==null || cm!=cm2) {
			if (cm==null) cm = getDefaultColorModel();
			image = new BufferedImage(cm, raster, false, null);
			cm2 = cm;
		}
		return image;
	}

	private void process(int op, double value) {
		double SCALE = 255.0/Math.log(255.0);
		int v;

		int[] lut = new int[256];
		for (int i=0; i<256; i++) {
			switch(op) {
			case INVERT:
				v = 255 - i;
				break;
			case FILL:
				v = fgColor;
				break;
			case SET:
				v = (int)value;
				break;
			case ADD:
				v = i + (int)value;
				break;
			case MULT:
				v = (int)Math.round(i * value);
				break;
			case AND:
				v = i & (int)value;
				break;
			case OR:
				v = i | (int)value;
				break;
			case XOR:
				v = i ^ (int)value;
				break;
			case GAMMA:
				v = (int)(Math.exp(Math.log(i/255.0)*value)*255.0);
				break;
			case LOG:
				if (i==0)
					v = 0;
				else
					v = (int)(Math.log(i) * SCALE);
				break;
			case EXP:
				v = (int)(Math.exp(i/SCALE));
				break;
			case SQR:
				v = i*i;
				break;
			case SQRT:
				v = (int)Math.sqrt(i);
				break;
			case MINIMUM:
				if (i<value)
					v = (int)value;
				else
					v = i;
				break;
			case MAXIMUM:
				if (i>value)
					v = (int)value;
				else
					v = i;
				break;
			default:
				v = i;
			}
			if (v < 0)
				v = 0;
			if (v > 255)
				v = 255;
			lut[i] = v;
		}
		applyTable(lut);
	}

	/** Sets the lower and upper threshold levels. The 'lutUpdate' argument
	can be RED_LUT, BLACK_AND_WHITE_LUT, OVER_UNDER_LUT or NO_LUT_UPDATE.
	Thresholding of RGB images is not supported. */
	public void setThreshold(double minThreshold, double maxThreshold, int lutUpdate) {
		//ij.IJ.log("setThreshold: "+" "+minThreshold+" "+maxThreshold+" "+lutUpdate);
		this.minThreshold = minThreshold;
		this.maxThreshold = maxThreshold;
		lutUpdateMode = lutUpdate;
		if (minThreshold==NO_THRESHOLD) {
			resetThreshold();
			return;
		}
		if (lutUpdate==NO_LUT_UPDATE)
			return;
		if (rLUT1==null) {
			if (cm==null)
				makeDefaultColorModel();
			baseCM = cm;
			IndexColorModel m = (IndexColorModel)cm;
			rLUT1 = new byte[256]; gLUT1 = new byte[256]; bLUT1 = new byte[256];
			m.getReds(rLUT1); m.getGreens(gLUT1); m.getBlues(bLUT1);
			rLUT2 = new byte[256]; gLUT2 = new byte[256]; bLUT2 = new byte[256];
		}
		int t1 = (int)minThreshold;
		int t2 = (int)maxThreshold;
		int index;
		if (lutUpdate==RED_LUT)
			for (int i=0; i<256; i++) {
				if (i>=t1 && i<=t2) {
					rLUT2[i] = (byte)255;
					gLUT2[i] = (byte)0;
					bLUT2[i] = (byte)0;
				} else {
					rLUT2[i] = rLUT1[i];
					gLUT2[i] = gLUT1[i];
					bLUT2[i] = bLUT1[i];
				}
			}
		else if (lutUpdate==BLACK_AND_WHITE_LUT) {
			// updated in v1.43i by Gabriel Lindini to use blackBackground setting
			byte  foreground = (byte)255;
			byte background = (byte)(255 - foreground);
			for (int i=0; i<256; i++) {
				if (i>=t1 && i<=t2) {
					rLUT2[i] = foreground;
					gLUT2[i] = foreground;
					bLUT2[i] = foreground;
				} else {
					rLUT2[i] = background;
					gLUT2[i] =background;
					bLUT2[i] =background;
				}
			}
		} else {
			for (int i=0; i<256; i++) {
				if (i>=t1 && i<=t2) {
					rLUT2[i] = rLUT1[i];
					gLUT2[i] = gLUT1[i];
					bLUT2[i] = bLUT1[i];
				} else if (i>t2) {
					rLUT2[i] = (byte)overRed;
					gLUT2[i] = (byte)overGreen;
					bLUT2[i] = (byte)overBlue;
				} else { 
					rLUT2[i] = (byte)underRed;
					gLUT2[i] = (byte)underGreen; 
					bLUT2[i] = (byte)underBlue;
				}
			}
		}
		cm = new IndexColorModel(8, 256, rLUT2, gLUT2, bLUT2);
		newPixels = true;
		source = null;
	}

	private void makeDefaultColorModel() {
		cm = getDefaultColorModel();
	}

	/** Disables thresholding. */
	public void resetThreshold() {
		minThreshold = NO_THRESHOLD;
		if (baseCM!=null) {
			cm = baseCM;
			baseCM = null;
		}
		rLUT1 = rLUT2 = null;
		newPixels = true;
		source = null;
	}

	/** Returns the lower threshold level. Returns NO_THRESHOLD
	if thresholding is not enabled. */
	public double getMinThreshold() {
		return minThreshold;
	}

	/** Returns the upper threshold level. */
	public double getMaxThreshold() {
		return maxThreshold;
	}

	/** Returns the LUT update mode, which can be RED_LUT, BLACK_AND_WHITE_LUT, 
	OVER_UNDER_LUT or NO_LUT_UPDATE. */
	public int getLutUpdateMode() {
		return lutUpdateMode;
	}

	/** Returns true if this image uses a color LUT. */
	public boolean isColorLut() {
		if (cm==null || !(cm instanceof IndexColorModel))
			return false;
		IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		icm.getReds(reds); 
		icm.getGreens(greens); 
		icm.getBlues(blues);
		boolean isColor = false;
		for (int i=0; i<mapSize; i++) {
			if ((reds[i] != greens[i]) || (greens[i] != blues[i])) {
				isColor = true;
				break;
			}
		}
		return isColor;
	}

	/** Returns true if the image is using the default grayscale LUT. */
	public boolean isDefaultLut() {
		if (cm==null)
			makeDefaultColorModel();
		if (!(cm instanceof IndexColorModel))
			return false;
		IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		if (mapSize!=256)
			return false;
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		icm.getReds(reds); 
		icm.getGreens(greens); 
		icm.getBlues(blues);
		boolean isDefault = true;
		for (int i=0; i<mapSize; i++) {
			if ((reds[i]&255)!=i || (greens[i]&255)!=i || (blues[i]&255)!=i) {
				isDefault = false;
				break;
			}
		}
		return isDefault;
	}


	/**
	Calculates the minimum and maximum pixel value for the entire image. 
	Returns without doing anything if fixedScale has been set true as a result
	of calling setMinAndMax(). In this case, getMin() and getMax() return the
	fixed min and max defined by setMinAndMax(), rather than the calculated min
	and max.
	@see #getMin()
	@see #getMin()
	*/
	public void findMinAndMax() {
		if (fixedScale)
			return;
		float v;
		for (int i=0; i<(width*height); i++) {
			v = pixels[i];
			if (v>max) {max = v;}
			if (v<min) {min = v;}
		}
		minMaxSet = true;
	}

	/**
	Sets the min and max variables that control how real
	pixel values are mapped to 0-255 screen values. Use
	resetMinAndMax() to enable auto-scaling;
	@see ij.plugin.frame.ContrastAdjuster 
	*/
	public void setMinAndMax(double minimum, double maximum) {
		if (minimum==0.0 && maximum==0.0)
			{resetMinAndMax(); return;}
		min = (float)minimum;
		max = (float)maximum;
		fixedScale = true;
		minMaxSet = true;
		resetThreshold();
	}

	/** Recalculates the min and max values used to scale pixel
		values to 0-255 for display. This ensures that this 
		FloatProcessor is set up to correctly display the image. */
	public void resetMinAndMax() {
		fixedScale = false;
		findMinAndMax();
		resetThreshold();
	}

	/** Returns the smallest displayed pixel value. */
	public double getMin() {
		if (!minMaxSet) findMinAndMax();
		return min;
	}

	/** Returns the largest displayed pixel value. */
	public double getMax() {
		if (!minMaxSet) findMinAndMax();
		return max;
	}

	/** Transforms the image or ROI using a lookup table. The
	length of the table must be 256 for byte images and 
	65536 for short images. RGB and float images are not
	supported. */
	public void applyTable(int[] lut) {
		int v;
		for (int i=0; i<(width*height); i++) {
				v = lut[(int)(pixels8[i])&0xff];
				pixels8[i] = (byte)v;
		}
		findMinAndMax();
	}

	/** Inverts the image or ROI. */
	public void invert() {process(INVERT, 0.0);}

	/** Adds 'value' to each pixel in the image or ROI. */
	public void add(int value) {process(ADD, value);}

	/** Adds 'value' to each pixel in the image or ROI. */
	public void add(double value) {process(ADD, value);}

	/** Subtracts 'value' from each pixel in the image or ROI. */
	public void subtract(double value) {
		add(-value);
	}

	/** Multiplies each pixel in the image or ROI by 'value'. */
	public void multiply(double value) {process(MULT, value);}

	/** Assigns 'value' to each pixel in the image or ROI. */
	public void set(double value) {process(SET, value);}

	/** Binary AND of each pixel in the image or ROI with 'value'. */
	public void and(int value) {process(AND, value);}

	/** Binary OR of each pixel in the image or ROI with 'value'. */
	public void or(int value) {process(OR, value);}

	/** Binary exclusive OR of each pixel in the image or ROI with 'value'. */
	public void xor(int value) {process(XOR, value);}

	/** Performs gamma correction of the image or ROI. */
	public void gamma(double value) {process(GAMMA, value);}

	/** Does a natural logarithmic (base e) transform of the image or ROI. */
	public void log() {process(LOG, 0.0);}

	/** Does a natural logarithmic (base e) transform of the image or ROI. */
	public void ln() {log();}

	/** Performs a exponential transform on the image or ROI. */
	public void exp() {process(EXP, 0.0);}

	/** Performs a square transform on the image or ROI. */
	public void sqr() {process(SQR, 0.0);}

	/** Performs a square root transform on the image or ROI. */
	public void sqrt() {process(SQRT, 0.0);}

	/** If this is a 32-bit or signed 16-bit image, performs an 
	absolute value transform, otherwise does nothing. */
	public void abs() {}

	/** Pixels less than 'value' are set to 'value'. */
	public void min(double value) {process(MINIMUM, value);}

	/** Pixels greater than 'value' are set to 'value'. */
	public void max(double value) {process(MAXIMUM, value);}

	public static int toUByte(byte b) {
		return b & 0xFF;
	}

	public static int[] toRGB(float[] val, float min, float max, boolean hasAlpha) {
		int[] p = new int[val.length];
		float v;
		Color c;
		for (int i=0;i<val.length;i++) {
			v = normalize(val[i], min, max);
			c = toColor(v, hasAlpha);
			p[i] = toRGB(c);
		}
		return p;
	}

	public static int toRGB(Color col) {
		return col.getRGB();
	}

	public static Color toColor(float val, boolean hasAlpha) {
		if (hasAlpha) {
			return new Color(val,val,val,val);
		} else {
			return new Color(val,val,val,1.0f);
		}
	}

	public static float normalize(float val, float min, float max) {
		if (val < min) {
			val = min;
		}		
		if (val > max) {
			val = max;
		}
		return (val / (max - min));
	}

	public static int color(int alpha, int red, int green, int blue) {
		return alpha<<24 | red<<16 | green<<8 | blue;	
	}

	public static int color(int red, int green, int blue) {
		return COLOR_BAND_MAX<<24 | red<<16 | green<<8 | blue;	
	}

	public static float signedMax(int bitsOfType) {
		return (float)((Math.pow(2.0d, (bitsOfType - 1.0d))) - 1);		  			  
	}

	public static float signedMin(int bitsOfType) {
		return (-1) * (float)((Math.pow(2.0d, (bitsOfType - 1.0d))));		  			  
	}

	public static float unsignedMax(int bitsOfType) {
		return (float)((Math.pow(2.0d, bitsOfType)) - 1);		  			  
	}

	public static float unsignedMin(int bitsOfType) {
		return 0;		  			  
	}

	protected SampleModel getIndexSampleModel() {
		if (sampleModel==null) {
			IndexColorModel icm = getDefaultColorModel();
			WritableRaster wr = icm.createCompatibleWritableRaster(1, 1);
			sampleModel = wr.getSampleModel();
			sampleModel = sampleModel.createCompatibleSampleModel(width, height);
		}
		return sampleModel;
	}

	/** Returns the default grayscale IndexColorModel. */
	public IndexColorModel getDefaultColorModel() {
		if (defaultColorModel==null) {
			byte[] r = new byte[256];
			byte[] g = new byte[256];
			byte[] b = new byte[256];
			for(int i=0; i<256; i++) {
				r[i]=(byte)i;
				g[i]=(byte)i;
				b[i]=(byte)i;
			}
			defaultColorModel = new IndexColorModel(8, 256, r, g, b);
		}
		return defaultColorModel;
	}





}
