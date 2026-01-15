package com.myproject.CyberTrace.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Model.Users;
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint,Long> {

    List<Complaint> findAllByStatus(ComplaintStatus pending);

    List<Complaint> findAllByStatusOrStatus(ComplaintStatus processing, ComplaintStatus resolved);

    Complaint findByComplaintId(String cid);

    long countByStatus(ComplaintStatus status);

List<Complaint> findTop5ByOrderByRegDateTimeDesc();

boolean existsByComplaintId(String cid);

List<Complaint> findAllByStatusAndAssignedTo(ComplaintStatus processing, Users investigator);



long countByStatusAndAssignedTo(ComplaintStatus status, Users assignedTo);

long countByAssignedToAndRegDateTimeBetween(Users assignedTo,
                                            LocalDateTime start,
                                            LocalDateTime end);

long countByAssignedToAndSolvedAtBetween(Users assignedTo,
                                         LocalDateTime start,
                                         LocalDateTime end);





List<Complaint> findTop5ByAssignedToOrderByRegDateTimeDesc(Users investigator);

@Query(value = """
        SELECT DATE_FORMAT(c.reg_date_time, '%Y-%m') AS month,
               COUNT(*) AS total
        FROM complaint c
        GROUP BY month
        ORDER BY month
        """, nativeQuery = true)
List<Object[]> findMonthlyComplaintCount();







}
