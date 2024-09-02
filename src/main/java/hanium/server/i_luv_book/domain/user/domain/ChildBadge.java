package hanium.server.i_luv_book.domain.user.domain;

import hanium.server.i_luv_book.global.common.basetime.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author ijin
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChildBadge extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "child_badge_id")
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Child child;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @Builder
    public ChildBadge(Child child, Badge badge) {
        this.child = child;
        this.badge = badge;
    }
}
