package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.StatementImpl;

import dataStructures.tuple.Couple;
import javafx.util.Pair;

public class MapaModel {
	Model model;
	long revision;

	public enum NodeType {
		Open,
		Closed
	}

	public enum MineralType {
		Gold,
		Diamond,
	}

	public enum AgentType {
		Explorer,
		Storage,
		Recollector,
	}

	public MapaModel(Model model) {
		this.model = model;
		this.revision = 0;
	}

	static Pattern patternIdCell = Pattern.compile("^http://mapa#Instance_(.+?)_cell$");
	static Pattern patternIdResource = Pattern.compile("^http://mapa#Instance_(.+?)_resource$");
	static Pattern patternIdAgent = Pattern.compile("^http://mapa#Instance_(.+?)_agent$");

	private String mapa(String hastag) {
		return "http://mapa#" + hastag;
	}

	private Resource getMineral(String id) {
		return model.createResource(mapa("Instance_" + id + "_resource"));
	}

	private Resource getAgent(String id) {
		return model.createResource(mapa("Instance_" + id + "_agent"));
	}

	private Resource getCell(String id) {
		return model.createResource(mapa("Instance_" + id + "_cell"));
	}

	private void addCheckStmt(StatementImpl stmt) {
		if (!model.contains(stmt)) {
			// System.out.println("Adding: " + stmt);
			++revision;
			model.add(stmt);
		}
	}

	private void updateEntityTime(Resource resource, long time) {
		model.addLiteral(resource, model.getProperty(mapa("lastUpdate")), time);
		// System.out.println("Updating: " + resource);
	}

	private void updateEntityTime(Resource resource) {
		// System.out.println("Updating: " + resource + " time " +
		// System.currentTimeMillis());
		updateEntityTime(resource, System.currentTimeMillis());
	}

	// Number of changes since last time
	public long revision() {
		return this.revision;
	}

	public void addMineral(String id, MineralType mineral) {
		addCheckStmt(new StatementImpl(
				getMineral(id),
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				mineral == MineralType.Gold ? model.getResource(mapa("Gold")) : model.getResource(mapa("Diamond"))));

	}

