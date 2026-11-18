/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.5.29-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: soul
-- ------------------------------------------------------
-- Server version	10.5.29-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `blocks`
--

DROP TABLE IF EXISTS `blocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blocks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blocked` varchar(64) NOT NULL,
  `blocker` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr6bst7i2b5wvnntgeukfyehm1` (`blocker`,`blocked`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blocks`
--

LOCK TABLES `blocks` WRITE;
/*!40000 ALTER TABLE `blocks` DISABLE KEYS */;
/*!40000 ALTER TABLE `blocks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_comments`
--

DROP TABLE IF EXISTS `board_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author` varchar(64) DEFAULT NULL,
  `content` varchar(2000) DEFAULT NULL,
  `date` varchar(64) DEFAULT NULL,
  `post_id` bigint(20) NOT NULL,
  `username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_comments`
--

LOCK TABLES `board_comments` WRITE;
/*!40000 ALTER TABLE `board_comments` DISABLE KEYS */;
INSERT INTO `board_comments` VALUES (1,'Sarah Kim','저도 참여하고 싶어요! 평일 저녁 8시면 가능합니다.','2026-10-21',1,'sarah'),(2,'Chen Wei','파이썬 자동화는 저도 관심 있어요. 같이 해요.','2026-10-21',1,'chen'),(3,'김철수','translate 같은 함수를 useCallback으로 감싸고 의존성 배열을 비우면 해결되는 경우가 많아요.','2026-10-21',2,'demo'),(4,'Alex Park','그리고 setState에 함수형 업데이트를 쓰면 리렌더가 줄어듭니다.','2026-10-21',2,'alex'),(5,'Yuki Tanaka','자료 공유 감사합니다! PDF 받고 싶어요.','2026-10-21',3,'yuki'),(6,'Maria Garcia','¡Muchas gracias! Me ayuda mucho.','2026-10-21',3,'maria'),(7,'김철수','주말 오후 괜찮으신가요? 저는 일본어 초보입니다.','2026-10-21',4,'demo'),(8,'Chen Wei','JPA 연관관계 부분 정말 도움됐어요. 감사합니다.','2026-10-21',5,'chen'),(9,'Sarah Kim','Te recomiendo ver series con subtítulos en español.','2026-10-21',6,'sarah'),(10,'Victoria Lee','I can help with UI feedback. Let\'s swap!','2026-10-21',7,'victoria'),(11,'Tom Chen','문제 정의를 먼저 보여주는 구성이 설득력 있었어요.','2026-10-21',8,'tom');
/*!40000 ALTER TABLE `board_comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `board_posts`
--

DROP TABLE IF EXISTS `board_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `avatar` varchar(512) DEFAULT NULL,
  `category` varchar(64) DEFAULT NULL,
  `comment_count` int(11) DEFAULT NULL,
  `content` varchar(4000) DEFAULT NULL,
  `date` varchar(64) DEFAULT NULL,
  `likes` int(11) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `title` varchar(512) DEFAULT NULL,
  `username` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_posts`
--

LOCK TABLES `board_posts` WRITE;
/*!40000 ALTER TABLE `board_posts` DISABLE KEYS */;
INSERT INTO `board_posts` VALUES (1,NULL,'자유게시판',2,'요즘 파이썬 자동화랑 자바 스프링을 같이 공부하고 있어요. 평일 저녁에 온라인으로 같이 공부하실 분 계실까요? 서로 막히는 부분 설명해주면서 하면 훨씬 빨리 늘 것 같아요.','2026-10-21',0,'김철수','스터디 메이트 구합니다 (Python / Java)','demo'),(2,NULL,'질문게시판',2,'컴포넌트에서 fetch 후 setState 하는데 useEffect가 계속 다시 돌아서 요청이 끝없이 나갑니다. 의존성 배열에 뭘 넣어야 하는지 감이 안 잡히네요. 경험 있으신 분 조언 부탁드립니다.','2026-10-21',0,'Chen Wei','React useEffect 무한 루프 어떻게 잡나요?','chen'),(3,NULL,'자료공유',2,'외국인 친구들이 한국어 배울 때 자주 막히는 부분(조사, 높임말, 발음 변화) 위주로 정리한 노트입니다. PDF로 묶어뒀으니 필요하신 분 댓글 남겨주세요.','2026-10-21',0,'Sarah Kim','한국어 학습 자료 정리해봤어요','sarah'),(4,NULL,'자유게시판',1,'도쿄에서 온 유키입니다. 일본어 가르쳐드리고 한국어나 중국어 배우고 싶어요. 주말에 화상통화로 30분씩 하면 좋을 것 같습니다.','2026-10-21',0,'Yuki Tanaka','일본어 회화 교환하실 분?','yuki'),(5,NULL,'자료공유',1,'스프링 부트로 첫 프로젝트 하면서 정리한 내용입니다. JPA 연관관계, 트랜잭션, 예외 처리 부분이 특히 헷갈렸는데 예제 코드랑 같이 정리해봤어요.','2026-10-21',0,'Alex Park','Spring Boot 초보자 정리 노트','alex'),(6,NULL,'질문게시판',1,'Estoy aprendiendo español y quiero mantener una rutina diaria. ¿Qué recursos o series recomiendan para escuchar todos los días?','2026-10-21',0,'Maria Garcia','¿Cómo practicar español todos los días?','maria'),(7,NULL,'자유게시판',1,'I build front-end apps with React and I want to improve my visual design. Looking for a designer who wants to exchange design feedback for coding help.','2026-10-21',0,'Tom Chen','Looking for a UI design partner','tom'),(8,NULL,'질문게시판',1,'UX 포트폴리오 준비 중인데 케이스 스터디 구성이 고민입니다. 문제 정의부터 결과까지 어떤 순서로 보여주는 게 좋을까요?','2026-10-21',0,'Victoria Lee','포트폴리오 피드백 부탁드립니다','victoria');
/*!40000 ALTER TABLE `board_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_ratings`
--

DROP TABLE IF EXISTS `chat_ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_ratings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `score` int(11) DEFAULT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_ratings`
--

LOCK TABLES `chat_ratings` WRITE;
/*!40000 ALTER TABLE `chat_ratings` DISABLE KEYS */;
INSERT INTO `chat_ratings` VALUES (1,1,5,'demo');
/*!40000 ALTER TABLE `chat_ratings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_requests`
--

DROP TABLE IF EXISTS `friend_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` varchar(64) DEFAULT NULL,
  `from_user` varchar(64) NOT NULL,
  `message` varchar(256) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `to_user` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_requests`
--

LOCK TABLES `friend_requests` WRITE;
/*!40000 ALTER TABLE `friend_requests` DISABLE KEYS */;
INSERT INTO `friend_requests` VALUES (1,'2026-10-17T13:08:46.190239948Z','test1',NULL,'PENDING','demo'),(2,'2026-10-17T14:13:14.851309122Z','test5',NULL,'PENDING','chen'),(3,'2026-10-17T17:32:29.584771835Z','test5',NULL,'PENDING','sarah'),(4,'2026-10-17T18:46:15.281244940Z','test1','11111','ACCEPTED','test5'),(5,'2026-10-18T02:15:13.547669136Z','test1','111','ACCEPTED','test5'),(6,'2026-10-19T13:47:11.956201805Z','test2','bro🤓👆','ACCEPTED','test5'),(7,'2026-10-19T16:04:31.153624680Z','test2','676767','REJECTED','test5'),(8,'2026-10-19T16:05:01.880733385Z','test2','bro😋','ACCEPTED','test5'),(9,'2026-10-27T05:07:35.933459703Z','demo','1234','ACCEPTED','test5'),(10,'2026-10-27T05:25:11.316779726Z','Test6','1234','ACCEPTED','test5'),(11,'2026-10-27T05:56:34.323479701Z','XIANGYU','','ACCEPTED','xy'),(12,'2026-10-27T06:07:24.153992406Z','XIANGYU','','ACCEPTED','xy'),(13,'2026-10-27T10:21:32.837620153Z','836390197','8888','REJECTED','test5'),(14,'2026-10-27T12:03:04.823801713Z','test1011','111','ACCEPTED','test5'),(15,'2026-10-28T06:27:03.268494043Z','test1','','PENDING','lhl'),(16,'2026-10-29T03:26:04.869194288Z','test1','gjgjhkjhk','PENDING','sophia'),(17,'2026-10-29T09:54:40.993703070Z','test1','','PENDING','linh');
/*!40000 ALTER TABLE `friend_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friends`
--

DROP TABLE IF EXISTS `friends`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `friends` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usera` varchar(64) NOT NULL,
  `userb` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKf44trcs80x4ciu6clvo2gvtic` (`usera`,`userb`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friends`
--

LOCK TABLES `friends` WRITE;
/*!40000 ALTER TABLE `friends` DISABLE KEYS */;
INSERT INTO `friends` VALUES (12,'alex','demo'),(11,'demo','sarah'),(17,'demo','test5'),(1,'test1','test2'),(3,'test1','test4'),(14,'test1','test5'),(21,'test1011','test5'),(5,'test2','test3'),(6,'test2','test4'),(16,'test2','test5'),(8,'test3','test4'),(10,'test4','test5'),(18,'Test6','test5'),(20,'XIANGYU','xy');
/*!40000 ALTER TABLE `friends` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_deletions`
--

DROP TABLE IF EXISTS `message_deletions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_deletions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `message_id` bigint(20) NOT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_deletions`
--

LOCK TABLES `message_deletions` WRITE;
/*!40000 ALTER TABLE `message_deletions` DISABLE KEYS */;
INSERT INTO `message_deletions` VALUES (1,86,'test5'),(2,85,'test5'),(3,91,'test1'),(4,90,'test1'),(5,91,'test5'),(6,90,'test5'),(7,89,'test5');
/*!40000 ALTER TABLE `message_deletions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_reads`
--

DROP TABLE IF EXISTS `message_reads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_reads` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `last_read_message_id` bigint(20) NOT NULL,
  `partner` varchar(64) NOT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_reads`
--

LOCK TABLES `message_reads` WRITE;
/*!40000 ALTER TABLE `message_reads` DISABLE KEYS */;
INSERT INTO `message_reads` VALUES (1,102,'test1','test5'),(2,95,'test5','test2'),(3,95,'test2','test5'),(4,0,'test4','test2'),(5,0,'sarah','test2'),(6,5,'test1','test2'),(7,0,'test3','test2'),(8,102,'test5','test1'),(9,17,'sophia','test2'),(10,127,'demo','test5'),(11,42,'chen','test5'),(12,47,'test3','test5'),(13,82,'test4','test5'),(14,5,'test2','test1'),(15,78,'test4','test1'),(16,0,'sarah','test1'),(17,0,'sarah','test5'),(18,12,'hana','test1'),(19,78,'test1','test4'),(20,79,'test5','test4'),(21,0,'test2','test4'),(22,10,'emma','test1'),(23,0,'alex','test5'),(24,0,'emma','test5'),(25,0,'victoria','test5'),(26,7,'test3','test1'),(27,0,'demo','test1'),(28,13,'kenji','test1'),(29,14,'wei','test1'),(30,143,'Test6','test5'),(31,144,'XIANGYU','xy'),(32,148,'test5','test1011'),(33,149,'test1011','test5'),(34,0,'chen','test1'),(35,0,'tom','test1'),(36,158,'linh','test1');
/*!40000 ALTER TABLE `message_reads` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(4000) DEFAULT NULL,
  `created_at` varchar(64) DEFAULT NULL,
  `media_url` varchar(512) DEFAULT NULL,
  `recalled` bit(1) NOT NULL,
  `receiver_username` varchar(64) NOT NULL,
  `sender_username` varchar(64) NOT NULL,
  `type` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=159 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,'안녕하세요! 스킬 교환 함께해요 😊','2026-10-17T12:52:07.712320630Z','','\0','demo','sarah','TEXT'),(2,'안녕하세요! 좋아요 😊','2026-10-17T12:52:07.718001742Z','','\0','sarah','demo','TEXT'),(3,'Python 배우고 싶어요!','2026-10-17T12:52:07.720463657Z','','\0','demo','alex','TEXT'),(4,'안녕하세요! 테스트 준비 완료~','2026-10-17T12:52:07.722772273Z','','\0','test2','test1','TEXT'),(5,'네, 통화 테스트 해볼까요?','2026-10-17T12:52:07.731033405Z','','\0','test1','test2','TEXT'),(6,'语聊房测试，欢迎加入！','2026-10-17T12:52:07.734285604Z','','\0','test3','test1','TEXT'),(7,'好的，我来了','2026-10-17T12:52:07.739377917Z','','\0','test1','test3','TEXT'),(8,'Hi! I saw you teach Chinese. I\'m learning Korean and Chinese, can we exchange? 😊','2026-10-17T12:52:07.752465982Z','','\0','test1','emma','TEXT'),(9,'当然可以！你想先从哪方面开始？','2026-10-17T12:52:07.755439180Z','','\0','emma','test1','TEXT'),(10,'Maybe pronunciation first? My tones are terrible lol','2026-10-17T12:52:07.758948709Z','','\0','test1','emma','TEXT'),(11,'저 한국어 가르쳐줄 수 있는데, 중국어 배우고 싶어요!','2026-10-17T12:52:07.766453209Z','','\0','test1','hana','TEXT'),(12,'좋아요! 같이 공부해요 😊','2026-10-17T12:52:07.772426468Z','','\0','hana','test1','TEXT'),(13,'こんにちは！日本語と中国語の言語交換しませんか？','2026-10-17T12:52:07.776144581Z','','\0','test1','kenji','TEXT'),(14,'你好，我在准备韩语考试，能帮帮我吗？','2026-10-17T12:52:07.778516170Z','','\0','test1','wei','TEXT'),(15,'Hey, I\'m from Vietnam. Wanna trade English for Chinese?','2026-10-17T12:52:07.782983398Z','','\0','test1','minh','TEXT'),(16,'Sure thing! What\'s your level?','2026-10-17T12:52:07.785691786Z','','\0','minh','test1','TEXT'),(17,'Hi! I teach Vietnamese, want to learn Japanese. Interested?','2026-10-17T12:52:07.788962677Z','','\0','test2','sophia','TEXT'),(18,'자바 질문 있어요. Spring Boot에서 JPA 쓰는데 에러가 나요','2026-10-17T12:52:07.792952663Z','','\0','test2','minho','TEXT'),(19,'어떤 에러인지 알려주세요! 같이 봐요','2026-10-17T12:52:07.795932131Z','','\0','minho','test2','TEXT'),(20,'Hey, I saw your Spring Boot skills. I\'m stuck on JPA relationships, mind helping?','2026-10-17T12:52:07.799067860Z','','\0','demo','jake','TEXT'),(21,'Sure! One-to-many or many-to-many?','2026-10-17T12:52:07.807514316Z','','\0','jake','demo','TEXT'),(22,'I can teach piano and guitar. Would love to learn some Chinese!','2026-10-17T12:52:07.810531971Z','','\0','demo','olivia','TEXT'),(23,'바이올린 배우고 싶으세요? 저 피아노도 가르쳐요','2026-10-17T12:52:07.813621412Z','','\0','demo','yuna','TEXT'),(24,'저 기타 치는데, 중국어 배우고 싶어요. 교환할래요?','2026-10-17T12:52:07.817026363Z','','\0','test3','jisoo','TEXT'),(25,'前端我也会一点，你在做什么项目呀？','2026-10-17T12:52:07.819396045Z','','\0','test1','xia','TEXT'),(26,'Oi! I\'m from Brazil, learning Korean. Can you help me with Chinese too?','2026-10-17T12:52:07.825103139Z','','\0','test1','lucas','TEXT'),(27,'Mình dạy tiếng Việt, muốn học Python, đổi không?','2026-10-17T12:52:07.827740075Z','','\0','test2','thuy','TEXT'),(28,'我做的越南菜很好吃哦，想学韩语 😋','2026-10-17T12:52:07.832332598Z','','\0','test3','ngoc','TEXT'),(29,'Привет! I can teach Russian, want to learn Chinese.','2026-10-17T12:52:07.835011427Z','','\0','demo','ivan','TEXT'),(30,'写真の撮り方、教えられますよ。英語勉強したいです','2026-10-17T12:52:07.837269906Z','','\0','test4','aiko','TEXT'),(31,'드럼 배워볼래요? 영어도 같이 연습해요!','2026-10-17T12:52:07.839745453Z','','\0','test5','park','TEXT'),(32,'','2026-10-17T13:08:11.367661730Z','/uploads/b1889ad2-8576-493c-a859-e8e382d05593.jpg','','test5','test1','IMAGE'),(33,'','2026-10-17T13:08:13.960382934Z','/uploads/81e6a436-63a4-420a-babf-c2241615c0e6.jpg','','test5','test1','IMAGE'),(34,'睡了吗','2026-10-17T13:43:31.665076195Z','','\0','test1','test5','TEXT'),(35,'在干嘛呢','2026-10-17T13:43:39.507085456Z','','\0','test2','test5','TEXT'),(36,'收到请扣1','2026-10-17T13:43:53.369476373Z','','\0','test3','test5','TEXT'),(37,'oioi','2026-10-17T13:44:30.708295624Z','','\0','test4','test5','TEXT'),(38,'','2026-10-17T13:50:20.776863898Z','/uploads/dd8ad00b-2d9a-45d6-a94b-d66d743f8a1b.jpg','','test1','test5','IMAGE'),(39,'','2026-10-17T13:50:31.132311287Z','/uploads/3e119e92-6df1-4c6f-a5e0-250392d8d8cf.jpg','\0','test2','test5','IMAGE'),(40,'','2026-10-17T13:50:36.162710198Z','/uploads/e2acaf23-ba1a-4507-acc7-297afefbb8ee.jpg','\0','test3','test5','IMAGE'),(41,'','2026-10-17T13:50:41.401216186Z','/uploads/0d88fb87-1fd6-403d-9ffd-e690124ed113.jpg','\0','test4','test5','IMAGE'),(42,'11','2026-10-17T14:01:56.991625032Z','','\0','chen','test5','TEXT'),(43,'111','2026-10-17T15:29:55.243963316Z','','\0','test5','test2','TEXT'),(44,'ddd','2026-10-17T15:35:03.882338490Z','','\0','test5','test1','TEXT'),(45,'eee','2026-10-17T15:35:09.066846694Z','','\0','test5','test1','TEXT'),(46,'晕倒了','2026-10-17T15:44:53.146107162Z','','\0','test1','test5','TEXT'),(47,'😭','2026-10-17T15:45:00.992218751Z','','\0','test3','test5','TEXT'),(48,'😭','2026-10-17T15:45:08.963307895Z','','\0','test4','test5','TEXT'),(49,'missed','2026-10-17T16:25:47.573669127Z','VOICE','\0','sarah','demo','CALL'),(50,'missed','2026-10-17T16:29:20.572425043Z','VIDEO','\0','sarah','demo','CALL'),(51,'ended:25','2026-10-17T16:33:22.985524341Z','VOICE','\0','test1','test5','CALL'),(52,'ended:87','2026-10-17T16:34:57.276165729Z','VIDEO','\0','test1','test5','CALL'),(53,'ended:87','2026-10-17T16:34:57.843008703Z','VIDEO','\0','test1','test5','CALL'),(54,'missed','2026-10-17T16:35:12.632975414Z','VOICE','\0','test5','test1','CALL'),(55,'ended:50','2026-10-17T16:36:12.939487985Z','VIDEO','\0','test5','test1','CALL'),(56,'missed','2026-10-17T16:36:33.059292717Z','VOICE','\0','test1','test5','CALL'),(57,'ended:25','2026-10-17T16:37:07.025142897Z','VIDEO','\0','test1','test5','CALL'),(58,'missed','2026-10-17T16:37:13.058442458Z','VOICE','\0','test1','test5','CALL'),(59,'ended:48','2026-10-17T16:38:04.522486139Z','VOICE','\0','test1','test5','CALL'),(60,'ended:18','2026-10-17T16:38:32.622288659Z','VOICE','\0','test5','test1','CALL'),(61,'ended:39','2026-10-17T17:08:39.635484974Z','VOICE','\0','test5','test1','CALL'),(62,'ended:25','2026-10-17T17:09:11.930155743Z','VOICE','\0','test5','test1','CALL'),(63,'ended:25','2026-10-17T17:09:12.031398025Z','VOICE','\0','test5','test1','CALL'),(64,'ended:56','2026-10-17T17:10:14.112153709Z','VIDEO','\0','test5','test1','CALL'),(65,'111','2026-10-17T17:11:37.502992170Z','','\0','test1','test5','TEXT'),(66,'','2026-10-17T17:22:20.268582460Z','/uploads/e17059b6-5bc9-4936-b617-878ba386ad14.jpg','','test5','test1','IMAGE'),(67,'canceled','2026-10-17T17:52:31.152742755Z','VOICE','\0','test1','test5','CALL'),(68,'ended:1','2026-10-17T17:53:17.044736788Z','VOICE','\0','test1','test5','CALL'),(69,'ended:15','2026-10-17T17:53:38.723447163Z','VIDEO','\0','test1','test5','CALL'),(70,'ended:15','2026-10-17T17:54:00.014599480Z','VIDEO','\0','test1','test5','CALL'),(71,'ended:44','2026-10-17T17:55:07.943903453Z','VIDEO','\0','test1','test5','CALL'),(72,'ended:114','2026-10-17T18:48:46.589899819Z','VIDEO','\0','test1','test5','CALL'),(73,'canceled','2026-10-17T18:48:54.326246769Z','VIDEO','\0','test4','test1','CALL'),(74,'canceled','2026-10-17T18:48:55.384349097Z','VIDEO','\0','test1','test5','CALL'),(75,'ended:56','2026-10-17T18:50:08.984753698Z','VIDEO','\0','test1','test5','CALL'),(76,'canceled','2026-10-17T18:50:19.004151057Z','VIDEO','\0','test4','test1','CALL'),(77,'ended:34','2026-10-17T18:50:59.289134514Z','VIDEO','\0','test5','test1','CALL'),(78,'hi','2026-10-18T00:59:11.178615449Z','','\0','test1','test4','TEXT'),(79,'working','2026-10-18T00:59:28.956118952Z','','\0','test5','test4','TEXT'),(80,'ended:22','2026-10-18T02:14:07.310705291Z','VIDEO','\0','test5','test1','CALL'),(81,'canceled','2026-10-18T08:45:32.528778342Z','VIDEO','\0','test4','test5','CALL'),(82,'canceled','2026-10-18T08:45:34.657944579Z','VOICE','\0','test4','test5','CALL'),(83,'work','2026-10-19T14:03:59.048664622Z','','\0','test5','test1','TEXT'),(84,'ended:95','2026-10-19T14:10:08.707216840Z','VIDEO','\0','test1','test5','CALL'),(85,'好想你','2026-10-19T15:27:42.521784211Z','','\0','test5','test1','TEXT'),(86,'好想你。','2026-10-19T15:28:04.064311720Z','','\0','test5','test1','TEXT'),(87,'要不要一起吃饭','2026-10-19T15:29:29.856535134Z','','\0','test5','test1','TEXT'),(88,'oi','2026-10-19T15:47:17.316457624Z','','\0','test1','test5','TEXT'),(89,'','2026-10-19T15:47:22.662406625Z','/uploads/4f13154b-10ef-4694-8b49-9a48572404a8.jpg','\0','test1','test5','IMAGE'),(90,'67','2026-10-19T15:47:30.487461884Z','','\0','test5','test1','TEXT'),(91,'要不要一起吃饭','2026-10-19T15:47:45.281958324Z','','\0','test5','test1','TEXT'),(92,'ended:6','2026-10-19T15:48:07.751638342Z','VIDEO','\0','test1','test5','CALL'),(93,'ended:13','2026-10-19T15:48:35.153113002Z','VIDEO','\0','test1','test5','CALL'),(94,'ended:10','2026-10-19T15:48:57.928715388Z','VOICE','\0','test1','test5','CALL'),(95,'。','2026-10-19T16:04:09.592366827Z','','\0','test5','test2','TEXT'),(96,'canceled','2026-10-19T16:14:11.890254976Z','VOICE','\0','test1','test5','CALL'),(97,'','2026-10-19T16:14:16.526734061Z','/uploads/25eb29e7-7daa-4c7f-8320-c57e54325a73.jpg','\0','test1','test5','IMAGE'),(98,'ended:85','2026-10-19T16:37:28.642107366Z','VIDEO','\0','test5','test1','CALL'),(99,'ended:17','2026-10-19T16:37:59.241638631Z','VIDEO','\0','test1','test5','CALL'),(100,'','2026-10-20T01:30:48.400425585Z','/uploads/613b791f-45ae-4fde-88fb-3065211e2119.jpg','\0','test1','test5','IMAGE'),(101,'一起吃饭吗？','2026-10-20T01:31:05.541231840Z','','\0','test5','test1','TEXT'),(102,'ended:13','2026-10-20T01:31:39.627275594Z','VIDEO','\0','test1','test5','CALL'),(103,'🤖\n您好，您提到的“最近的对话”中，我目前只看到您这一条提问，没有更早的聊天记录可供参考，因此暂时无法指出其中难以理解的知识点。烦请您把想了解的那段对话内容贴出来，我会为您逐条解释。','2026-10-21T15:36:10.282889118Z','','\0','test2','demo','TEXT'),(104,'6767','2026-10-21T15:37:51.900897917Z','','\0','test5','demo','TEXT'),(105,'666','2026-10-21T15:42:24.522094268Z','','\0','demo','test5','TEXT'),(106,'canceled','2026-10-22T00:22:14.531297053Z','VIDEO','\0','sarah','demo','CALL'),(107,'','2026-10-22T00:22:49.166832302Z','/uploads/fb2246c7-ed7c-4e37-8404-ffff27356e5a.png','\0','sarah','demo','IMAGE'),(108,'','2026-10-22T00:23:51.577190368Z','/uploads/771a7a57-421e-4eb6-8cca-ef37ca34cb0c.png','\0','sarah','demo','IMAGE'),(109,'','2026-10-22T00:23:54.234916686Z','/uploads/19b9578a-2962-4bd5-b4d3-bfbc2c706b05.png','\0','sarah','demo','IMAGE'),(110,'missed','2026-10-22T00:24:31.628581936Z','VIDEO','\0','demo','sarah','CALL'),(111,'canceled','2026-10-22T00:25:09.970221036Z','VIDEO','\0','sarah','demo','CALL'),(112,'canceled','2026-10-22T00:25:15.772894153Z','VIDEO','\0','sarah','demo','CALL'),(113,'canceled','2026-10-22T00:25:22.066537945Z','VOICE','\0','sarah','demo','CALL'),(114,'canceled','2026-10-22T01:08:09.819846861Z','VOICE','\0','sarah','demo','CALL'),(115,'67','2026-10-22T01:15:59.009783473Z','','\0','test5','demo','TEXT'),(116,'bor','2026-10-22T01:16:05.896158459Z','','\0','demo','test5','TEXT'),(117,'canceled','2026-10-27T05:06:37.386360190Z','VOICE','\0','sarah','demo','CALL'),(118,'canceled','2026-10-27T05:06:43.689157838Z','VIDEO','\0','sarah','demo','CALL'),(119,'canceled','2026-10-27T05:06:45.587775638Z','VIDEO','\0','sarah','demo','CALL'),(120,'canceled','2026-10-27T05:06:53.901656869Z','VOICE','\0','sarah','demo','CALL'),(121,'canceled','2026-10-27T05:06:58.426772729Z','VIDEO','\0','sarah','demo','CALL'),(122,'🤖\n您好，看起来这段对话内容都是“canceled”，可能表示连续的取消记录。请问您需要我帮您整理这段对话，还是解释“canceled”的含义呢？','2026-10-27T05:08:35.845959103Z','','\0','sarah','demo','TEXT'),(123,'1111','2026-10-27T05:09:18.586428955Z','','\0','test5','demo','TEXT'),(124,'ended:2','2026-10-27T05:09:33.752128013Z','VOICE','\0','demo','test5','CALL'),(125,'ended:11','2026-10-27T05:09:52.703749117Z','VIDEO','\0','demo','test5','CALL'),(126,'111','2026-10-27T05:26:04.519723064Z','','\0','demo','test5','TEXT'),(127,'111','2026-10-27T05:26:22.278476725Z','','\0','demo','test5','TEXT'),(128,'111','2026-10-27T05:26:34.476976885Z','','\0','Test6','test5','TEXT'),(129,'canceled','2026-10-27T05:26:45.681461604Z','VOICE','\0','test5','Test6','CALL'),(130,'ended:5','2026-10-27T05:48:15.838491765Z','VOICE','\0','test5','Test6','CALL'),(131,'ended:17','2026-10-27T05:48:39.074652989Z','VIDEO','\0','test5','Test6','CALL'),(132,'ended:3','2026-10-27T05:51:01.657772236Z','VOICE','\0','test5','Test6','CALL'),(133,'111','2026-10-27T05:54:03.508834347Z','','\0','alex','xy','TEXT'),(134,'111','2026-10-27T06:05:41.658519262Z','','\0','xy','XIANGYU','TEXT'),(135,'🤖\n안녕하세요. 무엇을 도와드릴까요?','2026-10-27T06:06:04.428771951Z','','\0','xy','XIANGYU','TEXT'),(136,'你好','2026-10-27T06:06:10.729240257Z','','\0','xy','XIANGYU','TEXT'),(137,'🤖\n안녕하세요! 무엇을 도와드릴까요? 😊','2026-10-27T06:06:16.775916712Z','','\0','xy','XIANGYU','TEXT'),(138,'ended:7','2026-10-27T06:20:14.022783015Z','VOICE','\0','test5','Test6','CALL'),(139,'ended:12','2026-10-27T06:20:31.360019680Z','VIDEO','\0','test5','Test6','CALL'),(140,'ended:1','2026-10-27T06:21:01.157667996Z','VOICE','\0','test5','Test6','CALL'),(141,'ended:9','2026-10-27T06:21:17.498225988Z','VIDEO','\0','test5','Test6','CALL'),(142,'canceled','2026-10-27T06:46:34.639144837Z','VOICE','\0','alex','demo','CALL'),(143,'ended:16','2026-10-27T06:50:02.223300777Z','VIDEO','\0','test5','Test6','CALL'),(144,'hi','2026-10-27T07:02:11.017383665Z','','\0','xy','XIANGYU','TEXT'),(145,'你好','2026-10-27T12:03:37.542853835Z','','\0','test5','test1011','TEXT'),(146,'','2026-10-27T12:03:56.822392742Z','/uploads/bd3e77f2-687c-4ed0-9c8d-3df950afa374.jpg','\0','test5','test1011','IMAGE'),(147,'一起吃饭嘛？','2026-10-27T12:04:10.899558596Z','','\0','test5','test1011','TEXT'),(148,'ended:12','2026-10-27T12:04:34.716066756Z','VIDEO','\0','test5','test1011','CALL'),(149,'1111','2026-10-27T14:46:24.200682365Z','','\0','test1011','test5','TEXT'),(150,'sync-test-225042','2026-10-27T14:50:42.836131470Z','','\0','alex','demo','TEXT'),(151,'nichimei','2026-10-28T06:24:24.544379086Z','','\0','demo','lhl','TEXT'),(152,'java','2026-10-28T06:28:23.507284319Z','','\0','test2','test1','TEXT'),(153,'🤖\n안녕하세요. Java 관련해서 어떤 부분이 궁금하신가요? 학습, 오류 해결, 프로젝트 등 구체적으로 말씀해 주시면 도와드리겠습니다.','2026-10-28T06:28:30.150300590Z','','\0','test2','test1','TEXT'),(154,'11111','2026-10-28T07:38:49.315738224Z','','\0','minh','1111111','TEXT'),(155,'canceled','2026-10-28T13:26:42.933499554Z','VOICE','\0','demo','test5','CALL'),(156,'레이아웃 확인용 메시지','2026-10-28T14:40:30.297586151Z','','\0','alex','demo','TEXT'),(157,'확인했습니다. 깔끔하네요.','2026-10-28T14:40:30.874687735Z','','\0','demo','alex','TEXT'),(158,'月跑来','2026-10-29T09:54:29.010115187Z','','\0','linh','test1','TEXT');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `remarks`
--

DROP TABLE IF EXISTS `remarks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `remarks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `owner` varchar(64) NOT NULL,
  `remark` varchar(64) DEFAULT NULL,
  `target` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7u7go8ndmjgyfmw8xkbpef7wh` (`owner`,`target`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `remarks`
--

LOCK TABLES `remarks` WRITE;
/*!40000 ALTER TABLE `remarks` DISABLE KEYS */;
INSERT INTO `remarks` VALUES (1,'test5','测试','test1');
/*!40000 ALTER TABLE `remarks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_matches`
--

DROP TABLE IF EXISTS `user_matches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_matches` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usera` varchar(64) NOT NULL,
  `userb` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKje14twv4ff2cvtofmimxx8od9` (`usera`,`userb`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_matches`
--

LOCK TABLES `user_matches` WRITE;
/*!40000 ALTER TABLE `user_matches` DISABLE KEYS */;
INSERT INTO `user_matches` VALUES (40,'1111111','minh'),(2,'alex','demo'),(39,'alex','test1'),(31,'alex','test5'),(35,'alex','xy'),(41,'chen','test1'),(25,'chen','test5'),(21,'demo','jake'),(38,'demo','lhl'),(22,'demo','olivia'),(1,'demo','sarah'),(24,'demo','test1'),(27,'demo','test2'),(26,'demo','test5'),(23,'demo','yuna'),(13,'emma','test1'),(32,'emma','test5'),(14,'hana','test1'),(20,'jisoo','test3'),(15,'kenji','test1'),(45,'linh','test1'),(17,'minh','test1'),(19,'minho','test2'),(29,'sarah','test1'),(28,'sarah','test2'),(30,'sarah','test5'),(18,'sophia','test2'),(3,'test1','test2'),(4,'test1','test3'),(5,'test1','test4'),(6,'test1','test5'),(43,'test1','tom'),(42,'test1','victoria'),(16,'test1','wei'),(44,'test1','yuki'),(37,'test1011','test5'),(7,'test2','test3'),(8,'test2','test4'),(9,'test2','test5'),(10,'test3','test4'),(11,'test3','test5'),(12,'test4','test5'),(33,'test5','victoria'),(34,'Test6','test5'),(36,'XIANGYU','xy');
/*!40000 ALTER TABLE `user_matches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `average_rating` double DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `nationality` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `rating_count` int(11) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `skill_offer` varchar(512) DEFAULT NULL,
  `skill_offer_level` varchar(255) DEFAULT NULL,
  `skill_want` varchar(512) DEFAULT NULL,
  `skill_want_level` varchar(255) DEFAULT NULL,
  `time_slot` varchar(255) DEFAULT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'대한민국',25,NULL,4.8,'demo@soul.com','남성','김철수','대한민국','123456','',12,'USER','Java,Spring Boot','중급','Python,디자인','입문','평일 저녁','demo'),(2,'대한민국',24,NULL,4.9,'sarah@soul.com','여성','Sarah Kim','대한민국','123456','',12,'USER','한국어,영어','중급','Python,디자인','입문','평일 저녁','sarah'),(3,'대한민국',27,NULL,4.7,'alex@soul.com','남성','Alex Park','대한민국','123456','',9,'USER','Python,Java','중급','영어,일본어','입문','평일 저녁','alex'),(4,'중국',26,NULL,4.8,'chen@soul.com','남성','Chen Wei','중국','123456','',15,'USER','중국어,영어','중급','Spring Boot,React','입문','평일 저녁','chen'),(5,'미국',25,NULL,4.6,'victoria@soul.com','여성','Victoria Lee','미국','123456','',8,'USER','디자인,Figma','중급','UX,영어','입문','평일 저녁','victoria'),(6,'미국',28,NULL,4.5,'tom@soul.com','남성','Tom Chen','미국','123456','',6,'USER','영어,프랑스어','중급','한국어','입문','평일 저녁','tom'),(7,'Tokyo',25,NULL,4.7,'yuki@soul.com','Female','Sato Yuki','日本','123456','+81-90-1234',10,'USER','日本語,イラスト','중급','한국어,English','입문','평일 저녁','yuki'),(8,'스페인',29,NULL,4.4,'maria@soul.com','여성','Maria Garcia','스페인','123456','',5,'USER','스페인어,영어','중급','프로그래밍','입문','평일 저녁','maria'),(9,'영국',30,NULL,4.6,'david@soul.com','남성','David Brown','영국','123456','',7,'USER','프로그래밍,Python','중급','중국어','입문','평일 저녁','david'),(10,'중국',24,'/uploads/3c3a58fe-b730-4859-a9c5-b2e50af1873d_SOUL_1792427898907.jpg',5,'test1@soul.com','여성','샹위','중국','123456','',1,'USER','중국어,영어','중급','Python','입문','평일 저녁','test1'),(11,'중국',25,NULL,5,'test2@soul.com','남성','구샤오충','중국','123456','',1,'USER','Python,Java','중급','중국어','입문','평일 저녁','test2'),(12,'대한민국',26,NULL,5,'test3@soul.com','여성','이호량','대한민국','123456','',1,'USER','한국어,영어','중급','디자인','입문','평일 저녁','test3'),(13,'일본',27,NULL,5,'test4@soul.com','남성','라이샬','일본','123456','',1,'USER','일본어,영어','중급','Python','입문','평일 저녁','test4'),(14,'미국',28,'/uploads/f2d2d5c7-9ad5-4bce-950d-8cdf2d30f204_1000566674.jpg',5,'test5@soul.com','기타','천위쉬안','미국','123456','',1,'USER','영어,프랑스어','중급','중국어','입문','평일 저녁','test5'),(15,'London',28,NULL,4.7,'emma@soul.com','Female','Emma Wilson','United Kingdom','123456','+44-7700-9001',8,'USER','English,writing','고급','중국어,한국어','초급','평일 오후','emma'),(16,'미국',26,NULL,4.5,'jake@soul.com','남성','Jake Miller','미국','123456','',5,'USER','Python,Java','중급','일본어','입문','평일 저녁','jake'),(17,'미국',23,NULL,4.8,'olivia@soul.com','여성','Olivia Davis','미국','123456','',11,'USER','피아노,기타','중급','중국어','입문','평일 저녁','olivia'),(18,'미국',28,NULL,4.6,'liam@soul.com','남성','Liam Brown','미국','123456','',7,'USER','사진,디자인','중급','한국어','입문','평일 저녁','liam'),(19,'베트남',25,NULL,4.7,'sophia@soul.com','여성','Sophia Nguyen','베트남','123456','',9,'USER','베트남어,영어','중급','일본어','입문','평일 저녁','sophia'),(20,'베트남',24,NULL,4.6,'minh@soul.com','남성','Minh Tran','베트남','123456','',6,'USER','Python,Java','중급','영어','입문','평일 저녁','minh'),(21,'베트남',22,NULL,4.5,'linh@soul.com','여성','Linh Pham','베트남','123456','',4,'USER','그림,디자인','중급','한국어','입문','평일 저녁','linh'),(22,'대한민국',25,NULL,4.9,'hana@soul.com','여성','Hana Kim','대한민국','123456','',13,'USER','한국어,일본어','중급','영어','입문','평일 저녁','hana'),(23,'대한민국',23,NULL,4.8,'jisoo@soul.com','여성','Jisoo Park','대한민국','123456','',10,'USER','기타,피아노','중급','중국어','입문','평일 저녁','jisoo'),(24,'대한민국',27,NULL,4.7,'minho@soul.com','남성','Minho Lee','대한민국','123456','',8,'USER','Java,Spring Boot','중급','영어','입문','평일 저녁','minho'),(25,'대한민국',26,NULL,4.6,'yuna@soul.com','여성','Yuna Choi','대한민국','123456','',7,'USER','바이올린,피아노','중급','베트남어','입문','평일 저녁','yuna'),(26,'일본',25,NULL,4.7,'kenji@soul.com','남성','Kenji Sato','일본','123456','',9,'USER','일본어,영어','중급','한국어','입문','평일 저녁','kenji'),(27,'일본',24,NULL,4.6,'aiko@soul.com','여성','Aiko Tanaka','일본','123456','',6,'USER','사진,디자인','중급','영어','입문','평일 저녁','aiko'),(28,'중국',26,NULL,4.8,'wei@soul.com','남성','Wei Zhang','중국','123456','',12,'USER','중국어,영어','중급','한국어','입문','평일 저녁','wei'),(29,'중국',25,NULL,4.7,'xia@soul.com','여성','Xia Li','중국','123456','',8,'USER','React,프론트엔드','중급','일본어','입문','평일 저녁','xia'),(30,'Berlin',30,NULL,4.5,'lucas@soul.com','Male','Lucas Meyer','Deutschland','123456','+49-151-2345',5,'USER','Deutsch,Python','고급','중국어,디자인','중급','주말','lucas'),(31,'러시아',29,NULL,4.4,'ivan@soul.com','남성','Ivan Petrov','러시아','123456','',4,'USER','러시아어,영어','중급','중국어','입문','평일 저녁','ivan'),(32,'베트남',23,NULL,4.6,'thuy@soul.com','여성','Thuy Le','베트남','123456','',6,'USER','베트남어,영어','중급','Python','입문','평일 저녁','thuy'),(33,'베트남',26,NULL,4.5,'ngoc@soul.com','여성','Ngoc Vu','베트남','123456','',5,'USER','요리,베트남어','중급','한국어','입문','평일 저녁','ngoc'),(34,'대한민국',24,NULL,4.7,'park@soul.com','남성','Park Jiho','대한민국','123456','',7,'USER','드럼,기타','중급','영어','입문','평일 저녁','park'),(36,NULL,NULL,NULL,4,'1224@gmail.com',NULL,'xy',NULL,'1234',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'xy'),(37,NULL,NULL,NULL,4,'1234@gmail.com',NULL,'xiangyu',NULL,'1234',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'123'),(38,NULL,NULL,NULL,4,'836390197@qq.com',NULL,'haha',NULL,'123456',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'Test6'),(39,NULL,NULL,NULL,4,'458488@qq.com',NULL,'haha',NULL,'123456',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'test7'),(40,NULL,NULL,NULL,4,'1234444@gmail.com',NULL,'XIANGYU',NULL,'1234',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'XIANGYU'),(41,NULL,NULL,NULL,4,'8848@qq.com',NULL,'IOSKD',NULL,'123456',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'test10'),(42,NULL,NULL,NULL,4,'8366499@126.com',NULL,'hhhhhj',NULL,'123456',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'Junli'),(43,NULL,NULL,NULL,4,'181819@qq.com',NULL,'48848',NULL,'8188',NULL,0,'USER',NULL,NULL,NULL,NULL,NULL,'464646'),(44,'55',88,NULL,4,'8848@163.com','남성 / Male','8848','Philippines','884888','178448899',0,'USER',NULL,NULL,NULL,NULL,'09:00 - 12:00','8848'),(45,'',22,NULL,4,'836390197@gmail.com','남성 / Male','836390197','中国','123456','178444',0,'USER',NULL,NULL,NULL,NULL,'','836390197'),(46,NULL,11,NULL,4,'1@gmail.com','Female','name','中国','123456','111',0,'USER',NULL,NULL,NULL,NULL,'Weekend','name'),(47,NULL,20,NULL,4,'888888888@qq.com','남성','test001','中国','123456','0108888888',0,'USER',NULL,NULL,NULL,NULL,'주말','test001'),(48,'',21,'/uploads/9f3a738b-55b8-445e-b9c1-d25d35a5a6e8_1000567939.jpg',4,'10101@hotmail.com','남성','Test101','中国','Aa123456','0108888888',0,'USER',NULL,NULL,NULL,NULL,'주말','Test101'),(49,'',21,'/uploads/7f033859-8188-4c14-958c-7769a93a9dbb_1000567939.jpg',4,'1011@qq.com','남성','test1011','中国','101111','01011111111',0,'USER',NULL,NULL,NULL,NULL,'주말','test1011'),(50,'as',22,NULL,4,'7812408@gmail.com','남성 / Male','lhl','中国','123456','0100000',0,'USER',NULL,'',NULL,NULL,'09:00 - 12:00','lhl'),(51,NULL,22,NULL,4,'111111@gmail.com','남성 / Male','1111111','中国','111111','1101100101',0,'USER',NULL,NULL,NULL,NULL,NULL,'1111111'),(52,'서울',23,NULL,4,'minji@soul.com','Female','Kim Minji','대한민국','123456','010-2201-1101',0,'USER','Python,UI 디자인','중급','중국어,영어','입문','평일 저녁','minji'),(53,'부산',26,NULL,4,'junho@soul.com','Male','Park Junho','대한민국','123456','010-2202-1102',0,'USER','Java,기타','고급','영어,한국어 작문','초급','주말','junho'),(54,'대구',21,NULL,4,'sujin@soul.com','Female','Lee Sujin','대한민국','123456','010-2203-1103',0,'USER','한국어,번역','중급','일본어,사진','입문','평일 오전','sujin'),(55,'杭州',24,NULL,4,'haowen@soul.com','Male','Chen Haowen','中国','123456','138-0011-2201',0,'USER','중국어,프론트엔드','고급','한국어,디자인','중급','평일 저녁','haowen'),(56,'上海',22,NULL,4,'yiran@soul.com','Female','Wang Yiran','中国','123456','138-0011-2202',0,'USER','사진,영상 편집','중급','영어,한국어','입문','주말','yiran'),(57,'Bengaluru',27,NULL,4,'ravi@soul.com','Male','Ravi Kumar','India','123456','+91-98000-1122',0,'USER','Java,Data','고급','한국어,중국어','초급','평일 오후','ravi'),(58,'서울',29,NULL,4,'soyeon@soul.com','Female','Choi Soyeon','대한민국','123456','010-2204-1104',0,'USER','한국어,UX','고급','English,프로그래밍','중급','평일 저녁','soyeon'),(59,'上海',24,NULL,4.6,'lina_cn@soul.com','female','Li Na','中国','123456','',3,'USER','中文,写作','advanced','English,UI 设计','beginner','weekday_evening','lina_cn'),(60,'北京',27,NULL,4.7,'zhangyu@soul.com','male','Zhang Yu','中国','123456','',5,'USER','Python,数据分析','advanced','日语,摄影','beginner','weekend','zhangyu'),(61,'成都',22,NULL,4.5,'wangxin@soul.com','female','Wang Xin','中国','123456','',2,'USER','UI 设计,Figma','intermediate','English,摄影','beginner','weekday_afternoon','wangxin'),(62,'深圳',25,NULL,4.8,'liuhao@soul.com','male','Liu Hao','中国','123456','',7,'USER','Java,Spring Boot','advanced','English,React','intermediate','weekday_evening','liuhao'),(63,'杭州',23,NULL,4.6,'chenjing@soul.com','female','Chen Jing','中国','123456','',4,'USER','中文,日语','advanced','English,吉他','beginner','weekend','chenjing'),(64,'广州',29,NULL,4.4,'zhaolei@soul.com','male','Zhao Lei','中国','123456','',2,'USER','吉他,钢琴','intermediate','中文,UI 设计','beginner','weekday_evening','zhaolei'),(65,'南京',26,NULL,4.9,'sunyue@soul.com','female','Sun Yue','中国','123456','',9,'USER','English,翻译','advanced','中文,한국어','basic','weekday_afternoon','sunyue'),(66,'武汉',24,NULL,4.5,'zhouqi@soul.com','male','Zhou Qi','中国','123456','',3,'USER','React,前端开发','intermediate','Python,English','beginner','weekend','zhouqi'),(67,'西安',21,NULL,4.3,'hemin@soul.com','female','He Min','中国','123456','',1,'USER','摄影,视频剪辑','intermediate','English,UI 设计','beginner','weekday_morning','hemin'),(68,'天津',28,NULL,4.7,'guoyang@soul.com','male','Guo Yang','中国','123456','',6,'USER','Python,机器学习','advanced','English,日语','basic','weekday_evening','guoyang'),(69,'苏州',25,NULL,4.6,'mayi@soul.com','female','Ma Yi','中国','123456','',4,'USER','中文,写作','advanced','吉他,摄影','beginner','weekend','mayi'),(70,'厦门',27,NULL,4.8,'tangwei2@soul.com','female','Tang Wei','中国','123456','',8,'USER','English,写作','advanced','中文,数据分析','intermediate','weekday_evening','tangwei2'),(71,'长沙',23,NULL,4.5,'fangzhi@soul.com','male','Fang Zhou','中国','123456','',2,'USER','Java,算法','advanced','English,钢琴','beginner','weekday_afternoon','fangzhi'),(72,'San Francisco',25,NULL,4.8,'emily_us@soul.com','female','Emily Carter','美国','123456','',10,'USER','English,UI 设计','advanced','中文,Python','beginner','weekday_evening','emily_us'),(73,'New York',28,NULL,4.7,'mikejohn@soul.com','male','Michael Johnson','美国','123456','',7,'USER','Python,机器学习','advanced','English,UI 设计','basic','weekend','mikejohn'),(74,'Seattle',24,NULL,4.6,'sarahlee@soul.com','female','Sarah Lee','美国','123456','',5,'USER','React,前端开发','intermediate','中文,韩语','beginner','weekday_afternoon','sarahlee'),(75,'Austin',30,NULL,4.7,'davidkim@soul.com','male','David Kim','美国','123456','',6,'USER','Java,Spring Boot','advanced','日语,摄影','basic','weekday_evening','davidkim'),(76,'Los Angeles',22,NULL,4.5,'jesswu@soul.com','female','Jessica Wu','美国','123456','',3,'USER','摄影,视频剪辑','intermediate','English,中文','beginner','weekend','jesswu'),(77,'Chicago',26,NULL,4.4,'brianm@soul.com','male','Brian Miller','美国','123456','',2,'USER','吉他,音乐制作','intermediate','中文,韩语','beginner','weekday_evening','brianm'),(78,'Boston',27,NULL,4.9,'racheld@soul.com','female','Rachel Davis','美国','123456','',11,'USER','English,写作','advanced','西班牙语,UI 设计','basic','weekday_morning','racheld'),(79,'San Jose',29,NULL,4.8,'kevinz@soul.com','male','Kevin Zhang','美国','123456','',9,'USER','Python,数据分析','advanced','中文,English','intermediate','weekday_evening','kevinz'),(80,'Denver',23,NULL,4.5,'laurab@soul.com','female','Laura Brown','美国','123456','',4,'USER','UI 设计,Figma','intermediate','English,摄影','beginner','weekend','laurab'),(81,'Miami',31,NULL,4.6,'jamesw@soul.com','male','James Wilson','美国','123456','',6,'USER','React,Node.js','advanced','中文,西班牙语','basic','weekday_afternoon','jamesw'),(82,'Phoenix',24,NULL,4.7,'amandag@soul.com','female','Amanda Garcia','美国','123456','',7,'USER','西班牙语,English','advanced','中文,韩语','beginner','weekday_evening','amandag'),(83,'Portland',33,NULL,4.5,'peters@soul.com','male','Peter Smith','美国','123456','',5,'USER','产品管理,写作','advanced','中文,Python','basic','weekend','peters'),(84,'San Diego',26,NULL,4.8,'ninac@soul.com','female','Nina Chen','美国','123456','',8,'USER','中文,翻译','advanced','English,吉他','basic','weekday_morning','ninac'),(85,'London',27,NULL,4.7,'oliveruk@soul.com','male','Oliver Bennett','英国','123456','',6,'USER','English,写作','advanced','中文,UI 设计','basic','weekday_evening','oliveruk'),(86,'Manchester',25,NULL,4.6,'charlotteh@soul.com','female','Charlotte Hughes','英国','123456','',4,'USER','UI 设计,Figma','intermediate','English,摄影','beginner','weekday_afternoon','charlotteh'),(87,'Birmingham',24,NULL,4.8,'harryp@soul.com','male','Harry Price','英国','123456','',7,'USER','Python,数据分析','advanced','韩语,English','beginner','weekend','harryp'),(88,'Bristol',23,NULL,4.9,'ameliaw@soul.com','female','Amelia Wood','英国','123456','',10,'USER','English,翻译','advanced','中文,日语','beginner','weekday_morning','ameliaw'),(89,'Leeds',29,NULL,4.7,'jackt@soul.com','male','Jack Taylor','英国','123456','',8,'USER','Java,Spring Boot','advanced','中文,React','intermediate','weekday_evening','jackt'),(90,'Edinburgh',22,NULL,4.4,'islaw@soul.com','female','Isla Watson','英国','123456','',2,'USER','摄影,视频剪辑','intermediate','English,中文','beginner','weekend','islaw'),(91,'Glasgow',31,NULL,4.5,'georgec@soul.com','male','George Clark','英国','123456','',3,'USER','吉他,钢琴','intermediate','韩语,English','beginner','weekday_evening','georgec'),(92,'Oxford',26,NULL,4.8,'freyaa@soul.com','female','Freya Adams','英国','123456','',9,'USER','English,翻译','advanced','中文,UI 设计','basic','weekday_afternoon','freyaa'),(93,'Liverpool',28,NULL,4.6,'thomasr@soul.com','male','Thomas Reed','英国','123456','',5,'USER','React,前端开发','intermediate','Python,韩语','beginner','weekend','thomasr'),(94,'Brighton',24,NULL,4.5,'poppyw@soul.com','female','Poppy Wright','英国','123456','',4,'USER','韩语,English','intermediate','中文,摄影','beginner','weekday_morning','poppyw'),(95,'Cambridge',34,NULL,4.7,'arthurm@soul.com','male','Arthur Moore','英国','123456','',7,'USER','产品管理,写作','advanced','中文,数据分析','basic','weekday_evening','arthurm'),(96,'Cardiff',21,NULL,4.6,'eviehj@soul.com','female','Evie Hall','英国','123456','',3,'USER','中文,日语','intermediate','English,钢琴','beginner','weekend','eviehj'),(97,'서울',23,NULL,4.9,'jiwoo_k@soul.com','female','Kim Jiwoo','韩国','123456','',12,'USER','한국어,翻译','advanced','English,中文','basic','weekday_evening','jiwoo_k'),(98,'부산',26,NULL,4.8,'minjunp@soul.com','male','Park Minjun','韩国','123456','',9,'USER','Java,Spring Boot','advanced','English,한국어','intermediate','weekday_afternoon','minjunp'),(99,'서울',24,NULL,4.6,'seoyeonl@soul.com','female','Lee Seoyeon','韩国','123456','',5,'USER','UI 设计,Figma','intermediate','中文,日语','beginner','weekend','seoyeonl'),(100,'인천',28,NULL,4.7,'dohyunk@soul.com','male','Choi Dohyun','韩国','123456','',7,'USER','Python,数据分析','advanced','中文,English','basic','weekday_evening','dohyunk'),(101,'대구',22,NULL,4.8,'haeunj@soul.com','female','Jung Haeun','韩国','123456','',8,'USER','韩语,한국어','advanced','English,摄影','beginner','weekday_morning','haeunj'),(102,'대전',30,NULL,4.5,'junseokl@soul.com','male','Kang Junseok','韩国','123456','',4,'USER','吉他,音乐制作','intermediate','中文,English','beginner','weekend','junseokl'),(103,'광주',25,NULL,4.7,'yerinp@soul.com','female','Lim Yerin','韩国','123456','',6,'USER','摄影,视频剪辑','intermediate','English,中文','basic','weekday_afternoon','yerinp'),(104,'울산',27,NULL,4.6,'taeyang@soul.com','male','Song Taeyang','韩国','123456','',5,'USER','React,前端开发','intermediate','中文,Python','beginner','weekday_evening','taeyang'),(105,'서울',26,NULL,4.8,'sooyeonh@soul.com','female','Han Sooyeon','韩国','123456','',9,'USER','English,翻译','advanced','中文,한국어','basic','weekend','sooyeonh'),(106,'제주',29,NULL,4.7,'jaewoo@soul.com','male','Yoon Jaewoo','韩国','123456','',7,'USER','中文,韩语','advanced','English,钢琴','beginner','weekday_evening','jaewoo'),(107,'서울',21,NULL,4.5,'hyejin@soul.com','female','Seo Hyejin','韩国','123456','',3,'USER','韩语,English','intermediate','中文,UI 设计','beginner','weekday_morning','hyejin'),(108,'서울',32,NULL,4.9,'seungho@soul.com','male','Kim Seungho','韩国','123456','',10,'USER','产品管理,数据分析','advanced','中文,English','basic','weekday_evening','seungho');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'soul'
--

--
-- Dumping routines for database 'soul'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-05  9:00:16
