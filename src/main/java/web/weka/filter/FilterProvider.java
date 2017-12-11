package web.weka.filter;

import weka.core.Instances;
import weka.filters.Filter;

import weka.filters.unsupervised.instance.*;//unsupervised.instance.imagefilter;
public class FilterProvider {

	// TODO:need to find weka's imagefilter, either maven repo or jar first (headache for external jar, haven't managed to push to heroku with ext jar~~~) 
//	public static Instances FilterInstances(Instances dataset, String filterName) {
//		switch(filterName) {
//			case FilterTypes.ColorLayoutFilter: 
//				ColorLayoutFilter _clf = new ColorLayoutFilter();
//				return Filter.useFilter(dataset, arg1)
//			break; 
//			default:break;
//		}
//	}
}
