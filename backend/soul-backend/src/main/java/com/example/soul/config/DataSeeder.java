package com.example.soul.config;

import com.example.soul.entity.Friend;
import com.example.soul.entity.Match;
import com.example.soul.entity.Message;
import com.example.soul.entity.User;
import com.example.soul.repository.FriendRepository;
import com.example.soul.repository.MatchRepository;
import com.example.soul.repository.MessageRepository;
import com.example.soul.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 首次启动时向 MySQL 灌入内置账户 / 匹配关系 / 示例消息（幂等：users 表非空则跳过）。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final FriendRepository friendRepository;

    public DataSeeder(UserRepository userRepository, MessageRepository messageRepository, MatchRepository matchRepository,
                      FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.matchRepository = matchRepository;
        this.friendRepository = friendRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        seedUser("demo", "김철수", "Java,Spring Boot", "Python,디자인", "남성", 25, "대한민국", 4.8, 12);
        seedUser("sarah", "Sarah Kim", "한국어,영어", "Python,디자인", "여성", 24, "대한민국", 4.9, 12);
        seedUser("alex", "Alex Park", "Python,Java", "영어,일본어", "남성", 27, "대한민국", 4.7, 9);
        seedUser("chen", "Chen Wei", "중국어,영어", "Spring Boot,React", "남성", 26, "중국", 4.8, 15);
        seedUser("victoria", "Victoria Lee", "디자인,Figma", "UX,영어", "여성", 25, "미국", 4.6, 8);
        seedUser("tom", "Tom Chen", "영어,프랑스어", "한국어", "남성", 28, "미국", 4.5, 6);
        seedUser("yuki", "Yuki Tanaka", "일본어,영어", "한국어,디자인", "여성", 23, "일본", 4.7, 10);
        seedUser("maria", "Maria Garcia", "스페인어,영어", "프로그래밍", "여성", 29, "스페인", 4.4, 5);
        seedUser("david", "David Brown", "프로그래밍,Python", "중국어", "남성", 30, "영국", 4.6, 7);
        seedUser("test1", "샹위", "중국어,영어", "Python", "여성", 24, "중국", 5.0, 1);
        seedUser("test2", "구샤오충", "Python,Java", "중국어", "남성", 25, "중국", 5.0, 1);
        seedUser("test3", "이호량", "한국어,영어", "디자인", "여성", 26, "대한민국", 5.0, 1);
        seedUser("test4", "라이샬", "일본어,영어", "Python", "남성", 27, "일본", 5.0, 1);
        seedUser("test5", "천위쉬안", "영어,프랑스어", "중국어", "여성", 28, "미국", 5.0, 1);
        // ===== 20 个随机用户（多国 + 随机技能） =====
        seedUser("emma", "Emma Wilson", "영어,프랑스어", "한국어,기타", "여성", 24, "미국", 4.7, 8);
        seedUser("jake", "Jake Miller", "Python,Java", "일본어", "남성", 26, "미국", 4.5, 5);
        seedUser("olivia", "Olivia Davis", "피아노,기타", "중국어", "여성", 23, "미국", 4.8, 11);
        seedUser("liam", "Liam Brown", "사진,디자인", "한국어", "남성", 28, "미국", 4.6, 7);
        seedUser("sophia", "Sophia Nguyen", "베트남어,영어", "일본어", "여성", 25, "베트남", 4.7, 9);
        seedUser("minh", "Minh Tran", "Python,Java", "영어", "남성", 24, "베트남", 4.6, 6);
        seedUser("linh", "Linh Pham", "그림,디자인", "한국어", "여성", 22, "베트남", 4.5, 4);
        seedUser("hana", "Hana Kim", "한국어,일본어", "영어", "여성", 25, "대한민국", 4.9, 13);
        seedUser("jisoo", "Jisoo Park", "기타,피아노", "중국어", "여성", 23, "대한민국", 4.8, 10);
        seedUser("minho", "Minho Lee", "Java,Spring Boot", "영어", "남성", 27, "대한민국", 4.7, 8);
        seedUser("yuna", "Yuna Choi", "바이올린,피아노", "베트남어", "여성", 26, "대한민국", 4.6, 7);
        seedUser("kenji", "Kenji Sato", "일본어,영어", "한국어", "남성", 25, "일본", 4.7, 9);
        seedUser("aiko", "Aiko Tanaka", "사진,디자인", "영어", "여성", 24, "일본", 4.6, 6);
        seedUser("wei", "Wei Zhang", "중국어,영어", "한국어", "남성", 26, "중국", 4.8, 12);
        seedUser("xia", "Xia Li", "React,프론트엔드", "일본어", "여성", 25, "중국", 4.7, 8);
        seedUser("lucas", "Lucas Silva", "포르투갈어,영어", "한국어", "남성", 27, "브라질", 4.5, 5);
        seedUser("ivan", "Ivan Petrov", "러시아어,영어", "중국어", "남성", 29, "러시아", 4.4, 4);
        seedUser("thuy", "Thuy Le", "베트남어,영어", "Python", "여성", 23, "베트남", 4.6, 6);
        seedUser("ngoc", "Ngoc Vu", "요리,베트남어", "한국어", "여성", 26, "베트남", 4.5, 5);
        seedUser("park", "Park Jiho", "드럼,기타", "영어", "남성", 24, "대한민국", 4.7, 7);

        match("demo", "sarah");
        match("demo", "alex");
        match("test1", "test2");
        match("test1", "test3");
        match("test1", "test4");
        match("test1", "test5");
        match("test2", "test3");
        match("test2", "test4");
        match("test2", "test5");
        match("test3", "test4");
        match("test3", "test5");
        match("test4", "test5");
        match("test1", "emma");
        match("test1", "hana");
        match("test1", "kenji");
        match("test1", "wei");
        match("test1", "minh");
        match("test2", "sophia");
        match("test2", "minho");
        match("test3", "jisoo");
        match("demo", "jake");
        match("demo", "olivia");
        match("demo", "yuna");

        // test1-5 互为好友 + demo 好友
        friend("test1", "test2");
        friend("test1", "test3");
        friend("test1", "test4");
        friend("test1", "test5");
        friend("test2", "test3");
        friend("test2", "test4");
        friend("test2", "test5");
        friend("test3", "test4");
        friend("test3", "test5");
        friend("test4", "test5");
        friend("demo", "sarah");
        friend("demo", "alex");

        msg("sarah", "demo", "안녕하세요! 스킬 교환 함께해요 😊");
        msg("demo", "sarah", "안녕하세요! 좋아요 😊");
        msg("alex", "demo", "Python 배우고 싶어요!");
        msg("test1", "test2", "안녕하세요! 테스트 준비 완료~");
        msg("test2", "test1", "네, 통화 테스트 해볼까요?");
        msg("test1", "test3", "语聊房测试，欢迎加入！");
        msg("test3", "test1", "好的，我来了");
        msg("emma", "test1", "Hi! I saw you teach Chinese. I'm learning Korean and Chinese, can we exchange? 😊");
        msg("test1", "emma", "当然可以！你想先从哪方面开始？");
        msg("emma", "test1", "Maybe pronunciation first? My tones are terrible lol");
        msg("hana", "test1", "저 한국어 가르쳐줄 수 있는데, 중국어 배우고 싶어요!");
        msg("test1", "hana", "좋아요! 같이 공부해요 😊");
        msg("kenji", "test1", "こんにちは！日本語と中国語の言語交換しませんか？");
        msg("wei", "test1", "你好，我在准备韩语考试，能帮帮我吗？");
        msg("minh", "test1", "Hey, I'm from Vietnam. Wanna trade English for Chinese?");
        msg("test1", "minh", "Sure thing! What's your level?");
        msg("sophia", "test2", "Hi! I teach Vietnamese, want to learn Japanese. Interested?");
        msg("minho", "test2", "자바 질문 있어요. Spring Boot에서 JPA 쓰는데 에러가 나요");
        msg("test2", "minho", "어떤 에러인지 알려주세요! 같이 봐요");
        msg("jake", "demo", "Hey, I saw your Spring Boot skills. I'm stuck on JPA relationships, mind helping?");
        msg("demo", "jake", "Sure! One-to-many or many-to-many?");
        msg("olivia", "demo", "I can teach piano and guitar. Would love to learn some Chinese!");
        msg("yuna", "demo", "바이올린 배우고 싶으세요? 저 피아노도 가르쳐요");
        msg("jisoo", "test3", "저 기타 치는데, 중국어 배우고 싶어요. 교환할래요?");
        msg("xia", "test1", "前端我也会一点，你在做什么项目呀？");
        msg("lucas", "test1", "Oi! I'm from Brazil, learning Korean. Can you help me with Chinese too?");
        msg("thuy", "test2", "Mình dạy tiếng Việt, muốn học Python, đổi không?");
        msg("ngoc", "test3", "我做的越南菜很好吃哦，想学韩语 😋");
        msg("ivan", "demo", "Привет! I can teach Russian, want to learn Chinese.");
        msg("aiko", "test4", "写真の撮り方、教えられますよ。英語勉強したいです");
        msg("park", "test5", "드럼 배워볼래요? 영어도 같이 연습해요!");
    }

    private void seedUser(String username, String name, String offer, String want, String gender, int age, String nation, double rating, int count) {
        User u = new User();
        u.username = username;
        u.password = "123456";
        u.name = name;
        u.email = username + "@soul.com";
        u.gender = gender;
        u.age = age;
        u.skillOffer = offer;
        u.skillWant = want;
        u.skillOfferLevel = "중급";
        u.skillWantLevel = "입문";
        u.nationality = nation;
        u.averageRating = rating;
        u.ratingCount = count;
        u.role = "USER";
        u.phone = "";
        u.address = nation;
        u.timeSlot = "평일 저녁";
        userRepository.save(u);
    }

    private void match(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        if (!matchRepository.existsByUserAAndUserB(x, y)) {
            Match m = new Match();
            m.userA = x;
            m.userB = y;
            matchRepository.save(m);
        }
    }

    private void friend(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        if (!friendRepository.existsByUserAAndUserB(x, y)) {
            Friend f = new Friend();
            f.userA = x;
            f.userB = y;
            friendRepository.save(f);
        }
    }

    private void msg(String sender, String receiver, String content) {
        Message m = new Message();
        m.senderUsername = sender;
        m.receiverUsername = receiver;
        m.content = content;
        m.type = "TEXT";
        m.mediaUrl = "";
        m.createdAt = Instant.now().toString();
        messageRepository.save(m);
    }
}
