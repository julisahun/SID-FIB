package eu.su.mas.dedaleEtu.sid.grupo03.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;

import org.apache.jena.ontology.OntDocumentManager;
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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;

import javafx.application.Platform;
import javafx.util.Pair;

public class MapaModel {
	public Model model;
	long revision;

	public enum NodeType {
		Open,
		Closed
	}

	public enum AgentType {
		Explorer,
		Storage,
		Recollector,
	}

	public class AgentInfo {
		public long goldAmount;
		public long diamondAmount;

		public AgentInfo(long goldAmount, long diamondAmount) {
			this.goldAmount = goldAmount;
			this.diamondAmount = diamondAmount;
		}
	};

	public class NodeInfo {
		public long goldAmount;
		public long diamondAmount;
		public long lockpickLevel;
		public long timesVisited;
		public long strength;

		public NodeInfo(long timesVisited, long goldAmount, long diamondAmount, long lockpickLevel, long strength) {
			this.timesVisited = timesVisited;
			this.goldAmount = goldAmount;
			this.diamondAmount = diamondAmount;
			this.lockpickLevel = lockpickLevel;
			this.strength = strength;
		}

		public Boolean hasResource(String resourceType, long strength, long lockpickLevel) {
			if (this.strength > strength || this.lockpickLevel > lockpickLevel) {
				return false;
			}
			if (resourceType.equals("gold")) {
				return goldAmount > 0;
			}
			if (resourceType.equals("diamond")) {
				return diamondAmount > 0;
			}
			return false;
		}
	};

	public class AgentConstantInfo {
		public AgentType type;
		public long goldCapacity;
		public long diamondCapacity;
		public long lockpickLevel;

		public AgentConstantInfo(AgentType type, long goldCapacity, long diamondCapacity, long lockpickLevel) {
			this.type = type;
			this.goldCapacity = goldCapacity;
			this.diamondCapacity = diamondCapacity;
			this.lockpickLevel = lockpickLevel;
		}
	};

	HashMap<String, NodeType> nodeCache = new HashMap<String, NodeType>();
	HashSet<String> windyCache = new HashSet<String>();
	HashMap<String, AgentConstantInfo> agentCache = new HashMap<String, AgentConstantInfo>();
	HashMap<String, String> agentPosCache = new HashMap<String, String>();
	HashMap<String, HashSet<String>> adjacencyCache = new HashMap<String, HashSet<String>>();

	public MapaModel(Model model) {
		this.model = model;
		this.revision = 0;
	}

	public MapaModel() {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		OntDocumentManager dm = model.getDocumentManager();
		URL fileAsResource = Utils.class.getClassLoader().getResource(Constants.FILE_NAME + ".owl");
		dm.addAltEntry(Constants.FILE_NAME, fileAsResource.toString());
		model.read(Constants.FILE_NAME);
		this.model = model;
		this.revision = 0;
	}

	static Pattern patternIdCell = Pattern.compile("^http://mapa#Instance_(.+?)_cell$");
	static Pattern patternIdAgent = Pattern.compile("^http://mapa#Instance_(.+?)_agent$");

	private String mapa(String hastag) {
		return "http://mapa#" + hastag;
	}

	private Resource getAgent(String id) {
		return model.createResource(mapa("Instance_" + id + "_agent"));
	}

	private Resource getCell(String id) {
		return model.createResource(mapa("Instance_" + id + "_cell"));
	}

	private void addCheckStmt(StatementImpl stmt) {
		if (!model.contains(stmt)) {
			++revision;
			model.add(stmt);
		}
	}

	private long getLong(Resource res, String literalName) {
		StmtIterator it = model.listStatements(res, model.getProperty(mapa(literalName)), (RDFNode) null);
		if (it.hasNext()) {
			Statement entry = it.next();
			long v = entry.getObject().asLiteral().getLong();
			it.close();
			return v;
		}
		it.close();
		Platform.exit();
		throw new RuntimeException();
	}

	private void updateEntityTime(Resource resource, long time) {
		StmtIterator it = model.listStatements(resource, model.getProperty(mapa("lastUpdate")), (RDFNode) null);
		ArrayList<Statement> rm = new ArrayList<Statement>();
		while (it.hasNext())
			rm.add(it.next());
		it.close();
		model.remove(rm);
		model.addLiteral(resource, model.getProperty(mapa("lastUpdate")), time);
	}

