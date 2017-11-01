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

import web.weka.exceptions.InvalidFileException;
import web.weka.exceptions.OutOfResourceException;
import web.weka.model.Feature;
import web.weka.model.GetFeaturesResponse;
import weka.core.Attribute;
import weka.core.Instances;

@Service
public class ProdictService {

	private int size = 5; // default initial size 5
	private int used = 0;
	ArrayList<String> mk = new ArrayList<String>();
	HashMap<String, ArrayList<Feature>> m = new HashMap<String, ArrayList<Feature>>();

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
			System.out.println("removing memory for:" + mk.get(0));
			m.remove(mk.get(0));
			mk.remove(0);
			this.used = this.used - 1;
		}
		
	}
	
	public GetFeaturesResponse get(String id) {
		return new GetFeaturesResponse(m.get(id));
	}
	
	public ProdictService loadStruct(String key, String structFilePath) throws IOException, InvalidFileException, OutOfResourceException {

		if(this.size<=0) throw new OutOfResourceException();
		
		if (mk.contains(key)) {
			System.out.println("--------- in memory: used="+this.used+"|memory="+m.size()+"|mkey="+mk.size() + " ["+this.size +"] -------------");
			return this;
		}

		if (mk.size() >= this.size) {
			System.out.println("--------- overflow: used="+this.used+"|memory="+m.size()+"|mkey="+mk.size() + " ["+this.size +"] -------------");
			this.releaseMemory(1);
		}

		File _f = new File(structFilePath);
		if (!_f.exists() || !_f.isFile() || !_f.canRead() || !_f.getName().toLowerCase().endsWith(".arff"))
			throw new InvalidFileException("Invalid URL");

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
		m.put(key, flist);
		mk.add(key);
		this.used = this.used + 1;
		System.out.println("--------- new added: used="+this.used+"|memory="+m.size()+"|mkey="+mk.size() + " ["+this.size +"] -------------");
		return this;

	}
}
