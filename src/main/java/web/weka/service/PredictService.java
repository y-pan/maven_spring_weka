package web.weka.service;

import static utils.Lib.GetInstances;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import utils.Lib;
import utils.Lib.FILE_TYPE;
import web.weka.exceptions.InvalidFileOrUrlException;
import web.weka.exceptions.OutOfResourceException;
import web.weka.model.Feature;
import web.weka.model.GetFeaturesResponse;
import web.weka.model.IPredictResponse;
import web.weka.model.IPrediction;
import web.weka.model.ModelMeta;
import web.weka.model.PredictRequest;
import web.weka.model.PredictResponse;
import web.weka.model.Prediction;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

@CrossOrigin(origins = "http://localhost:8080")
@Service
public class PredictService {

	public static enum MEMORY_TYPE { featureMemory, modelMemory, all }
	private int size = 50;  // default initial size 50
	
	private int fUsed = 0;  // for featueset
	private int mUsed = 0;  // for model
	
	HashMap<String, ModelMeta> fStatMap = new HashMap<String, ModelMeta>(); // model stats, only to add more elements, not going to remove
	HashMap<String, ModelMeta> mStatMap = new HashMap<String, ModelMeta>(); // model stats, only to add more elements, not going to remove

	ArrayList<String> fKeysList = new ArrayList<String>(); // element could be removed due to "this.size" limit, also use as the scope to determine which are to be removed
	ArrayList<String> mKeysList = new ArrayList<String>(); // element could be removed due to "this.size" limit, also use as the scope to determine which are to be removed

	HashMap<String, ArrayList<Feature>> fMemoryMap = new HashMap<String, ArrayList<Feature>>(); // could be removed due to "this.size" limit
	HashMap<String, Classifier> mMemoryMap = new HashMap<>(); // element could be removed due to "this.size" limit
		
	public PredictService() {

	}

	public synchronized void setSize(int size) {
		if(size < 0) return; // <0, invalid, ignore; >=0 up-scale/down-scale service, 0 -> stop service 
		this.size = size;
		releaseMemory(this.fUsed - this.size, MEMORY_TYPE.featureMemory);   // trigger release memory
		releaseMemory(this.mUsed - this.size, MEMORY_TYPE.modelMemory); 
	}
	
	private void removeOldOrFirstFeatureSet() {
		if(fStatMap.size() > 0 ) {
			String oldestKey = Lib.getOldestModel(fStatMap, fKeysList);
				if(oldestKey.equals("") || oldestKey.equals(null)) {
					System.out.println("F ok not found: ");
					fMemoryMap.remove(fKeysList.get(0));
					fKeysList.remove(0);
				}else {
					fMemoryMap.remove(oldestKey);
					fKeysList.remove(oldestKey);
				}
				this.fUsed = this.fUsed - 1;
				System.out.println("remove oldest f: " +oldestKey);
			}
	}
	private void removeOldOrFirstModel() {
		if(mStatMap.size() >0 ) {
			String oldestKey = Lib.getOldestModel(mStatMap, mKeysList);
			if(oldestKey.equals("") || oldestKey.equals(null)) {
				System.out.println("M ok not found: ");
				mMemoryMap.remove(mKeysList.get(0));
				mKeysList.remove(0);
			}
			else {
				mMemoryMap.remove(oldestKey);
				mKeysList.remove(oldestKey);
			}
			this.mUsed = this.mUsed - 1;
			System.out.println("remove oldest m: " +oldestKey);
		}
	}
	public void releaseMemory(int releaseCount, MEMORY_TYPE type) {
		
		if(releaseCount <= 0 ) return;

		for(int i = 0; i<releaseCount; i++) {

			if(type == MEMORY_TYPE.featureMemory ) {
				removeOldOrFirstFeatureSet();
			}else if(type == MEMORY_TYPE.modelMemory){
				removeOldOrFirstModel();
			}else if(type == MEMORY_TYPE.all) {
				removeOldOrFirstFeatureSet();
				removeOldOrFirstModel();
			}
		}
	}
	

