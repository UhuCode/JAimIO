/*
 * Created on 12.12.2005
 *
 * Author: kohler
 * 
 */
package ch.microct.io.aim;

import java.io.Serializable;
import java.nio.ByteBuffer;

import javax.vecmath.Tuple3i;

public class D3int extends Tuple3i {
	
	public D3int() {
		super(0,0,0);
	}

	public D3int(int x, int y, int z) {
		super(x,y,z);
	}

	public D3int(D3int v) {
		super(v);
	}

	public D3int(D3int64 v) {
		super(D3int64.get(v));
	}

	public float len() {
		return (float) Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
	}
	
	public String toString() {
		return "\t x = " + x + " \t y = " + y + " \t z = "+ z;
		
	}
	
	public static D3int decode(ByteBuffer buf) {
		return new D3int(buf.getInt(), buf.getInt(), buf.getInt());
	}
	
	public static ByteBuffer encode(ByteBuffer buf, D3int val) {
		buf.putInt((int) val.x);
		buf.putInt((int) val.y);
		buf.putInt((int) val.z);
		return buf;
	}
	
	public static ByteBuffer encode(ByteBuffer buf, D3int64 val) {
		buf.putInt((int) val.x);
		buf.putInt((int) val.y);
		buf.putInt((int) val.z);
		return buf;
	}


}
