package com.valeerp.inventory.stock;

import com.valeerp.inventory.cyclecount.CycleCount;
import com.valeerp.inventory.cyclecount.CycleCountRepository;
import com.valeerp.inventory.cyclecount.CycleCountStatus;
import com.valeerp.inventory.picking.PickingRepository;
import com.valeerp.inventory.service.model.AlertStatus;
import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.inventory.service.model.PickingStatus;
import com.valeerp.inventory.service.repository.AlertRepository;
import com.valeerp.inventory.service.repository.InventoryItemRepository;
import com.valeerp.inventory.stock.StockDashboardDtos.QuantView;
import com.valeerp.inventory.stock.StockDashboardDtos.Summary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StockDashboardService {

    private static final String ON_HAND_LOCATION = "STOCK";

    private final InventoryItemRepository itemRepository;
    private final AlertRepository alertRepository;
    private final PickingRepository pickingRepository;
    private final CycleCountRepository cycleCountRepository;
    private final Clock clock;

    public StockDashboardService(InventoryItemRepository itemRepository,
                                 AlertRepository alertRepository,
                                 PickingRepository pickingRepository,
                                 CycleCountRepository cycleCountRepository,
                                 Clock clock) {
        this.itemRepository = itemRepository;
        this.alertRepository = alertRepository;
        this.pickingRepository = pickingRepository;
        this.cycleCountRepository = cycleCountRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        List<InventoryItem> items = itemRepository.findAll();
        Set<Long> skus = new HashSet<>();
        Set<Long> warehouses = new HashSet<>();
        int onHand = 0;
        int reserved = 0;
        int available = 0;
        List<QuantView> quants = new java.util.ArrayList<>();
        for (InventoryItem item : items) {
            int itemOnHand = item.getQuantity() == null ? 0 : item.getQuantity();
            int itemReserved = item.getReservedQuantity() == null ? 0 : item.getReservedQuantity();
            onHand += itemOnHand;
            reserved += itemReserved;
            available += item.availableQuantity();
            if (item.getProduct() != null) {
                skus.add(item.getProduct().getId());
            }
            if (item.getWarehouse() != null) {
                warehouses.add(item.getWarehouse().getId());
            }
            quants.add(toQuant(item, itemOnHand, itemReserved));
        }
        int openPickings = pickingRepository.findByStatusOrderByCreatedAtDesc(PickingStatus.DRAFT).size()
                + pickingRepository.findByStatusOrderByCreatedAtDesc(PickingStatus.CONFIRMED).size();
        int pendingCounts = 0;
        for (CycleCount count : cycleCountRepository.findAllByOrderByCreatedAtDesc()) {
            if (count.getStatus() == CycleCountStatus.DRAFT || count.getStatus() == CycleCountStatus.IN_PROGRESS) {
                pendingCounts++;
            }
        }
        return new Summary(
                Instant.now(clock),
                skus.size(),
                warehouses.size(),
                onHand,
                reserved,
                available,
                alertRepository.findByStatusOrderBySeverityAscCreatedAtDesc(AlertStatus.ACTIVE).size(),
                openPickings,
                pendingCounts,
                quants
        );
    }

    private QuantView toQuant(InventoryItem item, int onHand, int reserved) {
        Long productId = item.getProduct() == null ? null : item.getProduct().getId();
        String sku = item.getProduct() == null ? "" : item.getProduct().getSku();
        String name = item.getProduct() == null ? "" : item.getProduct().getName();
        Long warehouseId = item.getWarehouse() == null ? null : item.getWarehouse().getId();
        String warehouseCode = item.getWarehouse() == null ? "" : item.getWarehouse().getCode();
        String lot = item.getLotNumber();
        return new QuantView(
                item.getId(),
                productId,
                sku,
                name,
                warehouseId,
                warehouseCode,
                item.getInternalLocationId(),
                item.getInternalLocationId() == null ? "" : item.getInternalLocationId().toString(),
                ON_HAND_LOCATION,
                lot == null || "-".equals(lot) ? null : lot,
                onHand,
                reserved,
                item.availableQuantity(),
                ""
        );
    }
}
