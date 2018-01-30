/*
 * Created on 12.12.2005
 *
 * Author: kohler
 * 
 */
package ch.microct.io.aim;

import java.io.Serializable;
import java.nio.ByteBuffer;

import javax.vecmath.Vector3f;

public class D3floatVector extends Vector3f {
	
	public D3floatVector() {
		super();
	}

	public D3floatVector(D3floatPoint p) {
		super(p);
	}

	public D3floatVector(D3float vec) {
		super(vec);
	}

	public D3floatVector(D3floatVector vec) {
		super(vec);
	}

    public D3floatVector(float x, float y, float z) {
    	super(x, y, z);
	}

	public static void main(String[] args) {
		System.out.println("Test D3floatVector.array():");
		D3floatVector d3f = new D3floatVector(new D3float(10,4,1));
		System.out.println(d3f.toString());
		float[] vec = new float[3];
		d3f.get(vec);
		System.out.println(vec[0] + " " + vec[1] + " " + vec[2]);
	}

}
