package web.weka.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import web.weka.exceptions.InvalidFileException;
import web.weka.model.GetFeaturesResponse;
import web.weka.service.ProdictService;

@RestController
public class PredictController {

	@Autowired
	ProdictService service;
		
	@RequestMapping("/predict/{id}")
	public GetFeaturesResponse get(@PathVariable("id") String id){
		service.setSize(5);
		try {
			return service.loadStruct(id, "./"+id+".arff").get(id);
		} catch (IOException | InvalidFileException e) {

			return new GetFeaturesResponse(e.getMessage());
		}
	}
	

	
}
