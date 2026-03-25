DASHBOARDS_SRC  := monitoring/dashboards
DASHBOARDS_HELM := helm/betamis-infra/files/dashboards

.PHONY: sync-dashboards check-dashboards

## Copy versioned dashboards into the Helm files directory.
## monitoring/dashboards/ is the single source of truth.
sync-dashboards:
	cp $(DASHBOARDS_SRC)/*.json $(DASHBOARDS_HELM)/

## Verify Helm dashboard copies are in sync with the source.
check-dashboards:
	@diff_output=$$(diff -rq $(DASHBOARDS_SRC) $(DASHBOARDS_HELM) 2>&1); \
	if [ -n "$$diff_output" ]; then \
		echo "ERROR: Helm dashboard copies are out of sync with $(DASHBOARDS_SRC):"; \
		echo "$$diff_output"; \
		echo "Run 'make sync-dashboards' to fix."; \
		exit 1; \
	fi
	@echo "Dashboards are in sync."
