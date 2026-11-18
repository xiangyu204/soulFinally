package com.example.soul.service;

import com.example.soul.entity.Block;
import com.example.soul.entity.Friend;
import com.example.soul.entity.Match;
import com.example.soul.repository.BlockRepository;
import com.example.soul.repository.FriendRepository;
import com.example.soul.repository.MatchRepository;
import com.example.soul.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** ?ⓩ댎阿뗩뿴?꾢뙶??/ 也썲룍 / ?됮퍚?녕내竊뚥빳?딁뵪?룟릫?↓챿??*/
@Service
public class RelationService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final FriendRepository friendRepository;
    private final BlockRepository blockRepository;

    public RelationService(UserRepository userRepository, MatchRepository matchRepository,
                           FriendRepository friendRepository, BlockRepository blockRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.friendRepository = friendRepository;
        this.blockRepository = blockRepository;
    }

    public void validateUsers(String sender, String receiver) {
        if (sender == null || sender.isBlank() || receiver == null || receiver.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        }
        if (sender.equals(receiver)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能和自己聊天");
        }
        if (!userRepository.existsByUsername(sender) || !userRepository.existsByUsername(receiver)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
    }

    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        }
        if (!userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
    }

    public void addMatch(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        if (!matchRepository.existsByUserAAndUserB(x, y)) {
            Match m = new Match();
            m.userA = x;
            m.userB = y;
            matchRepository.save(m);
        }
    }

    public boolean isMatched(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        return matchRepository.existsByUserAAndUserB(x, y);
    }

    public void addFriend(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        if (!friendRepository.existsByUserAAndUserB(x, y)) {
            Friend f = new Friend();
            f.userA = x;
            f.userB = y;
            friendRepository.save(f);
        }
    }

    public void removeFriend(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        friendRepository.deleteByUserAAndUserB(x, y);
    }

    public boolean isFriend(String a, String b) {
        String x = a.compareTo(b) <= 0 ? a : b;
        String y = a.compareTo(b) <= 0 ? b : a;
        return friendRepository.existsByUserAAndUserB(x, y);
    }

    public void addBlock(String blocker, String blocked) {
        if (!blockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            Block b = new Block();
            b.blocker = blocker;
            b.blocked = blocked;
            blockRepository.save(b);
        }
    }

    public void removeBlock(String blocker, String blocked) {
        blockRepository.deleteByBlockerAndBlocked(blocker, blocked);
    }

    public boolean isBlocked(String blocker, String blocked) {
        return blockRepository.existsByBlockerAndBlocked(blocker, blocked);
    }

    public boolean isBlockedEither(String a, String b) {
        return isBlocked(a, b) || isBlocked(b, a);
    }
}
