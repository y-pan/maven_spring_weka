package web.weka.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import web.weka.exceptions.InvalidFileException;
import web.weka.exceptions.OutOfResourceException;
import web.weka.model.GetFeaturesResponse;
import web.weka.service.ProdictService;

@RestController
public class PredictController {

	
	@Autowired
	ProdictService service;
		
	@RequestMapping("/predict/{id}")
	public GetFeaturesResponse get(@PathVariable("id") String id){
		//service.setSize(serviceSize); // 50 model feature set will be remembered, instead of load from xxx.arff file every time
		try {
			return service.loadStruct(id, "./"+id+".arff").get(id);
		} catch (IOException | InvalidFileException | OutOfResourceException e) {

			return new GetFeaturesResponse(e.getMessage());
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/predict/set/{size}")
	public void set(@PathVariable("size") int size) {
		System.out.println("post to set size to: " + size);
		service.setSize(size); // determines how many models' featureSet will be remembered, instead of load from xxx.arff file every time
		
	}

	
}
