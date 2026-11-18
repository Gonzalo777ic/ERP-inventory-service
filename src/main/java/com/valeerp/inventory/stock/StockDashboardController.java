package com.valeerp.inventory.stock;

import com.valeerp.inventory.stock.StockDashboardDtos.Summary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/stock-dashboard")
public class StockDashboardController {

    private final StockDashboardService stockDashboardService;

    public StockDashboardController(StockDashboardService stockDashboardService) {
        this.stockDashboardService = stockDashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public Summary summary() {
        return stockDashboardService.summary();
    }
}
