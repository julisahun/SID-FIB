Communication between agents:
  BDI -> Situated -> BDI
  - Before anything starts, BDIAgent will wait a not-understood msg (from us, the humans) with a number, this number will be its situatedAgentCode (3 or 4) in our case
  - Once set the situated Name, BDI will send a ping to its agent to check if its alive and to gather its basic data (PingAgentPlanBody.java -> MessageMapper.java::pong)
  - According to the type of the agent, the BDI will send a command with the action to perform (for now its only move) (CommandPlanBody.java -> MessageMapper.java::updatePosition)
  - The situated will act in consequence of the command (for now its only walking) and send feedback to BDI and the map update
  - The BDI will store the map update in a queue (this is done just in case a lot of mapUpdates happens).
  - BDI will update its internal state (BDIAgent03.java::overrideBeliefRevisionStrategy) and send to situated the new ontology
  - The situated will update the ontology (only for sharing purposes)
  - BDI will send the next command


Situated(guiri) -> Situated -> BDI -> Situated
  - If the situated receives an ontology from an other situated, it will send it to its BDI.
  - BDI will merge its ontology with the oneShared (KeepMailboxEmptyPlanBody.java::updateOntology) and generate a MapRepresentation according to the new data. (this is only done if the hash with our current ontology don't match, in case an agent spams the same ontology)
  - BDI sends back the new ontology and the new MapRepresentation to its situated