package com.example.application.data.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.example.application.data.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Henkilo extends AbstractEntity {

    private String nimi;
    private String puhelinnumero;
    private String email;
    
    @OneToMany(mappedBy = "henkilo")
	@JsonManagedReference // Tällä saadaan kirjat näkymään kirjastossa, mutta kirjasto ei näy kirjoissa
	private List<Mittaus> mittaukset;
    
	public String getNimi() {
        return nimi;
    }
    public void setNimi(String nimi) {
        this.nimi = nimi;
    }
    public String getPuhelinnumero() {
        return puhelinnumero;
    }
    public void setPuhelinnumero(String puhelinnumero) {
        this.puhelinnumero = puhelinnumero;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
	public List<Mittaus> getMittaukset() {
		return mittaukset;
	}

	public void setMittaukset(List<Mittaus> mittaukset) {
		this.mittaukset = mittaukset;
	}
	
	public Henkilo() {

	}

}
