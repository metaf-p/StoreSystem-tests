package data.product;

import java.util.Set;
import java.util.UUID;

public final class SupplierCleanup {
    private final Set<UUID> suppliers;

    public SupplierCleanup(Set<UUID> suppliers) {
        this.suppliers = suppliers;
    }

    public Set<UUID> createdSuppliers() {
        return suppliers;
    }

    public void addSupplier(UUID supplierId) {
        suppliers.add(supplierId);
    }

    public boolean isEmpty() {
        return suppliers.isEmpty();
    }

    public void clear() {
        suppliers.clear();
    }
}
