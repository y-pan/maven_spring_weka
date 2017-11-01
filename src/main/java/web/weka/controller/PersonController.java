package web.weka.controller;

import java.util.Hashtable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import web.weka.model.Person;
import web.weka.service.PersonService;

@RestController
public class PersonController {

	@Autowired
	PersonService ps;
	
	@RequestMapping("/persons/all")             //http://localhost:8080/persons/all
	public Hashtable<String, Person> getAll(){
		return ps.getAll();
	}
	
	@RequestMapping("/persons/{id}")       //http://localhost:8080/persons/1
	public Person getPerson(@PathVariable("id") String id) {
		return ps.getPerson(id);
	}
}
