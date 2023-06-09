package eu.su.mas.dedaleEtu.sid.core;

import jade.lang.acl.ACLMessage;

public class Message {
  public String sender;
  public String ontology;
  public String protocol;
  public String content;
  public String receiver;
  public int performative;

  public Message(ACLMessage msg, String receiver) {
    this.sender = msg.getSender().getLocalName();
    if (this.sender == null) {
      this.sender = "";
    }
    this.ontology = msg.getOntology();
    if (this.ontology == null) {
      this.ontology = "";
    }
    this.protocol = msg.getProtocol();
    if (this.protocol == null) {
      this.protocol = "";
    }
    this.content = msg.getContent();
    if (this.content == null) {
      this.content = "";
    }
    this.receiver = receiver;
    this.performative = msg.getPerformative();

  }
}
