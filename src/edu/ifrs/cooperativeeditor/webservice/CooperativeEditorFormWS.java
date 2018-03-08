/**
 * @license
 * Copyright 2018, Rodrigo Prestes Machado and Lauro Correa Junior
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ifrs.cooperativeeditor.webservice;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.ifrs.cooperativeeditor.dao.DataObject;
import edu.ifrs.cooperativeeditor.mail.CooperativeEditorMail;
import edu.ifrs.cooperativeeditor.model.MyDateTypeAdapter;
import edu.ifrs.cooperativeeditor.model.Production;
import edu.ifrs.cooperativeeditor.model.Rubric;
import edu.ifrs.cooperativeeditor.model.RubricProductionConfiguration;
import edu.ifrs.cooperativeeditor.model.User;
import edu.ifrs.cooperativeeditor.model.UserProductionConfiguration;

@Path("/form")
@Stateless
public class CooperativeEditorFormWS {

	@EJB
	private DataObject dao;

	@Context
	private HttpServletRequest request;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.TEXT_XML, MediaType.WILDCARD, MediaType.TEXT_PLAIN })
	@Path("/peoplesuggestion/{partemail}")
	public String peopleSuggestion(@PathParam("partemail") String partEmail) {

		StringBuilder json = new StringBuilder();

		if (!"".equals(partEmail)) {
			
			List<User> result = dao.getUsersByPartEmail(partEmail);

			StringBuilder strReturn = new StringBuilder();
			strReturn.append("[ ");

			for (User user : result) {
				strReturn.append("{");
				strReturn.append("\"id\":");
				strReturn.append("\"" + user.getId() + "\",");
				strReturn.append("\"name\":");
				if(user.getName() == null) {
					strReturn.append("\"" + user.getEmail() + "\"");
				}else {
					strReturn.append("\"" + user.getName() + "\"");
				}				
				strReturn.append("}");
				strReturn.append(",");

			}

			json.append(strReturn.substring(0, strReturn.length() - 1));
			json.append("]");

		}
		System.out.println("Retorno webservice peoplesuggestion " + json.toString());
		return json.toString();

	}
	
	/**
	 * rubric person search by the goal
	 * 
	 * @param partobjective
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.TEXT_XML, MediaType.WILDCARD, MediaType.TEXT_PLAIN })
	@Path("/rubricsuggestion/{partobjective}")
	public String rubricSuggestion(@PathParam("partobjective") String partObjective) {

		StringBuilder json = new StringBuilder();

		if (!"".equals(partObjective)) {
			
			List<Rubric> result = dao.getRubricsByPartObjctive(partObjective);

			StringBuilder strReturn = new StringBuilder();
			strReturn.append("[ ");

			for (Rubric rubric : result) {
				strReturn.append("{");
				strReturn.append("\"id\":");
				strReturn.append("\"" + rubric.getId() + "\",");
				strReturn.append("\"name\":");
				strReturn.append("\"" + rubric.getObjective().replace('"', '\'') + "\"");
				strReturn.append("}");
				strReturn.append(",");

			}

			json.append(strReturn.substring(0, strReturn.length() - 1));
			json.append("]");

		}
		System.out.println("Retorno webservice rubricsuggestion" + json.toString());
		return json.toString();

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.TEXT_XML, MediaType.WILDCARD, MediaType.TEXT_PLAIN })
	@Path("/getrubric/{rubricId}")
	public String getrubric(@PathParam("rubricId") Long rubricId) {

		StringBuilder json = new StringBuilder();

		if (rubricId != null) {
			Rubric rubric = dao.getRubric(rubricId);
			json.append(rubric.toString());
		}
		System.out.println("Retorno webservice getrubric " + json.toString());
		return json.toString();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.TEXT_XML, MediaType.WILDCARD, MediaType.TEXT_PLAIN })
	@Path("/getproduction/{productionId}")
	public String getProduction(@PathParam("productionId") Long productionId) {
		
		Production production = dao.getProduction(productionId);

		System.out.println("Retorno webservice getproduction " + production.toJson());
		return production.toJson();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/partialSubmit")
	public String partialSubmit(String jsonMessage) {

		System.out.println(jsonMessage);

		Gson gson = new GsonBuilder().registerTypeAdapter(Calendar.class, new MyDateTypeAdapter()).create();
		Production production = new Production();
		production = gson.fromJson(jsonMessage, Production.class);

		User user = new User((long) request.getSession().getAttribute("userId"));
		production.setOwner(user);

		if (production.getId() == null) {
			dao.persistProduction(production);
		} else {
			dao.mergeProduction(production);
		}

		System.out.println("Retorno webservice partialSubmit " + production.getId());

		return "{ \"id\": \"" + production.getId().toString() + "\" }";
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/salveProduction")
	public String salveProduction(String jsonMessage) {

		System.out.println(jsonMessage);
		Gson gson = new GsonBuilder().registerTypeAdapter(Calendar.class, new MyDateTypeAdapter()).create();
		Production production = new Production();
		production = gson.fromJson(jsonMessage, Production.class);

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = new BigInteger(1, md.digest(production.getObjective().getBytes())).toString(16);

		production.setUrl(url);

		User user = new User((long) request.getSession().getAttribute("userId"));
		production.setOwner(user);

		if (production.getId() == null) {
			dao.persistProduction(production);
		} else {
			dao.mergeProduction(production);
		}
		
		production = dao.getProduction(production.getId());
		
		CooperativeEditorMail mail = new CooperativeEditorMail();
		
		String addressUser = "";
		for (UserProductionConfiguration configuration : dao.getUserProductionConfigurationByProductionId(production.getId())) {
			addressUser = configuration.getUser().getEmail()+",";
		}
		mail.toUsers(addressUser);
		
		mail.setText("http://localhost:8080/CooperationEditor/editor/"+production.getUrl());
		
		mail.send();

		System.out.println("Retorno webservice salveProduction { \"isProductionValid\":" + true + ",\"url\" : \""
				+ production.getUrl() + "\"}");

		return "{ \"isProductionValid\":" + true + ",\"url\" : \"" + production.getUrl() + "\"}";
	}
	

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/rubricProductionConfiguration")
	public String rubricProductionConfiguration(String jsonMessage) {

		System.out.println("Entrada webservice rubricProductionConfiguration " + jsonMessage);

		Gson gson = new Gson();
		RubricProductionConfiguration configuration = new RubricProductionConfiguration();
		configuration = gson.fromJson(jsonMessage, RubricProductionConfiguration.class);
		
		System.out.println(configuration);
		System.out.println(configuration.getRubric());
		
		
		User user = dao.getUser((long) request.getSession().getAttribute("userId"));

		if (configuration.getProduction() == null) {
			Production production = new Production();
			production.setOwner(user);
			configuration.setProduction(production);
			dao.persistProduction(configuration.getProduction());
		}else {
			Long idProduction = configuration.getProduction().getId();
			configuration.setProduction(dao.getProduction(idProduction));
		}
		
		if (configuration.getRubric().isIdNull()) {
			configuration.getRubric().addOwner(user);
			dao.persistRubric(configuration.getRubric());
		}

		if (configuration.getId() == null) {
			dao.persistRubricProductionConfiguration(configuration);
		} else {
			configuration = dao.mergeRubricProductionConfiguration(configuration);
		}
		
		System.out.println("Retorno webservice rubricProductionConfiguration " + configuration.toString());

		return configuration.toString();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/userProductionConfiguration")
	public String userProductionConfiguration(String jsonMessage) {

		System.out.println("userProductionConfiguration " + jsonMessage);

		Gson gson = new Gson();
		UserProductionConfiguration configuration = new UserProductionConfiguration();
		configuration = gson.fromJson(jsonMessage, UserProductionConfiguration.class);

		if (configuration.getProduction() == null) {
			Production production = new Production();
			User user = new User((long) request.getSession().getAttribute("userId"));
			production.setOwner(user);
			configuration.setProduction(production);
			dao.persistProduction(production);
		}
		
		System.out.println(configuration.getUser());

		if (configuration.getUser().isIdNull()) {
			User user = dao.getUser(configuration.getUser().getEmail());
			if(user != null) {
				configuration.setUser(user);
			}else {
				configuration.setUser(dao.persistUser(configuration.getUser()));
			}
		}else {
			System.out.println("id nao null");
			configuration.setUser(dao.getUser(configuration.getUser().getId()));
		}

		if (configuration.getId() == null) {
			dao.persistUserProductionConfiguration(configuration);
		} else {
			configuration = dao.mergeUserProductionConfiguration(configuration);
		}

		System.out.println(configuration.toString());

		return configuration.toString();

	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.TEXT_XML, MediaType.WILDCARD, MediaType.TEXT_PLAIN })
	@Path("/deleteRubric/{rubricId}")
	public String deleteRubric(@PathParam("rubricId") Long rubricId) {

		System.out.println("deleRubrc " + rubricId);

		List<RubricProductionConfiguration> result = dao.getRubricProductionConfigurationByRubricId(rubricId);

		for (RubricProductionConfiguration configuration : result) {
			dao.removeRubricProductionConfiguration(configuration);
		}

		Rubric rubric = dao.getRubric(rubricId);
		if (rubric != null) {
			rubric.setId(rubricId);
			dao.removeRubric(rubric);
		}

		return "\"OK\"";
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({ MediaType.TEXT_XML, MediaType.WILDCARD, MediaType.TEXT_PLAIN })
	@Path("/disconnectRubric/{rubricProductionConfigurationId}")
	public String disconnectRubric(@PathParam("rubricProductionConfigurationId") Long configurationId) {

		System.out.println("disconnectRubric " + configurationId);

		RubricProductionConfiguration configuration = dao.getRubricProductionConfiguration(configurationId);

		if (configuration != null) {
			dao.removeRubricProductionConfiguration(configuration);
		}

		return "\"OK\"";
	}

}
