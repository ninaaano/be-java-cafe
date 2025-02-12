package kr.codesqaud.cafe.controller;


import kr.codesqaud.cafe.domain.Member;
import kr.codesqaud.cafe.repository.Member.JdbcMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
public class MemberController {

    private JdbcMemberRepository jdbcMemberRepository;


    Logger logger = LoggerFactory.getLogger(MemberController.class);

    public MemberController(JdbcMemberRepository jdbcMemberRepository) {
        this.jdbcMemberRepository = jdbcMemberRepository;
        System.out.println(this.getClass());
    }

    @PostMapping("/users")
    public String addUser(@ModelAttribute("user") Member member) {
        logger.debug("addUser");
        jdbcMemberRepository.save(member);
        return "redirect:/login";
    }

    @GetMapping("/list")
    public String getUserList(Model model) {
        logger.debug("getUserList");
        List<Member> userList = jdbcMemberRepository.findAll();
        model.addAttribute("users", userList);
        return "user/list";
    }

    @RequestMapping("/profile/{userId}")
    public String getUser(@PathVariable String userId, Model model) {
        Optional<Member> user = jdbcMemberRepository.findById(userId);
        model.addAttribute("profile", user.orElseThrow(IllegalArgumentException::new));
        return "user/profile";
    }

    @GetMapping("/users/{userId}/form")
    public String updateUser(@PathVariable("userId") String userId, Model model,HttpSession session) {
        logger.debug("updateUser : GET");
        Object value = session.getAttribute("sessionedUser");
        if (value != null) {
            Optional<Member> updateUser = jdbcMemberRepository.findById(userId);
            if (updateUser.isPresent()) {
                model.addAttribute("user", updateUser.get());
            } else {
                throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + userId);
            }
        }
        return "user/updateForm";
    }

    @PutMapping("/users/{userId}")
    public String updateUser(@ModelAttribute("user") Member member) {
        logger.debug("updateUser : PUT");
        jdbcMemberRepository.update(member);
        return "redirect:/users";
    }

    @GetMapping("/login")
    public String login() throws Exception{
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user")Member member, HttpSession session,Model model){
        logger.debug("login {} ", member);
        Member existUser = jdbcMemberRepository.findById(member.getUserId()).orElse(null);

        if(existUser!=null && member.getPassword().equals(existUser.getPassword())){
            session.setAttribute("sessionedUser",existUser);
            return "redirect:/";
        } else {
            model.addAttribute("notExist", "fail");
            return "user/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "user/login";
    }


}
