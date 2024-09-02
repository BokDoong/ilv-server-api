package hanium.server.i_luv_book.domain.user.domain;

import hanium.server.i_luv_book.domain.user.application.dto.request.ParentCreateCommand;
import hanium.server.i_luv_book.domain.user.login.domain.LoginType;
import hanium.server.i_luv_book.global.common.basetime.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.mapstruct.ap.internal.model.GeneratedType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ijin
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter

//이거 임시로 해놓은 거임 지워야함
@Builder
@AllArgsConstructor
@ToString
public class Parent extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parent_id")
    private long id;
    @Column(unique = true, name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "membership_type")
    private MembershipType membershipType;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private LoginType loginType;
    @Column(name = "social_id")
    private String socialId;


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Child> children = new ArrayList<>();

    @Getter
    @RequiredArgsConstructor
    public enum MembershipType {
        FREE,             // 무료 회원
        PAID_BASIC,       // 유료 회원(1) - 자녀 3명 추가 가능
        PAID_STANDARD,    // 유료 회원(2) - 자녀 5명 추가 가능
        PAID_PREMIUM      // 유료 회원(3) - 자녀 무제한 추가 가능
    }

    @Builder
    public Parent(ParentCreateCommand parentCreateCommand) {
        this.email = parentCreateCommand.email();
        this.password = parentCreateCommand.password();
        this.membershipType = MembershipType.FREE;
        this.role = Role.ROLE_FREE;
    }

    public void updateMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }

    public void addChild(Child child) {
        children.add(child);
    }

    // 자식을 추가할 수 있는지 검증
    public boolean canAddChild(int currentNumberOfChildren) {
        return switch (membershipType) {
            case FREE -> {
                yield currentNumberOfChildren < 1;
            }
            case PAID_BASIC -> {
                yield currentNumberOfChildren < 3;
            }
            case PAID_STANDARD -> {
                yield currentNumberOfChildren < 5;
            }
            case PAID_PREMIUM -> {
                yield true;
            }
        };
    }
}
