/**
 * 
 */
package ch.microct.io.aim;

import javax.vecmath.Point3i;

/**
 * @author thomas
 *
 */
public class D3Voxel {
	
	float val;
	Point3i pos;

	public D3Voxel(D3int64 pos, float val) {
		this.pos = new Point3i((int)pos.x, (int)pos.y, (int)pos.z);
		this.val = val;
	}
	

}
