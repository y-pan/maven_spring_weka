package web.weka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import web.weka.model.PredictRequest;
import web.weka.service.StorageService;

@RestController
public class FileUploadController {
	@Autowired
	StorageService storageService;

	@RequestMapping("/upload")
	public String get(){
		return "here to get uploaded images";
	}
	@RequestMapping(method=RequestMethod.POST,value="/upload/{file}")
	public String post(@RequestParam("file") MultipartFile file, @RequestBody PredictRequest req){
		System.out.println("here to upload images: " + file.getSize() +", "+ file.getOriginalFilename() +", "+ file.getName() +", "+ file.getContentType() );
		return "here to upload images: " + file.getSize() +", "+ file.getOriginalFilename() +", "+ file.getName() +", "+ file.getContentType() ; 
	}


}