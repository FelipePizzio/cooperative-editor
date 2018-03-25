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
package edu.ifrs.cooperativeeditor.mail;

import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Stateless
public class EmailClient {
	
	private static ConnectionFactory connectionFactory;
	
	private static Queue queue;

	public void sendEmail(String emails, String url) {
		
		try {
			InitialContext ic = new InitialContext();
			connectionFactory = (ConnectionFactory) ic.lookup("java:jboss/DefaultJMSConnectionFactory");
			queue = (Queue) ic.lookup("java:/jms/queue/CooperativeEditorEmailQueue");
			
			JMSContext jmsContext = connectionFactory.createContext();
			JMSProducer producer = jmsContext.createProducer();
			
			MapMessage map = jmsContext.createMapMessage();
			map.setString("emails", emails);
			map.setString("url", url);
			
	       	producer.send(queue, map);
	       	
		} catch (NamingException | JMSException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}