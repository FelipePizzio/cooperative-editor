/**
 * @license
 * Copyright 2018, Instituto Federal do Rio Grande do Sul (IFRS)
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
package edu.ifrs.cooperativeeditor.model;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "user_rubric_status")
public class UserRubricStatus {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private Boolean consent;
	private Integer ticketRound;
	@Enumerated
	private Situation situation;
	@ManyToOne
	@JoinColumn(name = "rubric_id", nullable = false)
	private Rubric rubric;
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public UserRubricStatus() {
		super();
		this.situation = Situation.FREE;
		this.ticketRound = 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isConsent() {
		return consent;
	}

	public void setConsent(boolean consent) {
		this.consent = consent;
	}

	public int getTicketRound() {
		return ticketRound;
	}

	public void setTicketRound(int ticketRound) {
		this.ticketRound = ticketRound;
	}

	public Situation getSituation() {
		return situation;
	}

	public void setSituation(Situation situation) {
		this.situation = situation;
	}

	public Rubric getRubric() {
		return rubric;
	}

	public void setRubric(Rubric rubric) {
		this.rubric = rubric;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@Override
	public String toString() {
		return " { \"id\":\"" + id + "\","
				+ "\"consent\":\"" + isConsent() + "\","
				+ "\"ticketRound\":\"" + getTicketRound() + "\","
				+ "\"situation\":\"" + getSituation() + "\","
				+ "\"user\":" + getUser() + ","
				+ "\"rubric\":" + getRubric() + "}";
	}
}
