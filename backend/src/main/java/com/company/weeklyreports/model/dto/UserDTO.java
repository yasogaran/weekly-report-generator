package com.company.weeklyreports.model.dto;

import com.company.weeklyreports.model.entity.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Public-facing shape of a User — used both embedded in AuthResponse and as the row shape
 * for GET /users (api-doc.md notes passwordHash is never present in this or any response
 * shape; there is no path from User -> UserDTO that touches it — see mapper/UserMapper.java).
 * <p>
 * {@code @JsonProperty("isActive")} on the boolean field below isn't decorative: Lombok
 * generates {@code isActive()}/{@code setActive(boolean)} for a field named {@code isActive}
 * (verified directly against Jackson). Two things had to be true together, both confirmed
 * empirically, not assumed:
 * <ol>
 *   <li>Without {@code @JsonProperty}, a plain getter/setter pair serializes this as
 *       {@code "active"} and *rejects* an incoming {@code "isActive"} key entirely with
 *       UnrecognizedPropertyException — standard JavaBean introspection derives the
 *       property name from the accessor method name, not the field name.</li>
 *   <li>{@code @JsonProperty} on the field alone, with a getter but NO setter (this class's
 *       original shape — {@code @Getter @Builder}, no {@code @Setter}), doesn't fix that: it
 *       makes Jackson emit BOTH properties — {@code {"active":true,"isActive":true}} — since
 *       Jackson only merges the field-level annotation into the getter-derived property when
 *       a setter is also present to associate them. Hence {@code @Setter} below despite
 *       nothing in this codebase calling it (construction is always via {@code @Builder}).</li>
 * </ol>
 * The frontend's lib/userTypes.ts contract is {@code isActive} exactly — every boolean field
 * on every DTO in this codebase needs this same {@code @JsonProperty + @Setter} pairing,
 * this isn't a one-off fix.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;

    @JsonProperty("isActive")
    private boolean isActive;

    private LocalDateTime createdAt;
}
