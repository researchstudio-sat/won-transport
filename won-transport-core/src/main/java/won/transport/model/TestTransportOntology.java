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


import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;
import won.transport.ont.Logico;
import won.transport.ont.Logiserv;
import won.transport.ont.Transport;

import java.io.FileNotFoundException;


public class TestTransportOntology
{

  public static void main(String... args) throws FileNotFoundException {

    OntModel individuals = ModelFactory.createOntologyModel();
    String senderUri = "https://node.matchat.org/won/resource/atom/sender-123";
    String recipientUri = "https://node.matchat.org/won/resource/atom/recipient-234";
    String transporterUri = "https://node.matchat.org/won/resource/atom/transporter-345";
    String consignmentUri = "https://node.matchat.org/won/resource/atom/consigment-456";

    individuals.setNsPrefix("atom","https://node.matchat.org/won/resource/atom/");

    individuals.setNsPrefix("lc", Logico.NS);
    individuals.setNsPrefix("ls", Logiserv.NS);
    individuals.setNsPrefix("tr", Transport.NS);

    individuals.setNsPrefix("sender", senderUri+"#");

    //the sender
    Individual sender = individuals.createIndividual(senderUri, Logico.Consignor);
    //a sender is a transport party and therefore may/must have:
    //* name (must)
    //* contact details
    //* physical location
    //* postal address
    //* registration address
    //* website
    sender.addProperty(Logico.hasName, "Father Christmas Productions Inc.");

    // contact details
    Individual senderContact = individuals.createIndividual(senderUri+"#contact", Transport.Contact);
    senderContact.addProperty(Logico.hasName, "Elf 11");
    senderContact.addProperty(Logico.hasEmail, "elfeleven@xmas.com");
    senderContact.addProperty(Logico.hasPhoneNr, "+000679 11223344");
    senderContact.addProperty(RDFS.label,"Contact(Sender)");
    sender.addProperty(Transport.hasContactDetails, senderContact);

    //physical location
    Individual senderLocation = individuals.createIndividual(senderUri+"#location", Logico.Address);
    Individual vienna = individuals.createIndividual(senderUri+"#Vienna", Logico.City);
    vienna.addProperty(Logico.hasCityName, "Vienna", "en");
    Individual thurngasse8 = individuals.createIndividual(senderUri+"#Thurngasse8", Logico.Street);
    thurngasse8.addProperty(Logico.hasStreetName, "Thurngasse");
    thurngasse8.addProperty(Logico.hasStreetNumber, "8", XSDBaseNumericType.XSDint);
    //now at this point, I'd expect something like an apartment number - in Austria, you need that.
    senderLocation.addProperty(Logico.hasAddress, thurngasse8);
    senderLocation.addProperty(Logico.hasCity, vienna);
    sender.addProperty(Transport.hasPhysicalLocation, senderLocation);

    //recipient
    Individual recipient = individuals.createIndividual(recipientUri, Logico.Consignee);
    //the recipient is also a transport party, see above for possible fields
    recipient.addProperty(Logico.hasName, "The Real Batman");
    Individual recipientContact = individuals.createIndividual(recipientUri+"#contact", Transport.Contact);
    recipientContact.addProperty(Logico.hasName, "Bruce Wayne");
    recipientContact.addProperty(Logico.hasEmail, "brucewayne@wayne.com");
    recipientContact.addProperty(Logico.hasPhoneNr, "+01606 00449922");
    recipientContact.addProperty(RDFS.label,"Contact(recipient)");
    
    Individual consignment = individuals.createIndividual(consignmentUri, Transport.Consignment);
    consignment.addProperty(Transport.hasConsignee, recipient);
    consignment.addProperty(Transport.hasConsignor, sender);

    individuals.write(System.out, "TTL");


  }
}
