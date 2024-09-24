package hanium.server.i_luv_book.domain.user.application;

import hanium.server.i_luv_book.domain.user.application.dto.UserCommandMapper;
import hanium.server.i_luv_book.domain.user.application.dto.request.ActivityInfoCreateCommand;
import hanium.server.i_luv_book.domain.user.application.dto.request.ChildCreateCommand;
import hanium.server.i_luv_book.domain.user.application.dto.request.NotificationInfoCreateCommand;
import hanium.server.i_luv_book.domain.user.domain.*;
import hanium.server.i_luv_book.domain.user.domain.notification.BadgeGrantedEvent;
import hanium.server.i_luv_book.domain.user.domain.notification.NotificationInfo;
import hanium.server.i_luv_book.global.exception.BusinessException;
import hanium.server.i_luv_book.global.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static hanium.server.i_luv_book.domain.user.domain.Badge.*;

/**
 * @author ijin
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserQueryService userQueryService;
    private final UserCommandMapper userCommandMapper;

    private final ApplicationEventPublisher publisher;;
    private final UserRepository userRepository;
    private final FileStore fileStore;

    // 자식 계정 가입
    public Long registerChild(ChildCreateCommand command, MultipartFile image) {
        Parent parent = findParent(command.parentId());
        checkIfChildNameAlreadyExists(command, parent);
        Child child = createChild(command, parent, image);

        return saveChild(parent, child);
    }

    // 자식 삭제
    public void deleteChild(Long parentId, String nickname) {
        userRepository.deleteChild(parentId, nickname);
    }

    // 비밀번호 변경
    public void changePassword(Long parentId, String password) {
        Parent parent = findParent(parentId);
        parent.updatePassword(password);
    }

    // 동화 읽은 시간 업데이트, 배지 부여
    public void updateFairytaleReadingDuration(long parentId, ActivityInfoCreateCommand info) {
        Child child = findChild(parentId, info.nickname());
        List<BadgeType> grantedBadgeTypes = child.updateFairytaleReadingInfo(info.minute());
        if (!grantedBadgeTypes.isEmpty()) {
            grantBadges(grantedBadgeTypes, child);
        }
    }

    // 퀴즈 푼 시간 업데이트, 배지 부여
    public void updateQuizSolvingDuration(long parentId, ActivityInfoCreateCommand info) {
        Child child = findChild(parentId, info.nickname());
        List<BadgeType> grantedBadgeTypes = child.updateQuizSolvingInfo(info.minute());
        if (!grantedBadgeTypes.isEmpty()) {
            grantBadges(grantedBadgeTypes, child);
        }
    }

    // 알림 정보 저장
    public void saveNotificationInfo(long parentId, NotificationInfoCreateCommand command) {
        Child child = findChild(parentId, command.nickname());
        NotificationInfo notificationInfo = createNotificationInfo(child.getId(), command);
        userRepository.save(notificationInfo);
    }

    // 알림 수신 동의, 미동의
    public boolean changeNotificationAgreement(long parentId, String nickname) {
        Child child = findChild(parentId, nickname);
        NotificationInfo notificationInfo = findNotificationInfo(child);
        return notificationInfo.updateIsNotified();
    }

    private NotificationInfo findNotificationInfo(Child child) {
        return userQueryService.findNotificationInfo(child.getId());
    }

    private NotificationInfo createNotificationInfo(long childId, NotificationInfoCreateCommand command) {
        return userCommandMapper.toNotificationInfo(childId, command.fcmToken());
    }

    private void grantBadges(List<BadgeType> grantedBadgeTypes, Child child) {
        grantedBadgeTypes.forEach(grantedBadgeType -> {
                Badge badge = userQueryService.findBadge(grantedBadgeType);
                ChildBadge childBadge = createChildBadge(child, badge);
                saveChildBadge(child, childBadge, badge);
            }
        );

        pushBadgeGrantedNotification(grantedBadgeTypes, child);
    }

    private void pushBadgeGrantedNotification(List<BadgeType> grantedBadgeTypes, Child child) {
        if (isNotificationAvailable(child)) {
            publisher.publishEvent(new BadgeGrantedEvent(grantedBadgeTypes, child.getId()));
        }
    }

    private boolean isNotificationAvailable(Child child) {
        return userQueryService.checkNotificationAgreement(child.getNickname());
    }

    private void saveChildBadge(Child child, ChildBadge childBadge, Badge badge) {
        child.addChildBadge(childBadge);
        badge.addChildBadge(childBadge);
        userRepository.save(childBadge);
    }

    private ChildBadge createChildBadge(Child child, Badge badge) {
        return userCommandMapper.toChildBadge(child, badge);
    }

    private void checkIfChildNameAlreadyExists(ChildCreateCommand command, Parent parent) {
        if (parent.hasChildWithName(command.nickname())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTED);
        }
    }

    private Child createChild(ChildCreateCommand command, Parent parent, MultipartFile image) {
        Child child = userCommandMapper.toChild(command, parent);
        uploadProfileImage(image, child);
        return child;
    }

    private void uploadProfileImage(MultipartFile image, Child child) {
        if (!image.isEmpty()) {
            String imageUrl = fileStore.upload(image);
            child.putProfileImage(imageUrl);
        }
    }

    private Long saveChild(Parent parent, Child child) {
        parent.addChild(child);
        return userRepository.save(child);
    }

    private Parent findParent(long parentId) {
        return userQueryService.findParent(parentId);
    }

    private Child findChild(long parentId, String nickname) {
        return userQueryService.findChild(parentId, nickname);
    }
}
