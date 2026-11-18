package com.valeerp.mrp;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MrpSummaryService {

    private final BomRepository bomRepository;
    private final ManufacturingOrderRepository orderRepository;

    public MrpSummaryService(BomRepository bomRepository, ManufacturingOrderRepository orderRepository) {
        this.bomRepository = bomRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        int bomActive = 0;
        int bomRetired = 0;
        for (BillOfMaterials bom : bomRepository.findAllByOrderByCodeAsc()) {
            if (bom.getStatus() == BomStatus.ACTIVE) {
                bomActive++;
            } else if (bom.getStatus() == BomStatus.ARCHIVED || bom.isRetired()) {
                bomRetired++;
            }
        }
        int moDraft = 0;
        int moOpen = 0;
        int moDone = 0;
        int moCancelled = 0;
        for (ManufacturingOrder order : orderRepository.findAllByOrderByIdDesc()) {
            ManufacturingOrderStatus status = order.getStatus();
            if (status == ManufacturingOrderStatus.DRAFT) {
                moDraft++;
                moOpen++;
            } else if (status == ManufacturingOrderStatus.CONFIRMED || status == ManufacturingOrderStatus.IN_PROGRESS) {
                moOpen++;
            } else if (status == ManufacturingOrderStatus.DONE) {
                moDone++;
            } else if (status == ManufacturingOrderStatus.CANCELLED) {
                moCancelled++;
            }
        }
        return new Summary(bomActive, bomRetired, moDraft, moOpen, moDone, moCancelled, 0);
    }

    public record Summary(
            int bomActive,
            int bomRetired,
            int moDraft,
            int moOpen,
            int moDone,
            int moCancelled,
            int scrapEntries
    ) {
    }
}
