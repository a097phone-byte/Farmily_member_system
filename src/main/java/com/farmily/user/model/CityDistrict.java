package com.farmily.user.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "city_district")
public class CityDistrict implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id", updatable = false)
    private Integer districtId;

    @Column(name = "city_name")
    private String  cityName;

    @Column(name = "city_eng")
    private String  cityEng;

    @Column(name = "dist_name")
    private String  distName;

    @Column(name = "dist_eng")
    private String  distEng;

    @Column(name = "zipcode")
    private Short zipcode;

    @Column(name = "note")
    private String  note;

    @OneToMany(mappedBy = "cityDistrict", cascade = CascadeType.ALL)
    private Set<User> users;

    @OneToMany(mappedBy = "cityDistrict", cascade = CascadeType.ALL)
    private Set<Farmer> farmers;


    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityEng() {
        return cityEng;
    }

    public void setCityEng(String cityEng) {
        this.cityEng = cityEng;
    }

    public String getDistName() {
        return distName;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public String getDistEng() {
        return distEng;
    }

    public void setDistEng(String distEng) {
        this.distEng = distEng;
    }

    public Short getZipcode() {
        return zipcode;
    }

    public void setZipcode(Short zipcode) {
        this.zipcode = zipcode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Farmer> getFarmers() {
        return farmers;
    }

    public void setFarmers(Set<Farmer> farmers) {
        this.farmers = farmers;
    }
}
