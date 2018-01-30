package ch.microct.io.aim;

import ij.io.FileInfo;

import java.text.DecimalFormat;



public class D3AnyImageInfo {
	
	//String fileName;
	
	short version;
	int id;
	int ref;
	int type;
	
	D3int64 pos;
	D3int64 dim;
	D3int64 off;
	D3int64 supdim;
	D3int64 suppos;
	D3int64 subdim;
	D3int64 testoff;
	D3float el_size_mm;

	String sampleName;
	int sampleNumber;
	int measNumber;	
	int site;
	int scannerID;
	int scannerType;
	int integrationTime;
	int recoAlgorithm;
	int energy;
	int intensity;
	int mu_scaling;
	String calibration_data;
	String calibration_unit;
	float calibration_slope;
	float calibration_intercept;
	float mu_water;
	
	
	
	DecimalFormat df = new DecimalFormat("0.0000");
	DecimalFormat dfb = new DecimalFormat("##0.00");

	private String procLog;

	public D3AnyImageInfo() {
		this(D1Type.D1Tundef);
		//this.fileName = file;
	}
	
	public D3AnyImageInfo(int d1type) {
		//fileName = "";
		version = 020;
		id = 0;
		ref = 0;
		type = d1type;
		pos = new D3int64();
		dim = new D3int64();
		off = new D3int64();
		supdim = new D3int64();
		suppos = new D3int64();
		subdim = new D3int64();
		testoff = new D3int64();
		el_size_mm = new D3float();

		sampleName = "";
		sampleNumber = -1;
		measNumber = -1;
		site = -1;
		scannerID = -1;
		scannerType = -1;
		integrationTime = -1;
		recoAlgorithm = -1;
		energy = -1;
		intensity = -1;
		mu_scaling = -1;
		calibration_data = "";
		calibration_unit = "";
		calibration_slope = -1.0f;
		calibration_intercept = -1.0f;
		mu_water = -1.0f;
		
	}
	
	public String getHeadInfo() {
		String header = 
			//" Aim:\t" + fileName + "\n" +
			" Ver:\t" + (int)version + "\n" +
			" Typ:\t" + getDataType() + " (" + getSizeOfType() + " Byte/Voxel)" + " \n" +
			" Dim:\t" + dim.x + "\t" + dim.y + "\t" + dim.z + " \n" +
			" Pos:\t" + pos.x + "\t" + pos.y + "\t" + pos.z + " \n" +
			" Off:\t" + off.x + "\t" + off.y + "\t" + off.z + " \n" +
			" Res:\t" + df.format(el_size_mm.x) + "\t" + df.format(el_size_mm.y) + "\t" + df.format(el_size_mm.z) + " \n" +
			" Len:\t" + getFormattedDataSize() + " \n";	
		
		return header;
	}

	public String getSampleInfo() {
		String header = 
			" Samplename:\t" + getSampleName() + " \n" +
			" Samplenumber:\t" + getSampleNumber() + " \n" +
			" Measnumber:\t" + getMeasNumber() + " \n";
		return header;
	}

	public String getMeasInfo() {
		String header = 
			getSampleInfo() +
			"\n" +
			" Site: \t\t\t" + site + " \n" +
			" Scanner ID: \t\t" + scannerID + " \n" +
			" Scanner Type: \t\t" + scannerType + " \n" +
			" Integration [us]: \t" + integrationTime + " \n" +
			" Reconstruction: \t" + recoAlgorithm + " \n" +
			" Energy [V]: \t\t" + energy + " \n" +
			" Intensity [uA]: \t" + intensity + " \n" +
			"\n" +
			" Mu Scaling: \t\t" + mu_scaling + " \n" +
			" Calibration Data: \t" + calibration_data + " \n" +
			" Density unit: \t\t" + calibration_unit + " \n" +
			" Density slope: \t" + calibration_slope + " \n" +
			" Density intercept: \t" + calibration_intercept + " \n" +
			" HU mu water: \t\t" + mu_water;
		
		return header;
	}

	public String getProcLog() {
		return procLog;
	}

