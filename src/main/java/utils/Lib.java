package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;

public class Lib {

	public enum FileType {ARFF, CSV}
    
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
                
            } catch(Exception e){
                
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
//                	System.out.println(line + " => " + result);
                	break;
        		}
        	}
        	return result;
    	}catch(Exception e) {
    		return "";
    	}

    }
}
