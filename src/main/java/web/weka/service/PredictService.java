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
import web.weka.model.AttributeTypes;
import web.weka.model.Feature;
import web.weka.model.GetFeaturesResponse;
import web.weka.model.IPredictResponse;
import web.weka.model.IPrediction;
import web.weka.model.ModelMeta;
import web.weka.model.ModelTypes;
import web.weka.model.PredictRequest;
import web.weka.model.PredictResponse;
import web.weka.model.Prediction;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.SMO;
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
					fMemoryMap.remove(fKeysList.get(0));
					fKeysList.remove(0);
				}else {
					fMemoryMap.remove(oldestKey);
					fKeysList.remove(oldestKey);
				}
				this.fUsed = this.fUsed - 1;
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
				f.setType(AttributeTypes.NUMERIC);
			} else if (att.isDate()) {
				f.setType(AttributeTypes.DATE);
			} else if (att.isString()) {
				f.setType(AttributeTypes.STRING);
			} else if (att.isNominal()) {
				f.setType(AttributeTypes.NOMINAL);

				List<String> optionList = new ArrayList<String>();
				Enumeration values = att.enumerateValues();

				while (values.hasMoreElements()) {
					optionList.add(values.nextElement().toString());
				}
				String[] optionArray = new String[optionList.size()];
				optionArray = optionList.toArray(optionArray);
				f.setOptions(optionArray);
			} else {
				f.setType(AttributeTypes.UNKNOWN);
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
		// front-end will also send in modelType in request like : { "modelType" : "J48", "data":[1,1,6,4] }, but I think better manage that by server itself, so we only need the least data (the data array) going between server and client
		Classifier _c = getModel(mStatMap.get(key).getModelType(), modelPath); // good to know we also have : mStatMap.get(key).getModelRelation(), not for backend, but for frontend user to get some idea about model
		System.out.println("k="+key);
		System.out.println("t="+mStatMap.get(key).getModelType());
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
	
	private Classifier getModel(String modelType, String modelPath) throws Exception {
		switch(modelType.toUpperCase()) {
			case ModelTypes.J48 : return (J48) SerializationHelper.read(modelPath);
			case ModelTypes.SMO : return (SMO) SerializationHelper.read(modelPath);
			case ModelTypes.LibSVM: return (LibSVM) SerializationHelper.read(modelPath);
			default: return null;
		}
	}
	
	/**todo: generalize method 
	 * @throws Exception */
	public IPredictResponse Predict(String key, PredictRequest req) throws InvalidFileOrUrlException, OutOfResourceException,Exception {
		
		if(!fMemoryMap.containsKey(key)) {
			prepareFeatures(key, Lib.getFilePathByName(key,FILE_TYPE.ARFF));
		}
		ArrayList<Feature> features = fMemoryMap.get(key); // features contains all features and class
		String[] values = req.getData();  // here no class in values, but only feature values
		Instances _ins = createInstances(features, values); // model + values => new instance
		double result = mMemoryMap.get(key).classifyInstance(_ins.instance(0));
		System.out.println("========== Predict result: " + result);
		IPredictResponse res = new PredictResponse(new Prediction(result));
        return res;
        
    }
	
	/**create 1 Instances containing 1 Instance for model to do prediction, base on Feature (recipe), and featureValues (material, sent from frontend, doesn't contain class value) */
	private Instances createInstances(ArrayList<Feature> features, String[] featureValues) {
		
		int numOfAtt = featureValues.length + 1; // NUMBER_OF_ATTRIBUTES include class
		int numOfIns = 1;   					// 1, unless for an arff file
		String _debug = "";
		for(String _v : featureValues) {
			_debug += _v + ",";
		}
		System.out.println("========== Try to predict: [ " + _debug + " ]");
		
		ArrayList<Attribute> _aList = new ArrayList<>();
		for(int i=0; i<features.size(); i++) {
			Feature f = features.get(i); // could be feature or class	
			if(f.getType() == AttributeTypes.NOMINAL) { // nominal  
				int olen = f.getOptions().length;
				List _vs = new ArrayList (olen); //olen
				for(int j=0; j<olen; j++) {
					_vs.add(f.getOptions()[j].toString());
				}
				_aList.add(new Attribute("a_n"+i, _vs));
			}else {		// not nominal
				_aList.add(new Attribute("a"+i));
			}
		}

        FastVector _fv = new FastVector(numOfAtt);
        _fv.addAll(_aList);
        Instances _ins = new Instances("i",_fv, numOfIns);
        _ins.setClassIndex(numOfAtt-1); // last one, including class totally there are 5, index starts from 0
        Instance _in = new DenseInstance(numOfAtt);
        for(int i=0; i< numOfAtt - 1; i++) {
        	_in.setValue(_aList.get(i), Double.parseDouble(featureValues[i]));
        }
        //_in.setValue(AttributeClass, "?");
         _ins.add(_in);   
        return _ins;
	}
	
	
}
