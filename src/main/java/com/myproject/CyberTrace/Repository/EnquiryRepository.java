package com.myproject.CyberTrace.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.myproject.CyberTrace.Model.Enquiry;

public interface EnquiryRepository extends JpaRepository<Enquiry,Long> {

    List<Enquiry> findTop3ByOrderByEnquiryDateDesc();

}
