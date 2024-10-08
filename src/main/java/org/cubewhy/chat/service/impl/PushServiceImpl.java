package org.cubewhy.chat.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.vo.ChannelVO;
import org.cubewhy.chat.entity.vo.ChatMessageVO;
import org.cubewhy.chat.entity.vo.SenderVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.PushService;
import org.cubewhy.chat.service.SessionService;
import org.cubewhy.chat.util.RedisConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Log4j2
@Service
public class PushServiceImpl implements PushService {
    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Resource
    FirebaseMessaging firebaseMessaging;

    @Resource
    ChannelService channelService;

    @Resource
    SessionService sessionService;

    @Resource
    AccountService accountService;

    @Value("${spring.application.push.fcm.state}")
    private boolean fcmState;

    @Override
    @Transactional
    public void push(ChatMessage message) throws FirebaseMessagingException, IOException {
        Channel channel = channelService.findChannelById(message.getChannel());
        Account senderAccount = accountService.findAccountByIdNoExtra(message.getSender());
        ChannelUser channelUser = channelService.findChannelUser(channel, senderAccount);
        for (ChannelUser user : channel.getChannelUsers()) {
            Account account = user.getUser();
            if (fcmState) {
                String token = getToken(account.getId());
                if (token != null) {
                    Message fcmMessage = Message.builder()
                            .setToken(token)
                            .setNotification(Notification.builder()
                                    .setTitle(channel.getTitle())
                                    .setBody(message.getShortContent())
                                    .build())
                            .build();
                    firebaseMessaging.send(fcmMessage);
                }
            }
            // push via websockets
            WebSocketSession session = sessionService.getSession(account.getId());
            if (session != null) {
                SenderVO sender = new SenderVO();
                sender.setId(senderAccount.getId());
                sender.setUsername(senderAccount.getUsername());
                sender.setNickname(channelUser.getChannelNickname());
                session.sendMessage(new TextMessage(new WebSocketResponse<>(WebSocketResponse.NEW_MESSAGE, message.asViewObject(ChatMessageVO.class, (vo) -> {
                    vo.setChannel(channelService.findChannelById(message.getChannel()).asViewObject(ChannelVO.class));
                    vo.setSender(sender);
                })).toJson()));
            }
        }
    }

    @Override
    public String getToken(long accountId) {
        return redisTemplate.opsForValue().get(RedisConstants.FCM_TOKEN + accountId);
    }

    @Override
    public void updateFirebaseToken(Account account, String token) {
        log.info("New FCM token for account {}: {}", account.getName(), token);
        redisTemplate.opsForValue().set(RedisConstants.FCM_TOKEN + account.getId(), token);
    }
}
