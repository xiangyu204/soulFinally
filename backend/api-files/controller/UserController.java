package com.example.soul.controller;

import com.example.soul.entity.Message;
import com.example.soul.entity.User;
import com.example.soul.repository.UserRepository;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器：负责登录、注册（含用户名可用性检查）、用户资料查询/修改、头像上传，
 * 以及供 Web 前端使用的兼容别名接口（/api/profile、/api/upload）。
 * 依赖 BaseApiController 中的 userRepository、uploadDir 等公共成员。
 */
@RestController
public class UserController extends BaseApiController {

    // ============ 登录 ============

    /**
     * POST /api/login —— 用户登录。
     *
     * @param req 登录请求（username 用户名、password 密码）
     * @return LoginResponse：success=true 时携带用户基本信息；失败时 success=false 且仅返回提示信息
     * 业务规则：按用户名查找用户，并直接比较密码是否一致。
     */
    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        User u = userRepository.findByUsername(req.username()).orElse(null);
        if (u != null && u.password.equals(req.password())) {
            return new LoginResponse(true, u.name, u.username, u.email, u.avatar, u.role, "로그인 성공");
        }
        return new LoginResponse(false, null, null, null, null, null, "아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    // ============ 注册 ============

    /**
     * 注册请求 DTO。
     * username 登录用户名；name 显示名（为空时使用 username）；email 邮箱；password 密码；
     * age 年龄；gender 性别；phone 电话；nationality 国籍；address 地址；
     * timeSlot 可用时间段；avatar 头像地址。
     */
    public record RegisterRequest(String username, String name, String email, String password,
                                  Integer age, String gender, String phone, String nationality,
                                  String address, String timeSlot, String avatar) {}

    /**
     * GET /api/register/check —— 注册时检查用户名是否可用。
     *
     * @param username 待检查的用户名
     * @return Map：available（是否可用）、message（提示信息）
     * 业务规则：必须为 3-20 位字母、数字或下划线；且不能与已有的用户名或显示名重复。
     */
    @GetMapping("/api/register/check")
    public java.util.Map<String, Object> checkUsername(@RequestParam("username") String username) {
        String value = username == null ? "" : username.trim();
        // 格式校验：仅允许字母、数字、下划线，长度 3-20
        boolean valid = value.matches("^[A-Za-z0-9_]{3,20}$");
        if (!valid) {
            return java.util.Map.of("available", false, "message", "用户名需 3-20 位字母、数字或下划线");
        }
        // 用户名与显示名两个维度都要保证唯一
        boolean usernameTaken = userRepository.existsByUsername(value);
        boolean nameTaken = userRepository.existsByName(value);
        if (usernameTaken || nameTaken) {
            return java.util.Map.of("available", false, "message", "该名字已被占用，请换一个");
        }
        return java.util.Map.of("available", true, "message", "可以使用");
    }

    /**
     * POST /api/register —— 用户注册。
     *
     * @param req 注册请求（见 RegisterRequest）
     * @return LoginResponse：成功时返回新用户信息；失败时 success=false 并给出原因
     * 业务规则：用户名、密码不能为空；用户名不能已存在；显示名不能已被使用；
     * 新用户角色默认为 USER，初始平均评分 4.0、评分次数 0。
     */
    @PostMapping("/api/register")
    public LoginResponse register(@RequestBody RegisterRequest req) {
        if (req.username() == null || req.username().isBlank()) {
            return new LoginResponse(false, null, null, null, null, null, "用户名不能为空");
        }
        if (req.password() == null || req.password().isBlank()) {
            return new LoginResponse(false, null, null, null, null, null, "密码不能为空");
        }
        String username = req.username().trim();
        if (userRepository.findByUsername(username).isPresent()) {
            return new LoginResponse(false, null, null, null, null, null, "用户名已存在");
        }
        // 显示名为空时回退使用用户名，并检查显示名唯一性
        String displayName = (req.name() == null || req.name().isBlank()) ? username : req.name().trim();
        if (userRepository.existsByName(displayName)) {
            return new LoginResponse(false, null, null, null, null, null, "该姓名已被使用，请换一个");
        }
        // 创建新用户并设置默认值（角色、初始评分）
        User u = new User();
        u.username = username;
        u.name = (req.name() == null || req.name().isBlank()) ? username : req.name().trim();
        u.email = req.email() == null ? "" : req.email().trim();
        u.password = req.password();
        u.role = "USER";
        u.averageRating = 4.0;
        u.ratingCount = 0;
        // 以下可选字段仅在非空时才写入，并去除首尾空格
        if (req.age() != null) u.age = req.age();
        if (req.gender() != null && !req.gender().isBlank()) u.gender = req.gender().trim();
        if (req.phone() != null && !req.phone().isBlank()) u.phone = req.phone().trim();
        if (req.nationality() != null && !req.nationality().isBlank()) u.nationality = req.nationality().trim();
        if (req.address() != null && !req.address().isBlank()) u.address = req.address().trim();
        if (req.timeSlot() != null && !req.timeSlot().isBlank()) u.timeSlot = req.timeSlot().trim();
        if (req.avatar() != null && !req.avatar().isBlank()) u.avatar = req.avatar().trim();
        // 邮箱兜底：保证不会保存 null
        if (u.email == null || u.email.isBlank()) {
            u.email = req.email() == null ? "" : req.email().trim();
        }
        userRepository.save(u);
        return new LoginResponse(true, u.name, u.username, u.email, u.avatar, u.role, "注册成功");
    }

    // ============ 资料 ============

    /**
     * GET /api/user/profile —— 查询用户资料。
     *
     * @param username 用户名
     * @return ProfileResponse 用户资料
     * 业务规则：用户不存在时抛出异常。
     */
    @GetMapping("/api/user/profile")
    public ProfileResponse getProfile(@RequestParam("username") String username) {
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        return toProfile(u);
    }

    /**
     * PUT /api/user/profile —— 更新用户资料（按 username 定位）。
     *
     * @param p 资料内容（姓名、邮箱、性别、年龄、技能及等级、国籍、电话、地址、时间段、评分等）
     * @return 保存后的 ProfileResponse
     * 业务规则：用户不存在时会新建用户并使用默认密码；评分字段仅在传入非空时才覆盖。
     */
    @PutMapping("/api/user/profile")
    public ProfileResponse updateProfile(@RequestBody ProfileResponse p) {
        User u = userRepository.findByUsername(p.username()).orElse(null);
        if (u == null) {
            // 用户不存在则新建，并设置默认密码
            u = new User();
            u.username = p.username();
            u.password = "123456";
        }
        u.name = p.name();
        u.email = p.email();
        u.gender = p.gender();
        u.age = p.age();
        u.skillOffer = p.skillOffer();
        u.skillWant = p.skillWant();
        u.skillOfferLevel = p.skillOfferLevel();
        u.skillWantLevel = p.skillWantLevel();
        u.nationality = p.nationality();
        u.phone = p.phone();
        u.address = p.address();
        u.timeSlot = p.timeSlot();
        // 评分信息仅在请求中提供时才更新，避免被 null 覆盖
        if (p.averageRating() != null) u.averageRating = p.averageRating();
        if (p.ratingCount() != null) u.ratingCount = p.ratingCount();
        return toProfile(userRepository.save(u));
    }

    /**
     * POST /api/user/avatar —— 上传用户头像（multipart/form-data）。
     *
     * @param username 用户名
     * @param file 头像文件
     * @return AvatarUploadResponse：success、可访问的文件地址（/uploads/...）、提示信息
     * 业务规则：文件保存到 uploadDir 目录；文件名加 UUID 前缀防止重名；
     * 用户存在时同步更新其 avatar 字段；发生异常时返回 success=false 及错误信息。
     */
    @PostMapping("/api/user/avatar")
    public AvatarUploadResponse uploadAvatar(@RequestParam("username") String username,
                                             @RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(uploadDir);
            // 上传目录不存在时自动创建
            if (!dir.exists()) dir.mkdirs();
            // 用 UUID 作为前缀，避免同名文件覆盖
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(new File(dir, filename).getAbsoluteFile());
            // 将头像访问路径保存到用户记录
            User u = userRepository.findByUsername(username).orElse(null);
            if (u != null) {
                u.avatar = "/uploads/" + filename;
                userRepository.save(u);
            }
            return new AvatarUploadResponse(true, "/uploads/" + filename, "ok");
        } catch (Exception e) {
            return new AvatarUploadResponse(false, null, e.getMessage());
        }
    }

