package eu.dec21.appointme.users.groups.entity;

import eu.dec21.appointme.users.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "groups")
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @CreatedDate
    @Column(insertable = true, nullable = false)
    private LocalDateTime updatedAt;
}
