package com.example.application.data.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.example.application.data.AbstractEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.joda.time.*;

@Entity
public class Mittaus extends AbstractEntity {

    private String toimenpide;
    private String yksikko;
    private String tulos;
    private LocalDateTime pvm;
    
    @ManyToOne
	@JoinColumn(name="henkilo_id")
	private Henkilo henkilo;
    
	public String getToimenpide() {
        return toimenpide;
    }
    public void setToimenpide(String toimenpide) {
        this.toimenpide = toimenpide;
    }
    public String getYksikko() {
        return yksikko;
    }
    public void setYksikko(String yksikko) {
        this.yksikko = yksikko;
    }
    public String getTulos() {
        return tulos;
    }
    public void setTulos(String tulos) {
        this.tulos = tulos;
    }
    public LocalDateTime getPvm() {
        return pvm;
    }
    public void setPvm(LocalDateTime pvm) {
        this.pvm = pvm;
    }
    
	public Henkilo getHenkilo() {
		return henkilo;
	}

	public void setHenkilo(Henkilo henkilo) {
		this.henkilo = henkilo;
	}

	public Mittaus() {
	
	}
	

}
