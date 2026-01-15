package com.myproject.CyberTrace.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Notification {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private long id ;
@Column (nullable = false, length=500,unique = true)
private String message;
@Enumerated
private NotificationStatus status;
private LocalDateTime publishedAt;


public enum NotificationStatus{
    RUNNING,OVER
}


public long getId() {
    return id;
}


public void setId(long id) {
    this.id = id;
}


public String getMessage() {
    return message;
}


public void setMessage(String message) {
    this.message = message;
}


public NotificationStatus getStatus() {
    return status;
}


public void setStatus(NotificationStatus status) {
    this.status = status;
}


public LocalDateTime getPublishedAt() {
    return publishedAt;
}


public void setPublishedAt(LocalDateTime publishedAt) {
    this.publishedAt = publishedAt;
}




}


