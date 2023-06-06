package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

public class Constants {
    public static String I_AM_REGISTERED = "IAmRegistered";
    public static String ONTOLOGY = "ontology";
    public static String ONTOLOGY_URI = "http://www.semanticweb.org/juli/ontologies/2023/3/untitled-ontology-2#";
    public static String QUERY_SITUATED_AGENT = "PREFIX example: <http://example#> " +
            "SELECT ?Agent where {" +
            " ?Agent a example:Agent ." +
            "}";
    public static String EXPLORER_ALIVE = "explorerAlive";
    public static String COLLECTOR_ALIVE = "collectorAlive";
    public static String TANKER_ALIVE = "tankerAlive";
    public static String MAP = "map";
    public static String IS_FULL_EXPLORED = "isFullExplored";
    public static String COMMAND_SENT = "commandSent";
    public static String REJECTED_NODES = "rejectedNodes";
    public static String MAILBOX_EMPTY = "mailboxEmpty";
    public static String SITUATED_PINGED = "situatedPinged";
    public static String SITUATED_COMMANDED = "situatedCommanded";
    public static String PING_SEND = "pingSend";
    public static String MAP_UPDATES = "mapUpdates";
    public static String CURRENT_SITUATED_POSITION = "currentSituatedPosition";
    public static String MAP_OUT_OF_SYNC = "mapOutOfSync";
    public static String TIMES_VISITED = "timesVisited";
    public static int WAITING_CYCLES = 3;
}
