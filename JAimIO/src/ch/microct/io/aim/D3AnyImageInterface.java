package ch.microct.io.aim;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public interface D3AnyImageInterface {
	
	public D3AnyImageIO subselect(String filename, D3int64 pos, D3int64 dim, boolean local) throws IOException;

	public String getAimFileName();
	
	public void free();
	
	
	public BufferedImage createSliceImage(PLANE plane, int slice, int width, int height);
	public BufferedImage createSliceImage(PLANE plane, int slice, int width, int height, int depth);
	public BufferedImage createBufferImage(PLANE plane, int from, int to, int inc, int width, int height);
	public BufferedImage createBufferImage(PLANE plane, int from, int to, int inc, int width, int height, int depth);
	
	public float[] getSlice(PLANE plane, int slice, int width, int height, int depth);
	public float[] getSlice(PLANE plane, int slice);

	public float[] getProjection(PLANE plane, int from, int to, int inc, int width, int height, int depth);
	public float[] getProjection(PLANE plane, int from, int to, int inc);

	public float getVoxel(int z, int y, int x);
	
	public float getMin();
	public float getMax();
	public float getDataMin();
	public float getDataMax();
	public float getTypeMin();
	public float getTypeMax();

	public void setMinAndMax(float min, float max);

	public int center(PLANE plane);
	public int center();

	public int width(PLANE plane);
	public int width();
	public int height(PLANE plane);
	public int height();
	public int depth(PLANE plane);
	public int depth();
	
	public long voxels();

	public D3AnyImageInfo getInfo();


}
