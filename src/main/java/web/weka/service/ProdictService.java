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

import utils.Lib;
import web.weka.exceptions.InvalidFileException;
import web.weka.exceptions.OutOfResourceException;
import web.weka.model.Feature;
import web.weka.model.GetFeaturesResponse;
import web.weka.model.ModelMeta;
import weka.core.Attribute;
import weka.core.Instances;

@Service
public class ProdictService {

	private int size = 5; // default initial size 5
	private int used = 0;
	HashMap<String, ModelMeta> mStat = new HashMap<String, ModelMeta>(); // model stats
	ArrayList<String> mKeys = new ArrayList<String>();
	
	HashMap<String, ArrayList<Feature>> memory = new HashMap<String, ArrayList<Feature>>();

	public ProdictService() {

	}


	public synchronized void setSize(int size) {
		if(size < 0) return; // <0, invalid, ignore; >=0 up-scale/down-scale service, 0 -> stop service 
		
		this.size = size;
		if(this.used > this.size) {
			releaseMemory(this.used - this.size);   // trigger release memory
		}
	}
	
	public void releaseMemory(int releaseCount) {
		if(releaseCount <=0 ) return;
		// first in first out approach
		// todo: may need to consider remove the least active one, in modelMeta: useCount, lastCalled time. Remove in this order: lastCalled -> useCount -> firstInFirstOut
		for(int i = 0; i<releaseCount; i++) {
			System.out.println("removing memory for:" + mKeys.get(0));
			memory.remove(mKeys.get(0));
			mStat.remove(mKeys.get(0));
			mKeys.remove(0);
			this.used = this.used - 1;
		}
		System.out.println("after rm:"+ mKeys.toString());
	}
	
	public GetFeaturesResponse get(String id) {
		return new GetFeaturesResponse(memory.get(id), mStat.get(id).getModelRelation() ,mStat.get(id).getModelType());
	}
	
	public ProdictService loadStruct(String key, String structFilePath) throws IOException, InvalidFileException, OutOfResourceException {

		if(this.size<=0) throw new OutOfResourceException();
		
		if (mKeys.contains(key)) {  // memory has this model's features
			System.out.println("--------- in memory: used="+this.used+"|memory="+memory.size()+"|mkey="+mKeys.size() + " ["+this.size +"] -------------");
			mStat.get(key).mark();// mark this model(name=key) is called now.
			return this;
		}

		if (mKeys.size() >= this.size) {  // memory maximum reached, dump one
			System.out.println("--------- overflow: used="+this.used+"|memory="+memory.size()+"|mkey="+mKeys.size() + " ["+this.size +"] -------------");
			this.releaseMemory(1);
		}

		// memory doesn't have it, load from .arff file
		File _f = new File(structFilePath);
		if (!_f.exists() || !_f.isFile() || !_f.canRead() || !_f.getName().toLowerCase().endsWith(".arff")) { 
			throw new InvalidFileException();
		}

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
		memory.put(key, flist);
		mKeys.add(key);
		ModelMeta _mm = new ModelMeta(struct.relationName(), Lib.GetModelType(structFilePath));
		mStat.put(key, _mm); // key is from URL path-value, could be filename(or part, or relevant to it if file encrypted) of .arff file  
		this.used = this.used + 1;
		System.out.println("--------- new added: used="+this.used+"|memory="+memory.size()+"|mkey="+mKeys.size() + " ["+this.size +"] -------------");
		return this;

	}
}
