package com.valeerp.inventory.stock;

import java.time.Instant;
import java.util.List;

public final class StockDashboardDtos {

    private StockDashboardDtos() {
    }

    public record QuantView(
            Long id,
            Long productId,
            String productSku,
            String productName,
            Long warehouseId,
            String warehouseCode,
            Long locationId,
            String locationCode,
            String locationKind,
            String lotNumber,
            int onHandQty,
            int reservedQty,
            int availableQty,
            String uom
    ) {
    }

    public record Summary(
            Instant asOf,
            int skuCount,
            int warehouseCount,
            int onHandUnits,
            int reservedUnits,
            int availableUnits,
            int lowStockAlertCount,
            int openPickingCount,
            int pendingCycleCountCount,
            List<QuantView> quants
    ) {
    }
}
