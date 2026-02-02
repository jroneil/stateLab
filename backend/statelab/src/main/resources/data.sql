-- Seed Data for State Lab
-- Clean start: optionally delete existing if you want to force fresh seeds
-- DELETE FROM work_order_events;
-- DELETE FROM work_orders;

-- Insert one Work Order in each major state to demonstrate the dashboard
INSERT INTO work_orders (id, title, description, state, version, created_at, updated_at) 
VALUES 
('11111111-1111-1111-1111-111111111111', 'Baseline Lab Order', 'Initial draft for system verification.', 'DRAFT', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'Active Review Request', 'An order that has been submitted for approval.', 'SUBMITTED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'Authorized Server Upgrade', 'Order cleared for implementation.', 'APPROVED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444444', 'Rejected Budget Proposal', 'Rejected due to missing cost analysis.', 'REJECTED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('55555555-5555-5555-5555-555555555555', 'Archived Work Record', 'Historically completed record.', 'COMPLETED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Initial Audit Events for visibility
INSERT INTO work_order_events (id, work_order_id, from_state, to_state, notes, action, occurred_at) 
VALUES
('a1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', NULL, 'DRAFT', 'Seeded via data.sql', 'CREATE', CURRENT_TIMESTAMP),
('a2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'DRAFT', 'SUBMITTED', 'Initial submission', 'SUBMIT', CURRENT_TIMESTAMP),
('a3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 'SUBMITTED', 'APPROVED', 'Approved by system', 'APPROVE', CURRENT_TIMESTAMP),
('a4444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 'SUBMITTED', 'REJECTED', 'Missing metrics', 'REJECT', CURRENT_TIMESTAMP),
('a5555555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 'APPROVED', 'COMPLETED', 'Final archival', 'COMPLETE', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
