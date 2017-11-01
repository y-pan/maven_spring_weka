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
import web.weka.model.Feature;
import web.weka.model.GetFeaturesResponse;
import weka.core.Attribute;
import weka.core.Instances;

@Service
public class ProdictService {

	public int size = 0;
	// ArrayList<Feature> flist = new ArrayList<Feature>();
	ArrayList<String> mk = new ArrayList<String>();
	HashMap<String, ArrayList<Feature>> m = new HashMap<String, ArrayList<Feature>>();

	public ProdictService() {

	}

	public void setSize(int size) {
		this.size = size;
	}

	public GetFeaturesResponse get(String id) {
		return new GetFeaturesResponse(m.get(id));
	}
	
	public ProdictService loadStruct(String key, String structFilePath) throws IOException, InvalidFileException {

		if (mk.contains(key)) {
			System.out.println("--------- in memory. Total: "+mk.size() + " out of "+this.size +"-------------");
			return this;
		}

		if (mk.size() >= this.size) {
			System.out.println("========== overflow: "+mk.size() + " out of "+this.size +" , removed the first add new ==========");
			m.remove(mk.get(0));
			mk.remove(0);
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
		System.out.println("--------- new added. Total: "+mk.size() + " out of "+this.size +"-------------");
		return this;

	}
}
