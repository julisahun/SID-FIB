package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

public class Constants {
    public static String I_AM_REGISTERED = "IAmRegistered";
    public static String ONTOLOGY = "ontology";
    public static String ONTOLOGY_URI = "http://www.semanticweb.org/juli/ontologies/2023/3/untitled-ontology-2#";
    public static String QUERY_SITUATED_AGENT = "PREFIX example: <http://example#> " +
            "SELECT ?Agent where {" +
            " ?Agent a example:Agent ." +
            "}";
    public static String IS_SLAVE_ALIVE = "isSlaveAlive";
    public static String MAP = "map";
    public static String IS_FULL_EXPLORED = "isFullExplored";
    public static String COMMAND_SENT = "commandSent";
    public static String REJECTED_NODES = "rejectedNodes";
    public static String MAILBOX_EMPTY = "mailboxEmpty";
    public static String SITUATED_PINGED = "situatedPinged";
}
