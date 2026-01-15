package com.myproject.CyberTrace.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Users;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Repository.ComplaintRepository;
import com.myproject.CyberTrace.Repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Investigator")
public class InvestigatorController {

    @Autowired
    private HttpSession session;

    @Autowired
    private ComplaintRepository complaintRepo;

    @Autowired
    private UserRepository userRepo;


    @GetMapping("/Dashboard")
public String showDashboard(Model model) {
    Users investigator = (Users) session.getAttribute("loggedInUser");
    if (investigator == null) {
        return "redirect:/login";
    }

    Long assignedCount =
            complaintRepo.countByStatusAndAssignedTo(ComplaintStatus.PROCESSING, investigator);
    Long resolvedCount =
            complaintRepo.countByStatusAndAssignedTo(ComplaintStatus.RESOLVED, investigator);
    Long rejectedCount =
            complaintRepo.countByStatusAndAssignedTo(ComplaintStatus.TERMINATED, investigator);

    LocalDate today = LocalDate.now();
    LocalDateTime start = today.atStartOfDay();
    LocalDateTime end = today.plusDays(1).atStartOfDay();

    Long todayAssigned =
            complaintRepo.countByAssignedToAndRegDateTimeBetween(investigator, start, end);
    Long todayResolved =
            complaintRepo.countByAssignedToAndSolvedAtBetween(investigator, start, end);

    List<Complaint> recent =
            complaintRepo.findTop5ByAssignedToOrderByRegDateTimeDesc(investigator);

    model.addAttribute("assignedCount", assignedCount);
    model.addAttribute("resolvedCount", resolvedCount);
    model.addAttribute("rejectedCount", rejectedCount);
    model.addAttribute("todayAssigned", todayAssigned);
    model.addAttribute("todayResolved", todayResolved);
    model.addAttribute("recentComplaints", recent);

    return "Investigator/Dashboard";
}



    @GetMapping("/logout")
    public String logout(HttpSession session)
    {

        session.removeAttribute("loggedInUser");
        return "redirect:/login";
    }


    // Assigned Complaint
    @GetMapping("/AssignedComplaints")
    public String showAssignedComplaints(Model model)
    {
        if (session.getAttribute("loggedInUser")==null) {
            return "redirect:/login";
        }
       
        Users investigator = (Users) session.getAttribute("loggedInUser");

        List<Complaint> complaints = complaintRepo.findAllByStatusAndAssignedTo(ComplaintStatus.PROCESSING,investigator);
        model.addAttribute("complaints", complaints);
        return "Investigator/AssignedComplaints";
    }



    @PostMapping("/CloseComplaint")
    public String CloseComplaint(@RequestParam ("cid")Long cid,@RequestParam("message")String message,RedirectAttributes attributes)
    {
        try {
            Complaint complaint = complaintRepo.findById(cid).get();
            complaint.setMessage(message);
            complaint.setStatus(ComplaintStatus.RESOLVED);
            complaint.setSolvedAt(LocalDateTime.now());
            complaintRepo.save(complaint);
            attributes.addFlashAttribute("msg", "Complaint Succesfully Closed");
        } catch (Exception e) {
           attributes.addFlashAttribute("msg", e.getMessage()); 
        }
        return "redirect:/Investigator/AssignedComplaints";
    }



     @GetMapping("/ClosedComplaints")
    public String showClosedComplaints(Model model)
    {
        if (session.getAttribute("loggedInUser")==null) {
            return "redirect:/login";
        }
        Users investigator = (Users) session.getAttribute("loggedInUser");

    List<Complaint> complaints =
            complaintRepo.findAllByStatusAndAssignedTo(ComplaintStatus.RESOLVED, investigator);

    model.addAttribute("complaints", complaints);
       
        return "Investigator/ClosedComplaints";
    }



    @GetMapping("/RejectedComplaints")
    public String showRejectedComplaints(Model model)
    {
        if (session.getAttribute("loggedInUser")==null) {
            return "redirect:/login";
        }
        Users investigator = (Users) session.getAttribute("loggedInUser");
    List<Complaint> complaints =
        complaintRepo.findAllByStatusAndAssignedTo(ComplaintStatus.TERMINATED, investigator);
    model.addAttribute("complaints", complaints);
    return "Investigator/RejectedComplaints";
       
        
    }

