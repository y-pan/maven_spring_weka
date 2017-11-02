package web.weka.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import utils.Lib.FILE_TYPE;
import utils.Lib;
import web.weka.exceptions.InvalidFileOrUrlException;
import web.weka.exceptions.OutOfResourceException;
import web.weka.model.GetFeaturesResponse;
import web.weka.model.IPredictResponse;
import web.weka.model.PredictRequest;
import web.weka.model.PredictResponse;
import web.weka.service.PredictService;

@RestController
public class PredictController {

	@Autowired
	PredictService service;
		
	@RequestMapping("/predict/{id}")
	public GetFeaturesResponse get(@PathVariable("id") String id){  // id : fileName without extension, like "iris" from "iris.arff", same with model name like iris.model 
		try {
			return service.prepareFeatures(id, Lib.getFilePathByName(id,FILE_TYPE.ARFF)).get(id);
		} catch (InvalidFileOrUrlException | OutOfResourceException e) {
			return new GetFeaturesResponse(e.getMessage());
		} catch (Exception e) {
			return new GetFeaturesResponse("Unknown error occurred");
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/predict/{id}")
	public IPredictResponse Post(@PathVariable("id") String id, @RequestBody PredictRequest req){  // id : filename without extension, like "iris" from "iris.model" 
		try {

			return service.prepareModel(id, Lib.getFilePathByName(id,FILE_TYPE.MODEL)).Predict(id, req);
		}catch(InvalidFileOrUrlException | OutOfResourceException ex) {
			return new PredictResponse(ex.getMessage());
		}catch (Exception e) {
			return new PredictResponse("Unknown error occurred");
		}
	}
	
	
	// [ admin user only ]
	@RequestMapping(method=RequestMethod.POST, value="/predict/set/{size}")
	public void set(@PathVariable("size") int size) {
		System.out.println("ADMIN set size to: " + size);
		service.setSize(size); // determines how many models/featureSets will be remembered, instead of load from xxx.arff, xxx.model file every time
		
	}

	
}
