package online.taphoaptit.controller;

import org.springframework.web.bind.annotation.*;
import online.taphoaptit.entity.User;
import online.taphoaptit.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // 🧩 API Đăng ký
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        if (userRepo.findByUsername(username) != null) {
            return "Tên đăng nhập đã tồn tại!";
        }
        userRepo.save(new User(username, password));
        return "Đăng ký thành công!";
    }

    // 🧩 API Đăng nhập
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        User u = userRepo.findByUsername(username);
        if (u == null) return "Tài khoản không tồn tại!";
        if (!u.getPassword().equals(password)) return "Sai mật khẩu!";
        return "Đăng nhập thành công!";
    }
}
