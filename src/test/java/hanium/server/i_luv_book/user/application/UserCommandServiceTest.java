package hanium.server.i_luv_book.user.application;

import hanium.server.i_luv_book.exception.BusinessException;
import hanium.server.i_luv_book.exception.NotFoundException;
import hanium.server.i_luv_book.user.application.dto.UserCommandMapper;
import hanium.server.i_luv_book.user.application.dto.request.ChildCreateCommand;
import hanium.server.i_luv_book.user.application.dto.request.ParentCreateCommand;
import hanium.server.i_luv_book.user.domain.Child;
import hanium.server.i_luv_book.user.domain.Parent;
import hanium.server.i_luv_book.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserCommandMapper userCommandMapper;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    @DisplayName("부모 계정 회원가입 테스트")
    void register() {
        // Given
        ParentCreateCommand command = new ParentCreateCommand("name1", "password1");
        Parent parent = Parent.builder().parentCreateCommand(command).build();

        when(userCommandMapper.toParent(command)).thenReturn(parent);
        when(userRepository.save(parent)).thenReturn(1L);

        // When
        Long result = userCommandService.registerParent(command);

        // Then
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("부모 계정 찾지 못했을 때 발생하는 예외테스트")
    void parent_NotFound() {
        // Given
        long nonExistentParentId = 999L;

        // Mocking
        when(userRepository.findParentById(nonExistentParentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userCommandService.canAddChildAccount(nonExistentParentId));

        verify(userRepository, times(1)).findParentById(nonExistentParentId);
        verify(userRepository, never()).countChildrenByParentId(anyLong());
        verify(userRepository, never()).save(any(Child.class));
    }

    @Test
    @DisplayName("자식 계정 회원가입 테스트")
    void addChild_Success() {
        // Given
        Parent parent = Parent.builder().parentCreateCommand(new ParentCreateCommand("부모1", "비밀번호1")).build();
        ChildCreateCommand command = new ChildCreateCommand("자식1", LocalDate.now(), Child.Gender.MALE, null, parent.getId());
        Child child = Child.builder().childCreateCommand(command).parent(parent).build();

        // Mocking
        when(userRepository.findParentById(command.parentId())).thenReturn(Optional.of(parent));
        when(userCommandMapper.toChild(command, parent)).thenReturn(child);
        when(userRepository.save(any(Child.class))).thenReturn(1L);

        // When
        Long result = userCommandService.registerChild(command);

        // Then
        assertEquals(1L, result);
    }

    @Test
    @DisplayName("자식계정 추가 전 멤버쉽 제한 테스트")
    void addChild_LimitedMembership() {
        // Given
        Parent parent = Parent.builder()
                .parentCreateCommand(new ParentCreateCommand("부모1", "비밀번호1"))
                .build();

        // Mocking
        when(userRepository.findParentById(parent.getId())).thenReturn(Optional.of(parent));
        when(userRepository.countChildrenByParentId(parent.getId())).thenReturn(1); // 자식 제한 초과

        // When & Then
        assertThrows(BusinessException.class, () -> userCommandService.canAddChildAccount(parent.getId()));
    }
}