    @PostMapping("/RejectComplaint")
public String rejectComplaint(@RequestParam("cid") Long cid,
                              @RequestParam("message") String message,
                              RedirectAttributes attributes) {
    try {
        Complaint complaint = complaintRepo.findById(cid).orElseThrow();
        complaint.setMessage(message);
        complaint.setStatus(ComplaintStatus.TERMINATED);   // ya REJECTED jo enum hai
        complaint.setSolvedAt(LocalDateTime.now());
        complaintRepo.save(complaint);
        attributes.addFlashAttribute("msg", "Complaint rejected successfully");
    } catch (Exception e) {
        attributes.addFlashAttribute("msg", e.getMessage());
    }
    return "redirect:/Investigator/AssignedComplaints";
}




    @GetMapping("/ViewProfile")
public String showViewProfile(Model model) {
    Users investigator = (Users) session.getAttribute("loggedInUser");
    if (investigator == null) return "redirect:/login";

    model.addAttribute("investigator", investigator);
    return "Investigator/ViewProfile";
}




@PostMapping("/UpdateProfile")
public String updateProfile(@ModelAttribute("investigator") Users formUser,
                            RedirectAttributes attributes) {

    Users sessionUser = (Users) session.getAttribute("loggedInUser");
    if (sessionUser == null) return "redirect:/login";

    try {
        // sirf allowed fields update karo
        sessionUser.setName(formUser.getName());
        sessionUser.setContactNo(formUser.getContactNo());
        sessionUser.setGender(formUser.getGender());
        sessionUser.setAddress(formUser.getAddress());
        sessionUser.setGovtIdProof(formUser.getGovtIdProof());

        userRepo.save(sessionUser);
        session.setAttribute("loggedInUser", sessionUser);   // session fresh

        attributes.addFlashAttribute("msg", "Profile updated successfully");
    } catch (Exception e) {
        attributes.addFlashAttribute("msg", "Error: " + e.getMessage());
    }
    return "redirect:/Investigator/ViewProfile";
}



// ======================= change passowrd and update Profile Pic =============
@GetMapping("/UpdateProfilePic")
public String showUpdateProfilePic() {
    if (session.getAttribute("loggedInUser") == null) {
        return "redirect:/login";
    }
    return "Investigator/UpdateProfilePic";
}

@PostMapping("/UpdateProfilePic")
public String updateProfilePic(@RequestParam("image") MultipartFile file,
                               RedirectAttributes attributes) {
    Users user = (Users) session.getAttribute("loggedInUser");
    if (user == null) return "redirect:/login";

    try {
        if (file.isEmpty()) {
            attributes.addFlashAttribute("msg", "Please select an image");
            return "redirect:/Investigator/UpdateProfilePic";
        }

        String uploadDir = "public/Profiles/";
        File folder = new File (uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        user.setProfilePic(fileName);
        userRepo.save(user);
        session.setAttribute("loggedInUser", user);

        attributes.addFlashAttribute("msg", "Profile picture updated successfully");
    } catch (Exception e) {
        attributes.addFlashAttribute("msg", "Error: " + e.getMessage());
    }
    return "redirect:/Investigator/ViewProfile";
}

@GetMapping("/ChangePassword")
public String showChangePassword() {
    if (session.getAttribute("loggedInUser") == null) {
        return "redirect:/login";
    }
    return "Investigator/ChangePassword";
}

@PostMapping("/ChangePassword")
public String changePassword(@RequestParam("currentPassword") String currentPassword,
                             @RequestParam("newPassword") String newPassword,
                             @RequestParam("confirmPassword") String confirmPassword,
                             RedirectAttributes attributes) {
    Users user = (Users) session.getAttribute("loggedInUser");
    if (user == null) return "redirect:/login";

    try {
        if (!user.getPassword().equals(currentPassword)) {
            attributes.addFlashAttribute("msg", "Current password is incorrect");
            return "redirect:/Investigator/ChangePassword";
        }
        if (!newPassword.equals(confirmPassword)) {
            attributes.addFlashAttribute("msg", "New password and confirm password do not match");
            return "redirect:/Investigator/ChangePassword";
        }
        if (newPassword.length() < 6) {
            attributes.addFlashAttribute("msg", "Password must be at least 6 characters");
            return "redirect:/Investigator/ChangePassword";
        }

        user.setPassword(newPassword);          // prod me hashing use karna
        userRepo.save(user);
        session.setAttribute("loggedInUser", user);

        attributes.addFlashAttribute("msg", "Password changed successfully");
    } catch (Exception e) {
        attributes.addFlashAttribute("msg", "Error: " + e.getMessage());
    }
    return "redirect:/Investigator/ChangePassword";
}




}
