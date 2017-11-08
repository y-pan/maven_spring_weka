package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import web.weka.model.ModelMeta;
import weka.core.Instances;

public class Lib {

	public enum FILE_TYPE {ARFF, CSV, MODEL}
    
	/**
	 * Usage: load data (Weka.Instances) from .arff file, could be served as data structure if file only contains @Relation, @Attrubites
	 * */
    public static Instances GetInstances(String inFile) throws IOException{
        BufferedReader reader = null;
        Instances data = null;
        if(inFile.toLowerCase().endsWith(".arff")){
            try {
                reader = new BufferedReader(new FileReader(inFile));
                data = new Instances(reader);
                reader.close();
                data.setClassIndex(data.numAttributes() - 1);

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }finally{
                if(reader != null){
                    reader.close();
                }
            }
        }else if(inFile.toLowerCase().endsWith(".csv")){
            // to do here ...   not sure if need to use .csv, since .arff is more functional & reliable to get @RELATION, @ATTRIBUTE, type, selections(for nominal type) 
        }
        return data;
    }
    
    public static String GetModelType(String filePath) {
    	try {
        	BufferedReader r = new BufferedReader(new FileReader(filePath));
        	String line, result = "";
        	while((line = r.readLine()) != null) {
        		if(line.toUpperCase().matches("^.*%TYPE=.*$")) {
        			result = line.replaceFirst("%TYPE=", "").trim(); // %TYPE=J48 => J48
                	break;
        		}
        	}
        	return result;
    	}catch(Exception e) {
    		return "";
    	}
    }
    
    public static String getFilePathByName(String name, FILE_TYPE type) {
    	String folder = "Content/"; // start from root level
    	switch(type) {
    		case ARFF: return  folder + name + ".arff"; 
    		case MODEL: return folder + name + ".model";
    		default: return "";
    	}
    }
    
    public static String getOldestModel(HashMap<String, ModelMeta> map, ArrayList<String> list) {
    	if(list.size() <= 0) return "";
    	
    	String ok = "";
    	ModelMeta om = null;
    	for(String key : list) {
    		if(ok.equals("") || ok.equals(null)) {
    			ok = key;
    			om = map.get(key);
    		}else {
    			ModelMeta tmp = map.get(key);
    			if(tmp.olderThan(om)) {
    				om = tmp;
    				ok = key;
    			}
    		}
    	}
    	
    	return ok;

    }
}