	// key is file name under one folder, iris.arff - iris.model
	public PredictService prepareFeatures(String key, String structFilePath) throws IOException, InvalidFileOrUrlException, OutOfResourceException {

		if(this.size<=0) throw new OutOfResourceException();
		
		if (fMemoryMap.containsKey(key)) {  // memory has this model's features
			System.out.println("--------- in feature memory: used="+this.fUsed+"|fmemory="+fMemoryMap.size()+"|fkey="+fKeysList.size() + " ["+this.size +"] -------------");
			if(fStatMap.containsKey(key)) {
				fStatMap.get(key).mark();// mark this model(name=key) is called now.
			}
			return this;
		}
		
		// memory doesn't have it, load from .arff file, CHECK file valid first
		File _f = new File(structFilePath);
		if (!_f.exists() || !_f.isFile() || !_f.canRead() || !_f.getName().toLowerCase().endsWith(".arff")) { 
			throw new InvalidFileOrUrlException();
		}
		
		// make room if full
		if (fKeysList.size() >= this.size) {  // memory maximum reached, dump one
			System.out.println("--------- overflow feature memory: used="+this.fUsed+"|fmemory="+fMemoryMap.size()+"|fkey="+fKeysList.size() + " ["+this.size +"] -------------");
			this.releaseMemory(1, MEMORY_TYPE.featureMemory);
		}
		// now load file
		Instances struct = GetInstances(structFilePath);
		ArrayList<Feature> flist = new ArrayList<Feature>();
		int numAtt = struct.numAttributes();
		for (int i = 0; i < numAtt; i++) {
			Feature f = new Feature();
			Attribute att = struct.attribute(i);
			f.setIndex(i);
			f.setName(att.name());

			if (att.isNumeric()) {
				f.setType("Numeric");
			} else if (att.isDate()) {
				f.setType("Date");
			} else if (att.isString()) {
				f.setType("String");
			} else if (att.isNominal()) {
				f.setType("Nominal");

				List<String> optionList = new ArrayList<String>();
				Enumeration values = att.enumerateValues();

				while (values.hasMoreElements()) {
					optionList.add(values.nextElement().toString());
				}
				String[] optionArray = new String[optionList.size()];
				optionArray = optionList.toArray(optionArray);
				f.setOptions(optionArray);
			} else {
				f.setType("Unknown");
			}
			flist.add(f);
		}
		fMemoryMap.put(key, flist);
		fKeysList.add(key);
		ModelMeta _mm = new ModelMeta(struct.relationName(), Lib.GetModelType(structFilePath));
		fStatMap.put(key, _mm); // key is from URL path-value, could be filename(or part, or relevant to it if file encrypted) of .arff file  
		fStatMap.get(key).mark();
		
		if(!mStatMap.containsKey(key)) { // if POST happens first, ModelMeta will be push into mStatMap only 
			mStatMap.put(key, _mm);
		}
		
		this.fUsed = this.fUsed + 1;
		System.out.println("--------- new added in feature memory: used="+this.fUsed+"|fmemory="+fMemoryMap.size()+"|fkey="+fKeysList.size() + " ["+this.size +"] -------------");
		return this;

	}
	public GetFeaturesResponse get(String id) {
		return new GetFeaturesResponse(fMemoryMap.get(id), fStatMap.get(id).getModelRelation() ,fStatMap.get(id).getModelType());
	}
	
	// ================================ predict ================================
	

