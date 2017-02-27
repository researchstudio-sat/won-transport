/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.transport.model;


import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import won.transport.ont.Logico;
import won.transport.ont.Logiserv;
import won.transport.ont.Transport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class TestTransportOntology
{

  public static void main(String... args) throws FileNotFoundException {

    OntModel m = ModelFactory.createOntologyModel();


    m.read(new FileInputStream("won-transport-core/src/main/vocabs/logiserv.ttl"),
           Transport.NS,
           "TTL");

    OntModel individuals = ModelFactory.createOntologyModel();
    String prefix = "https://node.matchat.org/won/resource/need/12345#";
    individuals.setNsPrefix("need", prefix);
    individuals.setNsPrefix("lc", Logico.NS);
    individuals.setNsPrefix("ls", Logiserv.NS);
    individuals.setNsPrefix("tr", Transport.NS);


    Individual sender = individuals.createIndividual(prefix+"sender", won.transport.ont.Logico.Consignor);
    Individual recipient = individuals.createIndividual(prefix+"recipient", won.transport.ont.Logico.Consignee);
    Individual consignment = individuals.createIndividual(prefix+"consignment", won.transport.ont.Transport.Consignment);
    consignment.addProperty(Transport.hasConsignee, recipient);
    consignment.addProperty(Transport.hasConsignor, sender);

    individuals.write(System.out, "TTL");


  }
}
