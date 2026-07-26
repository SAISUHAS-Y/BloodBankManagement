-- V5__add_stock_search_composite_index.sql
CREATE INDEX idx_stock_search_composite ON blood_stocks(blood_group_id, component_type_code, units_available);
CREATE INDEX idx_stock_group_units ON blood_stocks(blood_group_id, units_available);
