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

    @PostMapping("/register")
    public String register(@RequestBody User req) {
        if (req.getUsername() == null || req.getPassword() == null)
            return "Thiếu thông tin đăng ký!";

        if (userRepo.findByUsername(req.getUsername()) != null)
            return "Tên người dùng đã tồn tại!";

        userRepo.save(req);
        return "Đăng ký thành công!";
    }

    @PostMapping("/login")
    public String login(@RequestBody User req) {
        if (req.getUsername() == null || req.getPassword() == null)
            return "Thiếu thông tin đăng nhập!";

        User user = userRepo.findByUsername(req.getUsername());
        if (user == null)
            return "Tài khoản không tồn tại!";
        if (!user.getPassword().equals(req.getPassword()))
            return "Sai mật khẩu!";

        return "Đăng nhập thành công!";
    }
}
