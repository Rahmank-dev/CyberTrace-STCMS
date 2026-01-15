package com.myproject.CyberTrace.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.CyberTrace.DTO.UserDto;
import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Model.Notification.NotificationStatus;
import com.myproject.CyberTrace.Model.Enquiry;
import com.myproject.CyberTrace.Model.Notification;
import com.myproject.CyberTrace.Model.Users;
import com.myproject.CyberTrace.Model.Users.UserRole;
import com.myproject.CyberTrace.Model.Users.UserStatus;
import com.myproject.CyberTrace.Repository.ComplaintRepository;
import com.myproject.CyberTrace.Repository.EnquiryRepository;
import com.myproject.CyberTrace.Repository.NotificationRepository;
import com.myproject.CyberTrace.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Admin")
public class AdminController {

    @Autowired
    private HttpSession session;

    @Autowired
    private EnquiryRepository enquiryRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ComplaintRepository complaintRepo;

    @Autowired
    private NotificationRepository notificationRepo;

  @GetMapping("/Dashboard")
public String ShowDashboard(Model model) {
    if (session.getAttribute("loggedInAdmin") == null) {
        return "redirect:/login";
    }

    long totalComplaints    = complaintRepo.count();
    long pendingComplaints  = complaintRepo.countByStatus(ComplaintStatus.PENDING);
    long resolvedComplaints = complaintRepo.countByStatus(ComplaintStatus.RESOLVED);
    long totalInvestigators = userRepo.countByRole(UserRole.INVESTIGATOR);

    List<Notification> recentNotifications =
            notificationRepo.findTop5ByOrderByPublishedAtDesc();

            List<Enquiry> topEnquiries = enquiryRepo.findTop3ByOrderByEnquiryDateDesc();
model.addAttribute("topEnquiries", topEnquiries);

    // DB se month-wise count: "2025-01", 5 etc.
    List<Object[]> monthly = complaintRepo.findMonthlyComplaintCount();

    // map: monthNumber -> count
    Map<Integer, Long> monthCountMap = new HashMap<>();
    for (Object[] row : monthly) {
        String ym = (String) row[0];       // "2025-01"
        int month = Integer.parseInt(ym.substring(5, 7)); // 1..12
        long cnt  = ((Number) row[1]).longValue();
        monthCountMap.put(month, cnt);
    }

    List<String> monthLabels = Arrays.asList(
            "Jan","Feb","Mar","Apr","May","Jun",
            "Jul","Aug","Sep","Oct","Nov","Dec"
    );
    List<Long> monthCounts = new ArrayList<>();
    for (int m = 1; m <= 12; m++) {
        monthCounts.add(monthCountMap.getOrDefault(m, 0L));
    }

    model.addAttribute("totalComplaints", totalComplaints);
    model.addAttribute("pendingComplaints", pendingComplaints);
    model.addAttribute("resolvedComplaints", resolvedComplaints);
    model.addAttribute("totalInvestigators", totalInvestigators);
    model.addAttribute("recentNotifications", recentNotifications);
    model.addAttribute("monthLabels", monthLabels);
    model.addAttribute("monthCounts", monthCounts);

    return "Admin/Dashboard";
}