	public void addMineralPos(String idMineral, String idNode) {
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + idMineral + "_resource mapa:LocatedAt ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			ArrayList<StatementImpl> toRemove = new ArrayList<StatementImpl>();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					if (!matcher.group(1).equals(idNode))
						toRemove.add(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("LocatedAt")),
								getCell(matcher.group(1))));
					else {
						updateEntityTime(getMineral(idMineral));
						return;
					}
				}
			}
			qe.close();
			for (StatementImpl toRemoveStmt : toRemove) {
				model.remove(toRemoveStmt);
			}
		}
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Agent where {" +
							"  mapa:Instance_" + idMineral + "_resource mapa:CarriedBy ?Agent ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			ArrayList<StatementImpl> toRemove = new ArrayList<StatementImpl>();
			while (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdAgent.matcher(entry.get("Agent").toString());
				if (matcher.find()) {
					toRemove.add(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("CarriedBy")),
							getAgent(matcher.group(1))));
				}
			}
			qe.close();
			for (StatementImpl toRemoveStmt : toRemove) {
				model.remove(toRemoveStmt);
			}
		}
		addCheckStmt(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("LocatedAt")), getCell(idNode)));
		updateEntityTime(getMineral(idMineral));
	}

	public void addMineralCarried(String idMineral, String idAgent) {
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + idMineral + "_resource mapa:LocatedAt ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			ArrayList<StatementImpl> toRemove = new ArrayList<StatementImpl>();
			while (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					toRemove.add(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("LocatedAt")),
							getCell(matcher.group(1))));
				}
			}
			qe.close();
			for (StatementImpl toRemoveStmt : toRemove) {
				model.remove(toRemoveStmt);
			}
		}
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Agent where {" +
							"  mapa:Instance_" + idMineral + "_resource mapa:CarriedBy ?Agent ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			ArrayList<StatementImpl> toRemove = new ArrayList<StatementImpl>();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdAgent.matcher(entry.get("Agent").toString());
				if (matcher.find()) {
					if (!matcher.group(1).equals(idAgent))
						toRemove.add(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("CarriedBy")),
								getAgent(matcher.group(1))));
					else {
						updateEntityTime(getMineral(idMineral));
						return;
					}
				}
			}
			qe.close();
			for (StatementImpl toRemoveStmt : toRemove) {
				model.remove(toRemoveStmt);
			}
		}
		addCheckStmt(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("CarriedBy")), getAgent(idAgent)));
		updateEntityTime(getMineral(idMineral));
	}

	public void addAdjancency(String node1id, String node2id) {
		Resource node1 = getCell(node1id);
		Resource node2 = getCell(node2id);
		addCheckStmt(new StatementImpl(node1, model.getProperty(mapa("Adjacent")), node2));
		addCheckStmt(new StatementImpl(node2, model.getProperty(mapa("Adjacent")), node1));
	}

	public void addNode(String id, NodeType type) {
		Resource cell = getCell(id);
		boolean alreadyOpen = model.contains(new StatementImpl(cell,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Open"))));
		boolean alreadyClosed = model.contains(new StatementImpl(cell,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Closed"))));

		if (type == NodeType.Open && (alreadyClosed || alreadyOpen)) {
			return;
		} else if (type == NodeType.Closed && alreadyClosed) {
			return;
		} else if (type == NodeType.Closed && alreadyOpen) {
			model.remove(new StatementImpl(
					cell,
					model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
					model.getResource(mapa("Open"))));
		}
		addCheckStmt(new StatementImpl(
				cell,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				type == NodeType.Open ? model.getResource(mapa("Open")) : model.getResource(mapa("Closed"))));
	}

	public void addNodeWindy(String id) {
		addCheckStmt(new StatementImpl(
				getCell(id),
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				model.getResource(mapa("Windy"))));
	}

	public void addAgent(String agentName, AgentType agent) {
		Resource classOfAgent = null;
		switch (agent) {
			case Explorer:
				classOfAgent = model.getResource(mapa("Explorer"));
				break;
			case Recollector:
				classOfAgent = model.getResource(mapa("Recollector"));
				break;
			case Storage:
				classOfAgent = model.getResource(mapa("Storage"));
				break;
			default:
				break;
		}
		StatementImpl stmt = new StatementImpl(
				getAgent(agentName),
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				classOfAgent);
		addCheckStmt(stmt);
		updateEntityTime(getAgent(agentName));
	}

	public AgentType getAgentType(String agentName) {
		Resource agent = getAgent(agentName);
		if (model.contains(new StatementImpl(agent, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				model.getResource(mapa("Explorer"))))) {
			return AgentType.Explorer;
		}
		if (model.contains(new StatementImpl(agent, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				model.getResource(mapa("Recollector"))))) {
			return AgentType.Recollector;
		}
		return AgentType.Storage;
	}

	public MineralType getMineralType(String mineralName) {
		Resource mineral = getMineral(mineralName);
		if (model.contains(new StatementImpl(mineral, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				model.getResource(mapa("Gold"))))) {
			return MineralType.Gold;
		}
		return MineralType.Diamond;
	}

	public void addAgentPos(String agentName, String id) {
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + agentName + "_agent mapa:LocatedAt ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			ArrayList<StatementImpl> toRemove = new ArrayList<StatementImpl>();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					if (!matcher.group(1).equals(id))
						toRemove.add(new StatementImpl(getAgent(agentName), model.getProperty(mapa("LocatedAt")),
								getCell(matcher.group(1))));
					else {
						updateEntityTime(getAgent(agentName));
						return;
					}
				}
			}
			qe.close();
			for (StatementImpl toRemoveStmt : toRemove) {
				model.remove(toRemoveStmt);
			}
		}
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Agent where {" +
							"  ?Agent mapa:LocatedAt mapa:Instance_" + id + "_cell ;" +
							"  a ?Type ." +
							"  FILTER(?Type IN (mapa:Explorer, mapa:Recollector, mapa:Storage) )" +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			ArrayList<StatementImpl> toRemove = new ArrayList<StatementImpl>();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdAgent.matcher(entry.get("Agent").toString());
				if (matcher.find()) {
					toRemove
							.add(new StatementImpl(getAgent(matcher.group(1)), model.getProperty(mapa("LocatedAt")), getCell(id)));
				}
			}
			qe.close();
			for (StatementImpl toRemoveStmt : toRemove) {
				model.remove(toRemoveStmt);
			}
		}
		addCheckStmt(new StatementImpl(getAgent(agentName), model.getProperty(mapa("LocatedAt")), getCell(id)));
		updateEntityTime(getAgent(agentName));
	}

	public void addObectiveLocation(String agentName, String id) {
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + agentName + "_agent mapa:IntendsToWalkTo ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			ArrayList<StatementImpl> toRemove = new ArrayList<StatementImpl>();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					if (!matcher.group(1).equals(id))
						toRemove.add(new StatementImpl(getAgent(agentName), model.getProperty(mapa("IntendsToWalkTo")),
								getCell(matcher.group(1))));
					else {
						updateEntityTime(getAgent(agentName));
						return;
					}
				}
			}
			qe.close();
			for (StatementImpl toRemoveStmt : toRemove) {
				model.remove(toRemoveStmt);
			}
		}
		addCheckStmt(new StatementImpl(getAgent(agentName), model.getProperty(mapa("IntendsToWalkTo")), getCell(id)));
		updateEntityTime(getAgent(agentName));
	}

	public Map<String, Long> getUpdatetimesAgents() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Agent ?Update where {" +
						" ?Agent a ?Type ;" +
						"  mapa:lastUpdate ?Update ." +
						"  FILTER(?Type IN (mapa:Explorer, mapa:Recollector, mapa:Storage) )" +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		HashMap<String, Long> returnedList = new HashMap<String, Long>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher1 = patternIdAgent.matcher(entry.get("Agent").toString());
			if (matcher1.find() && entry.get("Update").isLiteral())
				returnedList.put(matcher1.group(1), entry.get("Update").asLiteral().getLong());
		}
		qe.close();
		return returnedList;
	}

	public Map<String, Long> getUpdatetimesMinerals() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Mineral ?Update where {" +
						" ?Mineral a ?Type ;" +
						"  mapa:lastUpdate ?Update ." +
						"  FILTER(?Type IN (mapa:Gold, mapa:Diamond) )" +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		HashMap<String, Long> returnedList = new HashMap<String, Long>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher1 = patternIdResource.matcher(entry.get("Mineral").toString());
			Long time = entry.get("Update").asLiteral().getLong();
			if (matcher1.find())
				returnedList.put(matcher1.group(1), time);
		}
		qe.close();
		return returnedList;
	}

	public void learnFromOtherOntology(MapaModel otherModel) {
		for (String open : otherModel.getClosedNodes()) {
			addNode(open, NodeType.Open);
		}
		for (String closed : otherModel.getClosedNodes()) {
			addNode(closed, NodeType.Closed);
		}
		for (String windy : otherModel.getWindyNodes()) {
			addNodeWindy(windy);
		}
		for (Pair<String, String> edge : otherModel.getEdges()) {
			this.addAdjancency(edge.getKey(), edge.getValue());
		}
		/*
		 * {
		 * Map<String, Long> ourUpdateTimes = getUpdatetimesAgents();
		 * //System.out.println("Our times: " + ourUpdateTimes);
		 * for (Entry<String, Long> agentUpdate :
		 * otherModel.getUpdatetimesAgents().entrySet()) {
		 * //System.out.println("Info Agent: " + agentUpdate);
		 * if (ourUpdateTimes.containsKey(agentUpdate.getKey()) &&
		 * ourUpdateTimes.get(agentUpdate.getKey()) > agentUpdate.getValue()) {
		 * //System.out.println("We have: " + ourUpdateTimes.get(agentUpdate.getKey()));
		 * continue;
		 * }
		 * System.out.println("Importing: " + agentUpdate);
		 * addAgent(agentUpdate.getKey(),
		 * otherModel.getAgentType(agentUpdate.getKey()));
		 * addAgentPos(agentUpdate.getKey(),
		 * otherModel.getAgentLocation(agentUpdate.getKey()));
		 * updateEntityTime(getAgent(agentUpdate.getKey()), agentUpdate.getValue());
		 * }
		 * }
		 * Map<String, Long> ourUpdateTimes = getUpdatetimesMinerals();
		 * for (Entry<String, Long> mineralUpdate :
		 * otherModel.getUpdatetimesMinerals().entrySet()) {
		 * if (ourUpdateTimes.containsKey(mineralUpdate.getKey()) &&
		 * ourUpdateTimes.get(mineralUpdate.getKey()) > mineralUpdate.getValue())
		 * continue;
		 * 
		 * addMineral(mineralUpdate.getKey(),
		 * otherModel.getMineralType(mineralUpdate.getKey()));
		 * Couple<String, String> locInfo =
		 * otherModel.getMineralLocation(mineralUpdate.getKey());
		 * if (locInfo.getLeft() != null) addMineralCarried(mineralUpdate.getKey(),
		 * locInfo.getLeft());
		 * else if (locInfo.getRight() != null) addMineralPos(mineralUpdate.getKey(),
		 * locInfo.getRight());
		 * updateEntityTime(getMineral(mineralUpdate.getKey()),
		 * mineralUpdate.getValue());
		 * }
		 */
	}

	public Map<String, String> getAgentPositions() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Agent ?Position where {" +
						" ?Agent a ?Type ;" +
						"  mapa:LocatedAt ?Position ." +
						"  FILTER(?Type IN (mapa:Explorer, mapa:Recollector, mapa:Storage) )" +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		HashMap<String, String> returnedList = new HashMap<String, String>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher1 = patternIdAgent.matcher(entry.get("Agent").toString());
			Matcher matcher2 = patternIdCell.matcher(entry.get("Position").toString());
			if (matcher1.find() && matcher2.find())
				returnedList.put(matcher1.group(1), matcher2.group(1));
		}
		qe.close();
		return returnedList;
	}

	/*
	 * public void removeAllAgentPositionsInSet(Set<String> agentNames) {
	 * Query query = QueryFactory.create
	 * (
	 * "PREFIX mapa: <http://mapa#> " +
	 * "SELECT ?Agent ?Position where {" +
	 * " ?Agent a ?Type ;" +
	 * "  mapa:LocatedAt ?Position ." +
	 * "  FILTER(?Type IN (mapa:Explorer, mapa:Recollector, mapa:Storage) )" +
	 * "}"
	 * );
	 * 
	 * QueryExecution qe = QueryExecutionFactory.create(query, model);
	 * ResultSet result = qe.execSelect();
	 * HashMap<String, String> removals = new HashMap<String, String>();
	 * while (result.hasNext()) {
	 * QuerySolution entry = result.next();
	 * 
	 * String position = null;
	 * String agent = null;
	 * Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
	 * if (matcher.find()) position = matcher.group(1);
	 * matcher = patternIdAgent.matcher(entry.get("Agent").toString());
	 * if (matcher.find()) agent = matcher.group(1);
	 * if (agent == null || position == null || !agentNames.contains(agent))
	 * continue;
	 * removals.put(agent, position);
	 * }
	 * qe.close();
	 * for (Entry<String, String> agentPosition : removals.entrySet()) {
	 * model.remove(new StatementImpl(getAgent(agentPosition.getKey()),
	 * model.getProperty(mapa("LocatedAt")), getCell(agentPosition.getValue())));
	 * }
	 * ++revision;
	 * }
	 */

	public String getOntology() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		model.write(bytes, "N-TRIPLE", null);
		return bytes.toString(StandardCharsets.UTF_8);
	}

	public static MapaModel importOntology(String onto) {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		InputStream stream = new ByteArrayInputStream(onto.getBytes(StandardCharsets.UTF_8));
		model.read(stream, null, "N-TRIPLE");
		return new MapaModel(model);
	}

	public Set<String> getClosedNodes() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Node where {" +
						" ?Node a mapa:Closed ." +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		Set<String> returnedList = new HashSet<String>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher = patternIdCell.matcher(entry.get("Node").toString());
			if (matcher.find())
				returnedList.add(matcher.group(1));
		}
		qe.close();
		return returnedList;
	}

	public Set<String> getOpenNodes() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Node where {" +
						" ?Node a mapa:Open ." +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		Set<String> returnedList = new HashSet<String>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher = patternIdCell.matcher(entry.get("Node").toString());
			if (matcher.find())
				returnedList.add(matcher.group(1));
		}
		qe.close();
		return returnedList;
	}

	public Set<String> getWindyNodes() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Node where {" +
						" ?Node a mapa:Windy ." +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		Set<String> returnedList = new HashSet<String>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher = patternIdCell.matcher(entry.get("Node").toString());
			if (matcher.find())
				returnedList.add(matcher.group(1));
		}
		qe.close();
		return returnedList;
	}

	public Set<Pair<String, String>> getEdges() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Node1 ?Node2 where {" +
						" ?Node1 mapa:Adjacent ?Node2 ." +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		Set<Pair<String, String>> returnedList = new HashSet<Pair<String, String>>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher1 = patternIdCell.matcher(entry.get("Node1").toString());
			Matcher matcher2 = patternIdCell.matcher(entry.get("Node2").toString());
			if (matcher1.find() && matcher2.find())
				returnedList.add(new Pair<String, String>(matcher1.group(1), matcher2.group(1)));
		}
		qe.close();
		return returnedList;
	}

	/*
	 * public void removeClosedNodes(Set<String> closedNodes) {
	 * Query query = QueryFactory.create
	 * (
	 * "PREFIX mapa: <http://mapa#> " +
	 * "SELECT ?Node where {" +
	 * " ?Node a mapa:Open ." +
	 * "}"
	 * );
	 * 
	 * QueryExecution qe = QueryExecutionFactory.create(query, model);
	 * ResultSet result = qe.execSelect();
	 * ArrayList<String> positions = new ArrayList<String>();
	 * while (result.hasNext()) {
	 * QuerySolution entry = result.next();
	 * 
	 * String position = null;
	 * Matcher matcher = patternIdCell.matcher(entry.get("Node").toString());
	 * if (matcher.find()) position = matcher.group(1);
	 * if (position == null || !closedNodes.contains(position))
	 * continue;
	 * positions.add(position);
	 * }
	 * qe.close();
	 * for (String position : positions) {
	 * model.remove(new StatementImpl(
	 * getCell(position),
	 * model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
	 * model.getResource(mapa("Open"))));
	 * }
	 * ++revision;
	 * }
	 */

	public void replaceModel(MapaModel mapa) {
		++revision;
		this.model = mapa.model;
	}

	public String getObjectiveLocation(String agentId) {
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + agentId + "_agent mapa:IntendsToWalkTo ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		}
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + agentId + "_agent mapa:LocatedAt ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		}
		return null;
	}

	// Left - Carried by
	// Right - Located at
	public Couple<String, String> getMineralLocation(String mineralId) {
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + mineralId + "_resource mapa:LocatedAt ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					return new Couple<String, String>(null, matcher.group(1));
				}
			}
		}
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Agent where {" +
							"  mapa:Instance_" + mineralId + "_resource mapa:CarriedBy ?Agent ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdAgent.matcher(entry.get("Agent").toString());
				if (matcher.find()) {
					return new Couple<String, String>(matcher.group(1), null);
				}
			}
		}
		return null;
	}

	public String getAgentLocation(String agentId) {
		{
			Query query = QueryFactory.create(
					"PREFIX mapa: <http://mapa#> " +
							"SELECT ?Position where {" +
							"  mapa:Instance_" + agentId + "_agent mapa:LocatedAt ?Position ." +
							"}");

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet result = qe.execSelect();
			if (result.hasNext()) {
				QuerySolution entry = result.next();
				Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		}
		return null;
	}

	public boolean hasOpenNodes() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Node where {" +
						" ?Node a mapa:Open ." +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		return result.hasNext();
	}

	public boolean hasClosedNodes() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Node where {" +
						" ?Node a mapa:Closed ." +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		return result.hasNext();
	}
}
