/*
 * Created on 12.12.2005
 *
 * Author: kohler
 * 
 */
package ch.microct.io.aim;

import java.io.Serializable;
import java.nio.ByteBuffer;

import javax.vecmath.Tuple3f;

public class D3float extends Tuple3f {
	
	public D3float() {
		this(0,0,0);
	}

	public D3float(float x, float y, float z) {
		super(x, y, z);
	}

    public D3float(ByteBuffer buf) {	
    	int xi = buf.getInt();
    	int yi = buf.getInt();
    	int zi = buf.getInt();
		x = cvtVaxFloat(xi);
		y = cvtVaxFloat(yi);
		z = cvtVaxFloat(zi);
	}

    public D3float(D3float other) {
		this(other.x, other.y, other.z);
	}

	public static ByteBuffer encode(ByteBuffer buf, D3float val) {
    	byte[] xb = cvtVaxFloat(val.x);
    	byte[] yb = cvtVaxFloat(val.y);
    	byte[] zb = cvtVaxFloat(val.z);
    	buf.put(xb);
    	buf.put(yb);
    	buf.put(zb);
		return buf;
	}

	public static D3float wrap(D3float d3f) {
		return new D3float(d3f.x, d3f.y, d3f.z);
	}

	public static D3float wrap(float[] f) {
		return new D3float(f[0], f[1], f[2]);
	}
	
	public float[] array() {
		return new float[]{x, y, z};
	}

	public float len() {		
		return (float) Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
	}
	
	public String toString() {
		return "D3float:\t " + super.toString();		
	}

    private static float cvtVaxFloat(int i) {
    	byte b0 = (byte) (i >> 24);
		byte b1 = (byte) (i >> 16);
		byte b2 = (byte) (i >> 8);
		byte b3 = (byte) (i >> 0);
		
		return cvtVaxFloat(new byte[]{b0, b1, b2, b3});
	}

    private static float cvtVaxFloat(byte[] b) {
		int bits = (int)(
				((b[2] & 0xff) << 24) |
			    ((b[3] & 0xff) << 16) |
			    ((b[0] & 0xff) <<  8) |
			    ((b[1] & 0xff) <<  0));
		
		float f = Float.intBitsToFloat(bits);
		f = f / 4.0f;
		return f;
	}

    private static byte[] cvtVaxFloat(float f) {
    	byte[] b = new byte[4];
		f = f * 4.0f;
		int bits = Float.floatToIntBits(f);
		b[2] = (byte) (bits >> 24);
		b[3] = (byte) (bits >> 16);
		b[0] = (byte) (bits >> 8);
		b[1] = (byte) (bits >> 0);
		return b;
	}

    public static void main(String[] args) {
		System.out.println("Test D3float.array():");
		D3float d3f = new D3float(1,2,3);
		System.out.println(d3f.toString());
		float[] vec = d3f.array();
		System.out.println(vec[0] + " " + vec[1] + " " + vec[2]);
	}


}