    // ============ Web 前端兼容别名 ============

    /** GET /api/profile —— /api/user/profile 的别名，参数与返回值相同。 */
    @GetMapping("/api/profile")
    public ProfileResponse profileAlias(@RequestParam("username") String username) {
        return getProfile(username);
    }

    /** PUT /api/profile —— /api/user/profile（更新资料）的别名，参数与返回值相同。 */
    @PutMapping("/api/profile")
    public ProfileResponse profileUpdateAlias(@RequestBody ProfileResponse p) {
        return updateProfile(p);
    }

    /**
     * POST /api/upload —— Web 前端通用文件上传（multipart/form-data）。
     *
     * @param username 可选；传入且用户存在时，同时把该文件设为用户头像
     * @param file 上传的文件
     * @return Map：url 为文件访问地址（/uploads/...）；失败时 url 为空字符串
     */
    @PostMapping("/api/upload")
    public Map<String, Object> uploadAlias(@RequestParam(value = "username", required = false) String username,
                                           @RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            // UUID 前缀避免重名
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(new File(dir, filename).getAbsoluteFile());
            String url = "/uploads/" + filename;
            // 仅当提供了 username 时才更新头像
            if (username != null && !username.isBlank()) {
                User u = userRepository.findByUsername(username).orElse(null);
                if (u != null) {
                    u.avatar = url;
                    userRepository.save(u);
                }
            }
            return Map.of("url", url);
        } catch (Exception e) {
            return Map.of("url", "");
        }
    }

}
