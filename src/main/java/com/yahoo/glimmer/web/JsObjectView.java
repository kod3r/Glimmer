package com.yahoo.glimmer.web;

import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Triple;
import org.springframework.web.servlet.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @{View} that renders an Object as JSON or a JavaScript
 * callback.
 * The Object is rendered as a callback only when the request has a 
 * parameter CALLBACK_PARAMETER defined.
 * 
 * This class uses Gson which is configured to render instances of the following class:
 * java.lang.CharSequence
 * org.semanticweb.yars.nx.Node
 * org.semanticweb.yars.nx.Triple
 * 
 * @author tep@yahoo-inc.com
 */
public class JsObjectView implements View {
    private static final String CALLBACK_PARAMETER = "callback";
    
    /** GsonBuilder object */
    private static Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat(DateFormat.LONG)
	    .registerTypeAdapter(CharSequence.class, new JsonSerializer<Object>() {
		public JsonElement serialize(Object object, Type arg1, JsonSerializationContext arg2) {
		    if (object instanceof CharSequence) {
			return new JsonPrimitive(((CharSequence) object).toString());
		    }
		    return null;
		}
	    }).registerTypeAdapter(Node.class, new JsonSerializer<Object>() {
		public JsonElement serialize(Object object, Type arg1, JsonSerializationContext arg2) {
		    if (object instanceof Node) {
			return new JsonPrimitive(((Node) object).toString());
		    }
		    return null;
		}
	    }).registerTypeAdapter(Triple.class, new JsonSerializer<Object>() {
		public JsonElement serialize(Object object, Type arg1, JsonSerializationContext arg2) {
		    if (object instanceof Triple) {
			JsonObject jo = new JsonObject();
			jo.addProperty("subject", ((Triple) object).getSubject().toString());
			jo.addProperty("predicate", ((Triple) object).getPredicate().toString());
			jo.addProperty("object", ((Triple) object).getObject().toString());
			return jo;
		    }
		    return null;
		}
	    }).create();

    @Override
    public String getContentType() {
	// Setting charset= means that response.getWriter() will return a Writer that writes in that charset.
	return "text/javascript; charset=UTF-8";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
	Object object = model.get(QueryController.OBJECT_KEY);
	if (object == null) {
	    throw new IllegalArgumentException("Model does not contain an object!");
	}
	
	response.setContentType(getContentType());
	PrintWriter writer = response.getWriter();
	
	String callback = request.getParameter(CALLBACK_PARAMETER);
	if (callback != null) {
	    writer.write(callback);
	    writer.write('(');
	    writer.write(gson.toJson(object));
	    writer.write(");");
	} else {
	    writer.write(gson.toJson(object));
	}
    }
}