package com.example.labs.labs.Controllers;

import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.example.labs.labs.Models.User;
import com.example.labs.labs.Repositories.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/User")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public ModelAndView getUsers()
    {
        ModelAndView mav=new ModelAndView("list-users.html");
        List<User>users=this.userRepository.findAll();
        mav.addObject("users", users);
        return mav;  
    }

    @GetMapping("Registration")
    public ModelAndView getRegistration()
    {
        ModelAndView mav=new ModelAndView("registration.html");
        User newUser = new User();
        mav.addObject("user",newUser);
        return mav;
    }

    @PostMapping("Registration")
    public ModelAndView saveClient(@ModelAttribute User user)
    {
        String encoddedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        user.setPassword(encoddedPassword);
        this.userRepository.save(user);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/User/Login");
        return modelAndView;
    }

    @GetMapping("Login")
    public ModelAndView getLogin()
    {
        ModelAndView mav=new ModelAndView("login.html");
        User newUser = new User();
        mav.addObject("user",newUser);
        return mav;
    }

    @PostMapping("Login")
    public RedirectView LoginProcess(@RequestParam("username") String username,@RequestParam("password") String password, HttpSession session)
    {
        User user=this.userRepository.findByUsername(username);
        if (user != null) {
            Boolean isPasswordMatched = BCrypt.checkpw(password, user.getPassword());
            if (isPasswordMatched) {
                session.setAttribute("loggedInUser", user);
                return new RedirectView("/User/Profile");
            } else {
                return new RedirectView("/User/Login?error=wrongPassword");
            }
        } else {
            return new RedirectView("/User/Login?error=userNotFound");
        }
    }

    @GetMapping("Profile")
    public ModelAndView getProfile(HttpSession session)
    {
        ModelAndView mav=new ModelAndView("user-profile.html");
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            mav.setViewName("redirect:/User/Login");
        } else {
            mav.addObject("loggedInUser", loggedInUser);
        }
        return mav;
    }
    
    @PostMapping("Profile")
    public ModelAndView updateClient(@Valid @ModelAttribute User user,HttpSession session,@RequestParam("action") String action) {
        User sessionClient = (User) session.getAttribute("loggedInUser");
        ModelAndView modelAndView = new ModelAndView();

        if ("update".equals(action)) {
            sessionClient.setName(user.getName());
            sessionClient.setUsername(user.getUsername());
            sessionClient.setDob(user.getDob());
            sessionClient.setPassword(user.getPassword());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String encodedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
                sessionClient.setPassword(encodedPassword);
            }

            try {
                this.userRepository.save(sessionClient);
                session.setAttribute("loggedInUser", sessionClient);
                System.out.println("User updated successfully");
                modelAndView.setViewName("redirect:/User");
            } catch (Exception e) {
                System.out.println("Error updating user: " + e.getMessage());
            }
        } else if ("delete".equals(action)) {
            try {
                System.out.println(sessionClient.getId());
                this.userRepository.deleteById(sessionClient.getId());
                session.invalidate();
                System.out.println("User deleted");
                modelAndView.setViewName("redirect:/User");
            } catch (Exception e) {
                System.out.println("Error deleting user: " + e.getMessage());
            }
        }

        return modelAndView;
    }


}
