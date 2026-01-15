package com.myproject.CyberTrace.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.CyberTrace.Model.Notification;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
List<Notification> findTop5ByOrderByPublishedAtDesc();

List<Notification> findTop20ByOrderByPublishedAtDesc();

}
