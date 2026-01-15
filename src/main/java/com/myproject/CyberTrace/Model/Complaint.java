package com.myproject.CyberTrace.Model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) 
    private String complaintId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactNo;

    @Column(nullable = false)
    private String whatsappNo;
    private String email;
    private String country;
    private String address;

    @Column(nullable = false)
    private String typeOfScam;


    private String Platform;
    private String title;

    @Column(length = 1000, nullable = false)
    private String description;


    private Double lostAmount;
    private String scammer;

    @ElementCollection
    private List<String> evidence;


    private LocalDateTime scamTime;
    private LocalDateTime regDateTime;

    private LocalDateTime solvedAt;

    @Column (length = 500)
    private String message;

    //private Assigned to ------>>
    @ManyToOne
    private Users assignedTo;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    public enum ComplaintStatus
    {
        PENDING,PROCESSING,RESOLVED,TERMINATED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getWhatsappNo() {
        return whatsappNo;
    }

    public void setWhatsappNo(String whatsappNo) {
        this.whatsappNo = whatsappNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTypeOfScam() {
        return typeOfScam;
    }

    public void setTypeOfScam(String typeOfScam) {
        this.typeOfScam = typeOfScam;
    }

    public String getPlatform() {
        return Platform;
    }

    public void setPlatform(String platform) {
        Platform = platform;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLostAmount() {
        return lostAmount;
    }

    public void setLostAmount(Double lostAmount) {
        this.lostAmount = lostAmount;
    }

    public String getScammer() {
        return scammer;
    }

    public void setScammer(String scammer) {
        this.scammer = scammer;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }

    public LocalDateTime getScamTime() {
        return scamTime;
    }

    public void setScamTime(LocalDateTime scamTime) {
        this.scamTime = scamTime;
    }

    public LocalDateTime getRegDateTime() {
        return regDateTime;
    }

    public void setRegDateTime(LocalDateTime regDateTime) {
        this.regDateTime = regDateTime;
    }

    public LocalDateTime getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
    }

    public Users getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Users assignedTo) {
        this.assignedTo = assignedTo;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