	private void updateEntityTime(Resource resource) {
		updateEntityTime(resource, System.currentTimeMillis());
	}

	public long revision() {
		return this.revision;
	}

	public void addAdjancency(String node1id, String node2id) {
		if (adjacencyCache.containsKey(node1id) && adjacencyCache.get(node1id).contains(node2id))
			return;
		Resource node1 = getCell(node1id);
		Resource node2 = getCell(node2id);
		addCheckStmt(new StatementImpl(node1, model.getProperty(mapa("Adjacent")), node2));
		addCheckStmt(new StatementImpl(node2, model.getProperty(mapa("Adjacent")), node1));

		if (!adjacencyCache.containsKey(node1id))
			adjacencyCache.put(node1id, new HashSet<String>());
		adjacencyCache.get(node1id).add(node2id);
		if (!adjacencyCache.containsKey(node2id))
			adjacencyCache.put(node2id, new HashSet<String>());
		adjacencyCache.get(node2id).add(node1id);
	}

	public void addNode(String id, NodeType type) {
		NodeType cachedType = nodeCache.get(id);
		boolean alreadyClosed = cachedType != null && cachedType == NodeType.Closed;
		boolean alreadyOpen = cachedType != null && cachedType == NodeType.Open;
		if (alreadyClosed)
			return;
		if (type == NodeType.Open && alreadyOpen) {
			return;
		}
		Resource cell = getCell(id);
		StatementImpl closedStmt = new StatementImpl(cell,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Closed")));
		StatementImpl openStmt = new StatementImpl(cell,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Open")));

		if (type == NodeType.Closed && alreadyOpen) {
			model.remove(openStmt);
		}
		addCheckStmt(type == NodeType.Open ? openStmt : closedStmt);
		nodeCache.put(id, type);
	}

	public void addNodeWindy(String id) {
		if (windyCache.contains(id))
			return;
		windyCache.add(id);
		addCheckStmt(new StatementImpl(
				getCell(id),
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				model.getResource(mapa("Windy"))));
	}

	public void addAgent(String agentName, AgentType agent, long goldCapacity, long diamondCapacity, long lockpickLevel) {
		AgentConstantInfo cachedType = agentCache.get(agentName);
		if (cachedType != null)
			return;
		agentCache.put(agentName, new AgentConstantInfo(agent, goldCapacity, diamondCapacity, lockpickLevel));
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
		Resource agentRes = getAgent(agentName);
		addCheckStmt(new StatementImpl(
				agentRes,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				classOfAgent));
		addCheckStmt(new StatementImpl(
				agentRes,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				model.getResource(mapa("Agent"))));
		addCheckStmt(new StatementImpl(
				agentRes,
				model.getProperty(mapa("lockpickLevel")),
				model.createTypedLiteral(lockpickLevel)));
		addCheckStmt(new StatementImpl(
				agentRes,
				model.getProperty(mapa("goldCapacity")),
				model.createTypedLiteral(goldCapacity)));
		addCheckStmt(new StatementImpl(
				agentRes,
				model.getProperty(mapa("diamondCapacity")),
				model.createTypedLiteral(diamondCapacity)));
		updateEntityTime(agentRes);
	}

	public void addAgentInfo(String agentId, long goldAmount, long diamondAmount) {
		Resource agent = getAgent(agentId);
		ArrayList<Statement> stmtsGold;
		ArrayList<Statement> stmtsDiamond;
		{
			StmtIterator it = model.listStatements(agent, model.getProperty(mapa("goldAmount")), (RDFNode) null);
			stmtsGold = new ArrayList<Statement>();
			while (it.hasNext())
				stmtsGold.add(it.next());
			it.close();
		}
		{
			StmtIterator it = model.listStatements(agent, model.getProperty(mapa("diamondAmount")), (RDFNode) null);
			stmtsDiamond = new ArrayList<Statement>();
			while (it.hasNext())
				stmtsDiamond.add(it.next());
			it.close();
		}
		if (stmtsDiamond.size() == 1 && stmtsGold.size() == 1
				&& stmtsDiamond.get(0).getObject().asLiteral().getLong() == diamondAmount
				&& stmtsGold.get(0).getObject().asLiteral().getLong() == goldAmount) {
			return;
		}
		model.remove(stmtsGold);
		model.remove(stmtsDiamond);
		addCheckStmt(new StatementImpl(
				agent,
				model.getProperty(mapa("goldAmount")),
				model.createTypedLiteral(goldAmount)));
		addCheckStmt(new StatementImpl(
				agent,
				model.getProperty(mapa("diamondAmount")),
				model.createTypedLiteral(diamondAmount)));
		updateEntityTime(agent);
	}

	public void addNodeInfo(String nodeId, long timesVisited, long goldAmount, long diamondAmount, long lockpickLevel,
			long strength) {
		Resource node = getCell(nodeId);
		ArrayList<Statement> stmtsTv;
		ArrayList<Statement> stmtsLvl;
		ArrayList<Statement> stmtsGold;
		ArrayList<Statement> stmtsDiamond;
		ArrayList<Statement> stmtsStrength;
		{
			StmtIterator it = model.listStatements(node, model.getProperty(mapa("timesVisited")), (RDFNode) null);
			stmtsTv = new ArrayList<Statement>();
			while (it.hasNext())
				stmtsTv.add(it.next());
			it.close();
		}
		{
			StmtIterator it = model.listStatements(node, model.getProperty(mapa("lockpickLevel")), (RDFNode) null);
			stmtsLvl = new ArrayList<Statement>();
			while (it.hasNext())
				stmtsLvl.add(it.next());
			it.close();
		}
		{
			StmtIterator it = model.listStatements(node, model.getProperty(mapa("goldAmount")), (RDFNode) null);
			stmtsGold = new ArrayList<Statement>();
			while (it.hasNext())
				stmtsGold.add(it.next());
			it.close();
		}
		{
			StmtIterator it = model.listStatements(node, model.getProperty(mapa("diamondAmount")), (RDFNode) null);
			stmtsDiamond = new ArrayList<Statement>();
			while (it.hasNext())
				stmtsDiamond.add(it.next());
			it.close();
		}
		{
			StmtIterator it = model.listStatements(node, model.getProperty(mapa("strength")), (RDFNode) null);
			stmtsStrength = new ArrayList<Statement>();
			while (it.hasNext())
				stmtsStrength.add(it.next());
			it.close();
		}
		if (stmtsDiamond.size() == 1 && stmtsGold.size() == 1 && stmtsLvl.size() == 1 && stmtsTv.size() == 1
				&& stmtsStrength.size() == 1
				&& stmtsDiamond.get(0).getObject().asLiteral().getLong() == diamondAmount
				&& stmtsGold.get(0).getObject().asLiteral().getLong() == goldAmount
				&& stmtsLvl.get(0).getObject().asLiteral().getLong() == lockpickLevel
				&& stmtsTv.get(0).getObject().asLiteral().getInt() == timesVisited
				&& stmtsStrength.get(0).getObject().asLiteral().getLong() == strength) {
			return;
		}
		model.remove(stmtsTv);
		model.remove(stmtsLvl);
		model.remove(stmtsGold);
		model.remove(stmtsDiamond);
		model.remove(stmtsStrength);
		addCheckStmt(new StatementImpl(
				node,
				model.getProperty(mapa("timesVisited")),
				model.createTypedLiteral(timesVisited)));
		addCheckStmt(new StatementImpl(
				node,
				model.getProperty(mapa("lockpickLevel")),
				model.createTypedLiteral(lockpickLevel)));
		addCheckStmt(new StatementImpl(
				node,
				model.getProperty(mapa("goldAmount")),
				model.createTypedLiteral(goldAmount)));
		addCheckStmt(new StatementImpl(
				node,
				model.getProperty(mapa("diamondAmount")),
				model.createTypedLiteral(diamondAmount)));
		addCheckStmt(new StatementImpl(
				node,
				model.getProperty(mapa("strength")),
				model.createTypedLiteral(strength)));
		updateEntityTime(node);
	}

	public AgentConstantInfo getAgentConstantInfo(String agentName) {
		AgentConstantInfo cache = agentCache.get(agentName);
		if (cache != null)
			return cache;
		AgentType type;
		Resource agent = getAgent(agentName);
		if (model.contains(new StatementImpl(agent, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				model.getResource(mapa("Explorer"))))) {
			type = AgentType.Explorer;
		} else if (model
				.contains(new StatementImpl(agent, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
						model.getResource(mapa("Recollector"))))) {
			type = AgentType.Recollector;
		} else {
			type = AgentType.Storage;
		}
		return new AgentConstantInfo(type, getLong(agent, "goldCapacity"), getLong(agent, "diamondCapacity"),
				getLong(agent, "lockpickLevel"));
	}

	public AgentInfo getAgentInfo(String agentName) {
		Resource agent = getAgent(agentName);
		return new AgentInfo(getLong(agent, "goldAmount"), getLong(agent, "diamondAmount"));
	}

	public NodeInfo getCellInfo(String nodeId) {
		Resource node = getCell(nodeId);
		return new NodeInfo(getLong(node, "timesVisited"), getLong(node, "goldAmount"), getLong(node, "diamondAmount"),
				getLong(node, "lockpickLevel"), getLong(node, "strength"));
	}

	public void addAgentPos(String agentName, String id) {
		String cachedPos = agentPosCache.get(agentName);
		if (cachedPos != null && cachedPos.equals(id)) {
			updateEntityTime(getAgent(agentName));
			return;
		}

		Property locatedAt = model.getProperty(mapa("LocatedAt"));
		{
			StmtIterator statements = model.listStatements(getAgent(agentName), locatedAt, (RDFNode) null);

			ArrayList<Statement> toRemove = new ArrayList<Statement>();
			if (statements.hasNext()) {
				Statement entry = statements.next();

				Matcher matcher = patternIdCell.matcher(entry.getObject().asResource().getURI());
				if (matcher.find()) {
					if (!matcher.group(1).equals(id)) {
						toRemove.add(entry);
					} else {
						updateEntityTime(getAgent(agentName));
						return;
					}
				}
			}
			statements.close();
			model.remove(toRemove);
		}
		{
			StmtIterator statements = model.listStatements((Resource) null, locatedAt, getCell(id));

			ArrayList<Statement> toRemove = new ArrayList<Statement>();
			if (statements.hasNext()) {
				Statement entry = statements.next();
				Matcher matcher = patternIdAgent.matcher(entry.getSubject().getURI());
				if (matcher.find()) {
					agentPosCache.clear();
					toRemove.add(entry);
				}
			}
			statements.close();
			model.remove(toRemove);
		}
		addCheckStmt(new StatementImpl(getAgent(agentName), model.getProperty(mapa("LocatedAt")), getCell(id)));
		updateEntityTime(getAgent(agentName));
		agentPosCache.put(agentName, id);
	}

	public void addObectiveLocation(String agentName, String id) {
		StmtIterator statements = model.listStatements(getAgent(agentName), model.getProperty(mapa("IntendsToWalkTo")),
				(RDFNode) null);

		ArrayList<Statement> toRemove = new ArrayList<Statement>();
		if (statements.hasNext()) {
			Statement entry = statements.next();

			Matcher matcher = patternIdCell.matcher(entry.getObject().asResource().getURI());
			if (matcher.find()) {
				if (!matcher.group(1).equals(id)) {
					toRemove.add(entry);
				} else {
					updateEntityTime(getAgent(agentName));
					return;
				}
			}
		}
		statements.close();
		model.remove(toRemove);
		addCheckStmt(new StatementImpl(getAgent(agentName), model.getProperty(mapa("IntendsToWalkTo")), getCell(id)));
		updateEntityTime(getAgent(agentName));
	}

	public Map<String, Long> getUpdatetimesAgents() {
		StmtIterator statements = model.listStatements((Resource) null, model.getProperty(mapa("lastUpdate")),
				(RDFNode) null);
		HashMap<String, Long> returnedList = new HashMap<String, Long>();
		while (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher1 = patternIdAgent.matcher(entry.getSubject().getURI());
			if (matcher1.find() && entry.getObject().isLiteral())
				returnedList.put(matcher1.group(1), entry.getObject().asLiteral().getLong());
		}
		statements.close();
		return returnedList;
	}

	public Map<String, Long> getUpdatetimesCells() {
		StmtIterator statements = model.listStatements((Resource) null, model.getProperty(mapa("lastUpdate")),
				(RDFNode) null);
		HashMap<String, Long> returnedList = new HashMap<String, Long>();
		while (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher1 = patternIdCell.matcher(entry.getSubject().getURI());
			if (matcher1.find() && entry.getObject().isLiteral())
				returnedList.put(matcher1.group(1), entry.getObject().asLiteral().getLong());
		}
		statements.close();
		return returnedList;
	}

	public void learnFromOtherOntology(MapaModel otherModel) {
		for (String open : otherModel.getOpenNodes()) {
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
		{
			Map<String, Long> ourUpdateTimes = getUpdatetimesAgents();
			for (Entry<String, Long> agentUpdate : otherModel.getUpdatetimesAgents().entrySet()) {
				if (ourUpdateTimes.containsKey(agentUpdate.getKey())
						&& ourUpdateTimes.get(agentUpdate.getKey()) > agentUpdate.getValue()) {
					continue;
				}
				AgentConstantInfo infoConstant = otherModel.getAgentConstantInfo(agentUpdate.getKey());
				addAgent(agentUpdate.getKey(), infoConstant.type, infoConstant.goldCapacity, infoConstant.diamondCapacity,
						infoConstant.lockpickLevel);
				AgentInfo info = otherModel.getAgentInfo(agentUpdate.getKey());
				addAgentInfo(agentUpdate.getKey(), info.goldAmount, info.diamondAmount);
				addAgentPos(agentUpdate.getKey(), otherModel.getAgentLocation(agentUpdate.getKey()));
				updateEntityTime(getAgent(agentUpdate.getKey()), agentUpdate.getValue());
			}
		}
		{
			Map<String, Long> ourUpdateTimes = getUpdatetimesCells();
			for (Entry<String, Long> cellUpdate : otherModel.getUpdatetimesCells().entrySet()) {
				if (ourUpdateTimes.containsKey(cellUpdate.getKey())
						&& ourUpdateTimes.get(cellUpdate.getKey()) > cellUpdate.getValue()) {
					continue;
				}
				NodeInfo info = otherModel.getCellInfo(cellUpdate.getKey());
				long timesVisited = 0;
				try {
					timesVisited = getLong(getCell(cellUpdate.getKey()), "timesVisited");
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
				addNodeInfo(cellUpdate.getKey(), timesVisited, info.goldAmount,
						info.diamondAmount,
						info.lockpickLevel,
						info.strength);
				updateEntityTime(getCell(cellUpdate.getKey()), cellUpdate.getValue());
			}
		}
	}

	public Map<String, String> getAgentPositions() {
		StmtIterator statements = model.listStatements((Resource) null, model.getProperty(mapa("LocatedAt")),
				(RDFNode) null);
		HashMap<String, String> returnedList = new HashMap<String, String>();
		while (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher1 = patternIdAgent.matcher(entry.getSubject().getURI());
			Matcher matcher2 = patternIdCell.matcher(entry.getObject().asResource().getURI());
			if (matcher1.find() && matcher2.find())
				returnedList.put(matcher1.group(1), matcher2.group(1));
		}
		statements.close();
		return returnedList;
	}

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

	public void exportOntology() {
		try {
			System.out.println("Saving ontology..." + this.model.isClosed());
			if (!this.model.isClosed()) {
				String sep = File.separator;
				Path resourcePath = Paths.get(Utils.class.getResource(sep).getPath());
				this.model.write(new FileOutputStream(resourcePath + sep + Constants.FILE_NAME +
						"-modified.owl", false));
				this.model.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<String> getClosedNodes() {
		StmtIterator statements = model.listStatements((Resource) null,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Closed")));
		Set<String> returnedList = new HashSet<String>();
		while (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher = patternIdCell.matcher(entry.getSubject().getURI());
			if (matcher.find())
				returnedList.add(matcher.group(1));
		}
		statements.close();
		return returnedList;
	}

	public Set<String> getOpenNodes() {
		StmtIterator statements = model.listStatements((Resource) null,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Open")));
		Set<String> returnedList = new HashSet<String>();
		while (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher = patternIdCell.matcher(entry.getSubject().getURI());
			if (matcher.find())
				returnedList.add(matcher.group(1));
		}
		statements.close();
		return returnedList;
	}

	public Set<String> getWindyNodes() {
		StmtIterator statements = model.listStatements((Resource) null,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Windy")));
		Set<String> returnedList = new HashSet<String>();
		while (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher = patternIdCell.matcher(entry.getSubject().getURI());
			if (matcher.find())
				returnedList.add(matcher.group(1));
		}
		statements.close();
		return returnedList;
	}

	public Set<Pair<String, String>> getEdges() {
		StmtIterator statements = model.listStatements((Resource) null, model.getProperty(mapa("Adjacent")),
				(RDFNode) null);
		Set<Pair<String, String>> returnedList = new HashSet<Pair<String, String>>();
		while (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher1 = patternIdCell.matcher(entry.getSubject().getURI());
			Matcher matcher2 = patternIdCell.matcher(entry.getObject().asResource().getURI());
			if (matcher1.find() && matcher2.find())
				returnedList.add(new Pair<String, String>(matcher1.group(1), matcher2.group(1)));
		}
		statements.close();
		return returnedList;
	}

	public HashSet<String> getNeighbors(String nodeId) {
		return this.adjacencyCache.get(nodeId);
	}

	private Boolean canReach(String node1, String node2, String blockedNode) {
		Queue<String> queue = new LinkedList<>();
		HashSet<String> visited = new HashSet<>();
		queue.add(node1);
		while (!queue.isEmpty()) {
			String node = queue.poll();
			if (node.equals(node2))
				return true;
			visited.add(node);
			HashSet<String> neighbors = this.getNeighbors(node);
			for (String neighbor : neighbors) {
				if (!visited.contains(neighbor) && !neighbor.equals(blockedNode))
					queue.add(neighbor);
			}
		}
		return false;
	}

	public Boolean isNonBlockingNode(String nodeId) {
		HashSet<String> neighbors = this.getNeighbors(nodeId);
		for (String neighbor : neighbors) {
			for (String neighbor2 : neighbors) {
				if (neighbor.equals(neighbor2))
					continue;
				final Boolean canReach = this.canReach(neighbor, neighbor2, nodeId);
				if (!canReach)
					return false;
			}
		}
		return true;
	}

	public HashSet<String> getResourceNodes(String resource, long strength, long level) {
		// StmtIterator statements = model.listStatements((Resource) null,
		// model.getProperty(mapa(resource + "Amount")), (RDFNode) null);
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT" +
						" ?Node" +
						" WHERE {" +
						" ?Node mapa:" + resource + "Amount ?amount ;" +
						" mapa:strength ?strength ;" +
						" mapa:lockpickLevel ?level ." +
						" FILTER (?amount > 0 && ?strength <= " + strength + " && ?level <= " + level + ")" +
						" }");
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet result = qexec.execSelect();
		HashSet<String> returnList = new HashSet<String>();
		while (result.hasNext()) {
			QuerySolution entry = result.next();
			Matcher matcher = patternIdCell.matcher(entry.getResource("Node").getURI());
			if (matcher.find()) {
				returnList.add(matcher.group(1));
			}
		}
		return returnList;
	}

	public void replaceModel(MapaModel mapa) {
		++revision;
		this.model = mapa.model;
		this.adjacencyCache.clear();
		this.agentCache.clear();
		this.agentPosCache.clear();
		this.nodeCache.clear();
		this.windyCache.clear();
	}

	public String getObjectiveLocation(String agentId) {
		StmtIterator statements = model.listStatements(getAgent(agentId), model.getProperty(mapa("IntendsToWalkTo")),
				(RDFNode) null);
		if (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher = patternIdCell.matcher(entry.getObject().asResource().getURI());
			if (matcher.find()) {
				String pos = matcher.group(1);
				statements.close();
				return pos;
			}
		}
		statements.close();
		return null;

	}

	public String getAgentLocation(String agentId) {
		String locCache = agentPosCache.get(agentId);
		if (locCache != null) {
			return locCache;
		}
		StmtIterator statements = model.listStatements(getAgent(agentId), model.getProperty(mapa("LocatedAt")),
				(RDFNode) null);
		if (statements.hasNext()) {
			Statement entry = statements.next();
			Matcher matcher = patternIdCell.matcher(entry.getObject().asResource().getURI());
			if (matcher.find()) {
				String pos = matcher.group(1);
				agentPosCache.put(agentId, pos);
				statements.close();
				return pos;
			}
		}
		statements.close();
		return null;
	}

	public boolean hasOpenNotWindyNodes() {
		Query query = QueryFactory.create(
				"PREFIX mapa: <http://mapa#> " +
						"SELECT ?Node where {" +
						" ?Node a mapa:Open ." +
						" FILTER NOT EXISTS {" +
						"  ?Node a mapa:Windy ." +
						" }" +
						"}");

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet result = qe.execSelect();
		return result.hasNext();
	}

	public boolean hasClosedNodes() {
		StmtIterator statements = model.listStatements((Resource) null,
				model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Closed")));
		boolean hasClosed = (statements.hasNext());
		statements.close();
		return hasClosed;
	}
}
