package com.smile.passionistar.ch0;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {
	
	public String jsonParsingForText(String s) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject)parser.parse(s);
		return jsonObj.get("text").toString();
	}
	
	public String jsonParsingForImage(String s) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject)parser.parse(s);
		return jsonObj.get("Image").toString();
	}
	
	public String jsonParsingForDate(String s) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject)parser.parse(s);
		return jsonObj.get("Date").toString();
	}
	
}