	/**
	 * todo: generalize method
	 * @throws Exception 
	 * */
	public PredictService prepareModel(String key, String modelPath) throws InvalidFileOrUrlException, OutOfResourceException,Exception {

		if(this.size<=0) throw new OutOfResourceException();
		
		if (mMemoryMap.containsKey(key)) {  // memory has this model's features
			System.out.println("========== in model memory: used="+this.mUsed+"|memory="+mMemoryMap.size()+"|mkey="+mKeysList.size() + " ["+this.size +"] ==========");
			
			if(mStatMap.containsKey(key)) {  
				mStatMap.get(key).mark();// mark this model(name=key) is called now.
			}else { // normally in Get method, both fStatMap and mStatMap will be populated, but in case no GET but POST here, populate mStatMap 
				String _structFilePath = Lib.getFilePathByName(key, FILE_TYPE.ARFF);//  modelPath.replace(".model", ".arff");
				File _f = new File(_structFilePath);
				if(!_f.exists() || !_f.isFile()) throw new InvalidFileOrUrlException();
				Instances struct = GetInstances(_structFilePath);
				ModelMeta _mm = new ModelMeta(struct.relationName(), Lib.GetModelType(_structFilePath));
				mStatMap.put(key, _mm);
				mStatMap.get(key).mark();
			}
			return this;
		}
		
		// not in memory, need to load from .model. CHECK file invalid first
		File _m = new File(modelPath);
		if(!_m.exists() || !_m.isFile()) throw new InvalidFileOrUrlException();

		// make room if full
		if (mKeysList.size() >= this.size) {  // memory maximum reached, dump one
			System.out.println("========== overflow model memory: used="+this.mUsed+"|memory="+mMemoryMap.size()+"|mkey="+mKeysList.size() + " ["+this.size +"] ==========");
			this.releaseMemory(1, MEMORY_TYPE.modelMemory);
		}

		// Now load file
		Classifier _c = (J48) SerializationHelper.read(modelPath);
		mMemoryMap.put(key, _c);
		mKeysList.add(key);
		this.mUsed = this.mUsed + 1; 
		
		if(mStatMap.containsKey(key)) {
			mStatMap.get(key).mark();// mark this model(name=key) is called now.
		}else {
			// normally in Get method, both fStatMap and mStatMap will be populated, but in case no GET but POST here, populate mStatMap 
			String _structFilePath = Lib.getFilePathByName(key, FILE_TYPE.ARFF);// modelPath.replace(".model", ".arff");
			File _f = new File(_structFilePath);
			if(!_f.exists() || !_f.isFile()) throw new InvalidFileOrUrlException();
			Instances struct = GetInstances(_structFilePath);
			ModelMeta _mm = new ModelMeta(struct.relationName(), Lib.GetModelType(_structFilePath));
			mStatMap.put(key, _mm);
			mStatMap.get(key).mark();

		}
		
		System.out.println("========== new added to model memory: used="+this.mUsed+"|memory="+mMemoryMap.size()+"|mkey="+mKeysList.size() + " ["+this.size +"] ==========");
		return this;//classifier;
	}
	
	/**todo: generalize method 
	 * @throws Exception */
	public IPredictResponse Predict(String modelKey, PredictRequest req) throws InvalidFileOrUrlException, OutOfResourceException,Exception {

		String[] values = req.getData();

        //String[] values = (txtInstance2Prodict.getText()).split(",");
		String _debug = "";
		for(String v : values) {
			_debug += v + ",";
		}
		System.out.println("========== Try to predict: [ " + _debug + " ]");

        int NUMBER_OF_ATTRIBUTES = 5;
        int NUMBER_OF_INSTANCES = 1;

        Attribute Attribute1 = new Attribute("sepallength");
        Attribute Attribute2 = new Attribute("sepalwidth");
        Attribute Attribute3 = new Attribute("petallength");
        Attribute Attribute4 = new Attribute("petalwidth");
        
        List my_nominal_values = new ArrayList (3); 
        my_nominal_values.add("Iris-setosa"); 
        my_nominal_values.add("Iris-versicolor"); 
        my_nominal_values.add("Iris-virginica");
        
        Attribute AttributeClass = new Attribute("classHere", my_nominal_values);

        FastVector fvWekaAttribute = new FastVector(NUMBER_OF_ATTRIBUTES);
        fvWekaAttribute.addElement(Attribute1);
        fvWekaAttribute.addElement(Attribute2);
        fvWekaAttribute.addElement(Attribute3);
        fvWekaAttribute.addElement(Attribute4);
        fvWekaAttribute.addElement(AttributeClass);
       

        Instances newSet = new Instances("NewOne",fvWekaAttribute,NUMBER_OF_INSTANCES);
        newSet.setClassIndex(4); // last one
        
        Instance newSet_instance1 = new DenseInstance(NUMBER_OF_ATTRIBUTES);
        newSet_instance1.setValue(Attribute1, Double.parseDouble(values[0]));
        newSet_instance1.setValue(Attribute2, Double.parseDouble(values[1]));
        newSet_instance1.setValue(Attribute3, Double.parseDouble(values[2]));
        newSet_instance1.setValue(Attribute4, Double.parseDouble(values[3]));
        //newSet_instance1.setValue(AttributeClass, "?");
        
        
        //4.9,3.0,1.4,0.2?
        newSet.add(newSet_instance1);   

		double result = mMemoryMap.get(modelKey).classifyInstance(newSet.instance(0));
		
		System.out.println("========== Predict result: [ " + _debug+ " ] => " + result);

		IPredictResponse res = new PredictResponse(new Prediction(result));

        return res;
        
    }
	
	
}