    @GetMapping("/Add-Investigator")
    public String ShowAddInvestigator(Model model) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        UserDto dto = new UserDto();
        model.addAttribute("dto", dto);
        return "Admin/AddInvestigator";
    }

    @PostMapping("/Add-Investigator")
    public String AddInvestigator(@ModelAttribute("dto") UserDto dto, RedirectAttributes attributes)

    {
        try {

            String storageProfilePicName = System.currentTimeMillis() + "_"
                    + dto.getProfilePic().getOriginalFilename().replaceAll("\s+", "_");
            String storageIDProofName = System.currentTimeMillis() + "_"
                    + dto.getGovtIdProof().getOriginalFilename().replaceAll("\s+", "_");

            String profileUploadDir = "Public/Profiles/";
            String idProofUploadDir = "Pubic/IdProof/";

            File profileFolder = new File(profileUploadDir);
            File idproofFolder = new File(idProofUploadDir);

            if (!profileFolder.exists()) {

                profileFolder.mkdirs();

            }

            if (!idproofFolder.exists()) {
                idproofFolder.mkdirs();
            }

            Path profilePath = Paths.get(profileUploadDir, storageProfilePicName);
            Path idproofPath = Paths.get(idProofUploadDir, storageIDProofName);

            Files.copy(dto.getProfilePic().getInputStream(), profilePath, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(dto.getGovtIdProof().getInputStream(), idproofPath, StandardCopyOption.REPLACE_EXISTING);

            // Setting data into database

            Users investigator = new Users();
            investigator.setName(dto.getName());
            investigator.setGender(dto.getGender());
            investigator.setContactNo(dto.getContactNo());
            investigator.setEmail(dto.getEmail());
            investigator.setAddress(dto.getAddress());
            investigator.setPassword(dto.getPassword());
            investigator.setProfilePic(storageProfilePicName);
            investigator.setGovtIdProof(storageIDProofName);
            investigator.setStatus(UserStatus.UNBLOCKED);
            investigator.setRole(UserRole.INVESTIGATOR);
            investigator.setRegDate(LocalDateTime.now());

            userRepo.save(investigator);
            attributes.addFlashAttribute("msg", "Data Successfully Added");

        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/Admin/Add-Investigator";
    }

    @GetMapping("/Manage-Investigator")
    public String ShowManageInvestigator(Model model) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }
        List<Users> investigators = userRepo.findAllByRole(UserRole.INVESTIGATOR);
        model.addAttribute("investigators", investigators);
        return "Admin/ManageInvestigator";
    }

    @GetMapping("/UpdateStatus/{id}")
    public String UpdateStatus(@PathVariable Long id) {
        Users investigator = userRepo.findById(id).get();
        if (investigator.getStatus().equals(UserStatus.UNBLOCKED)) {
            investigator.setStatus(UserStatus.BLOCKED);
        } else if (investigator.getStatus().equals(UserStatus.BLOCKED)) {
            investigator.setStatus(UserStatus.UNBLOCKED);
        }
        userRepo.save(investigator);

        return "redirect:/Admin/Manage-Investigator";
    }

    @GetMapping("/EditInvestigator/{id}")
    public String showEditInvestigator(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        Users investigator = userRepo.findById(id).orElse(null);
        if (investigator == null) {
            ra.addFlashAttribute("msg", "Investigator not found.");
            return "redirect:/Admin/Manage-Investigator";
        }

        model.addAttribute("investigator", investigator);
        return "Admin/EditInvestigator";
    }

    @PostMapping("/EditInvestigator")
    public String updateInvestigator(@ModelAttribute("investigator") Users form,
            HttpSession session,
            RedirectAttributes ra) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        Users existing = userRepo.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid investigator id"));

        existing.setName(form.getName());
        existing.setGender(form.getGender());
        existing.setContactNo(form.getContactNo());
        existing.setEmail(form.getEmail());
        existing.setAddress(form.getAddress());
        // Role / status ko yahi se change nahi karna to mat chhed

        userRepo.save(existing);
        ra.addFlashAttribute("msg", "Investigator updated successfully.");

        return "redirect:/Admin/Manage-Investigator";
    }

    @GetMapping("/Enquiry")
    public String ShowEnquiry(Model model) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        List<Enquiry> enquiries = enquiryRepo.findAll();
        model.addAttribute("enquiries", enquiries);

        return "Admin/Enquiry";
    }

    @GetMapping("/PendingComplaint")
    public String ShowPendingComplaint(Model model) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }
        List<Complaint> complaints = complaintRepo.findAllByStatus(ComplaintStatus.PENDING);
        model.addAttribute("complaints", complaints);

        List<Users> investigators = userRepo.findAllByRole(UserRole.INVESTIGATOR);
        model.addAttribute("investigators", investigators);
        return "Admin/PendingComplaint";
    }

    @PostMapping("/AssignComplaint")
    public String AssignComplaint(@RequestParam("assignTo") long uid, @RequestParam("cid") long cid,
            RedirectAttributes attributes) {
        try {
            Users investigators = userRepo.findById(uid).get();
            Complaint complaint = complaintRepo.findById(cid).get();
            complaint.setAssignedTo(investigators);
            complaint.setStatus(ComplaintStatus.PROCESSING);
            complaintRepo.save(complaint);
            attributes.addFlashAttribute("msg", "Successfully Assigned ");
        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/Admin/PendingComplaint";
    }

    @PostMapping("/RejectComplaint")
    public String RejectComplaint(@RequestParam("cid") long cid, @RequestParam("message") String message,
            RedirectAttributes attributes) {
        Complaint complaint = complaintRepo.findById(cid).get();
        complaint.setMessage(message);
        complaint.setStatus(ComplaintStatus.TERMINATED);
        complaintRepo.save(complaint);
        attributes.addFlashAttribute("msg", "Complaint Succesfully Rejected");
        return "redirect:/Admin/PendingComplaint";
    }

    @GetMapping("/ManageComplaint")
    public String showManageComplaint(Model model) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }
        List<Complaint> complaints = complaintRepo.findAllByStatusOrStatus(ComplaintStatus.PROCESSING,
                ComplaintStatus.RESOLVED);
        model.addAttribute("complaints", complaints);
        return "Admin/ManageComplaint";
    }

    @GetMapping("/RejectedComplaints")
    public String showRejectedComplaints(Model model) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        List<Complaint> rejectedComplaints = complaintRepo.findAllByStatus(ComplaintStatus.TERMINATED);

        model.addAttribute("complaints", rejectedComplaints);
        return "Admin/RejectedComplaints";
    }

    @GetMapping("/AddNotification")
    public String showAddNotification(HttpSession session, Model model) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        // latest 20 notifications table ke liye
        List<Notification> notifications = notificationRepo.findTop20ByOrderByPublishedAtDesc();
        model.addAttribute("notifications", notifications);

        return "Admin/AddNotification";
    }

    @PostMapping("/AddNotification")
    public String publishNotification(@RequestParam("message") String message,
            RedirectAttributes attributes,
            HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setPublishedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.RUNNING);

        notificationRepo.save(notification);
        attributes.addFlashAttribute("msg", "Notification published successfully.");

        return "redirect:/Admin/AddNotification";
    }

    @PostMapping("/DeleteNotification")
    public String deleteNotification(@RequestParam("id") Long id,
            RedirectAttributes attributes,
            HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }

        notificationRepo.deleteById(id);
        attributes.addFlashAttribute("msg", "Notification deleted.");

        return "redirect:/Admin/AddNotification";
    }

    @GetMapping("/ChangePassword")
    public String ShowChangePassword() {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/login";
        }
        return "Admin/ChangePassword";
    }

    @PostMapping("/ChangePassword")
    public String ChangePassword(HttpServletRequest request, RedirectAttributes attributes) {
        try {
            String oldPass = request.getParameter("oldPass");
            String newPass = request.getParameter("newPass");
            String confirmPass = request.getParameter("confirmPass");

            Users admin = (Users) session.getAttribute("loggedInAdmin");

            if (!newPass.equals(confirmPass)) {
                attributes.addFlashAttribute("msg", "New Password and confirm Password must be same.");
                return "redirect:/Admin/ChangePassword";
            }

            if (newPass.equals(admin.getPassword())) {
                attributes.addFlashAttribute("msg", "Old Password and new Password Cannot be same ");
                return "redirect:/Admin/ChangePassword";
            }

            if (oldPass.equals(admin.getPassword())) {

                admin.setPassword(confirmPass);
                userRepo.save(admin);
                session.removeAttribute("loggedInAdmin");
                attributes.addFlashAttribute("msg", "Password Change succesfully");
            } else {
                attributes.addFlashAttribute("msg", "Invalid old Password");
            }

        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }

        return "redirect:/Admin/ChangePassword";
    }

    @GetMapping("/Logout")
    public String Logout(RedirectAttributes attributes) {
        session.removeAttribute("loggedInAdmin");
        attributes.addFlashAttribute("msg", "Logout Succesfull");

        return "redirect:/login";
    }





@GetMapping("/UpdateProfilePic")
public String showUpdateProfilePic(HttpSession session, Model model) {
    if (session.getAttribute("loggedInAdmin") == null) {
        return "redirect:/login";
    }
    return "Admin/UpdateProfilePic";
}

@PostMapping("/UpdateProfilePic")
public String updateProfilePic(@RequestParam("file") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes ra) {
    Users admin = (Users) session.getAttribute("loggedInAdmin");
    if (admin == null) return "redirect:/login";

    try {
        if (file.isEmpty()) {
            ra.addFlashAttribute("msg", "Please select an image.");
            return "redirect:/Admin/UpdateProfilePic";
        }

        String uploadDir = "Public/ProfilePic/";
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String fileName = System.currentTimeMillis() + "_" +
                file.getOriginalFilename().replaceAll("\\s+", "_");

        Path path = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        admin.setProfilePic(fileName);
        userRepo.save(admin);
        session.setAttribute("loggedInAdmin", admin);

        ra.addFlashAttribute("msg", "Profile picture updated successfully.");
    } catch (Exception e) {
        ra.addFlashAttribute("msg", "Error: " + e.getMessage());
    }

    return "redirect:/Admin/UpdateProfilePic";
}
}

