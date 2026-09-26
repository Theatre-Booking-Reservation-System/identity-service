package com.theatre.identityservice.repository.spec;

import com.theatre.identityservice.repository.model.Patron;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Composable JPA {@link Specification}s for filtering patrons. Each factory
 * returns {@code null} when its filter value is absent, so callers can chain
 * them with {@link Specification#and(Specification)} and any {@code null}
 * fragments are ignored.
 */
public final class PatronSpecifications {

    private PatronSpecifications() {
    }

    /** Case-insensitive "contains" match on name. */
    public static Specification<Patron> nameContains(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String pattern = "%" + name.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    /** Case-insensitive "contains" match on email. */
    public static Specification<Patron> emailContains(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String pattern = "%" + email.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern);
    }

    /** Exact match on the loyalty-holder flag. */
    public static Specification<Patron> loyaltyHolderIs(Boolean loyaltyHolder) {
        if (loyaltyHolder == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("isLoyaltyHolder"), loyaltyHolder);
    }

    /** Exact match on status. */
    public static Specification<Patron> statusIs(Integer status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