	public String getFormattedDataSize() {
		double sizeByte = 1024;
		double sizeKB = 1024 * 1024;
		double sizeMB = 1024 * 1024 * 1024;
		double size = (double)getDataSize();
		if (size < sizeByte) {
			// Byte
			return (dfb.format(size) + " Byte");
		} else if ((sizeByte <= size) && (size < sizeKB)) {
			// Kilo Byte
			return (dfb.format(size / sizeByte) + " KB");
		} else if ((sizeKB <= size) && (size < sizeMB)) {
			// Mega Byte
			return (dfb.format(size / sizeKB) + " MB");
		}
		// Giga Byte
		return (dfb.format(size / sizeMB) + " GB");			
	}

	public double getDataSize() {
		return (double)dim.x * (double)dim.y * (double)dim.z * (double)getSizeOfType();			
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRef() {
		return ref;
	}

	public void setRef(int ref) {
		this.ref = ref;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDataType() {
		return D1Type.nameOfType(type);
	}

	public int getSizeOfType() {
		return D1Type.sizeOfType(type);
	}
	
	public D3int64 getDim() {
		return dim;
	}

	public void setDim(D3int64 dim) {
		this.dim = dim;
	}

	public D3float getEl_size_mm() {
		return el_size_mm;
	}

	public void setEl_size_mm(D3float el_size_mm) {
		this.el_size_mm = el_size_mm;
	}

	public D3int64 getOff() {
		return off;
	}

	public void setOff(D3int64 off) {
		this.off = off;
	}

	public D3int64 getPos() {
		return pos;
	}

	public void setPos(D3int64 pos) {
		this.pos = pos;
	}

	public void setSubdim(D3int64 d3int64) {
		subdim = d3int64;
		
	}

	public void setSuppos(D3int64 d3int64) {
		suppos = d3int64;
		
	}

	public void setSupdim(D3int64 d3int64) {
		supdim = d3int64;
		
	}

	public void setTestoff(D3int64 d3int64) {
		testoff = d3int64;
		
	}

	public int getMeasNumber() {
		return measNumber;
	}

	public void setMeasNumber(int measNumber) {
		this.measNumber = measNumber;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public int getSampleNumber() {
		return sampleNumber;
	}

	public void setSampleNumber(int sampleNumber) {
		this.sampleNumber = sampleNumber;
	}
	
	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public int getMu_scaling() {
		return mu_scaling;
	}

	public void setMu_scaling(int mu_scaling) {
		this.mu_scaling = mu_scaling;
	}
	
	public int getIntegrationTime() {
		return integrationTime;
	}
	
	public void setIntegrationTime(int integrationTime) {
		this.integrationTime = integrationTime;
	}

	public int getRecoAlgorithm() {
		return recoAlgorithm;
	}

	public void setRecoAlgorithm(int recoAlgorithm) {
		this.recoAlgorithm = recoAlgorithm;
	}

	public int getScannerID() {
		return scannerID;
	}

	public void setScannerID(int scannerID) {
		this.scannerID = scannerID;
	}

	public int getScannerType() {
		return scannerType;
	}

	public void setScannerType(int scannerType) {
		this.scannerType = scannerType;
	}

	public int getSite() {
		return site;
	}

	public void setSite(int site) {
		this.site = site;
	}
	
	public String getCalibration_data() {
		return calibration_data;
	}

	public void setCalibration_data(String calibration_data) {
		this.calibration_data = calibration_data;
	}

	public float getCalibration_intercept() {
		return calibration_intercept;
	}

	public void setCalibration_intercept(float calibration_intercept) {
		this.calibration_intercept = calibration_intercept;
	}

	public float getCalibration_slope() {
		return calibration_slope;
	}

	public void setCalibration_slope(float calibration_slope) {
		this.calibration_slope = calibration_slope;
	}

	public String getCalibration_unit() {
		return calibration_unit;
	}

	public void setCalibration_unit(String calibration_unit) {
		this.calibration_unit = calibration_unit;
	}

	public float getMu_water() {
		return mu_water;
	}

	public void setMu_water(float mu_water) {
		this.mu_water = mu_water;
	}
	
	public String toString() {
		String head = getHeadInfo();
		String proc = getMeasInfo();
		return head + "\n" + proc;
	}

	public void setProcLog(String procLog) {
		this.procLog = procLog;
		setSampleName(getSampleName(procLog));
		setSampleNumber(getSampleNumber(procLog));
		setMeasNumber(getMeasNumber(procLog));
		setSite(getSite(procLog));
		setScannerID(getScannerID(procLog));
		setScannerType(getScannerType(procLog));
		setIntegrationTime(getIntegrationTime(procLog));
		setRecoAlgorithm(getRecoAlgorithm(procLog));
		setEnergy(getEnergy(procLog));
		setIntensity(getIntensity(procLog));
		setMu_scaling(getMuScaling(procLog));
		setCalibration_data(getCalibrationData(procLog));
		setCalibration_unit(getDensityUnit(procLog));
		setCalibration_slope(getDensitySlope(procLog));
		setCalibration_intercept(getDensityIntercept(procLog));
		setMu_water(getMuWater(procLog));
	}
	
	/**
	 * Reading various parameters from procLog
	 * 
	 */
	private static String getSampleName(String procLog) {
		String search = "Patient Name";
		return readProcLogStringParam(procLog, search);
	}
	private static int getSampleNumber(String procLog) {
		String search = "Index Patient";
		return readProcLogIntParam(procLog, search);
	}
	private static int getMeasNumber(String procLog) {
		String search = "Index Measurement";
		return readProcLogIntParam(procLog, search);
	}	
	private static int getSite(String procLog) {
		String search = "Site";
		return readProcLogIntParam(procLog, search);
	}
	private static int getScannerID(String procLog) {
		String search = "Scanner ID";
		return readProcLogIntParam(procLog, search);
	}
	private static int getScannerType(String procLog) {
		String search = "Scanner type";
		return readProcLogIntParam(procLog, search);
	}
	private static int getIntegrationTime(String procLog) {
		String search = "Integration time [us]";
		return readProcLogIntParam(procLog, search);
	}
	private static int getRecoAlgorithm(String procLog) {
		String search = "Reconstruction-Alg.";
		return readProcLogIntParam(procLog, search);
	}
	private static int getEnergy(String procLog) {
		String search = "Energy [V]";
		return readProcLogIntParam(procLog, search);
	}
	private static int getIntensity(String procLog) {
		String search = "Intensity [uA]";
		return readProcLogIntParam(procLog, search);
	}
	private static int getMuScaling(String procLog) {
		String search = "Mu_Scaling";
		return readProcLogIntParam(procLog, search);
	}
	private static String getCalibrationData(String procLog) {
		String search = "Calibration Data";
		return readProcLogStringParam(procLog, search);
	}
	private static String getDensityUnit(String procLog) {
		String search = "Density: unit";
		return readProcLogStringParam(procLog, search);
	}
	private static float getDensitySlope(String procLog) {
		String search = "Density: slope";
		return readProcLogFloatParam(procLog, search);
	}
	private static float getDensityIntercept(String procLog) {
		String search = "Density: intercept";
		return readProcLogFloatParam(procLog, search);
	}
	private static float getMuWater(String procLog) {
		String search = "HU: mu water";
		return readProcLogFloatParam(procLog, search);
	}
	
	private static String readProcLogStringParam(String buf, String search) {
		String newLine = "\n";
		String stringParam = "";
		
		int start = buf.indexOf(search);
		if (start == -1) {
			return stringParam;
		}
		String procLog = buf.substring(start + search.length());
		int end = procLog.indexOf(newLine);
		stringParam = procLog.substring(0, end).trim();
		
		return stringParam;
	}

	private static int readProcLogIntParam(String buf, String search) {
		String newLine = "\n";
		int intParam = -1;
		
		int start = buf.indexOf(search);
		if (start == -1) {
			return intParam;
		}
		String procLog = buf.substring(start + search.length());
		int end = procLog.indexOf(newLine);
		procLog = procLog.substring(0, end).trim();
		try {
			intParam = new Integer(procLog).intValue();
		} catch (Exception e) {
			System.err.println(D3AnyImageDecoder.class.getName() + ": Error reading Integer: " + procLog);
		}
		
		return intParam;
	}

	private static float readProcLogFloatParam(String buf, String search) {
		String newLine = "\n";
		float floatParam = -1.0f;
		int start = buf.indexOf(search);
		if (start == -1) {
			return floatParam;
		}
		String procLog = buf.substring(start + search.length());
		int end = procLog.indexOf(newLine);
		procLog = procLog.substring(0, end).trim();
		try {
			floatParam = new Float(procLog).floatValue();
		} catch (Exception e) {
			System.err.println(D3AnyImageDecoder.class.getName() + ": Error reading Float: " + procLog);
		}
		
		return floatParam;
	}





}
