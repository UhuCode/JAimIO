/*
 * Created on 12.12.2005
 *
 * Author: kohler
 * 
 */
package ch.microct.io.aim;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class D3int64 extends Tuple3l {
	
	
	public D3int64() {
		super();
	}

	public D3int64(long x, long y, long z) {
		super(x, y, z);
	}

	public D3int64(D3int64 v) {
		this(v.x, v.y, v.z);
	}

	public D3int64(D3int v) {
		this(v.x, v.y, v.z);
	}

	public static D3int get(D3int64 other) {
		D3int v = new D3int();
		v.x = (int)other.x;
		v.y = (int)other.y;
		v.z = (int)other.z;
		return v;
	}
	public double len() {
		return (double) Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
	}
	
	public long size() {
		return this.x*this.y*this.z;
	}
	
	public String toString() {
		return "\t x = " + x + " \t y = " + y + " \t z = "+ z;
		
	}
	
	public static D3int64 decode(ByteBuffer buf) {
		return new D3int64(buf.getLong(), buf.getLong(), buf.getLong());
	}
	
	public static ByteBuffer encode(ByteBuffer buf, D3int64 val) {
		buf.putLong(val.x);
		buf.putLong(val.y);
		buf.putLong(val.z);
		return buf;
	}
	public static ByteBuffer encode(ByteBuffer buf, D3int val) {
		buf.putInt((int) val.x);
		buf.putInt((int) val.y);
		buf.putInt((int) val.z);
		return buf;
	}

	


}
