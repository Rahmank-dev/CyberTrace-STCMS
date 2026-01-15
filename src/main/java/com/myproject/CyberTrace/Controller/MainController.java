package com.myproject.CyberTrace.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myproject.CyberTrace.API.SendEmail;
import com.myproject.CyberTrace.DTO.ComplaintDto;
import com.myproject.CyberTrace.DTO.EnquiryDto;
import com.myproject.CyberTrace.DTO.LoginDto;
import com.myproject.CyberTrace.Model.Complaint;
import com.myproject.CyberTrace.Model.Enquiry;
import com.myproject.CyberTrace.Model.Notification;
import com.myproject.CyberTrace.Model.Users;
import com.myproject.CyberTrace.Model.Complaint.ComplaintStatus;
import com.myproject.CyberTrace.Model.Users.UserRole;
import com.myproject.CyberTrace.Repository.ComplaintRepository;
import com.myproject.CyberTrace.Repository.EnquiryRepository;
import com.myproject.CyberTrace.Repository.NotificationRepository;
import com.myproject.CyberTrace.Repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EnquiryRepository enquiryRepo;

    @Autowired
    private ComplaintRepository complaintRepo;

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private SendEmail sendEmail;

    // index page
    // @GetMapping("/")
    // public String ShowIndex() {
    //     return "index";
    // }





@GetMapping("/")
public String ShowIndex(Model model) {
    List<Notification> recentNotifications =
            notificationRepo.findTop5ByOrderByPublishedAtDesc();
    model.addAttribute("recentNotifications", recentNotifications);
    return "index";
}






    // about us page
    @GetMapping("/about")
    public String ShowAbout() {
        return "about";
    }

    // track page
    @GetMapping("/track")
    public String ShowTrack() {
        return "track";
    }

    @PostMapping("/track")
    public String track(@RequestParam ("cid") String cid, Model model,RedirectAttributes attributes)
    {

        if (!complaintRepo.existsByComplaintId(cid)) {
            attributes.addFlashAttribute("msg", "Invalid complaint id ");
            return "redirect:/track";
        }

        Complaint complaint = complaintRepo.findByComplaintId(cid);
        model.addAttribute("complaint", complaint);
        return "track";
    }

    // ================================================================

    // contact us page
    @GetMapping("/contact")
    public String ShowContact(Model model) {
        EnquiryDto dto = new EnquiryDto();
        model.addAttribute("dto", dto);
        return "contact";
    }

    @PostMapping("/contact")
    public String SubmitQuery(@ModelAttribute("dto") EnquiryDto dto, RedirectAttributes attributes) {

        Enquiry enquiry = new Enquiry();
        enquiry.setName(dto.getName());
        enquiry.setGender(dto.getGender());
        enquiry.setContactNo(dto.getContactNo());
        enquiry.setAddress(dto.getAddress());
        enquiry.setEmail(dto.getEmail());
        enquiry.setSubject(dto.getSubject());
        enquiry.setMessage(dto.getMessage());
        enquiry.setEnquiryDate(LocalDateTime.now());

        enquiryRepo.save(enquiry);
        attributes.addFlashAttribute("msg", "Query succesfully submited");
        return "redirect:/contact";
    }

    // =====================================================

    // complaint page
    @GetMapping("/complaint")
    public String ShowComplaint(Model model) {
        ComplaintDto dto = new ComplaintDto();
        model.addAttribute("dto", dto);
        return "complaint";
    }

    @PostMapping("/complaint")
    public String complaint(@ModelAttribute("dto") ComplaintDto dto,
            @RequestParam("evidenceImages") MultipartFile[] evidenceImages, RedirectAttributes attributes)
            throws IOException {

        try {

            if (evidenceImages.length == 0 || evidenceImages[0].isEmpty()) {
                attributes.addFlashAttribute("msg", "you have to  upload atleast 1 images");
                return "redirect:/complaint";
            }

            if (evidenceImages.length > 5) {
                attributes.addFlashAttribute("msg", "you can upload only five images");
                return "redirect:/complaint";
            }

            String uploadDir = "public/uploads/";

            File folder = new File(uploadDir);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            List<String> filenames = new ArrayList<>();

            for (MultipartFile file : evidenceImages) {
                String storageFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir, storageFileName);

                Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

                filenames.add((storageFileName));
            }

            Complaint complaint = new Complaint();
            String cid = "CT-" + System.currentTimeMillis();

            complaint.setComplaintId(cid);
            complaint.setName(dto.getName());
            complaint.setEmail(dto.getEmail());
            complaint.setAddress(dto.getAddress());
            complaint.setContactNo(dto.getContactNo());
            complaint.setWhatsappNo(dto.getWhatsappNo());
            complaint.setTitle(dto.getTitle());
            complaint.setDescription(dto.getDescription());
            complaint.setCountry(dto.getCountry());
            complaint.setLostAmount(dto.getLostAmount());
            complaint.setPlatform(dto.getPlatform());
            complaint.setScammer(dto.getScammer());
            complaint.setTypeOfScam(dto.getTypeOfScam());
            complaint.setRegDateTime(LocalDateTime.now());
            complaint.setStatus(ComplaintStatus.PENDING);
            complaint.setEvidence(filenames);

            complaintRepo.save(complaint);
            sendEmail.sendComplaintSuccessMail(complaint);



            attributes.addFlashAttribute("msg", "your complaint has been successfully Registerd");

        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/complaint";
    }

    // =======================================================

    // Login page
    @PostMapping("/login")
    public String Login(@ModelAttribute("dto") LoginDto dto, RedirectAttributes attributes, HttpSession session) {
        try {

            if (!userRepo.existsByEmail(dto.getEmail())) {
                attributes.addFlashAttribute("msg", "User Not Found!");
                return "redirect:/login";
            }

            Users user = userRepo.findByEmail(dto.getEmail());
            if (user.getPassword().equals(dto.getPassword()) && user.getEmail().equals(dto.getEmail())) {

                if (user.getRole().equals(UserRole.ADMIN)) {
                    // admin
                    System.err.println("error found");

                    session.setAttribute("loggedInAdmin", user);
                    return "redirect:/Admin/Dashboard";
                } else {
                    // investigator
                    session.setAttribute("loggedInUser", user);
                    return "redirect:/Investigator/Dashboard";
                }
            }

            else {
                attributes.addFlashAttribute("msg", "Wrong user id or Password!");
            }

        } catch (Exception e) {
            attributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/login";

    }

    @GetMapping("/login")
    public String ShowLogin(org.springframework.ui.Model model) {
        LoginDto dto = new LoginDto();
        model.addAttribute("dto", dto);
        return "login";
    }

}
