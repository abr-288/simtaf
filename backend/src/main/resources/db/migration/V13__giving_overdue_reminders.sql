-- Relance unique quand une échéance planifiée est dépassée sans avoir été réalisée.
ALTER TABLE giving_installments ADD COLUMN overdue_notified_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_giving_installments_planned_due ON giving_installments(due_date) WHERE status = 'planned';
