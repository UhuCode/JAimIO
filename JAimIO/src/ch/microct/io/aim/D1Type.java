/*
 * Created on 12.12.2005
 *
 * Author: kohler
 * 
 */
package ch.microct.io.aim;



public class D1Type {
	
	public enum INT {
		INT16,
		INT32,
		INT64
	}
	
	public static final int SIZE_OF_BYTE 	= 	1;
	public static final int SIZE_OF_SHORT 	= 	2;
	public static final int SIZE_OF_INT 	= 	4;
	public static final int SIZE_OF_LONG 	= 	8;
	public static final int SIZE_OF_FLOAT 	= 	4;
	public static final int SIZE_OF_DOUBLE 	= 	8;
	
	public static final int D1Tundef	  =  	0;
	public static final int D1Tchar		  =  	((1<<16)  + SIZE_OF_BYTE);
	public static final int D1Tpixel	  = 	((13<<16) + SIZE_OF_BYTE);
	public static final int D1Tshort	  =  	((2<<16)  + SIZE_OF_SHORT);
	public static final int D1Tlong		  =  	((3<<16)  + SIZE_OF_LONG);
	public static final int D1Tint  	  =		((3<<16)  + SIZE_OF_INT);
	public static final int D1Tfloat	  =  	((4<<16)  + SIZE_OF_FLOAT);
	public static final int D1TcharCmp 	  = 	((8<<16)  + 2*SIZE_OF_BYTE);
	public static final int D1TcharCmp2   = 	((8<<16)  + 3*SIZE_OF_BYTE);
	public static final int D1Tboolean	  = 	((12<<16) + SIZE_OF_LONG);
	public static final int D1Trgb		  = 	((18<<16) + SIZE_OF_INT);
	public static final int D1Tdouble	  = 	((20<<16) + SIZE_OF_DOUBLE);
	public static final int D1TbinCmp	  = 	((21<<16) + SIZE_OF_BYTE);
	public static final int D1Tuchar	  = 	((22<<16) + SIZE_OF_BYTE);
	public static final int D1Tushort	  = 	((23<<16) + SIZE_OF_SHORT);
	public static final int D1Ti3efloat	  =   	((26<<16) + SIZE_OF_FLOAT);
	public static final int D1TZBencoded  =   	((100<<16)+ SIZE_OF_BYTE); /* XAimpack */

	public static final int D1Tsize_mask  =     0x0000FFFF;		  /* first two bytes */
	
	public static int sizeOfType(int typeID) {
		switch (typeID) {
		
		case D1TcharCmp:
			return SIZE_OF_BYTE;
		case D1TcharCmp2:
			return SIZE_OF_BYTE;
		default:
			return typeID & D1Tsize_mask;
		}
	}
	
	public static float max(int typeID) {
		int sizeOfType = sizeOfType(typeID);
		return (float)((Math.pow(2.0d, (sizeOfType * 8.0d - 1.0d))) - 1);		  			  
	}

	public static float min(int typeID) {
		int sizeOfType = sizeOfType(typeID);
		return (-1) * (float)((Math.pow(2.0d, (sizeOfType * 8.0d - 1.0d))) - 1);		  			  
	}

	public static boolean isByteType(int typeID) {
		return (sizeOfType(typeID) == SIZE_OF_BYTE);
	}
	
	public static boolean isShortType(int typeID) {
		return (sizeOfType(typeID) == SIZE_OF_SHORT);
	}
	
	public static boolean isIntType(int typeID) {
		return (sizeOfType(typeID) == SIZE_OF_INT);
	}
	
	public static String nameOfType(int typeID) {
		
		switch (typeID) {
		
		case D1TbinCmp:
			return "D1TbinCmp";
		case D1TcharCmp:
			return "D1TcharCmp";
		case D1TcharCmp2:
			return "D1TcharCmp2";
		case D1Tchar:
			return "D1Tchar";
		case D1Tpixel:
			return "D1Tpixel";
			
		case D1Tshort:
			return "D1Tshort";
			
		case D1Tfloat:
			return "D1Tfloat";
			
		case D1Tint:
			return "D1Tint";
			
		case D1Tdouble:
			return "D1Tdouble";
			
		case D1Tlong:
			return "D1Tlong";
			
		default:
			return "D1Tundef";
		}
		
	}
	


}
